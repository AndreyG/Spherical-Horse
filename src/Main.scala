import scala.swing.{Label, SimpleSwingApplication, TextArea}
import scala.swing.event.Key
import java.awt.Color

import gui.{MainFrame, Editor, Field, StatusLine}
import core.{Interpretator, FieldState}

object Main extends SimpleSwingApplication {

    object Mode extends Enumeration {
        val Editing, Executing = Value
    }

    var mode = Mode.Executing

    val rows = 10
    val cols = 10
    val state = new FieldState(rows, cols)
    val editor = new Editor(switchMode)
    val field = new Field(rows, cols, state)
    var interpretator = new Interpretator(editor.getOperators)

    val modeLine = new Label {
        background = Color.lightGray

        def setMode(mode: Mode.Value) = mode match {
            case Mode.Editing   => text = "editing"
            case Mode.Executing => text = "executing"
        }
    }

    val helpText = new TextArea {
        editable = false

        peer.setTabSize(3)
    }

    val frame = new MainFrame(editor, field, modeLine, helpText, 
        key => mode match {
            case Mode.Executing => processKey(key)
            case Mode.Editing   => editor.processKey(key)
        }
    )

    private def move(op: core.operator.SimpleOperator) {
        if (state(op)) {
            field.repaint()
            StatusLine.removeError()
        } else {
            StatusLine.raiseError()
        }
    }

    import core.operator.{Step, Jump, TurnLeft, TurnRight}

    private def processKey(key: Key.Value) = key match {
        case Key.S => move(Step)
        case Key.J => move(Jump)
        case Key.L => move(TurnLeft)
        case Key.R => move(TurnRight)
        case Key.Escape => state.reset(); field.repaint()
        case Key.F9 => {
            interpretator.run(state); field.repaint()
        }
        case Key.E => switchMode()
        case _ => ()
    }

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
            interpretator = new Interpretator(editor.getOperators)
        } else {
            editor.prepare()
        }
        updateHelpText(help(mode))
    }

    updateHelpText(help(mode))
    modeLine.setMode(mode)

    override def top = frame 
}
