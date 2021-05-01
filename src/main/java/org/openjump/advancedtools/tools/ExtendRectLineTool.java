/*
 * Kosmo - Sistema Abierto de Información Geográfica
 * Kosmo - Open Geographical Information System
 *
 * http://www.saig.es
 * (C) 2006, SAIG S.L.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, contact:
 *
 * Sistemas Abiertos de Información Geográfica, S.L.
 * Avnda. República Argentina, 28
 * Edificio Domocenter Planta 2ª Oficina 7
 * C.P.: 41930 - Bormujos (Sevilla)
 * España / Spain
 *
 * Teléfono / Phone Number
 * +34 954 788876
 *
 * Correo electrónico / Email
 * info@saig.es
 *
 */
package org.openjump.advancedtools.tools;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.vividsolutions.jump.workbench.Logger;
import org.openjump.advancedtools.config.CADToolsOptionsPanel;
import org.openjump.advancedtools.language.I18NPlug;
import org.openjump.advancedtools.utils.EditUtils;
import org.openjump.advancedtools.utils.WorkbenchUtils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.ui.GeometryEditor;
import com.vividsolutions.jump.workbench.ui.cursortool.NClickTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;

/**
 * Herramienta que permite extender una linea por un extremo hasta otro elemento
 * cercano (de la misma capa u otra)
 * <p>
 * </p>
 * 
 * @author Gabriel Bellido P�rez
 * @since Kosmo 1.0.0
 */
public class ExtendRectLineTool extends NClickTool {

    public ExtendRectLineTool() {
        super(1);
        // setColor(Color.magenta);
        allowSnapping();
        this.self_intersection_active = true;
    }

    /** Name of the tool */
    public final static String NAME = I18NPlug
            .getI18N("org.openjump.core.ui.tools.ExtendRectLineTool.Extend-line");

    /** Icon of the tool */
    public static final ImageIcon ICON = org.openjump.advancedtools.icon.IconLoader
            .icon("extentLine.png");

    /** Cursor of the tool */
    public static final Cursor CURSOR = createCursor(IconLoader.icon(
            "PlusCursor.gif").getImage());

    /** Buffer radius */
    protected double BUFFER_RATIO = 1000;

    /** Check if the geometry has to be brocken or not */
    protected boolean broke_geom = true;

    /** Feature to break */
    protected Feature toBrokeFeat;

    /** New geometry */
    protected Geometry newGeom;

    /** */
    protected boolean self_intersection_active;

    /** */
    protected Coordinate tentativeCoordinate;

    /** Closest coordinate */
    protected Coordinate closestCoordinate;

    protected List<Feature> featsToAdd;

    protected List<Feature> featsToRemove;

    EnableCheckFactory checkFactory = new EnableCheckFactory(JUMPWorkbench
            .getInstance().getContext());

    /**
     *
     */
    public ExtendRectLineTool(int n) {
        super(n);
        self_intersection_active = true;
    }

    /**
     * Get the first point
     * 
     * @return Coordinate - coordinates of the first point
     */
    protected Coordinate getStartPoint() {
        return (Coordinate) getCoordinates().get(0);
    }

    public Layer getSelectedLayer() {
        Collection<Layer> editableLayers = getPanel().getLayerManager()
                .getEditableLayers();
        if (editableLayers.isEmpty()) {
            return null;
        }
        return editableLayers.iterator().next();
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

    public Geometry getExtendedGeometry() {
        Coordinate startCoord = getStartPoint();

        Feature selectedFeature = getSelectedFeature();
        Feature clonedFeature = selectedFeature.clone(true);

        Geometry clonedGeom = clonedFeature.getGeometry();
        int geomIndex = getInnGeometryIndex(clonedGeom, startCoord);
        LineString geomToExtend;

        if (geomIndex == -1) {
            geomToExtend = (LineString) clonedGeom;
        } else {
            geomToExtend = (LineString) clonedGeom.getGeometryN(geomIndex);
        }

        boolean isStart = isStart(geomToExtend, startCoord);

        Map<Feature, Layer> candidatesFeatures = getCandidates(startCoord,
                BUFFER_RATIO, selectedFeature);

        Geometry enlargedGeom = enlarge(candidatesFeatures, selectedFeature,
                geomToExtend, isStart, BUFFER_RATIO);

        if (enlargedGeom == null)
            return null;

        return join(clonedGeom, enlargedGeom, geomIndex);
    }

    /**
     * 
     */
    @Override
    protected void gestureFinished() throws java.lang.Exception {
        reportNothingToUndoYet();
        BUFFER_RATIO = CADToolsOptionsPanel.isExtendShortLineBuffer();
        broke_geom = CADToolsOptionsPanel.isExtendShortLineUnion();
        featsToAdd = new ArrayList<>();

        featsToRemove = new ArrayList<>();

        if (check(checkFactory.createExactlyNFeaturesMustBeSelectedCheck(1))) {
            brokeGeomsAndSave();
        }
    }

    public void brokeGeomsAndSave() {
        Coordinate startCoord = getStartPoint();
        final Layer editableLayer = WorkbenchUtils.getSelectedFeaturesLayer();
        if (editableLayer == null)
            return;
        final Collection<Feature> selectedFeatures = WorkbenchUtils
                .getSelectedFeatures(editableLayer);

        if (selectedFeatures == null || selectedFeatures.isEmpty()) {
            return;
        }

        Feature selectedFeature = selectedFeatures.iterator().next();
        Feature clonedFeature = selectedFeature.clone(true);

        Geometry clonedGeom = clonedFeature.getGeometry();
        int geomIndex = getInnGeometryIndex(clonedGeom, startCoord);
        LineString geomToExtend;

        if (geomIndex == -1) {
            geomToExtend = (LineString) clonedGeom;
        } else {
            geomToExtend = (LineString) clonedGeom.getGeometryN(geomIndex);
        }

        boolean isStart = isStart(geomToExtend, startCoord);

        Map<Feature, Layer> candidatesFeatures = getCandidates(startCoord,
                BUFFER_RATIO, selectedFeature);

        Geometry enlargedGeom = enlarge(candidatesFeatures, selectedFeature,
                geomToExtend, isStart, BUFFER_RATIO);

        if (enlargedGeom == null)
            return;

        candidatesFeatures.put(clonedFeature, editableLayer);

        // Si la geometria a romper no esta en la capa de edicion no la partimos
        if (!candidatesFeatures.get(toBrokeFeat).getName()
                .equals(editableLayer.getName()))
            broke_geom = false;

        // newGeom = join(clonedGeom, enlargedGeom, geomIndex);
        newGeom = getExtendedGeometry();

        if (broke_geom) {
            Coordinate cr;
            if (isStart) {
                cr = ((LineString) enlargedGeom).getStartPoint()
                        .getCoordinate();
            } else {
                cr = ((LineString) enlargedGeom).getEndPoint().getCoordinate();
            }

            LineSegment closestLineSement = EditUtils.segmentInRange(
                    toBrokeFeat.getGeometry(), cr);
            Geometry toBrokeGeom = toBrokeFeat.getGeometry();

            if (toBrokeGeom instanceof LineString) {
                LineString crg = (LineString) toBrokeGeom;
                if (!EditUtils.isConectedToExtreme(crg, cr)) {
                    Feature newFeature = toBrokeFeat.clone(true);
                    LineString newGeomet = (LineString) crg.copy();
                    newGeomet = (LineString) (new GeometryEditor())
                            .insertVertex(newGeomet, closestLineSement.p0,
                                    closestLineSement.p1, cr);
                    newFeature.setGeometry(newGeomet);
                    featsToAdd.add(newFeature);
                    featsToRemove.add(toBrokeFeat);
                }
            } else {
                Geometry newGeome = toBrokeGeom.copy();
                newGeome = (new GeometryEditor()).insertVertex(newGeome,
                        closestLineSement.p0, closestLineSement.p1, cr);

                Feature updatedFeature = toBrokeFeat.clone(true);
                updatedFeature.setGeometry(newGeome);
            }
        }
        clonedFeature.setGeometry(newGeom);
        featsToAdd.add(clonedFeature);
        featsToRemove.add(selectedFeature);
        try {
            WorkbenchUtils.executeUndoableAddNewFeatsRemoveSelectedFeats(NAME,
                    getPanel().getSelectionManager(), editableLayer,
                    featsToAdd, featsToRemove);
        } catch (Exception e) {
            Logger.warn(e);
        }

        // Cleaning up the values
        newGeom = null;
        //selectedFeature = null;
    }

    GeometryFactory geomFac = new GeometryFactory();

    /**
     * 
     * @param g a simple or a multi LineString
     * @param k another LineString
     * @param index geometry of g at index will be replaced by k
     * @return the new Geometry
     */
    protected Geometry join(Geometry g, Geometry k, int index) {
        if (index == -1)
            return k;

        LineString[] lss = new LineString[g.getNumGeometries()];
        for (int i = 0; i < lss.length; i++) {
            lss[i] = (LineString) g.getGeometryN(i);
        }
        lss[index] = (LineString) k;
        return geomFac.createMultiLineString(lss);
    }

    /**
     * 
     * @param candidates
     * @param selectedFeature
     * @param ls
     * @param isStart
     * @param ratio
     * @return Geometry
     */
    protected Geometry enlarge(Map<Feature, Layer> candidates,
            Feature selectedFeature, LineString ls, boolean isStart,
            double ratio) {
        Coordinate[] c = ls.getCoordinates();
        LineSegment segment;
        if (isStart) {
            segment = new LineSegment(c[1], c[0]);
        } else {
            segment = new LineSegment(c[c.length - 2], c[c.length - 1]);
        }
        double segLen = segment.getLength();
        Coordinate[] extendedCoord = {
                segment.p1,
                new Coordinate(segment.p1.x
                        + ((segment.p1.x - segment.p0.x) / segLen) * ratio * 5,
                        segment.p1.y + ((segment.p1.y - segment.p0.y) / segLen)
                                * ratio * 5) };

        LineString extended = geomFac.createLineString(extendedCoord);

        double d = Double.MAX_VALUE, daux;
        closestCoordinate = null;
        Feature brokeFeature = null;
        Coordinate[] intCoords;
        for (Feature f : candidates.keySet()) {
            Geometry g = makeLine(f.getGeometry());
            Geometry intersection = g.intersection(extended);
            intCoords = intersection.getCoordinates();
            for (Coordinate coord : intCoords) {
                daux = segment.p0.distance(coord);
                if (daux < d) {
                    d = daux;
                    closestCoordinate = coord;
                    brokeFeature = f;
                }
            }
        }
        Coordinate[] coordsFull = new Coordinate[c.length];
        System.arraycopy(c, 0, coordsFull, 0, c.length);
        if (isStart) {
            coordsFull[0] = extendedCoord[1];
        } else {
            coordsFull[c.length - 1] = extendedCoord[1];
        }

        if (closestCoordinate == null) {
            JUMPWorkbench
                    .getInstance()
                    .getFrame()
                    .getContext()
                    .getLayerViewPanel()
                    .getContext()
                    .warnUser(
                            I18NPlug.getI18N("org.openjump.core.ui.tools.ExtendRectLineTool.It-is-not-close-enough-to-a-line"));
            return null;
        }

        Coordinate[] k = new Coordinate[c.length];
        System.arraycopy(c, 0, k, 0, c.length);
        if (isStart) {

            k[0] = closestCoordinate;

        } else {

            k[k.length - 1] = closestCoordinate;

        }

        toBrokeFeat = brokeFeature;
        return geomFac.createLineString(k);
    }

    /**
     * 
     * @param feat feature geometry
     * @param lsToCross linestring to cross
     * @param isStart
     * @param click the clicked point
     * @return
     */
    protected Point autoIntersection(Geometry feat, Geometry lsToCross,
            boolean isStart, Point click) {

        HashMap<Geometry, Double> listPoints = new HashMap<>();

        double dmin = Double.MAX_VALUE;
        for (int k = 0; k < feat.getNumGeometries(); k++) {
            LineString line = (LineString) feat.getGeometryN(k);

            if (click.distance(line) < dmin) {
                dmin = click.distance(line);
                listPoints = new HashMap<>();
                Coordinate[] coords = line.getCoordinates();
                Coordinate[] c = new Coordinate[2];

                double dRecorridoi = 0;

                for (int i = isStart ? 2 : 0; i < (isStart ? coords.length - 2
                        : coords.length - 4); i++) {
                    c[0] = coords[i];
                    c[1] = coords[i + 1];

                    LineString ls = geomFac.createLineString(c);
                    if (ls.intersects(lsToCross)) {
                        Geometry intersection = ls.intersection(lsToCross);
                        if (intersection instanceof Point) {
                            listPoints.put(intersection,
                                dRecorridoi
                                    + new LineSegment(c[0],
                                    intersection
                                        .getCoordinate())
                                    .getLength());
                        }
                    }
                    dRecorridoi += new LineSegment(c[0], c[1]).getLength();
                }
            }

        }

        // get the closest or the further point depending to our needs
        double dmax = 0;
        dmin = Double.MAX_VALUE;
        Point minPoint = null, maxPoint = null;
        double distance;
        for (Geometry intersection : listPoints.keySet()) {
            distance = listPoints.get(intersection);
            if (distance < dmin) {
                dmin = distance;
                minPoint = intersection.getInteriorPoint();
            }
            if (distance > dmax) {
                dmax = distance;
                maxPoint = intersection.getInteriorPoint();
            }
        }
        if (isStart) {
            return minPoint;
        } else {
            return maxPoint;
        }
    }

    /**
     * 
     * @param g input geometry
     * @return
     */
    protected Geometry makeLine(Geometry g) {

        if (g instanceof Polygon)
            return geomFac.createLineString(((Polygon) g).getExteriorRing()
                    .getCoordinates());

        if (g instanceof MultiPolygon) {
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

    /**
     * 
     * @param g input LineString
     * @param c coordinate to compare to geometry ends
     * @return true is c is nearer from the start of g than from its end
     */
    protected boolean isStart(LineString g, Coordinate c) {
        Coordinate start = g.getStartPoint().getCoordinate();
        Coordinate end = g.getEndPoint().getCoordinate();
        return c.distance(start) < c.distance(end);
    }

    /**
     * 
     * @param g input geometry
     * @param c
     * @return
     */
    protected int getInnGeometryIndex(Geometry g, Coordinate c) {
        int i = -1;
        if (g instanceof LineString) {
            return -1;
        } else if (g instanceof MultiLineString) {
            MultiLineString mls = (MultiLineString) g;
            Point p = geomFac.createPoint(c);
            double dis = Double.MAX_VALUE;
            double disAux;
            for (int k = 0; k < g.getNumGeometries(); k++) {
                LineString ls = (LineString) mls.getGeometryN(k);
                disAux = p.distance(ls);
                if (disAux < dis) {
                    dis = disAux;
                    i = k;
                }
            }
        }
        return i;
    }

    protected Map<Feature, Layer> getCandidates(Coordinate c, double r, Feature f) {

        WorkbenchContext context = JUMPWorkbench.getInstance().getFrame()
                .getContext();
        Envelope env = new Envelope(c.x - r, c.x + r, c.y - r, c.y + r);
        Map<Feature,Layer> s = new HashMap<>();
        List<Layer> layers = context.getLayerManager().getVisibleLayers(false);
        for (Layer layer : layers) {
            try {
                Set<Feature> k = EditUtils.intersectingFeatures(layer, env);
                for (Feature f2 : k)
                    s.put(f2, layer);
            } catch (Exception e) {
                Logger.warn("Exception in ExtendRectLineTool", e);
            }
        }
        s.remove(f);
        return s;
    }

    /**
     * Obtiene el icono asociado a la herramienta
     * 
     * @return Icon - Icono asociado a la herramienta
     */
    @Override
    public Icon getIcon() {
        return ICON;
    }

    /**
     * Obtiene el nombre asociado a la herramienta
     * 
     * @return String - Nombre asociado a la herramienta
     */
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Cursor getCursor() {
        return CURSOR;
    }

    /**
     * Capa seleccionada para modificar
     *
     * @return Layer
     */
    public Layer getLayer() {
        Collection<Layer> editableLayers = getPanel().getLayerManager()
                .getEditableLayers();

        if (editableLayers.isEmpty()) {
            return null;
        }
        return editableLayers.iterator().next();
    }
}
