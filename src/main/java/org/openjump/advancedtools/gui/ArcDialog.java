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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.vividsolutions.jump.I18N;

import org.openjump.advancedtools.CadExtension;
import org.openjump.advancedtools.plugins.ArcPlugIn;
import org.openjump.advancedtools.utils.WorkbenchUtils;

/**
 * 
 * @since Kosmo 1.0.0
 */
public class ArcDialog extends JDialog implements ActionListener {
    /** long serialVersionUID field */
    private static final long serialVersionUID = 1L;

    public static ImageIcon ICON = org.openjump.advancedtools.icon.IconLoader
            .icon("cad.png");
    //JPanel p1 = new JPanel();
    //JPanel p2 = new JPanel();
    JPanel p3 = new JPanel();

    JButton jb1 = new JButton(
        CadExtension.I18N.get("org.openjump.core.ui.plugins.Dialog.Accept"));
    JButton jb2 = new JButton(
        CadExtension.I18N.get("org.openjump.core.ui.plugins.Dialog.Cancel"));

    JRadioButton jrbindicarRaton = new JRadioButton(
        CadExtension.I18N.get("org.openjump.core.ui.plugins.Circle.Draw-with-the-mouse"));
    JRadioButton jrbindicarPosicionRadio = new JRadioButton(
        CadExtension.I18N.get("org.openjump.core.ui.plugins.Circle.Point-out-radius-and-position"));

    JLabel jlx = new JLabel(
        CadExtension.I18N.get("org.openjump.core.ui.plugins.Circle.X") + ":");
    JLabel jly = new JLabel(
        CadExtension.I18N.get("org.openjump.core.ui.plugins.Circle.Y") + ":");
    JLabel jlr = new JLabel(
        CadExtension.I18N.get("org.openjump.core.ui.plugins.Circle.Radius") + ":");
    JLabel jla1 = new JLabel(
        CadExtension.I18N.get("org.openjump.core.ui.plugins.Circle.Start-angle")
                    + ":");
    JLabel jla2 = new JLabel(
        CadExtension.I18N.get("org.openjump.core.ui.plugins.Circle.End-angle")
                    + ":");

    JPanel jpRaton = new JPanel();
    JPanel jppRadioPosicion = new JPanel();

    JFormattedTextField jsAbsolutaX = WorkbenchUtils
            .getUSFormatedNumberTextField(100);
    JFormattedTextField jsAbsolutaY = WorkbenchUtils
            .getUSFormatedNumberTextField(100);
    JFormattedTextField jsRadio = WorkbenchUtils
            .getUSFormatedNumberTextField(100);
    JFormattedTextField jsAngle1 = WorkbenchUtils
            .getUSFormatedNumberTextField(100);
    JFormattedTextField jsAngle2 = WorkbenchUtils
            .getUSFormatedNumberTextField(100);

    ButtonGroup group1 = new ButtonGroup();

    public ArcDialog(JFrame parent) {
        super(parent, ArcPlugIn.NAME, true);

        p3.add(jb1);
        p3.add(jb2);

        jb1.addActionListener(this);
        jb2.addActionListener(this);

        group1.add(jrbindicarRaton);
        group1.add(jrbindicarPosicionRadio);

        jrbindicarPosicionRadio.addActionListener(this);
        jrbindicarRaton.addActionListener(this);

        jpRaton.setLayout(new FlowLayout(FlowLayout.LEFT));
        jppRadioPosicion.setLayout(new FlowLayout(FlowLayout.LEFT));

        jpRaton.add(jrbindicarRaton);

        jppRadioPosicion.add(jrbindicarPosicionRadio);
        jppRadioPosicion.add(jlx);
        jppRadioPosicion.add(jsAbsolutaX);
        jppRadioPosicion.add(jly);
        jppRadioPosicion.add(jsAbsolutaY);
        jppRadioPosicion.add(jlr);
        jppRadioPosicion.add(jsRadio);
        jppRadioPosicion.add(jla1);
        jppRadioPosicion.add(jsAngle1);
        jppRadioPosicion.add(jla2);
        jppRadioPosicion.add(jsAngle2);

        this.getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        this.getContentPane().add(jpRaton, c);

        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 1;
        this.getContentPane().add(jppRadioPosicion, c);

        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 3;
        this.getContentPane().add(p3, c);

        Dimension d = new Dimension(70, jsAbsolutaX.getPreferredSize().height);
        jsAbsolutaX.setPreferredSize(d);
        jsAbsolutaY.setPreferredSize(d);
        jsAngle1.setPreferredSize(d);
        jsAngle2.setPreferredSize(d);
        jsRadio.setPreferredSize(d);

        // jsAbsolutaX.setModel(new
        // SpinnerNumberModel(1.0,0.0,Double.MAX_VALUE,0.5));
        // jsAbsolutaY.setModel(new
        // SpinnerNumberModel(1.0,0.0,Double.MAX_VALUE,0.5));
        // jsRadio.setModel(new
        // SpinnerNumberModel(1.0,0.0,Double.MAX_VALUE,0.5));
        // jsAngle1.setModel(new SpinnerNumberModel(1.0,0.0,365.5,0.5));
        // jsAngle2.setModel(new SpinnerNumberModel(1.0,0.0,365.5,0.5));

        jrbindicarRaton.setSelected(true);

        setGuiEnable();

        this.setIconImage(org.openjump.advancedtools.icon.IconLoader
                .image("cadTools.png"));
        this.pack();
        this.setLocationRelativeTo(parent);
    }

    private void setGuiEnable() {
        jsAbsolutaX.setEnabled(jrbindicarPosicionRadio.isSelected());
        jsAbsolutaY.setEnabled(jrbindicarPosicionRadio.isSelected());
        jsRadio.setEnabled(jrbindicarPosicionRadio.isSelected());
        jsAngle1.setEnabled(jrbindicarPosicionRadio.isSelected());
        jsAngle2.setEnabled(jrbindicarPosicionRadio.isSelected());
    }

    public boolean raton = false;
    public boolean absoluto = false;
    public double x;
    public double y;
    public double r;
    public double a1;
    public double a2;
    public boolean cancelado = true;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == jb1) {
            cancelado = false;
            if (jrbindicarPosicionRadio.isSelected()) {
                absoluto = true;
                x = ((Number) jsAbsolutaX.getValue()).doubleValue();
                y = ((Number) jsAbsolutaY.getValue()).doubleValue();
                r = ((Number) jsRadio.getValue()).doubleValue();
                a1 = ((Number) jsAngle1.getValue()).doubleValue();
                a2 = ((Number) jsAngle2.getValue()).doubleValue();
            }

            if (jrbindicarRaton.isSelected()) {
                raton = true;
            }

            dispose();
        } else if (e.getSource() == jb2) {
            dispose();
        } else {
            setGuiEnable();
        }

    }

    public JRadioButton getJrbindicarRaton() {
        return jrbindicarRaton;
    }

    public JRadioButton getJrbindicarPosicionRadio() {
        return jrbindicarPosicionRadio;
    }
}
