package horse

import gui.{Field, Editor, MainFrame}

import core.{Interpreter, FieldState}
import swing._
import swing.event.Key
import javax.swing.KeyStroke
import java.awt.event.{InputEvent, KeyEvent}

object Main extends SimpleSwingApplication {

    object Mode extends Enumeration {
        val Editing, Executing = Value
    }

    val rows = 10
    val cols = 10
    val state = new FieldState(rows, cols)
    val editor = new Editor
    val field = new Field(rows, cols, state)
    var interpreter = new Interpreter(editor.getOperators)

    val switchToEdit: Action = Action("Edit") {
        frame.setMenuBar(editMenu)
        editMenu.enabled = true
        executeMenu.enabled = false

        switchButton.action = switchToExecute
        switchButton.mnemonic = Key.Q

        editor.prepare()
    }

    val switchToExecute: Action = Action("Execute") {
        frame.setMenuBar(executeMenu)
        executeMenu.enabled = true
        editMenu.enabled = false
        
        switchButton.action = switchToEdit
        switchButton.mnemonic = Key.Q

        executeMenu.requestFocus()
        interpreter = new Interpreter(editor.getOperators)
        editor.highlightOperator(interpreter.currentLine, editor.ProgramState.Normal)
    }

    val switchButton = new Button

    val frame = new MainFrame(editor, field, switchButton)

    import gui.MenuUtils._

    val editMenu: MenuBar = new MenuBar {
//        contents += createMenu("Operator",
//            createMenuItem("Step", KeyStroke.getKeyStroke('S'), editor.step()),
//            createMenuItem("Jump", KeyStroke.getKeyStroke('J'), editor.jump()),
//            createMenuItem("Turn left", KeyStroke.getKeyStroke('L'), editor.turnLeft()),
//            createMenuItem("Turn right", KeyStroke.getKeyStroke('R'), editor.turnRight())
//        )

        contents += createMenuItem("Step", KeyStroke.getKeyStroke('S'), editor.step())
        contents += new Separator
        contents += createMenuItem("Jump", KeyStroke.getKeyStroke('J'), editor.jump())
        contents += new Separator
        contents += createMenuItem("Turn left", KeyStroke.getKeyStroke('L'), editor.turnLeft())
        contents += new Separator
        contents += createMenuItem("Turn right", KeyStroke.getKeyStroke('R'), editor.turnRight())
        contents += new Separator
        contents += createMenuItem("If", KeyStroke.getKeyStroke("I"), editor.createIf())
        contents += new Separator
        contents += createMenuItem("Else", KeyStroke.getKeyStroke("E"), editor.createElse())
        contents += new Separator
        contents += createMenuItem("While", KeyStroke.getKeyStroke("W"), editor.createWhile())
        contents += new Separator
        contents += createMenuItem("Not", KeyStroke.getKeyStroke("N"), editor.inverse())

        enabled = false
    }

    import core.operator.{Step, Jump, TurnLeft, TurnRight}

    val executeMenu: MenuBar = new MenuBar {
        contents += createMenu("Execute",
            createMenuItem("Step", KeyStroke.getKeyStroke('S'), { state(Step); field.repaint() }),
            createMenuItem("Jump", KeyStroke.getKeyStroke('J'), { state(Jump); field.repaint() }),
            createMenuItem("Turn left", KeyStroke.getKeyStroke('L'), { state(TurnLeft); field.repaint() }),
            createMenuItem("Turn right", KeyStroke.getKeyStroke('R'), { state(TurnRight); field.repaint() }),
            new Separator,
            createMenuItem("Clear field", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true), {
                state.reset()
                field.repaint()
            })
        )

        import editor.ProgramState._

        contents += createMenu("Run",
            createMenuItem("Move", KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0, false), {
                val res = interpreter.step(state)
                if (res) {
                    field.repaint()
                    editor.highlightOperator(interpreter.currentLine, if (interpreter.isStopped) End else Normal)
                } else {
                    editor.highlightOperator(interpreter.currentLine, Error)
                }
            }),
            createMenuItem("Run...", KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0, true), {
                val res = interpreter.run(state)
                field.repaint()
                editor.highlightOperator(interpreter.currentLine, res match {
                    case Interpreter.Result.Success => End
                    case _ => Error
                })
            }),
            new Separator,
            createMenuItem("Restart", KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.CTRL_MASK, true), {
                interpreter.restart()
                editor.highlightOperator(interpreter.currentLine, editor.ProgramState.Normal)
            })
        )
    }

    switchToExecute()

    override def top = frame
}
