package horse.programtext

import scala.collection.mutable.{Buffer, ArrayBuffer}
import scala.swing.event.Key

import horse.gui.{TextPane, Document}
import horse.core.operator.{Operator, ConditionalOperator, Else, End}
import horse.core.program.Interpreter.{Procedure => IProcedure, Program}

class Line(val indent: Int, val operator: Operator)

class Procedure(val name: String) {
    val lines: Buffer[Line] = new ArrayBuffer
}

class ProgramText extends IProgramText {
    
    // Interface
    override def getPane        = pane
    override def getEditor      = editor
    override def getHighlightor = new Highlightor(new IndexedSeq[Procedure] {
        override def apply(i: Int) = prog(i)
        override def length = prog.length
    }, document)

    override def program = {
        prog.map {
            proc => new IProcedure(proc.name, proc.lines.map(_.operator).toIndexedSeq)
        }.toIndexedSeq 
    }

    override def program_=(program: Program) {
        prog.clear()
        for (procedure <- program) {
            val proc = new Procedure(procedure.name)
            for (op <- procedure.operators) {
                val indent = {
                    if (proc.lines.isEmpty) 
                        1 
                    else {
                        val lastLine = proc.lines.last
                        var res = lastLine.operator match {
                            case _: ConditionalOperator => lastLine.indent + 1
                            case Else                   => lastLine.indent + 1
                            case _                      => lastLine.indent
                        }
                        if ((op == End) || (op == Else))
                            res -= 1
                        res
                    }
                }
                proc.lines += new Line(indent, op) 
            }
            prog += proc
        }
        printProgram()
        editor.moveToBegin()
    }

    override def getProcNames = prog.toSeq.map(_.name)

    override def addProcedure(name: String) {
        val proc = new Procedure(name)
        prog += proc

        document.appendEmptyLine()
        printProcedure(proc)
    }

    // Private methods

    private def printProcedure(procedure: Procedure) {
        document.appendProcedureBegin(procedure.name)
        for (line <- procedure.lines) {
            document.append(line.operator, line.indent)
        }
        document.appendProcedureEnd(procedure.name)
    }

    private def printProgram() {
        document.clear()
        printProcedure(prog.head)
        for (i <- 1 until prog.size) {
            document.appendEmptyLine()
            printProcedure(prog(i))
        }
    }

    // Fields
    private[this] val pane          = new TextPane
    private[this] val document      = new Document(pane.document) 
    private[this] val prog: Buffer[Procedure] = new ArrayBuffer
    private[this] val editor        = new Editor(prog, document)

    // Constructor
    prog += new Procedure("main")
    printProgram()

    pane.addKeyListener(Key.Up,     editor.up())
    pane.addKeyListener(Key.Down,   editor.down())
    pane.addKeyListener(Key.Delete, editor.removeLine())
}
