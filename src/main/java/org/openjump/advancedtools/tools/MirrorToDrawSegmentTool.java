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

import java.awt.Cursor;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.openjump.advancedtools.CadExtension;
import org.openjump.advancedtools.utils.EditUtils;
import org.openjump.advancedtools.utils.WorkbenchUtils;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.Logger;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DragTool;

/**
 * Tool to create a symmetric axial image of selected geometries using a drawn
 * segment as symmetry axes
 *
 * @author Gabriel Bellido Perez
 * @since Kosmo 1.0.0
 * @author Giuseppe Aruta [Genuary 30th 2017] rewrite code to adapt to OpenJUMP
 *         1.10 (http://www.openjump.org/support.html)
 * @since OpenJUMP 1.10 (2017)
 */
public class MirrorToDrawSegmentTool extends DragTool {

    private static final I18N i18n = CadExtension.I18N;

    /** Name of the tool */
    public final static String NAME = i18n
        .get("org.openjump.core.ui.tools.Mirror");

    /** Cursor of the tool */
    public static final Cursor CURSOR = new Cursor(Cursor.CROSSHAIR_CURSOR);

    /** */
    protected Coordinate coordinateA;

    /** */
    protected Coordinate coordinateB;

    ///** Indicador de snap */
    //protected SnapIndicatorTool snapIndicatorTool;

    /** Lista de elementos a a�adir */
    protected List<Feature> featsToAdd;

    //protected Collection<Feature> GeometryCopies;

    EnableCheckFactory checkFactory =
        JUMPWorkbench.getInstance().getContext().createPlugInContext().getCheckFactory();

    /**
	 * 
	 *
	 */
    public MirrorToDrawSegmentTool() {
        super(JUMPWorkbench.getInstance().getContext());
        allowSnapping();
    }

    /**
     * Giving two coordinates
     * 
     */
    private void calculateMirrorFeatures(Collection<Feature> featureCopies,
            Coordinate coordinateA, Coordinate coordinateB) {
        for (Feature item : featureCopies) {
            Geometry espejo = item.getGeometry();
            rotate(espejo, rotationAngle(), coordinateA.x, coordinateA.y);
            translate(espejo, -coordinateA.x, -coordinateA.y);
            mirrorY(espejo);
            translate(espejo, coordinateA.x, coordinateA.y);
            rotate(espejo, -rotationAngle(), coordinateA.x, coordinateA.y);
            espejo.geometryChanged();
        }
    }

    private void calculateMirrorGeometries(Collection<Geometry> geometries,
            Coordinate coordinateA, Coordinate coordinateB) {
        for (Geometry espejo : geometries) {
            rotate(espejo, rotationAngle(), coordinateA.x, coordinateA.y);
            translate(espejo, -coordinateA.x, -coordinateA.y);
            mirrorY(espejo);
            translate(espejo, coordinateA.x, coordinateA.y);
            rotate(espejo, -rotationAngle(), coordinateA.x, coordinateA.y);
            espejo.geometryChanged();
        }
    }

    protected void mirrorAndSave(Layer editableLayer) {
        if (editableLayer == null)
            return;

        final Collection<Feature> selectedFeatures = WorkbenchUtils
                .getSelectedFeatures(editableLayer);

        final Collection<Feature> featureCopies = EditUtils.conformCollection(
                selectedFeatures, editableLayer.getFeatureCollectionWrapper()
                        .getFeatureSchema());
        calculateMirrorFeatures(featureCopies, coordinateA, coordinateB);

        try {

            WorkbenchUtils.executeUndoableAddNewFeatsLeaveSelectedFeats(NAME,
                    getPanel().getSelectionManager(), editableLayer,
                    featureCopies, selectedFeatures);

        } catch (Exception e) {
            Logger.warn(e);
            this.deactivate();
        }
    }

    /**
	 * 
	 */
    @Override
    protected void gestureFinished() throws java.lang.Exception {
        reportNothingToUndoYet();
        if (!check(checkFactory.createSelectedItemsLayersMustBeEditableCheck())) {
            return;
        }
        if (!check(checkFactory.createAtLeastNFeaturesMustBeSelectedCheck(1))) {
            return;
        }
        if (!check(checkFactory.createAtLeastNLayersMustBeEditableCheck(1))) {
            return;
        }
       
        for (Layer layerWithSelectedItems :
                getPanel().getSelectionManager().getLayersWithSelectedItems()) {
            mirrorAndSave(layerWithSelectedItems);
        }
    }

    /**
     * 
     * @param geometry
     * @param ang
     * @param xrp
     * @param yrp
     */
    protected void rotate(Geometry geometry, double ang, double xrp, double yrp) {
        final double angle = ang;
        final double xr = xrp;
        final double yr = yrp;
        geometry.apply(new CoordinateFilter() {
            @Override
            public void filter(Coordinate coordinate) {
                double cosAngle = Math.cos(angle);
                double sinAngle = Math.sin(angle);
                double x = coordinate.x - xr;
                double y = coordinate.y - yr;
                coordinate.x = xr + (x * cosAngle) + (y * sinAngle);
                coordinate.y = yr + (y * cosAngle) - (x * sinAngle);
            }
        });
    }

    /**
     * 
     * @param geometry
     * @param xrp
     * @param yrp
     */
    protected void translate(Geometry geometry, double xrp, double yrp) {
        final double xr = xrp;
        final double yr = yrp;
        geometry.apply(new CoordinateFilter() {
            @Override
            public void filter(Coordinate coordinate) {
                double x = coordinate.x + xr;
                double y = coordinate.y + yr;
                coordinate.x = x;
                coordinate.y = y;
            }
        });
    }

    /**
     * 
     * @param geometry
     */
    protected void mirrorY(Geometry geometry) {
        geometry.apply(new CoordinateFilter() {
            @Override
            public void filter(Coordinate coordinate) {
                double x = -coordinate.x;
                double y = coordinate.y;
                coordinate.x = x;
                coordinate.y = y;
            }
        });
    }

    @Override
    public Cursor getCursor() {
        return CURSOR;
    }

    @Override
    public Icon getIcon() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Calcula el angulo de rotacion que se desea aplicar
     * 
     * @return the rotation angle to apply
     */
    public double rotationAngle() {
        return EditUtils.getAngle(coordinateA.x, coordinateA.y, coordinateB.x,
                coordinateB.y);
    }

    /**
	   *
	   */
    @Override
    protected Shape getShape(Point2D source, Point2D destination)
            throws Exception {

        source = getPanel().getViewport().toViewPoint(snap(source));
        coordinateA = new Coordinate(getPanel().getViewport()
                .toModelCoordinate(source));
        coordinateB = new Coordinate(getPanel().getViewport()
                .toModelCoordinate(destination));

        return new Line2D.Double(source.getX(), source.getY(),
                destination.getX(), destination.getY());
    }

    /**
     * Obtiene el punto pulsado por el usuario
     * 
     * @return Point
     */
    public Point getClickedPoint() {
        double x = getModelSource().x;
        double y = getModelSource().y;

        return geomFac.createPoint(new Coordinate(x, y));
    }

    GeometryFactory geomFac = new GeometryFactory();

    /**
     *
     */
    public static MultiEnableCheck createEnableCheck(
            final WorkbenchContext workbenchContext, AbstractCursorTool tool) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory =
            workbenchContext.createPlugInContext().getCheckFactory();

        // al menos una capa debe tener elementos activos
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck())
                .add(checkFactory
                        .createWindowWithLayerManagerMustBeActiveCheck())
                .add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
        solucion.add(checkFactory
                .createSelectedItemsLayersMustBeEditableCheck());
        solucion.add(checkFactory.createAtLeastNFeaturesMustBeSelectedCheck(1));
        return solucion;
    }

}
