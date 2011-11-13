package horse.gui

import scala.swing.{BorderPanel, MenuBar, MenuItem, Frame, Action}
import BorderPanel.Position._
import javax.swing.{KeyStroke, JLabel}
import javax.swing.JOptionPane.{showMessageDialog, INFORMATION_MESSAGE, WARNING_MESSAGE}
import javax.swing.SwingConstants.CENTER
import java.awt.Color.{red, green}
import java.awt.Dimension
import java.awt.event.KeyEvent._

import horse.Config
import horse.core.fieldstate.Field

class ProblemFrame(field: Field, check: => Unit, close: => Unit) extends Frame {
    title = "Problem Checker"

    contents = new BorderPanel {
        add(new FieldImage(field), Center)
        menuBar = new MenuBar {
            contents += new MenuItem(new Action("Check") {
                accelerator = Some(KeyStroke.getKeyStroke(VK_C, 0, true))
                override def apply() {
                    check
                }
            })
        }
    }

    size = {
        val width   = Config.getInt("field-image.width")  
        val height  = Config.getInt("field-image.height")  
        new Dimension(width, height)
    }

    resizable = false

    override def closeOperation() { close }

    def reportSuccess() {
        showMessageDialog(peer, 
            new JLabel("Acceted!", CENTER) {
                setForeground(green)
            }, 
            "Status",
            INFORMATION_MESSAGE
        )
    }

    def reportFail() {
        showMessageDialog(peer, 
            new JLabel("Declined!", CENTER) {
                setForeground(red)
            }, 
            "Status",
            WARNING_MESSAGE
        )
    }
}
