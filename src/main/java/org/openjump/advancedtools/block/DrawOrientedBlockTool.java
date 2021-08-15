package org.openjump.advancedtools.block;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.io.ParseException;
import org.openjump.advancedtools.tools.ConstrainedNClickTool;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.CoordinateListMetrics;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.FeatureDrawingUtil;

public class DrawOrientedBlockTool extends ConstrainedNClickTool {

	private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");

	private final FeatureDrawingUtil featureDrawingUtil;
	//public static final String blockFolder = "VertexImages";
	public static final String NAME = i18n
			.get("org.openjump.core.ui.plugins.block.DrawOrientedBlockTool.description");

	String Azimuth = I18N.JUMP.get("ui.cursortool.CoordinateListMetrics.Azimuth");

	private final ImageIcon ICON = org.openjump.advancedtools.icon.IconLoader.icon("textblock/block_drag.png");

	final BlockPanel blockPanel;

	public DrawOrientedBlockTool(FeatureDrawingUtil featureDrawingUtil, BlockPanel blockPanel) {
		super(JUMPWorkbench.getInstance().getContext(), 2);
		this.featureDrawingUtil = featureDrawingUtil;
		setColor(Color.blue);
		setStroke(new BasicStroke(1.5F));
		allowSnapping();
		this.blockPanel = blockPanel;
	}

	double angle;
	//double azimuth;
	CoordinateListMetrics metrics = new CoordinateListMetrics();

	public int getDegreesAzimuth() {
		this.angle = (Math.round(metrics.azimuth(getCoordinates())) * 10000.0D) / 10000.0D;
		return (int) this.angle;
	}

	public int getDegreesAngle() {
		this.angle = (Math.round(metrics.angle(getCoordinates())) * 10000.0D) / 10000.0D;
		return (int) this.angle;
	}

	public static CursorTool create(LayerNamePanelProxy layerNamePanelProxy, BlockPanel blockPanel) {
		FeatureDrawingUtil featureDrawingUtil = new FeatureDrawingUtil(layerNamePanelProxy);

		return featureDrawingUtil.prepare(new DrawOrientedBlockTool(featureDrawingUtil, blockPanel), true);
	}

	public void initialize(PlugInContext context) throws Exception {
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	@SuppressWarnings({})
	public void gestureFinished() throws Exception {
		getWorkbench().getContext();

		// If the mouse is released before the scond click, do nothing
		if (getCoordinates().size() == 0)
			return;

		// Build the geometry at the specific displacement, dimension and
		// rotation

		Coordinate cursorPt = (Coordinate) getCoordinates().get(0);
		double angle_1;
		if (getCoordinates().size() == 1) {
			angle_1 = 0;
		} else {
			angle_1 = azimuth(cursorPt, tentativeCoordinate);
		}

		// Geometry geom2 = BlockUtils.getGeometry();
		Geometry geom2 = blockPanel.getSelection();

		// Get the centroid coordinates of the geometry
		Coordinate coord = geom2.getCentroid().getCoordinate();
		// Calculate the dsplacement of the geometry
		Coordinate displacement = CoordUtil.subtract(cursorPt, coord);

		GeometryUtils.rotateGeometry(geom2, angle_1);
		// Integer rotation = (Integer) BlockPanel.rotationSpinner.getValue();
		Integer dimension = (Integer) BlockPanel.dimensionSpinner.getValue();
		GeometryUtils.scaleGeometry(geom2, dimension);
		GeometryUtils.centerGeometry(geom2, displacement);
		// Get the selected layer if layer doesn't exist, add new layer

		getPanel().setViewportInitialized(true);
		execute(featureDrawingUtil.createAddCommand(geom2, isRollingBackInvalidEdits(), getPanel(), this));

	}

	@Override
	public ImageIcon getIcon() {
		return ICON;
	}

	@Override
	public void mouseLocationChanged(MouseEvent e) {
		try {
			if (isShapeOnScreen()) {
				super.mouseLocationChanged(e);
				redrawShape();
				ArrayList<Coordinate> currentCoordinates = new ArrayList<>();
				currentCoordinates.add(getPanel().getViewport().toModelCoordinate(e.getPoint()));
				display(getPanel());
			}

			// }
		} catch (Throwable t) {
			getPanel().getContext().handleThrowable(t);
		}
	}

	public static DecimalFormat df2 = new DecimalFormat("##0.0##");

	private void display(LayerViewPanel panel) {
		if (isShapeOnScreen()) {
			Coordinate cursorPt = (Coordinate) getCoordinates().get(0);

			panel.getContext().setStatusMessage(

					Azimuth + ": " + df2.format(azimuth(cursorPt, tentativeCoordinate)) + "\u00B0");
		}
	}

	private Shape selectedFeaturesShape;

	/**
	 * called from constructor and by mouse move event
	 * <p>
	 * calculates a geometry around the mouse pointer and converts it to a java
	 * shape
	 * 
	 * @param panel
	 */
	private void calculateShape(LayerViewPanel panel) {
		Coordinate cursorPt = (Coordinate) getCoordinates().get(0);
		double angle_1;
		if (getCoordinates().size() == 1) {
			angle_1 = azimuth(cursorPt, this.modelDestination);
		} else {
			angle_1 = azimuth(cursorPt, tentativeCoordinate);
		}
		getWorkbench().getContext();

		Geometry geom2 = blockPanel.getSelection();

		// Get the centroid of the geometry
		Coordinate coord = geom2.getCentroid().getCoordinate();

		// Calculate the displacement of the geometry
		Coordinate displacement = CoordUtil.subtract(cursorPt, coord);
		Double rotation = angle_1;
		Integer dimension = (Integer) BlockPanel.dimensionSpinner.getValue();
		GeometryUtils.rotate_clockwise_Geometry(geom2, rotation);
		GeometryUtils.scaleGeometry(geom2, dimension);
		GeometryUtils.centerGeometry(geom2, displacement);
		try {

			this.selectedFeaturesShape = panel.getJava2DConverter().toShape(geom2);

		} catch (NoninvertibleTransformException e) {
			System.out.println("DrawCircleWithGivenRadiusTool:Exception " + e);
		}
	}

	private void calculateShape_0(Coordinate middlePoint, LayerViewPanel panel) throws IOException, ParseException {
		getWorkbench().getContext();
		// Get the click coordinates
		Coordinate cursorPt = this.modelDestination;
		// Geometry geom2 = BlockUtils.getGeometry(context);
		// Geometry geom2 = BlockUtils.getGeometry();
		Geometry geom2 = blockPanel.getSelection();

		// Get the centroid coordinates of the geometry
		Coordinate coord = geom2.getCentroid().getCoordinate();

		// Calculate the displacement of the geometry
		Coordinate displacement = CoordUtil.subtract(cursorPt, coord);
		Integer dimension = (Integer) BlockPanel.dimensionSpinner.getValue();
		GeometryUtils.rotate_clockwise_Geometry(geom2, 0);
		GeometryUtils.scaleGeometry(geom2, dimension);
		GeometryUtils.centerGeometry(geom2, displacement);

		try {
			this.selectedFeaturesShape = panel.getJava2DConverter().toShape(geom2);
		} catch (NoninvertibleTransformException e) {
			System.out.println("DrawCircleWithGivenRadiusTool:Exception " + e);
		}
	}

	/**
	 * changed to get geometry around mouse pointer
	 */
	@Override
	protected Shape getShape() throws NoninvertibleTransformException {
		GeneralPath path = new GeneralPath();
		this.setColor(Color.RED.brighter());
		// this.setFilling(true);
		// this.setStroke(new BasicStroke(1));

		try {
			if (getCoordinates().size() == 0) {
				this.calculateShape_0(this.modelDestination, this.getPanel());
				path.append(this.selectedFeaturesShape, false);
			} else if (getCoordinates().size() == 1) {

				Coordinate a = (Coordinate) coordinates.get(0);
				// String Azimuth = df2.format(azimuth(a, this.modelDestination)) + "�";
				// Font f1 = new Font("Arial Black", Font.ITALIC, 20);
				Shape shape = BlockCell.getShape(blockPanel.getSelection());

				this.calculateShape(this.getPanel());
				// this.calculateShape_0(a, this.getPanel());
				Point2D firstPoint = getPanel().getViewport().toViewPoint(a);
				path.moveTo((float) firstPoint.getX(), (float) firstPoint.getY());

				Coordinate b = this.modelDestination;
				Point2D secondPoint = getPanel().getViewport().toViewPoint(b);
				path.lineTo((float) secondPoint.getX(), (float) secondPoint.getY());
				LineSegment ls = new LineSegment(a, b);
				double length = ls.getLength();
				double dy = a.y + length;
				Coordinate c = new Coordinate(a.x, dy);
				Point2D thirdPoint = getPanel().getViewport().toViewPoint(c);
				path.moveTo((float) firstPoint.getX(), (float) firstPoint.getY());
				path.lineTo((float) thirdPoint.getX(), (float) thirdPoint.getY());
				this.calculateShape(this.getPanel());
				AffineTransform at = new AffineTransform();
				at.translate(thirdPoint.getX(), thirdPoint.getY());
				at.createTransformedShape(shape);
				if (this.wasControlPressed()) {
					path.append(at.createTransformedShape(shape), false);
				}
				path.append(this.selectedFeaturesShape, false);
			} else {

				Coordinate a = (Coordinate) coordinates.get(0);
				// String Azimuth = df2.format(azimuth(a, tentativeCoordinate)) + "�";
				// Font f1 = new Font("Arial Black", Font.ITALIC, 20);

				Shape text = BlockCell.getShape(blockPanel.getSelection());

				Point2D firstPoint = getPanel().getViewport().toViewPoint(a);
				path.moveTo((float) firstPoint.getX(), (float) firstPoint.getY());

				Coordinate b = tentativeCoordinate;
				Point2D secondPoint = getPanel().getViewport().toViewPoint(b);
				path.lineTo((float) secondPoint.getX(), (float) secondPoint.getY());
				LineSegment ls = new LineSegment(a, b);
				double length = ls.getLength();
				double dy = a.y + length;
				Coordinate c = new Coordinate(a.x, dy);
				Point2D thirdPoint = getPanel().getViewport().toViewPoint(c);
				path.moveTo((float) firstPoint.getX(), (float) firstPoint.getY());
				path.lineTo((float) thirdPoint.getX(), (float) thirdPoint.getY());

				AffineTransform at = new AffineTransform();
				at.translate(thirdPoint.getX(), thirdPoint.getY());
				at.createTransformedShape(text);
				this.calculateShape(this.getPanel());
				if (this.wasControlPressed()) {
					path.append(at.createTransformedShape(text), false);
				}
				path.append(this.selectedFeaturesShape, false);
			}
		} catch (IOException | ParseException e) {
			Logger.warn(e);
			e.printStackTrace();
		}

		return path;

	}

	/**
	 * Computes the angle facing North betwee 2 points
	 *
	 * @param startPt start point
	 * @param endPt end point
	 * @return the angle in degrees
	 */
	public static double azimuth(Coordinate startPt, Coordinate endPt) {

		LineSegment ls = new LineSegment(startPt, endPt);
		double d = ls.angle();
		double DEG = 90.0D - d * 57.295779513082323D;
		double DEG1 = DEG;
		if (DEG < 0.0D)
			DEG1 += 360.0D;
		return DEG1;
	}

	public static double angle(Coordinate startPt, Coordinate endPt) {
		Coordinate r = new Coordinate(endPt.x - startPt.x, endPt.y - startPt.y);
		double rMag = Math.sqrt(r.x * r.x + r.y * r.y);
		if (rMag == 0.0) {
			return 0.0;
		} else {
			double rCos = r.x / rMag;
			double rAng = Math.acos(rCos);

			if (r.y < 0.0)
				rAng = -rAng;
			return rAng * 360.0 / (2 * Math.PI);
		}
	}

	/**** from drag tool ***/
	protected void setViewDestination(Point2D destination) throws NoninvertibleTransformException {
		this.setModelDestination(getPanel().getViewport().toModelCoordinate(destination));
	}

	protected Coordinate modelDestination = null;

	protected void setModelDestination(Coordinate destination) {
		this.modelDestination = snap(destination);
	}

	/**
	 * overwritten super method to show the block geometry on any mouse move
	 */

	@Override
	public void mouseMoved(MouseEvent e) {
		try {
			setViewDestination(e.getPoint());
			redrawShape();
		} catch (Throwable t) {
			getPanel().getContext().handleThrowable(t);
		}
	}

	// the Timer for checking a double click
	//private static Timer doubleClickTimer;

}
