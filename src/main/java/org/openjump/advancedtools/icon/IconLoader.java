/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI 
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package org.openjump.advancedtools.icon;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.ImageIcon;

import org.openjump.advancedtools.CadExtension;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.Logger;

/**
 * Gets an icon from this class' package.
 */
public class IconLoader {

    private static final I18N i18n = CadExtension.I18N;

    /** Icono por defecto, si no encuentra el indicado */
    public final static ImageIcon DEFAULT_UNKNOW_ICON = new ImageIcon(
            IconLoader.class.getResource("default_icon.png"));

    /** Logger */
    //public final static Logger LOGGER = Logger.getLogger(IconLoader.class);

    /**
     * Genera un icono a partir del nombre del fichero que se le pasa
     * 
     * @param filename
     *            Nombre del fichero de imagen (debe estar en el mismo
     *            directorio que esta clase)
     * @return ImageIcon - Icono
     */
    public static ImageIcon icon(String filename) {
        return icon(filename, true);
    }

    /**
     * Genera un icono a partir del nombre del fichero que se le pasa
     * 
     * @param filename
     *            Nombre del fichero de imagen (debe estar en el mismo
     *            directorio que esta clase)
     * @return ImageIcon - Icono
     */
    public static ImageIcon icon(String filename, boolean useDefaultForNull) {
        URL urlIcon = IconLoader.class.getResource(filename);
        if (urlIcon == null) {
            if (useDefaultForNull) {
                Logger.warn(i18n.get(
                        "com.vividsolutions.jump.workbench.ui.images.IconLoader.The-icon-{0}-has-not-been-found-default-icon-will-be-used",
                    filename));
                return DEFAULT_UNKNOW_ICON;
            } else {
                return null;
            }
        }
        return new ImageIcon(urlIcon);
    }

    /**
     * Genera un icono a partir de la URL del fichero de imagen que se le pasa
     * 
     * @param url
     *            URL del fichero de imagen
     * @return ImageIcon - Icono
     */
    public static ImageIcon icon(URL url) {
        if (url == null) {

            Logger.warn(i18n.get(
                "com.vividsolutions.jump.workbench.ui.images.IconLoader.The-icon-{0}-has-not-been-found-default-icon-will-be-used",
                url));
            return DEFAULT_UNKNOW_ICON;
        }
        return new ImageIcon(url);
    }

    public static BufferedImage image(String filename) {
        ImageIcon icon = icon(IconLoader.class
                .getResource(resolveFile(filename)));
        Image image = icon.getImage();

        // create a buffered image with transparency
        BufferedImage bufImg = new BufferedImage(image.getWidth(null),
                image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // draw the image on to the buffered image
        Graphics2D bGr = bufImg.createGraphics();
        bGr.drawImage(image, 0, 0, null);
        bGr.dispose();

        return bufImg;
    }

    protected static String resolveFile(String filename) {
        // iterate over each location, return on first hit
        for (String path : new String[] { "", "famfam/", "fugue/" }) {
            if (IconLoader.class.getResource(path + filename) != null)
                return path + filename;
        }

        // if push comes to shove, we let the calling method deal w/ the
        // consequences, exactly as it was before
        return filename;
    }
}
