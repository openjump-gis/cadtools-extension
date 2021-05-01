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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import org.openjump.advancedtools.utils.CADEnableCheckFactory;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.cursortool.CoordinateListMetrics;

/**
 * Tool to extend a selected linestring to a drawn segment
 *
 * @author Gabriel Bellido Perez
 * @since Kosmo SAIG 1.0.0 (2006)
 * @author Giuseppe Aruta [Genuary 30th 2017] rewrite code to adapt to OpenJUMP
 *         1.10 (http://www.openjump.org/support.html)
 * @since OpenJUMP 1.10 (2017)
 */
public class ExtendToDrawnLineTool extends ExtendRectLineTool {

    /** */
    protected Coordinate coordinateA;

    /** */
    protected Coordinate coordinateB;

    /**
     * 
     *
     */
    public ExtendToDrawnLineTool() {
        super(2);
        setColor(Color.GREEN.darker());
        setStroke(new BasicStroke(1.5f, // Width
                BasicStroke.CAP_SQUARE, // End cap
                BasicStroke.JOIN_ROUND, // Join style
                10.0f, // Miter limit
                new float[] { 10.0f, 5.0f }, // Dash pattern
                0.0f));
        allowSnapping();
        setMetricsDisplay(new CoordinateListMetrics());
        self_intersection_active = false;
    }

    @Override
    protected Shape getShape() throws NoninvertibleTransformException {
        if (getCoordinates().size() == 0)
            return null;

        coordinateA = (Coordinate) getCoordinates().get(0);
        if (getCoordinates().size() == 1)
            coordinateB = tentativeCoordinate;
        if (getCoordinates().size() == 2)
            coordinateB = (Coordinate) getCoordinates().get(1);
        if (coordinateB == null)
            return null;
        Point2D a, b;
        a = getPanel().getViewport().toViewPoint(coordinateA);
        b = getPanel().getViewport().toViewPoint(coordinateB);
        return new Line2D.Double(a, b);

    }

    /**
     * 
     */
    @Override
    protected void mouseLocationChanged(MouseEvent e) {
        try {
            if (getCoordinates().isEmpty()) {
                return;
            } // Check only for LineString. Excluding other geometry types
            if (!check(CADEnableCheckFactory
                    .createGeometryTypeOnSelectedFeaturesCheck(
                            new int[] { CADEnableCheckFactory.FEATURE_SCHEMA_LINESTRING },

                            new int[] {

                                    CADEnableCheckFactory.FEATURE_SCHEMA_GEOMETRYCOLLECTION,
                                    CADEnableCheckFactory.FEATURE_SCHEMA_MULTILINESTRING,
                                    CADEnableCheckFactory.FEATURE_SCHEMA_MULTIPOINT,
                                    CADEnableCheckFactory.FEATURE_SCHEMA_MULTIPOLYGON,
                                    CADEnableCheckFactory.FEATURE_SCHEMA_POINT,
                                    CADEnableCheckFactory.FEATURE_SCHEMA_POLYGON,

                                    CADEnableCheckFactory.FEATURE_SCHEMA_LINEARRING }))) {

                return;
            }
            tentativeCoordinate = snap(e.getPoint());
            redrawShape();
        } catch (Throwable t) {
            getPanel().getContext().handleThrowable(t);
        }
    }

    /**
     * 
     */
    @Override
    protected Map<Feature,Layer> getCandidates(Coordinate c, double r, Feature f) {
        HashMap<Feature,Layer> s = new HashMap<>();
        LineString ls = geomFac.createLineString(new Coordinate[] {
                coordinateA, coordinateB });
        Feature feat = FeatureUtil.toFeature(ls, f.getSchema());

        Layer editableLayer = getLayer();

        s.put(feat, editableLayer);
        broke_geom = false;
        return s;
    }

    /**
     * 
     */
    @Override
    protected Coordinate getStartPoint() {
        return (Coordinate) getCoordinates().get(1);
    }
}
