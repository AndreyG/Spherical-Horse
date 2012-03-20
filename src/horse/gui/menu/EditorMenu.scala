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
import horse.core.program.Interpreter

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
        editor.exec(a)
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

    contents += createMenu("Edit",
        createMenuItem("Line Up",           simpleKeyStroke(VK_UP),         exec(Up)),
        createMenuItem("Line Down",         simpleKeyStroke(VK_DOWN),       exec(Down)),
        createMenuItem("Prev Procudure",    simpleKeyStroke(VK_PAGE_UP),    exec(PrevProc)),
        createMenuItem("Next Procedure",    simpleKeyStroke(VK_PAGE_DOWN),  exec(NextProc)),
        createMenuItem("Procudure Begin",   simpleKeyStroke(VK_HOME),       exec(ProcBegin)),
        createMenuItem("Procudure End",     simpleKeyStroke(VK_END),        exec(ProcEnd)),
        createMenuItem("Program Begin",     ctrlKeyStroke(VK_HOME),         exec(ProgBegin)),
        createMenuItem("Program End",       ctrlKeyStroke(VK_END),          exec(ProgEnd)),
        createMenuItem("Delete Line",       simpleKeyStroke(VK_DELETE),     exec(Delete)),
        createMenuItem("Delete Program",    ctrlKeyStroke(VK_DELETE),       {
            text.program = Interpreter.emptyProgram
        }),
        new Separator,
        createMenuItem("Dump", ctrlKeyStroke(VK_D), {
          val chooser = new FileChooser(new File("."))
          if (chooser.showSaveDialog(this) == FileChooser.Result.Approve) {
              val out = new PrintStream(new FileOutputStream(chooser.selectedFile))
              Protocol.dump(out)
              out.close()
          }
        })
    )

    private def showError(err: String) {
        JOptionPane.showMessageDialog(peer, err, "error", JOptionPane.ERROR_MESSAGE)
    }
}
