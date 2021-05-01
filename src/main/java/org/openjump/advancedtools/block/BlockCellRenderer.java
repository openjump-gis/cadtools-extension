package org.openjump.advancedtools.block;

import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * @deprecated now included and inlined in {@link BlockPanel}.
 */
@Deprecated
public class BlockCellRenderer extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;
    private List<String> tooltips;
    private Map<Object, Icon> icons = new HashMap<>();

    @Override
    public Component getListCellRendererComponent(
            @SuppressWarnings("rawtypes") JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {

        JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
                index, isSelected, cellHasFocus);
        if (-1 < index && null != value && null != icons) {

            label.setIcon(icons.get(index));
        }
        if (-1 < index && null != value && null != tooltips) {
            list.setToolTipText(tooltips.get(index));

        }
        return label;
    }

    public void setTooltips(List<String> tooltips) {
        this.tooltips = tooltips;
    }

    public void setIcons(HashMap<Object, Icon> icons) {
        this.icons = icons;
    }
}