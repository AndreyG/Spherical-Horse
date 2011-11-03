package horse.gui

import scala.swing.Label
import java.awt.Color

object StatusLine extends Label {
    background = Color.lightGray
    
    def raiseError() {
        foreground = Color.red
        text = "impossible!"
    }

    def removeError() {
        text = ""
    }
}
