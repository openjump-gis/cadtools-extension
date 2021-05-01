/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI 
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * JUMP is Copyright (C) 2003 Vivid Solutions
 *
 * This program implements extensions to JUMP and is
 * Copyright (C) Stefan Steiniger.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 * Stefan Steiniger
 * perriger@gmx.de
 */
/*****************************************************
 * created:  		by Vivid Solutions
 * last modified:  	22.05.2005 by sstein
 * 					28.01.2006 new MultiInputDialog without cancel button
 * 
 * description:
 *  draws a circle for a given radius (showing the circle on mouse endpoint) 
 * 
 *****************************************************/
package org.openjump.advancedtools.tools;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openjump.advancedtools.config.CADToolsOptionsPanel;
import org.openjump.advancedtools.gui.CircleDialog;
import org.openjump.advancedtools.plugins.CirclePlugIn;
import org.openjump.advancedtools.utils.EditUtils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.valid.IsValidOp;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.NClickTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.FeatureDrawingUtil;

/**
 * @author Giuseppe Aruta [Genuary 30th 2017]
 * @since OpenJUMP 1.10 (2017)
 */

public class CircleByDefinedRadiusTool extends NClickTool {

    // ---- from DragTool -----------------
    /** Modify using #setDestination */
    protected Coordinate modelDestination = null;
    // ------------------------------------
    private final FeatureDrawingUtil featureDrawingUtil;
    private Shape selectedFeaturesShape;
    private final String name = I18N
            .get("org.openjump.core.ui.plugins.edittoolbox.cursortools.DrawCircleWithGivenRadiusTool.Draw-circle-with-given-radius-and-center");
    ///** Radio del circulo */
    //protected double ratio;
    protected double x;
    protected double y;

    private CircleByDefinedRadiusTool(FeatureDrawingUtil featureDrawingUtil) {
        super(1);
        this.featureDrawingUtil = featureDrawingUtil;
        setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL, 0, new float[] { 3, 3 }, 0));
        this.allowSnapping();

    }

    public static CursorTool create(LayerNamePanelProxy layerNamePanelProxy) {
        FeatureDrawingUtil featureDrawingUtil = new FeatureDrawingUtil(
                layerNamePanelProxy);

        return featureDrawingUtil.prepare(new CircleByDefinedRadiusTool(
                featureDrawingUtil), true);
    }

    private final GeometryFactory factory = new GeometryFactory();
    Double radius = Double.parseDouble(CircleDialog.jsRadio1.getText());// Double.parseDouble(CirclePlugIn.textField_radius.getText());

    /****************** events ********************************/
    @Override
    protected void gestureFinished() throws Exception {
        reportNothingToUndoYet();

        Point p = new GeometryFactory().createPoint(this.getModelDestination());
        Coordinate[] coords = EditUtils.createCircle(
                this.getModelDestination(), radius, CirclePlugIn.NUM_VERTICES);
        LinearRing ring = factory.createLinearRing(coords);
        this.checkCircle(ring);

        if (CADToolsOptionsPanel.isGetAsPolygon()) {
            Polygon geom = factory.createPolygon(ring, null);
            featureDrawingUtil.drawRing(geom,
                    isRollingBackInvalidEdits(), this, getPanel());

        } else {

            featureDrawingUtil.drawLineString(factory.createLineString(coords),
                    isRollingBackInvalidEdits(), this, getPanel());

        }

        if (CADToolsOptionsPanel.isGetCentroid()) {
            execute(featureDrawingUtil.createAddCommand(p,
                    isRollingBackInvalidEdits(), getPanel(), this));
        }

    }

    protected Point getCenter() throws NoninvertibleTransformException {
        return new GeometryFactory().createPoint(this.getModelDestination());
    }

    protected boolean checkCircle(Geometry circle) {
        IsValidOp isValidOp = new IsValidOp(circle);

        if (!isValidOp.isValid()) {
            getPanel().getContext().warnUser(
                    isValidOp.getValidationError().getMessage());

            if (getWorkbench().getBlackboard().get(
                    EditTransaction.ROLLING_BACK_INVALID_EDITS_KEY, false)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void activate(LayerViewPanel layerViewPanel) {
        super.activate(layerViewPanel);
        try {
            Envelope viewportEnvelope = layerViewPanel.getViewport()
                    .getEnvelopeInModelCoordinates();
            double x = viewportEnvelope.getMinX() + viewportEnvelope.getWidth()
                    / 2;
            double y = viewportEnvelope.getMinY()
                    + viewportEnvelope.getHeight() / 2;
            Coordinate initCoords = new Coordinate(x, y);
            this.calculateCircle(initCoords, layerViewPanel);
            redrawShape();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(getClass().getResource("DrawCircleByRadius.gif"));
    }

    /**
     * overwritten super method to show the circle on any mouse move
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

    /****************** other methods ********************************/

    /**
     * changed to get circle around mouse pointer
     */
    @Override
    protected Shape getShape() {
        this.calculateCircle(this.modelDestination, this.getPanel());
        return this.selectedFeaturesShape;
    }

    /**
     * called from constructor and by mouse move event
     * <p>
     * calculates a circle around the mouse pointer and converts it to a java
     * shape
     * 
     * @param middlePoint
     *            coordinates of the circle
     */
    private void calculateCircle(Coordinate middlePoint, LayerViewPanel panel) {
        // --calculate circle;

        Coordinate[] coords = EditUtils.createCircle(middlePoint, radius,
                CirclePlugIn.NUM_VERTICES);
        Geometry ring = factory.createLinearRing(coords);

        try {
            this.selectedFeaturesShape = panel.getJava2DConverter().toShape(
                    ring);
        } catch (NoninvertibleTransformException e) {
            System.out.println("DrawCircleWithGivenRadiusTool:Exception " + e);
        }
    }

    // ----------- from drag tool ------------//

    protected void setViewDestination(Point2D destination)
            throws NoninvertibleTransformException {
        this.setModelDestination(getPanel().getViewport().toModelCoordinate(
                destination));
    }

    protected void setModelDestination(Coordinate destination) {
        this.modelDestination = snap(destination);
    }
}
