package horse.programtext

import scala.swing.Component
import horse.core.program.Interpreter.Program

trait IEditor {
    def step()
    def jump()
    def turnLeft()
    def turnRight()
    def createIf()
    def createElse()
    def createWhile()
    def inverse()
    def createCall(procName: String)

    def prepare()
    def release()
}

trait IHighlightor {
    object ProgramState extends Enumeration {
        val Normal, Error, End = Value
    }

    def apply(proc: Int, line: Int, state: ProgramState.Value) 

    def prepare()
    def release()
}

trait IProgramText {
    def getPane:           Component
    def getEditor:         IEditor
    def getHighlightor:    IHighlightor

    def program: Program
    def program_=(prog: Program)

    def getProcNames: Seq[String]      
    def addProcedure(name: String)
}
