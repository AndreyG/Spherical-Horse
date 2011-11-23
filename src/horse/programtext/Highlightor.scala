package horse.programtext

import horse.gui.{Document, IDocument}

class Highlightor(prog: IndexedSeq[Procedure], document: IDocument) extends IHighlightor {

    override def apply(proc: Int, line: Int, state: ProgramState.Value) {
        import ProgramState._
        import Document.Background

        document.setBackground(currentLine, Background.Default)
        procIdx = proc
        lineIdx = line
        document.setBackground(currentLine, state match {
            case Normal    => Background.Debugged
            case Error     => Background.Error
            case End       => Background.Success
        })
    }

    override def prepare() {
        document.setBackground(0, Document.Background.Debugged)
    }

    override def release() {
        document.setBackground(currentLine, Document.Background.Default)
        procIdx = 0
        lineIdx = 0
    }

    // Private methods
    private def currentLine = {
        var line = lineIdx
        for (i <- 0 until procIdx) {
            line += prog(i).lines.length + 3
        }
        line
    }

    // Fields
    private[this] var procIdx = 0
    private[this] var lineIdx = 0
}
