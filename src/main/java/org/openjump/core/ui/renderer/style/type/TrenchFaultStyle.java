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

public class TrenchFaultStyle extends ExtendedDecorationStyle {
    private final static double SMALL_ANGLE = 8;
    private final static double MEDIUM_ANGLE = 60;

    private final static double SMALL_LENGTH = 6;
    private final static double MEDIUM_LENGTH = 10;
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
    public TrenchFaultStyle(String name, String iconFile, double finAngle,
            double finLength, boolean filled) {
        super(name, IconLoader.icon(iconFile));
        this.finAngle = finAngle;
        this.finLength = finLength;
        this.filled = filled;
    }

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
        Point2D finTip2 = fin2(mid, p0, finLength, finAngle);
        path.moveTo((float) finTip1.getX(), (float) finTip1.getY());

        path.lineTo((float) mid.getX(), (float) mid.getY());
        path.lineTo((float) finTip2.getX(), (float) finTip2.getY());

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

    private Point2D fin2(Point2D shaftTip, Point2D shaftTail, double length,
            double angle) {
        double shaftLength = shaftTip.distance(shaftTail);
        Point2D finTail = shaftTip;
        Point2D finTip = GUIUtil.add(
                GUIUtil.multiply(GUIUtil.subtract(shaftTail, shaftTip), length
                        / shaftLength), finTail);

        // Rotazione del simbolo rispetto alla linea digitalizzata Math.PI/2 =
        // 90°
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.rotate(-Math.PI / 2,
        // (angle * Math.PI) / 90,
                finTail.getX(), finTail.getY());

        return affineTransform.transform(finTip, null);
    }

    public static class Small extends TrenchFaultStyle {
        public Small() {
            super("Geo 7", "geo7.png", MEDIUM_ANGLE, SMALL_LENGTH, false);
        }
    }

    public static class Medium extends TrenchFaultStyle {
        public Medium() {
            super("Geo 7", "geo7.png", MEDIUM_ANGLE, MEDIUM_LENGTH, true);
        }
    }

    public static class Big extends TrenchFaultStyle {
        public Big() {
            super("Geo 7",

            "geo7.png", SMALL_ANGLE, LARGE_LENGTH, true);
        }
    }

}
