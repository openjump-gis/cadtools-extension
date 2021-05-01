package org.openjump.advancedtools.gui;

import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.openjump.advancedtools.language.I18NPlug;

import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public class GeneralPanel {
    public static JCheckBox centerCheck = new JCheckBox(
            I18NPlug.getI18N("org.openjump.core.ui.plugins.Dialog.draw-center-as-point"));
    public static JCheckBox polygonCheck = new JCheckBox(
            I18NPlug.getI18N("org.openjump.core.ui.plugins.Dialog.draw-as-filled-polygon"));

    public static JPanel mainPanel = new JPanel(new GridLayout(0, 1));

    @SuppressWarnings({})
    public JPanel generalPanel(PlugInContext context) {

        mainPanel.add(centerCheck);
        mainPanel.add(polygonCheck);

        // FormUtils.addRowInGBL(mainPanel, 3, 0, option_panel);

        return mainPanel;
    }

}
