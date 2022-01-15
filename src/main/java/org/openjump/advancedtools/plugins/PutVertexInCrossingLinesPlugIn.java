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

package org.openjump.advancedtools.plugins;

import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.locationtech.jts.geom.LineString;
import org.openjump.advancedtools.CadExtension;
import org.openjump.advancedtools.icon.IconLoader;
import org.openjump.advancedtools.utils.CADEnableCheckFactory;
import org.openjump.advancedtools.utils.WorkbenchUtils;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.Logger;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.cursortool.SelectFeaturesTool;

import es.kosmo.desktop.tools.algorithms.VertexInCrossingLinesAlgorithm;

/**
 * Plugin to adda vertex on crossing linestring Original code from Kosmo 3.0
 * SAIG - http://www.opengis.es/
 * 
 * @author Gabriel Bellido Perez
 * @since Kosmo SAIG 1.0 (2006)
 * @author Giuseppe Aruta [Genuary 30th 2017] rewrite code to adapt to OpenJUMP
 *         1.10 (http://www.openjump.org/support.html)
 * @since OpenJUMP 1.10 (2017)
 */
public class PutVertexInCrossingLinesPlugIn extends AbstractPlugIn {

    private static final I18N i18n = CadExtension.I18N;

    private static final String NAME = i18n
        .get("org.openjump.core.ui.tools.PutVertexInCrossingLinesPlugIn.Put-vertex-in-crossing-lines");
    private static final String DESCRIPTION = i18n
        .get("org.openjump.core.ui.tools.PutVertexInCrossingLinesPlugIn.description");
    private static final ImageIcon ICON = GUIUtil.resize(
            IconLoader.icon("vertexInCross.png"), 20);
    private VertexInCrossingLinesAlgorithm vertextAlg = new VertexInCrossingLinesAlgorithm();


    /** Selecting tool in case of no check conditions */
    protected SelectFeaturesTool select = null;

    public PutVertexInCrossingLinesPlugIn(PlugInContext context) throws Exception {
        super.initialize(context);
        createTools();
    }

    protected void createTools() {
        select =
            new SelectFeaturesTool(JUMPWorkbench.getInstance().getContext());
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        reportNothingToUndoYet(context);

        if (!(JUMPWorkbench.getInstance().getFrame().getActiveInternalFrame() instanceof TaskFrame)) {
            JUMPWorkbench
                    .getInstance()
                    .getFrame()
                    .warnUser(
                            I18N.JUMP.get("com.vividsolutions.jump.workbench.plugin.A-Task-Window-must-be-active"));
            return false;
        } else if (!WorkbenchUtils
                .check(CADEnableCheckFactory
                        .createExactlyNFeaturesWithGeometryTypeMustBeSelectedCheck(
                                new int[] {
                                        CADEnableCheckFactory.FEATURE_SCHEMA_LINESTRING,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_LINEARRING },
                                new int[] {
                                        CADEnableCheckFactory.FEATURE_SCHEMA_GEOMETRYCOLLECTION,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_MULTILINESTRING,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_MULTIPOINT,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_MULTIPOLYGON,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_POINT,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_POLYGON },
                                2))) {
            return false;

        }

        else {
            try {
                action(context);
            } catch (Exception ex) {
                Logger.warn(ex);
            }
            return true;
        }
    }

    public void action(PlugInContext context) throws Exception {
        Layer editableLayer = WorkbenchUtils.getSelectedFeaturesLayer();
        List<Feature> selectedFeatures = WorkbenchUtils
                .getSelectedFeatures(editableLayer);
        Feature feat1 = selectedFeatures.get(0);
        Feature feat2 = selectedFeatures.get(1);
        if (this.vertextAlg.putVertexInCrossingLines(
                (LineString) feat1.getGeometry(),
                (LineString) feat2.getGeometry())) {
            Feature clone1 = feat1.clone(true);
            clone1.setGeometry(this.vertextAlg.getFirstLine());
            Feature clone2 = feat2.clone(true);
            clone2.setGeometry(this.vertextAlg.getSecondLine());

            WorkbenchUtils.executeUndoableAddNewFeatsRemoveSelectedFeats(NAME,
                    context.getLayerViewPanel().getSelectionManager(),
                    editableLayer,
                    Arrays.asList(new Feature[] { clone1, clone2 }),
                    Arrays.asList(new Feature[] { feat1, feat2 }));

        } else {
            JUMPWorkbench
                    .getInstance()
                    .getFrame()
                    .warnUser(
                            i18n.get("org.openjump.core.ui.tools.ExtendLinesAndCutWhereTheyTouchTool.Lines-do-not-cross"));
            return;
        }

    }

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

}
