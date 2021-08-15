package org.openjump.advancedtools.tools;

import java.awt.Cursor;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.Logger;
import org.openjump.advancedtools.icon.IconLoader;
import org.openjump.advancedtools.utils.EditUtils;
import org.openjump.advancedtools.utils.WorkbenchUtils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.cursortool.DragTool;

/**
 * Tools that allows to generate a copy of selected features by dragging them.
 * Original code from Kosmo 3.0 SAIG - http://www.opengis.es/
 * 
 * @author Gabriel Bellido Perez - gbp@saig.es
 * @since Kosmo SAIG 1.2
 * @author Giuseppe Aruta [Genuary 30th 2017] rewrite code to adapt to OpenJUMP
 *         1.10 (http://www.openjump.org/support.html)
 * @since OpenJUMP 1.10 (2017)
 */

public class CopyDraggingTool extends DragTool {

    private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");

    //private static final Logger LOGGER = Logger
    //        .getLogger(CopyDraggingTool.class);

    /** Name of the tool */
    private static final String NAME = i18n
        .get("org.openjump.core.ui.tools.Clone");
    /** description of the tool */
    public final static String DESCRIPTION = i18n
        .get("org.openjump.core.ui.tools.Clone.description");
    /** Icon of the tool */
    private static final ImageIcon ICON = GUIUtil.resize(
            IconLoader.icon("cloneFeatures.png"), 20);
    /** Cursor of the tool */
    public static final Cursor CURSOR = new Cursor(Cursor.MOVE_CURSOR);

    protected Shape selectedFeatureShape;
    //protected SnapIndicatorTool snapIndicatorTool;
    protected List<Coordinate> verticesToSnap = null;
    EnableCheckFactory checkFactory =
        JUMPWorkbench.getInstance().getContext().createPlugInContext().getCheckFactory();

    public CopyDraggingTool(EnableCheckFactory checkFactory) {
        super(JUMPWorkbench.getInstance().getContext());
        allowSnapping();
    }

    @Override
    public String getName() {
        String tooltip;
        tooltip = "<HTML><BODY>";
        tooltip += "<DIV style=\"width: 300px; text-justification: justify;\">";
        tooltip += "<b>" + NAME + "</b>" + "<br>";
        tooltip += DESCRIPTION + "<br>";
        tooltip += "</DIV></BODY></HTML>";
        return tooltip;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public Cursor getCursor() {
        return CURSOR;
    }

    @Override
    protected void gestureFinished() throws java.lang.Exception {
        reportNothingToUndoYet();

        Collection<Layer> layers = getPanel().getSelectionManager()
                .getLayersWithSelectedItems();

        for (Layer layerWithSelectedItems : layers) {
            duplicate(layerWithSelectedItems);
        }
    }

    private void moveFeatures(Collection<Feature> featureCopies,
            Coordinate displacement) {
        for (Feature item : featureCopies) {
            Geometry espejo = item.getGeometry();
            move(espejo, displacement);
            espejo.geometryChanged();
        }
    }

    private void move(Geometry geometry, final Coordinate displacement) {
        geometry.apply(new CoordinateFilter() {
            @Override
            public void filter(Coordinate coordinate) {
                coordinate.x += displacement.x;
                coordinate.y += displacement.y;
            }
        });
    }

    protected Coordinate displacement() {
        Coordinate source = getModelSource();
        Coordinate destination = getModelDestination();
        return new Coordinate(destination.x - source.x,
                destination.y - source.y);

    }

    protected void duplicate(Layer editableLayer) {
        if (editableLayer == null)
            return;
        final Collection<Feature> selectedFeatures = WorkbenchUtils
                .getSelectedFeatures(editableLayer);

        final Collection<Feature> featureCopies = EditUtils.conformCollection(
                selectedFeatures, editableLayer.getFeatureCollectionWrapper()
                        .getFeatureSchema());

        moveFeatures(featureCopies, displacement());

        try {

            WorkbenchUtils.executeUndoableAddNewFeatsLeaveSelectedFeats(NAME,
                    getPanel().getSelectionManager(), editableLayer,
                    featureCopies, selectedFeatures);
        } catch (Exception e) {
            Logger.error("", e);
        }

        this.deactivate();
    }

    GeometryFactory geomFac = new GeometryFactory();

    protected Shape createSelectedItemsShape()
            throws NoninvertibleTransformException {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        List<Geometry> itemsToRender = new ArrayList(getPanel()
                .getSelectionManager().getSelectedItems());
        if (itemsToRender.size() > 100) {
            Collections.shuffle(itemsToRender);
            itemsToRender = itemsToRender.subList(0, 99);
        }
        GeometryCollection gc = geomFac.createGeometryCollection(itemsToRender
                .toArray(new Geometry[0]));

        return getPanel().getJava2DConverter().toShape(gc);
    }

    @Override
    protected Shape getShape() throws Exception {
        AffineTransform transform = new AffineTransform();
        transform.translate(getViewDestination().getX()
                - getViewSource().getX(), getViewDestination().getY()
                - getViewSource().getY());

        return transform.createTransformedShape(this.selectedFeatureShape);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!check(checkFactory.createTaskWindowMustBeActiveCheck())) {
            return;
        }
        if (!check(checkFactory.createWindowWithLayerManagerMustBeActiveCheck())) {
            return;
        }
        if (!check(checkFactory.createSelectedItemsLayersMustBeEditableCheck())) {
            return;
        }
        if (!check(checkFactory.createSelectedItemsLayersMustBeEditableCheck())) {
            return;
        }
        if (!check(checkFactory.createAtLeastNFeaturesMustBeSelectedCheck(1))) {
            return;
        }
        try {
            this.verticesToSnap = null;
            this.selectedFeatureShape = createSelectedItemsShape();
        } catch (NoninvertibleTransformException e1) {
            Logger.warn(e1);
        }
        super.mousePressed(e);
    }

}
