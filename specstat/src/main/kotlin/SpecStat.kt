import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.*
import kotlin.streams.asSequence

/**
 *
 * @author Alexander Weigl
 * @version 1 (23.05.22)
 */
class SpecStat : CliktCommand() {
    val color by option("--color").enum<ColorMode>().default(ColorMode.AUTO)
    val guards by option(
        "-g", "--guard",
        help = "a comma separated list of JML features for the evaluation of conditional JML comments"
    ).multiple()

    val verbose by option("-v", "--verbose", help = "verbose output")
        .flag("--no-verbose")

    val debug by option("-d", "--debug", help = "more verbose output")
        .flag("--no-debug")


    val inputFile by argument("JAVA-KEY-FILE", help = "key, java or a folder")
        .multiple(true)

    val inputs by lazy {
        inputFile.flatMap {
            val path = Paths.get(it)
            if (!path.exists()) {

                emptyList<Path>()
            }

            if (path.isDirectory()) {
                Files.walk(path).asSequence()
                    .filter { p -> p.fileName.endsWith(".java") }
                    .toList()
            } else {
                listOf(path)
            }
        }
    }

    override fun run() {
        ConsoleLog.useColor = when (color) {
            ColorMode.YES -> true
            ColorMode.AUTO -> System.console() != null || System.getenv("GIT_PAGER_IN_USE") != null
            ColorMode.NO -> false
        }
        if (debug) {
            inputs.forEach {
                println("Found file: $it")
            }
        }
        inputs.forEach { process(it) }
    }

    private fun process(file: Path) = process(file.readText())

    private fun process(content: String) {
        val lexer = SmallJmlLexer(content).lex()

    }
}



enum class ColorMode { YES, NO, AUTO }

object ConsoleLog {
    const val ESC = 27.toChar()
    const val RED = 31
    const val GREEN = 32
    const val YELLOW = 33
    const val BLUE = 34
    const val MAGENTA = 35
    const val CYAN = 36
    const val WHITE = 37

    var useColor = true
    var verbose = true

    fun colorfg(s: Any, c: Int) = "$ESC[${c}m$s$ESC[0m"
    fun colorbg(s: Any, c: Int) = "$ESC[${c + 10}m$s$ESC[0m"

    fun error(message: String) = printm("[ERR ] $message", fg = RED)
    fun fail(message: String) = printm("[FAIL] $message", fg = WHITE, bg = RED)
    fun warn(message: String) = printm("[WARN] $message", fg = YELLOW)
    fun info(message: String) = printm("[FINE] $message", fg = BLUE)
    fun fine(message: String) = printm("[OK  ] $message", fg = GREEN)
    fun debug(message: String) =
        if (verbose) printm("[    ] $message", fg = GREEN) else Unit

    fun printm(message: String, fg: Int? = null, bg: Int? = null) {
        print("  ".repeat(currentPrintLevel))
        val m =
            when {
                useColor -> message
                fg != null && bg != null -> colorbg(colorfg(message, fg), bg)
                fg != null -> colorfg(message, fg)
                bg != null -> colorbg(message, bg)
                else -> message
            }
        println(m)
    }


    var currentPrintLevel = 0
    fun printBlock(message: String, f: () -> Unit) {
        info(message)
        currentPrintLevel++
        f()
        currentPrintLevel--
    }

}