/* Copyright (2017) Giuseppe Aruta
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.openjump.advancedtools.plugins;

import javax.swing.Icon;

import org.openjump.advancedtools.gui.RegularPolygonDialog;
import org.openjump.advancedtools.icon.IconLoader;
import org.openjump.advancedtools.tools.RegularPolygonByDefinedRadiusTool;
import org.openjump.advancedtools.utils.WorkbenchUtils;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;

/**
 * @author Giuseppe Aruta [Genuary 30th 2017]
 * @since OpenJUMP 1.10 (2017)
 */
public class RegularPolygonPlugIn extends AbstractPlugIn {

    private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");

    /** Plugin name */
    public final static String NAME = i18n
        .get("org.openjump.core.ui.plugins.Regularpolygon");

    /** Plugin name */
    public final static String NAME2 = i18n
        .get("org.openjump.core.ui.plugins.Regularpolygon.description");

    /** Plugin icnon */
    public static final Icon ICON = IconLoader.icon("drawPolygon.png");

    // IconLoader.icon("DrawCircleConstrained.gif");

    /**
     * 
     */
    public RegularPolygonPlugIn(PlugInContext context) throws Exception {
        super.initialize(context);
    }

    @Override
    public String getName() {

        String tooltip;
        tooltip = "<HTML><BODY>";
        tooltip += "<DIV style=\"width: 300px; text-justification: justify;\">";
        tooltip += "<b>" + NAME + "</b>" + "<br>";
        tooltip += NAME2 + "<br>";
        tooltip += "</DIV></BODY></HTML>";
        return tooltip;
    }

    public Icon getIcon() {
        return ICON;
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
            action(context);
            return true;
        }
    }

    public void action(PlugInContext context) {
        CursorTool tool;
        RegularPolygonDialog cd = new RegularPolygonDialog(JUMPWorkbench
                .getInstance().getFrame());
        cd.setName(NAME);

        cd.setVisible(true);
        if (!cd.cancelado) {
            tool = WorkbenchUtils
                    .addStandardQuasimodes(RegularPolygonByDefinedRadiusTool
                            .create((LayerNamePanelProxy) context
                                    .getActiveInternalFrame()));

            context.getLayerViewPanel().setCurrentCursorTool(tool);
        }
    }

}