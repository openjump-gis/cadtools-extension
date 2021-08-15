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

public class VerHorLengthDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = 1L;
    private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");

    private double longitude;
    private JButton acceptButton = new JButton(
        i18n.get("org.openjump.core.ui.plugins.Dialog.Accept"));

    private JLabel label_1 = new JLabel("Horizontal Length");
    private JLabel label_2 = new JLabel("Vertical Length");
    private JButton cancelButton = new JButton("Cancel");
    private JFormattedTextField numberTextFiel_1;
    private JFormattedTextField numberTextFiel_2;
    private JFormattedTextField numberTextFiel;
    private JLabel label = new JLabel("Length");

    public boolean cancelado = true;

    public VerHorLengthDialog(JFrame parent, double length) {
        super(parent, "Length", true);
        this.longitude = length;

        JPanel p1 = new JPanel();
        this.numberTextFiel_1 = getUSFormatedNumberTextField(125);
        this.numberTextFiel_1.setValue(length);
        this.numberTextFiel_1.addActionListener(this);
        p1.add(this.label_1);
        p1.add(this.numberTextFiel_1);

        JPanel p2 = new JPanel();
        this.numberTextFiel_2 = getUSFormatedNumberTextField(125);
        this.numberTextFiel_2.setValue(length);
        this.numberTextFiel_2.addActionListener(this);
        p1.add(this.label_2);
        p1.add(this.numberTextFiel_2);

        JPanel p3 = new JPanel();
        this.acceptButton.addActionListener(this);
        this.cancelButton.addActionListener(this);
        p3.add(this.acceptButton);
        p3.add(this.cancelButton);
        getContentPane().setLayout(new BorderLayout());

        getContentPane().add(p1, "Center");
        getContentPane().add(p2, "Center");
        getContentPane().add(p3, "South");
        this.setIconImage(org.openjump.advancedtools.icon.IconLoader
                .image("cadTools.png"));
        pack();
        setLocationRelativeTo(parent);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ((e.getSource().equals(this.acceptButton))
                || (e.getSource().equals(this.numberTextFiel))) {
            this.cancelado = false;
            this.longitude = ((Number) this.numberTextFiel.getValue())
                    .doubleValue();
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
