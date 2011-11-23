package horse.core.program

import scala.collection.mutable.Stack

import horse.core.operator.{Operator, While}

object Interpreter {
    class Procedure(val name: String, val operators: IndexedSeq[Operator]) 
    type Program = IndexedSeq[Procedure]
}

object Result extends Enumeration {
    val Success, Crash, InfiniteLoop = Value
}

import horse.core.fieldstate.DynamicField
import Interpreter.Program

class Interpreter private (program: Program, start: State) {
    def this(program: Program) = this(program, Builder(program))

    type ResultType = Result.Value

    def isStopped = current match {
        case TerminalState(0) => procStack.isEmpty
        case _ => false
    }

    def currentOperator = current match {
        case StartState(procIdx)    => (procIdx, 0)
        case TerminalState(procIdx) => (procIdx, program(procIdx).operators.length + 1)
        case op: OperatorState      => (op.procIdx, op.line)
    }

    def step(field: DynamicField): Boolean = {
        val res = current match {
            case SimpleState(_, _, operator, _) => field(operator)
            case _ => true
        }
        current match {
            case st: StartState     => {
                current = st.next
            }
            case _: TerminalState   => {
                if (!procStack.isEmpty) {
                    current = procStack.top.next
                    procStack.pop
                }
            }
            case _: EndState => {
                opStack.top match {
                    case st: IfState        => opStack.pop(); current = st.next
                    case st: IfElseState    => opStack.pop(); current = st.next
                    case st: WhileState     => current = st
                }
            }
            case st: SimpleState => {
                if (res) 
                    current = st.next 
            }
            case st: IfState => {
                if (field(st.condition)) {
                    opStack.push(st)
                    current = st.body
                } else {
                    current = st.next
                }
            }
            case st: IfElseState => {
                opStack.push(st)
                current = 
                    if (field(st.condition)) 
                        st.trueBranch
                    else 
                        st.falseBranch
            }
            case st: WhileState => {
                if (field(st.condition)) {
                    opStack.push(st)
                    current = st.body
                } else {
                    current = st.next
                }
            }
            case st: CallState => {
                procStack.push(st)
                current = st.body
            }
        }
        res
    }

    def run(field: DynamicField): ResultType = {
        val cycles = 
            program.map {
                _.operators
                    .zipWithIndex
                    .filter(elem => elem._1.isInstanceOf[While])
                    .map(elem => (elem._2 + 1, 0))
                    .toMap 
            }.toArray

        val procedures: Array[Int] = Array.ofDim(program.length)

        while (!isStopped) {
            if (step(field)) {
                if (current.isInstanceOf[StartState]) {
                    if (procedures(current.procIdx) == 1000)
                        return Result.InfiniteLoop
                    else
                        procedures(current.procIdx) = procedures(current.procIdx) + 1
                } else current match {
                    case WhileState(procIdx, line, _, _, _) => {
                        val procCycles = cycles(procIdx)
                        val iter = procCycles(line) 
                        if (iter == 1000)
                            return Result.InfiniteLoop
                        else 
                            cycles(procIdx) = procCycles.updated(line, iter + 1)
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

    def initial = new Interpreter(program, start)

    private[this] var current = start
    private[this] val opStack:      Stack[ConditionalState] = new Stack
    private[this] val procStack:    Stack[CallState]        = new Stack
}
