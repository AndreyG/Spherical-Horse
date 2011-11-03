package horse.gui

import scala.collection.mutable.ArrayBuffer
import swing.event.{KeyPressed, Key}
import swing.EditorPane
import java.awt.Color

import horse.core.operator._

class Editor() extends EditorPane {

    // Interface
    def getOperators: IndexedSeq[Operator] = lines
    def step() { addLine(Step) }
    def jump() { addLine(Jump) }
    def turnLeft() { addLine(TurnLeft) }
    def turnRight() { addLine(TurnRight) }

    def createIf() {
        val indent = getIndent
        currentLine += 1
        lines.insert(currentLine, If(Condition.wall), End)
        indents.insert(currentLine, indent, indent)

        throw new UnsupportedOperationException("I don't know how to update view")
    }

    def createWhile() {
        val indent = getIndent
        currentLine += 1
        lines.insert(currentLine, While(Condition.wall), End)
        indents.insert(currentLine, indent, indent)

        throw new UnsupportedOperationException("I don't know how to update view")
    }

    def createElse() {
        if (lines(currentLine) == End) {
            if (lines.lastIndexWhere(_.isInstanceOf[ConditionalOperator], currentLine - 1) >= 0) {
                lines.insert(currentLine, Else)
                indents.insert(currentLine, indents(currentLine))

                throw new UnsupportedOperationException("I don't know how to update view")
            }
        }
    }

    def inverse() {
        lines(currentLine) match {
            case If(c) => {
                lines(currentLine) = If(Condition.not(c))
                throw new UnsupportedOperationException("I don't know how to update view")
            }
            case While(c) => {
                lines(currentLine) = While(Condition.not(c))
                throw new UnsupportedOperationException("I don't know how to update view")
            };
            case _ => ()
        }
    }

    def prepare() {
        currentLine = 0

        throw new UnsupportedOperationException("I don't know how to update selection")
    }

    // Constructor
    background = Color.gray
    enabled = false

    lines += ProgramBegin
    indents += 0
    lines += ProgramEnd
    indents += 0

    reactions += {
        case KeyPressed(_, key, _, _) => key match {
            case Key.Up =>
                if (currentLine != 0)
                    up()
            case Key.Down =>
                if (currentLine != lines.size - 2)
                    down()
            case Key.Delete => deleteOperator()
            case _ => ()
        }
    }

    // Private methods
    private def deleteOperator() {
        throw new UnsupportedOperationException("Delete operator")
    }

    private def up() {
        currentLine -= 1

        throw new UnsupportedOperationException("I don't know how to update selection")
    }

    private def down() {
        currentLine += 1

        throw new UnsupportedOperationException("I don't know how to update selection")
    }

    private def getIndent: Int = {
        val indent = indents(currentLine)
        lines(currentLine) match {
            case ProgramBegin => indent + 1
            case If(_) => indent + 1
            case Else => indent + 1
            case While(_) => indent + 1
            case _ => indent
        }
    }

    private def addLine(op: Operator) {
        val indent = getIndent
        currentLine += 1
        indents.insert(currentLine, indent)
        lines.insert(currentLine, op)

        throw new UnsupportedOperationException("I don't know how to update view")
    }

    // Fields
    private[this] val indents: ArrayBuffer[Int] = new ArrayBuffer
    private[this] val lines: ArrayBuffer[Operator] = new ArrayBuffer
    private[this] var currentLine = 0
}