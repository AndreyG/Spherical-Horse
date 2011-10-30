package gui

import scala.swing.{BorderPanel, BoxPanel, Component, Orientation}
import scala.swing.event.{Key, KeyReleased}
import java.awt.{Color, Dimension}

class MainFrame(editor: Component, field: Component, 
                modeLine: Component, 
                helpText: Component, controller: Key.Value => Unit) 
            extends scala.swing.MainFrame {

    title = "Shperical Horse"
    
    private[this] val height = 600
    private[this] val fieldWidth = height
    private[this] val editorWidth = 400

    val panel = new BoxPanel(Orientation.Horizontal) {
        import BorderPanel.Position._

        val labelPanel = new BorderPanel {
            add(modeLine,   West)
            add(StatusLine, East)
        }

        labelPanel.minimumSize = new Dimension(editorWidth, 15)
        labelPanel.maximumSize = new Dimension(editorWidth, 15)

        helpText.minimumSize = new Dimension(editorWidth, 150)
        helpText.maximumSize = new Dimension(editorWidth, 150)

        val editorPanel = new BorderPanel {
            add(helpText,   North)
            add(editor,     Center)
            add(labelPanel, South)
        }

        editorPanel.minimumSize = new Dimension(editorWidth, height)
        editorPanel.maximumSize = new Dimension(editorWidth, height)

        field.minimumSize = new Dimension(fieldWidth, height)
        field.maximumSize = new Dimension(fieldWidth, height)

        contents += editorPanel
        contents += field
    }

    contents = panel 

    size = new Dimension(editorWidth + fieldWidth, height)
    resizable = false

    panel.requestFocus
    listenTo(panel.keys)

    reactions += {
        case KeyReleased(_, key, _, _) => controller(key)
    }
}
