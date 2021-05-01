/*
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
 * jump-pilot-devel@lists.sourceforge.net
 * http://sourceforge.net/projects/jump-pilot
 */
package org.openjump.advancedtools.language;

import java.io.File;
import com.vividsolutions.jump.I18N;

/**
 * Lightweight wrapper reusing OJ's I18N class
 * @author ed
 *
 */
public class I18NPlug {
    private static File path = new File("org/openjump/advancedtools/language/cadtoolbox");

    public static String getI18N(String key) {
        return getMessage(key);
    }

    public static String getMessage(String label, final Object... objects) {
        return I18N.getMessage(path, label, objects);
    }
}
