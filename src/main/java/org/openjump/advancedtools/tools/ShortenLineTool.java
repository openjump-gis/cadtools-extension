package org.openjump.advancedtools.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.Logger;
import org.openjump.advancedtools.config.CADToolsOptionsPanel;
import org.openjump.advancedtools.utils.EditUtils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.util.CoordinateArrays;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.GeometryEditor;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.NClickTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;

public class ShortenLineTool extends NClickTool {

    private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");

    public static final String NAME = i18n
        .get("org.openjump.core.ui.tools.ShortenLineTool.Shorten-line");
    public static final Icon ICON = org.openjump.advancedtools.icon.IconLoader
        .icon("shortenLine.png");
    public static final Cursor CURSOR = createCursor(IconLoader.icon(
        "DeleteCursor.gif").getImage());
    protected double BUFFER_RATIO = 1000.0D;
    protected boolean broke_geom = true;
    protected boolean self_intersection_active;
    protected List<Feature> featsToAdd;
    protected List<Feature> featsToUpdate;
    protected List<Feature> featsSelectedToUpdate;

    public ShortenLineTool() {
        super(JUMPWorkbench.getInstance().getContext(), 1);
        setColor(Color.magenta);
        allowSnapping();
        this.self_intersection_active = true;
    }

    public ShortenLineTool(int n) {
        super(JUMPWorkbench.getInstance().getContext(), n);
        setColor(Color.magenta);
        this.self_intersection_active = true;
    }

    @Override
    public void activate(LayerViewPanel layerViewPanel) {
        super.activate(layerViewPanel);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        cancelGesture();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public Cursor getCursor() {
        return CURSOR;
    }

    public Feature getSelectedFeature() {
        Layer layer = getSelectedLayer();
        WorkbenchContext context = JUMPWorkbench.getInstance().getFrame()
                .getContext();
        Collection<Feature> features = context.getLayerViewPanel()
                .getSelectionManager().getFeaturesWithSelectedItems(layer);
        if (features.size() == 0) {
            return null;
        }
        return features.iterator().next();
    }

    protected Map<Feature, Layer> getCandidates(Geometry geom, Feature feat) {
        WorkbenchContext context = getWorkbench().getContext();
        Envelope env = geom.getEnvelopeInternal();
        Map<Feature, Layer> s = new HashMap<>();
        List<Layer> layers = context.getLayerManager().getVisibleLayers(false);
        for (Layer layer : layers) {
            Set<Feature> features;
            try {
                features = EditUtils.intersectingFeatures(layer, env);
                for (Feature f : features) {
                    s.put(f, layer);
                }
            } catch (Exception e) {
                Logger.warn("", e);
            }
        }
        s.remove(feat);
        return s;
    }

    GeometryFactory geomFac = new GeometryFactory();

    protected Geometry makeLine(Geometry g) {
        if ((g instanceof Polygon)) {
            return geomFac.createLineString(((Polygon) g).getExteriorRing()
                    .getCoordinates());
        }
        if ((g instanceof MultiPolygon)) {
            MultiPolygon mp = (MultiPolygon) g;

            LineString[] lss = new LineString[mp.getNumGeometries()];
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                lss[i] = geomFac
                        .createLineString(((Polygon) mp.getGeometryN(i))
                                .getExteriorRing().getCoordinates());
            }
            return geomFac.createMultiLineString(lss);
        }
        return g;
    }

    protected Point autoIntersection(Feature feat, Point click) {
        Map<Point, Double> listPoints = new HashMap<>();
        double dmin = Double.MAX_VALUE;
        LineString nearestLine = null;
        for (int k = 0; k < feat.getGeometry().getNumGeometries(); k++) {
            LineString line = (LineString) feat.getGeometry().getGeometryN(k);
            if (click.distance(line) < dmin) {
                nearestLine = line;
                dmin = click.distance(line);
                listPoints = new HashMap<>();
                Coordinate[] coords = line.getCoordinates();
                Coordinate[] c = new Coordinate[2];
                Coordinate[] cc = new Coordinate[2];
                double dRecorridoi = 0.0D;
                double dRecorridoj = 0.0D;
                for (int i = 0; i < coords.length - 2; i++) {
                    c[0] = coords[i];
                    c[1] = coords[(i + 1)];
                    LineString ls = geomFac.createLineString(c);
                    dRecorridoj = dRecorridoi
                            + new LineSegment(coords[(i + 1)], coords[(i + 2)])
                                    .getLength()
                            + new LineSegment(c[0], c[1]).getLength();
                    for (int j = i + 2; j < coords.length - 1; j++) {
                        cc[0] = coords[j];
                        cc[1] = coords[(j + 1)];
                        LineString lss = geomFac.createLineString(cc);
                        if (ls.intersects(lss)) {
                            Geometry intersection = ls.intersection(lss);
                            if ((intersection instanceof Point)) {
                                Point point = (Point) intersection;
                                listPoints
                                        .put(point,
                                            dRecorridoi
                                                + new LineSegment(
                                                c[0],
                                                intersection
                                                    .getCoordinate())
                                                .getLength());
                                listPoints
                                        .put(point,
                                            dRecorridoj
                                                + new LineSegment(
                                                cc[0],
                                                intersection
                                                    .getCoordinate())
                                                .getLength());
                            }
                        }
                        dRecorridoj += new LineSegment(cc[0], cc[1])
                                .getLength();
                    }
                    dRecorridoi += new LineSegment(c[0], c[1]).getLength();
                }
            }
        }
        //Iterator<Point> it = listPoints.keySet().iterator();
        double dmax = 0.0D;
        dmin = Double.MAX_VALUE;
        Point minPoint = null;
        Point maxPoint = null;
        for (Point intersection : listPoints.keySet()) {
            double distance = listPoints.get(intersection);
            if (distance < dmin) {
                dmin = distance;
                minPoint = intersection;
            }
            if (distance > dmax) {
                dmax = distance;
                maxPoint = intersection;
            }
        }
        Point start = nearestLine.getStartPoint();
        Point end = nearestLine.getEndPoint();
        if (click.distance(start) < click.distance(end)) {
            return minPoint;
        }
        return maxPoint;
    }

    public Geometry getShortenedGeometry() {
        Geometry newGeometry = null;
        Layer editableLayer = getSelectedLayer();
        if (editableLayer == null) {
            return null;
        }
        Feature selectedFeature = getSelectedFeature();
        Geometry selectedGeom = selectedFeature.getGeometry();
        boolean multilinedGeometry = false;
        int geometryIndex = 0;
        // TODO condition selectedGeom instanceof GeometryCollection
        // hides the following part where selectedGeom instanceof MultiLineString
        if ((selectedGeom instanceof Polygon
                || selectedGeom instanceof MultiPolygon
                || selectedGeom instanceof Point
                || selectedGeom instanceof MultiPoint || selectedGeom instanceof GeometryCollection)) {
            JUMPWorkbench
                    .getInstance()
                    .getFrame()
                    .getContext()
                    .getLayerViewPanel()
                    .getContext()
                    .warnUser(
                        i18n.get("org.openjump.core.ui.tools.ShortenLineTool.Only-line"));
            return null;
        }
        if ((selectedGeom instanceof MultiLineString)) {
            MultiLineString mls = (MultiLineString) selectedGeom;
            Point c = getClickedPoint();
            LineString closest = (LineString) mls.getGeometryN(0);
            double distance = c.distance(closest);
            for (int i = 0; i < mls.getNumGeometries(); i++) {
                LineString auxG = (LineString) mls.getGeometryN(i);
                double dis = c.distance(auxG);
                if (dis < distance) {
                    distance = dis;
                    closest = auxG;
                    geometryIndex = i;
                }
            }
            selectedGeom = closest;
            multilinedGeometry = true;
        }
        Geometry p;
        if ((selectedGeom instanceof LineString)) {
            LineString line = (LineString) selectedGeom;
            Point sp = line.getStartPoint();
            Point ep = line.getEndPoint();
            Point c = getClickedPoint();
            double d1 = c.distance(sp);
            double d2 = c.distance(ep);
            p = d1 < d2 ? sp : ep;
            Map<Feature, Layer> allFeatures = getCandidates(line,
                    selectedFeature);
            Geometry closerGeom = null;
            Geometry points = null;
            Feature closerRealFeature = null;
            double distance = Double.MAX_VALUE;
            Collection<Feature> featsCol = allFeatures.keySet();
            Feature[] feats = new Feature[featsCol.size()];
            feats = featsCol.toArray(feats);
            for (Feature feature : feats) {
                Geometry g = feature.getGeometry();
                Geometry auxpoints = line.intersection(makeLine(g));
                if (line.intersects(g)) {
                    double di = p.distance(auxpoints);
                    if (distance > di) {
                        distance = di;
                        closerGeom = g;
                        closerRealFeature = feature;
                        points = auxpoints;
                    }
                }
            }
            if (this.self_intersection_active) {
                Point autoIntr = autoIntersection(selectedFeature, (Point) p);
                if (autoIntr != null) {
                    double di = p.distance(autoIntr);
                    if (distance > di) {
                        //distance = di;
                        closerRealFeature = selectedFeature;
                        closerGeom = selectedFeature.getGeometry();
                        points = autoIntr;

                        this.broke_geom = false;
                    }
                }
            }
            if (closerGeom != null) {
                if ((allFeatures.get(closerRealFeature) != null)
                        && (!allFeatures.get(closerRealFeature).equals(
                                editableLayer))) {
                    this.broke_geom = false;
                }
                Coordinate closestCoordinate = EditUtils
                        .closestCoordinateToGeometry(p.getCoordinate(), points);

                newGeometry = cutLineString(closestCoordinate, (Point) p,
                        (LineString) selectedGeom);
                if (!validLine((LineString) newGeometry)) {
                    JUMPWorkbench
                            .getInstance()
                            .getFrame()
                            .getContext()
                            .getLayerViewPanel()
                            .getContext()
                            .warnUser(
                                i18n.get("org.openjump.core.ui.tools.ShortenLineTool.Operation-result-is-not-valid"));
                    return null;
                }
                if (multilinedGeometry) {
                    MultiLineString g = (MultiLineString) selectedFeature
                            .getGeometry();
                    LineString[] lineStrings = new LineString[g
                            .getNumGeometries()];
                    for (int i = 0; i < lineStrings.length; i++) {
                        lineStrings[i] = ((LineString) g.getGeometryN(i));
                    }
                    lineStrings[geometryIndex] = ((LineString) newGeometry);
                    newGeometry = geomFac.createMultiLineString(lineStrings);
                }
            }
        }
        return newGeometry;
    }

    protected void brokeAndSave() {
        final Layer editableLayer = getSelectedLayer();

        final SelectionManager selectionManager = getPanel()
                .getSelectionManager();
        if (editableLayer == null) {
            return;
        }
        final Collection<Feature> selectedFeatures = selectionManager
                .getFeaturesWithSelectedItems(editableLayer);
        if ((selectedFeatures == null) || (selectedFeatures.isEmpty())) {
            return;
        }
        Feature selectedFeature = selectedFeatures.iterator().next();
        Geometry selectedGeom = selectedFeature.getGeometry();
        boolean multilinedGeometry = false;
        int geometryIndex = 0;
        if ((selectedGeom instanceof MultiLineString)) {
            MultiLineString mls = (MultiLineString) selectedGeom;
            Point c = getClickedPoint();
            LineString closest = (LineString) mls.getGeometryN(0);
            double distance = c.distance(closest);
            for (int i = 0; i < mls.getNumGeometries(); i++) {
                LineString auxG = (LineString) mls.getGeometryN(i);
                double dis = c.distance(auxG);
                if (dis < distance) {
                    distance = dis;
                    closest = auxG;
                    geometryIndex = i;
                }
            }
            selectedGeom = closest;
            multilinedGeometry = true;
        }
        Geometry p;
        if ((selectedGeom instanceof LineString)) {
            LineString line = (LineString) selectedGeom;
            Point sp = line.getStartPoint();
            Point ep = line.getEndPoint();

            Point c = getClickedPoint();
            double d1 = c.distance(sp);
            double d2 = c.distance(ep);
            p = d1 < d2 ? sp : ep;

            Map<Feature, Layer> allFeatures = getCandidates(line,
                    selectedFeature);

            Geometry closerGeom = null;
            Geometry closerRealGeom = null;
            Geometry points = null;
            Feature closerRealFeature = null;

            double distance = Double.MAX_VALUE;
            Collection<Feature> featsCol = allFeatures.keySet();
            Feature[] feats = new Feature[featsCol.size()];
            feats = featsCol.toArray(feats);
            for (Feature feature : feats) {
                Geometry g = feature.getGeometry();
                Geometry auxpoints = line.intersection(makeLine(g));
                if (line.intersects(g)) {
                    double di = p.distance(auxpoints);
                    if (distance > di) {
                        distance = di;
                        closerGeom = g;
                        closerRealGeom = g;
                        closerRealFeature = feature;
                        points = auxpoints;
                    }
                }
            }
            if (this.self_intersection_active) {
                Point autoIntr = autoIntersection(selectedFeature, (Point) p);
                if (autoIntr != null) {
                    double di = p.distance(autoIntr);
                    if (distance > di) {
                        //distance = di;
                        closerRealGeom = selectedFeature.getGeometry();
                        closerRealFeature = selectedFeature;
                        closerGeom = selectedFeature.getGeometry();
                        points = autoIntr;

                        this.broke_geom = false;
                    }
                }
            }
            if (closerGeom != null) {
                if ((allFeatures.get(closerRealFeature) != null)
                        && (!allFeatures.get(closerRealFeature).equals(
                                editableLayer))) {
                    this.broke_geom = false;
                }
                Coordinate closestCoordinate = EditUtils
                        .closestCoordinateToGeometry(p.getCoordinate(), points);

                Geometry newGeometry = cutLineString(closestCoordinate,
                        (Point) p, (LineString) selectedGeom);
                if (!validLine((LineString) newGeometry)) {
                    JUMPWorkbench
                            .getInstance()
                            .getFrame()
                            .getContext()
                            .getLayerViewPanel()
                            .getContext()
                            .warnUser(
                                i18n.get("org.openjump.core.ui.tools.ShortenLineTool.Operation-result-is-not-valid"));
                    return;
                }
                if (multilinedGeometry) {
                    MultiLineString g = (MultiLineString) selectedFeature
                            .getGeometry();
                    LineString[] lineStrings = new LineString[g
                            .getNumGeometries()];
                    for (int i = 0; i < lineStrings.length; i++) {
                        lineStrings[i] = ((LineString) g.getGeometryN(i));
                    }
                    lineStrings[geometryIndex] = ((LineString) newGeometry);
                    newGeometry = geomFac.createMultiLineString(lineStrings);
                }
                Geometry[] dividedGeometry;
                if (this.broke_geom) {
                    LineSegment closestLineSement = segmentInRangeStart(
                            closerRealGeom, closestCoordinate);
                    if ((closerRealGeom instanceof LineString)) {
                        LineString crg = (LineString) closerRealGeom;
                        if (!EditUtils.isConectedToExtreme(crg,
                                closestCoordinate)) {
                            LineString newGeom = (LineString) crg.copy();
                            newGeom = (LineString) new GeometryEditor()
                                    .insertVertex(newGeom,
                                            closestLineSement.p0,
                                            closestLineSement.p1,
                                            closestCoordinate);
                            Feature newFeature = closerRealFeature.clone(true);
                            newFeature.setGeometry(newGeom);
                            this.featsToAdd.add(newFeature);
                            this.featsSelectedToUpdate.add(closerRealFeature);
                        }
                    } else {
                        dividedGeometry = new LineString[2];
                        //Geometry newGeom = closerRealGeom.copy();
                        //newGeom = new GeometryEditor().insertVertex(newGeom,
                        //        closestLineSement.p0, closestLineSement.p1,
                        //        closestCoordinate);

                        Feature updatedFeature = closerRealFeature.clone(true);
                        updatedFeature.setGeometry(dividedGeometry[0]);
                        this.featsToUpdate.add(updatedFeature);
                        this.featsSelectedToUpdate.add(closerRealFeature);
                    }
                }
                Feature clonedFeature = selectedFeature.clone(true);
                clonedFeature.setGeometry(newGeometry);
                this.featsToUpdate.add(clonedFeature);
                this.featsSelectedToUpdate.add(selectedFeature);
                try {
                    execute(new UndoableCommand(getName()) {
                        @Override
                        public void execute() {
                            selectionManager.unselectItems(editableLayer);
                            if (!featsToAdd.isEmpty()) {

                                editableLayer.getFeatureCollectionWrapper()
                                        .addAll(featsToAdd);
                                selectionManager.getFeatureSelection()
                                        .selectItems(editableLayer, featsToAdd);
                            }
                            if (!featsToUpdate.isEmpty()) {
                                editableLayer.getFeatureCollectionWrapper()
                                        .addAll(featsToUpdate);
                                editableLayer.getFeatureCollectionWrapper()
                                        .removeAll(featsSelectedToUpdate);
                                editableLayer.getLayerManager()
                                        .fireGeometryModified(featsToUpdate,
                                                editableLayer,
                                                featsSelectedToUpdate);
                                selectionManager.getFeatureSelection()
                                        .selectItems(editableLayer,
                                                featsToUpdate);
                            }
                        }

                        @Override
                        public void unexecute() {
                            selectionManager.unselectItems(editableLayer);
                            if (!featsToAdd.isEmpty()) {
                                editableLayer.getFeatureCollectionWrapper()
                                        .addAll(featsSelectedToUpdate);
                                editableLayer.getFeatureCollectionWrapper()
                                        .removeAll(featsToAdd);
                            }
                            if (!featsToUpdate.isEmpty()) {
                                editableLayer.getFeatureCollectionWrapper()
                                        .addAll(featsSelectedToUpdate);
                                editableLayer.getFeatureCollectionWrapper()
                                        .removeAll(featsToUpdate);
                            }
                            selectionManager.getFeatureSelection().selectItems(
                                    editableLayer, selectedFeatures);
                        }
                    });

                } catch (Exception e) {
                    Logger.warn("", e);
                }
            }

        }
    }

    @Override
    protected void gestureFinished() throws Exception {
        this.BUFFER_RATIO = CADToolsOptionsPanel.isExtendShortLineBuffer();
        this.broke_geom = CADToolsOptionsPanel.isExtendShortLineUnion();
        this.featsToAdd = new ArrayList<>();
        this.featsToUpdate = new ArrayList<>();
        this.featsSelectedToUpdate = new ArrayList<>();
        brokeAndSave();
    }

    protected boolean validLine(LineString string) {
        if (string.getNumPoints() > 2) {
            return true;
        }
        if (!string.getStartPoint().getCoordinate()
                .equals(string.getEndPoint().getCoordinate())) {
            return true;
        }
        return false;
    }

    protected LineString cutLineString(Coordinate c, Point ex, LineString g) {
        Coordinate start = g.getStartPoint().getCoordinate();
        Coordinate end = g.getEndPoint().getCoordinate();
        LineSegment closestLineSement;
        closestLineSement = ex.getCoordinate().distance(start) < ex
                .getCoordinate().distance(end) ? segmentInRangeStart(g, c)
                : segmentInRangeEnd(g, c);

        LineString newGeom = (LineString) new GeometryEditor().insertVertex(g,
                closestLineSement.p0, closestLineSement.p1, c);
        LineString[] lst = EditUtils.divideLineString(newGeom, c);
        return furthestLineString(lst, ex);
    }

    protected LineString furthestLineString(LineString[] lsa, Point p) {
        LineString sol;
        double distance = Double.NEGATIVE_INFINITY;
        sol = lsa[0];
        for (LineString ls : lsa) {
            double distanceAux = p.distance(ls);
            if (distanceAux > distance) {
                distance = distanceAux;
                sol = ls;
            }
        }
        return sol;
    }

    protected double modelRange() {
        return 5.0D / getPanel().getViewport().getScale();
    }

    protected LineSegment segmentInRangeStart(Geometry geometry,
            Coordinate target) {
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

    protected LineSegment segmentInRangeEnd(Geometry geometry, Coordinate target) {
        // It's possible that the geometry may have no segments in range; for
        // example, if it
        // is empty, or if only has points in range. [Jon Aquino]
        LineSegment closest = null;
        LineString closestLineString = null;
        Point targetPoint = geomFac.createPoint(target);
        List<Coordinate[]> coordArrays = CoordinateArrays.toCoordinateArrays(
                geometry, false);
        for (Coordinate[] coordinates : coordArrays) {
            for (int j = coordinates.length - 1; j > 0; j--) { // 1
                LineSegment candidate = new LineSegment(coordinates[j - 1],
                        coordinates[j]);
                LineString candidateLineString = geomFac
                        .createLineString(new Coordinate[] { candidate.p0,
                                candidate.p1 });

                if ((closestLineString == null)
                        || (candidateLineString.distance(targetPoint) < closestLineString
                                .distance(targetPoint))) {
                    closestLineString = candidateLineString;
                    closest = candidate;

                }
            }
        }
        return closest;
    }

    public Point getClickedPoint() {
        double x = getModelSource().x;
        double y = getModelSource().y;

        return geomFac.createPoint(new Coordinate(x, y));
    }

    public static MultiEnableCheck createEnableCheck(
            WorkbenchContext workbenchContext, AbstractCursorTool tool) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory =
            workbenchContext.createPlugInContext().getCheckFactory();

        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck())
                .add(checkFactory
                        .createWindowWithLayerManagerMustBeActiveCheck())
                .add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
        solucion.add(checkFactory
                .createSelectedItemsLayersMustBeEditableCheck());
        solucion.add(checkFactory.createExactlyNFeaturesMustBeSelectedCheck(1));
        return solucion;
    }

    public Layer getSelectedLayer() {
        Collection<Layer> editableLayers = getPanel().getLayerManager()
                .getEditableLayers();
        if (editableLayers.isEmpty()) {
            return null;
        }
        return editableLayers.iterator().next();
    }
}
