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

import org.locationtech.jts.geom.Coordinate;
import org.openjump.advancedtools.CadExtension;
import org.openjump.advancedtools.gui.SimpleLineDialog;

import com.vividsolutions.jump.I18N;

/**
 * Command line to define the module/angle
 * <p>
 * </p>
 * 
 * @author Eduardo Montero Ruiz
 * @since Kosmo 1.0.0
 */
public class AngLineCommand extends LineCommand {

    private static final I18N i18n = CadExtension.I18N;

    /** Nombre del comando */
    private final static String COMMAND_NAME = "@"; 
    /** Sintaxis del comando */
    private final static String SINTAXIS = "@ '" 
            + i18n.get("org.openjump.core.ui.tools.DrawLineStringCommandsTool.AngLineCommand.length")
            + "'>'" 
            + i18n.get("org.openjump.core.ui.tools.DrawLineStringCommandsTool.AngLineCommand.angle")
            + "'"; 

    private double parametro1;
    private double parametro2;
    private final String comando;

    public AngLineCommand(String comando) {
        this.comando = comando;
    }

    protected void setSecondPoint(SimpleLineDialog sld)
            throws LineCommandException {
        setParameters();
        sld.setSecondPointLongAngulo(parametro1, parametro2);

    }

    private void setParameters() throws LineCommandException {
        int posSimbolo = comando.indexOf(">"); 
        try {
            parametro1 = Double.parseDouble(comando.substring(1, posSimbolo));
            parametro2 = Double.parseDouble(comando.substring(posSimbolo + 1));

        } catch (Exception e) {
            throw new LineCommandException();
        }
    }

    @Override
    public String getSintaxis() {
        return SINTAXIS;
    }

    @Override
    protected Coordinate getSecondPointRelativeTo(Coordinate coordinate)
            throws LineCommandException {
        setParameters();
        double angulo = (parametro2 * 2 * Math.PI) / 360.0;
        double x = parametro1 * Math.cos(angulo);
        double y = parametro1 * Math.sin(angulo);
        return new Coordinate(coordinate.x + x, coordinate.y + y);
    }

    public static String getHelp() {
        return i18n.get("org.openjump.core.ui.tools.DrawLineStringCommandsTool.AngLineCommand.description");
    }

    public static String getName() {
        return COMMAND_NAME;
    }

}
