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
import java.awt.image.MemoryImageSource;

public class ColorDialog {

	/******************************************************************************/
	// FIELDS

	/******************************************************************************/

	private MyThinlet thinlet = null;
	private Task task = null;

	Image himage = null;

	int red, green, blue;
	float[] hsb = new float[3];

	/******************************************************************************/
	// CONSTRUCTOR

	ColorDialog(MyThinlet thinlet, Color color) {
		this.thinlet = thinlet;

		himage = createHueImage();

		red = color.getRed();
		green = color.getGreen();
		blue = color.getBlue();
		Color.RGBtoHSB(red, green, blue, hsb);

	}

	/******************************************************************************/

	public void setTask(Task task) {
		this.task = task;
	}

	/******************************************************************************/

	Image createHueImage() {
		int[] pixels = new int[256 * 256];

		for (int i = 0; i < 256; i++) {
			float sat = (255.0f - i) / 255.0f;
			for (int j = 0; j < 256; j++) {
				float hue = j / 255.0f;
				pixels[i * 256 + j] = Color.getHSBColor(hue, sat, 1.0f)
						.getRGB();
			}
		}

		MemoryImageSource source = new MemoryImageSource(256, 256, pixels, 0,
				256);
		return thinlet.createImage(source);

	}

	/******************************************************************************/

	public void cancel() {
		thinlet.remove(thinlet.find("colordialog"));
	}

	/******************************************************************************/

	public void ok() {
		thinlet.remove(thinlet.find("colordialog"));
		if (task != null) {
			task.execute();
		}
	}

	/******************************************************************************/

	public void show() {

		try {
			Object dialog = thinlet.parse("colordialog.xml", this);
			thinlet.add(dialog);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		updateFields();

	} // End of method shot

	/******************************************************************************/

	void updateFields() {
		setString(find("redfield"), "text", "" + red);
		setString(find("greenfield"), "text", "" + green);
		setString(find("bluefield"), "text", "" + blue);

		setString(find("huefield"), "text", "" + (int) (hsb[0] * 255));
		setString(find("satfield"), "text", "" + (int) (hsb[1] * 255));
		setString(find("brifield"), "text", "" + (int) (hsb[2] * 255));
	}

	/******************************************************************************/

	public void drawHueCanvas(Graphics g, Rectangle bounds) {
		g.drawImage(himage, 1, 1, 256, 256, null);
		g.setColor(Color.black);
		g.drawRect(0, 0, bounds.width - 1, bounds.height - 1);
		int x = (int) (hsb[0] * 255) + 1;
		int y = 255 - (int) (hsb[1] * 255) + 1;
		g.drawLine(x - 3, y, x + 3, y);
		g.drawLine(x, y - 3, x, y + 3);
	}

	/******************************************************************************/

	public void drawBriCanvas(Graphics g, Rectangle bounds) {
		g.setColor(new Color(0xFF8080));

		for (int i = 0; i < 256; i++) {
			g.setColor(Color.getHSBColor(hsb[0], hsb[1], (255 - i) / 255.0f));
			g.drawLine(1, i + 1, bounds.width, i + 1);
		}

		g.setColor(Color.black);
		g.drawRect(0, 0, bounds.width - 1, bounds.height - 1);

		g.setColor(new Color((red + 128) % 256, (green + 128) % 256,
				(blue + 128) % 256));
		int x = bounds.width / 2;
		int y = 1 + 255 - (int) (hsb[2] * 255);
		g.drawLine(x, y - 3, x, y + 3);
		g.drawLine(x - 3, y, x + 3, y);
	}

	/******************************************************************************/

	public void drawColCanvas(Graphics g, Rectangle bounds) {
		g.setColor(new Color(red, green, blue));
		g.fillRect(0, 0, bounds.width, bounds.height);
		g.setColor(Color.black);
		g.drawRect(0, 0, bounds.width - 1, bounds.height - 1);
	}

	/******************************************************************************/

	public void enterRGB() {
		try {
			red = Integer.parseInt(getString(find("redfield"), "text"));
			green = Integer.parseInt(getString(find("greenfield"), "text"));
			blue = Integer.parseInt(getString(find("bluefield"), "text"));
			Color.RGBtoHSB(red, green, blue, hsb);

			updateFields();
			thinlet.repaint();
		} catch (Exception ex) {
		}
	}

	/******************************************************************************/

	public void enterHSB() {
		try {
			int hue = Integer.parseInt(getString(find("huefield"), "text"));
			int sat = Integer.parseInt(getString(find("satfield"), "text"));
			int bri = Integer.parseInt(getString(find("brifield"), "text"));
			hsb[0] = hue / 255.0f;
			hsb[1] = sat / 255.0f;
			hsb[2] = bri / 255.0f;
			Color color = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
			red = color.getRed();
			green = color.getGreen();
			blue = color.getBlue();

			updateFields();
			thinlet.repaint();
		} catch (Exception ex) {
		}
	}

	/******************************************************************************/

	public void pressHueCanvas(MouseEvent e) {
		int hue = e.getX() - 1;
		if (hue < 0) {
			hue = 0;
		} else if (hue > 255) {
			hue = 255;
		}

		int sat = 255 - e.getY() + 1;
		if (sat < 0) {
			sat = 0;
		} else if (sat > 255) {
			sat = 255;
		}

		hsb[0] = hue / 255.0f;
		hsb[1] = sat / 255.0f;

		Color color = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
		red = color.getRed();
		green = color.getGreen();
		blue = color.getBlue();

		updateFields();
		thinlet.repaint();
	}

	/******************************************************************************/

	public void pressBriCanvas(MouseEvent e) {
		int bri = 255 - e.getY() + 1;
		if (bri < 0) {
			bri = 0;
		} else if (bri > 255) {
			bri = 255;
		}

		hsb[2] = bri / 255.0f;

		Color color = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
		red = color.getRed();
		green = color.getGreen();
		blue = color.getBlue();

		updateFields();
		thinlet.repaint();

	}

	/******************************************************************************/

	void setString(Object o, String s, String t) {
		thinlet.setString(o, s, t);
	}

	String getString(Object o, String s) {
		return thinlet.getString(o, s);
	}

	Object find(String s) {
		return thinlet.find(s);
	}

	/******************************************************************************/

	public Color getColor() {
		return new Color(red, green, blue);
	}

	/******************************************************************************/

} // End of class ColorDialog
