package horse.gui.menu

import scala.swing.{MenuBar, Separator, FileChooser}
import java.io.{File, FileOutputStream, PrintStream}
import java.awt.event.KeyEvent._
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter

import FileChooser.Result.Approve

import horse.core.serialization
import horse.core.{FieldState, Interpreter}
import horse.core.operator.{SimpleOperator, Step, Jump, TurnLeft, TurnRight}
import horse.gui.{Editor, Field}
import Editor.ProgramState._

import menu._

class ExecutionMenu(state: FieldState, field: Field) extends MenuBar {
    var interpreter = new Interpreter(Editor.program)

    contents += createMenu("Execute",
        createMenuItem("Step",          simpleKeyStroke(VK_S), move(Step)),
        createMenuItem("Jump",          simpleKeyStroke(VK_J), move(Jump)),
        createMenuItem("Turn left",     simpleKeyStroke(VK_L), move(TurnLeft)),
        createMenuItem("Turn right",    simpleKeyStroke(VK_R), move(TurnRight)),
        new Separator,
        createMenuItem("Clear field",   simpleKeyStroke(VK_ESCAPE), {
            state.reset()
            field.repaint()
        })
    )

    contents += createMenu("Run",
        createMenuItem("Move",      simpleKeyStroke(VK_F7), {
            val res = interpreter.step(state)
            val status = {
                if (res) 
                    if (interpreter.isStopped) 
                        End 
                    else
                        Normal
                else 
                    Error
            }
            if (res) 
                field.repaint()
            Editor.highlightOperator(interpreter.currentLine, status)
        }),
        createMenuItem("Run...",    simpleKeyStroke(VK_F9), {
            val res = interpreter.run(state)
            field.repaint()
            val status = {
                if (res == Interpreter.Result.Success) 
                    End 
                else 
                    Error
            }
            Editor.highlightOperator(interpreter.currentLine, status)
        }),
        new Separator,
        createMenuItem("Restart",   ctrlKeyStroke(VK_F2), {
            interpreter.restart()
            Editor.highlightOperator(interpreter.currentLine, Normal)
        })
    )

    private def move(op: SimpleOperator) {
        state(op)
        field.repaint()
    }

    private[this] val fileChooser = new FileChooser(new File("problems"))
    fileChooser.fileFilter = new FileNameExtensionFilter("Spherical horse problems", "shp")

    contents += createMenu("Problemset",
        createMenuItem("Create",    ctrlKeyStroke(VK_S), {
            val tmpState        =       state.initial
            val tmpInterpreter  = interpreter.initial
            if (tmpInterpreter.run(tmpState) == Interpreter.Result.Success) {
                if (fileChooser.showSaveDialog(field) == Approve) {
                    val out = new PrintStream(new FileOutputStream(fileChooser.selectedFile))
                    serialization.dump(tmpState, out)
                    out.close()
                }
            } else {
                JOptionPane.showMessageDialog(this.peer, 
                    "Erroneous programs are declined to be saved as problems", 
                    "error",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        })
    )

}
