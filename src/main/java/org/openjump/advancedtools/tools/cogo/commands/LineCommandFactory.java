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

import org.apache.commons.lang3.StringUtils;

/**
 * Factoria de comandos para dibujar lineas </p>
 * 
 * @author Eduardo Montero Ruiz
 * @since Kosmo 1.0.0
 */
public class LineCommandFactory {
    public static LineCommand getCommand(String cadena) {
        LineCommand lineCommand = null;
        String lowerCaseCadena = cadena.toLowerCase();
        String comando = getComandoName(lowerCaseCadena);
        if (IncrementoLineCommand.getName().equals(comando)) {
            lineCommand = new IncrementoLineCommand(lowerCaseCadena);
        } else if (AbsolutoLineCommand.getName().equals(comando)) {
            lineCommand = new AbsolutoLineCommand(lowerCaseCadena);
        } else if (AngLineCommand.getName().equals(comando)) {
            lineCommand = new AngLineCommand(lowerCaseCadena);
        } else if (PerpLineCommand.getName().equals(comando)) {
            lineCommand = new PerpLineCommand(lowerCaseCadena);
        } else if (HelpLineCommand.getName().equals(comando)) {
            lineCommand = new HelpLineCommand();
        } else if (EndLineCommand.getName().equals(comando)) {
            lineCommand = new EndLineCommand();
        } else if (ZoomLineCommand.getName().equals(comando)) {
            lineCommand = new ZoomLineCommand();
        } else if (ZoomLastLineCommand.getName().equals(comando)) {
            lineCommand = new ZoomLastLineCommand();
        } else if (RemoveLastLineCommand.getName().equals(comando)) {
            lineCommand = new RemoveLastLineCommand();
        } else if (InvertLineCommand.getName().equals(comando)) {
            lineCommand = new InvertLineCommand();
        }
        return lineCommand;
    }

    /**
     * Reconoce el comando al que hace referencia la cadena
     * 
     * @param cadena
     * @return
     */
    private static String getComandoName(String cadena) {
        // Comando incremento
        if (cadena.startsWith("@") && StringUtils.countMatches(cadena, "@") == 1  
                && cadena.contains(",") && StringUtils.countMatches(cadena, ",") == 1) {  
            return IncrementoLineCommand.getName();
        }
        // Comando angline (polares)
        else if (cadena.startsWith("@") && StringUtils.countMatches(cadena, "@") == 1  
                && cadena.contains(">") && StringUtils.countMatches(cadena, ">") == 1) {  
            return AngLineCommand.getName();
        }
        // Comando absolutas
        else if (!cadena.contains("@") && cadena.contains(",")  
                && StringUtils.countMatches(cadena, ",") == 1) { 
            return AbsolutoLineCommand.getName();
        }
        // Comando ayuda
        else if (cadena.equals("help") || cadena.equals("ayuda")) {  
            return HelpLineCommand.getName();
        }
        // Comando ayuda
        else if (cadena.equals("end")) { 
            return EndLineCommand.getName();
        } else if (cadena.equals("zoom")) { 
            return ZoomLineCommand.getName();
        } else if (cadena.equals("zoomlast")) { 
            return ZoomLastLineCommand.getName();
        } else if (cadena.equals("del")) { 
            return RemoveLastLineCommand.getName();
        } else if (cadena.equals("inv")) { 
            return InvertLineCommand.getName();
        }
        // TODO parametro perp
        /*
         * int primerParentesis = cadena.indexOf("("); //Si ha encontrado el
         * parentesis, devolvemos el comando. //Null en caso contrario
         * if(primerParentesis != -1 ){ return
         * cadena.substring(0,primerParentesis); }else{ return null; }
         */
        return null;
    }

}
