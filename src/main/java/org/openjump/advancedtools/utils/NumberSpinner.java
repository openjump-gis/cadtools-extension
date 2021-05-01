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
package org.openjump.advancedtools.utils;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 * Spinner numerico
 * <p>
 * </p>
 * 
 * @author Sergio Ba�os Calvo
 * @since Kosmo 1.0.0
 */
public class NumberSpinner extends JSpinner {

    /** long serialVersionUID field */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor para un spinner con valores double
     * 
     * @param defaultValue Valor por defecto
     * @param minValue Valor minimo
     * @param maxValue Valor maximo
     * @param step Incremento
     */
    public NumberSpinner( double defaultValue, double minValue, double maxValue, double step ) {
        SpinnerModel model = new SpinnerNumberModel(defaultValue, minValue, maxValue, step);
        setFont(new JLabel().getFont());
        setModel(model);
    }

    /**
     * Constructor para un spinner con valores enteros
     * 
     * @param defaultValue Valor por defecto
     * @param minValue Valor minimo
     * @param maxValue Valor maximo
     * @param step Incremento
     */
    public NumberSpinner( int defaultValue, int minValue, int maxValue, int step ) {
        SpinnerModel model = new SpinnerNumberModel(defaultValue, minValue, maxValue, step);
        setFont(new JLabel().getFont());
        setModel(model);
    }

    /**
     * Establece el valor por defecto del spinner
     * 
     * @param defaultValue
     */
    public void setDefaultValue( double defaultValue ) {
        this.getModel().setValue(defaultValue);
    }

    /**
     * Establece el valor por defecto del spinner
     * 
     * @param defaultValue
     */
    public void setDefaultValue( int defaultValue ) {
        this.getModel().setValue(Integer.valueOf(defaultValue));
    }

    /**
     * Establece el valor minimo del spinner
     * 
     * @param minValue
     */
    public void setMinValue( double minValue ) {
        ((SpinnerNumberModel) this.getModel()).setMinimum(minValue);
    }

    /**
     * Establece el valor maximo del spinner
     * 
     * @param maxValue
     */
    public void setMaxValue( double maxValue ) {
        ((SpinnerNumberModel) this.getModel()).setMaximum(maxValue);
    }

    /**
     * @param step
     */
    public void setStep( double step ) {
        ((SpinnerNumberModel) this.getModel()).setStepSize(step);
    }

    /**
     * Actualiza el modelo completo del spinner
     * 
     * @param defaultValue
     * @param minValue
     * @param maxValue
     * @param step
     */
    public void updateModel( double defaultValue, double minValue, double maxValue, double step ) {
        SpinnerModel model = new SpinnerNumberModel(defaultValue, minValue, maxValue, step);
        setModel(model);
    }

    /**
     * Recupera el valor del spinner como int
     * 
     * @return int - Valor del spinner
     */
    public int getIntValue() {
        return ((Number) getValue()).intValue();
    }

    /**
     * Recupera el valor del spinner como double
     * 
     * @return double - Valor del spinner como double
     */
    public double getDoubleValue() {
        return ((Number) getValue()).doubleValue();
    }
}
