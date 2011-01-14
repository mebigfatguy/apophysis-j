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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Fullscreen extends MyThinlet implements Constants {

	/*****************************************************************************/
	// CONSTANTS

	/*****************************************************************************/
	// FIELDS

	long remainder, starttime, t;
	int imgleft, imgtop, imgwidth, imgheight;
	boolean closing;

	Renderer renderer;

	Image image = null;

	boolean calculate;
	ControlPoint cp = new ControlPoint();
	double zoom;
	double center[] = new double[2];

	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	GraphicsDevice gd = ge.getDefaultScreenDevice();

	Window wscreen = null;

	/*****************************************************************************/

	Fullscreen(String title, String xmlfile, int width, int height)
			throws Exception {
		super(title, xmlfile, width, height);

		launcher.setResizable(false);

	}

	/*****************************************************************************/

	@Override
	public boolean destroy() {
		hide();
		return false;
	}

	/*****************************************************************************/

	@Override
	public void show() {

		wscreen = new ScreenWindow(launcher);

		boolean ok = gd.isFullScreenSupported();
		if (ok) {
			gd.setFullScreenWindow(wscreen);
		} else {
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			wscreen.setBounds(0, 0, d.width, d.height);
			wscreen.setVisible(true);
		}
	}

	/*****************************************************************************/

	void terminate() {
		try {
			gd.setFullScreenWindow(null);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		wscreen.setVisible(false);
		wscreen.dispose();
		wscreen = null;
		image = null;

		launcher.setLocation(-200, -200);

		// hide();

		System.out.println("full screen terminated!");

	}

	/*****************************************************************************/
	/*****************************************************************************/

	class ScreenWindow extends Window implements MouseListener {

		/*****************************************************************************/

		ScreenWindow(Frame frame) {
			super(frame);
			addMouseListener(this);
		}

		/*****************************************************************************/

		@Override
		public void paint(Graphics g) {
			Rectangle bounds = getBounds();

			g.setColor(Color.black);
			g.fillRect(0, 0, bounds.width, bounds.height);

			int w = image.getWidth(null);
			int h = image.getHeight(null);

			int h2 = bounds.width * h / w;
			int w2 = bounds.height * w / h;

			if (h2 < bounds.height) {
				g.drawImage(image, 0, bounds.height / 2 - h2 / 2, bounds.width,
						h2, null);
			} else {
				g.drawImage(image, bounds.width / 2 - w2 / 2, 0, w2,
						bounds.height, null);
			}
		} // End of method paint

		/*****************************************************************************/

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mouseClicked(MouseEvent e) {
		}

		public void mouseMoved(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
			terminate();
		}

	} // End of class ScreenWindow

	/*****************************************************************************/
	/*****************************************************************************/

} // End of class Fullscreen
