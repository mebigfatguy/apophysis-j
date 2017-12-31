/*

 Apophysis-j Copyright (C) 2008 Jean-Francois Bouzereau

 based on Apophysis ( http://www.apophysis.org )
 Apophysis Copyright (C) 2001-2004 Mark Townsend
 Apophysis Copyright (C) 2005-2006 Ronald Hordijk, Piotr Borys, Peter Sdobnov
 Apophysis Copyright (C) 2007 Piotr Borys, Peter Sdobnov

 based on Flam3 ( http://www.flam3.com )
 Copyright (C) 1992-2006  Scott Draves <source@flam3.com>

 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

 */

package org.apophysis;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

public class Preview extends MyThinlet implements Constants, ThreadTarget {

    /*****************************************************************************/
    // CONSTANTS

    /*****************************************************************************/
    // FIELDS

    ControlPoint cp = new ControlPoint();
    Renderer renderer = new Renderer(this);

    int imagewidth;
    int imageheight;

    Image image = null;

    boolean mustdraw = false;

    Object lock = new Object();

    /*****************************************************************************/

    Preview(String title, String xmlfile, int width, int height) throws Exception {
        super(title, xmlfile, width, height);

    }

    /*****************************************************************************/

    @Override
    public boolean destroy() {
        super.setVisible(false);

        Global.script.btnStopClick();

        return false;
    }

    /*****************************************************************************/

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

    } // End of method show

    /*****************************************************************************/

    public void drawFlame() {
        if (imagewidth == 0) {
            return;
        }

        renderAndDrawFlame();

    } // End of method drawFlame

    /*****************************************************************************/

    void renderAndDrawFlame() {
        renderer.stop();

        cp.adjustScale(imagewidth, imageheight);
        cp.width = imagewidth;
        cp.height = imageheight;
        renderer.setCP(cp);
        renderer.render();

        synchronized (lock) {
            image = renderer.getImage();
        }

        repaint();

    } // End of method drawFlame

    /*****************************************************************************/

    public void drawBackPanel(Graphics g, Rectangle bounds) {
        imagewidth = bounds.width;
        imageheight = bounds.height;

        g.setColor(new Color(cp.background[0], cp.background[1], cp.background[2]));
        g.fillRect(0, 0, bounds.width, bounds.height);

        synchronized (lock) {
            if (image != null) {
                g.drawImage(image, 0, 0, null);
            }
        }

    } // End of method drawBackPanel

    /*****************************************************************************/

    public void pressBackPanel(MouseEvent e) {
        Global.script.btnStopClick();
    } // End of method pressBackPanel

    /*****************************************************************************/
    // ThreadTarget implementation

    @Override
    public void message(int msg) {
    }

    @Override
    public void progress(double value) {
    }

    @Override
    public void output(String msg) {
    }

    /*****************************************************************************/

} // End of class Preview
