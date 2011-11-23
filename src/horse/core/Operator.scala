package horse.core.operator

abstract class Operator 

abstract class SimpleOperator extends Operator 

case object Step extends SimpleOperator
case object Jump extends SimpleOperator
case object TurnLeft extends SimpleOperator
case object TurnRight extends SimpleOperator

object Condition extends Enumeration {
    val wall, empty = Value

    def not(c: Value) = Condition(1 - c.id)
}

abstract class ConditionalOperator extends Operator 

sealed case class If(c: Condition.Value) extends ConditionalOperator 
case object Else extends Operator 

sealed case class While(c: Condition.Value) extends ConditionalOperator 

case object End extends Operator 

sealed case class Call(name: String) extends Operator
