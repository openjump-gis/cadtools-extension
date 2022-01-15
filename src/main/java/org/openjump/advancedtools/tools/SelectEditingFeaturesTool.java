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
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.openjump.advancedtools.CadExtension;
import org.openjump.advancedtools.icon.IconLoader;
import org.openjump.advancedtools.plugins.CalculateSelectionPlugIn;
import org.openjump.advancedtools.utils.WorkbenchUtils;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.Logger;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.SelectTool;
import com.vividsolutions.jump.workbench.ui.renderer.FeatureSelectionRenderer;

/**
 * Tool to select items on editing layer
 * 
 * @author Marco Antonio Fuentelsaz
 * @since Kosmo 1.0.0
 * @author Giuseppe Aruta rewrite code to adapt to OpenJUMP 1.10
 *         (http://www.openjump.org/support.html)
 * @since OpenJUMP 1.10
 */
public class SelectEditingFeaturesTool extends SelectTool {

    private static final I18N i18n = CadExtension.I18N;

    /** Nombre asociado a la herramienta */
    public final static String NAME = i18n
        .get("org.openjump.core.ui.tools.Select.Select-editing-features");
    /** Nombre asociado a la herramienta */
    public final static String NAME2 = i18n
        .get("org.openjump.core.ui.tools.Select.Select-editing-features.description");

    /** Icono asociado a la herramienta */
    public final static Icon ICON = IconLoader.icon("selectEditing.png");

    /** Cursor asociado a la herramienta */
    public final static Cursor CURSOR = createCursor(
            (IconLoader.icon("selectEditingCursor.png")).getImage(), new Point(
                    0, 0));


    public SelectEditingFeaturesTool() {
        super(JUMPWorkbench.getInstance().getContext(), FeatureSelectionRenderer.CONTENT_ID);

        WorkbenchFrame frameInstance = JUMPWorkbench.getInstance().getFrame();
        if (frameInstance != null)
            registerSelectKey(frameInstance.getContext());
    }

    @Override
    public String getName() {

        String tooltip;
        tooltip = "<HTML><BODY>";
        tooltip += "<DIV style=\"width: 300px; text-justification: justify;\">";
        tooltip += "<b>" + NAME + "</b>" + "<br>";
        tooltip += NAME2 + "<br>";
        tooltip += "</DIV></BODY></HTML>";
        return tooltip;
    }

    /**
     * Devuelve el icono asociado a la herramienta
     * 
     * @return Icon - Icono asociado a la herramienta
     */
    @Override
    public Icon getIcon() {
        return ICON;
    }

    /**
     * Devuelve el cursor asociado a la herramienta
     * 
     * @return Cursor - Cursor asociado a la herramienta
     */
    @Override
    public Cursor getCursor() {
        return CURSOR;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        try {
            super.mouseClicked(e);
            setViewSource(e.getPoint());
            setViewDestination(e.getPoint());
            fireGestureFinished();
        } catch (Throwable t) {
            getPanel().getContext().handleThrowable(t);
        }
    }


    @Override
    public void activate(LayerViewPanel layerViewPanel) {
        super.activate(layerViewPanel);
        if (!(JUMPWorkbench.getInstance().getFrame().getActiveInternalFrame() instanceof TaskFrame)) {
            JUMPWorkbench
                    .getInstance()
                    .getFrame()
                    .warnUser(
                            I18N.JUMP.get("com.vividsolutions.jump.workbench.plugin.A-Task-Window-must-be-active"));
            return;

        }
        selection = layerViewPanel.getSelectionManager().getFeatureSelection();
    }


    @Override
    protected void gestureFinished() throws NoninvertibleTransformException {

        try {
            selectFeatures(EnvelopeUtil.toGeometry(getBoxInModelCoordinates()));
        } catch (Exception e) {
            try {
                WorkbenchUtils.discardSelection();
            } catch (Exception e1) {
                Logger.warn(e1);
            }
            Logger.warn(e);
        }
    }

    /**
     * Selecciona los elementos de la capa que intersectan con la geometria de
     * seleccion
     *
     * @param selectionGeom selected geometry
     * @throws Exception
     */
    protected void selectFeatures(Geometry selectionGeom) throws Exception {
        reportNothingToUndoYet();

        if (WorkbenchUtils.getSelectedLayers().size() > 1) {

        }
        // Filtramos por la capa en edicion
        CalculateSelectionPlugIn calculateSelectionPlugIn = new CalculateSelectionPlugIn(
                NAME, wasShiftPressed(), selectionGeom, selection,
                getLayersFilter());

        calculateSelectionPlugIn.execute(JUMPWorkbench.getInstance().getFrame()
                .getContext().createPlugInContext());
    }

    EnableCheckFactory checkFactory =
        JUMPWorkbench.getInstance().getContext().createPlugInContext().getCheckFactory();

    @Override
    public void mousePressed(MouseEvent e) {
        try {
            if (!check(checkFactory.createTaskWindowMustBeActiveCheck())) {
                return;
            }
            if (!check(checkFactory
                    .createWindowWithLayerManagerMustBeActiveCheck())) {
                return;
            }
            if (!check(checkFactory
                    .createExactlyNLayerablesMustBeSelectedCheck(1, Layer.class))) {
                return;
            }

            super.mousePressed(e);
        } catch (Throwable t) {
            getPanel().getContext().handleThrowable(t);
        }
    }

    protected void deselectFeatures(Geometry selectionGeom) {
        reportNothingToUndoYet();
        WorkbenchUtils.discardSelection();
        // Filtramos por la capa en edicion
        /*
         * ClearLayerSelectionPlugIn calculateSelectionPlugIn = new
         * ClearLayerSelectionPlugIn( selection, getLayersFilter());
         * 
         * calculateSelectionPlugIn.execute(JUMPWorkbench.getInstance().getFrame(
         * ) .getContext().createPlugInContext());
         */
    }

    /**
     * Obtiene el filtro de capas para la consulta. En este caso, por la capa en
     * edicion
     *
     * @return
     */
    protected List<Layerable> getLayersFilter() {
        List<Layerable> layerFilter = new ArrayList<>();
        Collection<Layer> editableLayers = getPanel().getLayerManager()
                .getEditableLayers();
        if (editableLayers.isEmpty()) {
            return null;
        }
        Layer editableLayer = editableLayers.iterator().next();
        layerFilter.add(editableLayer);

        return layerFilter;
    }

    /**
     *
     */
    public static MultiEnableCheck createEnableCheck(
            final WorkbenchContext workbenchContext, CursorTool tool) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory =
            workbenchContext.createPlugInContext().getCheckFactory();
        // al menos una capa debe tener elementos activos
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        // solo una capa puede tener elementos seleccionados.
        solucion.add(checkFactory.createAtLeastNLayersMustExistCheck(1));
        return solucion;
    }

    /**
     * Registra la tecla correspondiente que permite seleccionar el elemento
     * situado bajo el cursor
     *
     * @param context
     *            Contexto de la aplicacion
     */
    private void registerSelectKey(final WorkbenchContext context) {
        final EnableCheck enableCheck = createEnableCheck(context, this);
        context.getWorkbench().getFrame().addEasyKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                // Nothing to do
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (context.getWorkbench().getFrame().getActiveInternalFrame() instanceof TaskFrame) {
                    if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
                        Coordinate coord = null;
                        try {
                            coord = getPanel().getViewport().toModelCoordinate(
                                    getViewSource());
                        } catch (NoninvertibleTransformException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        setControlPressed(e.isControlDown());
                        setShiftPressed(e.isShiftDown());

                        if (enableCheck.check(null) != null) {
                            return;
                        }
                        context.getLayerManager().getUndoableEditReceiver()
                                .startReceiving();
                        try {
                            Point2D point = getPanel().getViewport()
                                    .toViewPoint(coord);
                            // Point2D point = getPanel().getMousePosition();
                            Envelope env = new Envelope(point.getX()
                                    - modelClickBuffer(), point.getX()
                                    + modelClickBuffer(), point.getY()
                                    - modelClickBuffer(), point.getY()
                                    + modelClickBuffer());

                            selectFeatures(EnvelopeUtil.toGeometry(env));
                        } catch (Exception x) {
                            Logger.warn(x);
                        } finally {
                            context.getLayerManager().getUndoableEditReceiver()
                                    .stopReceiving();
                        }
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Nothing to do
            }
        });
    }

}