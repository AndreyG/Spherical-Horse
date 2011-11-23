package horse.gui

import scala.collection.mutable.{Map, HashMap}
import scala.swing.EditorPane
import scala.swing.event.{KeyPressed, Key}
import javax.swing.text.{StyledDocument, StyledEditorKit}
import java.awt.Color

class TextPane extends EditorPane {

    editorKit = new StyledEditorKit

    background = Color.darkGray
    editable = false
    focusable = true

    listenTo(keys)
    reactions += {
        case e: KeyPressed => {
            actions.get(e.key) match {
                case Some(action) => action()
                case _ => ()
            }
        }
    }

    def document = peer.getDocument.asInstanceOf[StyledDocument]

    def addKeyListener(key: Key.Value, action: => Unit) {
        actions(key) = () => action
    }
    
    private[this] val actions: Map[Key.Value, () => Unit] = new HashMap
}
