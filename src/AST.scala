import scala.collection.mutable.Stack

abstract class AST 

object AST {
    case object Empty extends AST 
    sealed case class SeqAST(operator: SimpleOperator, tail: AST) extends AST

    abstract class ConditionalAST extends AST
    sealed case class IfAST(c: Condition.Value, body: AST, tail: AST) extends ConditionalAST
    sealed case class IfElseAST(c: Condition.Value, trueBranch: AST, falseBranch: AST, tail: AST) extends ConditionalAST 
    sealed case class WhileAST(c: Condition.Value, body: AST, tail: AST) extends ConditionalAST
}

object Interpretator {
    object ReturnCode extends Enumeration {
        val success, error, continue = Value
    }
}
     
class Interpretator(ast: AST) {
    import AST._
    import Interpretator.ReturnCode

    private[this] var current = ast
    private[this] val stack: Stack[ConditionalAST] = new Stack

    def step(state: StateMachine): ReturnCode.Value = current match {
        case Empty => {
            if (stack.isEmpty) {
                ReturnCode.success
            } else {
                stack.top match {
                    case IfAST(_, _, tail) => {
                        stack.pop()
                        current = tail
                        step(state)
                    }
                    case IfElseAST(_, _, _, tail) => {
                        stack.pop()
                        current = tail
                        step(state)
                    }
                    case WhileAST(c, body, tail) => {
                        if (state(c)) {
                            current = body
                            step(state)
                        } else {
                            stack.pop()
                            current = tail
                            step(state)
                        }
                    }
                }
            }
        }
        case SeqAST(operator: SimpleOperator, tail: AST) => {
            if (state(operator)) {
                current = tail
                ReturnCode.continue
            } else {
                ReturnCode.error
            }
        }
        case IfAST(c, body, tail) => {
            if (state(c)) {
                stack.push(current.asInstanceOf[IfAST])
                current = body
            } else {
                current = tail
            }
            step(state)
        }
        case IfElseAST(c, trueBranch, falseBranch, _) => {
            stack.push(current.asInstanceOf[IfElseAST])
            if (state(c)) {
                current = trueBranch
            } else {
                current = falseBranch
            }
            step(state)
        }
        case WhileAST(c, body, tail) => {
            if (state(c)) {
                stack.push(current.asInstanceOf[WhileAST])
                current = body
            } else {
                current = tail
            }
            step(state)
        }
    }

    def exec(state: StateMachine): ReturnCode.Value = {
        while (true) {
            val r = step(state)
            if (r != ReturnCode.continue) {
                stack.clear()
                current = ast
                return r
            }
        }
        error("The End of Infinity")
    }
}
