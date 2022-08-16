package org.openjump.advancedtools.block;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import javax.swing.ImageIcon;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.UndoableCommand;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.openjump.advancedtools.CadExtension;

import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.NClickTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.FeatureDrawingUtil;

public class DrawBlockTool extends NClickTool {

    //public static final String blockFolder = "VertexImages";
    public static final String NAME = CadExtension.I18N
        .get("org.openjump.core.ui.plugins.block.DrawblockTool.description");

    private final FeatureDrawingUtil featureDrawingUtil;
    private final ImageIcon ICON = org.openjump.advancedtools.icon.IconLoader
            .icon("textblock/block_ins.png");

    final BlockPanel blockPanel;

    public DrawBlockTool(FeatureDrawingUtil featureDrawingUtil, BlockPanel blockPanel) {
        super(JUMPWorkbench.getInstance().getContext(), 1);
        this.featureDrawingUtil = featureDrawingUtil;
        setColor(Color.blue);
        setStroke(new BasicStroke(1.5F));
        allowSnapping();
        this.blockPanel = blockPanel;
    }

    public static CursorTool create(LayerNamePanelProxy layerNamePanelProxy, BlockPanel blockPanel) {
        FeatureDrawingUtil featureDrawingUtil = new FeatureDrawingUtil(
                layerNamePanelProxy);

        return featureDrawingUtil.prepare(
                new DrawBlockTool(featureDrawingUtil, blockPanel), true);
    }

    public void initialize(PlugInContext context) throws Exception {
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    @SuppressWarnings({})
    protected void gestureFinished() throws Exception {
        getWorkbench().getContext();
        // Get the click coordinates
        Coordinate cursorPt = (Coordinate) getCoordinates().get(0);

        // Build the geometry at the specific displacement, dimension and
        // rotation
        // Geometry geom2 = BlockUtils.getGeometry(context);
        //Geometry geom2 = BlockUtils.getGeometry();
        Geometry geom2 = blockPanel.getSelection();

        // Get the centroid coordinates of the geometry
        Coordinate coord = geom2.getCentroid().getCoordinate();

        // Calculate the displacement of the geometry
        Coordinate displacement = CoordUtil.subtract(cursorPt, coord);
        Integer rotation = (Integer) BlockPanel.rotationSpinner.getValue();
        Integer dimension = (Integer) BlockPanel.dimensionSpinner.getValue();
        GeometryUtils.rotate_clockwise_Geometry(geom2, rotation);
        GeometryUtils.scaleGeometry(geom2, dimension);
        GeometryUtils.centerGeometry(geom2, displacement);

        // TODO: [202208 ed] lots a symbols seem to not pass geom.isValid() anymore
        //       disabled the check, but am not sure this is the proper solution
        UndoableCommand cmd = featureDrawingUtil.createAddCommand(geom2,
            /*isRollingBackInvalidEdits()*/false, getPanel(), this);

        if (cmd!=null)
          execute(cmd);
    }

    @Override
    public ImageIcon getIcon() {
        return ICON;
    }

    /****************** other methods ********************************/

    /** Modify using #setDestination */
    protected Coordinate modelDestination = null;
    private Shape selectedFeaturesShape;

    /**
     * overwritten super method to show the block geometry on any mouse move
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        try {
            setViewDestination(e.getPoint());
            redrawShape();
        } catch (Throwable t) {
            getPanel().getContext().handleThrowable(t);
        }
    }

    /**** from drag tool ***/
    protected void setViewDestination(Point2D destination)
            throws NoninvertibleTransformException {
        this.setModelDestination(getPanel().getViewport().toModelCoordinate(
                destination));
    }

    protected void setModelDestination(Coordinate destination) {
        this.modelDestination = snap(destination);
    }

    /**
     * changed to get geometry around mouse pointer
     */
    @Override
    protected Shape getShape() {
        this.setColor(Color.RED.brighter());
        this.calculateShape(this.modelDestination, this.getPanel());
        return this.selectedFeaturesShape;
    }

    /**
     * called from constructor and by mouse move event
     * <p>
     * calculates a geometry around the mouse pointer and converts it to a java
     * shape
     * 
     * @param middlePoint
     *            coordinates of the circle
     */
    private void calculateShape(Coordinate middlePoint, LayerViewPanel panel) {
        getWorkbench().getContext();
        // Get the click coordinates
        Coordinate cursorPt = this.modelDestination;
        // get the geometry of the block
        Geometry geom2 = blockPanel.getSelection();
        // Get the centroid of the geometry
        Coordinate coord = geom2.getCentroid().getCoordinate();

        // Calculate the displacement of the geometry
        Coordinate displacement = CoordUtil.subtract(cursorPt, coord);
        Integer rotation = (Integer) BlockPanel.rotationSpinner.getValue();
        Integer dimension = (Integer) BlockPanel.dimensionSpinner.getValue();
        GeometryUtils.rotate_clockwise_Geometry(geom2, rotation);
        GeometryUtils.scaleGeometry(geom2, dimension);
        GeometryUtils.centerGeometry(geom2, displacement);

        try {
            this.selectedFeaturesShape = panel.getJava2DConverter().toShape(geom2);
        } catch (NoninvertibleTransformException e) {
            System.out.println("DrawCircleWithGivenRadiusTool:Exception " + e);
        }
    }

}