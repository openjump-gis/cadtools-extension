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
package org.openjump.advancedtools.plugins;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import com.vividsolutions.jump.workbench.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.openjump.advancedtools.gui.SimpleLineDialog;
import org.openjump.advancedtools.icon.IconLoader;
import org.openjump.advancedtools.language.I18NPlug;
import org.openjump.advancedtools.tools.DrawSimpleLineTool;
import org.openjump.advancedtools.utils.WorkbenchUtils;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.cursortool.QuasimodeTool;

/**
 * Tool to draw a segment using parameters
 * 
 * @author Gabriel Bellido Perez
 * @since Kosmo 1.0.0
 * @author Giuseppe Aruta [Genuary 30th 2017] rewrite code to adapt to OpenJUMP
 *         1.10 (http://www.openjump.org/support.html)
 * @since OpenJUMP 1.10 (2017)
 */

public class SimpleLinePlugIn extends AbstractPlugIn {

	/** Name of the tool */
	public final static String NAME = I18NPlug.getI18N("org.openjump.core.ui.tools.DrawSimpleLine.Draw-simple-line");
	public final static String DESCRIPTION = I18NPlug.getI18N("org.openjump.core.ui.tools.DrawSimpleLine.description");

	/** Icon of the tool */
	public static final Icon ICON = IconLoader.icon("drawSimpleLine.png");

	protected PlugInContext context;

	protected SimpleLineDialog sld;

	GeometryFactory geomFac = new GeometryFactory();

	/**
	 * 
	 */
	public SimpleLinePlugIn() {
		// Nothing to do
	}

	public Geometry createGeometry() {
		Coordinate coordinateA, coordinateB;
		coordinateA = new Coordinate(sld.x1, sld.y1);

		if (!sld.relativo) {
			coordinateB = new Coordinate(sld.x2, sld.y2);
		} else {
			coordinateB = new Coordinate(sld.x2 + coordinateA.x, sld.y2 + coordinateA.y);
		}
		Coordinate[] cords = new Coordinate[2];
		cords[0] = coordinateA;
		cords[1] = coordinateB;
		Geometry nuevaGeom = geomFac.createLineString(cords);
		return nuevaGeom;
	}

	public Feature createFeature() {
		Layer editableLayer = WorkbenchUtils.getlayer();
		return FeatureUtil.toFeature(createGeometry(), editableLayer.getFeatureCollectionWrapper().getFeatureSchema());
	}

	public void selectManualTool(int nPuntosACapturar, Coordinate c1, Coordinate c2) {
		DrawSimpleLineTool dslt = new DrawSimpleLineTool(nPuntosACapturar, sld, c1, c2);
		QuasimodeTool quasimodeTool = WorkbenchUtils.addStandardQuasimodes(dslt);
		context.getLayerViewPanel().setCurrentCursorTool(quasimodeTool);
	}

	@Override
	public boolean execute(PlugInContext context) throws Exception {
		reportNothingToUndoYet(context);

		if (!(JUMPWorkbench.getInstance().getFrame().getActiveInternalFrame() instanceof TaskFrame)) {
			JUMPWorkbench.getInstance().getFrame()
					.warnUser(I18N.get("com.vividsolutions.jump.workbench.plugin.A-Task-Window-must-be-active"));
			return false;
		} else {
			try {
				action(context);
			} catch (Exception ex) {
				Logger.warn(ex);
			}
			return true;
		}
	}

	public void action(PlugInContext context) throws Exception {
		reportNothingToUndoYet(context);
		WorkbenchUtils.checkActiveTaskWindow();
		this.context = context;
		Coordinate c1 = null;
		Coordinate c2 = null;

		int nPuntosACapturar;

		sld = new SimpleLineDialog(this, context);
		sld.setVisible(true);
		if (!sld.cancelado) {
			nPuntosACapturar = 0;
			if (sld.isPrimerPuntoCapturado()) {
				nPuntosACapturar++;
			} else {
				c1 = new Coordinate(sld.x1, sld.y1);
				nPuntosACapturar++;
			}

			if (sld.isSegundoPuntoCapturado())
				nPuntosACapturar++;

			if (sld.isPrimerPuntoCapturado() || sld.isSegundoPuntoCapturado()) {
				selectManualTool(nPuntosACapturar, c1, c2);
			} else {
				save();
			}
		}
	}

	public void createLine(SimpleLineDialog sld) throws Exception {
		this.sld = sld;
		save();
	}

	public void save() throws Exception {
		reportNothingToUndoYet(context);

		final Layer editableLayer = WorkbenchUtils.getlayer();
		if (editableLayer == null)
			return;
		final SelectionManager selectionManager = context.getLayerViewPanel().getSelectionManager();
		final List<Feature> featsToAdd = new ArrayList<>();
		featsToAdd.add(createFeature());
		WorkbenchUtils.executeUndoableAddNewFeats(NAME, selectionManager, editableLayer, featsToAdd);
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

	public Icon getIcon() {
		return ICON;
	}

	/**
	 *
	 */
	public static MultiEnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
		MultiEnableCheck solucion = new MultiEnableCheck();
		EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);

		// al menos una capa debe tener elementos activos
		solucion.add(checkFactory.createTaskWindowMustBeActiveCheck())
				.add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck())
				.add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));

		return solucion;
	}

}
