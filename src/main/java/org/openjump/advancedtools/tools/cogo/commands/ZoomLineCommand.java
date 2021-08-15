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

import java.awt.geom.NoninvertibleTransformException;
import java.util.Collection;
import java.util.List;

import com.vividsolutions.jump.I18N;
import org.openjump.advancedtools.gui.SimpleLineDialog;
import org.openjump.advancedtools.tools.cogo.DrawGeometryCommandsTool;
import org.openjump.advancedtools.utils.WorkbenchUtils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.util.CoordinateArrays;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;

/**
 * TODO Purpose of
 * <p>
 * </p>
 * 
 * @author Giuseppe Aruta
 * @since OpenJUMP 1.10
 */
public class ZoomLineCommand extends LineCommand {

    private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");

    /** Nombre del comando */
    private final static String COMMAND_NAME = "zoom";
    /** Sintaxis del comando */
    private final static String SINTAXIS = "zoom";

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
        return i18n.get("org.openjump.core.ui.tools.DrawLineStringCommandsTool.ZoomLineCommand.description");
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
        //WorkbenchContext context = JUMPWorkbench.getInstance().getFrame()
        //        .getContext();
        try {
            List coordinates = drawLineStringCommandsTool.getCoordinates();
            Coordinate[] coord = toArray(coordinates);
            WorkbenchUtils.zoomToCoordinates(coord);
        } catch (NoninvertibleTransformException e) {
            // TODO Auto-generated catch block
            JUMPWorkbench
                    .getInstance()
                    .getFrame()
                    .warnUser(
                            i18n.get("org.openjump.core.ui.tools.DrawLineStringCommandsTool.EndLineCommand.check"));
        }

    }

    protected Coordinate[] toArray(List coordinates) {
        return (Coordinate[]) coordinates.toArray(new Coordinate[] {});
    }

    private Envelope toEnvelope(Coordinate coordinate, LayerManager layerManager) {
        int segments = 0;
        int segmentSum = 0;
        outer: for (Layer layer : layerManager.getLayers()) {
            for (Feature feature : layer.getFeatureCollectionWrapper().getFeatures()) {
                Collection<Coordinate[]> coordinateArrays = CoordinateArrays
                        .toCoordinateArrays(feature.getGeometry(), false);
                for (Coordinate[] coordinates : coordinateArrays) {
                    for (int a = 1; a < coordinates.length; a++) {
                        segments++;
                        segmentSum += coordinates[a]
                                .distance(coordinates[a - 1]);
                        if (segments > 100) {
                            break outer;
                        }
                    }
                }
            }
        }
        Envelope envelope = new Envelope(coordinate);
        // Choose a reasonable magnification [Jon Aquino 10/22/2003]
        if (segmentSum > 0) {
            envelope = EnvelopeUtil.expand(envelope, segmentSum
                    / (double) segments);
        } else {
            envelope = EnvelopeUtil.expand(envelope, 50);
        }
        return envelope;
    }

}
