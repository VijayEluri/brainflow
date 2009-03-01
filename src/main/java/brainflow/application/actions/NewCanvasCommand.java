package brainflow.application.actions;

import brainflow.application.toplevel.DisplayManager;
import brainflow.application.toplevel.BrainFlow;
import brainflow.core.IBrainCanvas;

/**
 * Created by IntelliJ IDEA.
 * User: Brad
 * Date: Nov 9, 2008
 * Time: 7:13:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class NewCanvasCommand extends BrainFlowCommand {

    public NewCanvasCommand() {
        super("new-canvas");
    }

    protected void handleExecute() {
        IBrainCanvas canvas = DisplayManager.getInstance().newCanvas();
        BrainFlow.get().addCanvas(canvas);
    }
}