package core

import core.operator._

abstract class AST 

object AST {
    case object Empty extends AST 
    sealed case class SeqAST(operator: SimpleOperator, tail: AST) extends AST

    abstract class ConditionalAST extends AST
    sealed case class IfAST(c: Condition.Value, body: AST, tail: AST) extends ConditionalAST
    sealed case class IfElseAST(c: Condition.Value, trueBranch: AST, falseBranch: AST, tail: AST) extends ConditionalAST 
    sealed case class WhileAST(c: Condition.Value, body: AST, tail: AST) extends ConditionalAST

    def build(operators: List[Operator]): AST = {
        buildAST(operators) match {
            case (ast, List()) => ast
            case _ => sys.error("build tree error")
        }
    }

    private def buildAST(operators: List[Operator]): (AST, List[Operator]) = operators match {
        case (op: SimpleOperator) :: tail => {
            val (ast, rest) = buildAST(tail) 
            (SeqAST(op, ast), rest)
        }
        case While(c) :: tail => {
            val (body, withoutWhileBody) = buildAST(tail)
            val (ast, rest) = buildAST(withoutWhileBody.tail)
            (WhileAST(c, body, ast), rest)
        }
        case If(c) :: tail => {
            val (trueBranch, withoutIfBody) = buildAST(tail)
            withoutIfBody.head match {
                case End => {
                    val (ast, rest) = buildAST(withoutIfBody.tail)
                    (IfAST(c, trueBranch, ast), rest)
                }
                case Else => {
                    val (falseBranch, withoutElseBody) = buildAST(withoutIfBody.tail)
                    val (ast, rest) = buildAST(withoutElseBody.tail)
                    (IfElseAST(c, trueBranch, falseBranch, ast), rest)
                }
            }
        }
        case Else   :: _ => (AST.Empty, operators)
        case End    :: _ => (AST.Empty, operators)
        case _ => (AST.Empty, List()) 
    }
}
