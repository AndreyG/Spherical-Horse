abstract class Operator {
    val text: String

    override def toString = text
}

abstract class SimpleOperator(name: String) extends Operator {
    override val text = name
}

case object Step extends SimpleOperator("step")
case object Jump extends SimpleOperator("jump")
case object TurnLeft extends SimpleOperator("turn left")
case object TurnRight extends SimpleOperator("turn right")

object Condition extends Enumeration {
    val wall, empty = Value

    def not(c: Value) = Condition(1 - c.id)
}

abstract class ConditionalOperator extends Operator 

sealed case class If(c: Condition.Value) extends ConditionalOperator {
    override val text = "if (" + c + ") then"
}

case object Else extends Operator {
    override val text = "else"
}

sealed case class While(c: Condition.Value) extends ConditionalOperator {
    override val text = "while (" + c + ") do"
}

case object End extends Operator {
    override val text = "end"
}
