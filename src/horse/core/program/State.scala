package horse.core.program

import horse.core.operator._

abstract class State {
    val procIdx: Int 
}

sealed case class StartState(procIdx: Int) extends State {
    var next: State = null
}

sealed case class TerminalState(procIdx: Int) extends State

sealed abstract class OperatorState extends State {
    val line: Int
} 

sealed case class SimpleState(  procIdx: Int, 
                                line: Int, 
                                operator: SimpleOperator, 
                                next: State ) extends OperatorState 

sealed abstract class ConditionalState extends OperatorState 

sealed case class IfState    (  procIdx: Int, 
                                line: Int, 
                                condition: Condition.Value, 
                                body: State, 
                                next: State ) extends ConditionalState

sealed case class IfElseState(  procIdx: Int, 
                                line: Int, 
                                condition: Condition.Value, 
                                trueBranch: State, 
                                falseBranch: State, 
                                next: State ) extends ConditionalState 

sealed case class WhileState (  procIdx: Int, 
                                line: Int,
                                condition: Condition.Value, 
                                body: State,
                                next: State ) extends ConditionalState 

sealed case class EndState   (  procIdx: Int,
                                line: Int   ) extends OperatorState

sealed case class CallState  (  procIdx: Int,
                                line: Int, 
                                body: StartState,
                                next: State ) extends OperatorState
