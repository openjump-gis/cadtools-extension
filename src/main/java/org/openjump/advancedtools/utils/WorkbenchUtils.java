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
package org.openjump.advancedtools.utils;

import static com.vividsolutions.jump.I18N.get;
import static com.vividsolutions.jump.I18N.getMessage;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import com.vividsolutions.jump.workbench.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.util.Assert;
import org.openjump.advancedtools.icon.IconLoader;
import org.openjump.advancedtools.language.I18NPlug;
import org.openjump.util.python.JUMP_GIS_Framework;
import org.openjump.util.python.ModifyGeometry;
import org.openjump.util.python.PythonInteractiveInterpreter;
//import org.python.core.PySystemState;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.FenceLayerFinder;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.EditOptionsPanel;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.SelectionManagerProxy;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DelegatingTool;
import com.vividsolutions.jump.workbench.ui.cursortool.FeatureInfoTool;
import com.vividsolutions.jump.workbench.ui.cursortool.LeftClickFilter;
import com.vividsolutions.jump.workbench.ui.cursortool.QuasimodeTool;
import com.vividsolutions.jump.workbench.ui.cursortool.QuasimodeTool.ModifierKeySpec;
import com.vividsolutions.jump.workbench.ui.cursortool.SelectFeaturesTool;
import com.vividsolutions.jump.workbench.ui.plugin.AddNewLayerPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxDialog;
import com.vividsolutions.jump.workbench.ui.zoom.PanTool;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomToSelectedItemsPlugIn;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomTool;

import bsh.util.JConsole;
import org.python.core.PySystemState;

/**
 * <p>
 * Helps to manage OpenJUMP Desktop actions
 * </p>
 * 
 * @author Gabriel Bellido Perez
 * @since Kosmo 1.0.0
 * @author Giuseppe Aruta
 * @since OpenJUMP 1.10
 */
public class WorkbenchUtils {

	//private static final Logger LOGGER = Logger.getLogger(WorkbenchUtils.class);

	///**
	// * default charset for shapes
	// */
	//public static final String DEFAULT_STRING_CHARSET = "ISO-8859-1";

	/**
	 * Adds a layer to OpenJUMP from a FeatureCollection
	 */
	public static Layer addMemoryLayerToOpenJUMP(String categoryName, String layerName, FeatureCollection fc) {
		Layer layer = JUMPWorkbench.getInstance().getFrame().getContext().getLayerViewPanel().getLayerManager()
				.addLayer(categoryName, layerName, fc);
		return layer;
	}

	/**
	 * gets the selected features in the active view of OpenJUMP, if any. Never
	 * returns null.
	 * 
	 * @return
	 */
	public static List<Feature> getSelectedFeatures() {
		List<Feature> features = new ArrayList<>();
		LayerViewPanel layerViewPanel = JUMPWorkbench.getInstance().getFrame().getContext().getLayerViewPanel();

		if (layerViewPanel != null) {
			SelectionManager selectionManager = layerViewPanel.getSelectionManager();
			Collection<Feature> featuresWithSelectedItems = selectionManager.getFeaturesWithSelectedItems();
			features.addAll(featuresWithSelectedItems);
		}
		return features;
	}

	/**
	 * gets the selected features in the active view and given layer of OpenJUMP, if
	 * any. Never returns null.
	 * 
	 * @return
	 */
	public static List<Feature> getSelectedFeatures(Layer layer) {
		List<Feature> features = new ArrayList<>();
		LayerViewPanel layerViewPanel = JUMPWorkbench.getInstance().getFrame().getContext().getLayerViewPanel();

		if (layerViewPanel != null) {
			SelectionManager selectionManager = layerViewPanel.getSelectionManager();
			Collection<Feature> featuresWithSelectedItems = selectionManager.getFeaturesWithSelectedItems(layer);
			features.addAll(featuresWithSelectedItems);
		}
		return features;
	}

	/**
	 * Boolean if selected feature all belong to one layer *
	 *
	 */
	public static boolean selectedFeaturesBelongToOneLayer() {
		boolean resultado = false;
		LayerViewPanel layerViewPanel = JUMPWorkbench.getInstance().getFrame().getContext().getLayerViewPanel();
		SelectionManager selectionManager = layerViewPanel.getSelectionManager();
		Collection<Layer> layers = selectionManager.getLayersWithSelectedItems();
		if (layers.size() == 1) {
			return true;
		}

		return resultado;
	}

	/**
	 * Removes the current feature selection
	 */
	public static void discardSelection() {
		WorkbenchContext context = JUMPWorkbench.getInstance().getFrame().getContext();
		LayerViewPanel layerViewPanel = context.getLayerViewPanel();
		if (layerViewPanel != null) {
			SelectionManager selectionManager = layerViewPanel.getSelectionManager();
			selectionManager.clear();
		}
	}

	/**
	 * gets the layer that has selected features, if features of more than one layer
	 * are selected, returns null
	 * 
	 * @return the layer that has selected features, if features of more than one
	 *         layer are selected, returns null
	 */
	public static Layer getSelectedFeaturesLayer() {
		Layer layer = null;
		LayerViewPanel layerViewPanel = JUMPWorkbench.getInstance().getFrame().getContext().getLayerViewPanel();

		if (layerViewPanel != null) {
			SelectionManager selectionManager = layerViewPanel.getSelectionManager();
			Collection<Layer> layersWithSelectedItems = selectionManager.getLayersWithSelectedItems();
			if (layersWithSelectedItems.size() == 1) {
				layer = layersWithSelectedItems.iterator().next();
			}
		}
		return layer;
	}

	/**
	 * Gets the selected categories in the layerName panel if any
	 * 
	 * @return Collection<Category>, may be an empty collection but never returns
	 *         null.
	 */
	public static Collection<Category> getSelectedCategories() {
		Collection<Category> selectedCategories = new ArrayList<>();

		WorkbenchFrame frameInstance = JUMPWorkbench.getInstance().getFrame();
		WorkbenchContext context = frameInstance.getContext();
		LayerNamePanel layerNamePanel = context.getLayerNamePanel();

		if (layerNamePanel != null) {
			selectedCategories = layerNamePanel.getSelectedCategories();
		}

		return selectedCategories;
	}

	/**
	 * gets the selected layer in the layerName panel if any
	 * 
	 * @return
	 */
	public static Layer getSelectedLayer() {
		WorkbenchFrame frameInstance = JUMPWorkbench.getInstance().getFrame();
		WorkbenchContext context = frameInstance.getContext();
		LayerNamePanel layerNamePanel = context.getLayerNamePanel();
		if (layerNamePanel == null)
			return null;
		Layerable[] selectedLayers = layerNamePanel.getSelectedLayers();
		if (selectedLayers.length > 0) {
			Layerable layerable = selectedLayers[0];
			if (layerable instanceof Layer)
				return (Layer) layerable;
			else
				return null;
		}
		return null;
	}

	/**
	 * Gets the selected layerables in the layerName panel if any
	 * 
	 * @return Layerable[] Puede devolver null.
	 */
	public static Layerable[] getSelectedLayerables() {
		WorkbenchFrame frameInstance = JUMPWorkbench.getInstance().getFrame();
		WorkbenchContext context = frameInstance.getContext();
		LayerNamePanel layerNamePanel = context.getLayerNamePanel();
		if (layerNamePanel == null) {
			return null;
		}
		return layerNamePanel.getSelectedLayers();
	}

	/**
	 * Gets the selected layers in the layerName panel if any
	 * 
	 * @return List<Layer> de entre los layerables seleccioandos devuelve los que
	 *         sean de tipo Layer. Nunca devuelve null, a lo sumo una lista vac�a.
	 */
	public static List<Layer> getSelectedLayers() {
		Layerable[] selectedLayers = getSelectedLayerables();

		List<Layer> layers = new ArrayList<>();

		if (selectedLayers == null) {
			return layers;
		}

		for (Layerable layerable : selectedLayers) {
			if (layerable instanceof Layer) {
				layers.add((Layer) layerable);
			}
		}

		return layers;
	}

	/**
	 * gets the editable layer in OpenJUMP, if any
	 * 
	 * @return
	 */
	public static Layer getEditableLayer() {
		WorkbenchFrame frameInstance = JUMPWorkbench.getInstance().getFrame();
		WorkbenchContext context = frameInstance.getContext();
		LayerManager layerManager = context.getLayerManager();
		if (layerManager != null) {
			Collection<Layer> editableLayers = layerManager.getEditableLayers();
			Layer editableLayer;
			if (editableLayers.size() > 0) {
				editableLayer = editableLayers.iterator().next();
				return editableLayer;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public static Layer getlayer() {
		WorkbenchFrame frameInstance = JUMPWorkbench.getInstance().getFrame();
		WorkbenchContext context = frameInstance.getContext();
		if (context.getLayerableNamePanel().chooseEditableLayer() == null) {
			Layer layer = context.getLayerViewPanel().getLayerManager().addLayer(StandardCategoryNames.WORKING,
					I18N.get("ui.cursortool.editing.FeatureDrawingUtil.new"),
					AddNewLayerPlugIn.createBlankFeatureCollection());
			layer.setEditable(true);
		}
		return context.getLayerableNamePanel().chooseEditableLayer();
	}

	/**
	 * gets the current view pixel size in view units.<br>
	 * returns -1 if no view is selected
	 * 
	 * @return
	 */
	public static double getPixelSize() {
		WorkbenchContext context = JUMPWorkbench.getInstance().getFrame().getContext();
		LayerViewPanel layerViewPanel = context.getLayerViewPanel();
		if (layerViewPanel != null) {
			int screenWidth = layerViewPanel.getWidth();
			Envelope worldEnvelope = layerViewPanel.getViewport().getEnvelopeInModelCoordinates();
			return worldEnvelope.getWidth() / screenWidth;
		} else {
			return -1;
		}
	}

	/**
	 * Executes an undoable command add new features. Original features will not be
	 * deleted.
	 * 
	 * @param name
	 * @param selection
	 * @param layer
	 * @param featuresToAdd  features to add
	 * @param featuresOriginal features to remove
	 * @throws Exception
	 */

	public static void executeAddNewFeaturesRemoveOldFeatures(String name, SelectionManager selection, Layer layer,
			List<Feature> featuresToAdd, List<Feature> featuresOriginal) throws Exception {
		executeUndoableChanges(name, selection, layer, featuresToAdd, featuresOriginal);
	}

	/**
	 * Executes an undoable command add new features. Original features will be
	 * deleted.
	 * 
	 * @param name
	 * @param selection
	 * @param layer
	 * @param featuresToAdd    to add
	 * @param featuresOriginal original
	 * @throws Exception
	 */
	public static void executeAddNewFeaturesLeaveOldFeatures(
				String name, SelectionManager selection, Layer layer,
				List<Feature> featuresToAdd, List<Feature> featuresOriginal) throws Exception {
		executeUndoableChanges(name, selection, layer, featuresToAdd, new ArrayList<>());
	}

	/**
	 * executes an undoable command, a layer modification that can be undone, this
	 * call is too clomplex and must be protected. <br>
	 * If you need to add a new single feature, or anything less complicated create
	 * a new entry with a simpler interface and use this one to implement it if
	 * possible
	 * 
	 * @param selectionManager
	 * @param editableLayer
	 * @param toAdd      features to add
	 * @param toDelete   features to delete
	 */
	protected static void executeUndoableChanges(String name, final SelectionManager selectionManager,
			final Layer editableLayer, final List<Feature> toAdd, final List<Feature> toDelete) {
		WorkbenchContext context = JUMPWorkbench.getInstance().getFrame().getContext();
		final boolean selection = PersistentBlackboardPlugIn
				.get(context.getLayerViewPanel().getWorkBenchFrame().getContext())
				.get(EditOptionsPanel.SELECT_NEW_GEOMETRY_KEY, false);
		LayerViewPanel layerViewPanel = context.getLayerViewPanel();
		if (layerViewPanel != null) {
			AbstractPlugIn.execute(new UndoableCommand(name) {
				@Override
				public void execute() {
					selectionManager.unselectItems(editableLayer);
					try {
						if (!toAdd.isEmpty()) {
							editableLayer.getFeatureCollectionWrapper().removeAll(toDelete);
							editableLayer.getFeatureCollectionWrapper().addAll(toAdd);
						}
						if (selection) {
							selectionManager.getFeatureSelection().selectItems(editableLayer, toAdd);
						}
					} catch (Exception e) {
						selectionManager.getFeatureSelection().selectItems(editableLayer, toDelete);
						JUMPWorkbench.getInstance().getFrame().warnUser(e.getMessage());
					}
				}

				@Override
				public void unexecute() {
					selectionManager.unselectItems(editableLayer);
					if (!toAdd.isEmpty()) {
						editableLayer.getFeatureCollectionWrapper().removeAll(toAdd);
						editableLayer.getFeatureCollectionWrapper().addAll(toDelete);
					}
					selectionManager.getFeatureSelection().selectItems(editableLayer, toDelete);
				}
			}, layerViewPanel);
		}
	}

	/**
	 * It executes an undoable command: it create new features from selected ones
	 * saving the latter
	 * 
	 * @param selectionManager
	 * @param editableLayer
	 * @param featureCopies    features
	 * @param selectedFeatures features
	 */
	public static void executeUndoableAddNewFeatsLeaveSelectedFeats(String name,
			final SelectionManager selectionManager, final Layer editableLayer,
		  final Collection<Feature> featureCopies,
			final Collection<Feature> selectedFeatures) {
		WorkbenchContext context = JUMPWorkbench.getInstance().getFrame().getContext();
		final boolean selection = PersistentBlackboardPlugIn
				.get(context.getLayerViewPanel().getWorkBenchFrame().getContext())
				.get(EditOptionsPanel.SELECT_NEW_GEOMETRY_KEY, false);
		LayerViewPanel layerViewPanel = context.getLayerViewPanel();
		if (layerViewPanel != null) {
			AbstractPlugIn.execute(new UndoableCommand(name) {
				@Override
				public void execute() {
					selectionManager.unselectItems(editableLayer);
					try {
						if (!featureCopies.isEmpty()) {
							editableLayer.getFeatureCollectionWrapper().addAll(featureCopies);
						}
						if (selection) {
							selectionManager.getFeatureSelection().selectItems(editableLayer, featureCopies);
						}
					} catch (Exception e) {
						selectionManager.getFeatureSelection().selectItems(editableLayer, selectedFeatures);
						JUMPWorkbench.getInstance().getFrame().warnUser(e.getMessage());
					}
				}

				@Override
				public void unexecute() {
					selectionManager.unselectItems(editableLayer);
					if (!featureCopies.isEmpty()) {
						editableLayer.getFeatureCollectionWrapper().removeAll(featureCopies);
					}
					selectionManager.getFeatureSelection().selectItems(editableLayer, selectedFeatures);
				}
			}, layerViewPanel);
		}
	}

	/**
	 * It executes an undoable command: it create new features from selected ones
	 * deleting the latter.
	 * 
	 * @param selectionManager
	 * @param editableLayer
	 * @param toAdd    features to add
	 * @param toDelete features to delete
	 */
	public static void executeUndoableAddNewFeatsRemoveSelectedFeats(String name,
			final SelectionManager selectionManager, final Layer editableLayer,
			final Collection<Feature> toAdd,
			final Collection<Feature> toDelete) {
		WorkbenchContext context = JUMPWorkbench.getInstance().getFrame().getContext();
		final boolean selection = PersistentBlackboardPlugIn
				.get(context.getLayerViewPanel().getWorkBenchFrame().getContext())
				.get(EditOptionsPanel.SELECT_NEW_GEOMETRY_KEY, false);
		LayerViewPanel layerViewPanel = context.getLayerViewPanel();
		if (layerViewPanel != null) {
			AbstractPlugIn.execute(new UndoableCommand(name) {
				@Override
				public void execute() {
					selectionManager.unselectItems(editableLayer);
					try {

						editableLayer.getFeatureCollectionWrapper().removeAll(toDelete);
						editableLayer.getFeatureCollectionWrapper().addAll(toAdd);
						if (selection) {
							selectionManager.getFeatureSelection().selectItems(editableLayer, toAdd);
						}
					} catch (Exception e) {
						selectionManager.getFeatureSelection().selectItems(editableLayer, toDelete);
						JUMPWorkbench.getInstance().getFrame().warnUser(e.getMessage());
					}
				}

				@Override
				public void unexecute() {
					selectionManager.unselectItems(editableLayer);
					editableLayer.getFeatureCollectionWrapper().removeAll(toAdd);
					editableLayer.getFeatureCollectionWrapper().addAll(toDelete);
					selectionManager.getFeatureSelection().selectItems(editableLayer, toDelete);
				}
			}, layerViewPanel);
		}
	}

	/**
	 * It executes an undoable command adding new features.
	 * 
	 * @param selectionManager
	 * @param editableLayer
	 * @param toAdd features to add
	 */
	public static void executeUndoableAddNewFeats(String name, final SelectionManager selectionManager,
			final Layer editableLayer, final Collection<Feature> toAdd) {
		WorkbenchContext context = JUMPWorkbench.getInstance().getFrame().getContext();
		final boolean selection = PersistentBlackboardPlugIn
				.get(context.getLayerViewPanel().getWorkBenchFrame().getContext())
				.get(EditOptionsPanel.SELECT_NEW_GEOMETRY_KEY, false);
		LayerViewPanel layerViewPanel = context.getLayerViewPanel();
		if (layerViewPanel != null) {
			AbstractPlugIn.execute(new UndoableCommand(name) {
				@Override
				public void execute() {
					selectionManager.unselectItems(editableLayer);
					try {
						editableLayer.getFeatureCollectionWrapper().addAll(toAdd);
						if (selection) {
							selectionManager.getFeatureSelection().selectItems(editableLayer, toAdd);
						}
					} catch (Exception e) {
						JUMPWorkbench.getInstance().getFrame().warnUser(e.getMessage());
					}
				}

				@Override
				public void unexecute() {
					selectionManager.unselectItems(editableLayer);
					editableLayer.getFeatureCollectionWrapper().removeAll(toAdd);

				}
			}, layerViewPanel);
		}
	}

	/**
	 * zooms an envelope
	 * 
	 * @param env envelope
	 * @throws NoninvertibleTransformException
	 */
	public static void zoom(Envelope env) throws NoninvertibleTransformException {
		WorkbenchContext context = JUMPWorkbench.getInstance().getFrame().getContext();
		LayerViewPanel layerViewPanel = context.getLayerViewPanel();
		if (layerViewPanel != null && env != null) {
			layerViewPanel.getViewport().zoom(env);
		}
	}

	/**
	 * Method to zoom to selected features using OJ margin of the view
	 * 
	 * @param features
	 * @throws NoninvertibleTransformException
	 */

	public static void zoomFeatures(List<Feature> features) throws NoninvertibleTransformException {
		Envelope env = null;

		for (Feature feature : features) {
			Geometry geometry = feature.getGeometry();

			if (geometry != null) {
				if (env == null)
					env = geometry.getEnvelopeInternal();
				else
					env.expandToInclude(geometry.getEnvelopeInternal());
			}
		}
		if (env != null) {
			env.expandBy(zoomBufferAsExtentFraction(features));
			zoom(env);
		}
	}

	/**
	 * Zooms a bunch of features with a defined margin of the view in pixel
	 * 
	 * @param features
	 * @param margin
	 * @throws NoninvertibleTransformException
	 */
	public static void zoomFeatures(List<Feature> features, double margin) throws NoninvertibleTransformException {
		Envelope env = null;
		for (Feature feature : features) {
			Geometry geometry = feature.getGeometry();
			if (geometry != null) {
				if (env == null)
					env = geometry.getEnvelopeInternal();
				else
					env.expandToInclude(geometry.getEnvelopeInternal());
			}
		}
		if (env != null) {
			env.expandBy(margin);
			zoom(env);
		}
	}

	/**
	 * Method to call ZoomToSelectedItemsPlugIn
	 * 
	 * @throws Exception
	 */
	public static void zoomToSelectedFeatures() throws Exception {
		ZoomToSelectedItemsPlugIn zoom = new ZoomToSelectedItemsPlugIn();
		zoom.execute(JUMPWorkbench.getInstance().getContext().createPlugInContext());
	}

	/**
	 * Method to zoom to a list of coordinates
	 * 
	 * @param coord
	 * @throws NoninvertibleTransformException
	 */

	public static void zoomToCoordinates(Coordinate[] coord) throws NoninvertibleTransformException {
		Envelope envelope;
		Geometry geometry = new GeometryFactory().createLineString(coord);
		envelope = geometry.getEnvelopeInternal();
		zoom(envelope);
	}

	/*
	 * Inner method used bu GeneralUtils.zoomFeatures
	 * 
	 * @param features
	 * 
	 * @return
	 */
	private static double zoomBufferAsExtentFraction(List<Feature> features) {
		double averageExtent = averageExtent(features);
		double averageFullExtent = averageFullExtent(features);
		if (averageFullExtent == 0) {
			return 0;
		} else if (averageExtent == 0) {
			return 0.1;
		} else {
			return 0.5 * Math.sqrt(averageExtent / averageFullExtent);
		}
	}

	/*
	 * Inner method used bu GeneralUtils.zoomFeatures
	 * 
	 * @param features
	 * 
	 * @return
	 */
	private static double averageExtent(List<Feature> features) {
		Assert.isTrue(!features.isEmpty());
		double extentSum = 0;
		for (Feature feature : features) {
			Geometry geometry = feature.getGeometry();

			extentSum += geometry.getEnvelopeInternal().getWidth();
			extentSum += geometry.getEnvelopeInternal().getHeight();
		}
		return extentSum / (2d * features.size());
	}

	/*
	 * Inner method used bu GeneralUtils.zoomFeatures
	 * 
	 * @param features
	 * 
	 * @return
	 */
	private static double averageFullExtent(List<Feature> features) {
		Envelope envelope = new Envelope();
		for (Feature feature : features) {
			Geometry geometry = feature.getGeometry();
			if (geometry != null) {
				if (envelope == null)
					envelope = geometry.getEnvelopeInternal();
				else
					envelope.expandToInclude(geometry.getEnvelopeInternal());
			}
		}
		return (envelope.getWidth() + envelope.getHeight()) / 2d;
	}

	/**
	 * gets the SelectionManager if there's an active view
	 * 
	 * @return
	 */
	public static SelectionManager getFrontViewSelectionManager() {
		WorkbenchContext context = JUMPWorkbench.getInstance().getFrame().getContext();
		LayerViewPanel layerViewPanel = context.getLayerViewPanel();
		if (layerViewPanel != null) {
			return layerViewPanel.getSelectionManager();
		} else {
			return null;
		}
	}

	public static List<Layer> getActiveViewLayers() {
		return JUMPWorkbench.getInstance().getFrame().getContext().getLayerManager().getLayers();
	}

	public static List<Layer> getActiveNotRasterLayers() {
		return getActiveViewLayers();
	}

	/**
	 * gets the envelope of the active view
	 * 
	 * @return
	 */
	public static Envelope getActiveViewportEnvelope() {
		WorkbenchContext context = JUMPWorkbench.getInstance().getFrame().getContext();
		LayerViewPanel layerViewPanel = context.getLayerViewPanel();
		if (layerViewPanel != null) {
			return layerViewPanel.getViewport().getEnvelopeInModelCoordinates();
		}
		return null;
	}

	/**
	 * Gets the last not delegating tool in a delegating tool.
	 * 
	 * @param ct
	 * @return
	 */
	public static CursorTool getLastNotDelegatingCursorTool(CursorTool ct) {
		if (ct == null)
			return null;
		if (ct instanceof DelegatingTool) {
			return getLastNotDelegatingCursorTool(((DelegatingTool) ct).getDelegate());
		} else if (ct instanceof LeftClickFilter) {
			return getLastNotDelegatingCursorTool(((LeftClickFilter) ct).getWrappee());
		} else {
			return ct;
		}
	}

	/**
	 * Does not invalidate the prior selection (features are added to the group of
	 * selected features).
	 */
	public static void selectFeaturesInLayer(Layer layer, Collection<Feature> features) {
		WorkbenchContext context = JUMPWorkbench.getInstance().getFrame().getContext();
		LayerViewPanel layerViewPanel = context.getLayerViewPanel();
		if (layerViewPanel != null) {
			SelectionManager selectionManager = layerViewPanel.getSelectionManager();
			selectionManager.getFeatureSelection().selectItems(layer, features);
		}
	}

	/**
	 * quits from OpenJUMP
	 */
	// [Giuseppe Aruta 2021-04-28] Deactivated as it is not used
	// public static void quitOpenJUMP() {
	// JUMPWorkbench.getInstance().getFrame()..getApplicationExitHandler()
	// .exitApplication(JUMPWorkbench.getInstance().getFrame());
	// }

	/**
	 * Method to add quasimodo tools (select, zoom, pan) by keyboard to tools
	 * 
	 * @param tool
	 * @return
	 */
	public static QuasimodeTool addStandardQuasimodes(CursorTool tool) {
		QuasimodeTool quasimodeTool = tool instanceof QuasimodeTool ? (QuasimodeTool) tool : new QuasimodeTool(tool);
		quasimodeTool.add(new ModifierKeySpec(false, false, true), new ZoomTool());
		quasimodeTool.add(new ModifierKeySpec(false, true, true), new PanTool());
		SelectFeaturesTool selectFeaturesTool = new SelectFeaturesTool() {
			@Override
			protected boolean selectedLayersOnly() {
				return false;
			}
		};
		quasimodeTool.add(new ModifierKeySpec(true, false, false), selectFeaturesTool);
		quasimodeTool.add(new ModifierKeySpec(true, true, false), selectFeaturesTool);
		quasimodeTool.add(new ModifierKeySpec(true, false, true), new FeatureInfoTool());
		return quasimodeTool;
	}

	/**
	 * Get the wrapped tool (if any) from the given cursor tool
	 * 
	 * @param currentCursorTool
	 * @return
	 */
	public static CursorTool getWrappedTool(CursorTool currentCursorTool) {

		CursorTool wrappedCursorTool = null;

		if (currentCursorTool instanceof QuasimodeTool) {
			CursorTool defaultCursorTool = ((QuasimodeTool) currentCursorTool).getDefaultTool();
			if (defaultCursorTool instanceof LeftClickFilter) {
				CursorTool wrappeeCursorTool = ((LeftClickFilter) defaultCursorTool).getWrappee();

				if (wrappeeCursorTool instanceof DelegatingTool) {
					wrappeeCursorTool = ((DelegatingTool) wrappeeCursorTool).getDelegate();
				}

				wrappedCursorTool = wrappeeCursorTool;
			}
		} else if (currentCursorTool instanceof DelegatingTool) {
			DelegatingTool delegatingTool = (DelegatingTool) currentCursorTool;
			wrappedCursorTool = delegatingTool.getDelegate();

		} else {
			wrappedCursorTool = currentCursorTool;
		}

		return wrappedCursorTool;
	}

	/**
	 * Shows a geometry in a fence layer
	 * 
	 * @param geom
	 */
	public static void showFence(Geometry geom) {
		FenceLayerFinder fenceLayerFinder = new FenceLayerFinder(
				JUMPWorkbench.getInstance().getFrame().getContext().createPlugInContext());
		fenceLayerFinder.setFence(geom);
		fenceLayerFinder.getLayer().setVisible(true);
	}

	/**
	 * Activates a tool
	 * 
	 * @param tool
	 */
	public static void activateTool(CursorTool tool) {
		PlugInContext pContext = JUMPWorkbench.getInstance().getFrame().getContext().createPlugInContext();
		pContext.getLayerViewPanel().setCurrentCursorTool(tool);
	}

	/**
	 * Check that all the selected features are only from one layer.<This method
	 * should be moved to
	 * com.vividsolutions.jump.workbench.plugin.EnableCheckFactory>
	 * 
	 * @param layerName name of the layer
	 * @return
	 */

	public static boolean checkAllSelectedFeaturesAreInLayer(final String layerName) {

		Layer layer = JUMPWorkbench.getInstance().getFrame().getContext().getLayerManager().getLayer(layerName);

		if (layer == null) {
			String msg = I18NPlug.getMessage(
					"com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.layer-{0}-does-not-exist", //$NON-NLS-1$
					layerName);
			JUMPWorkbench.getInstance().getFrame().warnUser(msg);
			return false;
		}
		int numSelectedFeatures = WorkbenchUtils.getSelectedFeatures().size();
		int numSelectedFeaturesInLayer = WorkbenchUtils.getSelectedFeatures(layer).size();
		if (numSelectedFeatures != numSelectedFeaturesInLayer) {
			String msg = I18NPlug.getMessage(
					"com.vividsolutions.jump.workbench.plugin.EnableCheckFactory.All-the-selected-features-must-be-at-the-layer-{0}", //$NON-NLS-1$
					layerName);
			JUMPWorkbench.getInstance().getFrame().warnUser(msg);
			return false;
		}

		return true;

	}

	public static EnableCheck createExactlyNItemsMustBeSelectedCheck(final int n) {
		return new EnableCheck() {
			@Override
			public String check(JComponent component) {
				String msg;
				if (n == 1) {
					msg = get("com.vividsolutions.jump.workbench.plugin.Exactly-one-item-must-be-selected");
				} else {
					msg = getMessage("com.vividsolutions.jump.workbench.plugin.Exactly-n-items-must-be-selected",
							n);
				}
				return (n != ((SelectionManagerProxy) JUMPWorkbench.getInstance().getFrame().getContext().getWorkbench()
						.getFrame().getActiveInternalFrame()).getSelectionManager().getSelectedItemsCount()) ? msg
								: null;
			}
		};
	}

	public static boolean checkActiveTaskWindow() {
		if (!(JUMPWorkbench.getInstance().getFrame().getActiveInternalFrame() instanceof TaskFrame)) {
			JUMPWorkbench.getInstance().getFrame()
					.warnUser(I18N.get("com.vividsolutions.jump.workbench.plugin.A-Task-Window-must-be-active"));
			return false;
		} else {
			return true;
		}

	}

	/**
	 * General Check boolean to use with PlugIns
	 * 
	 * @param check
	 * @return
	 */
	public static boolean check(EnableCheck check) {
		String warning = check.check(null);
		if (warning != null) {
			JUMPWorkbench.getInstance().getFrame().getContext().getLayerViewPanel().getContext().warnUser(warning);
			return false;
		}
		return true;
	}

	///**
	// * Test Logger
	// *
	// * @param plugin
	// * @param e
	// */
	//public static void Logger(Class<?> plugin, Exception e) {
	//	Logger LOG = Logger.getLogger(plugin);
	//	JUMPWorkbench.getInstance().getFrame().warnUser(plugin.getSimpleName() + " Exception: " + e.toString());
	//	LOG.error(plugin.getName() + " Exception: ", e);
	//}

	public static Cursor getCursor(String name) {
		if (Toolkit.getDefaultToolkit().getBestCursorSize(32, 32).equals(new Dimension(0, 0))) {
			return Cursor.getDefaultCursor();
		}
		return Toolkit.getDefaultToolkit().createCustomCursor(
				com.vividsolutions.jump.workbench.ui.images.IconLoader.icon("Pen.gif").getImage(),
				new java.awt.Point(1, 31), name);
	}

	/**
	 * Formatted Text Field using US standard
	 * 
	 * @param length
	 * @return
	 */
	public static JFormattedTextField getUSFormatedNumberTextField(int length) {
		NumberFormat format = NumberFormat.getInstance(Locale.US);
		format.setGroupingUsed(false);
		JFormattedTextField numberTextFiel = new JFormattedTextField(format);
		numberTextFiel.setPreferredSize(new Dimension(length, numberTextFiel.getPreferredSize().height));
		return numberTextFiel;
	}

	/**
	 * <Should be moved to ViewPort.class>
	 * 
	 * @param d
	 * @return
	 */

	public static double viewportToMapDistance(int d) {
		double dist = -1;
		Viewport viewport = JUMPWorkbench.getInstance().getFrame().getContext().getLayerViewPanel().getViewport();
		try {
			dist = d / viewport.getModelToViewTransform().getScaleX();
		} catch (NoninvertibleTransformException e) {
			Logger.warn("", e);
		}

		return dist;
	}

	/**
	 * <Should be moved to ViewPort.class>
	 * 
	 * @param modelCoordinate
	 * @return
	 * @throws NoninvertibleTransformException
	 */
	public static Point2D viewportToViewPoint(Coordinate modelCoordinate) throws NoninvertibleTransformException {
		Viewport viewport = JUMPWorkbench.getInstance().getFrame().getContext().getLayerViewPanel().getViewport();
		return viewport.toViewPoint(new Point2D.Double(modelCoordinate.x, modelCoordinate.y));
	}

	static WorkbenchContext context = JUMPWorkbench.getInstance().getContext();

	public static JConsole console = new JConsole();

	/**
	 * This code allows to load Python console and tools into a generic
	 * ToolboxDialog
	 * 
	 * @param toolbox
	 */
	public static void loadPython(ToolboxDialog toolbox) {
		// Do not add console in the CAD Toolbox (available through custom menu)
		//console.setPreferredSize(new Dimension(450, 120));
		//// PythonToolsPlugIn pPlugIn = new PythonToolsPlugIn();
		//console.println(IconLoader.icon("jython_small_c.png"));
		//// new ImageIcon(this.getClass().getResource("jython_small_c.png")));
		//toolbox.getCenterPanel().add(console, BorderLayout.CENTER);
		//toolbox.setTitle("Jython");
		// setup the interpreter
		ClassLoader classLoader = context.getWorkbench().getPlugInManager().getPlugInClassLoader();
		Properties preProperties = new Properties(System.getProperties());
		String homepath = preProperties.getProperty("user.home");

		String sep = File.separator;
		// -- [sstein] - old */
		/*
		 * String WORKING_DIR = empty.getAbsoluteFile().getParent() + sep; String
		 * jarpathX = new String(WORKING_DIR + "lib"); String startuppathX = new
		 * String(WORKING_DIR + "lib" + sep + "ext" + sep + "jython" + sep);
		 */
		// -- [sstein] - new
		File plugInDirectory = context.getWorkbench().getPlugInManager().getExtensionDirs().get(0);// .getPlugInDirectory();
		String jarpath = plugInDirectory.getPath();
		String startuppath = plugInDirectory.getPath() + sep + "jython" + sep;

		Properties postProperties = new Properties();
		postProperties.put("python.home", homepath);
		postProperties.put("python.path", startuppath);
		PySystemState.initialize(preProperties, postProperties, new String[] { "" }, classLoader);
		String startupfile = startuppath + "CADstartup.py";
		PySystemState.add_extdir(jarpath);
		PySystemState.add_extdir(jarpath + sep + "ext");
		PythonInteractiveInterpreter interpreter = new PythonInteractiveInterpreter(console);
		interpreter.set("wc", context);
		interpreter.set("toolbox", toolbox);
		interpreter.set("startuppath", startuppath);
		interpreter.exec("import sys");
		JUMP_GIS_Framework.setWorkbenchContext(context);
		ModifyGeometry.setWorkbenchContext(context);
		toolbox.addToolBar(); // add a new tool bar to the console
		JMenuBar jMenuBar = new JMenuBar();
		jMenuBar.add(new JMenu(MenuNames.TOOLS));
		toolbox.setJMenuBar(jMenuBar);
		if (new File(startupfile).exists())
			interpreter.execfile(startupfile);
		new Thread(interpreter).start();
	}

}
