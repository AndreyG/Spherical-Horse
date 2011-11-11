package horse.core.program

import scala.collection.mutable.Stack

import horse.core.operator.{Operator, While}

object Interpreter {
    type Program = IndexedSeq[Operator]
}

object Result extends Enumeration {
    val Success, Crash, InfiniteLoop = Value
}

import horse.core.fieldstate.DynamicField
import Interpreter.Program

class Interpreter private (operators: Program, start: State) {
    def this(operators: Program) = this(operators, Builder(operators))

    type ResultType = Result.Value

    def isStopped = current.isInstanceOf[TerminalState]

    def currentLine = current.line

    def step(field: DynamicField): Boolean = {
        val res = current match {
            case SimpleState(operator, _, _) => field(operator)
            case _ => true
        }
        current match {
            case TerminalState(_) => ()
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

    def run(field: DynamicField): ResultType = {
        var cycles = 
            operators
                .zipWithIndex
                .filter(elem => elem._1.isInstanceOf[While])
                .map(elem => (elem._2, 0))
                .toMap 

        while (!isStopped) {
            if (step(field)) {
                cycles.get(current.line) match {
                    case Some(iter) => {
                        if (iter == 1000)
                            return Result.InfiniteLoop
                        else 
                            cycles = cycles.updated(current.line, iter + 1)
                    }
                    case _ => ()
                }
            } else {
                return Result.Crash
            }
        }
        Result.Success
    }

    def restart() {
        current = start
    }

    def initial = new Interpreter(operators, start)

    private[this] var current = start
    private[this] val stack: Stack[ConditionalState] = new Stack

}
