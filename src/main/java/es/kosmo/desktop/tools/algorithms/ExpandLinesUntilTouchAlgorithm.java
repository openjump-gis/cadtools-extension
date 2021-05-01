/* 
 * Kosmo - Sistema Abierto de Informaci�n Geogr�fica
 * Kosmo - Open Geographical Information System
 *
 * http://www.saig.es
 * (C) 2011, SAIG S.L.
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
 * Sistemas Abiertos de Informaci�n Geogr�fica, S.L.
 * Avnda. Rep�blica Argentina, 28
 * Edificio Domocenter Planta 2� Oficina 7
 * C.P.: 41930 - Bormujos (Sevilla)
 * Espa�a / Spain
 *
 * Tel�fono / Phone Number
 * +34 954 788876
 * 
 * Correo electr�nico / Email
 * info@saig.es
 *
 */
package es.kosmo.desktop.tools.algorithms;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineString;

/**
 * <p>
 * Expands two linestrings and cuts them in pieces
 * </p>
 * 
 * @author Gabriel Bellido P&eacute;rez - gbp@saig.es
 * @since Kosmo 1.2
 */
public class ExpandLinesUntilTouchAlgorithm {

    AuxiliaryParallelLinesAlgorithm expansionAlg = new AuxiliaryParallelLinesAlgorithm();
    VertexInCrossingLinesAlgorithm vertexAlg = new VertexInCrossingLinesAlgorithm();

    /**
     * Expand two linestrings from the closest extremes to c until they reach
     * env borders and cuts them in pieces in the places they cross.
     * 
     * @param line1 first line to expand
     * @param line2 second line to expand
     * @param env envelope to clip expanded lines on
     * @param c coordinate to indicate the expansion direction
     * @return a list of expanded lines
     */
    public List<LineString> expandAndCutLines(LineString line1,
            LineString line2, Envelope env, Coordinate c) {
        LineString expandedLine1 = (LineString) expansionAlg
                .expandClosestEndSegment(line1, env, c);
        LineString expandedLine2 = (LineString) expansionAlg
                .expandClosestEndSegment(line2, env, c);
        if (vertexAlg.cutCrossingLines(expandedLine1, expandedLine2)) {
            List<LineString> cuts = vertexAlg.getCuts();
            List<LineString> finalCuts = new ArrayList<>();
            finalCuts.addAll(cuts);
            for (LineString cut : cuts) {
                if (!(line1.intersects(cut) || line2.intersects(cut))) {
                    finalCuts.remove(cut);
                }
            }
            return finalCuts;
        }
        return new ArrayList<>();
    }

    public List<LineString> expandOneLine(LineString line1, LineString line2,
            Envelope env, Coordinate c) {
        LineString expandedLine1 = (LineString) expansionAlg
                .expandClosestEndSegment(line1, env, c);
        LineString expandedLine2 = line2;
        // List<LineString> finalLines = new ArrayList<LineString>();
        if (vertexAlg.cutCrossingLines(expandedLine1, expandedLine2)) {
            List<LineString> cuts = vertexAlg.getCuts();
            List<LineString> finalCuts = new ArrayList<>();
            finalCuts.addAll(cuts);
            for (LineString cut : cuts) {
                finalCuts.remove(cut);
                // if (!(line1.intersects(cut) || line2.intersects(cut))) {
                // finalCuts.remove(cut);
                // }
            }
            return finalCuts;
        }
        // finalLines.add(expandedLine1);
        // finalLines.add(expandedLine2);

        return new ArrayList<>();
    }
}
