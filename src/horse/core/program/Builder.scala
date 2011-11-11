package horse.core.program

import horse.core.operator._
import Interpreter.Program

object Builder {
    type Condition = Condition.Value

    def apply(operators: Program): State = {
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
