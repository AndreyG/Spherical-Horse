package horse

import gui.{StatusLine, Field, Editor, MainFrame}

import core.{Interpreter, FieldState}
import swing._
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

        editor.prepare()
    }

    val switchToExecute: Action = Action("Execute") {
        frame.setMenuBar(executeMenu)
        executeMenu.enabled = true
        editMenu.enabled = false
        switchButton.action = switchToEdit

        editor.requestFocus()
        interpreter = new Interpreter(editor.getOperators)
    }

    val switchButton = new Button(switchToEdit)

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
            createMenuItem("Step", KeyStroke.getKeyStroke('S'), move(Step)),
            createMenuItem("Jump", KeyStroke.getKeyStroke('J'), move(Jump)),
            createMenuItem("Turn left", KeyStroke.getKeyStroke('L'), move(TurnLeft)),
            createMenuItem("Turn right", KeyStroke.getKeyStroke('R'), move(TurnRight)),
            new Separator,
            createMenuItem("Clear field", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true), {
                state.reset()
                field.repaint()
            })
        )
        contents += createMenu("Run",
            createMenuItem("Move", KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0, false), {
                interpreter.step(state)
                field.repaint()
            }),
            createMenuItem("Run...", KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0, true), {
                interpreter.run(state)
                field.repaint()
            }),
            new Separator,
            createMenuItem("Restart", KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.CTRL_MASK, true), {
                interpreter.restart()
            })
        )
    }

    frame.setMenuBar(executeMenu)

    private def move(op: core.operator.SimpleOperator) {
        if (state(op)) {
            field.repaint()
            StatusLine.removeError()
        } else {
            StatusLine.raiseError()
        }
    }

    override def top = frame
}