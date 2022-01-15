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

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.openjump.advancedtools.CadExtension;
import org.openjump.advancedtools.utils.EditUtils;
import org.openjump.core.geomutils.Arc;
import org.openjump.core.ui.plugin.edittoolbox.cursortools.ConstrainedMultiClickArcTool;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.FeatureDrawingUtil;

public class ArcTool extends ConstrainedMultiClickArcTool {

    private static final I18N i18n = CadExtension.I18N;

    public static final String arcLength = i18n
        .get("org.openjump.core.ui.utils.CoordinateListMetrics.arc-length")
            + ": ";
    public static final String Angle = i18n
        .get("org.openjump.core.ui.utils.CoordinateListMetrics.arc-angle-center")
            + ": ";
    public static final String Center = i18n
        .get("org.openjump.core.ui.utils.CoordinateListMetrics.center-coordinates")
            + ": ";
    public static final String Radius = i18n
        .get("org.openjump.core.ui.utils.CoordinateListMetrics.radius")
            + ": ";
    private final FeatureDrawingUtil featureDrawingUtil;
    final static String drawConstrainedArc = I18N.JUMP
            .get("org.openjump.core.ui.plugins.edittoolbox.cursortools.DrawConstrainedArcTool.Draw-Constrained-Arc");
    final static String theArcMustHaveAtLeast3Points = I18N.JUMP
            .get("org.openjump.core.ui.plugins.edittoolbox.cursortools.DrawConstrainedArcTool.The-arc-must-have-at-least-3-points");

    private ArcTool(FeatureDrawingUtil featureDrawingUtil) {
        super(JUMPWorkbench.getInstance().getContext());
        drawClosed = false;
        this.featureDrawingUtil = featureDrawingUtil;
    }

    public static CursorTool create(LayerNamePanelProxy layerNamePanelProxy) {
        FeatureDrawingUtil featureDrawingUtil = new FeatureDrawingUtil(
                layerNamePanelProxy);

        return featureDrawingUtil
                .prepare(new ArcTool(featureDrawingUtil), true);
    }

    @Override
    public String getName() {
        return drawConstrainedArc;
    }

    private final GeometryFactory geomFac = new GeometryFactory();

    @Override
    public Icon getIcon() {
        return new ImageIcon(getClass().getResource("DrawArcConstrained.gif"));
    }

    /*
     * protected void gestureFinished() throws Exception {
     * reportNothingToUndoYet();
     * 
     * if (!checkArc()) { return; }
     * 
     * execute(featureDrawingUtil.createAddCommand(getArc(),
     * isRollingBackInvalidEdits(), getPanel(), this)); }
     */

    @Override
    protected void gestureFinished() throws Exception {
        reportNothingToUndoYet();

        Coordinate centerCoord = (Coordinate) getCoordinates().get(0);
        Coordinate startCoord = (Coordinate) getCoordinates().get(1);
        Coordinate endCoord = (Coordinate) getCoordinates().get(2);
        double startAngle = -EditUtils.getAngle(centerCoord.x, centerCoord.y,
                startCoord.x, startCoord.y);
        double endAngle = -EditUtils.getAngle(centerCoord.x, centerCoord.y,
                endCoord.x, endCoord.y);

        Coordinate[] coords = EditUtils.createArc(centerCoord,
                centerCoord.distance(startCoord), startAngle, endAngle, 40);
        LineString string = geomFac.createLineString(coords);
        featureDrawingUtil.drawLineString(string, isRollingBackInvalidEdits(),
                this, getPanel());

    }

    protected Point getCenter() throws NoninvertibleTransformException {
        ArrayList points = new ArrayList(getCoordinates());
        Coordinate a = (Coordinate) points.get(0);
        Coordinate b = this.tentativeCoordinate;
        Coordinate d = LineSegment.midPoint(a, b);

        return new GeometryFactory().createPoint(d);
    }

    protected LineString getArc() {
        ArrayList points = new ArrayList(getCoordinates());

        if (points.size() > 1) {
            Coordinate a = (Coordinate) points.get(0);
            Coordinate b = (Coordinate) points.get(1);
            if (points.size() > 2) {
            }

            Arc arc = new Arc(a, b, fullAngle);
            return arc.getLineString();
        }
        return null;
    }

    protected boolean checkArc() {
        if (getCoordinates().size() < 3) {
            getPanel().getContext().warnUser(theArcMustHaveAtLeast3Points);

            return false;
        }

        IsValidOp isValidOp = new IsValidOp(getArc());

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

    public void save(Coordinate centerCoord, double distance,
            double startAngle, double endAngle, LayerViewPanel panel)
            throws Exception {
        final Layer editableLayer = getLayer();
        if (editableLayer == null)
            return;

        final SelectionManager selectionManager = panel.getSelectionManager();
        final List<Feature> featsToAdd = new ArrayList<>();

        Geometry arc = createGeometry(centerCoord, distance, startAngle,
                endAngle);
        Feature nueva = FeatureUtil.toFeature(arc, editableLayer
                .getFeatureCollectionWrapper().getFeatureSchema());
        featsToAdd.add(nueva);

        execute(new UndoableCommand(getName()) {
            @Override
            public void execute() {
                selectionManager.unselectItems(editableLayer);
                editableLayer.getFeatureCollectionWrapper().addAll(featsToAdd);
                selectionManager.getFeatureSelection().selectItems(
                        editableLayer, featsToAdd);
            }

            @Override
            public void unexecute() {
                selectionManager.unselectItems(editableLayer);
                editableLayer.getFeatureCollectionWrapper().removeAll(
                        featsToAdd);
            }
        });
    }

    public Geometry createGeometry(Coordinate centerCoord, double distance,
            double startAngle, double endAngle) {
        Coordinate[] coords = EditUtils.createArc(centerCoord, distance,
                startAngle, endAngle, 40);

        return geomFac.createLineString(coords);
    }

    public Layer getLayer() {
        return (getPanel().getLayerManager().getEditableLayers().iterator()
                .next());
    }

    /**
     * 
     */
    @Override
    protected Shape getShape() throws NoninvertibleTransformException {
        if (getCoordinates().size() == 0)
            return null;
        GeneralPath path = new GeneralPath();
        Coordinate coordinateA = (Coordinate) getCoordinates().get(0);
        Point2D a = getPanel().getViewport().toViewPoint(coordinateA);
        Area sh = new Area();

        Rectangle2D rect = new Rectangle.Double(a.getX() - 3, a.getY() - 3, 6,
                6);
        sh.add(new Area(rect));
        path.append(sh, false);
        if (getCoordinates().size() == 1) {

            Coordinate coordinateB = tentativeCoordinate;
            if (coordinateB == null)
                return null;

            Point2D b = getPanel().getViewport().toViewPoint(coordinateB);
            double r = a.distance(b);
            Ellipse2D ellipse = new Ellipse2D.Double(a.getX() - r,
                    a.getY() - r, 2 * r, 2 * r);
            path.append(ellipse, false);
            return path;
        }

        Coordinate centerCoord = (Coordinate) getCoordinates().get(0);
        Point2D start = getPanel().getViewport().toViewPoint(
                (Coordinate) getCoordinates().get(1));
        Coordinate startCoord = (Coordinate) getCoordinates().get(1);
        Coordinate endCoord = tentativeCoordinate;

        path.moveTo((float) start.getX(), (float) start.getY());
        double startAngle = -EditUtils.getAngle(centerCoord.x, centerCoord.y,
                startCoord.x, startCoord.y);
        double endAngle = -EditUtils.getAngle(centerCoord.x, centerCoord.y,
                endCoord.x, endCoord.y);
        Coordinate[] coords = EditUtils.createArc(centerCoord,
                centerCoord.distance(startCoord), startAngle, endAngle, 40);
        for (int i = 1; i < coords.length; i++) {
            Coordinate nextCoordinate = coords[i];
            Point2D nextPoint = getPanel().getViewport().toViewPoint(
                    nextCoordinate);
            path.lineTo((int) nextPoint.getX(), (int) nextPoint.getY());
        }

        return path;
    }

    public static DecimalFormat df2 = new DecimalFormat("##0.0##");

    @Override
    public void mouseLocationChanged(MouseEvent e) {
        try {
            if (isShapeOnScreen()) {
                redrawShape();
            }
            super.mouseLocationChanged(e);
            if (getCoordinates().size() == 0) {

            }
            if (getCoordinates().size() == 1) {
                Coordinate a = (Coordinate) getCoordinates().get(0);
                Coordinate b = this.tentativeCoordinate;
                String all = Center + " [" + a.x + "," + a.y + "] " + Radius
                        + df2.format(a.distance(b)) + "  ";

                JUMPWorkbench.getInstance().getFrame().getContext()
                        .getLayerViewPanel().getContext().setStatusMessage(all);
            }

            if (getCoordinates().size() == 2) {
                Coordinate a = (Coordinate) getCoordinates().get(0);
                Coordinate b = (Coordinate) getCoordinates().get(1);

                Coordinate c = tentativeCoordinate;

                double startAngle = -EditUtils.getAngle(a.x, a.y, b.x, b.y);
                double endAngle = -EditUtils.getAngle(a.x, a.y, c.x, c.y);

                Coordinate[] coords = EditUtils.createArc(a, a.distance(b),
                        startAngle, endAngle, 40);
                double arc = geomFac.createLineString(coords).getLength();
                double rad = a.distance(b);
                double angle = (arc * 360) / (2 * Math.PI * rad);

                String all = Center + " [" + a.x + "," + a.y + "] " + Radius
                        + df2.format(rad) + "  " + Angle + df2.format(angle)
                        + "  " +

                        arcLength + df2.format(arc);

                JUMPWorkbench.getInstance().getFrame().getContext()
                        .getLayerViewPanel().getContext().setStatusMessage(all);
            }

        } catch (Throwable t) {
            getPanel().getContext().handleThrowable(t);
        }
    }

}
