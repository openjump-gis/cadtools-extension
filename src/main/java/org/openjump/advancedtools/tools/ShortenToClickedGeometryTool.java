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
package org.openjump.advancedtools.tools;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jump.workbench.Logger;
import org.openjump.advancedtools.gui.SelectLayerDialog;
import org.openjump.advancedtools.utils.EditUtils;
import org.openjump.advancedtools.utils.WorkbenchUtils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.cursortool.Animations;

/**
 * Herramienta que permite acortar el elemento seleccionado hasta la geometria
 * que se pulsa
 * <p>
 * </p>
 * 
 * @author Gabriel Bellido Perez
 * @since Kosmo 1.0.0
 */
public class ShortenToClickedGeometryTool extends ShortenLineTool {

    /**
     *
     */
    public ShortenToClickedGeometryTool() {
        super(1);
        self_intersection_active = false;
    }

    /**
     *
     */
    protected Map<Feature,Layer> getCandidates(Geometry g, Feature f) {

        HashMap<Feature,Layer> s = new HashMap<>();

        Layer selectedLayer = WorkbenchUtils.getSelectedLayer();
        if (selectedLayer == null)
            return s;
        Coordinate click = (Coordinate) getCoordinates().get(0);
        Feature feat = null;
        try {
            feat = EditUtils.getSelectedGeom(click, selectedLayer, getPanel()
                    .getViewport().getScale());
        } catch (Exception e) {
            Logger.warn(e);
        }
        if (feat == null)
            return s;
        s.put(feat, selectedLayer);
        // broke_geom = false;
        return s;
    }

    /**
     *
     */
    protected Map<Feature,Layer> getCandidates1(Geometry g, Feature f) {

        HashMap<Feature,Layer> s = new HashMap<>();

        Layer[] layers = getVisibleLayers();
        SelectLayerDialog sld = new SelectLayerDialog(JUMPWorkbench
                .getInstance().getFrame(), layers);
        sld.setVisible(true);
        Layer selectedLayer = sld.getLayer();
        if (selectedLayer == null)
            return s;
        Coordinate click = (Coordinate) getCoordinates().get(0);
        Feature feat = null;
        try {
            feat = EditUtils.getSelectedGeom(click, selectedLayer, getPanel()
                    .getViewport().getScale());
        } catch (Exception e) {
            Logger.warn(e);
        }
        if (feat == null)
            return s;
        s.put(feat, selectedLayer);
        // broke_geom = false;
        return s;
    }

    /**
     *
     */
    protected Layer[] getVisibleLayers() {
        Collection<Layer> editableLayers = JUMPWorkbench.getInstance().getFrame()
                .getContext().getLayerManager().getVisibleLayers(false);
        Layer[] layers = editableLayers.toArray(new Layer[0]);
        if (layers.length == 0)
            return new Layer[0];
        return layers;
    }

    /**
     * 
     */
    protected Shape getShape() throws NoninvertibleTransformException {
        if (getCoordinates().size() == 0)
            return null;

        if (getCoordinates().size() == 1) {
            Coordinate c = (Coordinate) getCoordinates().get(0);
            Point2D p = JUMPWorkbench.getInstance().getFrame().getContext()
                    .getLayerViewPanel().getViewport().toViewPoint(c);
            return new Ellipse2D.Double(p.getX() - 3, p.getY() - 3, 6, 6);
        }
        return null;
    }

    /**
     * 
     */
    public Point getClickedPoint() {
        return geomFac.createPoint((Coordinate) getCoordinates().get(0));
    }

    /**
     * 
     */
    protected void gestureFinished() throws Exception {
        List<Point2D> centers = new ArrayList<>();
        centers.add(getPanel().getViewport().toViewPoint(
                getClickedPoint().getCoordinate()));
        Animations.drawExpandingRings(centers, true, Color.BLUE, getPanel(),
                new float[] { 5, 5 });
        super.gestureFinished();
    }
}
