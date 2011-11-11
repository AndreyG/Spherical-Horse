package horse.gui

import scala.swing.{BorderPanel, Frame, MenuBar, MenuItem, Action}
import javax.swing.KeyStroke
import BorderPanel.Position._
import java.awt.Dimension
import java.awt.event.KeyEvent._

import horse.core.fieldstate.StaticField

import horse.Config

object TaskViewer {
    def apply(etalon: StaticField) {
        val frame = new Frame {
            contents = new BorderPanel {
                add(new FieldImage(etalon), Center)
                menuBar = new MenuBar {
                    contents += new MenuItem(new Action("Check") {
                        accelerator = Some(KeyStroke.getKeyStroke(VK_C, 0, true))
                        override def apply() {
                            println("Check")
                        }
                    })
                }
            }

            size = new Dimension(Config.getInt("field-image.width"), Config.getInt("field-image.height"))
            resizable = false
        }
        frame.visible = true
    }
}
