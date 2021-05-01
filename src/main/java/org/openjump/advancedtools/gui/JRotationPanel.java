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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Panel to select an angle (in deegrees)
 * 
 * @author Gabriel Bellido P&eacute;rez - gbp@saig.es
 * @since Kosmo 1.0
 */
public class JRotationPanel extends JPanel implements MouseListener,
        MouseMotionListener {

    /** long serialVersionUID field */
    private static final long serialVersionUID = 1L;

    /** Selected angle */
    protected float angle = 0.0f;

    /** */
    protected int x, y;

    /** Change listener */
    protected ChangeListener cl = null;

    /**
     * 
     */
    public JRotationPanel() {
        this(0);
    }

    /**
     * @param angle
     */
    public JRotationPanel(float angle) {
        this.angle = angle;

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(170, 170);
    }

    /**
     * Get the selected angle
     * 
     * @return
     */
    public float getAngle() {
        return angle;
    }

    /**
     * 
     */
    public void update() {
        if (cl != null) {
            ChangeEvent ev = new ChangeEvent(this);
            cl.stateChanged(ev);
            this.repaint();
        }
    }

    /**
     * @param cl
     */
    public void addChangeListener(ChangeListener cl) {
        this.cl = cl;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        int ratio = Math.min(this.getWidth(), this.getHeight());
        ratio *= 0.8;
        x = (this.getWidth() / 2);
        y = (this.getHeight() / 2);
        g2.setColor(Color.BLACK);
        Ellipse2D circle = new Ellipse2D.Float(
                (int) ((this.getWidth() - ratio) / 2),
                (int) ((this.getHeight() - ratio) / 2), ratio, ratio);
        g2.fill(circle);
        g2.setColor(Color.WHITE);
        for (int i = 0; i < 16; i++) {
            g2.drawLine(x, y - (ratio / 2), x, y - (int) (ratio * 0.8 / 2));
            g2.rotate((Math.PI * 2) / 16.0, x, y);
        }
        Polygon p = new Polygon();
        p.addPoint(x, y - (int) (ratio * 0.8 / 2));
        p.addPoint(x + 5, y + (int) (ratio * 0.1));
        p.addPoint(x - 5, y + (int) (ratio * 0.1));
        g2.rotate(angle, x, y);
        g2.setColor(Color.WHITE);
        g2.fill(p);

        Ellipse2D circle2 = new Ellipse2D.Float(x - 2, y - 2, 4, 4);
        g2.setColor(Color.BLACK);
        g2.fill(circle2);
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
        // Nothing to do
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
        // Nothing to do
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
        // Nothing to do
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        angle = getAngle(x, y);
        repaint();
        ChangeEvent event = new ChangeEvent(this);
        cl.stateChanged(event);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Nothing to do
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        angle = getAngle(x, y);
        repaint();
        ChangeEvent event = new ChangeEvent(this);
        cl.stateChanged(event);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Nothing to do
    }

    /**
     * @param x
     * @param y
     * @return
     */
    private float getAngle(int x, int y) {
        float angle;
        float d = (float) Math.sqrt((x - this.x) * (x - this.x) + (y - this.y)
                * (y - this.y));
        float sina = (x - this.x) / d;
        float cosa = (this.y - y) / d;
        if (cosa > 0)
            angle = (float) Math.asin(sina);
        else
            angle = (float) (-Math.asin(sina) + Math.PI);

        if (angle < 0)
            angle += Math.PI * 2;
        return angle;
    }

    /**
     * @param angle
     */
    public void setAngle(float angle) {
        this.angle = angle;
        repaint();
    }
}