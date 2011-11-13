package horse

import java.io.{File, FileReader, BufferedReader, FileOutputStream, PrintStream}
import javax.swing.JOptionPane

import core.fieldstate.{StaticField, DynamicField}
import core.program.Interpreter
import core.program.Result.Success

import gui.{Editor, ProblemFrame}

class Problem(editor: Editor) {
    def canSave: Boolean = {
        field = DynamicField.empty
        new Interpreter(editor.program).run(field) == Success
    }

    def save(file: File) {
        val out = new PrintStream(new FileOutputStream(file))
        serialization.dumpField(field, out)
        out.close()
    }

    def load(file: File): Boolean = {
        var in: BufferedReader = new BufferedReader(new FileReader(file)) 
        try {
            if (frame != null)
                frame.dispose()

            val etalon = serialization.loadField(in)

            frame = new ProblemFrame(etalon, check(etalon), frame = null)
            frame.setLocationRelativeTo(Main.frame)
            frame.visible = true
        } catch {
            case _ => return false
        } finally {
            in.close()
        }
        true
    }

    private def check(etalon: StaticField) {
        val field  = DynamicField.empty
        val result = new Interpreter(editor.program).run(field)

        if ((result == Success) && etalon.equals(field)) 
            frame.reportSuccess()
        else 
            frame.reportFail()
    }

    private[this] var frame: ProblemFrame = null

    private[this] var field: DynamicField = null
}
