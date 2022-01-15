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
package org.openjump.advancedtools.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import org.openjump.advancedtools.CadExtension;
import org.openjump.advancedtools.plugins.SimpleLinePlugIn;
import org.openjump.advancedtools.utils.WorkbenchUtils;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

/**
 *
 * @since Kosmo 1.0.0
 * @author Giuseppe Aruta rewrite code to adapt to OpenJUMP 1.10
 *         (http://www.openjump.org/support.html)
 * @since OpenJUMP 1.10
 */
public class SimpleLineDialog extends JDialog implements ActionListener,
        WindowListener {

    private static final long serialVersionUID = 1L;
    private static final I18N i18n = CadExtension.I18N;

    JPanel principioPanel = new JPanel();
    JPanel finalPanel = new JPanel();
    JPanel okPanel = new JPanel();
    JPanel closePanel = new JPanel();

    JButton aceptarButton = new JButton(
        i18n.get("org.openjump.core.ui.tools.DrawSimpleLine.SimpleLineDialog.Draw-line"));
    JButton cancelarButton = new JButton(
        i18n.get("org.openjump.core.ui.tools.DrawSimpleLine.SimpleLineDialog.Close"));

    JRadioButton punto1SobreElMapa = new JRadioButton(
        i18n.get("org.openjump.core.ui.tools.DrawSimpleLine.SimpleLineDialog.On-the-map"));
    JRadioButton punto1Absoluta = new JRadioButton(
        i18n.get("org.openjump.core.ui.tools.DrawSimpleLine.SimpleLineDialog.Absolute"));
    JRadioButton punto2SobreElMapa = new JRadioButton(
        i18n.get("org.openjump.core.ui.tools.DrawSimpleLine.SimpleLineDialog.On-the-map"));
    JRadioButton punto2Absoluta = new JRadioButton(
        i18n.get("org.openjump.core.ui.tools.DrawSimpleLine.SimpleLineDialog.Absolute"));
    JRadioButton punto2relativo = new JRadioButton(
        i18n.get("org.openjump.core.ui.tools.DrawSimpleLine.SimpleLineDialog.Relative"));
    JRadioButton punto2longitud = new JRadioButton(
        i18n.get(
            "org.openjump.core.ui.tools.DrawSimpleLine.SimpleLineDialog.Length-{0}-angle-with-the-mouse-{1}",
            "(", ")"));
    JRadioButton punto2longitudAngulo = new JRadioButton(
        i18n.get("org.openjump.core.ui.tools.DrawSimpleLine.SimpleLineDialog.Length-and-angle"));

    JPanel jppunto1SobreElMapa = new JPanel();
    JPanel jppunto1Absoluta = new JPanel();
    JPanel jppunto2SobreElMapa = new JPanel();
    JPanel jppunto2Absoluta = new JPanel();
    JPanel jppunto2relativo = new JPanel();
    JPanel jppunto2longitud = new JPanel();
    JPanel jppunto2longitudAngulo = new JPanel();
    JPanel jppunto2longitudAngulo2 = new JPanel();
    // JPanel comandosPanel ;

    JFormattedTextField jspunto1AbsolutaX = WorkbenchUtils
            .getUSFormatedNumberTextField(100);
    JFormattedTextField jspunto1AbsolutaY = WorkbenchUtils
            .getUSFormatedNumberTextField(100);
    JFormattedTextField jspunto2AbsolutaX = WorkbenchUtils
            .getUSFormatedNumberTextField(100);
    JFormattedTextField jspunto2AbsolutaY = WorkbenchUtils
            .getUSFormatedNumberTextField(100);
    JFormattedTextField jspunto2RelativoX = WorkbenchUtils
            .getUSFormatedNumberTextField(100);
    JFormattedTextField jspunto2RelativoY = WorkbenchUtils
            .getUSFormatedNumberTextField(100);
    JFormattedTextField jspunto2Longitud1 = WorkbenchUtils
            .getUSFormatedNumberTextField(100);
    JFormattedTextField jspunto2Longitud2 = WorkbenchUtils
            .getUSFormatedNumberTextField(100);
    JFormattedTextField jspunto2Angulo = WorkbenchUtils
            .getUSFormatedNumberTextField(100);

    JLabel jlx = new JLabel(
        i18n.get("org.openjump.core.ui.tools.DrawSimpleLine.SimpleLineDialog.X")
                    + ":");
    JLabel jly = new JLabel(
        i18n.get("org.openjump.core.ui.tools.DrawSimpleLine.SimpleLineDialog.Y")
                    + ":");
    JLabel jll = new JLabel(
        i18n.get("org.openjump.core.ui.tools.DrawSimpleLine.SimpleLineDialog.Length")
                    + ":");
    JLabel jla = new JLabel(
        i18n.get("org.openjump.core.ui.tools.DrawSimpleLine.SimpleLineDialog.Angle")
                    + ":");

    ButtonGroup group1 = new ButtonGroup();
    ButtonGroup group2 = new ButtonGroup();

    public SimpleLineDialog(SimpleLinePlugIn plugin, PlugInContext context) {
        super(
            JUMPWorkbench.getInstance().getFrame(),
            i18n.get("org.openjump.core.ui.tools.DrawSimpleLine.SimpleLineDialog.Simple-line"),
            true
        );

        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(this);
        this.setIconImage(org.openjump.advancedtools.icon.IconLoader
                .image("cadTools.png"));
        okPanel.add(aceptarButton);
        closePanel.add(cancelarButton);

        aceptarButton.addActionListener(this);
        cancelarButton.addActionListener(this);

        group1.add(punto1SobreElMapa);
        group1.add(punto1Absoluta);

        group2.add(punto2Absoluta);
        group2.add(punto2longitud);
        group2.add(punto2relativo);
        group2.add(punto2SobreElMapa);
        group2.add(punto2longitudAngulo);

        punto1Absoluta.addActionListener(this);
        punto1SobreElMapa.addActionListener(this);
        punto2Absoluta.addActionListener(this);
        punto2relativo.addActionListener(this);
        punto2longitud.addActionListener(this);
        punto2longitudAngulo.addActionListener(this);
        punto2SobreElMapa.addActionListener(this);

        jppunto1Absoluta.setLayout(new FlowLayout(FlowLayout.LEFT));
        jppunto1SobreElMapa.setLayout(new FlowLayout(FlowLayout.LEFT));
        jppunto2Absoluta.setLayout(new FlowLayout(FlowLayout.LEFT));
        jppunto2longitud.setLayout(new FlowLayout(FlowLayout.LEFT));
        jppunto2longitudAngulo.setLayout(new FlowLayout(FlowLayout.LEFT));
        jppunto2longitudAngulo2.setLayout(new FlowLayout(FlowLayout.RIGHT));
        jppunto2relativo.setLayout(new FlowLayout(FlowLayout.LEFT));
        jppunto2SobreElMapa.setLayout(new FlowLayout(FlowLayout.LEFT));

        jppunto1SobreElMapa.add(punto1SobreElMapa);

        jppunto1Absoluta.add(punto1Absoluta);
        jppunto1Absoluta.add(jlx);
        jppunto1Absoluta.add(jspunto1AbsolutaX);
        jppunto1Absoluta.add(jly);
        jppunto1Absoluta.add(jspunto1AbsolutaY);

        jppunto2SobreElMapa.add(punto2SobreElMapa);

        jppunto2Absoluta.add(punto2Absoluta);
        jppunto2Absoluta.add(jlx);
        jppunto2Absoluta.add(jspunto2AbsolutaX);
        jppunto2Absoluta.add(jly);
        jppunto2Absoluta.add(jspunto2AbsolutaY);

        jppunto2relativo.add(punto2relativo);
        jppunto2relativo.add(jlx);
        jppunto2relativo.add(jspunto2RelativoX);
        jppunto2relativo.add(jly);
        jppunto2relativo.add(jspunto2RelativoY);

        jppunto2longitud.add(punto2longitud);
        jppunto2longitud.add(jll);
        jppunto2longitud.add(jspunto2Longitud1);

        jppunto2longitudAngulo.add(punto2longitudAngulo);
        jppunto2longitudAngulo.add(jll);
        jppunto2longitudAngulo.add(jspunto2Longitud2);
        jppunto2longitudAngulo2.add(jla);
        jppunto2longitudAngulo2.add(jspunto2Angulo);

        TitledBorder title1;
        title1 = BorderFactory
            .createTitledBorder(i18n.get(
                "org.openjump.core.ui.tools.DrawSimpleLine.SimpleLineDialog.Start"));
        TitledBorder title2;
        title2 = BorderFactory
            .createTitledBorder(i18n.get(
                "org.openjump.core.ui.tools.DrawSimpleLine.SimpleLineDialog.End"));
        principioPanel.setBorder(title1);
        finalPanel.setBorder(title2);
        principioPanel.setLayout(new GridBagLayout());
        finalPanel.setLayout(new GridBagLayout());

        // Abyadimos los componentes sobre los paneles
        FormUtils.addRowInGBL(principioPanel, 0, 0, jppunto1SobreElMapa, true,
                false);
        FormUtils.addRowInGBL(principioPanel, 1, 0, jppunto1Absoluta, true,
                false);
        FormUtils.addRowInGBL(finalPanel, 0, 0, jppunto2SobreElMapa, true,
                false);
        FormUtils.addRowInGBL(finalPanel, 1, 0, jppunto2Absoluta, true, false);
        FormUtils.addRowInGBL(finalPanel, 2, 0, jppunto2relativo, true, false);
        FormUtils.addRowInGBL(finalPanel, 3, 0, jppunto2longitud, true, false);
        FormUtils.addRowInGBL(finalPanel, 4, 0, jppunto2longitudAngulo, true,
                false);
        FormUtils.addRowInGBL(finalPanel, 5, 0, jppunto2longitudAngulo2, true,
                false);

        this.getContentPane().setLayout(new GridBagLayout());
        JPanel northPanel = new JPanel(new GridBagLayout());
        JPanel southPanel = new JPanel(new GridBagLayout());

        FormUtils.addRowInGBL(northPanel, 0, 0, principioPanel, true, false);
        FormUtils.addRowInGBL(northPanel, 1, 0, finalPanel, true, false);
        FormUtils.addRowInGBL(northPanel, 2, 0, okPanel, true, false);
        FormUtils.addRowInGBL(southPanel, 1, 0, closePanel, true, false);

        Dimension d = new Dimension(70,
                jspunto1AbsolutaX.getPreferredSize().height);
        jspunto1AbsolutaX.setPreferredSize(d);
        jspunto1AbsolutaY.setPreferredSize(d);
        jspunto2AbsolutaX.setPreferredSize(d);
        jspunto2AbsolutaY.setPreferredSize(d);
        jspunto2RelativoX.setPreferredSize(d);
        jspunto2RelativoY.setPreferredSize(d);
        jspunto2Longitud1.setPreferredSize(d);
        jspunto2Longitud2.setPreferredSize(d);
        jspunto2Angulo.setPreferredSize(d);

        // jspunto1AbsolutaX.setModel(new
        // SpinnerNumberModel(1.0,0.0,Double.MAX_VALUE,0.5));
        // jspunto1AbsolutaY.setModel(new
        // SpinnerNumberModel(1.0,0.0,Double.MAX_VALUE,0.5));
        // jspunto2AbsolutaX.setModel(new
        // SpinnerNumberModel(1.0,0.0,Double.MAX_VALUE,0.5));
        // jspunto2AbsolutaY.setModel(new
        // SpinnerNumberModel(1.0,0.0,Double.MAX_VALUE,0.5));
        // jspunto2Longitud1.setModel(new
        // SpinnerNumberModel(1.0,0.0,Double.MAX_VALUE,0.5));
        // jspunto2Longitud2.setModel(new
        // SpinnerNumberModel(1.0,0.0,Double.MAX_VALUE,0.5));
        // jspunto2RelativoX.setModel(new
        // SpinnerNumberModel(0.0,-Double.MAX_VALUE,Double.MAX_VALUE,0.5));
        // jspunto2RelativoY.setModel(new
        // SpinnerNumberModel(0.0,-Double.MAX_VALUE,Double.MAX_VALUE,0.5));
        // jspunto2Angulo.setModel(new SpinnerNumberModel(0.0,0.0,359.5,0.5));
        //
        punto1SobreElMapa.setSelected(true);
        punto2SobreElMapa.setSelected(true);

        // Componentes de linea de comandos
        // comandosPanel = new CommandLinePanel(this, plugin, context);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(northPanel, BorderLayout.NORTH);
        // contentPane.add(comandosPanel, BorderLayout.CENTER);
        contentPane.add(southPanel, BorderLayout.SOUTH);
        // FormUtils.addRowInGBL((JComponent)this.getContentPane(), 2, 0,
        // comandosPanel, true, true, false);
        this.setContentPane(contentPane);

        setGuiEnable();
        this.pack();
        this.setLocationRelativeTo(JUMPWorkbench.getInstance().getFrame());
    }

    private void setGuiEnable() {
        jspunto1AbsolutaX.setEnabled(punto1Absoluta.isSelected());
        jspunto1AbsolutaY.setEnabled(punto1Absoluta.isSelected());
        jspunto2AbsolutaX.setEnabled(punto2Absoluta.isSelected());
        jspunto2AbsolutaY.setEnabled(punto2Absoluta.isSelected());
        jspunto2RelativoX.setEnabled(punto2relativo.isSelected());
        jspunto2RelativoY.setEnabled(punto2relativo.isSelected());
        jspunto2Longitud1.setEnabled(punto2longitud.isSelected());
        jspunto2Longitud2.setEnabled(punto2longitudAngulo.isSelected());
        jspunto2Angulo.setEnabled(punto2longitudAngulo.isSelected());
    }

    private boolean primerPuntoCapturado;
    private boolean segundoPuntoCapturado;
    public double x1;
    public double x2;
    public double y1;
    public double y2;
    public double longitud = -1;
    public boolean relativo = false;
    public boolean cancelado = false;

    public void setFirstPointAbsoluta(double x, double y) {
        punto1Absoluta.doClick();
        jspunto1AbsolutaX.setValue(x);
        jspunto1AbsolutaY.setValue(y);
        aceptarAction();
    }

    /**
     *
     * @param x x value of the point
     * @param y y value of the point
     */
    public void setSecondPointAbsoluta(double x, double y) {
        punto2Absoluta.doClick();
        jspunto2AbsolutaX.setValue(x);
        jspunto2AbsolutaY.setValue(y);
        aceptarAction();
    }

    /**
     *
     * @param x x value of the point
     * @param y y value of the point
     */
    public void setSecondPointRelativa(double x, double y) {
        punto2relativo.doClick();
        jspunto2RelativoX.setValue(x);
        jspunto2RelativoY.setValue(y);
        aceptarAction();
    }

    /**
     *
     * @param longitude longitude
     * @param angle angle
     */
    public void setSecondPointLongAngulo(double longitude, double angle) {
        punto2longitudAngulo.doClick();
        jspunto2Longitud2.setValue(longitude);
        jspunto2Angulo.setValue(angle);
        aceptarAction();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == aceptarButton) {
            aceptarAction();
            dispose();
        } else if (e.getSource() == cancelarButton) {
            cancelado = true;
            dispose();
        } else {
            setGuiEnable();
        }

    }

    /**
     * Metodo de la accion de aceptar
     */
    private void aceptarAction() {
        if (punto1Absoluta.isSelected()) {
            primerPuntoCapturado = false;
            x1 = ((Number) jspunto1AbsolutaX.getValue()).doubleValue();
            y1 = ((Number) jspunto1AbsolutaY.getValue()).doubleValue();
        } else if (punto1SobreElMapa.isSelected()) {
            primerPuntoCapturado = true;
        }

        if (punto2SobreElMapa.isSelected()) {
            segundoPuntoCapturado = true;
        } else if (punto2Absoluta.isSelected()) {
            segundoPuntoCapturado = false;
            x2 = ((Number) jspunto2AbsolutaX.getValue()).doubleValue();
            y2 = ((Number) jspunto2AbsolutaY.getValue()).doubleValue();
        } else if (punto2relativo.isSelected()) {
            segundoPuntoCapturado = false;
            relativo = true;
            x2 = ((Number) jspunto2RelativoX.getValue()).doubleValue();
            y2 = ((Number) jspunto2RelativoY.getValue()).doubleValue();
        } else if (punto2longitudAngulo.isSelected()) {
            segundoPuntoCapturado = false;
            relativo = true;
            double l = ((Number) jspunto2Longitud2.getValue()).doubleValue();
            double a = (((Number) jspunto2Angulo.getValue()).doubleValue() * 2 * Math.PI) / 360.0;
            x2 = l * Math.cos(a);
            y2 = l * Math.sin(a);
        } else if (punto2longitud.isSelected()) {
            segundoPuntoCapturado = true;
            relativo = true;
            longitud = ((Number) jspunto2Longitud1.getValue()).doubleValue();
        }
    }

    /**
     * @return the primerPuntoCapturado
     */
    public boolean isPrimerPuntoCapturado() {
        return primerPuntoCapturado;
    }

    /**
     * @return the segundoPuntoCapturado
     */
    public boolean isSegundoPuntoCapturado() {
        return segundoPuntoCapturado;
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        cancelado = true;
        dispose();
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

}
