package horse.gui

import java.awt.Dimension
import swing._

class MainFrame(editor: Component, field: Component, button: Component)
    extends scala.swing.MainFrame {

    title = "Shperical Horse"

    private[this] val height = 600
    private[this] val fieldWidth = height
    private[this] val editorWidth = 400

    contents = new BoxPanel(Orientation.Horizontal) {
        import BorderPanel.Position._

        val editorPanel = new BorderPanel {
            add(button, South)
            add(editor, Center)
        }

        editorPanel.minimumSize = new Dimension(editorWidth, height)
        editorPanel.maximumSize = new Dimension(editorWidth, height)

        field.minimumSize = new Dimension(fieldWidth, height)
        field.maximumSize = new Dimension(fieldWidth, height)

        contents += editorPanel
        contents += field
    }

    size = new Dimension(editorWidth + fieldWidth, height)
    resizable = false

    def setMenuBar(bar: MenuBar) {
        menuBar = bar
        peer.validate()
    }
}