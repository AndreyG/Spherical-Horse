package horse

import swing.SimpleSwingApplication

import gui.{FieldImage, MainFrame}
import gui.menu.{EditorMenu, ExecutionMenu}

import programtext.ProgramText

import core.fieldstate.DynamicField
import core.program.Interpreter

object Main extends SimpleSwingApplication {

    val field       = DynamicField.empty
    val image       = new FieldImage(field)

    val editor      = ProgramText.getEditor
    val textPane    = ProgramText.getPane
    val highlightor = ProgramText.getHighlightor

    val player      = new Player(field, image)
    val debugger    = new Debugger(field, image, highlightor)

    val problem     = new Problem(ProgramText)

    val frame = new MainFrame (
        textPane, image, 
        new ExecutionMenu(player, debugger, problem), new EditorMenu(ProgramText, editor),
        {
            highlightor.release()
            editor.prepare()
            textPane.requestFocus()
        },
        {
            editor.release()
            highlightor.prepare()
            debugger.set(new Interpreter(ProgramText.program))
            image.requestFocus()
        }
    ) 

    override def top = frame
}
