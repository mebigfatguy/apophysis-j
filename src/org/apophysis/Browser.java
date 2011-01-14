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
import java.awt.image.MemoryImageSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

public class Browser extends MyThinlet implements Constants {

	/*****************************************************************************/
	// CONSTANTS

	static final int pixelCountMax = 32768;
	static final int paletteTooltipTimeout = 1500;

	/*****************************************************************************/
	// FIELDS

	double previewdensity;
	int flameindex, gradientindex;
	String extension, identifier, filename;
	ControlPoint cp;
	int palette[][] = new int[256][3];
	double zoom;
	double center[] = new double[2];
	Renderer renderer;
	String flamestring;

	int indices[] = new int[400];
	int colors[] = new int[400];

	Image pimage = null;

	/*****************************************************************************/

	Browser(String title, String xmlfile, int width, int height)
			throws Exception {
		super(title, xmlfile, width, height);

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

		super.show();

	}

	/*****************************************************************************/

	public void listViewChange(Object list) {
		Object item = getSelectedItem(list);
		if (item == null) {
			return;
		}

		String name = getString(item, "text");

		createPalette(name);

	} // End of method listViewChange

	/*****************************************************************************/

	void createPalette(String name) {
		int ind, rgb, i, k;

		for (i = 0; i < 256; i++) {
			palette[i][0] = palette[i][1] = palette[i][2] = 0;
		}

		try {
			BufferedReader r = new BufferedReader(new FileReader(filename));

			// look for the right gradient

			while (true) {
				String line = r.readLine();
				if (line == null) {
					throw new IOException("Gradient " + name + " not found");
				}

				line = line.trim();
				if (!line.endsWith("{")) {
					continue;
				}

				String pname = line.substring(0, line.length() - 1).trim();
				if (pname.equals(name)) {
					break;
				}
			}

			// we are at the beginning of the gradient

			k = 0;

			while (true) {
				String line = r.readLine();
				if (line == null) {
					throw new IOException("Gradient " + name + " not complete");
				}

				if (line.indexOf("}") >= 0) {
					break;
				}

				ind = -1;
				rgb = -1;

				StringTokenizer tk = new StringTokenizer(line);

				while (tk.hasMoreTokens()) {
					String token = tk.nextToken();
					i = token.indexOf("=");
					if (i <= 0) {
						continue;
					}

					String key = token.substring(0, i);
					String val = token.substring(i + 1);

					if (key.equals("index")) {
						ind = Integer.parseInt(val);
					} else if (key.equals("color")) {
						rgb = Integer.parseInt(val);
					}
				}

				if ((ind >= 0) && (rgb >= 0)) {
					indices[k] = ind;
					colors[k] = rgb;
					k++;
				}
			}

			r.close();

			for (i = 0; i < k; i++) {
				ind = indices[i];
				while (ind < 0) {
					ind += 400;
				}
				ind = (int) (ind * 255 / 399 + 0.5);
				indices[i] = ind;

				palette[ind][0] = (colors[i]) & 0xFF;
				palette[ind][1] = (colors[i] >> 8) & 0xFF;
				palette[ind][2] = (colors[i] >> 16) & 0xFF;
			}

			i = 1;
			while (true) {
				int a = indices[i - 1];
				int b = indices[i];
				rgbBlend(a, b);
				i++;
				if (i == k) {
					break;
				}
			}

			if ((indices[0] != 0) || (indices[k - 1] != 255)) {
				int a = indices[k - 1];
				int b = indices[0] + 256;
				rgbBlend(a, b);
			}

			pimage = buildGradientImage(palette);
			repaint();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	} // End of method listViewChange

	/*****************************************************************************/

	// linear blending between indices a and b

	void rgbBlend(int a, int b) {
		double c, v;
		double vrange, range;

		if (a == b) {
			return;
		}

		range = b - a;
		vrange = palette[b % 256][0] - palette[a % 256][0];
		c = palette[a % 256][0];
		v = vrange / range;
		for (int i = a + 1; i < b; i++) {
			c += v;
			palette[i % 256][0] = (int) (c + 0.5);
		}

		vrange = palette[b % 256][1] - palette[a % 256][1];
		c = palette[a % 256][1];
		v = vrange / range;
		for (int i = a + 1; i < b; i++) {
			c += v;
			palette[i % 256][1] = (int) (c + 0.5);
		}

		vrange = palette[b % 256][2] - palette[a % 256][2];
		c = palette[a % 256][2];
		v = vrange / range;
		for (int i = a + 1; i < b; i++) {
			c += v;
			palette[i % 256][2] = (int) (c + 0.5);
		}
	}

	/*****************************************************************************/

	public void listViewOpen(Object list) {
		apply();
	} // End of method listViewOpen

	/*****************************************************************************/

	public void btnDefGradientClick() {

		Task task = new OpenFileTask();
		Global.opendialog = new OpenDialog(this, Global.browserPath, task);

		Global.opendialog.addFilter("Gradient files (*.ugr)", "*.ugr");
		Global.opendialog.addFilter("Fractint map files (*.map)", "*.map");

		Global.opendialog.show();

	} // End of method btnDefGradientClick

	/*****************************************************************************/

	void openFile() {
		if (Global.opendialog.filename != null) {
			filename = Global.opendialog.filename;

			Global.browserPath = (new File(filename)).getParent();

			Global.gradientFile = filename;

			if (filename.toLowerCase().endsWith(".map")) {
				listMapContents(filename);
			} else {
				listUgrContents(filename);
			}
		}

		Global.opendialog = null;

	}

	/*****************************************************************************/

	void listMapContents(String filename) {
		Object list = find("ListView");
		removeAll(list);

		Object item = createImpl("item");
		setString(item, "text", (new File(filename)).getName());

		add(list, item);

	} // End of method listFileContents

	/*****************************************************************************/

	void listUgrContents(String filename) {
		Object list = find("ListView");
		removeAll(list);

		Vector v = new Vector();

		try {
			BufferedReader r = new BufferedReader(new FileReader(filename));
			while (true) {
				String line = r.readLine();
				if (line == null) {
					break;
				}

				line = line.trim();
				if (!line.endsWith("{")) {
					continue;
				}

				String name = line.substring(0, line.length() - 1).trim();
				v.addElement(new SortableString(name));
			}
			r.close();

			QuickSort.qsort(v);

			int n = v.size();
			for (int i = 0; i < n; i++) {
				SortableString s = (SortableString) v.elementAt(i);
				Object item = createImpl("item");
				setString(item, "text", s.string);
				add(list, item);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	} // End of method listUgrContents

	/*****************************************************************************/

	public void drawPreview(Graphics g, Rectangle bounds) {

		g.setColor(Color.black);
		g.fillRect(0, 0, bounds.width, bounds.height);

		if (pimage != null) {
			g.drawImage(pimage, 1, 1, bounds.width - 2, bounds.height - 2, null);
		}

	} // End of method drawPreview

	/*****************************************************************************/

	Image buildGradientImage(int palette[][]) {
		int pixels[] = new int[256];

		for (int i = 0; i < 256; i++) {
			Color color = new Color(palette[i][0], palette[i][1], palette[i][2]);
			pixels[i] = color.getRGB();
		}

		MemoryImageSource source = new MemoryImageSource(256, 1, pixels, 0, 256);

		return createImage(source);

	}

	/*****************************************************************************/

	void apply() {
		Global.main.stopThread();
		Global.main.updateUndo();
		CMap.copyPalette(palette, Global.mainCP.cmap);
		Global.mainCP.cmapindex = -1;
		if (Global.adjust.visible()) {
			Global.adjust.updateDisplay();
		}
		if (Global.mutate.visible()) {
			Global.mutate.updateDisplay();
		}
		Global.main.timer.enable();
	}

	/*****************************************************************************/

	class OpenFileTask implements Task {

		public void execute() {
			openFile();
		}

	} // End of class OpenFileTask

	/*****************************************************************************/
	/*****************************************************************************/

} // End of class Browser
