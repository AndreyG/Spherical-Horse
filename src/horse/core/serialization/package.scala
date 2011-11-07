package horse.core

import operator._

package object serialization {
    def toText(program: Iterable[Operator]) = program.map(toString)
    def fromText(lines: Iterator[String])   = lines.map(fromString).toIndexedSeq

    private def toString(op: Operator) = op match {
        case If(c)      => "if " + c.toString
        case While(c)   => "while " + c.toString
        case _          => op.toString
    }

    private def fromString(line: String): Operator = {
        if (line.startsWith("if"))
            return If(Condition.withName(line.substring(3, line.length)))
        else if (line.startsWith("while"))
            return While(Condition.withName(line.substring(6, line.length)))
        else {
            for (op <- List(Step, Jump, TurnLeft, TurnRight, Else, End, ProgramBegin, ProgramEnd)) {
                if (line == op.toString)
                    return op
            }
        }
        sys.error("Undefined operator:\n" + line)
    }
}
