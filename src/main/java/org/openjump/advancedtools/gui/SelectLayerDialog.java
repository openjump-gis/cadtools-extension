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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.model.Layer;

/**
 * 
 * @since Kosmo 1.0
 */
public class SelectLayerDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = 1L;
    private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");

    private final JComboBox<Layer> jcb = new JComboBox<>();
    private Layer selected = null;
    private final JButton ok = new JButton(
            i18n.get("org.openjump.core.ui.tools.SelectLayerDialog.Accept"));
    private final JButton cancel = new JButton(
            i18n.get("org.openjump.core.ui.tools.SelectLayerDialog.Cancel"));

    /**
     * @param parent parent Frame
     * @param layers layers to be selected
     */
    public SelectLayerDialog(JFrame parent, Layer[] layers) {
        super(
                parent,
                i18n.get("org.openjump.core.ui.tools.SelectLayerDialog.Choose-a-layer"),
                true);
        for (Layer layer : layers) {
            jcb.addItem(layer);
        }
        this.getContentPane().setLayout(new BorderLayout());
        JPanel p1 = new JPanel();
        p1.add(new JLabel(i18n.get(
            "org.openjump.core.ui.tools.SelectLayerDialog.Choose-a-layer") + ":"));
        p1.add(jcb);
        JPanel p2 = new JPanel();
        p2.add(ok);
        p2.add(cancel);
        this.getContentPane().add(p1, BorderLayout.CENTER);
        this.getContentPane().add(p2, BorderLayout.SOUTH);
        this.setIconImage(org.openjump.advancedtools.icon.IconLoader
                .image("cadTools.png"));
        pack();
        setLocationRelativeTo(parent);
        ok.addActionListener(this);
        cancel.addActionListener(this);
    }

    /**
	   *
	   */
    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (arg0.getSource() == ok) {
            selected = (Layer) jcb.getSelectedItem();
        }
        dispose();

    }

    /**
     * @return
     */
    public Layer getLayer() {
        return selected;
    }
}
