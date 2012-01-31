package horse.gui.menu

import scala.swing.{MenuBar, Separator, FileChooser}

import java.io.{BufferedReader, File, FileReader, FileOutputStream, PrintStream}
import javax.swing.JOptionPane
import java.awt.event.KeyEvent._
import javax.swing.filechooser.FileNameExtensionFilter

import FileChooser.Result.Approve

import horse.serialization

import menu._
import horse.programtext.{Protocol, IProgramText, IEditor}

class EditorMenu(text: IProgramText, editor: IEditor) extends MenuBar {

    private[this] val fileChooser = new FileChooser(new File("progs"))
    fileChooser.fileFilter = new FileNameExtensionFilter("Horse programs", "hp")

    contents += createMenu("File",
        createMenuItem("Load",          ctrlKeyStroke(VK_L), 
            if (fileChooser.showOpenDialog(this) == Approve) {
                val in = new BufferedReader(new FileReader(fileChooser.selectedFile)) 
                try {
                    val prog = serialization.loadProgram(in)
                    Protocol.setProgram(prog)
                    text.program = prog
                } catch {
                    case _ => showError("corrupted file")
                } finally {
                    in.close()
                }
            }
        ),
        createMenuItem("Save",          ctrlKeyStroke(VK_S), 
            if (fileChooser.showSaveDialog(this) == Approve) {
                val out = new PrintStream(new FileOutputStream(fileChooser.selectedFile))
                serialization.dumpProgram(text.program, out) 
                out.close()
            }
        )
    )

    import horse.programtext.editor._

    private def exec(a: Action) {
        Protocol.log(a)
        editor.add(a)
    }

    contents += createMenu("Operator",
        createMenuItem("Step",          simpleKeyStroke(VK_S), exec(Step)),
        createMenuItem("Jump",          simpleKeyStroke(VK_J), exec(Jump)),
        createMenuItem("Turn left",     simpleKeyStroke(VK_L), exec(Left)),
        createMenuItem("Turn right",    simpleKeyStroke(VK_R), exec(Right)),
        new Separator,
        createMenuItem("If",            simpleKeyStroke(VK_I), exec(If)),
        createMenuItem("Else",          simpleKeyStroke(VK_E), exec(Else)),
        createMenuItem("While",         simpleKeyStroke(VK_W), exec(While)),
        new Separator,
        createMenuItem("Not",           simpleKeyStroke(VK_N), exec(Inverse))
    )

    contents += createMenu("Procedure",
        createMenuItem("New",           ctrlKeyStroke(VK_N), {
            val procName = JOptionPane.showInputDialog(text.getPane.peer, "Procedure name")
            if (procName != null) {
                if (text.getProcNames.indexOf(procName) == -1) {
                    Protocol.log(NewProc(procName))
                    text.addProcedure(procName)
                } else {
                    showError("duplicated procedure name")
                }
            }
        }),
        createMenuItem("Call",          simpleKeyStroke(VK_C), {
            val procName = JOptionPane.showInputDialog (
                text.getPane.peer, "Procedure name",
                "Select procedure name", JOptionPane.QUESTION_MESSAGE, null, 
                text.getProcNames.toArray, null
            )
            if (procName != null) {
                exec(Call(procName.asInstanceOf[String]))
            }
        })
    )

    private def showError(err: String) {
        JOptionPane.showMessageDialog(peer, err, "error", JOptionPane.ERROR_MESSAGE)
    }
}