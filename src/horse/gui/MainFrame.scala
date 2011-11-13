package horse.gui

import java.awt.Dimension
import swing._

import horse.Config

class MainFrame(editor: Component, field: Component, 
                executeMenu: MenuBar, editMenu: MenuBar,
                onEdit: => Unit, onExecute: => Unit) extends scala.swing.MainFrame {

    title = "Shperical Horse"

    private[this] val editorWidth   = Config.getInt("editor.width")
    private[this] val fieldWidth    = Config.getInt("field-image.width")
    private[this] val height        = Config.getInt("field-image.height")

    private[this] val button = new SwitchButton(this, executeMenu, editMenu, onEdit, onExecute)

    contents = new BoxPanel(Orientation.Horizontal) {
        import BorderPanel.Position._

        val editorPanel = new BorderPanel {
            add(button, South)
            add(
                new ScrollPane(
                    new BorderPanel {
                        add(editor, Center)
                    }) {
                        horizontalScrollBarPolicy = ScrollPane.BarPolicy.AsNeeded
                    },
                Center)
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

    button.toExecute()
}
