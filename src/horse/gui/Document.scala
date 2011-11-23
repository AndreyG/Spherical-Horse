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
    def insertAll(ops: Seq[Operator], indent: Int, line: Int)

    def append(op: Operator, indent: Int)
    def appendEmptyLine()
    def appendProcedureBegin(name: String)
    def appendProcedureEnd  (name: String)

    def remove(line: Int)
    def removeAll(startLine: Int, linesNum: Int)

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

    override def removeAll(startLine: Int, linesNum: Int) {
        document.remove(startLine * textWidth, linesNum * textWidth)
        this.linesNum -= linesNum
    }

    override def shift(line: Int) {
        document.remove(line * textWidth, tab.size)
        document.insertString(line * textWidth + textWidth - tab.size - 1, tab, defaultFont)
    }

    override def append(op: Operator, indent: Int) {
        insert(op, indent, linesNum)
    }

    override def appendEmptyLine() {
        val offset = linesNum * textWidth
        val emptyLine = new String(Array.fill(textWidth - 1)(' ')) 

        document.insertString(offset, emptyLine + '\n', defaultFont)
        linesNum += 1
    }

    override def appendProcedureBegin(name: String) {
        val keyword = Config.getString("procedure") 

        if (keyword.length + 1 + name.length >= textWidth) {
            textWidth *= 2
            update()
        }
        
        var offset = linesNum * textWidth

        def addWord(word: String, font: AttributeSet) {
            document.insertString(offset, word, font)
            offset += word.length
        }

        addWord(keyword + " ", keywordFont)
        addWord(name, procNameFont)

        linesNum += 1

        val tail = new String(Array.fill(linesNum * textWidth - offset - 1)(' ')) + "\n" 
        document.insertString(offset, tail, defaultFont)
    }

    override def appendProcedureEnd(name: String) {
        val keyword = Config.getString("end") 
        val offset = linesNum * textWidth

        document.insertString(offset, keyword, keywordFont)

        val tail = new String(Array.fill(textWidth - keyword.length - 1)(' ')) + "\n" 
        document.insertString(offset + keyword.length, tail, defaultFont)

        linesNum += 1
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

        def addWord(txt: String, font: AttributeSet = defaultFont) {
            document.insertString(offset, txt, font) 
            offset += txt.length
        }

        def addCfgWord(name: String, font: AttributeSet = defaultFont) {
            addWord(Config.getString(name), font)  
        }

        op match {
            case Step           => addCfgWord("step")
            case Jump           => addCfgWord("jump")
            case TurnLeft       => addCfgWord("turn-left")
            case TurnRight      => addCfgWord("turn-right")
            case If(c)          => {
                addCfgWord("if", keywordFont) 
                addWord(" (" + toString(c) + ") ")
                addCfgWord("then", keywordFont)
            }
            case Else           => addCfgWord("else", keywordFont)
            case While(c)       => {
                addCfgWord("while", keywordFont) 
                addWord(" (" + toString(c) + ") ")
                addCfgWord("do", keywordFont)
            }
            case End            => addCfgWord("end", keywordFont)
            case Call(proc)     => {
                addCfgWord("call")
                addWord(" " + proc, procNameFont)
            }
        }

        val tail = new String(Array.fill((line + 1) * textWidth - offset - 1)(' ')) + "\n" 
        document.insertString(offset, tail, defaultFont)

        linesNum += 1
    }

    override def insertAll(ops: Seq[Operator], indent: Int, line: Int) {
        var i = 0
        for (op <- ops) {
            insert(op, indent, line + i)
            i += 1
        }
    }

    override def setBackground(line: Int, background: BackgroundType) {
        assert((line >= 0) && (line < linesNum))
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
        case Step           => Config.getString("step").length
        case Jump           => Config.getString("jump").length
        case TurnLeft       => Config.getString("turn-left").length
        case TurnRight      => Config.getString("turn-right").length
        case If(c)          => Config.getString("if").length + 2 + textLen(c) + 2 + Config.getString("then").length
        case Else           => Config.getString("else").length
        case While(c)       => Config.getString("while").length + 2 + textLen(c) + 2 + Config.getString("do").length
        case End            => Config.getString("end").length
        case Call(proc)     => Config.getString("call").length + 1 + proc.length
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
    private[this] val procNameFont  = new SimpleAttributeSet
    private[this] val defaultFont   = new SimpleAttributeSet

    StyleConstants.setForeground(defaultFont, Color.white)
    StyleConstants.setFontSize(defaultFont, 12)
    StyleConstants.setForeground(keywordFont, Color.yellow)
    StyleConstants.setFontSize(keywordFont, 12)
    StyleConstants.setBold(keywordFont, true)
    StyleConstants.setForeground(procNameFont, Color.white)
    StyleConstants.setFontSize(procNameFont, 12)
    StyleConstants.setItalic(procNameFont, true)

    StyleConstants.setBackground(attrs(Background.Default),     Color.darkGray  )
    StyleConstants.setBackground(attrs(Background.Selected),    Color.gray      )
    StyleConstants.setBackground(attrs(Background.Debugged),    Color.blue      )
    StyleConstants.setBackground(attrs(Background.Error),       Color.red       )
    StyleConstants.setBackground(attrs(Background.Success),     new Color(0, 160, 80))
}
