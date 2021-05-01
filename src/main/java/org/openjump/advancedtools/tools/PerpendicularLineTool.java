package org.openjump.advancedtools.tools;

import java.awt.Cursor;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.vividsolutions.jump.workbench.Logger;
import org.openjump.advancedtools.gui.LengthDialog;
import org.openjump.advancedtools.icon.IconLoader;
import org.openjump.advancedtools.language.I18NPlug;
import org.openjump.advancedtools.utils.CADEnableCheckFactory;
import org.openjump.advancedtools.utils.WorkbenchUtils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.operation.distance.DistanceOp;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.util.CoordinateArrays;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DragTool;

/**
 * 
 * @author SAIG
 * @author Giuseppe Aruta [Genuary 30th 2017] rewrite code from Kosmo SAIG 3.0
 *         to adapt to OpenJUMP 1.10
 * @since 1.3.1
 */

public class PerpendicularLineTool extends DragTool {

    Geometry geomSelected = null;

    private static final GeometryFactory geomFac = new GeometryFactory();

    public static final String NAME = I18NPlug
            .getI18N("org.openjump.core.ui.tools.PerpendicularLineTool.Perpendicular");

    public final static String DESCRIPTION = I18NPlug
            .getI18N("org.openjump.core.ui.tools.PerpendicularLineTool.description");

    String sDistance = I18N.get("ui.cursortool.CoordinateListMetrics.Distance");

    public static final ImageIcon ICON = IconLoader
            .icon("drawPerpendicular.png");

    public static final Cursor CURSOR =

    // createCursor(
    // IconLoader.icon("icoBlue.png").getImage(),
    // new java.awt.Point(0, 0));

    new Cursor(Cursor.CROSSHAIR_CURSOR);

    protected Coordinate coordinateA;

    protected Coordinate coordinateB;

    protected double length;

    protected double angle;

    //protected SnapIndicatorTool snapIndicatorTool;

    protected Geometry nuevaGeom;

    protected List<Feature> featsToAdd;

    // protected List<Feature> featsToUpdate;
    /**
     * original features
     */
    protected List<Feature> featsOriginal;

    //protected List<Feature> featsSelectedToUpdate;
    EnableCheckFactory checkFactory = new EnableCheckFactory(JUMPWorkbench
            .getInstance().getContext());

    public PerpendicularLineTool(EnableCheckFactory checkFactory) {
        this.checkFactory = checkFactory;
        allowSnapping();
    }

    protected Geometry getPerpendicularLine() {
        Layer editableLayer = WorkbenchUtils.getSelectedFeaturesLayer();
        // Layer editableLayer = GeneralUtils.getSelectedLayer();
        WorkbenchContext context = getWorkbench().getContext();
        Collection<Feature> selectedFeatures = context.getLayerViewPanel()
                .getSelectionManager()
                .getFeaturesWithSelectedItems(editableLayer);
        for (Feature featureSelected : selectedFeatures) {
            this.geomSelected = featureSelected.getGeometry();
        }
        double z = Double.NaN;

        Coordinate[] cords = new Coordinate[2];
        this.coordinateA.z = z;
        cords[0] = this.coordinateA;
        this.coordinateB.z = z;
        cords[1] = this.coordinateB;
        LengthDialog ld = new LengthDialog(JUMPWorkbench.getInstance()
                .getFrame(), this.length);
        Geometry nuevaGeom = null;
        ld.setVisible(true);
        if (!ld.cancelado) {
            this.length = ld.getLength();
            cords[1] = new Coordinate(Math.cos(this.angle) * this.length
                    + this.coordinateA.x, Math.sin(this.angle) * this.length
                    + this.coordinateA.y, this.coordinateA.z);

            nuevaGeom = geomFac.createLineString(cords);

            if ((this.geomSelected instanceof MultiLineString)) {
                nuevaGeom = geomFac
                        .createMultiLineString(new LineString[] { (LineString) nuevaGeom });
            }
        }
        return nuevaGeom;
    }

    protected void getPerpendicularLineAndSave() {

        Layer editableLayer = WorkbenchUtils.getSelectedFeaturesLayer();
        if (editableLayer == null) {
            return;
        }
        this.featsToAdd = new ArrayList<>();
        this.featsOriginal = WorkbenchUtils.getSelectedFeatures(editableLayer);
        this.nuevaGeom = getPerpendicularLine();
        if (this.nuevaGeom != null) {
            this.featsToAdd.add(FeatureUtil.toFeature(this.nuevaGeom,
                    editableLayer.getFeatureCollectionWrapper()
                            .getFeatureSchema()));
            try {
                WorkbenchUtils.executeUndoableAddNewFeatsLeaveSelectedFeats(NAME,
                        getPanel().getSelectionManager(), editableLayer,
                        featsToAdd, featsOriginal);
            } catch (Exception e) {
                Logger.warn(e);
            }
        }
    }

    @Override
    protected void gestureFinished() throws Exception {
        reportNothingToUndoYet();
        getPerpendicularLineAndSave();
    }

    /*
     * [Giuseppe Aruta - Feb.07 2017] Kosmo allows to MultiEnableCheck to
     * activate/deactivate icon tools on toolbar.OpenJUMP seems not. So I use
     * mousePressed to activate/deactivate all the check warning messages,
     * including geometry type check
     */
    @Override
    public void mousePressed(MouseEvent e) {
        try {
            if (!check(checkFactory.createTaskWindowMustBeActiveCheck())) {
                return;
            }
            if (!check(checkFactory
                    .createWindowWithLayerManagerMustBeActiveCheck())) {
                return;
            }
            if (!check(checkFactory
                    .createSelectedItemsLayersMustBeEditableCheck())) {
                return;
            }

            if (!check(CADEnableCheckFactory
                    .createExactlyNFeaturesWithGeometryTypeMustBeSelectedCheck(
                            new int[] {
                                    CADEnableCheckFactory.FEATURE_SCHEMA_LINESTRING,
                                    CADEnableCheckFactory.FEATURE_SCHEMA_POLYGON,
                                    CADEnableCheckFactory.FEATURE_SCHEMA_GEOMETRYCOLLECTION,
                                    CADEnableCheckFactory.FEATURE_SCHEMA_MULTILINESTRING,
                                    CADEnableCheckFactory.FEATURE_SCHEMA_MULTIPOLYGON,
                                    CADEnableCheckFactory.FEATURE_SCHEMA_LINEARRING },

                            new int[] {

                            CADEnableCheckFactory.FEATURE_SCHEMA_MULTIPOINT,
                                    CADEnableCheckFactory.FEATURE_SCHEMA_POINT },
                            1))) {

                return;
            }

            super.mousePressed(e);
        } catch (Throwable t) {
            getPanel().getContext().handleThrowable(t);
        }
    }

    public Feature getClosestFeatureToPoint(Collection<Feature> features,
            Coordinate c) {
        Feature closest = null;
        Point p = geomFac.createPoint(c);
        double d = Double.MAX_VALUE;
        double daux;

        for (Feature currentFeature : features) {
            daux = currentFeature.getGeometry().distance(p);
            if (daux < d) {
                d = daux;
                closest = currentFeature;
            }
        }
        return closest;
    }

    @Override
    public Cursor getCursor() {
        return CURSOR;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {

        String tooltip;
        tooltip = "<HTML><BODY>";
        tooltip += "<DIV style=\"width: 300px; text-justification: justify;\">";
        tooltip += "<b>" + NAME + "</b>" + "<br>";
        tooltip += DESCRIPTION + "<br>";
        tooltip += "</DIV></BODY></HTML>";
        return tooltip;
    }

    protected LineSegment segmentInRange(Geometry geometry, Coordinate target) {
        // It's possible that the geometry may have no segments in range; for
        // example, if it
        // is empty, or if only has points in range. [Jon Aquino]
        LineSegment closest = null;
        List<Coordinate[]> coordArrays = CoordinateArrays.toCoordinateArrays(
                geometry, false);
        for (Coordinate[] coordinates : coordArrays) {
            for (int j = 1; j < coordinates.length; j++) { // 1
                LineSegment candidate = new LineSegment(coordinates[j - 1],
                        coordinates[j]);
                if ((closest == null)
                        || (candidate.distance(target) < closest
                                .distance(target))) {
                    closest = candidate;
                }
            }
        }
        return closest;
    }

    @Override
    protected Shape getShape(Point2D source, Point2D destination)
            throws Exception {
        source = getPanel().getViewport().toViewPoint(snap(source));
        this.coordinateA = new Coordinate(getPanel().getViewport()
                .toModelCoordinate(source));
        Layer layer = WorkbenchUtils.getSelectedFeaturesLayer();
        // Layer layer = GeneralUtils.getSelectedLayer();

        final Collection<Feature> selectedFeatures = WorkbenchUtils
                .getSelectedFeatures(layer);
        if (selectedFeatures.isEmpty()) {
            return null;
        }
        Geometry closest = getClosestFeatureToPoint(selectedFeatures,
                this.coordinateA).getGeometry();

        // Geometry closest =
        // getClosestFeatureToPoint(getSelectedFeatures(layer),
        // this.coordinateA).getGeometry();
        this.coordinateA = closestCoordinateToGeometry(this.coordinateA,
                closest);
        this.coordinateB = new Coordinate(getPanel().getViewport()
                .toModelCoordinate(destination));
        this.length = this.coordinateA.distance(this.coordinateB);
        LineSegment ls = segmentInRange(closest, this.coordinateA);
        double angleA = getAngle(ls.p0.x, ls.p0.y, ls.p1.x, ls.p1.y);
        double angleB = angleA + 3.141592653589793D;
        Coordinate a = new Coordinate(Math.cos(angleA) * this.length
                + this.coordinateA.x, Math.sin(angleA) * this.length
                + this.coordinateA.y);
        Coordinate b = new Coordinate(Math.cos(angleB) * this.length
                + this.coordinateA.x, Math.sin(angleB) * this.length
                + this.coordinateA.y);
        this.coordinateB = (this.coordinateB.distance(a) < this.coordinateB
                .distance(b) ? a : b);
        this.angle = (this.coordinateB.distance(a) < this.coordinateB
                .distance(b) ? angleA : angleB);
        source = getPanel().getViewport().toViewPoint(this.coordinateA);
        destination = getPanel().getViewport().toViewPoint(this.coordinateB);
        Line2D sh = new Line2D.Double(source.getX(), source.getY(),
                destination.getX(), destination.getY());
        DecimalFormat df2 = new DecimalFormat("##0.0##");
        getPanel().getContext().setStatusMessage(
                sDistance + ": " + df2.format(source.distance(destination)));
        return sh;
    }

    /*
     * public Point getClickedPoint() { Coordinate c = snap(getModelSource());
     * return geomFac.createPoint(c); }
     */

    public static Coordinate closestCoordinateToGeometry(Coordinate c,
            Geometry g) {
        Point p = geomFac.createPoint(c);
        DistanceOp dop = new DistanceOp(p, g);
        Coordinate[] coords = dop.nearestPoints();
        if (coords.length < 2)
            return null;
        return p.getCoordinate().equals(coords[0]) ? coords[1]
                : coords[0];
    }

    public static double getAngle(double x, double y, double xd, double yd) {
        float d = (float) Math.sqrt((x - xd) * (x - xd) + (y - yd) * (y - yd));
        float sina = (float) (x - xd) / d;
        float cosa = (float) (yd - y) / d;
        float angle;
        if (cosa > 0.0F) {
            angle = (float) Math.asin(sina);
        } else {
            angle = (float) (-Math.asin(sina) + 3.141592653589793D);
        }
        if (angle < 0.0F)
            angle = (float) (angle + 6.283185307179586D);
        return angle;
    }

    public static MultiEnableCheck createEnableCheck(
            WorkbenchContext workbenchContext, AbstractCursorTool tool) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(
                workbenchContext);

        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck())
                .add(checkFactory
                        .createWindowWithLayerManagerMustBeActiveCheck())
                .add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
        solucion.add(checkFactory
                .createSelectedItemsLayersMustBeEditableCheck());
        solucion.add(checkFactory.createAtLeastNFeaturesMustBeSelectedCheck(1));

        return solucion;
    }
}