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

import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.openjump.advancedtools.CadExtension;
import org.openjump.advancedtools.gui.SimpleLineDialog;
import org.openjump.advancedtools.plugins.SimpleLinePlugIn;
import org.openjump.advancedtools.tools.cogo.DrawGeometryCommandsTool;
import org.openjump.advancedtools.utils.WorkbenchUtils;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.Logger;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;

/**
 * Clase abstracta para los comandos de creacion de lineas
 * 
 * @author Eduardo Montero Ruiz - emontero@saig.es
 * @since Kosmo 1.0
 */
public abstract class LineCommand {

    private static final I18N i18n = CadExtension.I18N;

    /**
     * Ejecuta el comando para la herramienta simpleline
     * 
     * @param sld SimpleLineDialog
     * @param plugin SimpleLinePlugIn
     * @throws LineCommandException
     */
    public void execute(SimpleLineDialog sld, SimpleLinePlugIn plugin)
            throws LineCommandException {

        WorkbenchContext context = JUMPWorkbench.getInstance().getFrame()
                .getContext();
        // Obtenemos la capa seleccionada
        Layer selectedLayer = WorkbenchUtils.getlayer();

        // Obtenemos las features seleccionadas y comprobamos que sea
        // exactamente una
        Collection<Feature> selectedFeatures = context.getLayerViewPanel()
                .getSelectionManager()
                .getFeaturesWithSelectedItems(selectedLayer);
        if (selectedFeatures.size() != 1) {
            throw new LineCommandException();
        }

        // Obtenemos la geometria de la feature seleccionada y comprobamnos que
        // sea una linea
        Feature feat = selectedFeatures.iterator().next();
        Geometry geom = feat.getGeometry();
        if (geom instanceof LineString) {
            LineString line = (LineString) geom;
            Point nuevoInicio = line.getEndPoint();
            // Establecemos los 2 puntos de la linea
            sld.setFirstPointAbsoluta(nuevoInicio.getX(), nuevoInicio.getY());
            setSecondPoint(sld);
            // Dibujamos la nueva linea
            try {
                plugin.createLine(sld);
            } catch (Exception e) {
                Logger.error("", e);
            }
        } else {
            throw new LineCommandException();
        }

    }

    protected abstract void setSecondPoint(SimpleLineDialog sld)
            throws LineCommandException;

    public abstract String getSintaxis();

    /**
     * Ejecuta el comando para la herramienta drawLineStringCommand
     * 
     * @param drawLineStringCommandsTool
     */
    public void execute(DrawGeometryCommandsTool drawLineStringCommandsTool)
            throws LineCommandException {
        List<Coordinate> list = drawLineStringCommandsTool.getCoordinates();
        if (!list.isEmpty()) {
            Coordinate newCoordinate = getSecondPointRelativeTo(list.get(list
                    .size() - 1));
            drawLineStringCommandsTool.addCoordinate(newCoordinate);
        } else {
            JUMPWorkbench
                    .getInstance()
                    .getFrame()
                    .warnUser(
                        i18n.get("org.openjump.core.ui.tools.DrawLineStringCommandsTool.you-must-introduce-at-least-one-point-of-the-line"));
        }
    }

    /**
     * Metodo que los comandos deben implementar para obtener la coordenada
     * final del nuevo segmento relativo a la coordenada proporcionada
     * 
     * @param coordinate reference coordinate
     * @return
     */
    protected abstract Coordinate getSecondPointRelativeTo(Coordinate coordinate)
            throws LineCommandException;

}
