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
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.Logger;
import org.openjump.advancedtools.icon.IconLoader;
import org.openjump.advancedtools.utils.CADEnableCheckFactory;
import org.openjump.advancedtools.utils.EditUtils;
import org.openjump.advancedtools.utils.WorkbenchUtils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.operation.distance.DistanceOp;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.GeometryEditor;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.cursortool.Animations;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.NClickTool;
import com.vividsolutions.jump.workbench.ui.cursortool.SelectFeaturesTool;

/**
 * Removes the section of the selected line that is included between the to
 * points clicked by the user.
 * 
 * @author Francisco Abato Helguera - fabato@saig.es
 * @since 2.2
 */
public class RemoveSectionInLineTool extends NClickTool {

    private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");

    public final static String NAME = i18n
        .get("org.openjump.core.ui.tools.RemoveSectionInLine.remove-section-in-line"); //$NON-NLS-1$

    public final static String DESCRIPTION = i18n
        .get("org.openjump.core.ui.tools.RemoveSectionInLine.description");

    public final static Icon ICON = IconLoader.icon("removeSectionInLine1.png");//$NON-NLS-1$

    public final static Cursor CURSOR = createCursor(
            (IconLoader.icon("removeSectionInLineCursor1.png")).getImage(), //$NON-NLS-1$
            new java.awt.Point(0, 0));

    protected EnableCheckFactory checkFactory;

    protected final static int TOLERANCE_PX = 4;

    protected WorkbenchContext wContext;

    //protected SnapIndicatorTool snapIndicatorTool;

    GeometryFactory geomFac = new GeometryFactory();

    public RemoveSectionInLineTool(EnableCheckFactory checkFactory) {
        super(JUMPWorkbench.getInstance().getContext(), 2);
        this.checkFactory = checkFactory;
        allowSnapping();
        this.wContext = JUMPWorkbench.getInstance().getFrame().getContext();
    }

    protected Point getModelClickPoint() {
        return new GeometryFactory().createPoint(getModelDestination());
    }

    @Override
    protected void gestureFinished() throws Exception {
        reportNothingToUndoYet();
        if (!check(checkFactory.createTaskWindowMustBeActiveCheck())) {
            return;
        }

        if (!check(checkFactory.createWindowWithLayerManagerMustBeActiveCheck())) {
            return;
        }
        if (!check(checkFactory.createSelectedItemsLayersMustBeEditableCheck())) {
            return;
        }
        // Check only for LineString and MultiLineString. Excluding other
        // geometry types
        if (!WorkbenchUtils
                .check(CADEnableCheckFactory
                        .createExactlyNFeaturesWithGeometryTypeMustBeSelectedCheck(
                                new int[] { CADEnableCheckFactory.FEATURE_SCHEMA_LINESTRING },
                                new int[] {
                                        CADEnableCheckFactory.FEATURE_SCHEMA_GEOMETRYCOLLECTION,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_MULTILINESTRING,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_MULTIPOINT,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_MULTIPOLYGON,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_POINT,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_POLYGON,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_LINEARRING },
                                1))) {
            return;
        }

        final SelectionManager selectionManager = getPanel()
                .getSelectionManager();
        final Layer editableLayer = getLayer();

        final Feature featOrig = selectionManager
                .getFeaturesWithSelectedItems(editableLayer).iterator().next();
        // the checks ensure that there is exactly 1 item selected in editable
        // layer

        Feature featEdited = featOrig.clone(true);
        //LineString lineString = (LineString) featOrig.getGeometry();
        Point[] points = getClickedPoints();

        Geometry geomSelected = featEdited.getGeometry();
        LineString clickedLine = getClickedLine(geomSelected);

        /*
         * if (CollectionUtil.list(lineString.getStartPoint().getCoordinate(),
         * lineString.getEndPoint().getCoordinate()).contains(
         * DistanceOp.closestPoints(lineString, getModelClickPoint())[0])) {
         * getWorkbench().getFrame().warnUser( CHECKLINESTRING); return; }
         */

        if (clickedLine == null) {
            String msg = i18n
                .get("org.openjump.core.ui.tools.RemoveSectionInLine.must-do-click-on-selected-line"); //$NON-NLS-1$
            JUMPWorkbench.getInstance().getFrame().warnUser(msg);
            return;
        }

        // geometries stores all the lines except the one that is been modified
        Set<LineString> geometries = new HashSet<>();
        int numGeometries = geomSelected.getNumGeometries();
        for (int i = 0; i < numGeometries; i++) {
            LineString geometryN = (LineString) geomSelected.getGeometryN(i);

            if (geometryN != clickedLine) {
                geometries.add(geometryN);
            }
        }

        List<LineString> newGeometries = removeSection(clickedLine, points[0],
                points[1]);
        if (newGeometries != null) {
            // add the new lines result to the geometries
            geometries.addAll(newGeometries);
        }

        if (geometries.isEmpty()) {
            String msg = i18n
                .get("org.openjump.core.ui.tools.RemoveSectionInLine.in-order-to-remove-the-selected-element-use-the-remove-tool"); //$NON-NLS-1$
            JUMPWorkbench.getInstance().getFrame().warnUser(msg);
            return;
        }

        MultiLineString mls = geomFac.createMultiLineString(geometries
                .toArray(new LineString[0]));

        featEdited.setGeometry(mls);

        // if (!checkConditions()) {
        // return;
        // }

        gestureFinished(editableLayer, featOrig, featEdited, selectionManager);

    }

    /**
     * @param point1
     *            first clicked point
     * @param point2
     *            second clicked point
     */
    private List<LineString> removeSection(LineString line, Point point1,
            Point point2) {

        // compute the nearest point in the line to the clicked points
        Coordinate coord1InLine = DistanceOp.nearestPoints(line, point1)[0];
        Coordinate coord2InLine = DistanceOp.nearestPoints(line, point2)[0];

        // insert vertices in line (if not already exists vertices in
        // coord1InLine and coord2InLine)
        LineSegment closestLineSegment = EditUtils.segmentInRange(line,
                coord1InLine);
        LineString newGeom = null;
        if (!closestLineSegment.p0.equals(coord1InLine)
                && !closestLineSegment.p1.equals(coord1InLine)) {
            newGeom = (LineString) new GeometryEditor().insertVertex(line,
                    closestLineSegment.p0, closestLineSegment.p1, coord1InLine);
        }

        if (newGeom == null) {
            newGeom = line;
        }

        closestLineSegment = EditUtils.segmentInRange(newGeom, coord2InLine);
        if (!closestLineSegment.p0.equals(coord2InLine)
                && !closestLineSegment.p1.equals(coord2InLine)) {
            newGeom = (LineString) new GeometryEditor().insertVertex(newGeom,
                    closestLineSegment.p0, closestLineSegment.p1, coord2InLine);
        }

        ArrayList<LineString> list = new ArrayList<>();

        // remove the section of line delimited by point1 and point2
        List<Coordinate> coords = new ArrayList<>();
        int i = 0;
        while (i < newGeom.getNumPoints()) {
            Point pointN = newGeom.getPointN(i);
            Coordinate coordN = pointN.getCoordinate();

            if (coordN.equals2D(coord1InLine) || coordN.equals2D(coord2InLine)) {
                coords.add(coordN);
                break;
            }

            coords.add(coordN);
            i++;
        }

        if (i >= newGeom.getNumPoints()) {
            // all the coordinates of the line were inspected and none
            // coincided with point1 nor point2. This is an error that
            // never should occur.
            return null;
        }

        if (coords.size() > 1) {
            LineString ls = geomFac.createLineString(coords
                    .toArray(new Coordinate[0]));
            list.add(ls);
        }

        // now skip the coordinates between coord1InLine and coord2InLine
        while (i < newGeom.getNumPoints() - 1) { // -1 because i'm going to
                                                 // increment i before using it
            // its known (by the previous loop) that the i-th coordinate
            // coincides with point1 or point2, so skip it.
            i++;

            Point pointN = newGeom.getPointN(i);
            Coordinate coordN = pointN.getCoordinate();

            if (coordN.equals2D(coord1InLine) || coordN.equals2D(coord2InLine)) {
                break;
            }
        }

        // and now the rest of the line
        coords = new ArrayList<>();
        while (i < newGeom.getNumPoints()) {
            Point point = newGeom.getPointN(i);

            coords.add(point.getCoordinate());
            i++;
        }

        if (coords.size() > 1) {
            LineString ls = geomFac.createLineString(coords
                    .toArray(new Coordinate[0]));
            list.add(ls);
        }

        return list;
    }

    /**
     * @return the linestring in wich the user did the two clicks. Returns null
     *         if the two clicks were not over the same linestring (in case of a
     *         multilinestring and if the user did one click over one single
     *         linestring and the other click over another single linestring, or
     *         if the user clicked outside of geom).
     */
    private LineString getClickedLine(Geometry geom) {
        Point[] points = getClickedPoints();
        int numGeometries = geom.getNumGeometries();

        for (int i = 0; i < numGeometries; i++) {
            LineString geometryN = (LineString) geom.getGeometryN(i);
            double tolerance = WorkbenchUtils
                    .viewportToMapDistance(TOLERANCE_PX); // tolerance
                                                          // in
                                                          // model
                                                          // units

            Geometry buffer = geometryN.buffer(tolerance);

            if (buffer.covers(points[0]) && buffer.covers(points[1])) {
                return geometryN;
            }
        }

        return null;
    }

    /**
     * @return an array containing the two points clicked by the user.
     */
    private Point[] getClickedPoints() {
        List<Coordinate> coordinates = getCoordinates();

        Point point1 = geomFac.createPoint(coordinates.get(0));
        Point point2 = geomFac.createPoint(coordinates.get(1));

        return new Point[] { point1, point2 };
    }

    /**
     * Accion que se realiza al terminar el usuario la herramienta
     * 
     * @param featEdited
     * 
     * @throws java.lang.Exception
     */
    protected void gestureFinished(final Layer affectedLayer,
            final Feature featOrig, Feature featEdited,
            final SelectionManager selectionManager) throws Exception {
        final List<Feature> updatedFeatures = new ArrayList<>();
        updatedFeatures.add(featEdited);
        final List<Feature> origFeatures = new ArrayList<>();
        origFeatures.add(featOrig);
        try {
            WorkbenchUtils.executeUndoableAddNewFeatsRemoveSelectedFeats(NAME,
                    getPanel().getSelectionManager(), affectedLayer,
                    updatedFeatures, origFeatures);
        } catch (Exception e) {
            Logger.warn(e);
        }

    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        String tooltip;
        tooltip = "<HTML><BODY>";
        tooltip += "<DIV style=\"width: 200px; text-justification: justify;\">";
        tooltip += "<b>" + NAME + "</b>" + "<br>";
        tooltip += DESCRIPTION + "<br>";
        tooltip += "</DIV></BODY></HTML>";
        return tooltip;
    }

    @Override
    public Cursor getCursor() {
        return CURSOR;
    }

    public static MultiEnableCheck createEnableCheck(
            final WorkbenchContext workbenchContext, CursorTool tool) {

        EnableCheckFactory cf =
            workbenchContext.createPlugInContext().getCheckFactory();

        EnableCheck[] checks = { cf.createTaskWindowMustBeActiveCheck(),

        cf.createOnlyOneLayerMayHaveSelectedFeaturesCheck(),
                cf.createExactlyNFeaturesMustBeSelectedCheck(1),
                cf.createSelectedItemsLayersMustBeEditableCheck() };

        MultiEnableCheck mec = new MultiEnableCheck();

        for (EnableCheck check : checks) {
            mec.add(check);
        }

        return mec;
    }

    /**
     * Gets the layer affected by the plugin.
     * 
     * @return the editable layer.
     */
    protected Layer getLayer() {
        return wContext.getLayerManager().getEditableLayers().iterator().next();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        int lastClickIndex = getCoordinates().size() - 1;
        Coordinate lastClickCoord = (Coordinate) getCoordinates().get(
                lastClickIndex);
        Point lastClick = geomFac.createPoint(lastClickCoord);
        double tolerance = WorkbenchUtils.viewportToMapDistance(TOLERANCE_PX); // tolerance
                                                                               // in
                                                                               // model
                                                                               // units
        SelectionManager selectionManager = getPanel().getSelectionManager();
        Layer editableLayer = getLayer();
        Feature featSelected = selectionManager
                .getFeaturesWithSelectedItems(editableLayer).iterator().next();
        // the checks ensure that there is exactly 1 item selected in editable
        // layer
        Geometry geom = featSelected.getGeometry();
        Geometry buffer = geom.buffer(tolerance);
        List coordinates = getCoordinates();
        if (!buffer.covers(lastClick)) {
            coordinates.remove(lastClickIndex); // it was not valid
            String msg = i18n
                .get("org.openjump.core.ui.tools.RemoveSectionInLine.must-do-click-on-selected-line"); //$NON-NLS-1$
            JUMPWorkbench.getInstance().getFrame().warnUser(msg);
        } else {
            try {
                List<Point2D> centers = new ArrayList<>();
                centers.add(WorkbenchUtils
                        .viewportToViewPoint((Coordinate) coordinates
                                .get(coordinates.size() - 1)));
                Animations.drawExpandingRings(centers, true, Color.BLUE,
                        getPanel(), new float[] { 5, 5 });
            } catch (NoninvertibleTransformException ex) {
                Logger.error("", ex);
            }
        }
    }

    /** Selecting tool in case of no check conditions */
    protected SelectFeaturesTool select =
        new SelectFeaturesTool(JUMPWorkbench.getInstance().getContext());

    /**
     * @return
     */
    protected boolean checkConditions() {

        if (!WorkbenchUtils.check(checkFactory
                .createTaskWindowMustBeActiveCheck())) {
            return false;
        }
        if (!WorkbenchUtils.check(checkFactory
                .createWindowWithLayerManagerMustBeActiveCheck())) {
            return false;
        }

        if (getCoordinates().isEmpty()) {
            return false;
        } // Check only for LineString and MultiLineString. Excluding other
          // geometry types
        if (!WorkbenchUtils
                .check(CADEnableCheckFactory
                        .createExactlyNFeaturesWithGeometryTypeMustBeSelectedCheck(
                                new int[] { CADEnableCheckFactory.FEATURE_SCHEMA_LINESTRING },
                                new int[] {
                                        CADEnableCheckFactory.FEATURE_SCHEMA_GEOMETRYCOLLECTION,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_MULTILINESTRING,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_MULTIPOINT,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_MULTIPOLYGON,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_POINT,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_POLYGON,
                                        CADEnableCheckFactory.FEATURE_SCHEMA_LINEARRING },
                                1))) {
            return false;
        }
        return false;
    }

}
