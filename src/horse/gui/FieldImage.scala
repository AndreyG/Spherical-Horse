package horse.gui

import scala.swing.Component
import java.io.File
import java.awt.geom.Line2D.{Double => Line}
import javax.imageio.ImageIO

import horse.core.fieldstate.{Field, Horse}
import java.awt.{Image, Color, Graphics2D, BasicStroke}

class FieldImage(field: Field) extends Component {

    type DirectionType = Horse.Direction.Value

    import Horse.Direction._

    private[this] val horse: Map[DirectionType, Image] = Map(
        North -> "north",
        East -> "east",
        South -> "south",
        West -> "west"
    ).mapValues(dir => ImageIO.read(new File(new File("image"), "horse-" + dir + ".jpeg")))

    def xCellToReal(col: Int) = col * 1.0 / field.width  * size.width 
    def yCellToReal(row: Int) = row * 1.0 / field.height * size.height 

    override def paint(g: Graphics2D) {
        for (row <- 0 to field.height) {
            val y = yCellToReal(row) 
            g.draw(new Line(0, y, size.width, y))
        }
        for (col <- 0 to field.width) {
            val x = xCellToReal(col)
            g.draw(new Line(x, 0, x, size.height))
        }

        val cellWidth:  Int = xCellToReal(1).toInt
        val cellHeight: Int = yCellToReal(1).toInt

        g.setColor(Color.blue)
        g.setStroke(new BasicStroke(5))
        for ((p1, p2) <- field.getShow) {
            g.draw(new Line(xCellToReal(p1.x) + cellWidth / 2, yCellToReal(p1.y) + cellHeight / 2, 
                            xCellToReal(p2.x) + cellWidth / 2, yCellToReal(p2.y) + cellHeight / 2))
        }

        g.drawImage(horse(field.getDir), 
                    xCellToReal(field.getPos.x).toInt, 
                    yCellToReal(field.getPos.y).toInt,
                    cellWidth, cellHeight, null)  
    }
}
