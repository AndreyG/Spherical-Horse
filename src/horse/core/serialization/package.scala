package horse.core

import scala.collection.mutable.{Buffer, ArrayBuffer}

import java.io.{BufferedReader, PrintStream}

import horse.core.operator.Operator
import horse.core.Interpreter.Program
//import horse.core.FieldState

package object serialization {

    def dump(program: Program, out: PrintStream) {
        for (line <- program) {
            out.println(OperatorSerializer.toString(line))
        }
    }

    def load(in: BufferedReader) = {
        val buffer: Buffer[Operator] = new ArrayBuffer
        var s = in.readLine
        while (s != null) {
            buffer += OperatorSerializer.fromString(s)
            s = in.readLine
        }
        buffer.toIndexedSeq
    }

    def dump(field: FieldState, out: PrintStream) {
        out.println(field.width + " " + field.height)
        out.println(field.pos.x + " " + field.pos.y)
        out.println(field.direction)
        for ((p1, p2) <- field.getShow) {
            out.println(p1.x + " " + p1.y + " " + p2.x + " " + p2.y)
        }
    }
}
