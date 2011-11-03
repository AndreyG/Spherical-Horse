package horse.gui

import javax.swing.KeyStroke
import swing._

object MenuUtils {
    def createMenu(title: String, items: Component*) = new Menu(title) {
        for (item <- items) {
            contents += item
        }
    }

    def createMenuItem(title: String, key: KeyStroke, action: => Unit): MenuItem = {
        new MenuItem(new Action(title) {
            accelerator = Some(key)
            override def apply() {
                action
            }
        })
    }
}