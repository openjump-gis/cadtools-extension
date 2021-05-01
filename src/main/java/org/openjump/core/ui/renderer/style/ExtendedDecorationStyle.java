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

package org.openjump.core.ui.renderer.style;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import javax.swing.Icon;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.ChoosableStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.LineStringStyle;

public abstract class ExtendedDecorationStyle extends LineStringStyle implements
        ChoosableStyle {

    protected String name;

    protected Icon icon;

    public ExtendedDecorationStyle(String name, Icon icon) {
        super(name, icon);
        this.name = name;
        this.icon = icon;
    }

    @Override
    protected void paintLineString(LineString lineString, Viewport viewport,
            Graphics2D graphics) throws Exception {
        final double scale = viewport.getScale();
        Coordinate mid, previous = null;
        final double length = lineString.getLength();
        // Do not draw symbol if total feature length is < 6 pixels
        if (length * scale < 6) {
            return;
        }

        final CoordinateList coord = Coordinates(lineString);
        final Coordinate[] newCoordinateArray = coord.toCoordinateArray();
        final LineString line = new GeometryFactory()
                .createLineString(newCoordinateArray);

        for (int i = 0; i < line.getNumPoints() - 1; i++) {
            final Coordinate c0 = line.getCoordinateN(i);
            final Coordinate c1 = line.getCoordinateN(i + 1);

            mid = new Coordinate((c0.x + c1.x) / 2, (c0.y + c1.y) / 2);
            // Do not draw symbol if previous symbol for this feature is less
            // than 30 pixels far.
            // Do not draw symbols if middle point is outside the original
            // geometry. This prevent symbols floating outside the geometry.
            final Point p = new GeometryFactory().createPoint(mid);
            if (previous != null && previous.distance(mid) * scale < 30
                    && !p.intersects(lineString)) {
                continue;
            }
            paint(c0, c1, viewport, graphics);
            previous = mid;
        }
    }

    double distance = 10.0D;

    // [Giuseppe Aruta 2018_3_29] this code derives from
    // es.unex.sextante.vectorTools.linesToEquispacedPoints class form Sextante
    // Algorithms. It is used to calculate a List of coordinates along a defined
    // geometry at an equispaced distance (in this case: 10 px)
    private CoordinateList Coordinates(final Geometry geom) {
        final CoordinateList coordinates = new CoordinateList();
        int i, j;
        int iPoints;
        double dX1, dX2, dY1, dY2;
        double dAddedPointX, dAddedPointY;
        double dDX, dDY;
        double dRemainingDistFromLastSegment = 0;
        double dDistToNextPoint;
        double dDist;
        final Coordinate[] coords = geom.getCoordinates();
        if (coords.length == 0) {
            return coordinates;
        }

        dAddedPointX = dX1 = coords[0].x;
        dAddedPointY = dY1 = coords[0].y;
        final Coordinate coord1 = new Coordinate(dAddedPointX, dAddedPointY);
        coordinates.add(coord1);
        // m_Output.addFeature(point, record);
        for (i = 0; i < coords.length - 1; i++) {
            dX2 = coords[i + 1].x;
            dX1 = coords[i].x;
            dY2 = coords[i + 1].y;
            dY1 = coords[i].y;
            dDX = dX2 - dX1;
            dDY = dY2 - dY1;
            dDistToNextPoint = Math.sqrt(dDX * dDX + dDY * dDY);

            if (dRemainingDistFromLastSegment + dDistToNextPoint > distance) {
                iPoints = (int) ((dRemainingDistFromLastSegment + dDistToNextPoint) / distance);
                //dDist = distance - dRemainingDistFromLastSegment;
                for (j = 0; j < iPoints; j++) {
                    dDist = distance - dRemainingDistFromLastSegment;
                    dDist += j * distance;
                    dAddedPointX = dX1 + dDist * dDX / dDistToNextPoint;
                    dAddedPointY = dY1 + dDist * dDY / dDistToNextPoint;
                    final Coordinate coord2 = new Coordinate(dAddedPointX,
                            dAddedPointY);
                    coordinates.add(coord2);
                }
                dDX = dX2 - dAddedPointX;
                dDY = dY2 - dAddedPointY;
                dRemainingDistFromLastSegment = Math
                        .sqrt(dDX * dDX + dDY * dDY);
            } else {
                dRemainingDistFromLastSegment += dDistToNextPoint;
            }

        }
        return coordinates;
    }

    protected void paint(Coordinate p0, Coordinate p1, Viewport viewport,
            Graphics2D graphics) throws Exception {
        final Point2D firstPoint = new Point2D.Double(p0.x, p0.y);
        final Point2D secondPoint = new Point2D.Double(p1.x, p1.y);

        paint(viewport.toViewPoint(firstPoint),
                viewport.toViewPoint(secondPoint), viewport, graphics);
    }

    protected abstract void paint(Point2D p0, Point2D p1, Viewport viewport,
            Graphics2D graphics) throws Exception;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }
}
