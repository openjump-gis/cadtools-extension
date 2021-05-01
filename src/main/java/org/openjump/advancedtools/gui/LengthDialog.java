package org.openjump.advancedtools.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.vividsolutions.jump.I18N;

public class LengthDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;
    private double longitude;
    private final JButton acceptButton = new JButton(I18N.get("ui.OKCancelPanel.ok"));
    private final JLabel label = new JLabel(
            I18N.get("org.openjump.core.ui.plugin.edittoolbox.tab.ConstraintsOptionsPanel.Length"));
    private JButton cancelButton = new JButton(I18N.get("ui.OKCancelPanel.Cancel"));

    private final JFormattedTextField numberTextField;
    public boolean cancelado = true;

    public LengthDialog(JFrame parent, double length) {
        super(
                parent,
                I18N.get("org.openjump.core.ui.plugin.edittoolbox.tab.ConstraintsOptionsPanel.Length"),
                true);
        this.setIconImage(org.openjump.advancedtools.icon.IconLoader
                .image("cadTools.png"));
        this.longitude = length;
        JPanel p1 = new JPanel();
        this.numberTextField = getUSFormatedNumberTextField(125);
        this.numberTextField.setValue(length);
        this.numberTextField.addActionListener(this);
        p1.add(this.label);
        p1.add(this.numberTextField);
        JPanel p2 = new JPanel();
        this.acceptButton.addActionListener(this);
        this.cancelButton.addActionListener(this);
        p2.add(this.acceptButton);
        p2.add(this.cancelButton);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(p1, "Center");
        getContentPane().add(p2, "South");
        pack();
        setLocationRelativeTo(parent);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ((e.getSource().equals(this.acceptButton))
                || (e.getSource().equals(this.numberTextField))) {
            this.cancelado = false;
            this.longitude = ((Number) this.numberTextField.getValue())
                    .doubleValue();
        }
        dispose();
    }

    public void actionPerformed1(ActionEvent e) {
        if ((e.getSource().equals(this.acceptButton))
                || (e.getSource().equals(this.numberTextField))) {
            this.cancelado = true;
        }

        dispose();
    }

    public double getLength() {
        return this.longitude;
    }

    public void setLabel(String label) {
        this.label.setText(label);
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
    }

    public static JFormattedTextField getUSFormatedNumberTextField(int lenght) {
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        format.setGroupingUsed(false);
        JFormattedTextField numberTextFiel = new JFormattedTextField(format);
        numberTextFiel.setPreferredSize(new Dimension(lenght, numberTextFiel
                .getPreferredSize().height));
        return numberTextFiel;
    }
}
