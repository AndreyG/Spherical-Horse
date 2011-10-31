package gui

import scala.swing.event.Key
import scala.swing.ListView
import java.awt.Color

import core.operator._

class Editor(switchMode: () => Unit) extends ListView[Operator] {

    def getOperators: List[Operator] = prevLines.reverse ++ nextLines
    
    background = Color.darkGray
    selectionBackground = new Color(0, 100, 50)
    foreground = Color.white

    selection.intervalMode = ListView.IntervalMode.Single 
    enabled = false

    private[this] var prevLines: List[Operator] = List(ProgramBegin)
    private[this] var nextLines: List[Operator] = List(ProgramEnd)

    private[this] var currentLine = 0

    updateView()
    
    private def up() {
        nextLines = prevLines.head :: nextLines
        prevLines = prevLines.tail
        currentLine -= 1
        updateSelection()
    }

    private def down() {
        prevLines = nextLines.head :: prevLines
        nextLines = nextLines.tail
        currentLine += 1
        updateSelection()
    }

    def updateSelection() {
        selectIndices(currentLine)
    }

    def addLine(op: Operator) {
        prevLines = op :: prevLines
        currentLine += 1
        updateView()
    }

    def updateView() {
        listData = getOperators
        updateSelection()
    }

    def processKey(key: Key.Value) = key match {
        case Key.Up => 
            if (prevLines.size > 1) 
                up()
        case Key.Down => 
            if (nextLines.size > 1)
                down()
        case Key.S => addLine(Step)
        case Key.J => addLine(Jump)
        case Key.L => addLine(TurnLeft)
        case Key.R => addLine(TurnRight)
        case Key.I => {
            prevLines = new If(Condition.wall) :: prevLines
            nextLines = End :: nextLines
            currentLine += 1
            updateView()
        }
        case Key.W => {
            prevLines = new While(Condition.wall) :: prevLines
            nextLines = End :: nextLines
            currentLine += 1
            updateView()
        }
        case Key.N => prevLines match {
            case If(c)      :: tail => prevLines = If(Condition.not(c))     :: tail; updateView() 
            case While(c)   :: tail => prevLines = While(Condition.not(c))  :: tail; updateView() 
            case _ => ()
        }
        case Key.E => prevLines match {
            case End :: tail => {
                tail.find(_.isInstanceOf[ConditionalOperator]) match {
                    case Some(If(_)) => {
                        prevLines = Else :: tail
                        nextLines = End :: nextLines
                        updateView()
                    }
                    case _ => ()
                }
            }
            case _ => ()
        }
        case Key.Delete => deleteOperator()
        case Key.Escape => {
            switchMode()
        }
        case _ => ()
    }

    private def deleteOperator() {
        //prevLines match {
            //case current :: tail => {
                //prevRows = tail
                //current.operator match {
                    //case _: SimpleOperator => ()
                    //case End => {
                        //while (prevRows.head.indent != current.indent) {
                            //prevRows = prevRows.tail
                        //}
                        //prevRows = prevRows.tail
                    //}
                    //case While(_) => {
                        //while (nextRows.head.indent != current.indent) {
                            //nextRows = nextRows.tail
                        //}
                        //nextRows = nextRows.tail
                    //}
                    //case If(c) => {
                        //while (nextRows.head.indent != current.indent) {
                            //nextRows = nextRows.tail
                        //}
                        //if (nextRows.head.operator == End) {
                            //nextRows = nextRows.tail
                        //} else {
                            //nextRows = new Row(current.indent, If(Condition.not(c)), current.offset) :: nextRows.tail
                        //}
                    //}
                    //case Else => {
                        //while (nextRows.head.indent != current.indent) {
                            //nextRows = nextRows.tail
                        //}
                        //prevRows = nextRows.head :: prevRows
                        //nextRows = nextRows.tail
                    //}
                //}
                //if (prevRows.isEmpty) {
                    //nextRows match {
                        //case head :: tail => {
                            //prevRows = List(head)
                            //nextRows = tail
                            //update()
                        //}
                        //case _ => updateText()
                    //}
                //} else {
                    //update()
                //}
            //}
            //case _ => ()
        //}
    }

    def prepare() {
        getOperators match {
            case head :: tail => {
                prevLines = List(head)
                nextLines = tail
                currentLine = 0
                updateSelection()
            }
            case _ => ()
        }
    }
}
