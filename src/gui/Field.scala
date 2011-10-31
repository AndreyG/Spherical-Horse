package gui

import scala.swing.Component
import java.io.File
import java.awt.geom.Line2D.{Double => Line}
import java.awt.{Color, Graphics2D, BasicStroke}
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

import core.{FieldState => State}

class Field(rowsNum: Int, colsNum: Int, state: State) extends Component {
    import core.HorseState.Direction._

    private[this] val horse = Map(
        North -> "north",
        East -> "east",
        South -> "south",
        West -> "west"
    ).mapValues(dir => ImageIO.read(new File(new File("image"), "horse-" + dir + ".jpeg")))

    def xCellToReal(col: Int) = col * 1.0 / colsNum * size.width 
    def yCellToReal(row: Int) = row * 1.0 / rowsNum * size.height 

    override def paint(g: Graphics2D) {
        for (row <- 0 to rowsNum) {
            val y = yCellToReal(row) 
            g.draw(new Line(0, y, size.width, y))
        }
        for (col <- 0 to colsNum) {
            val x = xCellToReal(col)
            g.draw(new Line(x, 0, x, size.height))
        }

        val cellWidth:  Int = xCellToReal(1).toInt
        val cellHeight: Int = yCellToReal(1).toInt

        g.setColor(Color.blue)
        g.setStroke(new BasicStroke(5))
        for ((p1, p2) <- state.getShow) {
            g.draw(new Line(xCellToReal(p1.x) + cellWidth / 2, yCellToReal(p1.y) + cellHeight / 2, 
                            xCellToReal(p2.x) + cellWidth / 2, yCellToReal(p2.y) + cellHeight / 2))
        }

        g.drawImage(horse(state.direction), 
                    xCellToReal(state.pos.x).toInt, yCellToReal(state.pos.y).toInt,
                    cellWidth, cellHeight, null)  
    }
}
