package horse

import core.fieldstate.DynamicField
import core.program.Interpreter
import core.program.Result.Success
import gui.{Editor, FieldImage}
import Editor.ProgramState._

class Debugger(field: DynamicField, image: FieldImage, editor: Editor) {
    def step() {
        val res = interpreter.step(field)
        val status = {
            if (res) 
                if (interpreter.isStopped) 
                    End 
                else
                    Normal
            else 
                Error
        }
        if (res) 
            image.repaint()
        editor.highlightOperator(interpreter.currentLine, status)
    }

    def run() {
        val res = interpreter.run(field)
        image.repaint()
        val status = {
            if (res == Success) 
                End 
            else 
                Error
        }
        editor.highlightOperator(interpreter.currentLine, status)
    }

    def restart() {
        interpreter.restart()
        editor.highlightOperator(interpreter.currentLine, Normal)
    } 

    private[this] var interpreter: Interpreter = null
    
    def set(interpreter: Interpreter) {
        this.interpreter = interpreter
    }
}
