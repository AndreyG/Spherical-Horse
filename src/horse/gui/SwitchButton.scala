package horse.gui

import scala.swing.{Frame, Button, Action, MenuBar}
import scala.swing.event.Key

class SwitchButton( frame: Frame, executeMenu: MenuBar, editMenu: MenuBar,
                    onEdit: => Unit, onExecute: => Unit ) extends Button { self =>

    val toEdit: Action = Action("Edit") {
        frame.menuBar = editMenu
        frame.peer.validate()
        editMenu.enabled = true
        executeMenu.enabled = false

        self.action = toExecute
        self.mnemonic = Key.Q

        onEdit
    }

    val toExecute: Action = Action("Execute") {
        frame.menuBar = executeMenu
        frame.peer.validate()
        executeMenu.enabled = true
        editMenu.enabled = false
        
        self.action = toEdit
        self.mnemonic = Key.Q

        executeMenu.requestFocus()

        onExecute
    }
}
