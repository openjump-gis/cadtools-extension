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

import java.util.Collection;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openjump.advancedtools.CadExtension;
import org.openjump.advancedtools.icon.IconLoader;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.Logger;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.AbstractSelection;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.plugin.ClearSelectionPlugIn;

/**
 * Executes the selection
 * <p>
 * </p>
 * 
 * @author Sergio Ba&ntilde;os Calvo - sbc@saig.es
 * @since Kosmo 1.1
 * @author Giuseppe Aruta
 * @since OpenJUMP 1.10
 */
public class ClearLayerSelectionPlugIn extends AbstractPlugIn {

    private static final I18N i18n = CadExtension.I18N;

    /** Selection type */
    protected AbstractSelection selection;

    /** Name of tool */
    public final static String NAME = i18n
            .get("org.openjump.core.ui.plugins.Deselect");


    public ClearLayerSelectionPlugIn(PlugInContext context) throws Exception {
        super.initialize(context);
    }

    public Icon getIcon() {
        return ICON;
    }

    /** Icono asociado a la herramienta */
    public static ImageIcon ICON = IconLoader.icon("deselectEditing.png");

    @Override
    public String getName() {

        String tooltip;
        tooltip = "<HTML><BODY>";

        tooltip += "<b>" + NAME + "</b>";
        tooltip += "</BODY></HTML>";
        return tooltip;
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

        }
        try {
            new ClearSelectionPlugIn().execute(context);
        } catch (Exception ex) {
            Logger.warn(ex);
        }
        return true;

    }

    /**
     * Refresca la seleccion a partir del mapa de elementos seleccionados para
     * cada capa
     * 
     * @param layerToFeaturesInFenceMap a mapping of layers
     *        with features of this layer located in fence
     * @param context plugin context
     */
    protected void refreshSelection(
            Map<Layer, Collection<Feature>> layerToFeaturesInFenceMap,
            PlugInContext context) {
        Layer layer = context.getSelectedLayer(0);
        boolean originalPanelUpdatesEnabled = context.getLayerViewPanel()
                .getSelectionManager().arePanelUpdatesEnabled();
        context.getLayerViewPanel().getSelectionManager()
                .setPanelUpdatesEnabled(false);
        try {

            Collection<Feature> featuresToUnselect = selection
                    .getFeaturesWithSelectedItems(layer);
            selection.unselectItems(layer, featuresToUnselect);

        } finally {
            context.getLayerViewPanel().getSelectionManager()
                    .setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
        }
        context.getLayerViewPanel().getSelectionManager()
                .setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
        context.getLayerViewPanel().getSelectionManager().updatePanel();
    }

}