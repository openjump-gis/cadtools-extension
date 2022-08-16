package org.openjump.advancedtools.block;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.ImageIcon;

import org.openjump.core.geomutils.Circle;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.PlugInManager;

/**
 * @deprecated entirely replaced by the new {@link BlockCell} class.
 */
@Deprecated
public class BlockUtils {

    static WorkbenchContext context = JUMPWorkbench.getInstance().getFrame()
            .getContext();

    // Reads Geometry form a wkt file. Since the files are from VertexFolder and
    // have a difference
    // in wkt syntax, the text before the wkt geometry specification is excluded
    @Deprecated
    public static Geometry readGeomFromWKT(String filename, String folder)
            throws IOException, org.locationtech.jts.io.ParseException {
        PlugInManager manager = context.getWorkbench().getPlugInManager();
        Geometry geometry = null;
        File pluginDir = manager.getPlugInDirectory();

        String wd = pluginDir.getAbsolutePath();
        String filenamedir = wd + File.separator + folder + File.separator
                + filename;
        String text = readFile(filenamedir);
        String[] segments = text.split(":"); // Split path into segments using
                                             // (:) as separator
        String wkt = segments[segments.length - 1]; // Grab the last segment
        org.locationtech.jts.io.WKTReader wktReader = new WKTReader();// Create
                                                                        // a
                                                                        // WKTReader
        geometry = wktReader.read(wkt);// Read the geometry
        return geometry;
    }

    // Get the geometry
    /*
     * public static Geometry getGeometry() throws IOException, ParseException {
     * Geometry geom2 = null; if
     * (BlockPanel.blockBox.getSelectedItem().toString()
     * .equals(BlockPanel.triangle)) { geom2 = BlockUtils.triangle(); } else if
     * (BlockPanel.blockBox.getSelectedItem().toString()
     * .equals(BlockPanel.square)) { geom2 = BlockUtils.square(); } else if
     * (BlockPanel.blockBox.getSelectedItem().toString()
     * .equals(BlockPanel.circle)) { geom2 = BlockUtils.circle(); } else if
     * (BlockPanel.blockBox.getSelectedItem().toString()
     * .equals(BlockPanel.cross)) { geom2 = BlockUtils.cross(); } else if
     * (BlockPanel.blockBox.getSelectedItem().toString()
     * .equals(BlockPanel.star)) { geom2 = BlockUtils.star(); }
     * 
     * else { String blockName = (String) BlockPanel.blockBox.getSelectedItem()
     * + ".wkt"; geom2 = readGeomFromWKT(blockName, BlockPanel.blockFolder); }
     * return geom2; }
     */
/*
    // Get the geometry
    @Deprecated
    public static Geometry getGeometry() throws IOException, ParseException {
        Geometry geom2 = null;
        if (BlockPanel.chooseBox.getSelectedItem().toString()
                .equals(BlockPanel.triangle)) {
            geom2 = BlockUtils.triangle();
        } else if (BlockPanel.chooseBox.getSelectedItem().toString()
                .equals(BlockPanel.square)) {
            geom2 = BlockUtils.square();
        } else if (BlockPanel.chooseBox.getSelectedItem().toString()
                .equals(BlockPanel.circle)) {
            geom2 = BlockUtils.circle();
        } else if (BlockPanel.chooseBox.getSelectedItem().toString()
                .equals(BlockPanel.cross)) {
            geom2 = BlockUtils.cross();
        } else if (BlockPanel.chooseBox.getSelectedItem().toString()
                .equals(BlockPanel.star)) {
            geom2 = BlockUtils.star();
        }

        else {
            String blockName = (String) BlockPanel.chooseBox.getSelectedItem()
                    + ".wkt";
            geom2 = readGeomFromWKT(blockName, BlockPanel.blockFolder);
        }
        return geom2;
    }

    // Get the geometry
    @Deprecated
    public static Shape getShape() throws IOException, ParseException,
            NoninvertibleTransformException {
        Geometry geom2 = null;
        Shape shape2 = null;
        if (BlockPanel.chooseBox.getSelectedItem().toString()
                .equals(BlockPanel.triangle)) {
            geom2 = BlockUtils.triangle();

        } else if (BlockPanel.chooseBox.getSelectedItem().toString()
                .equals(BlockPanel.square)) {
            geom2 = BlockUtils.square();
        } else if (BlockPanel.chooseBox.getSelectedItem().toString()
                .equals(BlockPanel.circle)) {
            geom2 = BlockUtils.circle();
        } else if (BlockPanel.chooseBox.getSelectedItem().toString()
                .equals(BlockPanel.cross)) {
            geom2 = BlockUtils.cross();
        } else if (BlockPanel.chooseBox.getSelectedItem().toString()
                .equals(BlockPanel.star)) {
            geom2 = BlockUtils.star();
        }

        else {
            String blockName = (String) BlockPanel.chooseBox.getSelectedItem()
                    + ".wkt";
            geom2 = readGeomFromWKT(blockName, BlockPanel.blockFolder);

        }
        shape2 = JUMPWorkbench.getInstance().getFrame().getContext()
                .getLayerViewPanel().getJava2DConverter().toShape(geom2);
        return shape2;
    }
*/
    // Reads Geometry form a wkt file. Since the files are from VertexFolder and
    // have a difference
    // in wkt syntax, the text before the wkt geometry specification is excluded
    /*
     * public static Geometry readGeomFromWKT(WorkbenchContext context, String
     * filename, String folder) throws IOException,
     * org.locationtech.jts.io.ParseException { PlugInManager manager =
     * context.getWorkbench().getPlugInManager(); Geometry geometry = null; File
     * pluginDir = manager.getPlugInDirectory();
     * 
     * String wd = pluginDir.getAbsolutePath(); String filenamedir = wd +
     * File.separator + folder + File.separator + filename; String text =
     * readFile(filenamedir); String segments[] = text.split(":"); // Split path
     * into segments using // (:) as separator String wkt =
     * segments[segments.length - 1]; // Grab the last segment
     * org.locationtech.jts.io.WKTReader wktReader = new WKTReader();// Create
     * // a // WKTReader geometry = wktReader.read(wkt);// Read the geometry
     * return geometry; }
     */
    /*
     * Use Scanner to read the content of a file
     */
    @Deprecated
    public static String readFile(String pathname) throws IOException {

        File file = new File(pathname);
        StringBuilder fileContents = new StringBuilder((int) file.length());
        Scanner scanner = new Scanner(file);
        String lineSeparator = System.getProperty("line.separator");

        try {
            while (scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine() + lineSeparator);
            }
            return fileContents.toString();
        } finally {
            scanner.close();
        }
    }

    /*
     * List of embedded geometries
     */
    @Deprecated
    public static Geometry circle() {
        Circle circle = new Circle(new Coordinate(0, 0), 8);
        return circle.getPoly();
    }

  @Deprecated
    public static Geometry triangle() {
        Coordinate[] coords = new Coordinate[] { new Coordinate(0, 0),
                new Coordinate(16, 0), new Coordinate(8, 13.85),
                new Coordinate(0, 0) };
        LinearRing ring = new GeometryFactory().createLinearRing(coords);
        Geometry geo = new GeometryFactory().createPolygon(ring, null);
        return geo;
    }

  @Deprecated
    public static Geometry square() {
        Coordinate[] coords = new Coordinate[] { new Coordinate(0, 0),
                new Coordinate(0, 16), new Coordinate(16, 16),
                new Coordinate(16, 0), new Coordinate(0, 0) };
        LinearRing ring = new GeometryFactory().createLinearRing(coords);
        Geometry geo = new GeometryFactory().createPolygon(ring, null);
        return geo;
    }

  @Deprecated
    public static Geometry cross() {
        Coordinate[] coords = new Coordinate[] { new Coordinate(5, 0),
                new Coordinate(10, 0), new Coordinate(10, 5),
                new Coordinate(15, 5), new Coordinate(15, 10),
                new Coordinate(10, 10), new Coordinate(10, 15),
                new Coordinate(5, 15), new Coordinate(5, 10),
                new Coordinate(0, 10), new Coordinate(0, 5),
                new Coordinate(5, 5), new Coordinate(5, 0) };

        LinearRing ring = new GeometryFactory().createLinearRing(coords);
        Geometry geo = new GeometryFactory().createPolygon(ring, null);
        return geo;
    }

  @Deprecated
    public static Geometry star() {
        Coordinate[] coords = new Coordinate[] { new Coordinate(4, 6),
                new Coordinate(1, 6), new Coordinate(3, 4),
                new Coordinate(2, 1), new Coordinate(5, 3),
                new Coordinate(8, 1), new Coordinate(7, 4),
                new Coordinate(9, 6), new Coordinate(6, 6),
                new Coordinate(5, 9), new Coordinate(4, 6) };

        LinearRing ring = new GeometryFactory().createLinearRing(coords);
        Geometry geo = new GeometryFactory().createPolygon(ring, null);
        GeometryUtils.scaleGeometry(geo, 200);
        return geo;
    }

  @Deprecated
    public static Geometry star_old() {
        Coordinate[] coords = new Coordinate[] { new Coordinate(3.82, 0.0),
                new Coordinate(5, 6.88), new Coordinate(0.0, 11.76),
                new Coordinate(6.92, 12.76), new Coordinate(10, 19.02),
                new Coordinate(13.08, 12.76), new Coordinate(20, 11.76),
                new Coordinate(15, 6.88), new Coordinate(17.8, 0),
                new Coordinate(10, 3.24), new Coordinate(3.82, 0.0) };

        LinearRing ring = new GeometryFactory().createLinearRing(coords);
        Geometry geo = new GeometryFactory().createPolygon(ring, null);
        return geo;
    }

  @Deprecated
    public static Geometry star_test() {

        int s = 10;
        int x0 = 0;
        int y0 = 0;
        double sin36 = Math.sin(Math.toRadians(36));
        double cos36 = Math.cos(Math.toRadians(36));
        double sin18 = Math.sin(Math.toRadians(18));
        double cos18 = Math.cos(Math.toRadians(18));
        int smallRadius = (int) (s * sin18 / Math.sin(Math.toRadians(54)));

        int p0X = x0;
        int p0Y = y0 - s;
        int p1X = x0 + (int) (smallRadius * sin36);
        int p1Y = y0 - (int) (smallRadius * cos36);
        int p2X = x0 + (int) (s * cos18);
        int p2Y = y0 - (int) (s * sin18);
        int p3X = x0 + (int) (smallRadius * cos18);
        int p3Y = y0 + (int) (smallRadius * sin18);
        int p4X = x0 + (int) (s * sin36);
        int p4Y = y0 + (int) (s * cos36);
        int p5Y = y0 + smallRadius;
        int p6X = x0 - (int) (s * sin36);
        int p7X = x0 - (int) (smallRadius * cos18);
        int p8X = x0 - (int) (s * cos18);
        int p9X = x0 - (int) (smallRadius * sin36);

        Coordinate[] coords = new Coordinate[] { new Coordinate(p0X, p0Y),
                new Coordinate(p1X, p1Y), new Coordinate(p2X, p2Y),
                new Coordinate(p3X, p3Y), new Coordinate(p4X, p4Y),
                new Coordinate(p0X, p5Y),

                new Coordinate(p6X, p4Y), new Coordinate(p7X, p3Y),
                new Coordinate(p8X, p2Y), new Coordinate(p9X, p1Y),

                new Coordinate(p0X, p0Y) };
        LinearRing ring = new GeometryFactory().createLinearRing(coords);
        Geometry geo = new GeometryFactory().createPolygon(ring, null);
        return geo;

    }

    // Get the geometry
    /*
     * public static Geometry getGeometry(WorkbenchContext context) throws
     * IOException, ParseException { Geometry geom2 = null; if
     * (BlockPanel.blockBox.getSelectedItem().toString()
     * .equals(BlockPanel.triangle)) { geom2 = BlockUtils.triangle(); } else if
     * (BlockPanel.blockBox.getSelectedItem().toString()
     * .equals(BlockPanel.square)) { geom2 = BlockUtils.square(); } else if
     * (BlockPanel.blockBox.getSelectedItem().toString()
     * .equals(BlockPanel.circle)) { geom2 = BlockUtils.circle(); } else if
     * (BlockPanel.blockBox.getSelectedItem().toString()
     * .equals(BlockPanel.cross)) { geom2 = BlockUtils.cross(); } else if
     * (BlockPanel.blockBox.getSelectedItem().toString()
     * .equals(BlockPanel.star)) { geom2 = BlockUtils.star(); }
     * 
     * else { String blockName = (String) BlockPanel.blockBox.getSelectedItem()
     * + ".wkt"; geom2 = readGeomFromWKT(context, blockName,
     * BlockPanel.blockFolder); } return geom2; }
     */
    @Deprecated
    public static ImageIcon createIcon(Shape shape) throws IOException,
            ParseException, NoninvertibleTransformException {

        Rectangle r = shape.getBounds();
        BufferedImage image = new BufferedImage(r.width, r.height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.draw(shape);
        // move the shape in the region of the image
        g.translate(-r.x, -r.y);

        g.setPaint(Color.red);

        g.draw(shape);
        g.dispose();
        ImageIcon icon = new ImageIcon(image);

        return icon;
    }

  @Deprecated
    public ImageIcon creatIconFromShape(Shape s) {

        Rectangle r = s.getBounds();
        BufferedImage image = new BufferedImage(20, 20,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.fill(s);
        // move the shape in the region of the image
        g.translate(-r.x, -r.y);

        g.setPaint(Color.red);

        g.dispose();
        ImageIcon icon = new ImageIcon(image);

        return icon;
    }

  @Deprecated
    public static Shape generateShapeFromText(Font font, String string) {
        BufferedImage img = new BufferedImage(24, 24,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        try {
            GlyphVector vect = font.createGlyphVector(
                    g2.getFontRenderContext(), string);
            Shape shape = vect.getOutline(0f, (float) -vect.getVisualBounds()
                    .getY());

            return shape;
        } finally {
            g2.dispose();
        }
    }

}
