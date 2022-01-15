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
import java.util.List;

import javax.swing.Icon;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Point;
import org.openjump.advancedtools.CadExtension;
import org.openjump.advancedtools.gui.LengthDialog;
import org.openjump.advancedtools.icon.IconLoader;
import org.openjump.advancedtools.utils.CADEnableCheckFactory;
import org.openjump.advancedtools.utils.WorkbenchUtils;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.Logger;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DragTool;

import es.kosmo.desktop.tools.algorithms.AuxiliaryParallelLinesAlgorithm;

/**
 * Tools that allows to generate parallel line, extended to the view, to a
 * selected one. Original code is
 * es.kosmo.desktop.tools.cadTools.AuxiliaryParalellLineTool from Kosmo 3.0 SAIG
 * - http://www.opengis.es/
 * 
 * @author Gabriel Bellido P&eacute;rez - gpb@saig.es
 * @since Kosmo SAIG 1.2
 * @author Giuseppe Aruta [Genuary 30th 2017] rewrite code to adapt to OpenJUMP
 *         1.10 (http://www.openjump.org/support.html)
 * @since OpenJUMP 1.10 (2017)
 */
public class ParalelAuxiliarylLineTool extends DragTool {

    private static final I18N i18n = CadExtension.I18N;

    private static GeometryFactory geomFac = new GeometryFactory();

    /** Name of the tool */
    public static final String NAME = i18n
        .get("org.openjump.core.ui.tools.AuxiliaryParalellLineTool");

    /** description of the tool */
    public final static String DESCRIPTION = i18n
        .get("org.openjump.core.ui.tools.AuxiliaryParalellLineTool.description");

    String sDistance = I18N.JUMP
            .get("org.openjump.core.ui.plugin.edittoolbox.tab.ConstraintsOptionsPanel.Length");

    /** Icon of the tool */
    public static final Icon ICON = GUIUtil.resize(
            IconLoader.icon("auxiliarParallel.png"), 20);

    /** Cursor of the the tool */
    public static final Cursor CURSOR = new Cursor(Cursor.CROSSHAIR_CURSOR);

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
    protected AuxiliaryParallelLinesAlgorithm parallelAlg = new AuxiliaryParallelLinesAlgorithm();
    //protected SnapIndicatorTool snapIndicatorTool;
    EnableCheckFactory checkFactory;
    //    JUMPWorkbench.getInstance().getContext().createPlugInContext().getCheckFactory();

    public ParalelAuxiliarylLineTool(EnableCheckFactory checkFactory) {
        super(JUMPWorkbench.getInstance().getContext());
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

    protected Geometry getParallelCurve() {
        Geometry nuevaGeom = null;
        Feature selectedFeature = getSelectedFeature();
        if (selectedFeature == null) {
            return null;
        }
        Geometry selected = selectedFeature.getGeometry();

        LengthDialog ld = new LengthDialog(JUMPWorkbench.getInstance()
                .getFrame(), selected.distance(geomFac
                .createPoint(this.coordinateA)));
        ld.setVisible(true);
        if (!ld.cancelado) {
            this.length = ld.getLength();
            Envelope activeViewportEnvelope = WorkbenchUtils
                    .getActiveViewportEnvelope();
            if (activeViewportEnvelope == null) {
                return null;
            }
            nuevaGeom = this.parallelAlg.calculateParallelCurve(selected,
                    this.length, this.coordinateA, activeViewportEnvelope);
            if (selected instanceof MultiLineString) {
                nuevaGeom = geomFac
                        .createMultiLineString(new LineString[] { (LineString) nuevaGeom });
            }

        }

        return nuevaGeom;
    }

    protected void calculateParallelAndSave() {
        final Layer editableLayer = WorkbenchUtils.getSelectedFeaturesLayer();
        if (editableLayer == null) {
            return;
        }
        this.featsOriginal = WorkbenchUtils.getSelectedFeatures(editableLayer);

        this.featsToAdd = new ArrayList<>();
        Geometry nuevaGeom = getParallelCurve();
        if (nuevaGeom == null) {
            return;
        }

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
        calculateParallelAndSave();
    }

    /*
     * [Giuseppe Aruta - Feb.07 2017] Kosmo allows to MultiEnableCheck to
     * activate/deactivate icon tools on toolbar.OpenJUMP seems not. So I use
     * mousePressed to activate/deactivate all the check warning messages,
     * including geometry type chek
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
        Geometry selected = feat.getGeometry();
        double d = selected.distance(geomFac.createPoint(this.coordinateA));

        Envelope activeViewportEnvelope = WorkbenchUtils
                .getActiveViewportEnvelope();
        if (activeViewportEnvelope == null) {
            return null;
        }
        Geometry nueva = this.parallelAlg.calculateParallelCurve(selected, d,
                this.coordinateA, activeViewportEnvelope);
        this.length = selected.distance(nueva);
        DecimalFormat df2 = new DecimalFormat("##0.0##");
        getPanel().getContext().setStatusMessage(
                sDistance + ": " + df2.format(length));
        return getPanel().getJava2DConverter().toShape(nueva);
    }

    public Point getClickedPoint() {
        Coordinate c = snap(getModelSource());
        return geomFac.createPoint(c);
    }

    public static MultiEnableCheck createEnableCheck(
            WorkbenchContext workbenchContext, AbstractCursorTool tool) {
        MultiEnableCheck check = new MultiEnableCheck();
        EnableCheckFactory checkFactory =
            workbenchContext.createPlugInContext().getCheckFactory();

        check.add(checkFactory.createTaskWindowMustBeActiveCheck());
        check.add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck());
        check.add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
        check.add(checkFactory.createSelectedItemsLayersMustBeEditableCheck());
        check.add(checkFactory.createAtLeastNFeaturesMustBeSelectedCheck(1));

        return check;
    }

}
