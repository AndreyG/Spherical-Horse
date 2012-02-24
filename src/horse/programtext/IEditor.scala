package horse.programtext

package editor {
    abstract class Action

    case object Step extends Action
    case object Jump extends Action
    case object Left extends Action
    case object Right extends Action
    case object If extends Action
    case object Else extends Action
    case object While extends Action
    case object Inverse extends Action
    sealed case class Call(procName: String) extends Action
    sealed case class NewProc(procName: String) extends Action
    case object Up extends Action
    case object Down extends Action
    case object Delete extends Action
}

trait IEditor {
    def exec(a: editor.Action)

    def prepare()
    def release()
}
