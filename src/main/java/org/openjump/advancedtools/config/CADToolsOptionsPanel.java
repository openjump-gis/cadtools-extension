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
package org.openjump.advancedtools.config;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.vividsolutions.jump.I18N;
import org.openjump.advancedtools.utils.NumberSpinner;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.widgets.config.ConfigTooltipPanel;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;

/**
 * Panel de opciones de las herramientas CAD
 * <p>
 * </p>
 * 
 * @author Gabriel Bellido Perez
 * @since Kosmo 1.0.0
 */
public class CADToolsOptionsPanel extends JPanel implements OptionsPanel {

    private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");

    /** long serialVersionUID field */
    private static final long serialVersionUID = 1L;

    private final JLabel bufferRatio = new JLabel(
        i18n.get("org.openjump.core.ui.config.CADToolsOptionsPanel.ExtendLineOptions.Tolerance-radius"));

    /** Permite seleccionar el radio del buffer */
    private NumberSpinner ratioSpinner;

    public final static String ShortenLinesOptions = i18n
        .get("org.openjump.core.ui.config.CADToolsOptionsPanel.ExtendLineOptions");
    public final static String ClosedGeometriesOptions = i18n
        .get("org.openjump.core.ui.config.CADToolsOptionsPanel.ClosedGeometryOptions");
    public final static String SelectFeaturesOptions = i18n
        .get("org.openjump.core.ui.config.CADToolsOptionsPanel.SelectOptions");
    public final static String EditingFeaturesOptions = i18n
        .get("org.openjump.core.ui.config.CADToolsOptionsPanel.EditingOptions");

    /** Allows to decide if the lines must be broken at the junction */
    private final JCheckBox unionCheckbox = new JCheckBox(
        i18n.get("org.openjump.core.ui.config.CADToolsOptionsPanel.ExtendLineOptions.break-lines-create-new-vertices-in-crosses"));
    private final JCheckBox centerCheckBox = new JCheckBox(
        i18n.get("org.openjump.core.ui.config.CADToolsOptionsPanel.ClosedGeometryOptions.draw-center-as-point"));
    private final JCheckBox polygonCheckBox = new JCheckBox(
        i18n.get("org.openjump.core.ui.config.CADToolsOptionsPanel.ClosedGeometryOptions.draw-as-filled-polygon"));
    private final JCheckBox allSelectedFeaturesCheckBox = new JCheckBox(
        i18n.get("org.openjump.core.ui.config.CADToolsOptionsPanel.SelectOptions.Allows-selection-features"));

    private final JCheckBox showPanleCheckBox = new JCheckBox(
            i18n.get("org.openjump.core.ui.config.CADToolsOptionsPanel.EditingOptions.show-panel"));

    /** Key for length of buffer */
    public final static String EXTEND_LINE_BUFFER_KEY = CADToolsOptionsPanel.class
            .getName() + " - EXTEND LINE BUFFER";
    /** Key for drawing center as point */
    public final static String CENTERCHECK_KEY = CADToolsOptionsPanel.class
            .getName() + " - DRAW CENTER AS POINT";
    /** Key for drawing center as point */
    public final static String POLYGONCHECK_KEY = CADToolsOptionsPanel.class
            .getName() + " - DRAW GEOMETRY AS POLYGON";
    /** Key for drawing center as point */
    public final static String SELFEATSCHECK_KEY = CADToolsOptionsPanel.class
            .getName() + " - ONE LAYER ONLY";
    /** Clave para la edicion concurrente */
    public final static String SHOW_ANGLE_AND_LENGHT_KEY = ConfigTooltipPanel.class
            .getName() + " - SHOW_ANGLE_AND_LENGHT";
    /**
     * Clave que indica si se desea partir las lineas correspondientes en la
     * union
     */
    public final static String EXTEND_LINE_UNION_KEY = CADToolsOptionsPanel.class
            .getName() + " - EXTEND LINE UNION";

    /** Panel de las opciones de las herramientas Extender/Acortar lineas */
    private JPanel extentShortenOptionsPanel;

    private JPanel saveOptionsPanel;
    private JPanel editOptionsPanel;
    private JPanel selectedOptionsPanel;

    //private JCheckBox preguntarAntesDeCortarCheckBox;

    //private JPanel moveOptionsPanel;

    /**
     * 
     */
    public CADToolsOptionsPanel() {
        this.setLayout(new GridBagLayout());
        // setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 0, 0, getGeometryOptionsPanel());
        FormUtils.addRowInGBL(this, 2, 0, getExtentShortenOptionsPanel());
        // FormUtils.addRowInGBL(this, 4, 0, getSelectedOptionsPanel());
        // FormUtils.addRowInGBL(this, 6, 0, getShowAngleAndLenghtPanel());
        FormUtils.addFiller(this, 6, 0);
    }

    /**
     *
     */
    public JPanel getExtentShortenOptionsPanel() {
        if (extentShortenOptionsPanel == null) {
            extentShortenOptionsPanel = new JPanel(new GridBagLayout());
            TitledBorder titledBorder2 = new TitledBorder(
                    BorderFactory.createEtchedBorder(Color.white, new Color(
                            148, 145, 140)), ShortenLinesOptions);
            extentShortenOptionsPanel.setBorder(titledBorder2);
            ratioSpinner = new NumberSpinner(0.0d, 0.0d, Double.MAX_VALUE, 0.1d);
            ratioSpinner.setPreferredSize(new Dimension(80, ratioSpinner
                    .getPreferredSize().height));

            FormUtils.addRowInGBL(extentShortenOptionsPanel, 0, 0, bufferRatio,
                    ratioSpinner);
            FormUtils.addRowInGBL(extentShortenOptionsPanel, 1, 0,
                    unionCheckbox);

        }

        return extentShortenOptionsPanel;
    }

    public JPanel getGeometryOptionsPanel() {
        if (saveOptionsPanel == null) {
            saveOptionsPanel = new JPanel(new GridBagLayout());
            TitledBorder titledBorder1 = new TitledBorder(
                    BorderFactory.createEtchedBorder(Color.white, new Color(
                            148, 145, 140)), ClosedGeometriesOptions);
            saveOptionsPanel.setBorder(titledBorder1);
            FormUtils.addRowInGBL(saveOptionsPanel, 0, 0, centerCheckBox);
            FormUtils.addRowInGBL(saveOptionsPanel, 1, 0, polygonCheckBox);
        }

        return saveOptionsPanel;
    }

    public JPanel getShowAngleAndLengthPanel() {
        if (editOptionsPanel == null) {
            editOptionsPanel = new JPanel(new GridBagLayout());
            TitledBorder titledBorder1 = new TitledBorder(
                    BorderFactory.createEtchedBorder(Color.white, new Color(
                            148, 145, 140)), EditingFeaturesOptions);
            editOptionsPanel.setBorder(titledBorder1);
            FormUtils.addRowInGBL(editOptionsPanel, 0, 0, showPanleCheckBox);
        }

        return editOptionsPanel;
    }

    public JPanel getSelectedOptionsPanel() {
        if (selectedOptionsPanel == null) {
            selectedOptionsPanel = new JPanel(new GridBagLayout());
            TitledBorder titledBorder1 = new TitledBorder(
                    BorderFactory.createEtchedBorder(Color.white, new Color(
                            148, 145, 140)), SelectFeaturesOptions);
            selectedOptionsPanel.setBorder(titledBorder1);
            FormUtils.addRowInGBL(selectedOptionsPanel, 0, 0,
                    allSelectedFeaturesCheckBox);

        }

        return selectedOptionsPanel;
    }

    /**
     * 
     */
    @Override
    public String validateInput() {
        return null;
    }

    /**
     * 
     */
    @Override
    public void okPressed() {
        PersistentBlackboardPlugIn.get(
                JUMPWorkbench.getInstance().getFrame().getContext()).put(
                EXTEND_LINE_BUFFER_KEY, ratioSpinner.getDoubleValue());
        PersistentBlackboardPlugIn.get(
                JUMPWorkbench.getInstance().getFrame().getContext()).put(
                EXTEND_LINE_UNION_KEY, unionCheckbox.isSelected());
        PersistentBlackboardPlugIn.get(
                JUMPWorkbench.getInstance().getFrame().getContext()).put(
                POLYGONCHECK_KEY, polygonCheckBox.isSelected());
        PersistentBlackboardPlugIn.get(
                JUMPWorkbench.getInstance().getFrame().getContext()).put(
                CENTERCHECK_KEY, centerCheckBox.isSelected());
        PersistentBlackboardPlugIn.get(
                JUMPWorkbench.getInstance().getFrame().getContext()).put(
                SELFEATSCHECK_KEY, allSelectedFeaturesCheckBox.isSelected());
        PersistentBlackboardPlugIn.get(
                JUMPWorkbench.getInstance().getFrame().getContext()).put(
                SHOW_ANGLE_AND_LENGHT_KEY, showPanleCheckBox.isSelected());
    }

    /**
     * 
     */
    @Override
    public void init() {
        ratioSpinner.setValue(isExtendShortLineBuffer());
        unionCheckbox.setSelected(isExtendShortLineUnion());
        centerCheckBox.setSelected(isGetCentroid());
        polygonCheckBox.setSelected(isGetAsPolygon());
        allSelectedFeaturesCheckBox.setSelected(isMoreLayersSelected());
        showPanleCheckBox.setSelected(getShowAngleAndLenght());
    }

    /**
     * Devuelve el icono asociado al panel de opciones de las herramientas CAD
     * 
     * @return Icon - Icono asociado al panel
     */
    public Icon getIcon() {
        return null;
    }

    /**
     * Devuelve el nombre asociado al panel de opciones de las herramientas CAD
     * 
     * @return String - Nombre asociado al panel
     */
    @Override
    public String getName() {
        return i18n.get("org.saig.jump.widgets.config.CADToolsOptionsPanel.CAD");
    }

    /**
     * Obtiene la distancia de extender/acortar linea
     * 
     * @return
     */
    public static Double isExtendShortLineBuffer() {
        return PersistentBlackboardPlugIn.get(
                JUMPWorkbench.getInstance().getFrame().getContext()).get(
                EXTEND_LINE_BUFFER_KEY, 1000.0d);
    }

    /**
     * Obtiene el color configurado para la guia al punto medio
     * 
     * @return
     */
    public static boolean isExtendShortLineUnion() {
        return PersistentBlackboardPlugIn.get(
                JUMPWorkbench.getInstance().getFrame().getContext()).get(
                EXTEND_LINE_UNION_KEY, true);
    }

    public static boolean isGetAsPolygon() {
        return PersistentBlackboardPlugIn.get(
                JUMPWorkbench.getInstance().getFrame().getContext()).get(
                POLYGONCHECK_KEY, false);
    }

    public static boolean isGetCentroid() {
        return PersistentBlackboardPlugIn.get(
                JUMPWorkbench.getInstance().getFrame().getContext()).get(
                CENTERCHECK_KEY, false);
    }

    public static boolean isMoreLayersSelected() {
        return PersistentBlackboardPlugIn.get(
                JUMPWorkbench.getInstance().getFrame().getContext()).get(
                SELFEATSCHECK_KEY, false);
    }

    public static boolean getShowAngleAndLenght() {
        return PersistentBlackboardPlugIn.get(
                JUMPWorkbench.getInstance().getFrame().getContext()).get(
                SHOW_ANGLE_AND_LENGHT_KEY, false);
    }

}