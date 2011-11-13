package horse

import swing.SimpleSwingApplication

import gui.{FieldImage, Editor, MainFrame}
import gui.menu.{EditorMenu, ExecutionMenu}

import core.fieldstate.DynamicField
import core.program.Interpreter

object Main extends SimpleSwingApplication {

    val field       = DynamicField.empty
    val image       = new FieldImage(field)

    val editor      = new Editor

    val player      = new Player(field, image)
    val debugger    = new Debugger(field, image, editor)

    val problem     = new Problem(editor)

    val frame = new MainFrame (
        editor, image, 
        new ExecutionMenu(player, debugger, problem), new EditorMenu(editor), 
        {
            editor.prepare() 
            editor.requestFocus()
        },
        {
            val interpreter = new Interpreter(editor.program) 
            debugger.set(interpreter)
            editor.highlightOperator(0, Editor.ProgramState.Normal)
            image.requestFocus()
        }
    ) 

    override def top = frame
}
