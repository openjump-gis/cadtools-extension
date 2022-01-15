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
/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI 
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * JUMP is Copyright (C) 2003 Vivid Solutions
 *
 * This program implements extensions to JUMP and is
 * Copyright (C) 2004 Integrated Systems Analysts, Inc.
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
 * Integrated Systems Analysts, Inc.
 * 630C Anchors St., Suite 101
 * Fort Walton Beach, Florida 32548
 * USA
 *
 * (850)862-7321
 * www.ashs.isa.com
 */

package org.openjump.advancedtools.tools;

import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.Icon;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.openjump.advancedtools.CadExtension;
import org.openjump.advancedtools.config.CADToolsOptionsPanel;
import org.openjump.advancedtools.icon.IconLoader;
import org.openjump.advancedtools.utils.CoordinateListMetricsUtils;
import org.openjump.core.geomutils.GeoUtils;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.geom.Angle;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.FeatureDrawingUtil;

/**
 * Tool draw constrained parallelograms
 * 
 * @author Giuseppe Aruta [Genuary 30th 2017] adapted from SkyJUMP to OpenJUMP
 *         1.10 (http://www.openjump.org/support.html)
 * @since OpenJUMP 1.10 (2017)
 */
public class DrawConstrainedParallelogramTool extends ConstrainedNClickTool {

    private static final I18N i18n = CadExtension.I18N;

    private final FeatureDrawingUtil featureDrawingUtil;

    private DrawConstrainedParallelogramTool(
            FeatureDrawingUtil paramFeatureDrawingUtil) {
        super(JUMPWorkbench.getInstance().getContext(), 3);
        allowSnapping();
        this.drawClosed = true;
        this.featureDrawingUtil = paramFeatureDrawingUtil;
    }

    /** Name of the tool */
    public final static String NAME = i18n
        .get("org.openjump.core.ui.plugins.Parallelogram");
    /** Description of the tool */
    public final static String DESCRIPTION = i18n
        .get("org.openjump.core.ui.plugins.Parallelogram.description");

    public static CursorTool create(LayerNamePanelProxy paramLayerNamePanelProxy) {
        FeatureDrawingUtil localFeatureDrawingUtil = new FeatureDrawingUtil(
                paramLayerNamePanelProxy);
        return localFeatureDrawingUtil.prepare(
                new DrawConstrainedParallelogramTool(localFeatureDrawingUtil),
                true);
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
    public Icon getIcon() {
        return GUIUtil.resize(IconLoader.icon("drawParallelogram1.png"), 20);
    }

    private final GeometryFactory factory = new GeometryFactory();

    @Override
    protected void gestureFinished() throws Exception {
        reportNothingToUndoYet();
        if (!goodParallelogram()) {
            return;
        }

        if (CADToolsOptionsPanel.isGetAsPolygon()) {
            Polygon geom = factory.createPolygon(getParallelogram(), null);
            featureDrawingUtil.drawRing(geom,
                    isRollingBackInvalidEdits(), this, getPanel());

        } else {
            Coordinate[] coords = getParallelogram().getCoordinates();
            featureDrawingUtil.drawLineString(factory.createLineString(coords),
                    isRollingBackInvalidEdits(), this, getPanel());

        }

        if (CADToolsOptionsPanel.isGetCentroid()) {
            Geometry geom;
            geom = factory.createPolygon(getParallelogram(), null);

            execute(featureDrawingUtil.createAddCommand(geom.getCentroid(),
                    isRollingBackInvalidEdits(), getPanel(), this));
        }

    }

    @Override
    protected Shape getShape() throws NoninvertibleTransformException {
        Point2D localPoint2D1 = getPanel().getViewport().toViewPoint(
                (Coordinate) this.coordinates.get(0));
        GeneralPath localGeneralPath = new GeneralPath();
        localGeneralPath.moveTo((float) localPoint2D1.getX(),
                (float) localPoint2D1.getY());
        Object localObject;
        if (this.coordinates.size() > 1) {
            localObject = this.coordinates.get(1);
            Point2D localPoint2D2 = getPanel().getViewport().toViewPoint(
                    (Coordinate) localObject);
            localGeneralPath.lineTo((int) localPoint2D2.getX(),
                    (int) localPoint2D2.getY());
            Point2D localPoint2D3 = getPanel().getViewport().toViewPoint(
                    this.tentativeCoordinate);
            localGeneralPath.lineTo((int) localPoint2D3.getX(),
                    (int) localPoint2D3.getY());
            Coordinate localCoordinate1 = new Coordinate(localPoint2D1.getX(),
                    localPoint2D1.getY());
            Coordinate localCoordinate2 = new Coordinate(localPoint2D2.getX(),
                    localPoint2D2.getY());
            Coordinate localCoordinate3 = new Coordinate(localPoint2D3.getX(),
                    localPoint2D3.getY());
            Coordinate localCoordinate4 = GeoUtils.vectorBetween(
                    localCoordinate2, localCoordinate1);
            localCoordinate4 = GeoUtils.vectorAdd(localCoordinate3,
                    localCoordinate4);
            Point2D.Double localDouble = new Point2D.Double(localCoordinate4.x,
                    localCoordinate4.y);
            localGeneralPath.lineTo((int) localDouble.getX(),
                    (int) localDouble.getY());
        } else {
            localObject = getPanel().getViewport().toViewPoint(
                    this.tentativeCoordinate);
            localGeneralPath.lineTo((int) ((Point2D) localObject).getX(),
                    (int) ((Point2D) localObject).getY());
        }
        if (this.drawClosed) {
            localGeneralPath.lineTo((float) localPoint2D1.getX(),
                    (float) localPoint2D1.getY());
        }
        return localGeneralPath;
    }

    protected LinearRing getParallelogram() {
        if (this.coordinates.size() > 2) {
            Coordinate localCoordinate1 = (Coordinate) this.coordinates.get(0);
            Coordinate localCoordinate2 = (Coordinate) this.coordinates.get(1);
            Coordinate localCoordinate3 = (Coordinate) this.coordinates.get(2);
            Coordinate localCoordinate4 = GeoUtils.vectorBetween(
                    localCoordinate2, localCoordinate1);
            localCoordinate4 = GeoUtils.vectorAdd(localCoordinate3,
                    localCoordinate4);
            ArrayList<Coordinate> localArrayList = new ArrayList();
            localArrayList.add(new Coordinate((float) localCoordinate1.x,
                    (float) localCoordinate1.y));
            localArrayList.add(new Coordinate((float) localCoordinate2.x,
                    (float) localCoordinate2.y));
            localArrayList.add(new Coordinate((float) localCoordinate3.x,
                    (float) localCoordinate3.y));
            localArrayList.add(new Coordinate((float) localCoordinate4.x,
                    (float) localCoordinate4.y));
            localArrayList.add(new Coordinate((float) localCoordinate1.x,
                    (float) localCoordinate1.y));

            return new GeometryFactory()
                    .createLinearRing(toArray(localArrayList));

        }
        return null;
    }

    @Override
    public void mouseLocationChanged(MouseEvent e) {
        try {
            if (isShapeOnScreen()) {
                redrawShape();
            }
            super.mouseLocationChanged(e);

            if (getCoordinates().size() > 1) {

                Coordinate localCoordinate1 = (Coordinate) this.coordinates
                        .get(0);
                Coordinate localCoordinate2 = (Coordinate) this.coordinates
                        .get(1);
                Coordinate localCoordinate3 = this.tentativeCoordinate;
                Coordinate localCoordinate4 = GeoUtils.vectorBetween(
                        localCoordinate2, localCoordinate1);
                localCoordinate4 = GeoUtils.vectorAdd(localCoordinate3,
                        localCoordinate4);
                ArrayList<Coordinate> localArrayList = new ArrayList<>();
                localArrayList.add(new Coordinate((float) localCoordinate1.x,
                        (float) localCoordinate1.y));
                localArrayList.add(new Coordinate((float) localCoordinate2.x,
                        (float) localCoordinate2.y));
                localArrayList.add(new Coordinate((float) localCoordinate3.x,
                        (float) localCoordinate3.y));
                localArrayList.add(new Coordinate((float) localCoordinate4.x,
                        (float) localCoordinate4.y));
                localArrayList.add(new Coordinate((float) localCoordinate1.x,
                        (float) localCoordinate1.y));

                Polygon polygon = new GeometryFactory()
                        .createPolygon(toArray(localArrayList));

                double distance1 = localCoordinate1.distance(localCoordinate2);
                double distance2 = localCoordinate2.distance(localCoordinate3);
                double angle = Math.toDegrees(Angle.angleBetween(
                        localCoordinate2, localCoordinate1, localCoordinate3));
                double area = polygon.getArea();
                double perimeter = polygon.getLength();

                CoordinateListMetricsUtils.setParallelogramMessage(distance1,
                        distance2, angle, perimeter, area);
            }
        } catch (Throwable t) {
            getPanel().getContext().handleThrowable(t);
        }
    }

    protected boolean goodParallelogram() {
        if (getCoordinates().size() < 3) {
            return false;
        }
        Coordinate localCoordinate1 = (Coordinate) this.coordinates.get(0);
        Coordinate localCoordinate2 = this.tentativeCoordinate;
        if (localCoordinate1.x == localCoordinate2.x) {
            return false;
        }
        return localCoordinate1.y != localCoordinate2.y;
    }
}
