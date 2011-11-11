package horse

import swing.SimpleSwingApplication

import gui.{FieldImage, Editor, MainFrame}
import gui.menu.{EditorMenu, ExecutionMenu}

import core.fieldstate.DynamicField
import core.program.Interpreter

object Main extends SimpleSwingApplication {

    val field = DynamicField.empty
    val image = new FieldImage(field)

    val player      = new Player(field, image)
    val debugger    = new Debugger(field, image)

    override def top = new MainFrame (
        image, 
        new ExecutionMenu(player, debugger),
        EditorMenu, 
        {
            Editor.prepare() 
            Editor.requestFocus()
        },
        {
            val interpreter = new Interpreter(Editor.program) 
            debugger.set(interpreter)
            Problem.set(interpreter)
            Editor.highlightOperator(0, Editor.ProgramState.Normal)
            image.requestFocus()
        }
    )

}
