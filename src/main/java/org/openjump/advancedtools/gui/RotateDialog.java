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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openjump.advancedtools.CadExtension;

import com.vividsolutions.jump.I18N;


/**
 * 
 * @since Kosmo 1.0
 */
public class RotateDialog extends JDialog implements ChangeListener,
        ActionListener {

    private static final long serialVersionUID = 1L;
    private static final I18N i18n = CadExtension.I18N;

    JRotationPanel rotationPanel = new JRotationPanel(0);
    JSpinner jspinner = new JSpinner();
    JButton cancelar = new JButton(
        i18n.get("org.openjump.core.ui.plugins.Dialog.Cancel"));
    JButton apply = new JButton(
        i18n.get("org.openjump.core.ui.plugins.Dialog.Apply")
                    + " ->");
    float angle = 0;
    public boolean cancelado = true;

    public RotateDialog(JFrame frame) {
        super(frame, i18n
            .get("org.openjump.core.ui.plugins.Dialog.Angle"), true);
        this.setIconImage(org.openjump.advancedtools.icon.IconLoader
                .image("cadTools.png"));

        JPanel jp = new JPanel();
        jp.add(jspinner);
        jp.add(cancelar);
        jp.add(apply);
        apply.addActionListener(this);
        cancelar.addActionListener(this);
        jspinner.setModel(new SpinnerNumberModel(0, 0, 359, 1));
        jspinner.addChangeListener(this);
        rotationPanel.addChangeListener(this);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(rotationPanel, BorderLayout.CENTER);
        getContentPane().add(jp, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(frame);

        this.setVisible(true);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource().equals(jspinner)) {
            angle = (float) (((Integer) jspinner.getValue() * 2 * Math.PI) / 360);
            rotationPanel.setAngle(angle);
        } else {
            angle = rotationPanel.getAngle();
            jspinner.setValue((int) (angle * 360 / (2 * Math.PI)));
        }
    }

    public float getAngle() {
        return angle;
    }

    public float getDegreeAngle() {
        return (int) (angle * 360 / (2 * Math.PI));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (arg0.getSource() == apply) {
            cancelado = false;
        }
        this.dispose();
    }
}
