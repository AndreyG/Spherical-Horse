package horse.core

import horse.core.operator._

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

    def buildState(operators: IndexedSeq[Operator]): State = {
        def step(i: Int): (State, Int) = {
            operators(i) match {
                case ProgramBegin => {
                    val (next, rest) = step(i + 1)
                    (StartState(next), rest)
                }
                case op: SimpleOperator => {
                    val (next, rest) = step(i + 1) 
                    (SimpleState(op, i, next), rest)
                }
                case While(c) => {
                    val (body, withoutWhileBody) = step(i + 1)
                    val (next, rest) = step(withoutWhileBody + 1)
                    (WhileState(c, i, body, next), rest)
                }
                case If(c) => {
                    val (trueBranch, withoutIfBody) = step(i + 1)
                    operators(withoutIfBody) match {
                        case End => {
                            val (next, rest) = step(withoutIfBody + 1)
                            (IfState(c, i, trueBranch, next), rest)
                        }
                        case Else => {
                            val (falseBranch, withoutElseBody) = step(withoutIfBody + 1)
                            val (next, rest) = step(withoutElseBody + 1)
                            (IfElseState(c, i, trueBranch, falseBranch, next), rest)
                        }
                    }
                }
                case Else       => (EndState(i), i)
                case End        => (EndState(i), i) 
                case ProgramEnd => (TerminalState(i), i + 1)
            }
        }

        val (state, n) = step(0)
        assert(n == operators.size)
        state
    }
}
