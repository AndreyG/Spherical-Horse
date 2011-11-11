package horse.core.program

import horse.core.operator._

abstract class State {
    val line: Int
}
abstract class ConditionalState(line: Int) extends State 

sealed case class StartState(next: State) extends State {
    override val line = 0
}
sealed case class TerminalState(line: Int) extends State
sealed case class SimpleState(operator: SimpleOperator, line: Int, next: State) extends State
sealed case class IfState(c: Condition.Value, line: Int, body: State, next: State) extends ConditionalState(line)
sealed case class IfElseState(c: Condition.Value, line: Int, trueBranch: State, falseBranch: State, next: State) extends ConditionalState(line) 
sealed case class WhileState(c: Condition.Value, line: Int, body: State, next: State) extends ConditionalState(line)
sealed case class EndState(line: Int) extends State
