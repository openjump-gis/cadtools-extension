package org.openjump.advancedtools.deactivated;

import javax.swing.ImageIcon;

import org.openjump.advancedtools.icon.IconLoader;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;

/**
 * measures angle;
 *
 * @author Giuseppe Aruta - Sept 1th 2015
 */
public class ForceQuitPlugIn extends AbstractPlugIn {

	public static ImageIcon ICON = GUIUtil.resize(IconLoader.icon("test.png"), 20);

	public static final String NAME = "Force Quit";

	@Override
	public boolean execute(PlugInContext context) throws Exception {
		reportNothingToUndoYet(context);

		// WorkbenchUtils.quitOpenJUMP();
		context.getWorkbenchFrame().dispose();
		return true;
	}

	@Override
	public String getName() {
		return NAME;
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
