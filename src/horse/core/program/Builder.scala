package horse.core.program

import horse.core.operator._
import Interpreter.Program

object Builder {
    def apply(program: Program): State = {
        val indexedProcedures = program.zipWithIndex
        
        val startState: Map[String, StartState] = {
            for ((proc, idx) <- indexedProcedures) yield (proc.name, new StartState(idx))
        }.toMap

        for ((proc, procIdx) <- indexedProcedures) {
            def step(i: Int): (State, Int) = {
                if (i == proc.operators.length) 
                    (TerminalState(procIdx), i + 1)
                else proc.operators(i) match {
                    case op: SimpleOperator => {
                        val (next, rest) = step(i + 1) 
                        (SimpleState(procIdx, i + 1, op, next), rest)
                    }
                    case While(c) => {
                        val (body, withoutWhileBody) = step(i + 1)
                        val (next, rest) = step(withoutWhileBody + 1)
                        (WhileState(procIdx, i + 1, c, body, next), rest)
                    }
                    case If(c) => {
                        val (trueBranch, withoutIfBody) = step(i + 1)
                        proc.operators(withoutIfBody) match {
                            case End => {
                                val (next, rest) = step(withoutIfBody + 1)
                                (IfState(procIdx, i + 1, c, trueBranch, next), rest)
                            }
                            case Else => {
                                val (falseBranch, withoutElseBody) = step(withoutIfBody + 1)
                                val (next, rest) = step(withoutElseBody + 1)
                                (IfElseState(procIdx, i + 1, c, trueBranch, falseBranch, next),
                                rest)
                            }
                        }
                    }
                    case Call(procName) => {
                        val (next, rest) = step(i + 1) 
                        (CallState(procIdx, i + 1, startState(procName), next), rest)
                    }
                    case Else       => (EndState(procIdx, i + 1), i)
                    case End        => (EndState(procIdx, i + 1), i) 
                }
            }
            val (state, n) = step(0)
            assert(n == proc.operators.length + 1)
            startState(proc.name).next = state
        }
        startState(program.head.name)
    }
}
