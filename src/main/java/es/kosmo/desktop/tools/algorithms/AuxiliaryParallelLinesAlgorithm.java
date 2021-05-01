/* 
 * Kosmo - Sistema Abierto de Informaci�n Geogr�fica
 * Kosmo - Open Geographical Information System
 *
 * http://www.saig.es
 * (C) 2011, SAIG S.L.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, contact:
 * 
 * Sistemas Abiertos de Informaci�n Geogr�fica, S.L.
 * Avnda. Rep�blica Argentina, 28
 * Edificio Domocenter Planta 2� Oficina 7
 * C.P.: 41930 - Bormujos (Sevilla)
 * Espa�a / Spain
 *
 * Tel�fono / Phone Number
 * +34 954 788876
 * 
 * Correo electr�nico / Email
 * info@saig.es
 *
 */
package es.kosmo.desktop.tools.algorithms;

import java.util.Arrays;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;

/**
 * Creates parallel lines and expand them inside an envelope
 * <p>
 * </p>
 * 
 * @author Gabriel Bellido P&eacute;rez - gbp@saig.es
 * @since Kosmo 1.2
 */
public class AuxiliaryParallelLinesAlgorithm {

	private final GeometryFactory geomFact = new GeometryFactory();

	ParallelLinesAlgorithm parallelLinesAlg = new ParallelLinesAlgorithm();

	/**
	 * Calculates the parallel line with expanded borders
	 * <p>
	 * How it works: uses the old algorithm of parallel line and takes the last and
	 * first segment of it, expands it larger than the screen width or height and
	 * then clips it with the screen envelope.
	 * </p>
	 * 
	 * @param selected the base line
	 * @param distance offset distance
	 * @param startCoordinate start coordinate
	 * @param env envelope to clip the result on
	 * @return the offset line
	 */
	public Geometry calculateParallelCurve(Geometry selected, double distance, Coordinate startCoordinate,
			Envelope env) {
		LineString parallelCurve = (LineString) parallelLinesAlg.calculateParallelCurve(selected, distance,
				startCoordinate);
		double lenght = Math.sqrt(env.getHeight() * env.getHeight() + env.getWidth() * env.getWidth());
		LineSegment firstSegment = getFirstSegment(parallelCurve);
		LineSegment expandedFirstSegment = normalize(firstSegment, lenght);
		Coordinate[] coordinates = Arrays.copyOf(parallelCurve.getCoordinates(), parallelCurve.getCoordinates().length);
		coordinates[0] = expandedFirstSegment.p1;

		LineSegment lastSegment = getLastSegment(parallelCurve);
		LineSegment expandedLastSegment = normalize(lastSegment, lenght);
		coordinates[coordinates.length - 1] = expandedLastSegment.p1;

		LineString lineString = geomFact.createLineString(coordinates);
		return lineString.intersection(geomFact.toGeometry(env));

	}

	/**
	 * Expand the beginning of the line to the screen border
	 * 
	 * @param line the line to expand
	 * @param env the envelope to expand th eline to
	 * @return the extended line
	 */
	public Geometry expandFirstSegment(LineString line, Envelope env) {
		LineString geom = (LineString) line.copy();
		double lenght = Math.sqrt(env.getHeight() * env.getHeight() + env.getWidth() * env.getWidth());
		LineSegment firstSegment = getFirstSegment(geom);
		LineSegment expandedFirstSegment = normalize(firstSegment, lenght);
		Coordinate[] coordinates = Arrays.copyOf(geom.getCoordinates(), geom.getCoordinates().length);
		coordinates[0] = expandedFirstSegment.p1;
		LineString createLineString = geomFact.createLineString(coordinates);
		return createLineString.intersection(geomFact.toGeometry(env));
	}

	/**
	 * Expand the end of the line to the screen border
	 *
	 * @param line the line to expand
	 * @param env the envelope to expand th eline to
	 * @return the extended line
	 */
	public Geometry expandLastSegment(LineString line, Envelope env) {
		LineString geom = (LineString) line.copy();
		double length = Math.sqrt(env.getHeight() * env.getHeight() + env.getWidth() * env.getWidth());
		Coordinate[] coordinates = Arrays.copyOf(geom.getCoordinates(), geom.getCoordinates().length);
		LineSegment lastSegment = getLastSegment(geom);
		LineSegment expandedLastSegment = normalize(lastSegment, length);
		coordinates[coordinates.length - 1] = expandedLastSegment.p1;
		LineString createLineString = geomFact.createLineString(coordinates);
		return createLineString.intersection(geomFact.toGeometry(env));
	}

	/**
	 * Expand the closest extreme of geom to c to the env border
	 * 
	 * @param geom line to expand
	 * @param env  envelope to cut
	 * @param c the coordinate located near the endpoint to expand
	 * @return the expanded line
	 */
	public Geometry expandClosestEndSegment(LineString geom, Envelope env, Coordinate c) {
		double distanceStart = geom.getStartPoint().getCoordinate().distance(c);
		double distanceEnd = geom.getEndPoint().getCoordinate().distance(c);
		if (distanceStart > distanceEnd) {
			return expandLastSegment(geom, env);
		} else {
			return expandFirstSegment(geom, env);
		}
	}

	/**
	 * Get the first segment of the linestring
	 * 
	 * @param parallelCurve input parallel curve
	 * @return the first segment of the input parallel curve
	 */
	private LineSegment getFirstSegment(LineString parallelCurve) {
		return new LineSegment(parallelCurve.getCoordinateN(1), parallelCurve.getCoordinateN(0));
	}

	/**
	 * Get the last segment of the linestring
	 * 
	 * @param parallelCurve input parallel curve
	 * @return the last segment of the input parallel curve
	 */
	private LineSegment getLastSegment(LineString parallelCurve) {
		int i = parallelCurve.getNumPoints();
		return new LineSegment(parallelCurve.getCoordinateN(i - 2), parallelCurve.getCoordinateN(i - 1));
	}

	/**
	 * Make the line segment have a length of 1
	 * 
	 * @param segment segment to normalize
	 * @return a normalized LineSegment
	 */
	private LineSegment normalize(LineSegment segment, double lambda) {
		Coordinate coordinate = new Coordinate(segment.p1.x - segment.p0.x, segment.p1.y - segment.p0.y);
		double length = segment.getLength();
		Coordinate normalizedLengths = new Coordinate((coordinate.x / length) * lambda,
				(coordinate.y / length) * lambda);
		return new LineSegment(segment.p0.x, segment.p0.y, segment.p0.x + normalizedLengths.x,
				segment.p0.y + normalizedLengths.y);
	}

}
