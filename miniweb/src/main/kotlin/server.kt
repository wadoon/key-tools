package org.key_project.web

import de.uka.ilkd.key.api.KeYApi
import de.uka.ilkd.key.api.ProofApi
import de.uka.ilkd.key.api.ProofManagementApi
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.withContext
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.dom.document
import kotlinx.html.li
import kotlinx.html.stream.appendHTML
import kotlinx.html.ul
import org.slf4j.LoggerFactory
import java.io.File
import java.io.PrintWriter
import java.io.Writer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.io.path.createDirectories
import kotlin.system.measureTimeMillis
import io.ktor.server.plugins.cors.*
import kotlin.math.abs

val TIMEOUT_IN_SECONDS = System.getenv().getOrDefault("TIMEOUT", "60").toLong()
val TEMP_DIR: String = System.getenv().getOrDefault("TEMP_DIR", "miniweb/tmp")
val WEB_DIR: Path = Paths.get(System.getenv().getOrDefault("WEB_DIR", "miniweb/src/web")).toAbsolutePath()
val TEMP_PATH: Path = Paths.get(TEMP_DIR)

val compute = newFixedThreadPoolContext(4, "compute")

val LOGGER = LoggerFactory.getLogger(Server::class.java)


/**
 *
 * @author Alexander Weigl
 * @version 1 (14.05.19)
 */
object Server {
    @JvmStatic
    fun main(args: Array<String>) {
        TEMP_PATH.createDirectories()
        LOGGER.info("Temp folder: {}", TEMP_PATH)
        LOGGER.info("Web folder: {}", WEB_DIR)
        embeddedServer(
            Netty, port = 8080, module = Application::mainModule
        ).start(wait = true)
    }
}

fun Application.mainModule() {
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
    }

    install(CallLogging)
    install(AutoHeadResponse)
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondTextWriter(status = HttpStatusCode.InternalServerError) {
                write("500: ${cause.message}\n")
                cause.printStackTrace(PrintWriter(this))
            }
        }
    }



    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")

        static("/") {
            staticRootFolder = WEB_DIR.toFile()
            files(".")
            default("index.html")
        }

        post("/prepareInteractive") {
            val jmlInput = call.receiveText()
            if (jmlInput.isBlank()) {
                call.respondText(buildString {
                    appendHTML().div("error") {
                        +"No input provided."
                    }
                })
            } else {
                val env = startKey(jmlInput)
                val id = abs(Random().nextInt())
                managedEnvs[id] = env
                call.respondText(buildString {
                    appendHTML().div {
                        ul {
                            env.proofContracts.forEach {
                                li {
                                    a("/proof/$id/${it.name.hashCode()}") {
                                        +"Start proof: ${it.name}"
                                    }
                                }
                            }
                        }
                    }
                })
            }
        }

        get("/proof/{env}/{contract}/") {
            val id = call.parameters["env"]!!.toInt()
            val proofName = call.parameters["contract"]?.toInt()!!
            val env = managedEnvs[id]
            val contract = env!!.proofContracts.find { it.name.hashCode() == proofName }
            val proof = env.startProof(contract)
            managedProofs[proofName] = proof
            call.respondFile(WEB_DIR.toFile(), "interactive.html")
        }


        post("/run") {
            val jmlInput = call.receiveText()
            if (jmlInput.isBlank()) {
                val myDiv = document {
                    create.div("error") {
                        +"No input provided."
                    }
                }
                call.respondText(myDiv.textContent, ContentType.Text.Html, HttpStatusCode.OK)
            } else {
                call.respondTextWriter(ContentType.Text.Html) {
                    val w = this
                    withContext(compute) {
                        runKey(jmlInput, w)
                    }
                }
            }
        }

        trace { application.log.warn(it.buildText()) }

    }
}


val managedEnvs = hashMapOf<Int, ProofManagementApi>()
val managedProofs = hashMapOf<Int, ProofApi>()
fun startKey(input: String): ProofManagementApi {

    val folder = Files.createTempDirectory(TEMP_PATH, "keyquickweb")
    LOGGER.info("Run in folder $folder")
    val className = findClassName(input)
    val file = folder.resolve("$className.java")
    Files.createFile(file)
    Files.newBufferedWriter(file).use {
        it.write(input)
    }
    return KeYApi.loadProof(file.toFile())

}


fun runKey(input: String, writer: Writer) {
    val out = PrintWriter(writer)
    try {
        out.run {
            val folder = Files.createTempDirectory(TEMP_PATH, "keyquickweb")
            info("Run in folder $folder")
            val className = findClassName(input)
            val file = folder.resolve("$className.java")
            Files.createFile(file)
            Files.newBufferedWriter(file).use {
                it.write(input)
            }
            startKey(this, file.toFile())
        }
    } catch (e: Exception) {
        out.append("<div class=\"error\"><pre>")
        e.printStackTrace(out)
        e.printStackTrace()
        out.append("</pre></div>")
    }
}


fun findClassName(input: String): String {
    val p = "public class (\\w+)".toRegex()
    return p.find(input)?.groupValues?.get(1)
        ?: throw RuntimeException("Could not find the public class name. Used pattern: ${p.pattern}.")
}

/**
 * Spawn a second process in which key [Worker] runs.
 */
private fun startKey(out: PrintWriter, input: File, vararg options: String) {
    val javaHome = System.getProperty("java.home")
    val javaBin = "$javaHome${File.separator}bin${File.separator}java"
    val classpath =
        System.getProperty("java.class.path").splitToSequence(":").joinToString(":") { File(it).absolutePath }

    val className = Worker.javaClass.name

    val builder = ProcessBuilder(
        javaBin, "-cp", classpath, className, *options, input.absolutePath
    ).directory(input.parentFile).redirectErrorStream(true)

    println("Commands: ${builder.command()}")

    val ms = measureTimeMillis {
        val process = builder.start()
        val x = process.waitFor(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
        if (!x) {
            process.destroyForcibly()
            out.info("KeY exceeded timeout")
        }
        val exit = process.exitValue()
        if (exit != 0) {
            out.error("KeY exited with $exit")
        }
        val text = process.inputStream.bufferedReader().readText()
        out.println(text)
    }
    out.info("Request took: $ms milliseconds.")
}

private fun Writer.info(s: String) {
    append("<div class=\"info\">$s</div>")
}

private fun Writer.error(s: String) {
    append("<div class=\"error\">$s</div>")
}
