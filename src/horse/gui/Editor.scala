package horse.gui

import scala.collection.mutable.ArrayBuffer
import scala.swing.event.{KeyPressed, Key}
import scala.swing.EditorPane
import javax.swing.text._
import java.awt.Color

import horse.core.operator._
import horse.Config

object Editor extends EditorPane {

    // Interface
    def program: operator.Program = lines
    def program_=(program: operator.Program) {
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
            val correspondingOperator = lines.lastIndexWhere(_.isInstanceOf[ConditionalOperator], currentLine)
            if ((correspondingOperator > 0) && (lines(correspondingOperator).isInstanceOf[If])) {
                val indent = indents(currentLine)
                insertString(Else, indent, currentLineAttributes)
                lines.insert(currentLine, Else)
                indents.insert(currentLine, indent)
                highlight(currentLine + 1, defaultAttributes)
            }
        }
    }

    def inverse() {
        def replaceOp(op: Operator) {
            document.remove(currentLine * textWidth, textWidth)
            lines.remove(currentLine)
            insertString(op, indents(currentLine), currentLineAttributes)
            lines.insert(currentLine, op)
        }
        lines(currentLine) match {
            case If(c)      => replaceOp(If   (Condition.not(c)))
            case While(c)   => replaceOp(While(Condition.not(c)))
            case _          => ()
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
        highlight(currentLine, unselectedLineAttributes)
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

    private[this] val defaultAttributes         = new SimpleAttributeSet
    private[this] val currentLineAttributes     = new SimpleAttributeSet
    private[this] val selectedLineAttributes    = new SimpleAttributeSet
    private[this] val unselectedLineAttributes  = new SimpleAttributeSet
    private[this] val keywordAttributes         = new SimpleAttributeSet

    private[this] val debuggedAttributes = Map (
        ProgramState.Normal -> new SimpleAttributeSet,
        ProgramState.Error  -> new SimpleAttributeSet,
        ProgramState.End    -> new SimpleAttributeSet
    )

    private[this] val tab = "   "

    private[this] var textWidth = 58

    // Constructor
    StyleConstants.setFontSize(defaultAttributes, 12);
    StyleConstants.setBackground(defaultAttributes, Color.darkGray)
    StyleConstants.setForeground(defaultAttributes, Color.white);

    StyleConstants.setFontSize(currentLineAttributes, 12);
    StyleConstants.setBackground(currentLineAttributes, Color.gray);
    StyleConstants.setForeground(currentLineAttributes, Color.white);

    StyleConstants.setBackground(unselectedLineAttributes,    Color.darkGray);
    StyleConstants.setBackground(selectedLineAttributes,      Color.gray);

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
    highlight(0, selectedLineAttributes)

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

        val maxWidth = (0 until lines.size).map(i => indents(i) * tab.size + textLen(lines(i))).max
        while (maxWidth >= textWidth) {
            textWidth *= 2
        }        
        for (i <- 0 until lines.size) {
            insertString(lines(i), indents(i), defaultAttributes, i)
        }
    }

    private def highlight(line: Int, attrs: AttributeSet) {
        document.setCharacterAttributes(line * textWidth, textWidth, attrs, false)
    }
         

    private def toString(op: Operator): String = op.toString

    //private def getString(op: Operator, indent: Int): String = {
        //val sb = new StringBuffer  
        //for (_ <- 0 until indent) {
            //sb.append("   ")
        //}
        //sb.append(toString(op))
        //if (sb.length + 1 > textWidth) {
            //textWidth *= 2
            //fillDocument()
        //} 
        //sb.append(Array.fill(textWidth - sb.length - 1)(' '))
        //sb.append('\n')
        //sb.toString
    //}

    private def moveCurrentLine(newValue: Int) {
        highlight(currentLine, unselectedLineAttributes)
        currentLine = newValue
        highlight(currentLine, selectedLineAttributes)
    }

    private def toString(c: Condition.Value) = c match {
        case Condition.wall     => Config.getString("wall")
        case Condition.empty    => Config.getString("empty")
    }
    
    private def textLen(c: Condition.Value): Int = toString(c).length

    private def textLen(op: Operator): Int = op match {
        case ProgramBegin   => Config.getString("program-begin").length  
        case ProgramEnd     => Config.getString("program-end").length
        case Step           => Config.getString("step").length
        case Jump           => Config.getString("jump").length
        case TurnLeft       => Config.getString("turn-left").length
        case TurnRight      => Config.getString("turn-right").length
        case If(c)          => Config.getString("if").length + 2 + textLen(c) + 2 + Config.getString("then").length
        case Else           => Config.getString("else").length
        case While(c)       => Config.getString("while").length + 2 + textLen(c) + 2 + Config.getString("do").length
        case End            => Config.getString("end").length
    }

    private def insertString(op: Operator, indent: Int, attrs: AttributeSet, line: Int = currentLine) {
        if (indent * tab.size + textLen(op) >= textWidth) {
            textWidth *= 2
            fillDocument()
        }
        var offset = line * textWidth
        for (i <- 0 until indent) {
            document.insertString(offset, tab, attrs)
            offset += tab.size
        }
        def addKeyword(name: String) {
            val txt = Config.getString(name)  
            document.insertString(offset, txt, attrs) 
            document.setCharacterAttributes(offset, txt.length, keywordAttributes, false)
            offset += txt.length
        }
        def addWord(name: String) {
            val txt = Config.getString(name)  
            document.insertString(offset, txt, attrs) 
            offset += txt.length
        }

        op match {
            case ProgramBegin   => addKeyword("program-begin")
            case ProgramEnd     => addKeyword("program-end")
            case Step           => addWord("step")
            case Jump           => addWord("jump")
            case TurnLeft       => addWord("turn-left")
            case TurnRight      => addWord("turn-right")
            case If(c)          => {
                addKeyword("if") 
                val condition = " (" + toString(c) + ") "
                document.insertString(offset, condition, attrs)
                offset += condition.length
                addKeyword("then")
            }
            case Else           => addKeyword("else")
            case While(c)       => {
                addKeyword("while") 
                val condition = " (" + toString(c) + ") "
                document.insertString(offset, condition, attrs)
                offset += condition.length
                addKeyword("do")
            }
            case End            => addKeyword("end")
        }
        val tail = new String(Array.fill((line + 1) * textWidth - offset - 1)(' ')) + "\n" 
        document.insertString(offset, tail, attrs)
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

    private def addLine(op: SimpleOperator) {
        document.setCharacterAttributes(currentLine * textWidth, textWidth, defaultAttributes, false)

        val indent = getIndent()

        currentLine += 1
        
        insertString(op, indent, currentLineAttributes)
        lines.insert(currentLine, op)
        indents.insert(currentLine, indent)
    }

    private def addConditionalOperator(op: ConditionalOperator) {
        document.setCharacterAttributes(currentLine * textWidth, textWidth, defaultAttributes, false)

        val indent = getIndent()
        
        currentLine += 1

        insertString(op, indent, currentLineAttributes)
        lines.insert(currentLine, op)
        indents.insert(currentLine, indent)

        insertString(End, indent, defaultAttributes, currentLine + 1)
        lines.insert(currentLine + 1, End)
        indents.insert(currentLine + 1, indent)
    }
}
