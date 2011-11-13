package horse.core.fieldstate

import scala.collection.mutable.Set

import horse.core.operator._

import Horse._

class DynamicField private (width: Int, height: Int) extends Field(width, height) {

    override def getPos     = pos
    override def getDir     = direction
    override def getShow    = show.toIterable

    def reset() {
        pos         = initialPos
        direction   = initialDir
        show.clear()
    }

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
            direction = Direction((direction.id + 3) % 4)
            true
        }
        case TurnRight => {
            direction = Direction((direction.id + 1) % 4)
            true
        }
    }

    private def move() = { 
        import Direction._
        
        direction match {
            case North  => new Pos(pos.x, pos.y - 1)
            case East   => new Pos(pos.x + 1, pos.y)
            case South  => new Pos(pos.x, pos.y + 1)
            case West   => new Pos(pos.x - 1, pos.y) 
        }
    }

    private def isValid(pos: Pos) = {
        (pos.x >= 0) && 
        (pos.y >= 0) && 
        (pos.x < width) && 
        (pos.y < height)
    }

    private def initialPos = new Pos(0, height - 1) 
    private def initialDir = Direction.North

    private[this] var pos                   = initialPos
    private[this] var direction             = initialDir
    private[this] val show: Set[(Pos, Pos)] = Set.empty
}


object DynamicField {
    import horse.Config

    val width   = Config.getInt("field.width")
    val height  = Config.getInt("field.height")

    def empty = new DynamicField(width, height)
}
