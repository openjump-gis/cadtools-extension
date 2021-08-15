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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.vividsolutions.jump.I18N;
import org.openjump.advancedtools.plugins.CirclePlugIn;
import org.openjump.advancedtools.utils.WorkbenchUtils;

/**
 * 
 * @since Kosmo 1.0.0
 */
public class CircleDialog extends JDialog implements ActionListener {
    /** long serialVersionUID field */
    private static final long serialVersionUID = 1L;
    private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");
    public static ImageIcon ICON = org.openjump.advancedtools.icon.IconLoader
            .icon("cad.png");
    //JPanel p1 = new JPanel();
    //JPanel p2 = new JPanel();
    JPanel p4 = new JPanel();

    /** Plugin name */
    public final static String NAME = i18n
        .get("org.openjump.core.ui.plugins.Circle-ellipse");
    public static String radius = i18n
        .get("org.openjump.core.ui.plugins.Circle.by-radius");
    public static String diameter = i18n
        .get("org.openjump.core.ui.plugins.Circle.by-diameter");
    public static String tangent = i18n
        .get("org.openjump.core.ui.plugins.Circle.by-tangent");
    public static String threepoints = i18n
        .get("org.openjump.core.ui.plugins.Circle.3-points");

    public static String[] circletools = { radius, tangent, diameter,
            threepoints };
    public static JComboBox circleCombo = new JComboBox(circletools);

    JButton jb1 = new JButton(
        i18n.get("org.openjump.core.ui.plugins.Dialog.Accept"));
    JButton jb2 = new JButton(
        i18n.get("org.openjump.core.ui.plugins.Dialog.Cancel"));

    JRadioButton jrbindicarRaton = new JRadioButton(
        i18n.get("org.openjump.core.ui.plugins.Circle.Draw-with-the-mouse"));
    JRadioButton jrbindicarRadio = new JRadioButton(
        i18n.get("org.openjump.core.ui.plugins.Circle.Point-out-radius"));
    JRadioButton jrbindicarPosicionRadio = new JRadioButton(
        i18n.get("org.openjump.core.ui.plugins.Circle.Point-out-radius-and-position"));

    JRadioButton jrbEllipse = new JRadioButton(
        i18n.get("org.openjump.core.ui.plugins.Ellipse"));

    JPanel jpRaton = new JPanel();
    JPanel jpEllipse = new JPanel();
    JPanel jpRadio = new JPanel();
    JPanel jppRadioPosicion = new JPanel();

    public static JFormattedTextField jsAbsolutaX = WorkbenchUtils
            .getUSFormatedNumberTextField(100);
    public static JFormattedTextField jsAbsolutaY = WorkbenchUtils
            .getUSFormatedNumberTextField(100);
    public static JFormattedTextField jsRadio1 = WorkbenchUtils
            .getUSFormatedNumberTextField(100);

    public static JFormattedTextField jsRadio2 = WorkbenchUtils
            .getUSFormatedNumberTextField(100);

    JLabel jlx = new JLabel(
        i18n.get("org.openjump.core.ui.plugins.circle.X") + ":");
    JLabel jly = new JLabel(
        i18n.get("org.openjump.core.ui.plugins.circle.Y") + ":");
    JLabel jlr = new JLabel(
        i18n.get("org.openjump.core.ui.plugins.circle.Radius") + ":");

    ButtonGroup group1 = new ButtonGroup();

    public CircleDialog(JFrame parent) {
        super(parent, CirclePlugIn.NAME, true);

        p4.add(jb1);
        p4.add(jb2);

        jb1.addActionListener(this);
        jb2.addActionListener(this);

        group1.add(jrbindicarRaton);
        group1.add(jrbindicarRadio);
        group1.add(jrbindicarPosicionRadio);
        // group1.add(jrbEllipse);

        jrbindicarPosicionRadio.addActionListener(this);
        jrbindicarRadio.addActionListener(this);
        jrbindicarRaton.addActionListener(this);
        jrbEllipse.addActionListener(this);

        jpRadio.setLayout(new FlowLayout(FlowLayout.LEFT));
        jpRaton.setLayout(new FlowLayout(FlowLayout.LEFT));
        jppRadioPosicion.setLayout(new FlowLayout(FlowLayout.LEFT));
        jpEllipse.setLayout(new FlowLayout(FlowLayout.LEFT));

        jpRaton.add(jrbindicarRaton);
        jpRaton.add(circleCombo);
        jpRadio.add(jrbindicarRadio);
        jpRadio.add(jlr);
        jpRadio.add(jsRadio1);
        // jpEllipse.add(jrbEllipse);

        jppRadioPosicion.add(jrbindicarPosicionRadio);
        jppRadioPosicion.add(jlx);
        jppRadioPosicion.add(jsAbsolutaX);
        jppRadioPosicion.add(jly);
        jppRadioPosicion.add(jsAbsolutaY);
        jppRadioPosicion.add(jlr);
        jppRadioPosicion.add(jsRadio2);

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
        this.getContentPane().add(jpRadio, c);

        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 2;
        this.getContentPane().add(jppRadioPosicion, c);

        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 3;
        this.getContentPane().add(jpEllipse, c);

        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 4;
        this.getContentPane().add(p4, c);

        Dimension d = new Dimension(70, jsAbsolutaX.getPreferredSize().height);
        jsAbsolutaX.setPreferredSize(d);
        jsAbsolutaY.setPreferredSize(d);
        jsRadio1.setPreferredSize(d);
        jsRadio2.setPreferredSize(d);

        // jsAbsolutaX.setModel(new
        // SpinnerNumberModel(1.0,0.0,Double.MAX_VALUE,0.5));
        // jsAbsolutaY.setModel(new
        // SpinnerNumberModel(1.0,0.0,Double.MAX_VALUE,0.5));
        // jsRadio1.setModel(new
        // SpinnerNumberModel(1.0,0.0,Double.MAX_VALUE,0.5));
        // jsRadio2.setModel(new
        // SpinnerNumberModel(1.0,0.0,Double.MAX_VALUE,0.5));

        jrbindicarRaton.setSelected(true);

        setGuiEnable();

        KeyListener kl = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent ke) {
                if (ke.getKeyChar() == KeyEvent.VK_ENTER) {
                    performAccept();
                }
            }
        };

        jsAbsolutaX.addKeyListener(kl);
        jsAbsolutaY.addKeyListener(kl);
        jsRadio1.addKeyListener(kl);

        jsRadio2.addKeyListener(kl);

        this.setIconImage(org.openjump.advancedtools.icon.IconLoader
                .image("cadTools.png"));
        this.pack();
        this.setLocationRelativeTo(parent);
        this.setName(i18n.get("org.openjump.core.ui.plugins.Circle"));

        jsRadio1.requestFocusInWindow();
    }

    private void setGuiEnable() {
        jsAbsolutaX.setEnabled(jrbindicarPosicionRadio.isSelected());
        jsAbsolutaY.setEnabled(jrbindicarPosicionRadio.isSelected());
        jsRadio2.setEnabled(jrbindicarPosicionRadio.isSelected());
        jsRadio1.setEnabled(jrbindicarRadio.isSelected());

    }

    public boolean raton = false;
    public boolean radio = false;
    public boolean absoluto = false;
    public boolean ellipse = false;
    public double x;
    public double y;
    public double r1;
    public double r2;
    public boolean cancelado = true;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == jb1) {
            performAccept();
        } else if (e.getSource() == jb2) {
            dispose();
        } else {
            setGuiEnable();
        }

    }

    private void performAccept() {
        cancelado = false;
        if (jrbindicarPosicionRadio.isSelected()) {
            absoluto = true;
            x = ((Number) jsAbsolutaX.getValue()).doubleValue();
            y = ((Number) jsAbsolutaY.getValue()).doubleValue();
            r2 = ((Number) jsRadio2.getValue()).doubleValue();
        }

        if (jrbindicarRadio.isSelected()) {
            radio = true;
            r1 = ((Number) jsRadio1.getValue()).doubleValue();
        }

        if (jrbindicarRaton.isSelected()) {
            raton = true;
        }
        if (jrbEllipse.isSelected()) {
            ellipse = true;
        }

        dispose();
    }

}
