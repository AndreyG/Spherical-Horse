package horse.core.fieldstate

import scala.collection.mutable.{Set, HashSet}

import Horse._

class StaticField(  width: Int, height: Int, pos: Pos, 
                    dir: Direction.Value, plainShow: Show ) extends Field(width, height) {
    override def getPos = pos
    override def getDir = dir
    override def getShow = plainShow

    def equals(field: Field): Boolean = {
        if ((pos != field.getPos) || (dir != field.getDir))
            return false

        val otherShow = field.getShow
        if (show.size != otherShow.size) 

        for (seg <- otherShow) {
            if (!show.contains(seg))
                return false
        }
        true
    }

    private def equals(p1: Pos, p2: Pos) = {
        (p1.x == p2.x) && (p1.y == p2.y)
    }

    private[this] def show: Set[(Pos, Pos)] = new HashSet
    show ++= plainShow
}
