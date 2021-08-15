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
package org.openjump.advancedtools.tools.cogo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.vividsolutions.jump.I18N;
import org.openjump.advancedtools.tools.cogo.commands.HelpLineCommand;
import org.openjump.advancedtools.tools.cogo.commands.LineCommand;
import org.openjump.advancedtools.tools.cogo.commands.LineCommandException;
import org.openjump.advancedtools.tools.cogo.commands.LineCommandFactory;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;

/**
 * Commands panel for the tool DrawGeometryCommand
 * <p>
 * </p>
 * 
 * @author Eduardo Montero Ruiz - emontero@saig.es
 * @since Kosmo 1.0
 * @author Giuseppe Aruta
 * @since OpenJUMP 1.10
 */
public class CommandLineStringPanel extends JPanel {

    /** long serialVersionUID field */
    private static final long serialVersionUID = 1L;
    private static final I18N i18n = I18N.getInstance("org.openjump.advancedtools");

    private final DrawGeometryCommandsTool drawLineStringCommandsTool;

    // Componentes
    private JEditorPane informacionArea;
    private JScrollPane comandosScrollPane;
    private JTextField lineaComandoTextField;
    private JButton ejecutarButton;
    private String textoInformacion;
    public static JCheckBox polygonCheckBox = new JCheckBox(
        i18n.get("org.openjump.core.ui.config.CADToolsOptionsPanel.ClosedGeometryOptions.draw-as-filled-polygon"));

    public CommandLineStringPanel(
            DrawGeometryCommandsTool drawLineStringCommandsTool) {
        super(new BorderLayout());
        this.drawLineStringCommandsTool = drawLineStringCommandsTool;

        textoInformacion = "";
        initialize();
    }

    /**
     *
     */
    private void initialize() {
        JPanel lineaInferiorPanel = new JPanel(new GridBagLayout());

        this.setBorder(BorderFactory.createTitledBorder(i18n
                .get("org.openjump.core.ui.tools.DrawLineStringCommandsTool.draw-with-commands-panel")));
        ActionListener ejecucionAl = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ejecutarAction();
            }
        };

        informacionArea = new JEditorPane("text/html", textoInformacion);
        informacionArea.setEditable(false);
        comandosScrollPane = new JScrollPane(informacionArea);
        comandosScrollPane.setPreferredSize(new Dimension(300, 100));
        lineaComandoTextField = new JTextField();
        lineaComandoTextField.setToolTipText(
            i18n.get("org.openjump.core.ui.tools.DrawLineStringCommandsTool.digit-help"));
        lineaComandoTextField.setColumns(20);
        lineaComandoTextField.addActionListener(ejecucionAl);
        ejecutarButton = new JButton(
            i18n.get("org.openjump.core.ui.tools.DrawLineStringCommandsTool.execute"));
        ejecutarButton.addActionListener(ejecucionAl);

        FormUtils.addRowInGBL(lineaInferiorPanel, 1, 0, lineaComandoTextField,
                true, false, false);
        FormUtils.addRowInGBL(lineaInferiorPanel, 1, 2, ejecutarButton, false,
                true, false);
        this.add(polygonCheckBox, BorderLayout.NORTH);
        this.add(comandosScrollPane, BorderLayout.CENTER);
        this.add(lineaInferiorPanel, BorderLayout.SOUTH);

    }

    /**
     *
     */
    protected void ejecutarAction() {
        // Obtenemos el comando introducido, lo anyadimos al campo de
        // informacion
        // y limpiamos la linea de comando
        String comando = lineaComandoTextField.getText();
        lineaComandoTextField.setText("");
        addLineToCommandInfo(comando, true);
        // Parseamos el comando
        LineCommand comandoParseado = LineCommandFactory.getCommand(comando);
        if (comandoParseado == null) {
            addLineToCommandInfo(
                i18n.get("org.openjump.core.ui.tools.DrawLineStringCommandsTool.command-introduced-not-recognized"));
        } else if (comandoParseado instanceof HelpLineCommand) {
            addLineToCommandInfo(HelpLineCommand.getHelp(), true);
        } else {
            try {
                comandoParseado.execute(drawLineStringCommandsTool);
            } catch (LineCommandException e) {
                addLineToCommandInfo(i18n.get(
                    "org.openjump.core.ui.tools.DrawLineStringCommandsTool.correct-syntax-of-command-is-br-{0}",
                    comandoParseado.getSintaxis()));
            }
        }
        // devolvemos el foco a la linea de comandos
        lineaComandoTextField.requestFocusInWindow();
        // refrescamos los componentes
    }

    /**
     * Anyade una linea al area de informacion de comandos
     * 
     * @param cadena
     */
    public void addLineToCommandInfo(String cadena) {
        addLineToCommandInfo(cadena, false);
    }

    /**
     * Anyade una linea al area de informacion de comandos
     * 
     * @param comando
     * @param isComando
     */
    private void addLineToCommandInfo(String comando, boolean isComando) {
        // Si imprimimos un comando, anyadimos la marca
        if (isComando) {
            textoInformacion = textoInformacion + ">";
        }
        textoInformacion = textoInformacion + comando + "<br>";
        informacionArea.setText(textoInformacion);
    }

}
