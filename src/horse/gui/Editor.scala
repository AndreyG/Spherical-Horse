package horse.gui

import scala.collection.mutable.ArrayBuffer
import scala.swing.event.{KeyPressed, Key}
import scala.swing.EditorPane
import javax.swing.text._
import java.awt.Color

import horse.core.operator._

object Editor extends EditorPane {

    // Interface
    def program: operator.Program = lines
    def program_=(program: operator.Program) {
        lines.clear()
        lines ++= program
        fillDocument()
        moveCurrentLine(0)
    }

    def step() { addLine(Step) }
    def jump() { addLine(Jump) }
    def turnLeft() { addLine(TurnLeft) }
    def turnRight() { addLine(TurnRight) }

    def createIf()      { addConditionalOperator(If   (Condition.wall)) }
    def createWhile()   { addConditionalOperator(While(Condition.wall)) }

    def createElse() {
        if (lines(currentLine) == End) {
            val correspondingOperator = lines.lastIndexWhere(_.isInstanceOf[ConditionalOperator], currentLine)
            if ((correspondingOperator > 0) && (lines(correspondingOperator).isInstanceOf[If])) {
                lines.insert(currentLine, Else)
                indents.insert(currentLine, indents(currentLine))
                document.insertString(  currentLine * textWidth, 
                                        getString(lines(currentLine), indents(currentLine)),
                                        currentLineAttributes   )
                highlight(currentLine + 1, defaultAttributes)
            }
        }
    }

    def inverse() {
        lines(currentLine) match {
            case If(c) => {
                document.remove(currentLine * textWidth, textWidth)
                lines(currentLine) = If(Condition.not(c))
                document.insertString(  currentLine * textWidth, 
                                        getString(lines(currentLine), indents(currentLine)), 
                                        currentLineAttributes   )
            }
            case While(c) => {
                document.remove(currentLine * textWidth, textWidth)
                lines(currentLine) = While(Condition.not(c))
                document.insertString(  currentLine * textWidth, 
                                        getString(lines(currentLine), indents(currentLine)), 
                                        currentLineAttributes   )
            }
            case _ => ()
        }
    }

    def prepare() { 
        requestFocus()
        moveCurrentLine(0) 
    }

    object ProgramState extends Enumeration {
        val Normal, Error, End = Value
    }

    def highlightOperator(line: Int, state: ProgramState.Value) {
        highlight(currentLine, defaultAttributes)
        currentLine = line
        highlight(currentLine, debuggedAttributes(state))
    }

    // Fields
    private[this] val indents: ArrayBuffer[Int] = new ArrayBuffer
    private[this] val lines: ArrayBuffer[Operator] = new ArrayBuffer
    private[this] var currentLine = 0
    
    private[this] val document = {
        editorKit = new StyledEditorKit
        peer.getDocument.asInstanceOf[StyledDocument] 
    }

    private[this] val defaultAttributes     = new SimpleAttributeSet
    private[this] val currentLineAttributes = new SimpleAttributeSet
    private[this] val keywordAttributes     = new SimpleAttributeSet

    private[this] val debuggedAttributes = Map (
        ProgramState.Normal -> new SimpleAttributeSet,
        ProgramState.Error  -> new SimpleAttributeSet,
        ProgramState.End    -> new SimpleAttributeSet
    )

    private[this] var textWidth = 58

    // Constructor
    StyleConstants.setFontSize(defaultAttributes, 12);
    StyleConstants.setBackground(defaultAttributes, Color.darkGray)
    StyleConstants.setForeground(defaultAttributes, Color.white);

    StyleConstants.setFontSize(currentLineAttributes, 12);
    StyleConstants.setBackground(currentLineAttributes, Color.gray);
    StyleConstants.setForeground(currentLineAttributes, Color.white);

    StyleConstants.setForeground(keywordAttributes, Color.yellow);
    StyleConstants.setBold(keywordAttributes, true);

    StyleConstants.setBackground(debuggedAttributes(ProgramState.Normal), Color.blue)
    StyleConstants.setBackground(debuggedAttributes(ProgramState.End),    new Color(0, 160, 80))
    StyleConstants.setBackground(debuggedAttributes(ProgramState.Error),  Color.red)

    background = Color.darkGray
    editable = false
    focusable = true

    lines += ProgramBegin
    indents += 0
    lines += ProgramEnd
    indents += 0

    fillDocument()
    highlight(0, currentLineAttributes)

    listenTo(keys)

    reactions += {
        case KeyPressed(_, Key.Up,      _, _) => up()
        case KeyPressed(_, Key.Down,    _, _) => down()
        case KeyPressed(_, Key.Delete,  _, _) => deleteOperator()
    }

    // Private methods
    private def up() {
        if (currentLine != 0)
           moveCurrentLine(currentLine - 1) 
    }

    private def down() {
        if (currentLine != lines.size - 2)
            moveCurrentLine(currentLine + 1)
    }

    private def deleteOperator() {
        throw new UnsupportedOperationException("Delete operator")
    }

    private def fillDocument() {
        document.remove(0, document.getLength)

        for ((op, indent) <- lines zip indents) {
            document.insertString(document.getLength, getString(op, indent), defaultAttributes)
        }
    }

    private def highlight(line: Int, attrs: AttributeSet) {
        document.setCharacterAttributes(line * textWidth, textWidth, attrs, false)
    }
         

    private def toString(op: Operator): String = op.toString

    private def getString(op: Operator, indent: Int): String = {
        val sb = new StringBuffer  
        for (_ <- 0 until indent) {
            sb.append("   ")
        }
        sb.append(toString(op))
        if (sb.length + 1 > textWidth) {
            textWidth *= 2
            fillDocument()
        } 
        sb.append(Array.fill(textWidth - sb.length - 1)(' '))
        sb.append('\n')
        sb.toString
    }

    private def moveCurrentLine(newValue: Int) {
        highlight(currentLine, defaultAttributes)
        currentLine = newValue
        highlight(currentLine, currentLineAttributes)
    }

    private def getIndent: Int = {
        val indent = indents(currentLine)
        println(currentLine)
        println(lines(currentLine))
        lines(currentLine) match {
            case ProgramBegin => indent + 1
            case If(_) => indent + 1
            case Else => indent + 1
            case While(_) => indent + 1
            case _ => indent
        }
    }

    private def addLine(op: SimpleOperator) {
        document.setCharacterAttributes(currentLine * textWidth, textWidth, defaultAttributes, false)

        val indent = getIndent

        currentLine += 1
        
        lines.insert(currentLine, op)
        indents.insert(currentLine, indent)
        
        document.insertString(currentLine * textWidth, getString(op, indent), currentLineAttributes)
    }

    private def addConditionalOperator(op: ConditionalOperator) {
        document.setCharacterAttributes(currentLine * textWidth, textWidth, defaultAttributes, false)

        val indent = getIndent
        
        currentLine += 1

        lines.insert(currentLine, op, End)
        indents.insert(currentLine, indent, indent)

        document.insertString(currentLine * textWidth,          getString(op, indent),  currentLineAttributes)
        document.insertString((currentLine + 1) * textWidth,    getString(End, indent), defaultAttributes)
    }
}
