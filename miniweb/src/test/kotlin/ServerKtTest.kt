import io.ktor.client.request.*
import io.ktor.server.testing.*
import org.key_project.web.mainModule
import kotlin.test.Test

class ServerKtTest {

    @Test
    fun testGetProofEnvContract() = testApplication {
        application {
            mainModule()
        }
        client.get("/proof/{env}/{contract}").apply {
            TODO("Please write your test here")
        }
    }
}