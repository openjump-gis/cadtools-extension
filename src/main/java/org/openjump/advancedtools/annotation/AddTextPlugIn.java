package org.openjump.advancedtools.annotation;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import com.vividsolutions.jump.I18N;
import org.openjump.advancedtools.icon.IconLoader;

import com.vividsolutions.jump.feature.FeatureCollectionWrapper;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public class AddTextPlugIn extends AbstractPlugIn {

    private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");

    public static ImageIcon ICON = IconLoader
            .icon("textblock/annotation_add.png");

    /** Name of the tool */
    public final static String NAME = i18n
            .get("org.openjump.core.ui.plugins.annotation.AddTextTool.name");
    /** Description of the tool */
    public final static String DESCRIPTION = i18n
            .get("org.openjump.core.ui.plugins.annotation.AddTextTool.description");

    public final static String MESSAGE = i18n
            .get("org.openjump.core.ui.plugins.annotation.AddTextTool.message");

    @Override
    public String getName() {

        String tooltip = "";
        tooltip = "<HTML><BODY>";
        tooltip += "<DIV style=\"width: 200px; text-justification: justify;\">";
        tooltip += "<b>" + NAME + "</b>" + "<br>";
        tooltip += DESCRIPTION + "<br>";
        tooltip += "</DIV></BODY></HTML>";
        return tooltip;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        reportNothingToUndoYet(context);

        context.getLayerViewPanel().setCurrentCursorTool(new AddTextTool(context));
        return true;
    }

    public ImageIcon getIcon() {
        return ICON;
    }

    public static EnableCheck createEnableCheck(
            final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory =
            workbenchContext.createPlugInContext().getCheckFactory();

        return new MultiEnableCheck()
                .add(checkFactory
                        .createWindowWithLayerViewPanelMustBeActiveCheck())
                .add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1))
                .add(new EnableCheck() {
                    @Override
                    public String check(JComponent component) {

                        Layer layer = workbenchContext.createPlugInContext()
                                .getSelectedLayer(0);
                        FeatureCollectionWrapper fcw = layer
                                .getFeatureCollectionWrapper();
                        FeatureSchema schema = fcw.getFeatureSchema();
                        if ((schema.hasAttribute("COLOR") && schema
                                .hasAttribute("TEXT"))

                        ) {
                            return null;
                        }
                        return MESSAGE;
                    }
                });
    }
}
