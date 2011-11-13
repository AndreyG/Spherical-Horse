package horse.core.fieldstate

object Horse {
    object Direction extends Enumeration {
        val North, East, South, West = Value
    }

    class Pos(val x: Int, val y: Int) {
        override def toString = "(%d, %d)".format(x, y)

        override def equals(other: Any) = other match {
            case that: Pos => (this.x == that.x) && (this.y == that.y)
            case _ => false
        }

        override def hashCode = (x, y).hashCode
    }

    type Show = Iterable[(Pos, Pos)]
}

import Horse._

abstract class Field(val width: Int, val height: Int) {
    def getPos: Pos
    def getDir: Direction.Value
    def getShow: Show
}
