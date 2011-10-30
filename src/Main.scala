import scala.swing._
import scala.swing.event.{Key, KeyPressed, KeyReleased}
import java.awt.{Color, Dimension}

object Main extends SimpleSwingApplication {

    object Mode extends Enumeration {
        val Editing, Executing = Value
    }

    var mode = Mode.Executing

    val rows = 10
    val cols = 10
    val state = new StateMachine(rows, cols)
    val editor = new Editor(() => mode == Mode.Editing, switchMode, processKey)
    val field = new Field(rows, cols, state)

    val modeLine = new Label {
        background = Color.lightGray

        def setMode(mode: Mode.Value) = mode match {
            case Mode.Editing => text = "editing"
            case Mode.Executing => text = "executing"
        }

        setMode(mode)
    }

    val statusLine = new Label {
        background = Color.lightGray
        
        def raiseError() {
            foreground = Color.red
            text = "impossible!"
        }

        def removeError() {
            text = ""
        }
    }

    def processKey(key: Key.Value): Unit = key match {
        case Key.S => {
           if (state(Step)) {
               field.repaint()
               statusLine.removeError()
           } else {
              statusLine.raiseError()
           }
        }
        case Key.J => {
           if (state(Jump)) {
               field.repaint()
               statusLine.removeError()
           } else {
              statusLine.raiseError()
           }
        }
        case Key.L => {
            state(TurnLeft)
            field.repaint()  
            statusLine.removeError()
        }
        case Key.R => {
            state(TurnRight)
            field.repaint()
            statusLine.removeError()
        }
        case Key.Escape => state.reset(); field.repaint()
        case Key.F9 => {
            val res = interpretator.exec(state)
            field.repaint()
            if (res == Interpretator.ReturnCode.error)
                statusLine.raiseError()
        }
        case Key.E => {
            editor.prepare()
            switchMode()
        }

        case _ => ()
    }

    var interpretator = new Interpretator(AST.Empty)

    val help = Map (
        Mode.Executing -> List (
            ("s", "step"),
            ("j", "jump"),
            ("l", "rotate left"),
            ("r", "rotate right"),
            ("e", "edit"),
            ("Esc", "restart"),
            ("F9", "run")
        ),
        Mode.Editing -> List (
            ("s", "step"),
            ("j", "jump"),
            ("l", "rotate left"),
            ("r", "rotate right"),
            ("i", "if"),
            ("e", "else"),
            ("w", "while"),
            ("n", "invert condition"),
            ("Esc", "executing")
        )
    )

    val helpText = new TextArea {
        editable = false

        peer.setTabSize(3)
    }

    def updateHelpText(keybindings: List[(String, String)]) {
        val sb = new StringBuilder
        for ((key, command) <- keybindings) {
            sb.append('\t').append(key).append("\t-\t").append(command).append('\n')
        }
        helpText.text = sb.substring(0, sb.length - 1)
    }

    def switchMode() {
        mode = Mode(1 - mode.id)
        modeLine.setMode(mode)
        if (mode == Mode.Executing) {
            interpretator = new Interpretator(editor.buildAST)
        }
        updateHelpText(help(mode))
    }

    updateHelpText(help(mode))

    override def top = new MainFrame {
        title = "Shperical Horse"
        
        val height = 600
        val fieldWidth = height
        val editorWidth = 400

        contents = {
            val panel = new BoxPanel(Orientation.Horizontal)
            val editorPanel = new BoxPanel(Orientation.Vertical)

            val labelPanel = new BorderPanel {
                add(modeLine, BorderPanel.Position.West)
                add(statusLine, BorderPanel.Position.East)
            }

            labelPanel.minimumSize = new Dimension(editorWidth, 15)
            labelPanel.maximumSize = new Dimension(editorWidth, 15)

            helpText.minimumSize = new Dimension(editorWidth, 150)
            helpText.maximumSize = new Dimension(editorWidth, 150)

            editorPanel.contents += helpText
            editorPanel.contents += editor
            editorPanel.contents += labelPanel

            editorPanel.minimumSize = new Dimension(editorWidth, height)
            editorPanel.maximumSize = new Dimension(editorWidth, height)

            field.minimumSize = new Dimension(fieldWidth, height)
            field.maximumSize = new Dimension(fieldWidth, height)

            panel.contents += editorPanel
            panel.contents += field
            panel
        }

        size = new Dimension(editorWidth + fieldWidth, height)
        resizable = false

        editor.requestFocus()
    }
}
