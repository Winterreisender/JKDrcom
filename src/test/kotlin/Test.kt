import io.github.winterreisender.jkdrcom.gui.Utils
import kotlin.test.Test
import io.github.winterreisender.webviewko.WebviewKo

internal class Test {
    @Test fun `Test showNetWindow`() {
        Utils.showNetWindow().join()
    }
}