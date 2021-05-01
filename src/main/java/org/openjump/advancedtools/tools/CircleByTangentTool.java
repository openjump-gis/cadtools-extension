package org.openjump.advancedtools.tools;

/**
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

import org.openjump.advancedtools.config.CADToolsOptionsPanel;
import org.openjump.advancedtools.language.I18NPlug;
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
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.FeatureDrawingUtil;

public class CircleByTangentTool extends ConstrainedMultiClickTool {

    private FeatureDrawingUtil featureDrawingUtil;

    static final String drawConstrainedCircle = I18N
            .get("org.openjump.core.ui.plugins.edittoolbox.cursortools.DrawConstrainedCircleTool.Draw-Constrained-Circle");

    static final String theCircleMustHaveAtLeast2Points = I18N
            .get("org.openjump.core.ui.plugins.edittoolbox.cursortools.DrawConstrainedCircleTool.The-circle-must-have-at-least-2-points");

    public CircleByTangentTool(int n) {
    }

    private CircleByTangentTool(FeatureDrawingUtil featureDrawingUtil) {
        this.featureDrawingUtil = featureDrawingUtil;
    }

    public CircleByTangentTool(EnableCheckFactory checkFactory) {
    }

    public static CursorTool create(LayerNamePanelProxy layerNamePanelProxy) {
        FeatureDrawingUtil featureDrawingUtil = new FeatureDrawingUtil(
                layerNamePanelProxy);

        return featureDrawingUtil.prepare(new CircleByTangentTool(
                featureDrawingUtil), true);
    }

    @Override
    public String getName() {
        return I18NPlug.getI18N("Draw.Circle.by.diameter");
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
                    getPolygonCircleTangent(), isRollingBackInvalidEdits(),
                    getPanel(), this));
        } else {
            execute(featureDrawingUtil.createAddCommand(getCircleTangent(),
                    isRollingBackInvalidEdits(), getPanel(), this));
        }

        if (CADToolsOptionsPanel.isGetCentroid()) {
            execute(featureDrawingUtil.createAddCommand(getCenter(),
                    isRollingBackInvalidEdits(), getPanel(), this));
        }

    }

    protected Point getCenter() throws NoninvertibleTransformException {
        //ArrayList points = new ArrayList(getCoordinates());
        Coordinate b = this.tentativeCoordinate;
        return new GeometryFactory().createPoint(b);
    }

    protected LineString getCircleTangent() {
        ArrayList points = new ArrayList(getCoordinates());
        Coordinate a = (Coordinate) points.get(0);
        Coordinate b = this.tentativeCoordinate;
        LineSegment ls = new LineSegment(a, b);
        Coordinate d = LineSegment.midPoint(a, b);
        new GeometryFactory().createPoint(ls.midPoint());
        Circle circle = new Circle(this.tentativeCoordinate,
                this.tentativeCoordinate.distance((Coordinate) points.get(0)));
        return circle.getLineString();
    }

    protected Polygon getPolygonCircleTangent() {
        ArrayList points = new ArrayList(getCoordinates());
        Coordinate a = (Coordinate) points.get(0);
        Coordinate b = this.tentativeCoordinate;
        LineSegment ls = new LineSegment(a, b);
        //Coordinate d = LineSegment.midPoint(a, b);
        new GeometryFactory().createPoint(ls.midPoint());
        Circle circle = new Circle(this.tentativeCoordinate,
                this.tentativeCoordinate.distance((Coordinate) points.get(0)));
        return circle.getPoly();
    }

    protected boolean checkCircle() {
        if (getCoordinates().size() < 2) {
            getPanel().getContext().warnUser(theCircleMustHaveAtLeast2Points);

            return false;
        }

        IsValidOp isValidOp = new IsValidOp(getCircleTangent());

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
                double radius = a.distance(b);
                double area = Math.PI * Math.pow(radius, 2);

                CoordinateListMetricsUtils.setCircleMessage(a.distance(b),
                        getCircleTangent().getLength(), area, a, b);

            }
        } catch (Throwable t) {
            getPanel().getContext().handleThrowable(t);
        }
    }

    @Override
    protected Shape getShape() throws NoninvertibleTransformException {
        GeneralPath shape = new GeneralPath();

        ArrayList points = new ArrayList(getCoordinates());
        Coordinate a = (Coordinate) this.coordinates.get(0);
        Coordinate b = this.tentativeCoordinate;

        LineSegment ls = new LineSegment(a, b);
        Coordinate d = LineSegment.midPoint(a, b);
        new GeometryFactory().createPoint(ls.midPoint());
        ((Coordinate) points.get(0)).distance(d);

        Circle circle = new Circle(this.tentativeCoordinate,
                this.tentativeCoordinate.distance((Coordinate) points.get(0)));

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
        Point2D p = getPanel().getViewport().toViewPoint(b);

        Rectangle2D rect = new Rectangle.Double(p.getX() - 3, p.getY() - 3, 6,
                6);
        sh.add(new Area(rect));

        shape.append(sh, false);
        return shape;
    }

}
