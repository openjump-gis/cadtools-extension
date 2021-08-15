package org.openjump.advancedtools.tools;

import java.awt.Cursor;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import org.openjump.advancedtools.icon.IconLoader;
import org.openjump.advancedtools.utils.EditUtils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.precision.EnhancedPrecisionOp;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.GeometryEditor;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.PolygonTool;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;


public class RemoveAreaTool extends PolygonTool {

	public final static String NAME = "Remove Area";

	public final static String DESCRIPTION = "Select only one feature of polygonal type";



	public static final Icon ICON = IconLoader.icon("RemoveAreaToPolygon.gif");



	public static final Cursor CURSOR = createCursor(IconLoader.icon("removeAreaCursor.png").getImage(), 
			new Point(0, 0));



	//protected ArrayList coordinatesCorrected;



	public RemoveAreaTool() {
		super(JUMPWorkbench.getInstance().getContext());
		allowSnapping();
		setCloseRing(true);
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

	@Override
	public Icon getIcon() {
		return ICON;
	}
	@Override
	public Cursor getCursor() {
		return CURSOR;
	}

	public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext, CursorTool tool) {
		MultiEnableCheck solucion = new MultiEnableCheck();
		EnableCheckFactory checkFactory =
				workbenchContext.createPlugInContext().getCheckFactory();
		solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
		solucion.add(checkFactory.createOnlyOneLayerMayHaveSelectedFeaturesCheck());
		int MAX_FEATURES_SELECTED = 1;
		solucion.add(checkFactory.createExactlyNFeaturesMustBeSelectedCheck(MAX_FEATURES_SELECTED));
		solucion.add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck());

		/*      solucion.add(checkFactory
          .createEditableLayerTypeGeometryCheck(new int[] { 5, 4, 15 }));


      solucion.add(checkFactory

          .createAtLeastNFeaturesMustBeSelectedCheck(new int[] { 5, 4 }, new int[] { 10, 11, 9 }, 1));
		 */  
		solucion.add(checkFactory.createSelectedItemsLayersMustBeEditableCheck());
		return solucion;
	}




	@Override
	protected Polygon getPolygon() {

		ArrayList<Coordinate> closedPoints = new ArrayList<>(getCoordinates());

		if (!closedPoints.get(0).equals(closedPoints.get(closedPoints.size() - 1))) {
			closedPoints.add(new Coordinate(closedPoints.get(0)));
		}

		return new GeometryFactory().createPolygon(
				new GeometryFactory().createLinearRing(toArray(closedPoints)),
				null);
	}




	@Override
	protected void gestureFinished() throws Exception {
		reportNothingToUndoYet();
		if (!checkPolygon()) {
			getPanel().getContext().warnUser(
					I18N.JUMP.get("ui.EditTransaction.the-geometry-is-invalid-cancelled"));

			return;
		} 
		//    boolean selectAdjacents = EditOptionsPanel.isAdjacentEditionActivated();

		Polygon polygon = getPolygon();

		if (isRollingBackInvalidEdits() && !polygon.isValid()) {
			getPanel()
			.getContext()
			.warnUser(

					"Polygon with invalid geometry");
			//	I18N.getString("org.saig.jump.tools.editing.AddAreaTool.polygon-with-invalid-geometry"));

			return;
		} 

		//WorkbenchContext context = getWorkbench().getContext();
		//Collection col = context.getLayerViewPanel().getSelectionManager()
		//		.getLayersWithSelectedItems();

		final Layer editableLayer = getPanel().getLayerManager().getEditableLayers()
				.iterator().next();

		if (editableLayer == null) {
			return;
		}
		final SelectionManager selectionManager = getPanel().getSelectionManager();
		final Collection<Feature> selectedFeatures = selectionManager
				.getFeaturesWithSelectedItems(editableLayer);

		if (selectedFeatures == null || selectedFeatures.isEmpty()) {
			getPanel()
			.getContext()
			.warnUser(

					I18N.JUMP.get("com.vividsolutions.jump.workbench.plugin.At-least-one-layer-must-be-selected"));

			return;
		} 
		if (selectedFeatures.size()>1) {
			getPanel()
			.getContext()
			.warnUser(

					I18N.JUMP.get("com.vividsolutions.jump.workbench.plugin.Exactly-one-item-must-be-selected"));

			return;
		}
		Feature selectedFeature = selectedFeatures.iterator().next();
		GeometryEditor editor = new GeometryEditor();
		Geometry digitizedGeometry = editor.removeRepeatedPoints(polygon);

		Geometry geomSel = selectedFeature.getGeometry();
		if ((geomSel instanceof LineString) || (geomSel instanceof org.locationtech.jts.geom.Point) )
		{
			getPanel()
			.getContext()
			.warnUser(
					"Selected feature must be a Polygon");
			//	I18N.get("com.vividsolutions.jump.workbench.plugin.Exactly-one-item-must-be-selected"));

			return;
		}
		if (!digitizedGeometry.intersects(geomSel)) {
			getPanel()
			.getContext()
			.warnUser(
					"It does not intersect the selected geometry");
			//I18N.getString("org.saig.jump.tools.editing.AddAreaTool.it-does-not-intersect-with-the-selection"));
			return;
		} 
		if (geomSel.contains(digitizedGeometry)) {
			getPanel()
			.getContext()
			.warnUser(
					"Digitalized geometry is contained");
			//I18N.getString("org.saig.jump.tools.editing.AddAreaTool.the-digitized-geometry-is-contained"));

			return;
		} 

		final List<Feature> featsToUpdate = new ArrayList<>();
		final List<Feature> featsSelectedToUpdate = new ArrayList<>();

		Geometry modGeometry;

		try {
			modGeometry = EnhancedPrecisionOp.difference(geomSel, digitizedGeometry);

			//    if (selectAdjacents) {
			Collection<Feature> colindantes = EditUtils.getColindantes(geomSel, editableLayer);
			Iterator<Feature> itColindantes = colindantes.iterator();
			int contador = 0;
			while (itColindantes.hasNext()) {
				Feature featCol = itColindantes.next();
				Geometry geomCol = featCol.getGeometry();
				if (digitizedGeometry.intersects(geomCol)) {
					if (contador == 1) {
						getPanel()
						.getContext()
						.warnUser(
								"The digitized polygon intersects with more than one neighbour"); 
						//    I18N.getString("org.saig.jump.tools.editing.RemoveAreaTool.the-digitized-polygon-intersects-with-more-than-one-neighbour"));

						return;
					} 
					GeometryFactory geomFac = null;
					Geometry[] geometrias = 
							EditUtils.getFragmentos(geomCol, EnhancedPrecisionOp.intersection(geomSel, digitizedGeometry));
					GeometryCollection gc = geomFac.createGeometryCollection(geometrias);
					Geometry geomColMod = gc.buffer(0.0D);

					Feature featColClone = featCol.clone(true);
					featColClone.setGeometry(geomColMod);
					featsToUpdate.add(featColClone);
					featsSelectedToUpdate.add(featCol);
					contador++;
				} 
			} 
			//    } 
		} catch (TopologyException ex) {
			getPanel()
			.getContext()
			.warnUser(
					I18N.JUMP.get("ui.cursortool.editing.FeatureDrawingUtil.draw-feature-tool-topology-error"));
			// I18N.getString("org.saig.jump.tools.editing.RemoveAreaTool.topology-error-repeat-the-operation"));

			return;
		} 
		Feature clonedFeature = selectedFeature.clone(true);
		clonedFeature.setGeometry(modGeometry);
		featsToUpdate.add(clonedFeature);
		featsSelectedToUpdate.add(selectedFeature);

		execute(new UndoableCommand(getName() + " - " +
				I18N.JUMP.get("org.saig.jump.tools.editing.RemoveAreaTool.{0}-features-modified",
						featsToUpdate.size())) {
			@Override
			public void execute() {
				selectionManager.unselectItems(editableLayer);
				if (!featsToUpdate.isEmpty()) {
					editableLayer.getFeatureCollectionWrapper().removeAll(featsSelectedToUpdate);
					editableLayer.getFeatureCollectionWrapper().addAll(featsToUpdate);


					//   editableLayer.getFeatureCollectionWrapper().updateAll(featsToUpdate);
					editableLayer.getLayerManager().fireGeometryModified(featsToUpdate, 
							editableLayer, featsSelectedToUpdate);
					selectionManager.getFeatureSelection()
					.selectItems(editableLayer, featsToUpdate);
				} 
			}


			@Override
			public void unexecute() {
				selectionManager.unselectItems(editableLayer);
				if (!featsToUpdate.isEmpty()) {
					editableLayer.getFeatureCollectionWrapper().removeAll(featsToUpdate);
					editableLayer.getFeatureCollectionWrapper().addAll(featsSelectedToUpdate);
					//  editableLayer.getFeatureCollectionWrapper().updateAll(featsSelectedToUpdate);
					editableLayer.getLayerManager().fireGeometryModified(featsSelectedToUpdate, 
							editableLayer, featsToUpdate);
				} 
				selectionManager.getFeatureSelection().selectItems(editableLayer, 
						selectedFeatures);
			}
		});
	}





	@Override
	protected boolean checkPolygon() {
		if (getCoordinates().size() < 3) {
			getPanel()
			.getContext()
			.warnUser(
					I18N.JUMP.get("ui.cursortool.PolygonTool.the-polygon-must-have-at-least-3-points"));

			return false;
		}

		IsValidOp isValidOp = new IsValidOp(getPolygon());

		if (!isValidOp.isValid()) {
			getPanel().getContext().warnUser(
					isValidOp.getValidationError().getMessage());

			if (PersistentBlackboardPlugIn.get(getWorkbench().getContext())
					.get(EditTransaction.ROLLING_BACK_INVALID_EDITS_KEY, false)) {
				return false;
			}
		}

		return true;
	}
}


