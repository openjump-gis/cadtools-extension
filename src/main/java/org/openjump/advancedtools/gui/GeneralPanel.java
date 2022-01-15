package org.openjump.advancedtools.gui;

import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.openjump.advancedtools.CadExtension;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public class GeneralPanel {

    private static final I18N i18n = CadExtension.I18N;

    public static JCheckBox centerCheck = new JCheckBox(
        i18n.get("org.openjump.core.ui.plugins.Dialog.draw-center-as-point"));
    public static JCheckBox polygonCheck = new JCheckBox(
        i18n.get("org.openjump.core.ui.plugins.Dialog.draw-as-filled-polygon"));

    public static JPanel mainPanel = new JPanel(new GridLayout(0, 1));

    @SuppressWarnings({})
    public JPanel generalPanel(PlugInContext context) {

        mainPanel.add(centerCheck);
        mainPanel.add(polygonCheck);

        // FormUtils.addRowInGBL(mainPanel, 3, 0, option_panel);

        return mainPanel;
    }

}
