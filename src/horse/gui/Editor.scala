package horse.gui

import scala.collection.mutable.ArrayBuffer
import scala.swing.event.{KeyPressed, Key}
import scala.swing.EditorPane
import javax.swing.text.{StyledDocument, StyledEditorKit}
import java.awt.Color

import horse.core.operator._
import horse.core.program.Interpreter.Program

object Editor extends EditorPane {

    // Interface
    def program: Program = lines
    def program_=(program: Program) {
        lines.clear()
        lines ++= program
        indents.clear()
        indents += 0
        for (i <- 1 until lines.size - 1) {
            var indent = getIndent(i - 1)
            if ((lines(i) == End) || (lines(i) == Else))
                indent -= 1
            indents += indent
        }
        indents += 0
        fillDocument()
        moveCurrentLine(0)
    }

    def step()      { addLine(Step)      }
    def jump()      { addLine(Jump)      }
    def turnLeft()  { addLine(TurnLeft)  }
    def turnRight() { addLine(TurnRight) }

    def createIf()      { addConditionalOperator(If   (Condition.wall)) }
    def createWhile()   { addConditionalOperator(While(Condition.wall)) }

    def createElse() {
        if (lines(currentLine) == End) {
            val correspondingOperator = indents.lastIndexOf(indents(currentLine), currentLine - 1)
            if (lines(correspondingOperator).isInstanceOf[If]) {
                insert(Else, indents(currentLine), currentLine)

                import Document.Background._

                document.setBackground(currentLine, Selected)
                document.setBackground(currentLine + 1, Default)
            }
        }
    }

    def inverse() {
        def replaceOp(op: Operator) {
            lines.remove(currentLine)
            document.remove(currentLine)
            lines.insert(currentLine, op)
            document.insert(op, indents(currentLine), currentLine)

            document.setBackground(currentLine, Document.Background.Selected)
        }
        lines(currentLine) match {
            case If(c)      => replaceOp(If   (Condition.not(c)))
            case While(c)   => replaceOp(While(Condition.not(c)))
            case _          => ()
        }
    }

    def prepare() { 
        moveCurrentLine(0) 
    }

    object ProgramState extends Enumeration {
        val Normal, Error, End = Value
    }

    def highlightOperator(line: Int, state: ProgramState.Value) {
        import Document.Background

        document.setBackground(currentLine, Background.Default)
        currentLine = line
        document.setBackground(currentLine, state match {
            case ProgramState.Normal    => Background.Debugged
            case ProgramState.Error     => Background.Error
            case ProgramState.End       => Background.Success
        })
    }

    // Private methods
    import Document.Background

    private def up() {
        if (currentLine != 0)
           moveCurrentLine(currentLine - 1) 
    }

    private def down() {
        if (currentLine != lines.size - 2)
            moveCurrentLine(currentLine + 1)
    }

    private def moveCurrentLine(newValue: Int) {
        document.setBackground(currentLine, Background.Default)
        currentLine = newValue
        document.setBackground(currentLine, Background.Selected)
    }

    private def deleteOperator() {
        def remove(i: Int) {
            lines.remove(i)
            indents.remove(i)
            document.remove(i)
        }

        def findCorrespondingOperator(delta: Int) = {
            var i = currentLine + delta
            while (indents(i) != indents(currentLine)) {
                indents(i) = indents(i) - 1
                document.shift(i)
                i += delta
            }
            i
        }

        if (currentLine != 0) {
            lines(currentLine) match {
                case _: SimpleOperator => {
                    remove(currentLine) 
                }
                case If(c) => {
                    val i = findCorrespondingOperator(1)

                    val toInsert = lines(i) == Else
                    
                    remove(currentLine)
                    remove(i - 1)
                    if (toInsert) {
                        insert(If(Condition.not(c)), indents(currentLine), i - 1)
                    }                         
                }
                case Else => {
                    val i = findCorrespondingOperator(1)
                    val indent = indents(currentLine)
                    remove(currentLine)
                    remove(i - 1)
                    insert(End, indent, currentLine)
                }
                case While(_) => {
                    val i = findCorrespondingOperator(1)
                    remove(currentLine)
                    remove(i - 1)
                }
                case End => {
                    val i = findCorrespondingOperator(-1)

                    val indent = indents(i)
                    val toInsert = lines(i) == Else

                    remove(currentLine)
                    remove(i)
                    currentLine -= 1
                    if (toInsert) {
                        insert(End, indent, i)
                    }
                }
            }
            if (currentLine == lines.size - 1)
                currentLine -= 1
            document.setBackground(currentLine, Background.Selected)
        }
    }

    private def fillDocument() {
        document.clear()
        for (i <- 0 until lines.size) {
            document.insert(lines(i), indents(i), i)
        }
    }

    private def getIndent(i: Int = currentLine) = {
        val indent = indents(i)
        lines(i) match {
            case ProgramBegin => indent + 1
            case If(_) => indent + 1
            case Else => indent + 1
            case While(_) => indent + 1
            case _ => indent
        }
    }

    private def insert(op: Operator, indent: Int, line: Int) {
        lines.insert(line, op)
        indents.insert(line, indent)
        document.insert(op, indent, line)
    }

    private def addLine(op: SimpleOperator) {
        document.setBackground(currentLine, Background.Default)

        val indent = getIndent()

        currentLine += 1
        
        insert(op, indent, currentLine)
        document.setBackground(currentLine, Background.Selected)
    }

    private def addConditionalOperator(op: ConditionalOperator) {
        document.setBackground(currentLine, Background.Default)

        val indent = getIndent()
        
        currentLine += 1

        insert(op, indent, currentLine)
        insert(End, indent, currentLine + 1)

        document.setBackground(currentLine, Background.Selected)
    }

    // Fields
    private[this] val indents: ArrayBuffer[Int] = new ArrayBuffer
    private[this] val lines: ArrayBuffer[Operator] = new ArrayBuffer
    private[this] var currentLine = 0
    
    editorKit = new StyledEditorKit
    private[this] val document: IDocument = new Document(peer.getDocument.asInstanceOf[StyledDocument]) 

    // Constructor

    background = Color.darkGray
    editable = false
    focusable = true

    lines += ProgramBegin
    indents += 0
    lines += ProgramEnd
    indents += 0

    fillDocument()
    document.setBackground(0, Background.Selected)

    listenTo(keys)
    reactions += {
        case KeyPressed(_, Key.Up,      _, _) => up()
        case KeyPressed(_, Key.Down,    _, _) => down()
        case KeyPressed(_, Key.Delete,  _, _) => deleteOperator()
    } 
}
