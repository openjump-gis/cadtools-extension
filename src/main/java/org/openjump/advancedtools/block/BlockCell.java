package org.openjump.advancedtools.block;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.ui.renderer.java2D.Java2DConverter;
import org.openjump.core.geomutils.Circle;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/**
 * Thin wrapper containing the definition of a block (geometry), and how to show it
 * in a ComboBox (name and icon).
 */
public class BlockCell {

  // TODO: language strings are located in OJ core. Why?
  public static BlockCell CIRCLE = new BlockCell(
          I18N.JUMP.get("deejump.ui.style.RenderingStylePanel.circle"),
          new Circle(new Coordinate(0, 0), 8).getPoly());
  public static BlockCell TRIANGLE = new BlockCell(
          I18N.JUMP.get("deejump.ui.style.RenderingStylePanel.triangle"),
          "POLYGON((0 0, 16 0, 8 13.85, 0 0))");
  public static BlockCell SQUARE = new BlockCell(
          I18N.JUMP.get("deejump.ui.style.RenderingStylePanel.square"),
          "POLYGON((0 0, 16 0, 16 16, 0 16,  0 0))");
  public static BlockCell CROSS = new BlockCell(
          I18N.JUMP.get("deejump.ui.style.RenderingStylePanel.cross"),
          "POLYGON((5 0, 10 0, 10 5, 15 5, 15 10, 10 10, 10 15, 5 15, 5 10, 0 10, 0 5, 5 5, 5 0))");
  public static BlockCell STAR = new BlockCell(
          I18N.JUMP.get("deejump.ui.style.RenderingStylePanel.star"),
          "POLYGON((4 6, 1 6, 3 4, 2 1, 5 3, 8 1, 7 4, 9 6, 6 6, 5 9, 4 6))");


  private String name;
  private Geometry geometry;
  private Icon icon;

  BlockCell(String name, Geometry geometry) {
    try {
      this.name = name;
      this.geometry = geometry;
      this.icon = createIcon(geometry);
    } catch (NoninvertibleTransformException e) {}
  }

  BlockCell(String name, String wkt) {
    try {
      this.name = name;
      this.geometry = new WKTReader().read(wkt);
      this.icon = createIcon(geometry);
    }
    catch (NoninvertibleTransformException | ParseException e) {}
  }

  BlockCell(File file) throws IOException, ParseException, NoninvertibleTransformException {
    if (!file.isFile()) throw new IOException(file.getName() + " is not a valid file");
    String fileName = file.getName();
    if (!fileName.toUpperCase().endsWith(".WKT")) throw new IOException(fileName + " is not a valid filename for a WKT");
    name = fileName.substring(0, fileName.lastIndexOf('.'));
    String fileContent = readFile(file);
    String[] segments = fileContent.split(":"); // Split into segments using : as separator
    String wkt = segments[segments.length - 1]; // Grab the last segment
    geometry = new WKTReader().read(wkt);
    icon = createIcon(geometry);
  }

  public String toString() {
    return name;
  }

  public Geometry getGeometry() {
    return geometry;
  }

  public Icon getIcon() {
    return icon;
  }

  private String readFile(File file) throws FileNotFoundException {
    StringBuilder fileContents = new StringBuilder((int) file.length());
    Scanner scanner = new Scanner(file);
    String lineSeparator = System.getProperty("line.separator");
    try {
      while (scanner.hasNextLine()) {
        fileContents.append(scanner.nextLine());
        if (scanner.hasNextLine()) fileContents.append(lineSeparator);
      }
      return fileContents.toString();
    } finally {
      scanner.close();
    }
  }

  /**
   * Returns the Geometry as a Shape centered on the Geometry centroid
   * @param geometry to convert
   * @return a awt.Shape representing the Geometry
   */
  public static Shape getShape(Geometry geometry) throws NoninvertibleTransformException {
    final Coordinate c = geometry.getCentroid().getCoordinate();
    Java2DConverter java2DConverter = new Java2DConverter(new Java2DConverter.PointConverter() {
      public Point2D toViewPoint(Coordinate modelCoordinate) {
        return new Point2D.Double((modelCoordinate.x-c.x), (modelCoordinate.y-c.y));
      }
      public double getScale() { return 1.0;}
      public Envelope getEnvelopeInModelCoordinates() {
        return new Envelope(0.0, 20, 0.0, 20);
      }
    });
    return java2DConverter.toShape(geometry);
  }

  /** Create an icon from the Geometry.*/
  private ImageIcon createIcon(Geometry geometry) throws NoninvertibleTransformException {
    final Envelope env = geometry.getEnvelopeInternal();
    final double max = Math.max(env.getWidth(), env.getHeight());
    Java2DConverter java2DConverter = new Java2DConverter(new Java2DConverter.PointConverter() {
      public Point2D toViewPoint(Coordinate modelCoordinate) {
        return new Point2D.Double((modelCoordinate.x-env.getMinX()) * getScale(), 20.0 - (modelCoordinate.y-env.getMinY()) * getScale());
      }
      public double getScale() { return 20.0/max;}
      public Envelope getEnvelopeInModelCoordinates() {
        return new Envelope(0.0, 20, 0.0, 20);
      }
    });
    Shape shape = java2DConverter.toShape(geometry);
    //Rectangle r = shape.getBounds();
    BufferedImage image = new BufferedImage(22, 22, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = image.createGraphics();
    g.draw(shape);
    // move the shape in the region of the image
    //g.translate(10, 10);
    g.setPaint(Color.red);
    g.draw(shape);
    g.fill(shape);
    g.dispose();
    return new ImageIcon(image);
  }

}
