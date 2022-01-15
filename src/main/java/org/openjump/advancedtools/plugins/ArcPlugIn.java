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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.openjump.advancedtools.CadExtension;
import org.openjump.advancedtools.gui.ArcDialog;
import org.openjump.advancedtools.icon.IconLoader;
import org.openjump.advancedtools.tools.ArcTool;
import org.openjump.advancedtools.utils.EditUtils;
import org.openjump.advancedtools.utils.WorkbenchUtils;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.Logger;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;

/**
 * Draw a arc
 * <p>
 * </p>
 * 
 * @author Gabriel Bellido P&eacute;rez - gbp@saig.es
 * @since Kosmo 1.0
 * @author Giuseppe Aruta
 * @since OpenJUMP 1.10
 */
public class ArcPlugIn extends AbstractPlugIn {

    private static final I18N i18n = CadExtension.I18N;

    /** Plugin name */
    public final static String NAME = i18n
        .get("org.openjump.core.ui.plugins.Arc");

    public final static String DESCRIPTION = i18n
        .get("org.openjump.core.ui.plugins.Arc.description");
    /** Plugin icon */
    public static final ImageIcon ICON = IconLoader.icon("drawArc.png");

    ///** Drawing tool */
    //protected QuasimodeTool quasimodeTool;
    //protected ArcTool dcp;

    /**
     * 
     */
    public ArcPlugIn(PlugInContext context) throws Exception {
        super.initialize(context);
    }

    @Override
    public String getName() {

        String tooltip;
        tooltip = "<HTML><BODY>";
        tooltip += "<DIV style=\"width: 200px; text-justification: justify;\">";
        tooltip += "<b>" + NAME + "</b>" + "<br>";
        tooltip += DESCRIPTION + "<br>";
        tooltip += "</DIV></BODY></HTML>";
        return tooltip;
    }

    public Icon getIcon() {
        return ICON;
    }

    /** Intended to be used by subclases. */
    protected void setDefaultOption(ArcDialog dialog) {
        dialog.getJrbindicarRaton().doClick();
        // doClick to select and to fire the actionlistener
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
        } else {
            try {
                action(context);
            } catch (Exception ex) {
                Logger.warn(ex);
            }
            return true;

        }
    }

    public void action(PlugInContext context) throws Exception {
        ArcDialog cd = new ArcDialog(JUMPWorkbench.getInstance().getFrame());
        CursorTool tool;
        cd.setName(NAME);
        setDefaultOption(cd);
        cd.setVisible(true);
        if (!cd.cancelado) {
            if (cd.raton) {
                tool = WorkbenchUtils.addStandardQuasimodes(ArcTool
                        .create((LayerNamePanelProxy) context
                                .getActiveInternalFrame()));
                WorkbenchUtils.activateTool(tool);
            } else if (cd.absoluto) {
                double x = cd.x;
                double y = cd.y;
                double r = cd.r;
                double a1 = cd.a1 * Math.PI / 180d;
                double a2 = cd.a2 * Math.PI / 180d;
                save(new Coordinate(x, y), r, a1, a2,
                        context.getLayerViewPanel(), context);
            }
        }
        return;

    }

    public void save(Coordinate centerCoord, double distance,
            double startAngle, double endAngle, LayerViewPanel panel,
            PlugInContext context) throws Exception {
        final Layer editableLayer = editableLayer(context);
        if (editableLayer == null)
            return;

        final SelectionManager selectionManager = panel.getSelectionManager();
        final List<Feature> featsToAdd = new ArrayList<>();

        Geometry arc = createGeometry(centerCoord, distance, startAngle,
                endAngle);
        Feature nueva = FeatureUtil.toFeature(arc, editableLayer
                .getFeatureCollectionWrapper().getFeatureSchema());
        featsToAdd.add(nueva);

        selectionManager.unselectItems(editableLayer);
        editableLayer.getFeatureCollectionWrapper().addAll(featsToAdd);
        selectionManager.getFeatureSelection().selectItems(editableLayer,
                featsToAdd);

    }

    GeometryFactory geomFac = new GeometryFactory();

    public Geometry createGeometry(Coordinate centerCoord, double distance,
            double startAngle, double endAngle) {
        Coordinate[] coords = EditUtils.createArc(centerCoord, distance,
                startAngle, endAngle, 40);

        return geomFac.createLineString(coords);
    }

    public Layer editableLayer(PlugInContext context) {
        Layer layer;
        final WorkbenchContext wbcontext = context.getWorkbenchContext();
        LayerManager layerManager = wbcontext.getLayerManager();
        @SuppressWarnings("rawtypes")
        Collection layerCollection = layerManager.getLayers();
        if (layerCollection.isEmpty()) {
            FeatureSchema featureSchema = new FeatureSchema();
            featureSchema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
            FeatureCollection myCollA = new FeatureDataset(featureSchema);
            layer = context
                    .addLayer(
                            StandardCategoryNames.WORKING,
                            I18N.JUMP.get("org.openjump.core.ui.plugins.edit.ReplicateSelectedItemsPlugIn.new"),
                            myCollA);
        } else {
            layer = wbcontext.createPlugInContext().getSelectedLayer(0);
        }
        layer.setEditable(true);
        return layer;

    }

}