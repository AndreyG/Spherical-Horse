package horse

import swing.SimpleSwingApplication

import gui.{Field, Editor, MainFrame}
import gui.menu.{EditorMenu, ExecutionMenu}

import core.{Interpreter, FieldState}

object Main extends SimpleSwingApplication {

    val rows = Config.getInt("field.rows")
    val cols = Config.getInt("field.cols")

    val state = new FieldState(cols, rows)
    val field = new Field(rows, cols, state)

    val executionMenu = new ExecutionMenu(state, field)

    override def top = new MainFrame (
        field, 
        executionMenu,
        EditorMenu, 
        {
            Editor.prepare() 
            Editor.requestFocus()
        },
        {
            executionMenu.interpreter = new Interpreter(Editor.program)
            Editor.highlightOperator(0, Editor.ProgramState.Normal)
            field.requestFocus()
        }
    )

}
