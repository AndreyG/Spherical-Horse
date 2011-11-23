package horse.serialization

import horse.core.operator._

object OperatorSerializer {
    private[serialization] def toString(op: Operator) = op match {
        case If(c)      => "if " + c.toString
        case While(c)   => "while " + c.toString
        case Call(proc) => "call " + proc
        case _          => op.toString
    }

    private[serialization] def fromString(line: String): Operator = {
        if (line.startsWith("if"))
            return If(Condition.withName(line.substring(3, line.length)))
        else if (line.startsWith("while"))
            return While(Condition.withName(line.substring(6, line.length)))
        else if (line.startsWith("call"))
            return Call(line.substring(5, line.length))
        else {
            for (op <- List(Step, Jump, TurnLeft, TurnRight, Else, End)) {
                if (line == op.toString)
                    return op
            }
        }
        sys.error("Undefined operator:\n" + line)
    }
}
