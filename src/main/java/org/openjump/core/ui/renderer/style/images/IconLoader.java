package org.openjump.core.ui.renderer.style.images;

import java.awt.Image;
import javax.swing.ImageIcon;

/**
 * Gets an icon from this class' package.
 */
public class IconLoader {
    public static ImageIcon icon(String filename) {
        return new ImageIcon(IconLoader.class.getResource(filename));
    }
    
    public static Image image(String filename) {
        return IconLoader.icon(filename).getImage();
    }
}