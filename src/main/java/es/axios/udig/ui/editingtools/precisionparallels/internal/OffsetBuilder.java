/* Spatial Operations & Editing Tools for uDig
 * 
 * Axios Engineering under a funding contract with: 
 *      Diputación Foral de Gipuzkoa, Ordenación Territorial 
 *
 *      http://b5m.gipuzkoa.net
 *      http://www.axios.es 
 *
 * (C) 2006, Diputación Foral de Gipuzkoa, Ordenación Territorial (DFG-OT). 
 * DFG-OT agrees to licence under Lesser General Public License (LGPL).
 * 
 * You can redistribute it and/or modify it under the terms of the 
 * GNU Lesser General Public License as published by the Free Software 
 * Foundation; version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package es.axios.udig.ui.editingtools.precisionparallels.internal;

import java.util.ArrayList;

import org.locationtech.jts.algorithm.Intersection;
import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Position;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * <p>
 * 
 * <pre>
 * Class responsible of calculating the parallel curve.
 * 
 * Receives input coordinates, start position and offset position.
 * Calculate the parallel curve also called the offset curve.
 * 
 * Input coordinates: An array with the coordinates of the geometry.
 *  
 * Start position: The position of where a click have been done,
 * based on the direction of the line. It will be RIGHT or LEFT.
 * 
 * Offset position: Based on the reference line, the position of the parallel curve,
 * that means, if it's inside or outside the reference line.
 * </pre>
 * 
 * </p>
 * 
 * 
 * @author Aritz Davila (www.axios.es)
 * @author Mauricio Pazos (www.axios.es)
 */
public class OffsetBuilder {

	/**
	 * The position of the offset curve.
	 */
	public static OffsetPosition CURRENT_POSITION = OffsetPosition.POSITION_UPPER;
	/**
	 * The angle quantum with which to approximate a fillet curve (based on the
	 * input # of quadrant segments)
	 */
	private double filletAngleQuantum;

	private static final double MIN_CURVE_VERTEX_FACTOR = 1.0E-6;

	private double distance = 0.0;

	private OffsetVertexList vertexList;
	private LineIntersector li;

	private Coordinate s0, s1, s2;
	private LineSegment seg0 = new LineSegment();
	private LineSegment seg1 = new LineSegment();
	private LineSegment offset0 = new LineSegment();
	private LineSegment offset1 = new LineSegment();

	private int side = 0;
	private Boolean lastOutsideTurn = null;

	private PrecisionModel precisionModel;
	/**
	 * Start position of the offset curve respect the reference line and its
	 * direction.
	 */
	private int startPosition;

	/**
	 * <p>
	 * The position of the offset.
	 * <li>UPPER means that the generated offset curve will be outside the reference
	 * line turn.</li>
	 * <li>UNDER means that the generated offset curve will be inside the reference
	 * line turn.</li>
	 * </p>
	 */
	public static enum OffsetPosition {
		POSITION_UPPER, POSITION_UNDER,
	}

	/**
	 * <p>
	 * Creates the offset builder.
	 * </p>
	 * <p>
	 * 
	 * </p>
	 * 
	 * @param offsetPosition The position of the offset(outside curve or inside).
	 * @param startPosition  The position of the offset respect the segment and its
	 *                       direction(Left or right).
	 */
	public OffsetBuilder(OffsetPosition offsetPosition, int startPosition) {

		CURRENT_POSITION = offsetPosition;
		this.startPosition = startPosition;
		this.precisionModel = new PrecisionModel();

		// compute intersections in full precision, to provide accuracy
		// the points are rounded as they are inserted into the curve line
		li = new RobustLineIntersector();
		filletAngleQuantum = Math.PI / 2.0 / 1;
	}

	/**
	 * This method handles single points as well as lines. Lines are assumed to
	 * <b>not</b> be closed (the function will not fail for closed lines, but will
	 * generate superfluous line caps).
	 * 
	 * @return a List of Coordinate[]
	 */
	public ArrayList<Coordinate> getLineCurve(Coordinate[] inputPts, double distance) {

		ArrayList<Coordinate> arrayList = new ArrayList<>();

		if (distance <= 0.0)
			return arrayList;

		init(distance);

		computeParallelLineCurve(inputPts);

		arrayList = vertexList.getList();

		return arrayList;
	}

	/**
	 * Test method
	 * 
	 * @return the initial Coordinate of the list
	 */
	public Coordinate getInitialCoordinate() {

		return vertexList.getInitialCoor();
	}

	private void init(double distance) {

		this.distance = distance;
		vertexList = new OffsetVertexList();
		vertexList.setPrecisionModel(precisionModel);
		/**
		 * Choose the min vertex separation as a small fraction of the offset distance.
		 */
		vertexList.setMinimumVertexDistance(distance * MIN_CURVE_VERTEX_FACTOR);
	}

	private void computeParallelLineCurve(Coordinate[] inputPts) {

		// TODO when it is interior offset and the distance is higher than
		// the distance to the centroid,
		// don't draw, or draw correctly because is doing something wrong.
		int n = inputPts.length - 1;

		if (CURRENT_POSITION == OffsetPosition.POSITION_UPPER) {
			// compute points for left side of line
			initSideSegments(inputPts[0], inputPts[1], startPosition);
			for (int i = 2; i <= n; i++) {
				addNextSegment(inputPts[i], true);
			}
			addLastSegmentCW();
		} else { // When CURRENT_POSITION = OffsetPosition.POSITON_UNDER

			// compute points for right side of line
			initSideSegments(inputPts[n], inputPts[n - 1], startPosition);
			for (int i = n - 2; i >= 0; i--) {
				addNextSegment(inputPts[i], true);
			}
			addLastSegmentCCW();
		}
	}

	/**
	 * Add last offset point when is out side the turn.
	 */
	private void addLastSegmentCW() {

		if ((lastOutsideTurn != null && lastOutsideTurn) || vertexList.size() == 0) {
			vertexList.addPt(offset1.p0, false);
		}
		vertexList.addPt(offset1.p1, false);
	}

	/**
	 * Add last offset point when is inside the turn.
	 */
	private void addLastSegmentCCW() {

		if ((lastOutsideTurn != null && lastOutsideTurn) || vertexList.size() == 0) {
			vertexList.addPt(offset1.p0, false);
		}
		vertexList.addPt(offset1.p1, false);
	}

	private void initSideSegments(Coordinate s1, Coordinate s2, int side) {
		this.s1 = s1;
		this.s2 = s2;
		this.side = side;
		seg1.setCoordinates(s1, s2);
		computeOffsetSegment(seg1, side, distance, offset1);
	}

	/**
	 * Compute an offset segment for an input segment on a given side and at a given
	 * distance. The offset points are computed in full double precision, for
	 * accuracy.
	 * 
	 * @param seg      the segment to offset
	 * @param side     the side of the segment ({@link Position}) the offset lies on
	 * @param distance the offset distance
	 * @param offset   the points computed for the offset segment
	 */
	private void computeOffsetSegment(LineSegment seg, int side, double distance, LineSegment offset) {
		int sideSign = side == Position.LEFT ? 1 : -1;
		double dx = seg.p1.x - seg.p0.x;
		double dy = seg.p1.y - seg.p0.y;
		double len = Math.sqrt(dx * dx + dy * dy);
		// u is the vector that is the length of the offset, in the direction of
		// the segment
		double ux = sideSign * distance * dx / len;
		double uy = sideSign * distance * dy / len;
		offset.p0.x = seg.p0.x - uy;
		offset.p0.y = seg.p0.y + ux;
		offset.p1.x = seg.p1.x - uy;
		offset.p1.y = seg.p1.y + ux;
	}

	private void addNextSegment(Coordinate p, boolean addStartPoint) {
		// s0-s1-s2 are the coordinates of the previous segment and the current
		// one

		s0 = s1;
		s1 = s2;
		s2 = p;
		seg0.setCoordinates(s0, s1);
		computeOffsetSegment(seg0, side, distance, offset0);
		seg1.setCoordinates(s1, s2);
		computeOffsetSegment(seg1, side, distance, offset1);

		// do nothing if points are equal
		if (s1.equals(s2))
			return;

		int orientation = Orientation.index(s0, s1, s2);
		boolean outsideTurn = (orientation == Orientation.CLOCKWISE && side == Position.LEFT)
				|| (orientation == Orientation.COUNTERCLOCKWISE && side == Position.RIGHT);
		if (lastOutsideTurn == null) {
			lastOutsideTurn = outsideTurn;
		}

		if (orientation == 0) { // lines are collinear
			addCollinear(addStartPoint);
		} else if (outsideTurn) {
			// when outsideTurn and lastOutsideTurn are different don't add the
			// p0
			if (lastOutsideTurn) {
				vertexList.addPt(offset0.p0, false);
			}
			addOutsideTurn(addStartPoint);
		} else { // inside turn
			if (vertexList.size() == 0) {
				vertexList.addPt(offset0.p0, false);
			}
			if (lastOutsideTurn) {
				// add random point, because the last point added was marked
				// with vertexList.addPt(xxxx,TRUE)
				// so this point will be deleted.
				vertexList.addPt(offset0.p0, false);
			}
			addInsideTurn();
		}
		lastOutsideTurn = outsideTurn;
	}

	private void addCollinear(boolean addStartPoint) {
		li.computeIntersection(s0, s1, s1, s2);
		int numInt = li.getIntersectionNum();
		/**
		 * if numInt is < 2, the lines are parallel and in the same direction. In this
		 * case the point can be ignored, since the offset lines will also be parallel.
		 */
		if (numInt >= 2) {
			/**
			 * segments are collinear but reversing. Add an "end-cap" fillet all the way
			 * around to other direction This case should ONLY happen for LineStrings, so
			 * the orientation is always CW. (Polygons can never have two consecutive
			 * segments which are parallel but reversed, because that would be a self
			 * intersection.
			 * 
			 */
			// TODO check this case
			addFillet(s1, offset0.p1, offset1.p0, Orientation.CLOCKWISE, distance);
		}
	}

	/**
	 * Add points for a circular fillet around a reflex corner. Adds the start and
	 * end points
	 * 
	 * @param p         base point of curve
	 * @param p0        start point of fillet curve
	 * @param p1        endpoint of fillet curve
	 * @param direction the orientation of the fillet
	 * @param radius    the radius of the fillet
	 */
	private void addFillet(Coordinate p, Coordinate p0, Coordinate p1, int direction, double radius) {
		double dx0 = p0.x - p.x;
		double dy0 = p0.y - p.y;
		double startAngle = Math.atan2(dy0, dx0);
		double dx1 = p1.x - p.x;
		double dy1 = p1.y - p.y;
		double endAngle = Math.atan2(dy1, dx1);

		if (direction == Orientation.CLOCKWISE) {
			if (startAngle <= endAngle)
				startAngle += 2.0 * Math.PI;
		} else { // direction == COUNTERCLOCKWISE
			if (startAngle >= endAngle)
				startAngle -= 2.0 * Math.PI;
		}
		vertexList.addPt(p0, false);
		addFillet(p, startAngle, endAngle, direction, radius);
		vertexList.addPt(p1, false);
	}

	/**
	 * When 2 offset segment intersects or will intersect if they would be larger,
	 * calculate the intersection and add this coordinate.
	 * 
	 * @param offset0
	 * @param offset1
	 * @param b
	 */
	private void addCornerPoint(LineSegment offset0, LineSegment offset1, boolean b) {
		Coordinate pt = new Coordinate();

		pt = intersection(offset0.p0, offset0.p1, offset1.p0, offset1.p1);
		vertexList.addPt(pt, b);
	}

	/**
	 * Adds points for a circular fillet arc between two specified angles. The start
	 * and end point for the fillet are not added - the caller must add them if
	 * required.
	 * 
	 * @param direction is -1 for a CW angle, 1 for a CCW angle
	 * @param radius    the radius of the fillet
	 */
	private void addFillet(Coordinate p, double startAngle, double endAngle, int direction, double radius) {
		int directionFactor = direction == Orientation.CLOCKWISE ? -1 : 1;

		double totalAngle = Math.abs(startAngle - endAngle);
		int nSegs = (int) (totalAngle / filletAngleQuantum + 0.5);

		if (nSegs < 1)
			return; // no segments because angle is less than increment -
		// nothing to do!

		double initAngle, currAngleInc;

		// choose angle increment so that each segment has equal length
		initAngle = 0.0;
		currAngleInc = totalAngle / nSegs;

		double currAngle = initAngle;
		Coordinate pt = new Coordinate();
		while (currAngle < totalAngle) {
			double angle = startAngle + directionFactor * currAngle;
			pt.x = p.x + radius * Math.cos(angle);
			pt.y = p.y + radius * Math.sin(angle);
			vertexList.addPt(pt, false);
			currAngle += currAngleInc;
		}
	}

	private void addOutsideTurn(boolean addStartPoint) {

		if (addStartPoint) {
			vertexList.addPt(offset0.p1, false);
		}
		addCornerPoint(offset0, offset1, true);
	}

	private void addInsideTurn() {
		/**
		 * add intersection point of offset segments (if any)
		 */
		li.computeIntersection(offset0.p0, offset0.p1, offset1.p0, offset1.p1);
		if (li.hasIntersection()) {
			vertexList.addPt(li.getIntersection(0), false);
		} else {
			addCornerPoint(offset0, offset1, false);
		}
	}

	private Coordinate[][] inputLines = new Coordinate[2][2];

	/**
	 * This method computes the actual value of the intersection point. To obtain
	 * the maximum precision from the intersection calculation, the coordinates are
	 * normalized by subtracting the minimum ordinate values (in absolute value).
	 * This has the effect of removing common significant digits from the
	 * calculation to maintain more bits of precision.
	 */
	private Coordinate intersection(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2) {
		inputLines[0][0] = p1;
		inputLines[0][1] = p2;
		inputLines[1][0] = q1;
		inputLines[1][1] = q2;
		Coordinate intPt = intersectionWithNormalization(p1, p2, q1, q2);

		if (precisionModel != null) {
			precisionModel.makePrecise(intPt);
		}

		return intPt;
	}

	private Coordinate intersectionWithNormalization(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2) {
		Coordinate n1 = new Coordinate(p1);
		Coordinate n2 = new Coordinate(p2);
		Coordinate n3 = new Coordinate(q1);
		Coordinate n4 = new Coordinate(q2);
		Coordinate normPt = new Coordinate();
		normalizeToEnvCentre(n1, n2, n3, n4, normPt);

		Coordinate intPt = safeHCoordinateIntersection(n1, n2, n3, n4);

		intPt.x += normPt.x;
		intPt.y += normPt.y;

		return intPt;
	}

	/**
	 * Normalize the supplied coordinates to so that the midpoint of their
	 * intersection envelope lies at the origin.
	 * 
	 * @param n00
	 * @param n01
	 * @param n10
	 * @param n11
	 * @param normPt
	 */
	private void normalizeToEnvCentre(Coordinate n00, Coordinate n01, Coordinate n10, Coordinate n11,
			Coordinate normPt) {
		double minX0 = n00.x < n01.x ? n00.x : n01.x;
		double minY0 = n00.y < n01.y ? n00.y : n01.y;
		double maxX0 = n00.x > n01.x ? n00.x : n01.x;
		double maxY0 = n00.y > n01.y ? n00.y : n01.y;

		double minX1 = n10.x < n11.x ? n10.x : n11.x;
		double minY1 = n10.y < n11.y ? n10.y : n11.y;
		double maxX1 = n10.x > n11.x ? n10.x : n11.x;
		double maxY1 = n10.y > n11.y ? n10.y : n11.y;

		double intMinX = minX0 > minX1 ? minX0 : minX1;
		double intMaxX = maxX0 < maxX1 ? maxX0 : maxX1;
		double intMinY = minY0 > minY1 ? minY0 : minY1;
		double intMaxY = maxY0 < maxY1 ? maxY0 : maxY1;

		double intMidX = (intMinX + intMaxX) / 2.0;
		double intMidY = (intMinY + intMaxY) / 2.0;
		normPt.x = intMidX;
		normPt.y = intMidY;

		/*
		 * // equilavalent code using more modular but slower method Envelope env0 = new
		 * Envelope(n00, n01); Envelope env1 = new Envelope(n10, n11); Envelope intEnv =
		 * env0.intersection(env1); Coordinate intMidPt = intEnv.centre();
		 * 
		 * normPt.x = intMidPt.x; normPt.y = intMidPt.y;
		 */

		n00.x -= normPt.x;
		n00.y -= normPt.y;
		n01.x -= normPt.x;
		n01.y -= normPt.y;
		n10.x -= normPt.x;
		n10.y -= normPt.y;
		n11.x -= normPt.x;
		n11.y -= normPt.y;
	}

	/**
	 * Computes a segment intersection using homogeneous coordinates. Round-off
	 * error can cause the raw computation to fail, (usually due to the segments
	 * being approximately parallel). If this happens, a reasonable approximation is
	 * computed instead.
	 * 
	 * @param p1 a segment endpoint
	 * @param p2 a segment endpoint
	 * @param q1 a segment endpoint
	 * @param q2 a segment endpoint
	 * @return the computed intersection point
	 */
	private Coordinate safeHCoordinateIntersection(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2) {

		return Intersection.intersection(p1, p2, q1, q2);
		/*
		 * Coordinate intPt = null; try { intPt = HCoordinate.intersection(p1, p2, q1,
		 * q2); } catch (NotRepresentableException e) { // compute an approximate result
		 * intPt = CentralEndpointIntersector.getIntersection(p1, p2, q1, q2); } return
		 * intPt;
		 */
	}

}
