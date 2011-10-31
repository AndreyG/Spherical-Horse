package core

import scala.collection.mutable.Stack

object Interpretator {
    object Result extends Enumeration {
        val Success, Crash, InfiniteLoop = Value
    }
}

class Interpretator(operators: List[operator.Operator]) {
    import Program._
    import Interpretator.Result._
    type ResultType = Interpretator.Result.Value 

    def isStopped = current.isInstanceOf[TerminalState]

    def step(field: FieldState): Boolean = {
        val res = current match {
            case SimpleState(operator, _, _) => field(operator)
            case _ => true
        }
        current match {
            case StartState(next) => current = next
            case EndState(_) => {
                val st = stack.top
                st match {
                    case st: IfState        => stack.pop(); current = st.next
                    case st: IfElseState    => stack.pop(); current = st.next
                    case st: WhileState     => current = st
                }
            }
            case SimpleState(_, _, next) => if (res) current = next 
            case IfState(c, _, body, next) => {
                if (field(c)) {
                    stack.push(current.asInstanceOf[IfState])
                    current = body
                } else {
                    current = next
                }
            }
            case IfElseState(c, _, trueBranch, falseBranch, _) => {
                stack.push(current.asInstanceOf[IfElseState])
                if (field(c)) {
                    current = trueBranch
                } else {
                    current = falseBranch
                }
            }
            case WhileState(c, _, body, next) => {
                if (field(c)) {
                    stack.push(current.asInstanceOf[WhileState])
                    current = body
                } else {
                    current = next
                }
            }
        }
        res
    }

    def currentLine = current.line

    def run(field: FieldState): ResultType = {
        var cycles = {
            for ((op, i) <- operators.zip(0 until operators.size); if op.isInstanceOf[operator.While]) 
                yield (i, 0)
        }.toMap

        while (!isStopped) {
            if (step(field)) {
                cycles.get(current.line) match {
                    case Some(iter) => {
                        if (iter == 1000)
                            return InfiniteLoop
                        else 
                            cycles = cycles.updated(current.line, iter + 1)
                    }
                    case _ => ()
                }
            } else {
                return Crash
            }
        }
        return Success
    }

    private[this] val start = {
        buildState(operators.zip(0 until operators.size)) match {
            case (state, List()) => state
            case _ => sys.error("build tree error")
        }
    }

    private[this] var current = start

    private[this] val stack: Stack[ConditionalState] = new Stack

}
