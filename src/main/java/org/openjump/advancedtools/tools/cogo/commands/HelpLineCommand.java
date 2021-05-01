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

import org.openjump.advancedtools.gui.SimpleLineDialog;
import org.openjump.advancedtools.language.I18NPlug;

import org.locationtech.jts.geom.Coordinate;

/**
 * <p>
 * Command line for displaying help
 * </p>
 * 
 * @author Juan Jose Macias Moron
 * @since Kosmo 1.1.1
 */
public class HelpLineCommand extends LineCommand {

    /** Nombre del comando */
    private final static String COMMAND_NAME = "help"; 
    /** Sintaxis del comando */
    private final static String SINTAXIS = "help"; 

    @Override
    protected Coordinate getSecondPointRelativeTo(Coordinate coordinate)
            throws LineCommandException {
        return null;
    }

    @Override
    public String getSintaxis() {
        return SINTAXIS;
    }

    @Override
    protected void setSecondPoint(SimpleLineDialog sld)
            throws LineCommandException {
    }

    public static String getHelp() {

        String helpText = "<TABLE BORDER=1><TR><TH>"
                + I18NPlug
                        .getI18N("org.openjump.core.ui.tools.DrawLineStringCommandsTool.HelpLineCommand.syntax")  
                + "<TH>"
                + I18NPlug
                        .getI18N("org.openjump.core.ui.tools.DrawLineStringCommandsTool.HelpLineCommand.function");  
        helpText = helpText + "<TR><TD>" + SINTAXIS + "<TD>"  
                + I18NPlug
                        .getI18N("org.openjump.core.ui.tools.DrawLineStringCommandsTool.HelpLineCommand.description"); 
        helpText = helpText
                + "<TR><TD>" + (new AbsolutoLineCommand("")).getSintaxis() + "<TD>"  
                + AbsolutoLineCommand.getHelp();
        helpText = helpText
                + "<TR><TD>" + (new IncrementoLineCommand("")).getSintaxis() + "<TD>"  
                + IncrementoLineCommand.getHelp();
        helpText = helpText
                + "<TR><TD>" + (new AngLineCommand("")).getSintaxis() + "<TD>"  
                + AngLineCommand.getHelp();
        helpText = helpText
                + "<TR><TD>" + (new ZoomLineCommand()).getSintaxis() + "<TD>"  
                + ZoomLineCommand.getHelp();
        helpText = helpText
                + "<TR><TD>" + (new ZoomLastLineCommand()).getSintaxis() + "<TD>"  
                + ZoomLastLineCommand.getHelp();
        helpText = helpText
                + "<TR><TD>" + (new EndLineCommand()).getSintaxis() + "<TD>"  
                + EndLineCommand.getHelp();
        helpText = helpText + "</TABLE>"; 

        return helpText;
    }

    public static String getName() {
        return COMMAND_NAME;
    }

}
