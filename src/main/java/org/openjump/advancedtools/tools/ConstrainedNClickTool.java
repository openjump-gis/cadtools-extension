package org.openjump.advancedtools.tools;

/**
 * @author Giuseppe Aruta. Adapted from OpenJUMP ConstrainedClickTool class
 * @since OpenJUMP 1.10 
 */
import java.awt.event.MouseEvent;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import org.openjump.core.ui.plugin.edittoolbox.cursortools.ConstrainedMultiClickTool;

import org.locationtech.jts.geom.Coordinate;

public abstract class ConstrainedNClickTool extends ConstrainedMultiClickTool {

    protected int n;

    public ConstrainedNClickTool(WorkbenchContext wc, int n) {
        super(wc);
        this.n = n;
    }

    public int numClicks() {
        return this.n;
    }

    protected Coordinate getModelSource() {
        return (Coordinate) getCoordinates().get(0);
    }

    protected Coordinate getModelDestination() {
        return (Coordinate) getCoordinates().get(this.n - 1);
    }

    @Override
    protected boolean isFinishingRelease(MouseEvent e) {
        return ((e.getClickCount() == 1) && (shouldGestureFinish()))
                || (super.isFinishingRelease(e));
    }

    private boolean shouldGestureFinish() {
        return getCoordinates().size() == this.n;
    }
}
