package sc.brainflow.app.commands

import sc.brainflow.core.{ImageView, AbstractImageView}
import com.pietschy.command.toggle.ToggleCommand
import brainflow.image.anatomy.Anatomy3D
import com.pietschy.command.ActionCommand

/**
 * Created by IntelliJ IDEA.
 * User: Brad
 * Date: Sep 12, 2010
 * Time: 8:43:58 PM
 * To change this template use File | Settings | File Templates.
 */




class SwitchAxialCommand(val view: AbstractImageView) extends ToggleCommand {
  def handleSelection(b: Boolean) = {
    if (b && !view.selectedPlot().displayAnatomy.isAxial) {
      view.selectedPlot().plotAnatomy := Anatomy3D.getCanonicalAxial
    }
  }
}

class SwitchCoronalCommand(val view: AbstractImageView) extends ToggleCommand {
  def handleSelection(b: Boolean) = {
    if (b && !view.selectedPlot().displayAnatomy.isCoronal) {
      view.selectedPlot().plotAnatomy := Anatomy3D.getCanonicalCoronal
    }
  }
}

class SwitchSagittalCommand(val view: AbstractImageView) extends ToggleCommand {
  def handleSelection(b: Boolean) = {
    if (b && !view.selectedPlot().displayAnatomy.isSagittal) {
      view.selectedPlot().plotAnatomy := Anatomy3D.getCanonicalSagittal
    }
  }
}