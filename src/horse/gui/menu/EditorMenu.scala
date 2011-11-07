package horse.gui.menu

import scala.swing.{MenuBar, Separator, FileChooser}
import scala.io.Source

import java.io.{File, FileOutputStream, PrintStream}
import java.awt.event.KeyEvent._
import javax.swing.filechooser.FileNameExtensionFilter

import FileChooser.Result.Approve

import horse.core.serialization
import horse.gui.Editor

import menu._

object EditorMenu extends MenuBar {

    private[this] val fileChooser = new FileChooser(new File("progs"))
    fileChooser.fileFilter = new FileNameExtensionFilter("Horse programs", "hp")

    contents += createMenu("File",
        createMenuItem("Load", ctrlKeyStroke(VK_L), {
            if (fileChooser.showOpenDialog(Editor) == Approve) {
                val in = Source.fromFile(fileChooser.selectedFile) 
                Editor.program = serialization.fromText(in.getLines)
            }
        }),
        createMenuItem("Save", ctrlKeyStroke(VK_S), {
            if (fileChooser.showSaveDialog(Editor) == Approve) {
                val out = new PrintStream(new FileOutputStream(fileChooser.selectedFile))
                for (line <- serialization.toText(Editor.program)) {
                    out.println(line)
                }
                out.close()
            }
        })
    )

    contents += createMenu("Operator",
        createMenuItem("Step",          simpleKeyStroke(VK_S), Editor.step()),
        createMenuItem("Jump",          simpleKeyStroke(VK_J), Editor.jump()),
        createMenuItem("Turn left",     simpleKeyStroke(VK_L), Editor.turnLeft()),
        createMenuItem("Turn right",    simpleKeyStroke(VK_R), Editor.turnRight()),
        new Separator,
        createMenuItem("If",            simpleKeyStroke(VK_I), Editor.createIf()),
        createMenuItem("Else",          simpleKeyStroke(VK_E), Editor.createElse()),
        createMenuItem("While",         simpleKeyStroke(VK_W), Editor.createWhile()),
        new Separator,
        createMenuItem("Not",           simpleKeyStroke(VK_N), Editor.inverse())
    )
}
