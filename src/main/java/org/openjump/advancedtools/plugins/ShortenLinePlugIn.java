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

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.openjump.advancedtools.CadExtension;
import org.openjump.advancedtools.icon.IconLoader;
import org.openjump.advancedtools.tools.ShortenLineTool;
import org.openjump.advancedtools.tools.ShortenToClickedGeometryTool;
import org.openjump.advancedtools.tools.ShortenToDrawnLineTool;
import org.openjump.advancedtools.utils.CADEnableCheckFactory;
import org.openjump.advancedtools.utils.WorkbenchUtils;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.cursortool.SelectFeaturesTool;

/**
 * PlugIn that allows to extent a selected line to a desired target, depending
 * on the selected option. Original code from Kosmo 3.0 SAIG -
 * http://www.opengis.es/
 * <p>
 * The line can be extended to the nearby one, to the clicked geometry or to the
 * drawn line
 * </p>
 *
 * @author Gabriel Bellido Perez
 * @since Kosmo SAIG 1.0 (2006)
 * @author Giuseppe Aruta [Genuary 30th 2017] rewrite code to adapt to OpenJUMP
 *         1.10 (http://www.openjump.org/support.html)
 * @since OpenJUMP 1.10 (2017)
 */
public class ShortenLinePlugIn extends AbstractPlugIn {

    private static final I18N i18n = CadExtension.I18N;

    /** Plugin name */
    public final static String NAME = i18n
        .get("org.openjump.core.ui.plugins.ShortenLinePlugIn.Shorten-line");

    public final static String DESCRIPTION = i18n
        .get("org.openjump.core.ui.plugins.ShortenLinePlugIn.description");
    /** Options */
    public final static String OPTION = i18n
        .get("org.openjump.core.ui.plugins.ShortenLinePlugIn.Shorten-options");

    /** Plugin icon */
    public static final Icon ICON = IconLoader.icon("shortenLine.png");

    /** Plugin options */
    public static final String NEARBY_OPTION = i18n
        .get("org.openjump.core.ui.tools.General.Nearby");
    public static final String DRAWN_OPTION = i18n
        .get("org.openjump.core.ui.tools.General.Drawn");
    public static final String SELECTED_OPTION = i18n
        .get("org.openjump.core.ui.tools.General.Selected");
    public static final String CANCEL_OPTION = i18n
        .get("org.openjump.core.ui.tools.General.Cancel");

    /** Shorten Tools */
    protected ShortenLineTool elt = null;
    protected ShortenToDrawnLineTool etdlt = null;
    protected ShortenToClickedGeometryTool etcgt = null;

    /** Selecting tool in case of no check conditions */
    protected SelectFeaturesTool select = null;


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

    public Icon getIcon() {
        return ICON;
    }

    public ShortenLinePlugIn(PlugInContext context) throws Exception {
        super.initialize(context);
        createTools();
    }

    protected void createTools() {
        elt = new ShortenLineTool();
        etdlt = new ShortenToDrawnLineTool();
        etcgt = new ShortenToClickedGeometryTool();
        select = new SelectFeaturesTool(JUMPWorkbench.getInstance().getContext());
    }

    @Override
    public boolean execute(PlugInContext context) {
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
                                new int[] { CADEnableCheckFactory.FEATURE_SCHEMA_LINESTRING },
                                new int[] {
                                        CADEnableCheckFactory.FEATURE_SCHEMA_GEOMETRYCOLLECTION,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_MULTILINESTRING,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_MULTIPOINT,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_MULTIPOLYGON,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_POINT,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_POLYGON,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_LINEARRING },
                                1))) {
            return false;
        } else {
            int n;
            Object[] options = { NEARBY_OPTION, DRAWN_OPTION, SELECTED_OPTION,
                    CANCEL_OPTION };
            n = JOptionPane.showOptionDialog(context.getWorkbenchFrame(),
                    OPTION, NAME, JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[2]);

            // ... Use a switch statement to check which button was clicked.
            switch (n) {
            case 0:
                context.getLayerViewPanel().setCurrentCursorTool(elt);
                break;
            case 1:
                context.getLayerViewPanel().setCurrentCursorTool(etdlt);
                break;
            case 2:
                context.getLayerViewPanel().setCurrentCursorTool(etcgt);
                break;

            }
            return n != 3;
        }
    }
}
