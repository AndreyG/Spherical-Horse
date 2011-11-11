package horse.gui.menu

import scala.swing.{MenuBar, Separator, FileChooser}
import java.awt.event.KeyEvent._
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter
import java.io.File

import FileChooser.Result.Approve

import horse.{Player, Debugger, Problem}
import horse.core.operator.{Step, Jump, TurnLeft, TurnRight}

import menu._

class ExecutionMenu(player: Player, debugger: Debugger) extends MenuBar {

    contents += createMenu("Execute",
        createMenuItem("Step",          simpleKeyStroke(VK_S), player.move(Step)),
        createMenuItem("Jump",          simpleKeyStroke(VK_J), player.move(Jump)),
        createMenuItem("Turn left",     simpleKeyStroke(VK_L), player.move(TurnLeft)),
        createMenuItem("Turn right",    simpleKeyStroke(VK_R), player.move(TurnRight)),
        new Separator,
        createMenuItem("Clear field",   simpleKeyStroke(VK_ESCAPE), player.clearField())
    )

    contents += createMenu("Run",
        createMenuItem("Step",      simpleKeyStroke(VK_F7), debugger.step()),
        createMenuItem("Run...",    simpleKeyStroke(VK_F9), debugger.run()),
        new Separator,
        createMenuItem("Restart",   ctrlKeyStroke(VK_F2),   debugger.restart())
    )

    private[this] val fileChooser = new FileChooser(new File("problems"))
    fileChooser.fileFilter = new FileNameExtensionFilter("Spherical horse problems", "shp")

    contents += createMenu("Problemset",
        createMenuItem("Create",    ctrlKeyStroke(VK_C), {
            if (Problem.canSave) {
                if (fileChooser.showSaveDialog(this) == Approve) {
                    Problem.save(fileChooser.selectedFile)
                }
            } else {
                JOptionPane.showMessageDialog(this.peer, 
                    "Erroneous programs are declined to be saved as problems", 
                    "error",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }),
        createMenuItem("Load",  ctrlKeyStroke(VK_L), 
            if (fileChooser.showOpenDialog(this) == Approve) {
                if (!Problem.load(fileChooser.selectedFile)) {
                    JOptionPane.showMessageDialog(this.peer, 
                        "corruped file", 
                        "error",
                        JOptionPane.ERROR_MESSAGE
                    )
                }
            }
        )
    )

}
