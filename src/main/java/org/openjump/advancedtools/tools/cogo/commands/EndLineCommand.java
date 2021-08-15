/*
 * Kosmo - Sistema Abierto de Información Geográfica
 * Kosmo - Open Geographical Information System
 *
 * http://www.saig.es
 * (C) 2006, SAIG S.L.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, contact:
 *
 * Sistemas Abiertos de Información Geográfica, S.L.
 * Avnda. República Argentina, 28
 * Edificio Domocenter Planta 2ª Oficina 7
 * C.P.: 41930 - Bormujos (Sevilla)
 * España / Spain
 *
 * Teléfono / Phone Number
 * +34 954 788876
 *
 * Correo electrónico / Email
 * info@saig.es
 *
 */
package org.openjump.advancedtools.tools.cogo.commands;

import java.util.List;

import com.vividsolutions.jump.I18N;
import org.openjump.advancedtools.gui.SimpleLineDialog;
import org.openjump.advancedtools.tools.cogo.DrawGeometryCommandsTool;

import org.locationtech.jts.geom.Coordinate;
import com.vividsolutions.jump.workbench.JUMPWorkbench;

/**
 * <p>
 *   Command line to end the command
 * </p>
 * 
 * @author Juan Jose Macias Moron
 * @since Kosmo 1.1.1
 */
public class EndLineCommand extends LineCommand {

    private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");

    /** Nombre del comando */
    private final static String COMMAND_NAME = "end"; 
    /** Sintaxis del comando */
    private final static String SINTAXIS = "end"; 

    @Override
    protected Coordinate getSecondPointRelativeTo(Coordinate coordinate)
            throws LineCommandException {
        return null;
    }

    @Override
    protected void setSecondPoint(SimpleLineDialog sld)
            throws LineCommandException {
        // Nada
    }

    public static String getHelp() {
        return i18n.get("org.openjump.core.ui.tools.DrawLineStringCommandsTool.EndLineCommand.description");
    }

    public static String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getSintaxis() {
        return SINTAXIS;
    }

    @Override
    public void execute(DrawGeometryCommandsTool drawLineStringCommandsTool)
            throws LineCommandException {
        List<Coordinate> list = drawLineStringCommandsTool.getCoordinates();
        if (!list.isEmpty()) {
            drawLineStringCommandsTool.setGestureFinished();
        } else {
            JUMPWorkbench
                    .getInstance()
                    .getFrame()
                    .warnUser(
                            i18n.get("org.openjump.core.ui.tools.DrawLineStringCommandsTool.EndLineCommand.check"));
        }
    }

}
