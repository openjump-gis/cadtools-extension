package org.openjump.advancedtools.block;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.SwingConstants;

@Deprecated
public class ImageCellRenderer extends DefaultListCellRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    // Icon mappings
    private final Map<String, Icon> mapImages;

    public ImageCellRenderer(Map<String, Icon> mapImages) {
        // Make a new reference to the icon mappings
        this.mapImages = new HashMap<>(mapImages);
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected,
                cellHasFocus);
        if (value instanceof String) {
            // Look up the icon associated with the animal...
            Icon icon = mapImages.get(value.toString());
            setIcon(icon);
        }
        return this;
    }

}