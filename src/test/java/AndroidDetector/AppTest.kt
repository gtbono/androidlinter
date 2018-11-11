package AndroidDetector

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class AppTest {

    @Before
    fun setUp() {

    }

    @Test
    fun testa_se_o_diretorio_do_app_existe() {
        val app = App()
        val diretorio = app.run()
    }

}