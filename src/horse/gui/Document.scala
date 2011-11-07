package horse.gui

import javax.swing.text._
import java.awt.Color

import horse.core.operator._

import horse.Config

object Document {
    object Background extends Enumeration {
        val Default, Selected, Debugged, Error, Success = Value
    }

    type BackgroundType = Background.Value

    private val tab = "   "
}

trait IDocument {
    def insert(op: Operator, indent: Int, line: Int)
    def remove(line: Int)
    def shift(line: Int)
    
    def setBackground(line: Int, background: Document.BackgroundType)

    def clear()
}

class Document(document: StyledDocument) extends IDocument {
    import Document._

    //Interface
    override def clear() {
        document.remove(0, document.getLength)
        linesNum = 0
    }

    override def remove(line: Int) {
        document.remove(line * textWidth, textWidth)
        linesNum -= 1
    }

    override def shift(line: Int) {
        document.remove(line * textWidth, tab.size)
        document.insertString(line * textWidth + textWidth - tab.size - 1, tab, defaultFont)
    }

    override def insert(op: Operator, indent: Int, line: Int) {
        assert(line <= linesNum)

        if (indent * tab.size + textLen(op) >= textWidth) {
            textWidth *= 2
            update()
        }
        
        var offset = line * textWidth

        for (i <- 0 until indent) {
            document.insertString(offset, tab, defaultFont)
            offset += tab.size
        }

        def addWord(name: String, font: AttributeSet = defaultFont) {
            val txt = Config.getString(name)  
            document.insertString(offset, txt, font) 
            offset += txt.length
        }

        op match {
            case ProgramBegin   => addWord("program-begin", keywordFont)
            case ProgramEnd     => addWord("program-end", keywordFont)
            case Step           => addWord("step")
            case Jump           => addWord("jump")
            case TurnLeft       => addWord("turn-left")
            case TurnRight      => addWord("turn-right")
            case If(c)          => {
                addWord("if", keywordFont) 
                val condition = " (" + toString(c) + ") "
                document.insertString(offset, condition, defaultFont)
                offset += condition.length
                addWord("then", keywordFont)
            }
            case Else           => addWord("else", keywordFont)
            case While(c)       => {
                addWord("while", keywordFont) 
                val condition = " (" + toString(c) + ") "
                document.insertString(offset, condition, defaultFont)
                offset += condition.length
                addWord("do", keywordFont)
            }
            case End            => addWord("end", keywordFont)
        }

        val tail = new String(Array.fill((line + 1) * textWidth - offset - 1)(' ')) + "\n" 
        document.insertString(offset, tail, defaultFont)

        linesNum += 1
    }

    override def setBackground(line: Int, background: BackgroundType) {
        document.setCharacterAttributes(line * textWidth, textWidth, attrs(background), false)
    }

    // Private methods
    def update() {
        val spaces = new String(Array.fill(textWidth / 2)(' '))
        for (i <- 0 until linesNum) {
            document.insertString(i * textWidth + textWidth / 2 - 1, spaces, defaultFont)
        }
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

    // Fields
    private[this] var textWidth = 58
    private[this] var linesNum = 0 

    // Attributes
    private[this] val attrs: Map[BackgroundType, SimpleAttributeSet] = {
        for (b <- Background.values)
            yield (b, new SimpleAttributeSet)
    }.toMap

    private[this] val keywordFont   = new SimpleAttributeSet
    private[this] val defaultFont  = new SimpleAttributeSet

    StyleConstants.setForeground(defaultFont, Color.white)
    StyleConstants.setFontSize(defaultFont, 12)
    StyleConstants.setForeground(keywordFont, Color.yellow)
    StyleConstants.setFontSize(keywordFont, 12)
    StyleConstants.setBold(keywordFont, true);

    StyleConstants.setBackground(attrs(Background.Default),     Color.darkGray  )
    StyleConstants.setBackground(attrs(Background.Selected),    Color.gray      )
    StyleConstants.setBackground(attrs(Background.Debugged),    Color.blue      )
    StyleConstants.setBackground(attrs(Background.Error),       Color.red       )
    StyleConstants.setBackground(attrs(Background.Success),     new Color(0, 160, 80))
}
