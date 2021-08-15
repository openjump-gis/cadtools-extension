/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI 
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * JUMP is Copyright (C) 2003 Vivid Solutions
 *
 * This program implements extensions to JUMP and is
 * Copyright (C) 2004 Integrated Systems Analysts, Inc.
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
 *
 * Integrated Systems Analysts, Inc.
 * 630C Anchors St., Suite 101
 * Fort Walton Beach, Florida
 * USA
 *
 * (850)862-7321
 */

package org.openjump.advancedtools.tools;

/**
 *
 * @author Giuseppe Aruta [Genuary 30th 2017]
 * @since OpenJUMP 1.10 (2017)
 */
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.Icon;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import org.openjump.advancedtools.config.CADToolsOptionsPanel;
import org.openjump.advancedtools.utils.CoordinateListMetricsUtils;
import org.openjump.core.geomutils.Circle;
import org.openjump.core.ui.plugin.edittoolbox.cursortools.ConstrainedMultiClickTool;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.valid.IsValidOp;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.FeatureDrawingUtil;

public class CircleByDiameterTool extends ConstrainedMultiClickTool {

    private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");

    private FeatureDrawingUtil featureDrawingUtil;

    final static String drawConstrainedCircle = I18N.JUMP
            .get("org.openjump.core.ui.plugins.edittoolbox.cursortools.DrawConstrainedCircleTool.Draw-Constrained-Circle");
    final static String theCircleMustHaveAtLeast2Points = I18N.JUMP
            .get("org.openjump.core.ui.plugins.edittoolbox.cursortools.DrawConstrainedCircleTool.The-circle-must-have-at-least-2-points");

    public CircleByDiameterTool() {
        super(JUMPWorkbench.getInstance().getContext());
    }

    private CircleByDiameterTool(FeatureDrawingUtil featureDrawingUtil) {
        super(JUMPWorkbench.getInstance().getContext());
        // drawClosed = true;
        this.featureDrawingUtil = featureDrawingUtil;
    }

    public static CursorTool create(LayerNamePanelProxy layerNamePanelProxy) {
        FeatureDrawingUtil featureDrawingUtil = new FeatureDrawingUtil(
                layerNamePanelProxy);

        return featureDrawingUtil.prepare(new CircleByDiameterTool(
                featureDrawingUtil), true);
    }

    @Override
    public String getName() {
        return i18n.get("Draw.Circle.by.diameter");
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    protected void gestureFinished() throws Exception {
        reportNothingToUndoYet();
        getPanel().setViewportInitialized(true);
        if (!checkCircle()) {
            return;
        }
        if (CADToolsOptionsPanel.isGetAsPolygon()) {
            execute(featureDrawingUtil.createAddCommand(
                    getPolygonCircleDiameter(), isRollingBackInvalidEdits(),
                    getPanel(), this));
        } else {
            execute(featureDrawingUtil.createAddCommand(getCircleDiameter(),
                    isRollingBackInvalidEdits(), getPanel(), this));
        }

        if (CADToolsOptionsPanel.isGetCentroid()) {
            execute(featureDrawingUtil.createAddCommand(getCenter(),
                    isRollingBackInvalidEdits(), getPanel(), this));
        }

    }

    protected LineString getCircleDiameter() {
        ArrayList points = new ArrayList(getCoordinates());
        Coordinate a = (Coordinate) points.get(0);
        Coordinate b = this.tentativeCoordinate;
        //LineSegment ls = new LineSegment(a, b);
        Coordinate d = LineSegment.midPoint(a, b);
        //Geometry midPoint = new GeometryFactory().createPoint(ls.midPoint());
        //double angle = 360.0;
        Circle circle = new Circle(d, ((Coordinate) points.get(0)).distance(d));
        return circle.getLineString();
    }

    protected Polygon getPolygonCircleDiameter() {
        ArrayList points = new ArrayList(getCoordinates());
        Coordinate a = (Coordinate) points.get(0);
        Coordinate b = this.tentativeCoordinate;
        //LineSegment ls = new LineSegment(a, b);
        Coordinate d = LineSegment.midPoint(a, b);
        //Geometry midPoint = new GeometryFactory().createPoint(ls.midPoint());
        //double angle = 360.0;
        Circle circle = new Circle(d, ((Coordinate) points.get(0)).distance(d));
        return circle.getPoly();
    }

    protected Point getCenter() throws NoninvertibleTransformException {
        ArrayList points = new ArrayList(getCoordinates());
        Coordinate a = (Coordinate) points.get(0);
        Coordinate b = this.tentativeCoordinate;
        Coordinate d = LineSegment.midPoint(a, b);

        return new GeometryFactory().createPoint(d);
    }

    protected boolean checkCircle() {
        if (getCoordinates().size() < 2) {
            getPanel().getContext().warnUser(theCircleMustHaveAtLeast2Points);
            return false;
        }
        IsValidOp isValidOp = new IsValidOp(getCircleDiameter());
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
    public void mouseLocationChanged(MouseEvent e) {
        try {
            if (isShapeOnScreen()) {
                redrawShape();
            }
            super.mouseLocationChanged(e);
            if (getCoordinates().size() == 0) {

            }
            if (getCoordinates().size() > 0) {
                Coordinate a = (Coordinate) getCoordinates().get(0);
                Coordinate b = this.tentativeCoordinate;
                double radius = a.distance(b) / 2;
                double diameter = getCircleDiameter().getLength();
                double area = Math.PI * Math.pow(radius, 2);

                CoordinateListMetricsUtils.setCircleMessage(radius, diameter,
                        area, a, b);

            }
        } catch (Throwable t) {
            getPanel().getContext().handleThrowable(t);
        }
    }

    @Override
    protected Shape getShape() throws NoninvertibleTransformException {
        GeneralPath shape = new GeneralPath();

        //ArrayList points = new ArrayList(getCoordinates());
        Coordinate a = (Coordinate) this.coordinates.get(0);
        Coordinate b = this.tentativeCoordinate;

        Coordinate d = LineSegment.midPoint(a, b);

        Circle circle = new Circle(d, a.distance(b) / 2);

        LineString polygon = circle.getLineString();

        Coordinate[] polygonCoordinates = polygon.getCoordinates();
        Coordinate firstCoordinate = polygonCoordinates[0];
        Point2D firstPoint = getPanel().getViewport().toViewPoint(
                firstCoordinate);
        shape.moveTo((float) firstPoint.getX(), (float) firstPoint.getY());
        int i = 1;
        for (int n = polygonCoordinates.length; i < n; i++) {
            Point2D nextPoint = getPanel().getViewport().toViewPoint(
                    polygonCoordinates[i]);
            shape.lineTo((int) nextPoint.getX(), (int) nextPoint.getY());
        }

        if (!this.coordinates.isEmpty()) {
            Point2D initialPoint = getPanel().getViewport().toViewPoint(a);
            shape.moveTo((float) initialPoint.getX(),
                    (float) initialPoint.getY());
            Point2D tentativePoint = getPanel().getViewport().toViewPoint(
                    this.tentativeCoordinate);
            shape.lineTo((int) tentativePoint.getX(),
                    (int) tentativePoint.getY());
        }

        Area sh = new Area();
        Point2D p = getPanel().getViewport().toViewPoint(d);
        //Ellipse2D ell = new Ellipse2D.Double(p.getX() - 2, p.getY() - 2, 4, 4);

        Rectangle2D rect = new Rectangle.Double(p.getX() - 3, p.getY() - 3, 6,
                6);
        sh.add(new Area(rect));

        shape.append(sh, false);

        return shape;
    }

}