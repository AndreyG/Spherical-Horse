package core

import core.operator._

object Program {
    type Condition = operator.Condition.Value

    abstract class State {
        val line: Int
    }
    abstract class ConditionalState(line: Int) extends State 

    sealed case class StartState(next: State) extends State {
        override val line = 0
    }
    sealed case class TerminalState(line: Int) extends State
    sealed case class SimpleState(operator: SimpleOperator, line: Int, next: State) extends State
    sealed case class IfState(c: Condition, line: Int, body: State, next: State) extends ConditionalState(line)
    sealed case class IfElseState(c: Condition, line: Int, trueBranch: State, falseBranch: State, next: State) extends ConditionalState(line) 
    sealed case class WhileState(c: Condition, line: Int, body: State, next: State) extends ConditionalState(line)
    sealed case class EndState(line: Int) extends State

    def buildState(operators: List[(Operator, Int)]): (State, List[(Operator, Int)]) = {
        val (operator, line) = operators.head
        operator match {
            case ProgramBegin => {
                val (next, rest) = buildState(operators.tail)
                (StartState(next), rest)
            }
            case op: SimpleOperator => {
                val (next, rest) = buildState(operators.tail) 
                (SimpleState(op, line, next), rest)
            }
            case While(c) => {
                val (body, withoutWhileBody) = buildState(operators.tail)
                val (next, rest) = buildState(withoutWhileBody.tail)
                (WhileState(c, line, body, next), rest)
            }
            case If(c) => {
                val (trueBranch, withoutIfBody) = buildState(operators.tail)
                withoutIfBody.head match {
                    case (End, _) => {
                        val (next, rest) = buildState(withoutIfBody.tail)
                        (IfState(c, line, trueBranch, next), rest)
                    }
                    case (Else, _) => {
                        val (falseBranch, withoutElseBody) = buildState(withoutIfBody.tail)
                        val (next, rest) = buildState(withoutElseBody.tail)
                        (IfElseState(c, line, trueBranch, falseBranch, next), rest)
                    }
                }
            }
            case Else       => (EndState(line), operators)
            case End        => (EndState(line), operators) 
            case ProgramEnd => (TerminalState(line), List())
        }
    }
}
