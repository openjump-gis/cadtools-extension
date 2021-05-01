/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI 
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package org.openjump.advancedtools.tools.cogo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.util.ArrayList;

import javax.swing.Icon;

import org.openjump.advancedtools.icon.IconLoader;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.valid.IsValidOp;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.CoordinateListMetrics;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.MultiClickTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.FeatureDrawingUtil;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;

/**
 * Depending to the command line check it draws a linestring or a polygon
 * <p>
 * </p>
 */
public class DrawGeometryTool extends MultiClickTool {

    /** Cursor of the tool */
    public static final Cursor CURSOR = createCursor(IconLoader.icon(
            "commands_cursor.png").getImage());

    /** Tool name */
    public final static String NAME = I18N
            .get("workbench.ui.cursortool.editing.DrawLineStringTool.name");

    /** Tool icon */
    public final static Icon ICON = IconLoader.icon("DrawLineString.gif");

    /** */
    protected FeatureDrawingUtil featureDrawingUtil;

    /**
     * 
     */
    protected DrawGeometryTool(FeatureDrawingUtil featureDrawingUtil) {
        this(featureDrawingUtil, true);
        setColor(Color.RED.darker());
        setStroke(new BasicStroke(1.5f, // Width
                BasicStroke.CAP_SQUARE, // End cap
                BasicStroke.JOIN_ROUND, // Join style
                10.0f, // Miter limit
                new float[] { 10.0f, 5.0f }, // Dash pattern
                0.0f));
        setMetricsDisplay(new CoordinateListMetrics());
    }

    protected DrawGeometryTool(FeatureDrawingUtil featureDrawingUtil,
            boolean check) {
        super();
        this.featureDrawingUtil = featureDrawingUtil;
        setMetricsDisplay(new CoordinateListMetrics());
    }

    /**
     * @param layerNamePanelProxy a LayerNamePanel proxy
     * @return a CursorTool
     */
    public static CursorTool create(LayerNamePanelProxy layerNamePanelProxy) {
        FeatureDrawingUtil featureDrawingUtil = new FeatureDrawingUtil(
                layerNamePanelProxy);

        return featureDrawingUtil.prepare(new DrawGeometryTool(
                featureDrawingUtil), true);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    protected void gestureFinished() throws Exception {
        reportNothingToUndoYet();

        if (CommandLineStringPanel.polygonCheckBox.isSelected()) {

            if (!checkPolygon()) {
                return;
            }
            featureDrawingUtil.drawRing(getPolygon(),
                    isRollingBackInvalidEdits(), this, getPanel());
        } else {
            if (!checkLineString()) {
                return;
            }
            featureDrawingUtil.drawLineString(getLineString(),
                    isRollingBackInvalidEdits(), this, getPanel());
        }

    }

    protected void remove(Coordinate c) {
        if (getWorkbench().getContext().getLayerManager().getEditableLayers()
                .size() == 0) {
            cancelGesture();
            getWorkbench()
                    .getContext()
                    .getWorkbench()
                    .getFrame()
                    .warnUser(
                            I18N.get("workbench.ui.cursortool.MultiClickTool.an-editable-layer-must-exist"));
            return;
        }
        getCoordinates().remove(c);
    }

    GeometryFactory geomFac = new GeometryFactory();

    /**
     *
     */
    protected LineString getLineString() {
        return geomFac.createLineString(toArray(getCoordinates()));
    }

    protected Polygon getPolygon() {
        @SuppressWarnings("unchecked")
        ArrayList<Coordinate> closedPoints = new ArrayList<>(
                getCoordinates());
        if (!closedPoints.get(0).equals(
                closedPoints.get(closedPoints.size() - 1))) {
            closedPoints.add(new Coordinate(closedPoints.get(0)));
        }
        return geomFac.createPolygon(
                new GeometryFactory().createLinearRing(toArray(closedPoints)),
                null);
    }

    protected MultiPoint getMultiPoint() {
        @SuppressWarnings("unchecked")
        ArrayList<Coordinate> closedPoints = new ArrayList<>(
                getCoordinates());
        if (!closedPoints.get(0).equals(
                closedPoints.get(closedPoints.size() - 1))) {
            closedPoints.add(new Coordinate(closedPoints.get(0)));
        }
        return geomFac.createMultiPointFromCoords(toArray(closedPoints));
    }

    /**
     *
     */
    protected boolean checkLineString() {
        if (getCoordinates().size() < 2) {
            getPanel()
                    .getContext()
                    .warnUser(
                            I18N.get("ui.cursortool.editing.DrawLineString.the-linestring-must-have-at-least-2-points"));

            return false;
        }

        IsValidOp isValidOp = new IsValidOp(getLineString());

        if (!isValidOp.isValid()) {
            getPanel().getContext().warnUser(
                    isValidOp.getValidationError().getMessage());

            if (isRollingBackInvalidEdits()) {
                return false;
            }
        }

        return true;
    }

    protected boolean checkPolygon() {
        if (getCoordinates().size() < 3) {
            getPanel()
                    .getContext()
                    .warnUser(
                            I18N.get("ui.cursortool.PolygonTool.the-polygon-must-have-at-least-3-points"));

            return false;
        }

        IsValidOp isValidOp = new IsValidOp(getPolygon());

        if (!isValidOp.isValid()) {
            getPanel().getContext().warnUser(
                    isValidOp.getValidationError().getMessage());

            if (PersistentBlackboardPlugIn.get(getWorkbench().getContext())
                    .get(EditTransaction.ROLLING_BACK_INVALID_EDITS_KEY, false)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void activate(LayerViewPanel layerViewPanel) {
        super.activate(layerViewPanel);

    }

}
