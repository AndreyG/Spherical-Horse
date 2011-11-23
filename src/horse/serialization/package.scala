package horse

import scala.collection.mutable.{Buffer, ArrayBuffer}

import java.io.{BufferedReader, PrintStream}

import core.operator.Operator
import core.program.Interpreter.{Procedure, Program}
import core.fieldstate.{Field, StaticField}
import core.fieldstate.Horse.Pos

package object serialization {

    def dumpProgram(program: Program, out: PrintStream) {
        for (proc <- program) {
            out.println(proc.name)
            for (op <- proc.operators) {
                out.println(OperatorSerializer.toString(op))
            }    
            out.println()
        }
    }

    def loadProgram(in: BufferedReader): Program = {
        val prog: Buffer[Procedure] = new ArrayBuffer
        var procName = in.readLine
        while (procName != null) {
            val operators: Buffer[Operator] = new ArrayBuffer
            var s = in.readLine
            while (s != "") {
                operators += OperatorSerializer.fromString(s)
                s = in.readLine
            }
            prog += new Procedure(procName, operators.toIndexedSeq) 
            procName = in.readLine
        }
        prog.toIndexedSeq
    }

    def dumpField(field: Field, out: PrintStream) {
        out.println(HorseSerializer.toString(field.width, field.height))
        out.println(HorseSerializer.toString(field.getPos))
        out.println(field.getDir)
        for ((p1, p2) <- field.getShow) {
            out.println(HorseSerializer.toString(p1, p2))
        }
    }

    def loadField(in: BufferedReader) = {
        import HorseSerializer._

        val (width, height) = parseIntPair(in.readLine)
        val pos = parsePos(in.readLine)
        val dir = parseDirection(in.readLine)
        val buffer: Buffer[(Pos, Pos)] = new ArrayBuffer
        var s = in.readLine
        while (s != null) {
            buffer += parsePosPair(s)
            s = in.readLine
        }
        new StaticField(width, height, pos, dir, buffer.toIterable)
    }
}
