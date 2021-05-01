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

package org.openjump.core.ui.renderer.style.type;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import org.openjump.core.ui.renderer.style.ExtendedDecorationStyle;
import org.openjump.core.ui.renderer.style.images.IconLoader;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.Viewport;



public abstract class BedStyle extends ExtendedDecorationStyle {
    private final static int DIAMETER = 3;
    private Stroke circleStroke = new BasicStroke(2);
   
    protected double finLength;
   

    private final static double SMALL_LENGTH = 5;
    private final static double MEDIUM_LENGTH = 7;
   

    private BedStyle(String name, boolean start, String iconFile,
    		   double finLength, boolean filled
    		) {
        super(name, IconLoader.icon(iconFile));
        
    }

  /*  protected void paint(Point2D terminal, Point2D next, Viewport viewport,
        Graphics2D graphics) throws NoninvertibleTransformException {*/
    	
    	protected void paint(Point2D p0, Point2D p1, Viewport viewport,
    	        Graphics2D graphics) throws NoninvertibleTransformException {
    	        if (p0.equals(p1)) {
    	            return;
    	        }
    	
    	
    	
        graphics.setColor(lineColorWithAlpha);
        graphics.setStroke(circleStroke);
        graphics.fill(toShape(p0, p1));
        graphics.draw(toShape(p0, p1));
       
    }

    private Shape toShape(Point2D p0, Point2D p1) {
    	Point2D mid = new Point2D.Float( (float) ((p0.getX() + p1.getX()) / 2),
                (float) ((p0.getY() + p1.getY()) / 2) );
    	 Point2D finTip1 = fin(mid, p0, 5, 20);
    	Ellipse2D Elly = new Ellipse2D.Double(finTip1.getX() - (DIAMETER / 2d),
    			finTip1.getY() - (DIAMETER / 2d), DIAMETER, DIAMETER);
    	
    	
        return Elly
        	/*	new Ellipse2D.Double(mid.getX() - (DIAMETER / 2d),
            mid.getY() - (DIAMETER / 2d), DIAMETER, DIAMETER)*/
            ;
    }


    
    private Point2D fin(Point2D shaftTip, Point2D shaftTail, double length,
            double angle) {
            double shaftLength = shaftTip.distance(shaftTail);
            Point2D finTail = shaftTip;
            Point2D finTip = GUIUtil.add(GUIUtil.multiply(GUIUtil.subtract(
                            shaftTail, shaftTip), length / shaftLength), finTail);
            
            //Rotazione del simbolo rispetto alla linea digitalizzata Math.PI/2 = 90°
            AffineTransform affineTransform = new AffineTransform();
           affineTransform.rotate(
        		   Math.PI/2,
        		   //(angle * Math.PI) / 90, 
        		   finTail.getX(), finTail.getY());

            return affineTransform.transform(finTip, null);
        }
    
    
    
    
    public static class Start extends BedStyle {
        public Start() {
            super( "Geo 1", 
            		true, 
            		"geo2a.png", 
            		 
                    SMALL_LENGTH,
            		true);
        }
    }

    public static class End extends BedStyle {
        public End() {
            super("Geo 1a", 
            		false, 
            		"geo2.png",
            		 MEDIUM_LENGTH,
            		true);
        }
    }
}
