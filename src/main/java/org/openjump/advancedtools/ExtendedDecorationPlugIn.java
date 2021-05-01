/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI 
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package org.openjump.advancedtools;

import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxDialog;
import org.openjump.core.ui.renderer.style.type.AnticlineStyle;
import org.openjump.core.ui.renderer.style.type.BedStyle;
import org.openjump.core.ui.renderer.style.type.BoxStyle;
import org.openjump.core.ui.renderer.style.type.CircleMiddleStyle;
import org.openjump.core.ui.renderer.style.type.NormalFaultStyle;
import org.openjump.core.ui.renderer.style.type.ReverseFaultStyle;
import org.openjump.core.ui.renderer.style.type.Syncline2Style;
import org.openjump.core.ui.renderer.style.type.SynclineStyle;
import org.openjump.core.ui.renderer.style.type.TrenchFaultStyle;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxPlugIn;

public class ExtendedDecorationPlugIn extends ToolboxPlugIn {

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void initialize(PlugInContext context) {
        WorkbenchContext workbenchContext = context.getWorkbenchContext();
        WorkbenchFrame frame = workbenchContext.getWorkbench().getFrame();
        frame.addChoosableStyleClass(CircleMiddleStyle.Start.class);
        frame.addChoosableStyleClass(BedStyle.Start.class);
        frame.addChoosableStyleClass(NormalFaultStyle.Small.class);
        frame.addChoosableStyleClass(NormalFaultStyle.VerySmall.class);
        frame.addChoosableStyleClass(ReverseFaultStyle.Small_open.class);
        frame.addChoosableStyleClass(ReverseFaultStyle.Small_closed.class);
        frame.addChoosableStyleClass(AnticlineStyle.Close.class);
        frame.addChoosableStyleClass(AnticlineStyle.Open.class);
        frame.addChoosableStyleClass(SynclineStyle.Small.class);
        frame.addChoosableStyleClass(Syncline2Style.Small.class);
        frame.addChoosableStyleClass(TrenchFaultStyle.Small.class);
        frame.addChoosableStyleClass(BoxStyle.Small.class);

    }

    public static MultiEnableCheck createEnableCheck(
            final WorkbenchContext workbenchContext) {

        MultiEnableCheck multiEnableCheck = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(
                workbenchContext);
        multiEnableCheck.add(checkFactory.createTaskWindowMustBeActiveCheck());

        return multiEnableCheck;
    }

    @Override
    protected void initializeToolbox(ToolboxDialog toolboxDialog) {

    }
}
