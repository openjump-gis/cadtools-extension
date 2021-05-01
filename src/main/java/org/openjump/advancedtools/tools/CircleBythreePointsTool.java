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
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.valid.IsValidOp;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.FeatureDrawingUtil;

public class CircleBythreePointsTool extends ConstrainedMultiClickTool {

    private final FeatureDrawingUtil featureDrawingUtil;

    static final String theCircleMustHaveAtLeast2Points = I18N
            .get("org.openjump.core.ui.plugins.edittoolbox.cursortools.DrawConstrainedCircleTool.The-circle-must-have-at-least-2-points");

    private CircleBythreePointsTool(FeatureDrawingUtil featureDrawingUtil) {

        this.featureDrawingUtil = featureDrawingUtil;
    }

    public static CursorTool create(LayerNamePanelProxy layerNamePanelProxy) {
        FeatureDrawingUtil featureDrawingUtil = new FeatureDrawingUtil(
                layerNamePanelProxy);

        return featureDrawingUtil.prepare(new CircleBythreePointsTool(
                featureDrawingUtil), true);
    }

    @Override
    public String getName() {
        return I18NPlug.getI18N("Draw.Circle.by.3points");
    }

    @Override
    protected void gestureFinished() throws Exception {
        reportNothingToUndoYet();

        if (!checkCircle()) {
            return;
        }

        if (CADToolsOptionsPanel.isGetAsPolygon()) {
            execute(featureDrawingUtil.createAddCommand(getPolygonCircle(),
                    isRollingBackInvalidEdits(), getPanel(), this));
        } else {
            execute(featureDrawingUtil.createAddCommand(getCircle(),
                    isRollingBackInvalidEdits(), getPanel(), this));
        }
        if (CADToolsOptionsPanel.isGetCentroid()) {
            execute(featureDrawingUtil.createAddCommand(getCenter(),
                    isRollingBackInvalidEdits(), getPanel(), this));
        }
    }

    protected Point getCenter() throws NoninvertibleTransformException {
        ArrayList points = new ArrayList(getCoordinates());

        if (getCoordinates().size() == 2) {
            //Circle circle = new Circle((Coordinate) points.get(0),
            //        ((Coordinate) points.get(0)).distance((Coordinate) points
            //                .get(1)));
            return null;
        }

        Coordinate a = (Coordinate) points.get(0);
        Coordinate b = (Coordinate) points.get(1);
        Coordinate c = (Coordinate) points.get(2);
        return getCenter3Point(a, b, c);
    }

    @Override
    public void mouseLocationChanged(MouseEvent e) {
        try {
            if (isShapeOnScreen()) {
                redrawShape();
            }
            super.mouseLocationChanged(e);
            /*
             * if (getCoordinates().size() == 2) { Coordinate a = (Coordinate)
             * getCoordinates().get(0); Coordinate b = this.tentativeCoordinate;
             * CoordinateListMetricsUtils.setMessage(a.distance(b)); }
             */
            if (getCoordinates().size() > 1) {
                Coordinate a = (Coordinate) getCoordinates().get(0);
                Coordinate b = (Coordinate) getCoordinates().get(1);
                Coordinate c = this.tentativeCoordinate;

                Point center = getCenter3Point(a, b, c);
                LineString circle = getCircle3points(a, b, c);
                Polygon circleP = getPolygonCircle3points(a, b, c);

                double area = circleP.getArea();
                double circumference = circle.getLength();
                double radius = circumference / (2 * Math.PI);

                CoordinateListMetricsUtils.setCircle3PointsMessage(radius,
                        circumference, area, center.getCoordinate());

            }
        } catch (Throwable t) {
            getPanel().getContext().handleThrowable(t);
        }
    }

    protected LineString getCircle() {
        //double angle = 360.0D;
        ArrayList points = new ArrayList(getCoordinates());

        Coordinate a = (Coordinate) points.get(0);
        Coordinate b = (Coordinate) points.get(1);
        Coordinate c = (Coordinate) points.get(2);
        return getCircle3points(a, b, c);
    }

    protected Polygon getPolygonCircle() {
        //double angle = 360.0D;
        ArrayList points = new ArrayList(getCoordinates());

        Coordinate a = (Coordinate) points.get(0);
        Coordinate b = (Coordinate) points.get(1);
        Coordinate c = (Coordinate) points.get(2);
        return getPolygonCircle3points(a, b, c);
    }

    private LineString getCircle3points(Coordinate a, Coordinate b, Coordinate c) {
        double A = b.x - a.x;
        double B = b.y - a.y;
        double C = c.x - a.x;
        double D = c.y - a.y;
        double E = A * (a.x + b.x) + B * (a.y + b.y);
        double F = C * (a.x + c.x) + D * (a.y + c.y);
        double G = 2.0D * (A * (c.y - b.y) - B * (c.x - b.x));
        if (G != 0.0D) {
            double px = (D * E - B * F) / G;
            double py = (A * F - C * E) / G;
            Coordinate center = new Coordinate(px, py);
            double radius = Math.sqrt((a.x - px) * (a.x - px) + (a.y - py)
                    * (a.y - py));
            Circle circle = new Circle(center, radius);
            return circle.getLineString();
        }

        Circle circle = new Circle(b, b.distance(c));
        return circle.getLineString();
    }

    private Polygon getPolygonCircle3points(Coordinate a, Coordinate b,
            Coordinate c) {
        double A = b.x - a.x;
        double B = b.y - a.y;
        double C = c.x - a.x;
        double D = c.y - a.y;
        double E = A * (a.x + b.x) + B * (a.y + b.y);
        double F = C * (a.x + c.x) + D * (a.y + c.y);
        double G = 2.0D * (A * (c.y - b.y) - B * (c.x - b.x));
        if (G != 0.0D) {
            double px = (D * E - B * F) / G;
            double py = (A * F - C * E) / G;
            Coordinate center = new Coordinate(px, py);
            double radius = Math.sqrt((a.x - px) * (a.x - px) + (a.y - py)
                    * (a.y - py));
            Circle circle = new Circle(center, radius);
            return circle.getPoly();
        }

        Circle circle = new Circle(b, b.distance(c));
        return circle.getPoly();
    }

    protected Point getCenter3Point(Coordinate a, Coordinate b, Coordinate c) {
        double A = b.x - a.x;
        double B = b.y - a.y;
        double C = c.x - a.x;
        double D = c.y - a.y;
        double E = A * (a.x + b.x) + B * (a.y + b.y);
        double F = C * (a.x + c.x) + D * (a.y + c.y);
        double G = 2.0D * (A * (c.y - b.y) - B * (c.x - b.x));

        double px = (D * E - B * F) / G;
        double py = (A * F - C * E) / G;
        Coordinate center = new Coordinate(px, py);

        return new GeometryFactory().createPoint(center);
    }

    protected boolean checkCircle() {
        if (getCoordinates().size() < 2) {
            getPanel().getContext().warnUser(theCircleMustHaveAtLeast2Points);

            return false;
        }

        IsValidOp isValidOp = new IsValidOp(getCircle());

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
    protected Shape getShape() throws NoninvertibleTransformException {

        if (this.coordinates.size() > 1) {

            GeneralPath shape = new GeneralPath();

            Coordinate a = (Coordinate) this.coordinates.get(0);
            Coordinate b = (Coordinate) this.coordinates.get(1);
            Coordinate c = this.tentativeCoordinate;

            LineString polygon = getCircle3points(a, b, c);
            Coordinate[] polygonCoordinates = polygon.getCoordinates();

            Coordinate firstCoordinate = polygonCoordinates[0];
            Point2D firstPoint = getPanel().getViewport().toViewPoint(
                    firstCoordinate);
            Point2D secondPoint = getPanel().getViewport().toViewPoint(b);
            shape.moveTo((float) firstPoint.getX(), (float) firstPoint.getY());

            int i = 1;
            for (int n = polygonCoordinates.length; i < n; i++) {
                Point2D nextPoint = getPanel().getViewport().toViewPoint(
                        polygonCoordinates[i]);
                shape.lineTo((int) nextPoint.getX(), (int) nextPoint.getY());
            }
            Coordinate center = null;
            if (!this.coordinates.isEmpty()) {
                double A = b.x - a.x;
                double B = b.y - a.y;
                double C = c.x - a.x;
                double D = c.y - a.y;
                double E = A * (a.x + b.x) + B * (a.y + b.y);
                double F = C * (a.x + c.x) + D * (a.y + c.y);
                double G = 2.0D * (A * (c.y - b.y) - B * (c.x - b.x));

                double px = (D * E - B * F) / G;
                double py = (A * F - C * E) / G;
                center = new Coordinate(px, py);

                Point2D thirdPoint = getPanel().getViewport().toViewPoint(c);
                Point2D initialPoint = getPanel().getViewport().toViewPoint(a);
                shape.moveTo((float) thirdPoint.getX(),
                        (float) thirdPoint.getY());
                Point2D centerPoint = getPanel().getViewport().toViewPoint(
                        center);

                shape.lineTo((float) centerPoint.getX(),
                        (float) centerPoint.getY());
                shape.lineTo((float) initialPoint.getX(),
                        (float) initialPoint.getY());
                shape.moveTo((float) secondPoint.getX(),
                        (float) secondPoint.getY());
                shape.lineTo((float) centerPoint.getX(),
                        (float) centerPoint.getY());
            }
            Area sh = new Area();
            Point2D p = getPanel().getViewport().toViewPoint(center);
            Rectangle2D rect = new Rectangle.Double(p.getX() - 3, p.getY() - 3,
                    6, 6);
            sh.add(new Area(rect));

            shape.append(sh, false);
            return shape;
        }
        return super.getShape();
    }

    @Override
    public Icon getIcon() {
        // TODO Auto-generated method stub
        return null;
    }
}
