package horse.programtext

import scala.collection.mutable.Buffer

import horse.gui.{Document, IDocument}
import horse.core.operator._

class Editor(prog: Buffer[Procedure], document: IDocument) extends IEditor {

    // Interface 

    override def step()          { addOperators(Step)      }
    override def jump()          { addOperators(Jump)      }
    override def turnLeft()      { addOperators(TurnLeft)  }
    override def turnRight()     { addOperators(TurnRight) }

    override def createIf()      { addOperators(If(Condition.wall),     End) }
    override def createWhile()   { addOperators(While(Condition.empty), End) }

    override def createCall(procName: String) { addOperators(Call(procName)) }

    override def createElse() {
        val procedure = prog(procIdx).lines

        if ((lineIdx != 0) && (lineIdx - 1 != procedure.length)) {
            val line = procedure(lineIdx - 1)
            if (line.operator == End) {
                val corresponding = procedure(procedure.lastIndexWhere(_.indent == line.indent, lineIdx - 2))
                if (corresponding.operator.isInstanceOf[If]) {
                    val globalLine = currentLine

                    procedure.insert(lineIdx - 1, new Line(line.indent, Else))
                    document.insert(Else, line.indent, globalLine)

                    import Document.Background._
                    document.setBackground(globalLine,      Selected)
                    document.setBackground(globalLine + 1,  Default)
                }
            }
        }
    }

    override def inverse() {
        val proc = prog(procIdx).lines
        if ((lineIdx != 0) && (lineIdx - 1 != proc.size)) {
            val line = proc(lineIdx - 1)

            def replaceOp(op: Operator) {
                val globalLine = currentLine

                proc.remove(lineIdx - 1)
                document.remove(globalLine)

                proc.insert(lineIdx - 1, new Line(line.indent, op))
                document.insert(op, line.indent, globalLine)

                document.setBackground(globalLine, Document.Background.Selected)
            }
            line.operator match {
                case If(c)      => replaceOp(If   (Condition.not(c)))
                case While(c)   => replaceOp(While(Condition.not(c)))
                case _          => ()
            }
        }
    }

    override def prepare() {
        document.setBackground(currentLine, Document.Background.Selected)
    }

    override def release() {
        document.setBackground(currentLine, Document.Background.Default)
    }

    // Methods for ProgramText

    private[programtext] def up() {
        if (lineIdx == 0) {
            if (procIdx != 0) {
                moveCurrentLine(prog(procIdx - 1).lines.length, procIdx - 1)
            }
        } else {
            moveCurrentLine(lineIdx - 1)
        }
    }

    private[programtext] def down() {
        if (lineIdx == prog(procIdx).lines.length) {
            if (procIdx + 1 != prog.size)
                moveCurrentLine(0, procIdx + 1)
        } else {
            moveCurrentLine(lineIdx + 1)
        }
    }

    private[programtext] def moveToBegin() {
        moveCurrentLine(0, 0)
    }

    private[programtext] def removeLine() {
        if ((lineIdx == 0) || (lineIdx - 1 == prog(procIdx).lines.length)) {
            if (procIdx != 0) {
                removeProcedure()
            }
        } else {
            removeOperator(prog(procIdx).lines)
        }
    }

    // Private methods
    import Document.Background

    private def addOperators(ops: Operator*) {
        val globalLine = currentLine
        val indent = getIndent
        
        prog(procIdx).lines.insertAll(lineIdx, ops.map(new Line(indent, _)))
        document.insertAll(ops, indent, globalLine + 1)

        lineIdx += 1

        document.setBackground(globalLine, Background.Default)
        document.setBackground(globalLine + 1, Background.Selected)
    }

    private def currentLine = {
        var line = lineIdx
        for (i <- 0 until procIdx) {
            line += prog(i).lines.length + 3
        }
        line
    }

    private def getIndent = {
        if (lineIdx == 0) {
            1
        } else {
            val lines = prog(procIdx).lines
            if (lineIdx - 1 == lines.length) {
                1   
            } else {
                val line = lines(lineIdx - 1)
                line.operator match {
                    case _: ConditionalOperator => line.indent + 1
                    case Else                   => line.indent + 1
                    case _                      => line.indent
                }
            }
        }
    }

    private def moveCurrentLine(line: Int, proc: Int = procIdx) {
        document.setBackground(currentLine, Background.Default)
        procIdx = proc
        lineIdx = line
        document.setBackground(currentLine, Background.Selected)
    }

    private def removeProcedure() {
        val proc = prog(procIdx)

        for (i <- 0 until prog.size; if i != procIdx) {
            for (line <- prog(i).lines) {
                line.operator match {
                    case Call(name) => 
                        if (name == proc.name) 
                            return
                    case _ => ()
                }
            }
        }

        var start = -1
        for (i <- 0 until procIdx) {
            start += prog(i).lines.size + 3
        }
        document.removeAll(start, proc.lines.size + 3) 

        prog.remove(procIdx)

        procIdx -= 1
        lineIdx = 0
        document.setBackground(currentLine, Document.Background.Selected)
    }

    private def removeOperator(procedure: Buffer[Line]) {
        var baseLine = 1
        for (i <- 0 until procIdx) {
            baseLine += prog(procIdx).lines.size + 3
        }

        def insert(idx: Int, indent: Int, op: Operator) {
            procedure.insert(idx, new Line(indent, op))
            document.insert(op, indent, baseLine + idx)
        }

        def remove(idx: Int) {
            procedure.remove(idx)
            document.remove(baseLine + idx)
        }

        def findCorrespondingOperator(delta: Int) = {
            var i = lineIdx - 1 + delta
            val indent = procedure(lineIdx - 1).indent 
            while (procedure(i).indent != indent) {
                procedure(i) = new Line(indent - 1, procedure(i).operator)
                document.shift(baseLine + i)
                i += delta
            }
            i
        }

        procedure(lineIdx - 1).operator match {
            case _: SimpleOperator => {
                remove(lineIdx - 1) 
            }
            case _: Call => {
                remove(lineIdx - 1)
            }
            case If(c) => {
                val i = findCorrespondingOperator(1)

                val toInsert = procedure(i).operator == Else
                val indent = procedure(i).indent
                
                remove(lineIdx - 1)
                remove(i - 1)
                if (toInsert) {
                    insert(i - 1, indent, If(Condition.not(c))) 
                }                         
            }
            case Else => {
                val i = findCorrespondingOperator(1)
                val indent = procedure(i).indent
                remove(lineIdx - 1)
                remove(i - 1)
                insert(lineIdx - 1, indent, End)
            }
            case While(_) => {
                val i = findCorrespondingOperator(1)
                remove(lineIdx - 1)
                remove(i - 1)
            }
            case End => {
                val i = findCorrespondingOperator(-1)

                val indent = procedure(i).indent
                val toInsert = procedure(i).operator == Else

                remove(lineIdx - 1)
                remove(i)
                lineIdx -= 1
                if (toInsert) {
                    insert(i, indent, End)
                }
            }
        }
        if (lineIdx == procedure.length + 1)
            lineIdx -= 1
        document.setBackground(currentLine, Background.Selected)
    }

    // Fields
    private[this] var procIdx = 0
    private[this] var lineIdx = 0
}
