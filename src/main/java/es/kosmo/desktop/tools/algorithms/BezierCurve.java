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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

public class BezierCurve {
    private GeometryFactory factory = new GeometryFactory();

    public Coordinate pointOnCubicBezier(Coordinate[] cp, double t) {
        Coordinate result = new Coordinate();

        double cx = 3.0D * (cp[1].x - cp[0].x);
        double bx = 3.0D * (cp[2].x - cp[1].x) - cx;
        double ax = cp[3].x - cp[0].x - cx - bx;

        double cy = 3.0D * (cp[1].y - cp[0].y);
        double by = 3.0D * (cp[2].y - cp[1].y) - cy;
        double ay = cp[3].y - cp[0].y - cy - by;

        double tSquared = t * t;
        double tCubed = tSquared * t;

        result.x = (ax * tCubed + bx * tSquared + cx * t + cp[0].x);
        result.y = (ay * tCubed + by * tSquared + cy * t + cp[0].y);

        return result;
    }

    public Coordinate pointOncubicBezier(Coordinate p1, Coordinate p2,
            Coordinate p3, Coordinate p4, double t) {
        Coordinate result = new Coordinate();
        double ax = (1.0D - t) * (1.0D - t) * (1.0D - t);
        double bx = 3.0D * (1.0D - t) * (1.0D - t) * t;
        double cx = 3.0D * (1.0D - t) * t * t;
        double dx = t * t * t;
        result.x = (ax * p1.x + bx * p2.x + cx * p3.x + dx * p4.x);
        result.y = (ax * p1.y + bx * p2.y + cx * p3.y + dx * p4.y);
        return result;
    }

    public Coordinate pointSpline(Coordinate[] ps, double t) {
        Coordinate result = new Coordinate();

        int num = ps.length;
        double[] px = new double[num];
        double[] py = new double[num];
        for (int i = 0; i < num; i++) {
            Coordinate p = ps[i];
            px[i] = p.x;
            py[i] = p.y;
            double ax = (1.0D - t) * (1.0D - t);
            double bx = 2.0D * (1.0D - t) * t;
            double cx = t * t;
            result.x = (ax * px[i] + bx * px[(i + 1)] + cx * px[(i + 2)]);
            result.y = (ax * py[i] + bx * py[(i + 1)] + cx * py[(i + 3)]);
        }

        return result;
    }

    public LineString calculateSpline(Coordinate[] ps, int nSegments) {
        Coordinate[] coords = new Coordinate[nSegments + 1];
        double delta = 1.0D / nSegments;
        for (int i = 0; i <= nSegments; i++) {
            double t = i * delta;
            coords[i] = pointSpline(ps, t);
        }
        return this.factory.createLineString(coords);
    }

    public Coordinate pointOnCuadraticBezier(Coordinate p1, Coordinate p2,
            Coordinate p3, double t) {
        Coordinate result = new Coordinate();
        double ax = (1.0D - t) * (1.0D - t);
        double bx = 2.0D * (1.0D - t) * t;
        double cx = t * t;
        result.x = (ax * p1.x + bx * p2.x + cx * p3.x);
        result.y = (ax * p1.y + bx * p2.y + cx * p3.y);
        return result;
    }

    public static Coordinate pointOnCuadraticBezier2(Coordinate p1,
            Coordinate p2, Coordinate p3, double t) {
        Coordinate result = new Coordinate();
        double ax = (1.0D - t) * (1.0D - t);
        double bx = 2.0D * (1.0D - t) * t;
        double cx = t * t;
        result.x = (ax * p1.x + bx * p2.x + cx * p3.x);
        result.y = (ax * p1.y + bx * p2.y + cx * p3.y);
        return result;
    }

    public LineString calculateCuadraticBezier(Coordinate p1, Coordinate p2,
            Coordinate p3, int nSegments) {
        Coordinate[] coords = new Coordinate[nSegments + 1];
        double delta = 1.0D / nSegments;
        for (int i = 0; i <= nSegments; i++) {
            double t = i * delta;
            coords[i] = pointOnCuadraticBezier(p1, p2, p3, t);
        }
        return this.factory.createLineString(coords);
    }

    public LineString calculateCubicBezier(Coordinate p1, Coordinate p2,
            Coordinate p3, Coordinate p4, int nSegments) {
        Coordinate[] coords = new Coordinate[nSegments + 1];
        double delta = 1.0D / nSegments;
        for (int i = 0; i <= nSegments; i++) {
            double t = i * delta;
            coords[i] = pointOncubicBezier(p1, p2, p3, p4, t);
        }
        return this.factory.createLineString(coords);
    }

    static class Spline {
        private double[] y2;

        public Spline(double[] y) {
            int n = y.length;
            this.y2 = new double[n];
            double[] u = new double[n];
            for (int i = 1; i < n - 1; i++) {
                this.y2[i] = (-1.0D / (4.0D + this.y2[(i - 1)]));
                u[i] = ((6.0D * (y[(i + 1)] - 2.0D * y[i] + y[(i - 1)]) - u[(i - 1)]) / (4.0D + this.y2[(i - 1)]));
            }
            for (int i = n - 2; i >= 0; i--) {
                this.y2[i] = (this.y2[i] * this.y2[(i + 1)] + u[i]);
            }
        }
    }
}
