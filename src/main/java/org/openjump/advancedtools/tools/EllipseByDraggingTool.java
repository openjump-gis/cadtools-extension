package org.openjump.advancedtools.tools;

import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.openjump.advancedtools.config.CADToolsOptionsPanel;
import org.openjump.advancedtools.icon.IconLoader;
import org.openjump.advancedtools.utils.CoordinateListMetricsUtils;

import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DragTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.FeatureDrawingUtil;

public class EllipseByDraggingTool extends DragTool {

	private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");

	private FeatureDrawingUtil featureDrawingUtil;

	//static final String drawConstrainedCircle = I18N.get(
	//		"org.openjump.core.ui.plugins.edittoolbox.cursortools.DrawConstrainedCircleTool.Draw-Constrained-Circle");
	//static final String theCircleMustHaveAtLeast2Points = I18N.get(
	//		"org.openjump.core.ui.plugins.edittoolbox.cursortools.DrawConstrainedCircleTool.The-circle-must-have-at-least-2-points");

	/** Plugin name */
	public final static String NAME = i18n.get("org.openjump.core.ui.plugins.Ellipse");

	//public static final String Area = I18N.get("ui.cursortool.CoordinateListMetrics.Area");
	//public static final String Diameter = I18N.get("ui.cursortool.CoordinateListMetrics.Diameter");

	protected Coordinate tentativeCoordinate;

	public EllipseByDraggingTool() {
		super(JUMPWorkbench.getInstance().getContext());
	}

	private EllipseByDraggingTool(FeatureDrawingUtil featureDrawingUtil) {
		super(JUMPWorkbench.getInstance().getContext());
		this.featureDrawingUtil = featureDrawingUtil;
	}

	public EllipseByDraggingTool(EnableCheckFactory checkFactory) {
		super(JUMPWorkbench.getInstance().getContext());
	}

	public static CursorTool create(LayerNamePanelProxy layerNamePanelProxy) {
		FeatureDrawingUtil featureDrawingUtil = new FeatureDrawingUtil(layerNamePanelProxy);

		return featureDrawingUtil.prepare(new EllipseByDraggingTool(featureDrawingUtil), true);
	}

	private void display(List coordinates, LayerViewPanel panel) throws NoninvertibleTransformException {

		Envelope e = getBoxInModelCoordinates();

		Coordinate[] coordHeight = new Coordinate[] { new Coordinate(e.getMinX(), e.getMinY()),
				new Coordinate(e.getMinX(), e.getMaxY()) };
		Coordinate[] coordWidth = new Coordinate[] { new Coordinate(e.getMinX(), e.getMinY()),
				new Coordinate(e.getMaxX(), e.getMinY()) };

		LineString lineVertical = new GeometryFactory().createLineString(coordHeight);

		LineString lineHorizontal = new GeometryFactory().createLineString(coordWidth);

		double lengthVertical = lineVertical.getLength() / 2;
		double lengthHorizontal = lineHorizontal.getLength() / 2;
		double mean = (lengthVertical + lengthHorizontal) / 2;
		double circumference = 2 * Math.PI * (mean);
		double area = Math.PI * Math.pow(mean, 2);

		CoordinateListMetricsUtils.setEllipseMessage(lengthHorizontal, lengthVertical, circumference, area);
	}

	public void mouseLocationChanged(MouseEvent e) {
		try {
			if (isShapeOnScreen()) {
				ArrayList<Coordinate> currentCoordinates = new ArrayList<>(getEllipseCoordinates());
				currentCoordinates.add(getPanel().getViewport().toModelCoordinate(e.getPoint()));
				display(getEllipseCoordinates(), getPanel());
				// getPanel().getContext().setStatusMessage("");
			}
			snap(e.getPoint());
			// super.mousePressed(e);
			super.mouseDragged(e);

		} catch (Throwable t) {
			getPanel().getContext().handleThrowable(t);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		try {
			if (isShapeOnScreen()) {
				ArrayList<Coordinate> currentCoordinates = new ArrayList<>(getEllipseCoordinates());
				currentCoordinates.add(getPanel().getViewport().toModelCoordinate(e.getPoint()));
				display(currentCoordinates, getPanel());
				// getPanel().getContext().setStatusMessage("");
			}
			snap(e.getPoint());
			// super.mousePressed(e);
			super.mouseDragged(e);

		} catch (Throwable t) {
			getPanel().getContext().handleThrowable(t);
		}
	}

	@Override
	public String getName() {

		String tooltip;
		tooltip = "<HTML><BODY>";

		tooltip += "<b>" + NAME + "</b>";

		tooltip += "</BODY></HTML>";
		return tooltip;
	}

	@Override
	public Icon getIcon() {
		return IconLoader.icon("drawEllipse.png");
	}

	@Override
	protected void gestureFinished() throws Exception {
		reportNothingToUndoYet();
		getPanel().setViewportInitialized(true);

		if (CADToolsOptionsPanel.isGetAsPolygon()) {
			execute(featureDrawingUtil.createAddCommand(getEllipsePolygon(), isRollingBackInvalidEdits(), getPanel(),
					this));
		} else {
			execute(featureDrawingUtil.createAddCommand(getEllipseLineString(), isRollingBackInvalidEdits(), getPanel(),
					this));
		}

		if (CADToolsOptionsPanel.isGetCentroid()) {
			execute(featureDrawingUtil.createAddCommand(getCenter(), isRollingBackInvalidEdits(), getPanel(), this));
		}

	}

	protected Geometry getEllipsePolygon() {
		Envelope env = new Envelope(getModelSource().x, getModelDestination().x, getModelSource().y,
				getModelDestination().y);
		GeometricShapeFactory gsf = new GeometricShapeFactory();
		gsf.setNumPoints(100);
		gsf.setEnvelope(env);

		return gsf.createCircle();
	}

	protected Geometry getEllipseLineString() {
		Envelope env = new Envelope(getModelSource().x, getModelDestination().x, getModelSource().y,
				getModelDestination().y);
		GeometricShapeFactory gsf = new GeometricShapeFactory();
		gsf.setNumPoints(100);
		gsf.setEnvelope(env);

		Geometry geom = gsf.createCircle();
		Coordinate[] coords = geom.getCoordinates();
		GeometryFactory factory = new GeometryFactory();

		return factory.createLineString(coords);
	}

	protected Geometry getCenter() throws NoninvertibleTransformException {
		Envelope env = new Envelope(getModelSource().x, getModelDestination().x, getModelSource().y,
				getModelDestination().y);
		GeometricShapeFactory gsf = new GeometricShapeFactory();
		gsf.setNumPoints(100);
		gsf.setEnvelope(env);

		return gsf.createCircle().getCentroid();
	}

	@Override
	protected Shape getShape(Point2D source, Point2D destination) throws Exception {
		GeneralPath shape = new GeneralPath();
		double minX = Math.min(source.getX(), destination.getX());
		double minY = Math.min(source.getY(), destination.getY());
		double maxX = Math.max(source.getX(), destination.getX());
		double maxY = Math.max(source.getY(), destination.getY());
		double width = Math.max(source.getX(), destination.getX()) - Math.min(source.getX(), destination.getX());
		double height = Math.max(source.getY(), destination.getY()) - Math.min(source.getY(), destination.getY());

		shape.append(new Ellipse2D.Double(minX, minY, width, height), true);
		shape.moveTo(minX, (maxY + minY) / 2.0D);
		shape.lineTo(maxX, (maxY + minY) / 2.0D);
		shape.moveTo((maxX + minX) / 2.0D, minY);
		shape.lineTo((maxX + minX) / 2.0D, maxY);
		return shape;
	}

	public CoordinateList getEllipseCoordinates() {

		Envelope env = new Envelope(getModelSource().x, getModelDestination().x, getModelSource().y,
				getModelDestination().y);
		GeometricShapeFactory gsf = new GeometricShapeFactory();
		gsf.setNumPoints(100);
		gsf.setEnvelope(env);

		Geometry geom = gsf.createCircle();
		Coordinate[] coords = geom.getCoordinates();
		GeometryFactory factory = new GeometryFactory();

		LineString line = factory.createLineString(coords);

		CoordinateList coordinates = new CoordinateList();

		coordinates.add(line.getCoordinates(), true);

		return coordinates;
	}

}
