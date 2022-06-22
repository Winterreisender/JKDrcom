import io.github.winterreisender.jkdrcom.gui.Utils
import kotlin.test.Test

internal class Test {
    @Test fun `Test showNetWindow`() {
        Utils.showNetWindow(closeAfterSecs = 5).join()
    }
}