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

package org.openjump.core.ui.renderer.style.type;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import org.openjump.core.ui.renderer.style.ExtendedDecorationStyle;
import org.openjump.core.ui.renderer.style.images.IconLoader;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.Viewport;

public class NormalFaultStyle extends ExtendedDecorationStyle {
    private final static double SMALL_ANGLE = 8;
    private final static double MEDIUM_ANGLE = 60;

    private final static double SMALL_LENGTH = 8;
    private final static double VERY_SMALL_LENGTH = 4;
    private final static double LARGE_LENGTH = 15;
    private boolean filled;
    private double finAngle;
    protected double finLength;

    /**
     * @param finAngle
     *            degrees
     * @param finLength
     *            pixels
     */
    public NormalFaultStyle(String name, String iconFile, double finAngle,
            double finLength, boolean filled) {
        super(name, IconLoader.icon(iconFile));
        this.finAngle = finAngle;
        this.finLength = finLength;
        this.filled = filled;
    }

    @Override
    protected void paint(Point2D p0, Point2D p1, Viewport viewport,
            Graphics2D graphics) throws NoninvertibleTransformException {
        if (p0.equals(p1)) {
            return;
        }

        graphics.setColor(lineColorWithAlpha);
        graphics.setStroke(stroke);

        GeneralPath arrowhead = arrowhead(p0, p1, finLength);
        // ,finAngle);

        if (filled) {
            arrowhead.closePath();
            graphics.fill(arrowhead);
        }

        // #fill isn't affected by line width, but #draw is. Therefore, draw
        // even
        // if we've already filled. [Jon Aquino]
        graphics.draw(arrowhead);
    }

    /**
     * @param p0
     *            the tail of the whole arrow; just used to determine angle
     * @param finLength
     *            required distance from the tip to each fin's tip
     */
    private GeneralPath arrowhead(Point2D p0, Point2D p1, double finLength)
    {
        Point2D mid = new Point2D.Float((float) ((p0.getX() + p1.getX()) / 2),
                (float) ((p0.getY() + p1.getY()) / 2));
        GeneralPath path = new GeneralPath();
        Point2D finTip1 = fin(mid, p0, finLength, finAngle);
        path.moveTo((float) finTip1.getX(), (float) finTip1.getY());

        path.lineTo((float) mid.getX(), (float) mid.getY());

        return path;
    }

    private Point2D fin(Point2D shaftTip, Point2D shaftTail, double length,
            double angle) {
        double shaftLength = shaftTip.distance(shaftTail);
        Point2D finTail = shaftTip;
        Point2D finTip = GUIUtil.add(
                GUIUtil.multiply(GUIUtil.subtract(shaftTail, shaftTip), length
                        / shaftLength), finTail);

        // Rotazione del simbolo rispetto alla linea digitalizzata Math.PI/2 =
        // 90°
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.rotate(Math.PI / 2,
        // (angle * Math.PI) / 90,
                finTail.getX(), finTail.getY());

        return affineTransform.transform(finTip, null);
    }

    public static class Small extends NormalFaultStyle {
        public Small() {
            super("Geo 3a", "geo3a.png", MEDIUM_ANGLE, SMALL_LENGTH, false);
        }
    }

    public static class VerySmall extends NormalFaultStyle {
        public VerySmall() {
            super("Geo 3b", "geo3b.png", MEDIUM_ANGLE, VERY_SMALL_LENGTH, true);
        }
    }

    public static class Big extends NormalFaultStyle {
        public Big() {
            super("Geo 3b", "geo3b.png", SMALL_ANGLE, LARGE_LENGTH, true);
        }
    }

}
