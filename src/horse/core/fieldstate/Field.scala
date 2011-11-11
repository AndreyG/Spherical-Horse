package horse.core.fieldstate

object Horse {
    object Direction extends Enumeration {
        val North, East, South, West = Value
    }

    class Pos(val x: Int, val y: Int) 

    type Show = Iterable[(Pos, Pos)]
}

import Horse._

abstract class Field(val width: Int, val height: Int) {
    def getPos: Pos
    def getDir: Direction.Value
    def getShow: Show
}
