package org.openjump.advancedtools.block;

import javax.swing.*;

import com.vividsolutions.jump.workbench.Logger;
import org.openjump.advancedtools.icon.IconLoader;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;

public class DrawBlockPlugIn extends AbstractPlugIn {

    private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");

    public static ImageIcon ICON = IconLoader.icon("textblock/block_ins.png");

    public static final String NAME = i18n
        .get("org.openjump.core.ui.plugins.block.DrawblockTool");
    public static final String NAME2 = i18n
        .get("org.openjump.core.ui.plugins.block.DrawblockTool.description");

    BlockPanel blockPanel;

    public DrawBlockPlugIn(BlockPanel blockPanel) {
        super();
        this.blockPanel = blockPanel;
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
                CursorTool polyTool = DrawBlockTool
                        .create((LayerNamePanelProxy) context
                                .getActiveInternalFrame(), blockPanel);
                context.getLayerViewPanel().setCurrentCursorTool(polyTool);
            } catch (Exception ex) {
                Logger.warn(ex);
            }
            return true;
        }
    }

    @Override
    public String getName() {

        String tooltip;
        tooltip = "<HTML><BODY>";
        tooltip += "<DIV style=\"width: 320px; text-justification: justify;\">";
        tooltip += "<b>" + NAME + "</b>" + "<br>";
        tooltip += NAME2 + "<br>";
        tooltip += "</DIV></BODY></HTML>";
        return tooltip;
    }

    public ImageIcon getIcon() {
        return ICON;
    }

    public static MultiEnableCheck createEnableCheck(
            WorkbenchContext workbenchContext) {

        EnableCheckFactory checkFactory =
            workbenchContext.createPlugInContext().getCheckFactory();

        return new MultiEnableCheck().add(checkFactory
                .createWindowWithSelectionManagerMustBeActiveCheck());
    }
}
