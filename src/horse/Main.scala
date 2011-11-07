package horse

import gui.{Field, Editor, MainFrame}

import core.{Interpreter, FieldState}
import swing._
import swing.event.Key
import javax.swing.KeyStroke
import java.awt.event.{InputEvent, KeyEvent}
import java.io.File

object Main extends SimpleSwingApplication {

    object Mode extends Enumeration {
        val Editing, Executing = Value
    }

    val rows = Config.getInt("field.rows")
    val cols = Config.getInt("field.cols")

    val state = new FieldState(cols, rows)
    val field = new Field(rows, cols, state)
    var interpreter = new Interpreter(Editor.program)

    import gui.MenuUtils._

    val fileChooser = new FileChooser(new File("progs"))

    val editMenu: MenuBar = new MenuBar {
        contents += createMenu("File",
            createMenuItem("Load", KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK, true), {
                if (fileChooser.showOpenDialog(Editor) == FileChooser.Result.Approve) {
                }
            }),
            createMenuItem("Save", KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK, true), {
                if (fileChooser.showSaveDialog(Editor) == FileChooser.Result.Approve) {
                }
            })
        )

        contents += createMenu("Operator",
            createMenuItem("Step", KeyStroke.getKeyStroke('S'), Editor.step()),
            createMenuItem("Jump", KeyStroke.getKeyStroke('J'), Editor.jump()),
            createMenuItem("Turn left", KeyStroke.getKeyStroke('L'), Editor.turnLeft()),
            createMenuItem("Turn right", KeyStroke.getKeyStroke('R'), Editor.turnRight()),
            new Separator,
            createMenuItem("If", KeyStroke.getKeyStroke("I"), Editor.createIf()),
            createMenuItem("Else", KeyStroke.getKeyStroke("E"), Editor.createElse()),
            createMenuItem("While", KeyStroke.getKeyStroke("W"), Editor.createWhile()),
            new Separator,
            createMenuItem("Not", KeyStroke.getKeyStroke("N"), Editor.inverse())
        )

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

        import Editor.ProgramState._

        contents += createMenu("Run",
            createMenuItem("Move", KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0, false), {
                val res = interpreter.step(state)
                if (res) {
                    field.repaint()
                    Editor.highlightOperator(interpreter.currentLine, if (interpreter.isStopped) End else Normal)
                } else {
                    Editor.highlightOperator(interpreter.currentLine, Error)
                }
            }),
            createMenuItem("Run...", KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0, true), {
                val res = interpreter.run(state)
                field.repaint()
                Editor.highlightOperator(interpreter.currentLine, res match {
                    case Interpreter.Result.Success => End
                    case _ => Error
                })
            }),
            new Separator,
            createMenuItem("Restart", KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.CTRL_MASK, true), {
                interpreter.restart()
                Editor.highlightOperator(interpreter.currentLine, Editor.ProgramState.Normal)
            })
        )
    }

    override def top = new MainFrame(field, executeMenu, editMenu, Editor.prepare(), {
        interpreter = new Interpreter(Editor.program)
        Editor.highlightOperator(interpreter.currentLine, Editor.ProgramState.Normal)
    })
}
