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

import java.util.StringTokenizer;

/**
 * Clase de utilidades para los comandos de dibujo de lineas
 *
 * </p>
 * @author Eduardo Montero Ruiz
 * @since Kosmo 1.0.0
 */
public class LineCommandUtils {
    
    public static double getParametroNAsDouble(String comando, int n) throws LineCommandException{
        try{
            return Double.parseDouble(getParametroN(comando,n));
        }catch(NumberFormatException e){
            throw new LineCommandException();
        }
    }
    
    public static boolean getParametroNAsBoolean(String comando, int n) throws LineCommandException{
        try{
            return Boolean.parseBoolean(getParametroN(comando,n));
        }catch(Exception e){
            throw new LineCommandException();
        }
    }
    
    public static String getParametroN(String comando, int n) throws LineCommandException{
        String parametros = getParametros(comando);
        StringTokenizer tokenizer = new StringTokenizer(parametros, ","); 
        String param = null;
        //Si falla la obtencion del token, el comando esta mal formado
        try{
            for(int i = 0; i < n; i++){
                param  = tokenizer.nextToken();
            }
            return param;
        }catch(Exception e){
            throw new LineCommandException();
        }
    }
    
    /**
     *
     * @param comando
     * @return
     * @throws LineCommandException 
     */
    private static String getParametros( String comando ) throws LineCommandException {
        int primerParentesis = comando.indexOf("("); 
        int segundoParentesis = comando.indexOf(")"); 
        //Si no encuentra alguno de los parentesis, la expresion esta mal formada
        if(primerParentesis == -1 || segundoParentesis == -1){
            throw new LineCommandException();
        }
        return comando.substring(primerParentesis + 1, segundoParentesis );
    }
}
