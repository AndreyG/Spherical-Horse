package horse.gui.menu

import scala.swing.{MenuBar, Separator, FileChooser}

import java.io.{BufferedReader, File, FileReader, FileOutputStream, PrintStream}
import javax.swing.JOptionPane
import java.awt.event.KeyEvent._
import javax.swing.filechooser.FileNameExtensionFilter

import FileChooser.Result.Approve

import horse.serialization
import horse.gui.Editor

import menu._

class EditorMenu(editor: Editor) extends MenuBar {

    private[this] val fileChooser = new FileChooser(new File("progs"))
    fileChooser.fileFilter = new FileNameExtensionFilter("Horse programs", "hp")

    contents += createMenu("File",
        createMenuItem("Load", ctrlKeyStroke(VK_L), {
            if (fileChooser.showOpenDialog(this) == Approve) {
                val in = new BufferedReader(new FileReader(fileChooser.selectedFile)) 
                try {
                    editor.program = serialization.loadProgram(in)
                } catch {
                    case _ => JOptionPane.showMessageDialog(this.peer, 
                        "corruped file", 
                        "error",
                        JOptionPane.ERROR_MESSAGE
                    )
                } finally {
                    in.close()
                }
            }
        }),
        createMenuItem("Save", ctrlKeyStroke(VK_S), {
            if (fileChooser.showSaveDialog(this) == Approve) {
                val out = new PrintStream(new FileOutputStream(fileChooser.selectedFile))
                serialization.dumpProgram(editor.program, out) 
                out.close()
            }
        })
    )

    contents += createMenu("Operator",
        createMenuItem("Step",          simpleKeyStroke(VK_S), editor.step()),
        createMenuItem("Jump",          simpleKeyStroke(VK_J), editor.jump()),
        createMenuItem("Turn left",     simpleKeyStroke(VK_L), editor.turnLeft()),
        createMenuItem("Turn right",    simpleKeyStroke(VK_R), editor.turnRight()),
        new Separator,
        createMenuItem("If",            simpleKeyStroke(VK_I), editor.createIf()),
        createMenuItem("Else",          simpleKeyStroke(VK_E), editor.createElse()),
        createMenuItem("While",         simpleKeyStroke(VK_W), editor.createWhile()),
        new Separator,
        createMenuItem("Not",           simpleKeyStroke(VK_N), editor.inverse())
    )
}
