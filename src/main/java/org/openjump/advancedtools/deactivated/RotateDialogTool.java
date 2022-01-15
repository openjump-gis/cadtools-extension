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
package org.openjump.advancedtools.deactivated;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.vividsolutions.jump.I18N;

import org.openjump.advancedtools.CadExtension;
import org.openjump.advancedtools.gui.RotateDialog;
import org.openjump.advancedtools.icon.IconLoader;
import org.openjump.advancedtools.tools.ConstrainedNClickTool;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.snap.SnapIndicatorTool;

/**
 * Tool to rotate elements defing parameters
 * <p>
 * User can select a point of rotation and than the angle using a dialog
 * </p>
 * 
 * @since Kosmo 1.0.0
 * @author Giuseppe Aruta [January 30th 2017] rewrite code to adapt to OpenJUMP
 *         1.10 (http://www.openjump.org/support.html)
 * @since OpenJUMP 1.10 (2017)
 */
public class RotateDialogTool extends ConstrainedNClickTool {

    private static final I18N i18n = CadExtension.I18N;

    /** Nombre asignado a la herramienta */
    public final static String NAME = i18n
            .get("org.openjump.core.ui.tools.RotateDialogTool.Rotate-by-given-angle");
    /** Description of the tool */
    public final static String DESCRIPTION = i18n
            .get("org.openjump.core.ui.tools.RotateDialogTool.description");
    /** Icon of the tool */
    public final static ImageIcon ICON = IconLoader.icon("rotateByAngle.png"); 

    /** Cursor to select a point of rotation */
    public static final Cursor DEFAULT_CURSOR = createCursor(
            IconLoader.icon("refreshAnchorCursor.png").getImage(), 
            new java.awt.Point(0, 0));

    /** Coordenada central */
    protected Coordinate centerCoord;

    /** Sentido de reloj o inverso */
    protected boolean clockwise = true;

    /** Angulo de rotacion que se desea aplicar */
    protected double fullAngle = 0.0;

    /** Indicador de snap */
    protected SnapIndicatorTool snapIndicatorTool;

    /** Elementos para rotar */
    protected List<Feature> featsToRotate;

    /**
     * Constructor de la herramienta
     */

    public RotateDialogTool(EnableCheckFactory checkFactory) {
        super(JUMPWorkbench.getInstance().getContext(), 1);
        this.checkFactory = checkFactory;
        allowSnapping();
    }

    EnableCheckFactory checkFactory;

    @Override
    public Cursor getCursor() {
        return DEFAULT_CURSOR;
    }

    protected void gestureFinished() throws java.lang.Exception {
        reportNothingToUndoYet();

        if (!check(checkFactory.createSelectedItemsLayersMustBeEditableCheck())) {
            return;
        }
        if (!check(checkFactory.createAtLeastNFeaturesMustBeSelectedCheck(1))) {
            return;
        }
        RotateDialog ad = new RotateDialog(JUMPWorkbench.getInstance()
                .getFrame());
        if (!ad.cancelado) {
            fullAngle = ad.getAngle();
            centerCoord = getClickedPoint().getCoordinate();
            ArrayList<EditTransaction> transactions = new ArrayList<>();
            for (Layer layerWithSelectedItems :
                    getPanel().getSelectionManager().getLayersWithSelectedItems()) {
                transactions.add(createTransaction(layerWithSelectedItems));
            }
            EditTransaction.commit(transactions);
        }
    }

    private EditTransaction createTransaction(Layer layer) {
        EditTransaction transaction = EditTransaction
                .createTransactionOnSelection(
                        new EditTransaction.SelectionEditor() {
                            public Geometry edit(
                                    Geometry geometryWithSelectedItems,
                                    Collection selectedItems) {
                                for (Iterator j = selectedItems.iterator(); j
                                        .hasNext();) {
                                    Geometry item = (Geometry) j.next();
                                    rotate(item);
                                }
                                return geometryWithSelectedItems;
                            }
                        }, getPanel(), getPanel().getContext(), getName(),
                        layer, isRollingBackInvalidEdits(), false);
        return transaction;
    }

    /**
     * Devuelve el icono asociado a la herramienta
     * 
     * @return Icon - Icono asociado a la herramienta
     */
    public Icon getIcon() {
        return ICON;
    }

    public String getName() {
        String tooltip = "";
        tooltip = "<HTML><BODY>";
        tooltip += "<DIV style=\"width: 300px; text-justification: justify;\">";
        tooltip += "<b>" + NAME + "</b>" + "<br>";
        tooltip += DESCRIPTION + "<br>";
        tooltip += "</DIV></BODY></HTML>";
        return tooltip;
    }

    /**
     * 
     */
    public static MultiEnableCheck createEnableCheck(
            final WorkbenchContext workbenchContext, AbstractCursorTool tool) {

        MultiEnableCheck solucion = new MultiEnableCheck();

        EnableCheckFactory checkFactory =
            workbenchContext.createPlugInContext().getCheckFactory();

        // al menos una capa debe tener elementos activos
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck())
                .add(checkFactory
                        .createWindowWithLayerManagerMustBeActiveCheck())
                .add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
        solucion.add(checkFactory
                .createSelectedItemsLayersMustBeEditableCheck());
        solucion.add(checkFactory.createAtLeastNFeaturesMustBeSelectedCheck(1));
        return solucion;
    }

    private static GeometryFactory geomFac = new GeometryFactory();

    /**
     * Calcula el punto donde se ha pulsado
     *
     * @return Point - Punto donde se ha pulsado
     */
    public Point getClickedPoint() {
        Coordinate c = snap(getModelSource());
        return geomFac.createPoint(c);
    }

    /**
     * Rota la geometria aplicandole el filtro de rotacion correspondiente
     *
     * @param geometry
     *            Geometria que se desea rotar
     */
    protected void rotate(Geometry geometry) {
        geometry.apply(new CoordinateFilter() {
            public void filter(Coordinate coordinate) {
                double cosAngle = Math.cos(fullAngle);
                double sinAngle = Math.sin(fullAngle);
                double x = coordinate.x - centerCoord.x;
                double y = coordinate.y - centerCoord.y;
                coordinate.x = centerCoord.x + (x * cosAngle) + (y * sinAngle);
                coordinate.y = centerCoord.y + (y * cosAngle) - (x * sinAngle);
            }
        });
        geometry.geometryChanged();
    }

}
