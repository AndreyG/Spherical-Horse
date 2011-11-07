package horse.core

import scala.collection.mutable.{Buffer, ArrayBuffer}

object HorseState {
    object Direction extends Enumeration {
        val North, East, South, West = Value
    }

    class Pos(var x: Int, var y: Int)
}

import operator._

class FieldState(width: Int, height: Int) {
    import HorseState.Direction._
    import HorseState.Pos 

    var pos = new Pos(0, height - 1)
    var direction = North

    private[this] val show: Buffer[(Pos, Pos)] = new ArrayBuffer

    def getShow: Iterable[(Pos, Pos)] = show.toIterable

    def reset() {
        pos = new Pos(0, height - 1)
        direction = North 
        show.clear()
    }

    private def move() = direction match {
        case North => new Pos(pos.x, pos.y - 1)
        case East => new Pos(pos.x + 1, pos.y)
        case South => new Pos(pos.x, pos.y + 1)
        case West => new Pos(pos.x - 1, pos.y) 
    }

    private def isValid(pos: Pos) = (pos.x >= 0) && (pos.y >= 0) && (pos.x < width) && (pos.y < height)

    def apply(c: Condition.Value) = {
        isValid(move()) != (c == Condition.wall)
    }

    def apply(op: SimpleOperator) = op match {
        case Step => {
            val next = move()
            if (isValid(next)) {
                show += ((pos, next))
                pos = next
                true
            }
            else
                false
        }
        case Jump => {
            val next = move()
            if (isValid(next)) {
                pos = next
                true
            }
            else
                false
        }
        case TurnLeft => {
            direction = HorseState.Direction((direction.id + 3) % 4)
            true
        }
        case TurnRight => {
            direction = HorseState.Direction((direction.id + 1) % 4)
            true
        }
    }
}
