import scala.swing.event._
import javax.swing.text._
import java.awt.Color

class Editor(   enabled: () => Boolean, switchMode: () => Unit,
                parentProcessKey: Key.Value => Unit  ) extends scala.swing.EditorPane {

    private[this] object Row {
        private val tab = "   "
    }

    private class Row(val indent: Int, val operator: Operator, var offset: Int) {
        import Row.tab

        def text = {
            val sb = new StringBuffer
            for (i <- 0 until indent)
                sb.append(tab)
            sb.append(operator.text)
            sb.toString
        }

        def realTextLength = operator.text.length
        def realTextOffset = offset + indent * tab.size

        def endOffset = realTextOffset + realTextLength + 1
    }

    editable = false
    listenTo(keys)
    editorKit = new StyledEditorKit
    
    background = Color.darkGray

    private[this] val document = peer.getDocument.asInstanceOf[StyledDocument] 

    private[this] val default = new SimpleAttributeSet
    private[this] val highlight = new SimpleAttributeSet
    private[this] val keyword = new SimpleAttributeSet

    StyleConstants.setFontSize(default, 10);
    StyleConstants.setForeground(default, Color.white);
    StyleConstants.setBold(default, false);

    StyleConstants.setFontSize(highlight, 12);
    StyleConstants.setForeground(keyword, Color.yellow);
    StyleConstants.setBold(keyword, true);
    
    private[this] var prevRows: List[Row] = List()
    private[this] var nextRows: List[Row] = List()
    
    private def createRow(op: Operator): Row = {
        val row = 
            if (prevRows.isEmpty)
                new Row(0, op, 0)
            else {
                val prev = prevRows.head
                var indent = prev.operator match {
                    case _: ConditionalOperator => prev.indent + 1
                    case Else => prev.indent + 1
                    case _ => prev.indent
                }
                new Row(indent, op, prev.endOffset)
            }
        prevRows = row :: prevRows
        update()
        return row
    }

    private def invertCondition(op: ConditionalOperator) {
        val row = prevRows.head
        val inverted = op match {
            case If(c) => new If(Condition.not(c))
            case While(c) => new While(Condition.not(c))
        }
        prevRows = new Row(row.indent, inverted, row.offset) :: prevRows.tail
    }

    private def up() {
        nextRows = prevRows.head :: nextRows
        prevRows = prevRows.tail
        updateSelection()
    }

    private def down() {
        prevRows = nextRows.head :: prevRows
        nextRows = nextRows.tail
        updateSelection()
    }

    private def update() {
        updateText()
        updateSelection()
    }

    private def updateText() {
        val sb = new StringBuffer
        var offset = 0
        for (row <- prevRows.reverse ++ nextRows) {
            val rowText = row.text
            sb.append(rowText).append("\n")
            row.offset = offset
            offset += rowText.size + 1
        }
        text = sb.toString
    }

    private def updateSelection(selectCurrentLine: Boolean = true) {
        val txt = text
        document.setCharacterAttributes(0, txt.length, default, false)

        for (word <- List("if", "else", "while", "end", "then", "else")) {
            var index = txt.indexOf(word, 0);
            while (index != -1) {
                document.setCharacterAttributes(index, word.length, keyword, false)
                index = txt.indexOf(word, index + word.length + 1);
            }
        }

        if (selectCurrentLine) {
            val current = prevRows.head
            document.setCharacterAttributes(current.realTextOffset, current.realTextLength, highlight, false)
        }
    }

    private def processKey(key: Key.Value) = key match {
        case Key.Up => 
            if (prevRows.size > 1) 
                up()
        case Key.Down => 
            if (!nextRows.isEmpty)
                down()
        case Key.S => createRow(Step)
        case Key.L => createRow(TurnLeft)
        case Key.R => createRow(TurnRight)
        case Key.I => {
            val current = createRow(new If(Condition.wall))
            nextRows = new Row(current.indent, End, current.endOffset) :: nextRows
            update()
        }
        case Key.W => {
            val current = createRow(new While(Condition.empty))
            nextRows = new Row(current.indent, End, current.endOffset) :: nextRows
            update()
        }
        case Key.N => {
            if (!prevRows.isEmpty) {
                prevRows.head.operator match {
                    case op: ConditionalOperator => {
                        invertCondition(op)
                        update()
                    }
                    case _ => ()
                }
            }
        }
        case Key.E => prevRows match {
            case current :: tail => {
                if (current.operator == End) {
                    val correspondingOperator = tail.find(_.indent == current.indent).get.operator
                    correspondingOperator match {
                        case If(_) => {
                            prevRows = new Row(current.indent, Else, tail.head.endOffset) :: tail
                            nextRows = current :: nextRows
                            update()
                        }
                    }
                }
            }
            case _ => ()
        }
        case Key.Delete => deleteOperator()
        case Key.Escape => {
            if (!prevRows.isEmpty)
                updateSelection(false)
            switchMode()
        }
        case _ => ()
    }

    reactions += {
        case e: KeyReleased =>  {
            if (enabled()) {
                processKey(e.key)
            } else {
                parentProcessKey(e.key)
            }
        }
    }

    private def deleteOperator() {
        prevRows match {
            case current :: tail => {
                prevRows = tail
                current.operator match {
                    case _: SimpleOperator => ()
                    case End => {
                        while (prevRows.head.indent != current.indent) {
                            prevRows = prevRows.tail
                        }
                        prevRows = prevRows.tail
                    }
                    case While(_) => {
                        while (nextRows.head.indent != current.indent) {
                            nextRows = nextRows.tail
                        }
                        nextRows = nextRows.tail
                    }
                    case If(c) => {
                        while (nextRows.head.indent != current.indent) {
                            nextRows = nextRows.tail
                        }
                        if (nextRows.head.operator == End) {
                            nextRows = nextRows.tail
                        } else {
                            nextRows = new Row(current.indent, If(Condition.not(c)), current.offset) :: nextRows.tail
                        }
                    }
                    case Else => {
                        while (nextRows.head.indent != current.indent) {
                            nextRows = nextRows.tail
                        }
                        prevRows = nextRows.head :: prevRows
                        nextRows = nextRows.tail
                    }
                }
                if (prevRows.isEmpty) {
                    nextRows match {
                        case head :: tail => {
                            prevRows = List(head)
                            nextRows = tail
                            update()
                        }
                        case _ => updateText()
                    }
                } else {
                    update()
                }
            }
            case _ => ()
        }
    }


    def prepare() {
        if (!prevRows.isEmpty) {
            nextRows = prevRows.reverse ++ nextRows
            prevRows = List(nextRows.head)
            nextRows = nextRows.tail
            updateSelection()
        }
    }

    def buildAST: AST = {
        buildAST(prevRows.reverse ++ nextRows) match {
            case (ast, List()) => ast
            case _ => error("build tree error")
        }
    }

    private def buildAST(rows: List[Row]): (AST, List[Row]) = {
        import AST._

        if (rows.isEmpty)
            (AST.Empty, rows)
        else rows.head.operator match {
            case op: SimpleOperator => {
                val (ast, tail) = buildAST(rows.tail) 
                (SeqAST(op, ast), tail)
            }
            case While(c) => {
                val (body, rowsWithoutWhileBody) = buildAST(rows.tail)
                val (ast, tail) = buildAST(rowsWithoutWhileBody.tail)
                (WhileAST(c, body, ast), tail)
            }
            case If(c) => {
                val (trueBranch, rowsWithoutIfBody) = buildAST(rows.tail)
                rowsWithoutIfBody.head.operator match {
                    case End => {
                        val (ast, tail) = buildAST(rowsWithoutIfBody.tail)
                        (IfAST(c, trueBranch, ast), tail)
                    }
                    case Else => {
                        val (falseBranch, rowsWithoutElseBody) = buildAST(rowsWithoutIfBody.tail)
                        val (ast, tail) = buildAST(rowsWithoutElseBody.tail)
                        (IfElseAST(c, trueBranch, falseBranch, ast), tail)
                    }
                }
            }
            case Else => (AST.Empty, rows)
            case End => (AST.Empty, rows)
        }
    }
}
