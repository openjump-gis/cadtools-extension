/*
 * Kosmo - Sistema Abierto de Información Geográfica
 * Kosmo - Open Geographical Information System
 *
 * http://www.saig.es
 * (C) 2006, SAIG S.L.
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
 * Sistemas Abiertos de Información Geográfica, S.L.
 * Avnda. República Argentina, 28
 * Edificio Domocenter Planta 2ª Oficina 7
 * C.P.: 41930 - Bormujos (Sevilla)
 * España / Spain
 *
 * Teléfono / Phone Number
 * +34 954 788876
 *
 * Correo electrónico / Email
 * info@saig.es
 *
 */
package org.openjump.advancedtools.tools;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.openjump.advancedtools.CadExtension;
import org.openjump.advancedtools.gui.SimpleLineDialog;
import org.openjump.advancedtools.icon.IconLoader;
import org.openjump.advancedtools.utils.EditUtils;
import org.openjump.advancedtools.utils.WorkbenchUtils;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.cursortool.NClickTool;

/**
 * Tool that allows to draw a segment. Original code from Kosmo 3.0 SAIG -
 * http://www.opengis.es/
 * 
 * @author Gabriel Bellido Perez
 * @since Kosmo 1.0.0
 * @author Giuseppe Aruta [Genuary 30th 2017] rewrite code to adapt to OpenJUMP
 *         1.10 (http://www.openjump.org/support.html)
 * @since OpenJUMP 1.10 (2017)
 */
public class DrawSimpleLineTool extends NClickTool {

	private static final I18N i18n = CadExtension.I18N;

	/** Name of the tool */
	public final static String NAME = i18n.get("org.openjump.core.ui.tools.DrawSimpleLine.Draw-simple-line");
	public final static String DESCRIPTION = i18n.get("org.openjump.core.ui.tools.DrawSimpleLine.description");

	/** Icon of the tool */
	public static final Icon ICON = IconLoader.icon("drawSimpleLine.png");

	/** Cursor of the the tool */
	public static final Cursor CURSOR = new Cursor(Cursor.CROSSHAIR_CURSOR);

	/** */
	protected Coordinate coordinateA;

	/** */
	protected Coordinate coordinateB;

	/** Dialogo de seleccion de opciones */
	protected SimpleLineDialog sld;

	GeometryFactory geomFac = new GeometryFactory();

	/**
	 * 
	 * @param n
	 * @param sld
	 * @param c1
	 * @param c2
	 */
	public DrawSimpleLineTool(int n, SimpleLineDialog sld, Coordinate c1, Coordinate c2) {
		super(JUMPWorkbench.getInstance().getContext(), n);
		coordinateA = c1;
		coordinateB = c2;
		this.sld = sld;
		allowSnapping();
	}

	/**
	 * 
	 */
	@Override
	protected void gestureFinished() throws Exception {
		reportNothingToUndoYet();

		if (!sld.isPrimerPuntoCapturado()) {
			coordinateA = new Coordinate(sld.x1, sld.y1);
		} else {
			coordinateA = (Coordinate) getCoordinates().get(0);
		}

		if (!sld.isSegundoPuntoCapturado()) {
			if (!sld.relativo) {
				coordinateB = new Coordinate(sld.x2, sld.y2);
			} else {
				coordinateB = new Coordinate(sld.x2 + coordinateA.x, sld.y2 + coordinateA.y);
			}
		} else {
			coordinateB = (Coordinate) getCoordinates().get(getCoordinates().size() - 1);
			if (sld.longitud != -1) {
				double angulo = -EditUtils.getAngle(coordinateA.x, coordinateA.y, coordinateB.x, coordinateB.y);
				coordinateB = new Coordinate(coordinateA.x + (Math.sin(angulo) * sld.longitud),
						coordinateA.y + (Math.cos(angulo) * sld.longitud));
			}

		}

		save();
	}

	public Geometry createGeometry() {
		Coordinate[] cords = new Coordinate[2];
		cords[0] = coordinateA;
		cords[1] = coordinateB;
		return geomFac.createLineString(cords);
	}

	public Feature createFeature() {
		Layer editableLayer = WorkbenchUtils.getlayer();
		return FeatureUtil.toFeature(createGeometry(), editableLayer.getFeatureCollectionWrapper().getFeatureSchema());
	}

	/**
	 *
	 * @throws Exception
	 */
	public void save() throws Exception {
		final Layer editableLayer = WorkbenchUtils.getlayer();

		if (editableLayer == null)
			return;

		final List<Feature> featsToAdd = new ArrayList<>();
		featsToAdd.add(createFeature());

		WorkbenchUtils.executeUndoableAddNewFeats(NAME, getPanel().getSelectionManager(), editableLayer, featsToAdd);
		// WorkbenchUtils.zoomFeatures(featsToAdd);
	}

	@Override
	public Cursor getCursor() {
		if (Toolkit.getDefaultToolkit().getBestCursorSize(32, 32).equals(new Dimension(0, 0))) {
			return Cursor.getDefaultCursor();
		}
		return Toolkit.getDefaultToolkit().createCustomCursor(
				com.vividsolutions.jump.workbench.ui.images.IconLoader.icon("Pen.gif").getImage(),
				new java.awt.Point(1, 31), NAME);

	}

	@Override
	public Icon getIcon() {
		return ICON;
	}

	@Override
	public String getName() {

		String tooltip;
		tooltip = "<HTML><BODY>";
		tooltip += "<DIV style=\"width: 200px; text-justification: justify;\">";
		tooltip += "<b>" + NAME + "</b>" + "<br>";
		tooltip += DESCRIPTION + "<br>";
		tooltip += "</DIV></BODY></HTML>";
		return tooltip;
	}

}
