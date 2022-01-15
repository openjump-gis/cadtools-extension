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

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

/**
 * @author Giuseppe Aruta
 * @since OpenJUMP 1.10
 */
public class CadExtension extends Extension {

  public static final I18N I18N = com.vividsolutions.jump.I18N.getInstance("org.openjump.advancedtools");

  @Override
  public String getName() {
    return I18N.get("org.openjump.core.ui.CAD.extension.name");
  }

  @Override
  public String getVersion() {
    return I18N.get("org.openjump.core.ui.CAD.extension.version");
  }

  @Override
  public void configure(PlugInContext context) {

    new EditToolboxCADPlugIn().initialize(context);
    new ExtendedDecorationPlugIn().initialize(context);
  }
}
