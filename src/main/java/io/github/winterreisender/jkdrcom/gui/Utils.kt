import java.awt.Component
import java.awt.Desktop
import java.awt.Dimension
import java.awt.FlowLayout
import java.net.URI
import javax.swing.*

fun String.isValidMacAddress() = this.matches(Regex("""([A-E,\d]{2}-?){5}([A-E,\d]{2})""",RegexOption.IGNORE_CASE))


// Bug: not work after jpackage
fun showNetWindow() {
    val url = "http://login.jlu.edu.cn/notice_win.php"

    JFrame("校园网之窗").apply {
        size = Dimension(600,540)
        JPanel().apply {
            layout = FlowLayout()
            alignmentX = Component.CENTER_ALIGNMENT
            size = Dimension(600,510)

            JButton("在浏览器中打开").apply {
                addActionListener {
                    Desktop.getDesktop().browse(URI(url))
                }
            }.also(::add)

            JEditorPane(url).apply {
                contentType = "text/html; charset=gb2312"
                size = Dimension(600,465)
                isEditable = false
            }.also(::add)
        }.also(::add)
        isVisible = true
    }

}