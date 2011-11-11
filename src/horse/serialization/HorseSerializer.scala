package horse.serialization

import java.util.StringTokenizer

import horse.core.fieldstate.Horse.{Pos, Direction}

object HorseSerializer {
    private[serialization] def toString(x: Int, y: Int): String = x + " " + y
    private[serialization] def toString(pos: Pos): String = toString(pos.x, pos.y)
    private[serialization] def toString(p1: Pos, p2: Pos): String = {
        toString(p1) + " " + toString(p2)
    }

    private[serialization] def parseIntPair(str: String): (Int, Int) = {
        parseIntPair(new StringTokenizer(str))
    }

    private[serialization] def parsePos(str: String): Pos = {
        parsePos(new StringTokenizer(str))
    }

    private[serialization] def parseDirection(str: String): Direction.Value = {
        for (d <- Direction.values) {
            if (d.toString == str)
                return d
        }
        sys.error("unknown direction: " + str)
    }

    private[serialization] def parsePosPair(str: String) = {
        val st = new StringTokenizer(str)
        val p1 = parsePos(st)
        val p2 = parsePos(st)
        (p1, p2)
    }

    // -------------------------------------------------- //

    private[this] def parseIntPair(st: StringTokenizer): (Int, Int) = { 
        val x = st.nextToken.toInt 
        val y = st.nextToken.toInt 
        (x, y)
    }

    private[this] def parsePos(st: StringTokenizer): Pos = {
        val (x, y) = parseIntPair(st)
        new Pos(x, y)
    }
}
