package horse.gui.menu

import java.awt.event.InputEvent.CTRL_MASK
import javax.swing.KeyStroke
import swing.{Component, Menu, MenuItem, Action}

package object menu {
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

    def simpleKeyStroke(key: Int)   = KeyStroke.getKeyStroke(key, 0, true)
    def ctrlKeyStroke(key: Int)     = KeyStroke.getKeyStroke(key, CTRL_MASK, true)
}
