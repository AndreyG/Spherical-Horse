package horse.programtext

import swing.Component
import horse.core.program.Interpreter._

trait IProgramText {
  def getPane:           Component
  def getEditor:         IEditor
  def getHighlightor:    IHighlightor

  def program: Program
  def program_=(prog: Program)

  def getProcNames: Seq[String]
  def addProcedure(name: String)
}