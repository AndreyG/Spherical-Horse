package horse

import java.io.{File, FileInputStream}
import java.util.Properties

object Config {
    private[this] val properties = new Properties
    properties.load(new FileInputStream(new File("horse.conf")))

    def getString(name: String) = properties.getProperty(name)
    def getInt(name: String)    = getString(name).toInt
}
