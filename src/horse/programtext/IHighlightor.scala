package horse.programtext

trait IHighlightor {
  object ProgramState extends Enumeration {
    val Normal, Error, End = Value
  }

  def apply(proc: Int, line: Int, state: ProgramState.Value)

  def prepare()
  def release()
}