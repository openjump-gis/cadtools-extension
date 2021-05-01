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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;

import com.vividsolutions.jump.workbench.Logger;
import com.vividsolutions.jump.workbench.model.Layerable;
import org.openjump.advancedtools.language.I18NPlug;
import org.openjump.advancedtools.utils.EditUtils;
import org.openjump.advancedtools.utils.WorkbenchUtils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.util.CoordinateArrays;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.Animations;
import com.vividsolutions.jump.workbench.ui.cursortool.SpecifyFeaturesTool;

/**
 * Tool to create a symmetric axial image of selected geometries selecting a
 * segment as symmetry axes. Original code from Kosmo 3.0 SAIG -
 * http://www.opengis.es/
 * 
 * @author Gabriel Bellido Perez
 * @since Kosmo 1.0.0
 * @author Giuseppe Aruta [Genuary 30th 2017] rewrite code to adapt to OpenJUMP
 *         1.10 (http://www.openjump.org/support.html)
 * @since OpenJUMP 1.10 (2017)
 */

public class MirrorToSelectedSegmentTool extends SpecifyFeaturesTool {

    /** Name of the tool */
    public final static String NAME = I18NPlug
            .getI18N("org.openjump.core.ui.tools.Mirror");

    /** Cursor asociado a la herramienta */
    public static final Cursor CURSOR = createCursor(com.vividsolutions.jump.workbench.ui.images.IconLoader
            .icon("SnapVerticesTogetherCursor3.gif").getImage());

    /** */
    protected Coordinate coordinateA;

    /** */
    protected Coordinate coordinateB;

    protected Coordinate midPoint;

    /** */
    public static final int PIXEL_RANGE = 5;

    EnableCheckFactory checkFactory = new EnableCheckFactory(JUMPWorkbench
            .getInstance().getContext());

    /**
	 * 
	 *
	 */
    public MirrorToSelectedSegmentTool() {
        // Nothing to do
    }

    private void calculateMirrorFeatures(Collection<Feature> featureCopies,
            Coordinate coordinateA, Coordinate coordinateB) {
        for (Feature item : featureCopies) {
            Geometry espejo = item.getGeometry();
            rotate(espejo, rotationAngle(), coordinateA.x, coordinateA.y);
            translate(espejo, -coordinateA.x, -coordinateA.y);
            mirrorY(espejo);
            translate(espejo, coordinateA.x, coordinateA.y);
            rotate(espejo, -rotationAngle(), coordinateA.x, coordinateA.y);
            espejo.geometryChanged();
        }
    }

    protected void mirrorAndSave(Layer editableLayer) {
        if (editableLayer == null)
            return;
        LineSegment mirrorLineSegment = getClosestSegmentToClick();
        coordinateA = mirrorLineSegment.p0;
        coordinateB = mirrorLineSegment.p1;
        midPoint = mirrorLineSegment.midPoint();
        final List<Point2D> centers = new ArrayList<>();
        try {
            centers.add(getPanel().getViewport().toViewPoint(midPoint));
        } catch (NoninvertibleTransformException e1) {
            Logger.warn(e1);
        }
        final Collection<Feature> selectedFeatures = WorkbenchUtils
                .getSelectedFeatures(editableLayer);

        final Collection<Feature> featureCopies = EditUtils.conformCollection(
                selectedFeatures, editableLayer.getFeatureCollectionWrapper()
                        .getFeatureSchema());
        calculateMirrorFeatures(featureCopies, coordinateA, coordinateB);
        try {
            Animations.drawExpandingRings(centers, true, Color.BLUE,
                    getPanel(), new float[] { 5, 5 });

            WorkbenchUtils.executeUndoableAddNewFeatsLeaveSelectedFeats(NAME,
                    getPanel().getSelectionManager(), editableLayer,
                    featureCopies, selectedFeatures);
        } catch (Exception e) {
            Logger.warn(e);
            this.deactivate();
        }
    }

    /**
     */

    @Override
    protected void gestureFinished() throws java.lang.Exception {
        reportNothingToUndoYet();
        if (!check(checkFactory.createSelectedItemsLayersMustBeEditableCheck())) {
            return;
        }
        LineSegment mirrorLineSegment = getClosestSegmentToClick();
        if (mirrorLineSegment == null) {
            JUMPWorkbench
                    .getInstance()
                    .getFrame()
                    .getContext()
                    .getLayerViewPanel()
                    .getContext()
                    .warnUser(
                            I18NPlug.getI18N("org.openjump.core.ui.tools.MirrorSegmentTool.No-segment-was-selected"));
            return;
        }

        if (!check(checkFactory.createAtLeastNFeaturesMustBeSelectedCheck(1))) {
            return;
        }
        if (!check(checkFactory.createAtLeastNLayersMustBeEditableCheck(1))) {
            return;
        }
        for (Layer layerWithSelectedItems :
                getPanel().getSelectionManager().getLayersWithSelectedItems()) {
            mirrorAndSave(layerWithSelectedItems);
        }
    }

    /**
     * 
     * @return the closest LineSegment
     */
    protected LineSegment getClosestSegmentToClick() {
        LineSegment lineSegment = null;
        double distance = Double.MAX_VALUE;
        Coordinate click = getClickedPoint().getCoordinate();
        try {
            List<Layer> layersList = getWorkbench().getContext().getLayerManager()
                    .getVisibleLayers(true);
            Layer[] layers = layersList.toArray(new Layer[0]);
            Map<Layer,Collection<Feature>> map = layerToSpecifiedFeaturesMap(layers);
            for (Layer layer : layers) {
                Collection<Feature> features = map.get(layer);
                if (features == null)
                    continue;
                for (Feature feature : features) {
                    Geometry geometry = feature.getGeometry();
                    if (!(geometry instanceof LineString))
                        continue;
                    LineSegment ls = segmentInRange(feature.getGeometry(), click);
                    if (ls == null)
                        continue;
                    double d = ls.distance(click);
                    if (d < distance) {
                        distance = d;
                        lineSegment = ls;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lineSegment;
    }

    /**
     *
     * @param layerables
     * @return
     * @throws Exception
     */
    protected Map<Layer, Collection<Feature>> layerToSpecifiedFeaturesMap(
            Layerable[] layerables) throws Exception {
        HashMap<Layer, Collection<Feature>> layerToFeaturesMap = new HashMap<>();
        for (Layerable layerable : layerables) {
            if (layerable instanceof Layer) {
                Layer layer = (Layer) layerable;
                Set<Feature> intersectingFeatures = EditUtils.intersectingFeatures(
                    layer, getBoxInModelCoordinates());
                if (intersectingFeatures.isEmpty()) {
                    continue;
                }
                layerToFeaturesMap.put(layer, intersectingFeatures);
            }
        }
        return layerToFeaturesMap;
    }

    /**
     * 
     * @param geometry
     * @param ang
     * @param xrp
     * @param yrp
     */
    protected void rotate(Geometry geometry, double ang, double xrp, double yrp) {
        final double angle = ang;
        final double xr = xrp;
        final double yr = yrp;
        geometry.apply(new CoordinateFilter() {
            @Override
            public void filter(Coordinate coordinate) {
                double cosAngle = Math.cos(angle);
                double sinAngle = Math.sin(angle);
                double x = coordinate.x - xr;
                double y = coordinate.y - yr;
                coordinate.x = xr + (x * cosAngle) + (y * sinAngle);
                coordinate.y = yr + (y * cosAngle) - (x * sinAngle);
            }
        });
    }

    /**
     * 
     * @param geometry
     * @param xrp
     * @param yrp
     */
    protected void translate(Geometry geometry, double xrp, double yrp) {
        final double xr = xrp;
        final double yr = yrp;
        geometry.apply(new CoordinateFilter() {
            @Override
            public void filter(Coordinate coordinate) {
                double x = coordinate.x + xr;
                double y = coordinate.y + yr;
                coordinate.x = x;
                coordinate.y = y;
            }
        });
    }

    /**
     * 
     * @param geometry
     */
    protected void mirrorY(Geometry geometry) {
        geometry.apply(new CoordinateFilter() {
            @Override
            public void filter(Coordinate coordinate) {
                double x = -coordinate.x;
                double y = coordinate.y;
                coordinate.x = x;
                coordinate.y = y;
            }
        });
    }

    @Override
    public Cursor getCursor() {
        return CURSOR;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 
     * @return
     */
    public double rotationAngle() {
        return EditUtils.getAngle(coordinateA.x, coordinateA.y, coordinateB.x,
                coordinateB.y);
    }

    GeometryFactory geomFac = new GeometryFactory();

    /**
     * 
     */
    public Point getClickedPoint() {
        double x = getModelSource().x;
        double y = getModelSource().y;

        return geomFac.createPoint(new Coordinate(x, y));
    }

    /**
     * 
     * @param geometry
     * @param target
     * @return
     */
    protected LineSegment segmentInRange(Geometry geometry, Coordinate target) {
        // It's possible that the geometry may have no segments in range; for
        // example, if it
        // is empty, or if only has points in range. [Jon Aquino]
        LineSegment closest = null;
        List<Coordinate[]> coordArrays = CoordinateArrays.toCoordinateArrays(geometry, false);
        for (Coordinate[] coordinates : coordArrays) {
            for (int j = 1; j < coordinates.length; j++) { // 1
                LineSegment candidate = new LineSegment(coordinates[j - 1],
                        coordinates[j]);
                if (candidate.distance(target) > modelRange()) {
                    continue;
                }
                if ((closest == null)
                        || (candidate.distance(target) < closest
                                .distance(target))) {
                    closest = candidate;
                }
            }
        }
        return closest;
    }

    /**
     *
     */
    protected double modelRange() {
        return PIXEL_RANGE / getPanel().getViewport().getScale();
    }

    /**
     *
     */
    public static MultiEnableCheck createEnableCheck(
            final WorkbenchContext workbenchContext, AbstractCursorTool tool) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(
                workbenchContext);

        // al menos una capa debe tener elementos activos
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
