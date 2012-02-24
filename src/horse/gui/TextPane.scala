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
    focusable = false

    def document = peer.getDocument.asInstanceOf[StyledDocument]
}
