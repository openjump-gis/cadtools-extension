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
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;

import com.vividsolutions.jump.workbench.Logger;
import org.openjump.advancedtools.gui.LengthDialog;
import org.openjump.advancedtools.icon.IconLoader;
import org.openjump.advancedtools.language.I18NPlug;
import org.openjump.advancedtools.utils.CADEnableCheckFactory;
import org.openjump.advancedtools.utils.WorkbenchUtils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.operation.distance.DistanceOp;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.ui.cursortool.DragTool;

import es.kosmo.desktop.tools.algorithms.ParallelLinesAlgorithm;

/**
 * Tools that allows to generate a parallel line to a selected one. Original
 * code from Kosmo 3.0 SAIG - http://www.opengis.es/
 *
 * <p>
 * [Gabriel Bellido [March 10th, 2011]-> Parallel algorithm separated to the
 * class {@link ParallelLinesAlgorithm} to improve rehusability
 * </p>
 * 
 * @author Gabriel Bellido Perez
 * @author Sergio Ba�os Calvo - Changed code to use the parallel algorithm
 *         implemented by Axios at UDIG
 * @author Giuseppe Aruta [Genuary 30th 2017] rewrite code to adapt to OpenJUMP
 *         1.10 (http://www.openjump.org/support.html)
 * @since OpenJUMP 1.10 (2017)
 */
public class ParalelLineTool extends DragTool {

    private static final GeometryFactory geomFac = new GeometryFactory();

    /** Name of the tool */
    public final static String NAME = I18NPlug
            .getI18N("org.openjump.core.ui.tools.ParalellLineTool");
    /** description of the tool */
    public final static String DESCRIPTION = I18NPlug
            .getI18N("org.openjump.core.ui.tools.ParalellLineTool.description");
    /** Icon of the tool */
    public static final Icon ICON = IconLoader.icon("drawParallel.png");
    /** Cursor of the the tool */
    public static final Cursor CURSOR = new Cursor(Cursor.CROSSHAIR_CURSOR);

    String sDistance = I18N
            .get("org.openjump.core.ui.plugin.edittoolbox.tab.ConstraintsOptionsPanel.Length");
    /** Coordinate selected by user */
    protected Coordinate coordinateA;

    /** Length from original feature and new one */
    protected double length;

    /**
     * Features to Add
     */
    protected List<Feature> featsToAdd;
    /**
     * original features
     */
    protected List<Feature> featsOriginal;

    protected ParallelLinesAlgorithm parallelAlg = new ParallelLinesAlgorithm();

    EnableCheckFactory checkFactory = new EnableCheckFactory(JUMPWorkbench
            .getInstance().getContext());

    public ParalelLineTool(EnableCheckFactory checkFactory) {
        this.checkFactory = checkFactory;
        allowSnapping();

    }

    protected Feature getSelectedFeature() {
        final Layer editableLayer = WorkbenchUtils.getSelectedFeaturesLayer();
        final Collection<Feature> selectedFeatures = WorkbenchUtils
                .getSelectedFeatures(editableLayer);
        if (selectedFeatures == null || selectedFeatures.isEmpty()) {
            return null;
        }
        return selectedFeatures.iterator().next();
    }

    /**
     * builds the parallel geometry
     * 
     * @param editableLayer an editable Layer
     * @return geometry
     */
    protected Geometry getParallelCurve(Layer editableLayer) {
        Geometry nuevaGeom = null;
        Feature selectedFeature = getSelectedFeature();
        if (selectedFeature == null) {
            return null;
        }
        Geometry selected = selectedFeature.getGeometry();

        LengthDialog ld = new LengthDialog(JUMPWorkbench.getInstance()
                .getFrame(), this.length);

        ld.setVisible(true);
        if (!ld.cancelado) {
            length = ld.getLength();
            nuevaGeom = parallelAlg.calculateParallelCurve(selected, length,
                    coordinateA);

            if (selected instanceof MultiLineString) {
                nuevaGeom = geomFac
                        .createMultiLineString(new LineString[] { (LineString) nuevaGeom });
            }
        }

        return nuevaGeom;
    }

    /**
     * return the parallel geometry
     * 
     * @param editableLayer an editable Layer
     */
    protected void calculateParallelAndSave(Layer editableLayer) {

        if (editableLayer == null)
            return;
        this.featsOriginal = WorkbenchUtils.getSelectedFeatures(editableLayer);
        this.featsToAdd = new ArrayList<>();
        Geometry nuevaGeom = getParallelCurve(editableLayer);

        if (nuevaGeom != null) {
            featsToAdd.add(FeatureUtil.toFeature(nuevaGeom, editableLayer
                    .getFeatureCollectionWrapper().getFeatureSchema()));
            try {
                WorkbenchUtils.executeUndoableAddNewFeatsLeaveSelectedFeats(
                        NAME, getPanel().getSelectionManager(), editableLayer,
                        featsToAdd, featsOriginal);

            } catch (Exception e) {
                Logger.warn(e);
            }
        }
    }

    @Override
    protected void gestureFinished() throws Exception {
        reportNothingToUndoYet();
        Collection<Layer> layers = getPanel().getSelectionManager()
                .getLayersWithSelectedItems();

        for (Layer layerWithSelectedItems : layers) {
            calculateParallelAndSave(layerWithSelectedItems);
        }
    }

    /*
     * [Giuseppe Aruta - Feb.07 2017] Kosmo allows to MultiEnableCheck to
     * activate/deactivate icon tools on toolbar.OpenJUMP seems not. Using a
     * dragtool, we moved all the check warning messages, including check
     * geometry type to mousePressed
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
            if (!check(checkFactory.createAtLeastNLayersMustBeEditableCheck(1))) {
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
                                    CADEnableCheckFactory.FEATURE_SCHEMA_LINEARRING },

                            new int[] {

                                    CADEnableCheckFactory.FEATURE_SCHEMA_GEOMETRYCOLLECTION,

                                    CADEnableCheckFactory.FEATURE_SCHEMA_MULTIPOINT,
                                    CADEnableCheckFactory.FEATURE_SCHEMA_POINT,
                                    CADEnableCheckFactory.FEATURE_SCHEMA_MULTILINESTRING,
                                    CADEnableCheckFactory.FEATURE_SCHEMA_MULTIPOLYGON },
                            1))) {

                return;
            }
            super.mousePressed(e);
        } catch (Throwable t) {
            getPanel().getContext().handleThrowable(t);
        }
    }

    /**
     * Gets selected item
     * 
     * @param layer
     * @return Feature - selected item
     */
    public Feature getSelectedFeature(Layer layer) {
        Collection<Feature> features = getPanel().getSelectionManager()
                .getFeaturesWithSelectedItems(layer);
        if (features.size() == 0) {
            return null;
        }
        return features.iterator().next();
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

    @Override
    protected Shape getShape(Point2D source, Point2D destination)
            throws Exception {
        this.coordinateA = getPanel().getViewport().toModelCoordinate(
                destination);

        Layer layer = WorkbenchUtils.getSelectedFeaturesLayer();

        Feature feat = getSelectedFeature(layer);
        if (feat == null) {
            return null;
        }
        final Collection<Feature> selectedFeatures = WorkbenchUtils
                .getSelectedFeatures(layer);
        if (selectedFeatures.isEmpty()) {
            return null;
        }
        Geometry selected = getClosestFeatureToPoint(selectedFeatures,
                this.coordinateA).getGeometry();

        double d = selected.distance(geomFac.createPoint(this.coordinateA));
        Geometry newGeom = parallelAlg.calculateParallelCurve(selected, d,
                this.coordinateA);
        length = selected.distance(newGeom);
        DecimalFormat df2 = new DecimalFormat("##0.0##");
        getPanel().getContext().setStatusMessage(
                sDistance + ": " + df2.format(length));
        return getPanel().getJava2DConverter().toShape(newGeom);
    }

    /**
     * gets the closest feature to a coordinate from a collection
     * 
     * @param features
     *            Collection of features
     * @param c a Coordinate
     * @return closest feature to point c
     */
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

    /**
     * Not used. giving a coordinate A and a geometry g, it gets the closest
     * coordinate B (of the geometry g) to A
     * 
     * @param c a Coordinate A
     * @param g a Geometry
     * @return the Coordinate B
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

}
