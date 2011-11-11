package horse

import java.io.{File, FileReader, BufferedReader, FileOutputStream, PrintStream}

import core.fieldstate.DynamicField
import core.program.Interpreter
import core.program.Result.Success

import gui.TaskViewer

object Problem {
    def canSave = isCorrect 

    def save(file: File) {
        val out = new PrintStream(new FileOutputStream(file))
        serialization.dumpField(field, out)
        out.close()
    }

    def load(file: File): Boolean = {
        val in = new BufferedReader(new FileReader(file)) 
        try {
            TaskViewer(serialization.loadField(in))
        } catch {
            case _ => return false
        } finally {
            in.close()
        }
        true
    }

    def set(interpreter: Interpreter) {
        field = DynamicField.empty
        isCorrect = interpreter.initial.run(field) == Success
    }

    private[this] var field = DynamicField.empty
    private[this] var isCorrect = true
}
