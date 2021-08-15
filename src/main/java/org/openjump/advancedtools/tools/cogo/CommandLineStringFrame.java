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

package org.openjump.advancedtools.tools.cogo;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import org.openjump.advancedtools.icon.IconLoader;
import org.openjump.core.ui.swing.DetachableInternalFrame;

import javax.swing.*;

/**
 * InternalFrame containing the CommandLineStringPanel
 */
public class CommandLineStringFrame extends DetachableInternalFrame {

    private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");

    public CommandLineStringFrame(DrawGeometryCommandsTool tool) {
        super();
        JPanel commandPanel = new CommandLineStringPanel(tool);
        setContentPane(commandPanel);
        setTitle(i18n.get("org.openjump.core.ui.tools.DrawLineStringCommandsTool.commands"));
        setIconifiable(true);
        setFrameIcon(GUIUtil.toSmallIcon(IconLoader.icon("cadTools.png")));
        pack();
        setClosable(true);
        setResizable(true);
        setMaximizable(true);
        JUMPWorkbench.getInstance().getFrame().addInternalFrame(this, true, true);
        setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
        setVisible(false);
    }

}
