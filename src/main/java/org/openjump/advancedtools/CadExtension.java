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
package org.openjump.advancedtools;

import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

/**
 * @author Giuseppe Aruta
 * @since OpenJUMP 1.10
 */
public class CadExtension extends Extension {
	@Override
	public String getName() {
		return "Cad Tools (Giuseppe Aruta, Michaël Michaud - cad tools adapted from Kosmo 3.0, SkyJUMP and new tools)";
	}

	@Override
	public String getVersion() {
		return "2.0 (2021-05-01)";
	}

	@Override
	public void configure(PlugInContext context) {

		new EditToolboxCADPlugIn().initialize(context);
		new ExtendedDecorationPlugIn().initialize(context);
	}
}
