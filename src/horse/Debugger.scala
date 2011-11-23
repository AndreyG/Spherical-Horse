package horse

import core.fieldstate.DynamicField
import core.program.Interpreter
import core.program.Result.Success
import gui.FieldImage
import programtext.IHighlightor


class Debugger(field: DynamicField, image: FieldImage, highlightor: IHighlightor) {
    import highlightor.ProgramState._

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

        val (procIdx, line) = interpreter.currentOperator
        highlightor(procIdx, line, status)
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
        val (procIdx, line) = interpreter.currentOperator
        highlightor(procIdx, line, status)
    }

    def restart() {
        interpreter.restart()
        val (procIdx, line) = interpreter.currentOperator
        highlightor(procIdx, line, Normal)
    } 

    private[this] var interpreter: Interpreter = null
    
    def set(interpreter: Interpreter) {
        this.interpreter = interpreter
    }
}
