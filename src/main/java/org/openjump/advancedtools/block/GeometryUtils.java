package org.openjump.advancedtools.block;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTFileReader;
import org.locationtech.jts.io.ParseException;
import com.vividsolutions.jump.geom.CoordUtil;
import org.locationtech.jts.io.WKTReader;

public class GeometryUtils {

    /**
     * Method to scale a selected geometry of a scale factor
     * 
     * @param geometry input geometry
     * @param scale Scale factor (50 = half, 100 = no rescale, 200 = scale two
     *        times)
     */
    public static void scaleGeometry(Geometry geometry, final double scale) {
        final Coordinate center = geometry.getCentroid().getCoordinate();
        geometry.apply(new CoordinateFilter() {
            @Override
            public void filter(Coordinate coordinate) {
                coordinate.x = center.x + (scale / 100)
                        * (coordinate.x - center.x);
                coordinate.y = center.y + (scale / 100)
                        * (coordinate.y - center.y);
            }
        });
    }

    /**
     * Method to rotate a geometry of a defined angle
     * 
     * @param geometry input geometry
     * @param angle in degree
     */
    public static void rotateGeometry(Geometry geometry, final double angle) {
        final Coordinate center = geometry.getCentroid().getCoordinate();
        double Deg2Rad = 0.0174532925199432;
        double radiansAngle = 0.0;
        radiansAngle = Deg2Rad * (-angle);
        final double cosAngle = Math.cos(radiansAngle);
        final double sinAngle = Math.sin(radiansAngle);
        geometry.apply(new CoordinateFilter() {
            @Override
            public void filter(Coordinate coordinate) {

                double x = coordinate.x - center.x;
                double y = coordinate.y - center.y;
                coordinate.x = center.x + (x * cosAngle) - (y * sinAngle);
                coordinate.y = center.y + (y * cosAngle) + (x * sinAngle);
            }
        });
    }

    /**
     * Method to counterclock wise rotate a geometry of a defined angle and
     * setting center of rotation
     * 
     * @param geometry input geometry
     * @param angle in degree
     * @param center coordinate center of rotation
     */

    public static void rotateGeometry(Geometry geometry, final double angle,
            final Coordinate center) {

        geometry.apply(new CoordinateFilter() {
            @Override
            public void filter(Coordinate coordinate) {
                double cosAngle = Math.cos(angle);
                double sinAngle = Math.sin(angle);
                double x = coordinate.x - center.y;
                double y = coordinate.y - center.x;
                coordinate.x = center.x + (x * cosAngle) + (y * sinAngle);
                coordinate.y = center.y + (y * cosAngle) - (x * sinAngle);
            }
        });
    }

    public static void rotate_Geometry(Geometry geometry, final double angle) {
        final Coordinate center = geometry.getCentroid().getCoordinate();
        geometry.apply(new CoordinateFilter() {
            @Override
            public void filter(Coordinate coordinate) {
                double cosAngle = Math.cos(angle);
                double sinAngle = Math.sin(angle);
                double x = coordinate.x - center.y;
                double y = coordinate.y - center.x;
                coordinate.x = center.x + (x * cosAngle) + (y * sinAngle);
                coordinate.y = center.y + (y * cosAngle) - (x * sinAngle);
            }
        });
    }

    /**
     * Method to counterclock wise rotate a geometry of a defined angle
     * 
     * @param geometry input geometry
     * @param angle in degree
     */
    public static void rotate_counterclockwise_Geometry(Geometry geometry,
            final double angle) {
        final Coordinate center = geometry.getCentroid().getCoordinate();
        double Deg2Rad = 0.0174532925199432;
        double radiansAngle = 0.0;
        radiansAngle = Deg2Rad * (-angle);
        final double cosAngle = Math.cos(radiansAngle);
        final double sinAngle = Math.sin(radiansAngle);
        geometry.apply(new CoordinateFilter() {
            @Override
            public void filter(Coordinate coordinate) {
                double x = coordinate.x - center.x;
                double y = coordinate.y - center.y;
                coordinate.x = center.x + (x * cosAngle) - (y * sinAngle);
                coordinate.y = center.y + (y * cosAngle) + (x * sinAngle);
            }
        });
    }

    /**
     * Method to clock wise rotate a geometry of a defined angle
     * 
     * @param geometry input geometry
     * @param angle in degree
     */
    public static void rotate_clockwise_Geometry(Geometry geometry,
            final double angle) {
        final Coordinate center = geometry.getCentroid().getCoordinate();
        double Deg2Rad = 0.0174532925199432;
        double radiansAngle = 0.0;
        radiansAngle = Deg2Rad * (-angle);
        final double cosAngle = Math.cos(radiansAngle);
        final double sinAngle = Math.sin(radiansAngle);
        geometry.apply(new CoordinateFilter() {
            @Override
            public void filter(Coordinate coordinate) {
                double x = coordinate.x - center.x;
                double y = coordinate.y - center.y;
                coordinate.x = center.x + (x * cosAngle) - (y * sinAngle);
                coordinate.y = center.y + (y * cosAngle) + (x * sinAngle);
            }
        });
    }

    /**
     * Move a geometry to a defined coordinate
     * 
     * @param geometry the geometry to move
     * @param displacement displacement value
     */
    public static void centerGeometry(final Geometry geometry,
            final Coordinate displacement) {
        geometry.apply(new CoordinateFilter() {
            @Override
            public void filter(Coordinate coordinate) {
                coordinate.setCoordinate(CoordUtil
                        .add(coordinate, displacement));
            }
        });
    }

    /**
     * Translate a geometry to a defined coordinate
     * 
     * @param geometry the geometry to move
     * @param displacement displacement value
     */

    protected void translateGeometry(Geometry geometry,
            final Coordinate displacement) {

        geometry.apply(new CoordinateFilter() {
            @Override
            public void filter(Coordinate coordinate) {
                double x = coordinate.x + displacement.x;
                double y = coordinate.y + displacement.y;
                coordinate.x = x;
                coordinate.y = y;
            }
        });
    }

    /**
     * Mirror a geometry to a defined coordinate
     * 
     * @param geometry the Geometry to transform
     */

    public static void mirrorY(Geometry geometry) {
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

    /**
     * Reads a WKT file and returns the first geometry.
     * 
     * @param wktPath path of the file containing the wkt geometries
     * @return the geometry read in the file located at wktPath
     * @throws IOException
     * @throws ParseException
     */
    public static Geometry geom(String wktPath) throws IOException, ParseException {
        WKTReader reader = new WKTReader();
        WKTFileReader fileReader = new WKTFileReader(wktPath, reader);
        @SuppressWarnings("rawtypes")
        List geomList = fileReader.read();
        if (geomList.size() == 1)
            return (Geometry) geomList.get(0);
        return null;
    }

    /**
     * Write a geometry to WKT file
     * 
     * @param geom the geometry to write to file
     * @param path the path to write to
     * @throws IOException
     * @throws ParseException
     */
    public static void writeToFile(Geometry geom, String path) {
        String wkt = geom.toString();

        Writer writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(path + ".wkt"), StandardCharsets.UTF_8));
            writer.write("1:16:" + wkt);
        } catch (IOException ex) {
            // report
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (Exception ex) {/* ignore */
            }
        }
    }

}
