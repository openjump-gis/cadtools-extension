package org.openjump.advancedtools.utils;

import java.text.DecimalFormat;

import org.locationtech.jts.geom.Coordinate;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.HTMLFrame;

public class CoordinateListMetricsUtils {

    private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");

    public static final String Radius = i18n
        .get("org.openjump.core.ui.utils.CoordinateListMetrics.radius")
            + ": ";
    public static final String Circum = i18n
        .get("org.openjump.core.ui.utils.CoordinateListMetrics.circumference")
            + ": ";
    public static final String Source = i18n
        .get("org.openjump.core.ui.utils.CoordinateListMetrics.source")
            + ": ";
    public static final String Target = i18n
        .get("org.openjump.core.ui.utils.CoordinateListMetrics.target")
            + ": ";
    public static final String Coordinates = i18n
        .get("org.openjump.core.ui.utils.CoordinateListMetrics.coordinates");

    public static final String Circle = i18n
        .get("org.openjump.core.ui.utils.CoordinateListMetrics.Circle");

    public static final String Measure = i18n
        .get("org.openjump.core.ui.utils.CoordinateListMetrics.Measure");

    public static final String Centre = i18n
        .get("org.openjump.core.ui.utils.CoordinateListMetrics.center-coordinates")
            + ": ";
    /*
     * From
     * com.vividsolutions.jump.workbench.ui.cursortool.CoordinateListMetrics
     */
    public static final String Area = I18N.JUMP
            .get("ui.cursortool.CoordinateListMetrics.Area");
    public static final String Azimuth = I18N.JUMP
            .get("ui.cursortool.CoordinateListMetrics.Azimuth");
    public static final String Angle = I18N.JUMP
            .get("ui.cursortool.CoordinateListMetrics.Angle");
    public static final String Distance = I18N.JUMP
            .get("ui.cursortool.CoordinateListMetrics.Distance");

    public static final String Perimeter = I18N.JUMP
            .get("ui.cursortool.CoordinateListMetrics.Perimeter");

    public static DecimalFormat df2 = new DecimalFormat("##0.0##");
    public static DecimalFormat df1 = new DecimalFormat("##0.#");

    public static String circleString(double a, double b, double c,
            Coordinate start, Coordinate target) {
        return Coordinates + " [" + Source + df1.format(start.x) + " ; "
                + df1.format(start.y) + " - " + Target + df1.format(target.x)
                + " ; " + df1.format(target.y) + "]  " + Radius + df2.format(a)
                + "  " + Circum + df2.format(b) + "  " + Area + ": "
                + df2.format(c);

    }

    public static void setCircleMessage(double radius, double circumference,
            double area, Coordinate start, Coordinate target) {
        String all = Coordinates + " [" + Source + df1.format(start.x) + " ; "
                + df1.format(start.y) + " - " + Target + df1.format(target.x)
                + " ; " + df1.format(target.y) + "]  " + Radius
                + df2.format(radius) + "  " + Circum
                + df2.format(circumference) + "  " + Area + ": "
                + df2.format(area);
        JUMPWorkbench.getInstance().getFrame().getContext().getLayerViewPanel()
                .getContext().setStatusMessage(all);
    }

    public static void setCircle3PointsMessage(double radius,
            double circumference, double area, Coordinate start) {
        String all = Coordinates + " [" + Centre + df1.format(start.x) + " ; "
                + df1.format(start.y) + "]  " + Radius + df2.format(radius)
                + "  " + Circum + df2.format(circumference) + "  " + Area
                + ": " + df2.format(area);
        JUMPWorkbench.getInstance().getFrame().getContext().getLayerViewPanel()
                .getContext().setStatusMessage(all);
    }

    public static void setEllipseMessage(double length1, double length2,
            double circumference, double area) {
        String all = "l1: " + df2.format(length1) + "  l2: "
                + df2.format(length2) + "  " + Circum
                + df2.format(circumference) + "  " + Area + ": "
                + df2.format(area);
        JUMPWorkbench.getInstance().getFrame().getContext().getLayerViewPanel()
                .getContext().setStatusMessage(all);
    }

    public static void setParallelogramMessage(double length1, double length2,
            double angle, double perimeter, double area) {
        String all;

        all = "l1: " + df2.format(length1) + "  l2: " + df2.format(length2)
                + "  " + Angle + ": " + df2.format(angle) + "  " + Perimeter
                + ": " + df2.format(perimeter) + "  " + Area + ": "
                + df2.format(area);

        JUMPWorkbench.getInstance().getFrame().getContext().getLayerViewPanel()
                .getContext().setStatusMessage(all);
    }

    public static void setCircleHTMLFrame(double a, double b, double c,
            Coordinate start, Coordinate target) {
        HTMLFrame out = JUMPWorkbench.getInstance().getContext()
                .createPlugInContext().getOutputFrame();
        out.createNewDocument();
        out.addHeader(1, Measure);
        out.addHeader(2, Circle);
        out.addHeader(
                3,
                Coordinates + " [" + Source + df2.format(start.x) + ";"
                        + df2.format(start.y) + " - " + Target
                        + df2.format(target.x) + ";" + df2.format(target.y)
                        + "]  ");
        out.addHeader(3, Radius + df2.format(a));
        out.addHeader(3, Circum + df2.format(b));
        out.addHeader(3, Area + ": " + df2.format(c));
    }

    public static void setMessage(double a) {
        String all = Distance + ": " + df2.format(a);
        JUMPWorkbench.getInstance().getFrame().getContext().getLayerViewPanel()
                .getContext().setStatusMessage(all);
    }

}
