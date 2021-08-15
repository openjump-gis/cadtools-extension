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
package org.openjump.advancedtools.deactivated;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import org.openjump.advancedtools.icon.IconLoader;
import org.openjump.advancedtools.utils.EditUtils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.cursortool.DragTool;

/**
 * Tool to rotate a set of elements
 * <p>
 * The user can select a rotation point with a first click. Than the angle of
 * rotation by dragging the shape of the item(s) on the view
 * </p>
 * 
 * @author Gabriel Bellido Perez
 * @since Kosmo 1.0.0
 * @author Giuseppe Aruta [Genuary 30th 2017] rewrite code to adapt to OpenJUMP
 *         1.10 (http://www.openjump.org/support.html)
 * @since OpenJUMP 1.10 (2017)
 */
public class RotateTool extends DragTool {

    private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");

    /** Name of the tool */
    public final static String NAME = i18n
        .get("org.openjump.core.ui.tools.RotateTool.Rotate");
    /** Description of the tool */
    public final static String DESCRIPTION = i18n
        .get("org.openjump.core.ui.tools.RotateTool.description");

    final static String angleST = I18N.JUMP
            .get("org.openjump.core.ui.plugins.edittoolbox.cursortools.angle");
    final static String degrees = I18N.JUMP
            .get("org.openjump.core.ui.plugins.edittoolbox.cursortools.degrees");

    /** Icon of the tool */
    public static final ImageIcon ICON = IconLoader.icon("rotate.png");

    /** Cursor to select a point of rotation */
    public static final Cursor DEFAULT_CURSOR = createCursor(
            IconLoader.icon("refreshAnchorCursor.png").getImage(),
            new java.awt.Point(0, 0));

    /** Cursor to get the angle of rotation */
    public static final Cursor ROTATE_CURSOR = createCursor(IconLoader.icon(
            "refreshCursor.png").getImage());

    /** Central coordinate */
    protected Coordinate centerCoord;

    /** */
    protected boolean clockwise = true;

    /** */
    protected double fullAngle = 0.0;

    protected Shape line = null;

    /** Features to rotate */
    protected List<Feature> featsToRotate;

    private final EnableCheckFactory checkFactory;

    /**
     * 
     *
     */
    public RotateTool(EnableCheckFactory checkFactory) {
        super(JUMPWorkbench.getInstance().getContext());
        this.checkFactory = checkFactory;
        allowSnapping();
    }

    public Collection<Feature> getSelectedFeatures() {
        Layer editableLayer = getSelectedLayer();
        SelectionManager selectionManager = getPanel().getSelectionManager();
        return selectionManager.getFeaturesWithSelectedItems(editableLayer);
    }

    @SuppressWarnings({})
    protected void rotateAndSave() {
        ArrayList<EditTransaction> transactions = new ArrayList<>();

        for (Layer layerWithSelectedItems :
                getPanel().getSelectionManager().getLayersWithSelectedItems()) {
            transactions.add(createTransaction(layerWithSelectedItems));
        }
        EditTransaction.commit(transactions);
    }

    private EditTransaction createTransaction(Layer layer) {
        EditTransaction transaction = EditTransaction
                .createTransactionOnSelection(
                        new EditTransaction.SelectionEditor() {
                            @Override
                            @SuppressWarnings("rawtypes")
                            public Geometry edit(
                                    Geometry geometryWithSelectedItems,
                                    Collection selectedItems) {
                                for (Iterator j = selectedItems.iterator(); j
                                        .hasNext();) {
                                    Geometry item = (Geometry) j.next();
                                    rotate(item);
                                }
                                return geometryWithSelectedItems;
                            }
                        }, getPanel(), getPanel().getContext(), getName(),
                        layer, isRollingBackInvalidEdits(), false);
        return transaction;
    }

    @Override
    protected void gestureFinished() throws java.lang.Exception {
        reportNothingToUndoYet();
        if (!check(checkFactory.createSelectedItemsLayersMustBeEditableCheck())) {
            return;
        }
        if (!check(checkFactory.createAtLeastNFeaturesMustBeSelectedCheck(1))) {
            return;
        }
        rotateAndSave();
    }

    /**
     * Rotate the geometry applying a filter
     * 
     * @param geometry
     *            Geometria que se desea rotar
     */
    protected void rotate(Geometry geometry) {
        geometry.apply(new CoordinateFilter() {
            @Override
            public void filter(Coordinate coordinate) {
                double cosAngle = Math.cos(fullAngle);
                double sinAngle = Math.sin(fullAngle);
                double x = coordinate.x - centerCoord.x;
                double y = coordinate.y - centerCoord.y;
                coordinate.x = centerCoord.x + (x * cosAngle) + (y * sinAngle);
                coordinate.y = centerCoord.y + (y * cosAngle) - (x * sinAngle);
            }
        });
        geometry.geometryChanged();
    }

    @Override
    public Cursor getCursor() {
        return DEFAULT_CURSOR;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        String tooltip = "";
        tooltip = "<HTML><BODY>";
        tooltip += "<DIV style=\"width: 300px; text-justification: justify;\">";
        tooltip += "<b>" + NAME + "</b>" + "<br>";
        tooltip += DESCRIPTION + "<br>";
        tooltip += "</DIV></BODY></HTML>";
        return tooltip;
    }

    /**
     * Create the Shape of selected items. If the number is >100 it limits the
     * shape to 100 elements
     * 
     * @return Shape -
     * @throws NoninvertibleTransformException
     */
    protected Shape createSelectedItemsShape()
            throws NoninvertibleTransformException {
        List<Geometry> selectedGeos;
        selectedGeos = new ArrayList<>(getPanel().getSelectionManager()
                .getSelectedItems());
        if (selectedGeos.size() == 0) {
            return null;
        } else if (selectedGeos.size() > 100) {
            Collections.shuffle(selectedGeos);
            selectedGeos = selectedGeos.subList(0, 99);
        }

        Geometry[] allGeoms = new Geometry[selectedGeos.size()];
        int i = 0;
        for (Geometry geom : selectedGeos) {
            allGeoms[i++] = geom;
        }
        Geometry geo = geomFac.createGeometryCollection(allGeoms);
        if (centerCoord == null) {
            centerCoord = geo.getCentroid().getCoordinate();
        }
        return getPanel().getJava2DConverter().toShape(geo);
    }

    /**
     * Get the shape depending of initial and final coordinates
     * 
     * @param source
     * @param destination
     * @return Shape
     */
    @Override
    protected Shape getShape(Point2D source, Point2D destination)
            throws Exception {
        GeneralPath path = new GeneralPath();
        Coordinate end = getPanel().getViewport()
                .toModelCoordinate(destination);
        source = getPanel().getViewport().toViewPoint(snap(source));
        Shape sh = createSelectedItemsShape();
        AffineTransform af = new AffineTransform();
        fullAngle = EditUtils.getAngle(source.getX(), source.getY(),
                destination.getX(), destination.getY());
        af.rotate(fullAngle, source.getX(), source.getY());
        sh = af.createTransformedShape(sh);
        centerCoord = getPanel().getViewport().toModelCoordinate(source);// getClickedPoint().getCoordinate();
        line = new Rectangle.Double(source.getX() - 3, source.getY() - 3, 6, 6);
        path.moveTo(centerCoord.x, centerCoord.y);
        path.moveTo(end.x, end.y);
        path.append(sh, false);
        DecimalFormat df2 = new DecimalFormat("##0.0#");
        getPanel().getContext().setStatusMessage(
                angleST + ": " + df2.format(Math.toDegrees(fullAngle)) + " "
                        + degrees);
        return sh;
    }

    /**
     * Calculate the click point
     * 
     * @return Point
     */
    public Point getClickedPoint() {
        Coordinate c = snap(getModelSource());
        return geomFac.createPoint(c);
    }

    GeometryFactory geomFac = new GeometryFactory();

    /**
     * 
     */
    @Override
    protected void drawShapeXOR(Graphics2D g) throws Exception {
        super.drawShapeXOR(g);
        setFilling(true);
        drawShapeXOR(line, g);
        setFilling(false);
    }

    /**
     * Begins handling of the drag. Subclasses can prevent handling of the drag
     * by overriding this method and not calling it.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        try {
            if (!check(checkFactory
                    .createSelectedItemsLayersMustBeEditableCheck())) {
                return;
            }
            super.mousePressed(e);
        } finally {
            // Cambiamos el cursor
            getPanel().setCursor(ROTATE_CURSOR);
        }
    }

    /**
     * Begins handling of the drag. Subclasses can prevent handling of the drag
     * by overriding this method and not calling it.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        try {
            super.mouseReleased(e);
        } finally {
            // Cambiamos el cursor
            getPanel().setCursor(DEFAULT_CURSOR);
        }

    }

    /**
     * Get the layer
     * 
     * @return Layer
     */
    public Layer getSelectedLayer() {
        Collection<Layer> editableLayers = getPanel().getLayerManager()
                .getEditableLayers();

        if (editableLayers.isEmpty()) {
            return null;
        }
        return editableLayers.iterator().next();
    }
}