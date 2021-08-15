package org.openjump.advancedtools.annotation;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import com.vividsolutions.jump.I18N;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.WorkbenchToolBar;

public class TextPanel extends JPanel {

    private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");

    private static final long serialVersionUID = 1L;
    public static PlugInContext context;
    public static final String NAME = "Insert symbol as block";

    public static JPanel text_panel = new JPanel(new GridLayout(0, 1));
    public static JPanel textMainPanel = new JPanel(new GridLayout(0, 1));
    public static JLabel text_panel_jlabel1;
    public static JLabel text_panel_jlabel2;
    private static TitledBorder textTitledBorder;

    public static SpinnerModel textRotationModel = new SpinnerNumberModel(0, // initial
            // value
            0, // min
            359, // max
            1);
    public static SpinnerModel textDimensionModel = new SpinnerNumberModel(14, // initial
            // value
            6, // min
            96, // max
            1);
    public static JSpinner textRotationSpinner = new JSpinner();
    public static JSpinner textDimensionSpinner = new JSpinner();

    private static final WorkbenchToolBar textToolBar =
        new WorkbenchToolBar(null) {

        private static final long serialVersionUID = 1L;

        @Override
        public JButton addPlugIn(Icon icon, PlugIn plugIn,
                EnableCheck enableCheck, WorkbenchContext workbenchContext) {
            return super.addPlugIn(icon, plugIn, enableCheck, workbenchContext);
        }

    };

    public JPanel panel_text(PlugInContext context) {
        text_panel = new JPanel(new GridBagLayout());
        text_panel_jlabel1 = new JLabel(
                i18n.get("org.openjump.core.ui.plugins.block.dialog-rotation"));
        text_panel_jlabel1
                .setToolTipText(i18n
                        .get("org.openjump.core.ui.plugins.annotation.dialog.font-rotation-message"));
        text_panel_jlabel1.setHorizontalTextPosition(JLabel.LEFT);
        textRotationSpinner = new JSpinner(textRotationModel);
        textDimensionSpinner = new JSpinner(textDimensionModel);
        text_panel_jlabel2 = new JLabel(
            i18n.get("org.openjump.core.ui.plugins.block.dialog-dimension"));
        text_panel_jlabel2.setToolTipText(gedtDescription());
        //FormUtils.addRowInGBL(text_panel, 1, 0, null, text_panel_jlabel1,
        //        textRotationSpinner, text_panel_jlabel2, textDimensionSpinner);
        AddTextPlugIn add = new AddTextPlugIn();
        ChangeTextPlugIn change = new ChangeTextPlugIn();

        textMainPanel = new JPanel(new GridBagLayout());
        textTitledBorder = new TitledBorder(BorderFactory.createEtchedBorder(
                Color.white, new Color(148, 145, 140)), "Annotation");
        textMainPanel.setBorder(textTitledBorder);
        textToolBar.addPlugIn(add.getIcon(), add, null,
                context.getWorkbenchContext());
        textToolBar.addPlugIn(change.getIcon(), change, null,
                context.getWorkbenchContext());

        FormUtils.addRowInGBL(textMainPanel, 1, 0, textToolBar);

        FormUtils.addRowInGBL(textMainPanel, 2, 0, null, text_panel_jlabel2,
                textDimensionSpinner, text_panel_jlabel1, textRotationSpinner);
        return textMainPanel;
    }

    public String gedtDescription() {

        String tooltip;
        tooltip = "<HTML><BODY>";
        tooltip += "<DIV style=\"width: 200px; text-justification: justify;\">";
        tooltip += "<b>"
                + i18n.get("org.openjump.core.ui.plugins.annotation.dialog.dimension")
                + "</b>" + "<br>";
        tooltip += i18n.get("org.openjump.core.ui.plugins.annotation.dialog.dimension-description")
                + "<br>";
        tooltip += "</DIV></BODY></HTML>";
        return tooltip;
    }

}