package horse

import core.operator.SimpleOperator
import core.fieldstate.DynamicField
import gui.FieldImage

class Player(field: DynamicField, image: FieldImage) {
    def move(op: SimpleOperator) {
        field(op)
        image.repaint()
    }

    def clearField() {
        field.reset()
        image.repaint()
    }
}
