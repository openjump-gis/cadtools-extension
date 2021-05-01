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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.vividsolutions.jump.workbench.Logger;
import org.openjump.advancedtools.icon.IconLoader;
import org.openjump.advancedtools.language.I18NPlug;
import org.openjump.advancedtools.utils.CADEnableCheckFactory;
import org.openjump.advancedtools.utils.WorkbenchUtils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineString;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.FeatureDrawingUtil;

import es.kosmo.desktop.tools.algorithms.ExpandLinesUntilTouchAlgorithm;

/**
 * Tools that allows to extend two converging linestrings untill they touch
 * Original code from Kosmo 3.0
 * 
 * @author Gabriel Bellido Perez
 * @author Giuseppe Aruta [Genuary 30th 2017] rewrite code to adapt to OpenJUMP
 *         1.10
 * @since OpenJUMP 1.3.1
 */

public class ExtendLinesAndCutWhereTheyTouchTool extends ConstrainedNClickTool {

    private static final String NAME = I18NPlug
            .getI18N("org.openjump.core.ui.tools.ExtendLinesAndCutWhereTheyTouchTool.Expand-lines-until-crossing");
    private static final String DESCRIPTION = I18NPlug
            .getI18N("org.openjump.core.ui.tools.ExtendLinesAndCutWhereTheyTouchTool.description");
    private static final ImageIcon ICON = GUIUtil.resize(
            IconLoader.icon("extendAndCut.png"), 20);
    // public static final Cursor CURSOR = new Cursor(Cursor.CROSSHAIR_CURSOR);

    private final ExpandLinesUntilTouchAlgorithm algorithm =
        new ExpandLinesUntilTouchAlgorithm();

    EnableCheckFactory checkFactory = new EnableCheckFactory(JUMPWorkbench
            .getInstance().getContext());
    FeatureDrawingUtil featureDrawingUtil;

    public ExtendLinesAndCutWhereTheyTouchTool(
            FeatureDrawingUtil featureDrawingUtil) {
        super(1);
        this.featureDrawingUtil = featureDrawingUtil;
        allowSnapping();
    }

    public static CursorTool create(LayerNamePanelProxy layerNamePanelProxy) {
        FeatureDrawingUtil featureDrawingUtil = new FeatureDrawingUtil(
                layerNamePanelProxy);
        return featureDrawingUtil.prepare(
                new ExtendLinesAndCutWhereTheyTouchTool(featureDrawingUtil),
                true);
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
    protected void gestureFinished() throws Exception {
        reportNothingToUndoYet();
        Collection<Layer> layers = getPanel().getSelectionManager()
                .getLayersWithSelectedItems();
        if (!check(checkFactory.createSelectedItemsLayersMustBeEditableCheck())) {
            return;
        }

        if (!check(checkFactory
                .createOnlyOneLayerMayHaveSelectedFeaturesCheck())) {
            return;
        }
        if (!WorkbenchUtils
                .check(CADEnableCheckFactory
                        .createExactlyNFeaturesWithGeometryTypeMustBeSelectedCheck(
                                new int[] { CADEnableCheckFactory.FEATURE_SCHEMA_LINESTRING },
                                new int[] {
                                        CADEnableCheckFactory.FEATURE_SCHEMA_GEOMETRYCOLLECTION,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_MULTILINESTRING,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_MULTIPOINT,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_MULTIPOLYGON,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_POINT,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_POLYGON,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_LINEARRING },
                                2))) {
            return;
        }
        for (Layer layerWithSelectedItems : layers) {
            expandToCrossing(layerWithSelectedItems);
        }

    }

    protected void expandToCrossing(Layer editableLayer) {
        if (editableLayer == null)
            return;
        Coordinate c = (Coordinate) getCoordinates().get(0);
        Envelope activeViewportEnvelope = WorkbenchUtils
                .getActiveViewportEnvelope();
        if (activeViewportEnvelope == null) {
            JUMPWorkbench
                    .getInstance()
                    .getFrame()
                    .warnUser(
                            NAME
                                    + " - "
                                    + I18NPlug
                                            .getI18N("org.openjump.core.ui.tools.ExtendLinesAndCutWhereTheyTouchTool.There-must-be-an-active-view"));
            return;
        }

        List<Feature> selectedFeatures = WorkbenchUtils
                .getSelectedFeatures(editableLayer);
        Feature feat1 = selectedFeatures.get(0);
        Feature feat2 = selectedFeatures.get(1);

        LineString line1 = (LineString) feat1.getGeometry();
        LineString line2 = (LineString) feat2.getGeometry();
        List<LineString> lines = this.algorithm.expandAndCutLines(line1, line2,
                activeViewportEnvelope, c);
        if (lines.size() == 0) {
            JUMPWorkbench
                    .getInstance()
                    .getFrame()
                    .warnUser(
                            I18NPlug.getI18N("org.openjump.core.ui.tools.ExtendLinesAndCutWhereTheyTouchTool.Lines-do-not-cross"));
            return;
        }
        LineString lineString1 = lines.get(0);
        LineString lineString2 = lines.get(1);

        Feature clone1 = feat1.clone(true);
        Feature clone2 = feat2.clone(true);
        clone1.setGeometry(lineString1);
        clone2.setGeometry(lineString2);

        try {

            WorkbenchUtils.executeUndoableAddNewFeatsRemoveSelectedFeats(NAME,
                    getPanel().getSelectionManager(), editableLayer,
                    Arrays.asList(new Feature[] { clone1, clone2 }),
                    Arrays.asList(new Feature[] { feat1, feat2 }));

        } catch (Exception e) {
            Logger.warn(e);
        }
        this.deactivate();
    }
}
