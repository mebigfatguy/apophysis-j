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
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringReader;

public class Adjust extends MyThinlet implements Constants, ThreadTarget {

	/*****************************************************************************/
	// CONSTANTS

	static final int modeRotate = 0;
	static final int modeDummy1 = 1;
	static final int modeHue = 2;
	static final int modeSaturation = 3;
	static final int modeBrightness = 4;
	static final int modeContrast = 5;
	static final int modeDummy2 = 6;
	static final int modeBlur = 7;
	static final int modeFrequency = 8;

	static final int imgDragNone = 0;
	static final int imgDragRotate = 1;
	static final int imgDragStretch = 2;

	/*****************************************************************************/
	// FIELDS

	private boolean resetting;

	private final ControlPoint cp = new ControlPoint();

	private double previewdensity;

	private int[][] palette = new int[256][3];
	private final int[][] backupal = new int[256][3];
	private final int[][] tmpbackupal = new int[256][3];

	private int scrollmode = modeRotate;

	private Image pimage = null; // preview image
	private int imageheight = 0, imagewidth = 0;
	private int previewwidth = 0, previewheight = 0;

	private Image gimage = null; // gradient image

	private float[] hsv = new float[3];

	private final Renderer renderer;

	private final Color gray80 = new Color(0x808080);

	/*****************************************************************************/

	Adjust(String title, String xmlfile, int width, int height)
			throws Exception {
		super(title, xmlfile, width, height);

		launcher.setResizable(false);

		renderer = new Renderer(this);

		// populate the combobox

		try {
			Object combo = find("cmbPalette");

			for (int i = 0; i < CMap.cmapnames.length; i++) {
				Object choice = createImpl("choice");

				String pname = "" + (i + 1000) + "  " + CMap.cmapnames[i];
				setString(choice, "text", pname.substring(1));

				add(combo, choice);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		switch (Global.adjustPrevQual) {
		case 0:
			setBoolean(find("LowQuality"), "selected", true);
			previewdensity = Global.prevLowQuality;
			break;

		case 1:
			setBoolean(find("MediumQuality"), "selected", true);
			previewdensity = Global.prevMediumQuality;
			break;

		case 2:
			setBoolean(find("HighQuality"), "selected", true);
			previewdensity = Global.prevHighQuality;
			break;
		}

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

	public void setTab(int index) {
		setInteger(find("PageControl"), "selected", index);
	}

	/*****************************************************************************/

	public void drawGradientImage(Graphics g, Rectangle bounds) {
		g.setColor(Color.black);
		g.fillRect(0, 0, bounds.width, bounds.height);

		if (gimage != null) {
			g.drawImage(gimage, 1, 1, bounds.width - 2, bounds.height - 2, null);
		}

	} // End of method drawGradientImage

	/*****************************************************************************/

	public void pressGradientImage(MouseEvent e, Object canvas, Rectangle bounds) {
		if (e.isPopupTrigger()) {
			Rectangle r = new Rectangle(bounds);
			setToAbsolutePosition(canvas, r);
			Object popup = find("GradientPopup");
			popupPopup(popup, r.x + e.getX(), r.y + e.getY());
		} else {
		}

	} // End of method pressGradientImage

	/*****************************************************************************/

	public void btnColorPresetClick() {
		int n = CMap.cmapnames.length;
		int i = (int) (Math.random() * n);

		Object combo = find("cmbPalette");
		setInteger(combo, "selected", i);
		cmbPaletteChange(combo);

	}

	/*****************************************************************************/

	public void cmbPaletteChange(Object combo) {

		int i = getSelectedIndex(combo);
		if (i < 0) {
			return;
		}

		CMap.getCMap(i, 1, palette);
		CMap.copyPalette(palette, backupal);

		gimage = buildGradientImage(palette);

		CMap.copyPalette(palette, cp.cmap);

		drawPreview();

		apply();

	} // End of method cmdPaletteChange

	/*****************************************************************************/

	Image buildGradientImage(int palette[][]) {
		int[] pixels = new int[256];

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

		Global.mainCP.cmapindex = getSelectedIndex(find("cmbPalette"));
		CMap.copyPalette(palette, Global.mainCP.cmap);

		if (Global.editor.visible()) {
			Global.editor.updateDisplay();
		}
		if (Global.mutate.visible()) {
			Global.mutate.updateDisplay();
		}

		repaint(find("GradientImage"));

		updateFlame();

	} // End of method apply

	/*****************************************************************************/

	public void scrollBarChange(Object slider) {
		int value = getInteger(slider, "value");

		setString(find("lblVal"), "text", "" + value);

		if (resetting) {
			return;
		}

		switch (scrollmode) {
		case modeRotate:
			applyRotate(value);
			break;
		case modeHue:
			applyHue(value);
			break;
		case modeSaturation:
			applySaturation(value);
			break;
		case modeContrast:
			applyContrast(value);
			break;
		case modeBrightness:
			applyBrightness(value);
			break;
		case modeBlur:
			applyBlur(value);
			break;
		case modeFrequency:
			applyFrequency(value);
			break;
		}

		CMap.copyPalette(palette, cp.cmap);

		drawPreview();

	} // End of method scrollBarChange

	/*****************************************************************************/

	void applyRotate(int value) {
		for (int i = 0; i < 256; i++) {
		    int[] tuple = backupal[(256 + i - value) % 256];
			palette[i][0] = tuple[0];
			palette[i][1] = tuple[1];
			palette[i][2] = tuple[2];
		}

	} // End of method applyRotate

	/*****************************************************************************/

	void applyHue(int value) {
		for (int i = 0; i < 256; i++) {
			hsv = Color.RGBtoHSB(backupal[i][0], backupal[i][1],
					backupal[i][2], hsv);
			if (hsv[1] != 0) {
				int hue = (int) (360 * hsv[0]);
				hue = (360 + hue + value) % 360;
				hsv[0] = hue / 360.0f;

				Color color = Color.getHSBColor(hsv[0], hsv[1], hsv[2]);
				palette[i][0] = color.getRed();
				palette[i][1] = color.getGreen();
				palette[i][2] = color.getBlue();
			}
		}

	} // End of method applyHue

	/*****************************************************************************/

	void applySaturation(int value) {
		for (int i = 0; i < 256; i++) {
			hsv = Color.RGBtoHSB(backupal[i][0], backupal[i][1],
					backupal[i][2], hsv);
			if (hsv[1] != 0) {
				int sat = (int) (100 * hsv[1]);
				sat = sat + value;
				if (sat < 0) {
					sat = 0;
				}
				if (sat > 100) {
					sat = 100;
				}
				hsv[1] = sat / 100.0f;

				Color color = Color.getHSBColor(hsv[0], hsv[1], hsv[2]);
				palette[i][0] = color.getRed();
				palette[i][1] = color.getGreen();
				palette[i][2] = color.getBlue();
			}
		}

	} // End of method applySaturation

	/*****************************************************************************/

	void applyContrast(int value) {
		if (value > 0) {
			value = 2 * value;
		}

		for (int i = 0; i < 256; i++) {
			int r = backupal[i][0];
			int g = backupal[i][1];
			int b = backupal[i][2];

			r = (r + value * (r - 127) / 100);
			g = (g + value * (g - 127) / 100);
			b = (b + value * (b - 127) / 100);

			if (r > 255) {
				r = 255;
			} else if (r < 0) {
				r = 0;
			}

			if (g > 255) {
				g = 255;
			} else if (g < 0) {
				g = 0;
			}

			if (b > 255) {
				b = 255;
			} else if (b < 0) {
				b = 0;
			}

			palette[i][0] = r;
			palette[i][1] = g;
			palette[i][2] = b;
		}

	} // End of method applyContrast

	/*****************************************************************************/

	void applyBrightness(int value) {

		for (int i = 0; i < 256; i++) {
			int r = backupal[i][0] + value;
			if (r > 255) {
				r = 255;
			} else if (r < 0) {
				r = 0;
			}

			int g = backupal[i][1] + value;
			if (g > 255) {
				g = 255;
			} else if (g < 0) {
				g = 0;
			}

			int b = backupal[i][2] + value;
			if (b > 255) {
				b = 255;
			} else if (b < 0) {
				b = 0;
			}

			palette[i][0] = r;
			palette[i][1] = g;
			palette[i][2] = b;
		}

	} // End of method applyBrightness

	/*****************************************************************************/

	void applyBlur(int value) {
		int r, g, b, n, k;

		for (int i = 0; i < 256; i++) {
			n = 0;
			r = 0;
			g = 0;
			b = 0;
			for (int j = i - value; j <= i + value; j++) {
				k = (j + 256) % 256;
				if (k != i) {
					r += backupal[k][0];
					g += backupal[k][1];
					b += backupal[k][2];
					n += 1;
				}
			}
			if (n != 0) {
				palette[i][0] = r / n;
				palette[i][1] = g / n;
				palette[i][2] = b / n;
			} else {
				palette[i][0] = backupal[i][0];
				palette[i][1] = backupal[i][1];
				palette[i][2] = backupal[i][2];
			}
		}

	} // End of method applyBlur

	/*****************************************************************************/

	void applyFrequency(int value) {

		for (int i = 0; i < 256; i++) {
			palette[i][0] = backupal[i][0];
			palette[i][1] = backupal[i][1];
			palette[i][2] = backupal[i][2];
		}

		int n = 256 / value;
		for (int j = 0; j <= value; j++) {
			for (int i = 0; i <= n; i++) {
				if (i + j * n < 256) {
					if (i * value < 256) {
						palette[i + j * n][0] = backupal[i * value][0];
						palette[i + j * n][1] = backupal[i * value][1];
						palette[i + j * n][2] = backupal[i * value][2];
					}
				}
			}
		}

	} // End of method applyFrequency

	/*****************************************************************************/

	public void btnMenuClick(Object combo) {
		scrollmode = getSelectedIndex(combo);

		CMap.copyPalette(palette, backupal);

		switch (scrollmode) {
		case modeRotate:
			setScroll(-128, 128, 0);
			break;
		case modeHue:
			setScroll(-180, 180, 0);
			break;
		case modeSaturation:
			setScroll(-100, 100, 0);
			break;
		case modeContrast:
			setScroll(-100, 100, 0);
			break;
		case modeBrightness:
			setScroll(-255, 255, 0);
			break;
		case modeBlur:
			setScroll(0, 127, 0);
			break;
		case modeFrequency:
			setScroll(1, 10, 1);
			break;
		}

	} // End of method btnMenuClick

	/*****************************************************************************/

	void setScroll(int min, int max, int val) {

		Object slider = find("ScrollBar");
		setInteger(slider, "minimum", min);
		setInteger(slider, "maximum", max);
		setInteger(slider, "value", val);

		Object label = find("lblVal");
		setString(label, "text", "" + val);

	} // End of method setScroll

	/*****************************************************************************/

	void drawPreview() {
		if (resetting) {
			return;
		}
		if (imagewidth == 0) {
			return;
		}

		renderer.stop();

		cp.sample_density = previewdensity;
		cp.spatial_oversample = Global.defOversample;
		cp.spatial_filter_radius = Global.defFilterRadius;

		renderer.setCP(cp);

		renderer.render();
		pimage = renderer.getImage();

		gimage = buildGradientImage(palette);

		repaint();

	} // End of method drawPreview

	/*****************************************************************************/

	public void btnOpenClick() {

		Global.browser.show();

	} // End of method btnOpenClick

	/*****************************************************************************/

	public void updateDisplay() {
		updateDisplay(false);
	}

	public void updateDisplay(boolean previewonly) {
		int pw, ph;
		double r;

		cp.copy(Global.mainCP);

		Rectangle bounds = getRectangle(find("PrevPnl"), "bounds");

		if (bounds == null) {
			pw = 160 - 2;
			ph = 120 - 2;
		} else {
			pw = bounds.width - 2;
			ph = bounds.height - 2;
		}

		if ((cp.width * 1.0 / cp.height) > (pw * 1.0 / ph)) {
			previewwidth = pw;
			r = cp.width * 1.0 / previewwidth;
			previewheight = (int) (cp.height / r + 0.5);
		} else {
			previewheight = ph;
			r = cp.height * 1.0 / previewheight;
			previewwidth = (int) (cp.width / r + 0.5);
		}

		cp.adjustScale(previewwidth, previewheight);

		if (!previewonly) {
			resetting = true;

			setInteger(find("scrollGamma"), "value", (int) (cp.gamma * 100));
			setString(find("txtGamma"), "text", "" + round6(cp.gamma));

			setInteger(find("scrollBrightness"), "value",
					(int) (cp.brightness * 100));
			setString(find("txtBrightness"), "text", "" + round6(cp.brightness));

			setInteger(find("scrollVibrancy"), "value",
					(int) (cp.vibrancy * 100));
			setString(find("txtVibrancy"), "text", "" + round6(cp.vibrancy));

			setInteger(find("scrollZoom"), "value", (int) (cp.zoom * 1000));
			setString(find("txtZoom"), "text", "" + round6(cp.zoom));

			setInteger(find("scrollAngle"), "value",
					(int) (cp.fangle * 18000 / Math.PI));
			setString(find("txtAngle"), "text", ""
					+ (cp.fangle * 180 / Math.PI));

			if ((Math.abs(cp.center[0]) < 1000)
					&& (Math.abs(cp.center[1]) < 1000)) {
				setInteger(find("scrollCenterX"), "value",
						(int) (cp.center[0] * 1000));
				setString(find("txtCenterX"), "text", "" + round6(cp.center[0]));

				setInteger(find("scrollCenterY"), "value",
						(int) (cp.center[1] * 1000));
				setString(find("txtCenterY"), "text", "" + round6(cp.center[1]));
			} else {
				setInteger(find("scrollCenterX"), "value", 0);
				setInteger(find("scrollCenterY"), "value", 0);
			}

			setColor(find("btnBackground"), "background", new Color(
					cp.background[0], cp.background[1], cp.background[2]));

			double scale = 100 * cp.pixels_per_unit / previewwidth;
			setString(find("editPPU"), "text", "" + round6(scale));
			setString(find("txtTime"), "text", "" + round6(cp.time));

			getMainWindowSize();

			if (cp.cmapindex >= 0) {
				setInteger(find("cmbPalette"), "selected", cp.cmapindex);
				setString(find("cmbPalette"), "text",
						CMap.cmapnames[cp.cmapindex]);
			} else {
				setInteger(find("cmbPalette"), "selected", -1);
				setString(find("cmbPalette"), "text", " ");
			}

			setInteger(find("ScrollBar"), "value", 0);

			CMap.copyPalette(cp.cmap, palette);
			CMap.copyPalette(cp.cmap, backupal);

			for (int i = 0; i < 3; i++) {
				setString(find("btnPreset" + (i + 1)), "text",
						Global.sizepresets[i][2] + " x "
								+ Global.sizepresets[i][3]);
			}

			updateSizePresetButtons();

			setBoolean(find("chkResizeMain"), "selected", Global.resizeMain);
			setBoolean(find("chkMaintainRatio"), "selected",
					Global.maintainRatio);

			resetting = false;
		}

		drawPreview();

	} // End of method updateDisplay

	/*****************************************************************************/

	void updateSizePresetButtons() {
		for (int i = 0; i < 3; i++) {
			setString(find("btnPreset" + (i + 1)), "text",
					Global.sizepresets[i][2] + " x " + Global.sizepresets[i][3]);
		}
	}

	/*****************************************************************************/

	void updateFlame() {
		updateFlame(false);
	}

	void updateFlame(boolean bgonly) {

		if (!bgonly) {
			Global.main.stopThread();
		}

		Global.main.updateUndo();
		Global.mainCP.copy(cp, true);

		if (Global.editor.visible()) {
			Global.editor.updateDisplay();
		}
		if (Global.mutate.visible()) {
			Global.mutate.updateDisplay();
		}

		if (bgonly) {
			Global.main.repaint();
		} else {
			Global.main.timer.enable();
		}

	} // End of method updateFlame

	/*****************************************************************************/

	public void txtRenderChange() {
		String s;

		try {
			s = getString(find("txtGamma"), "text");
			cp.gamma = Double.parseDouble(s);
		} catch (Exception ex) {
		}

		try {
			s = getString(find("txtBrightness"), "text");
			cp.brightness = Double.parseDouble(s);
		} catch (Exception ex) {
		}

		try {
			s = getString(find("txtVibrancy"), "text");
			cp.vibrancy = Double.parseDouble(s);
		} catch (Exception ex) {
		}

		updateFlame();

	}

	/*****************************************************************************/

	public void scrollEnd() {
		updateFlame();
	}

	/*****************************************************************************/

	public void scrollCenterXChange(Object slider) {

		cp.center[0] = getInteger(slider, "value") / 1000.0;
		setString(find("txtCenterX"), "text", cp.center[0] + "");
		drawPreview();

	}

	/*****************************************************************************/

	public void scrollCenterYChange(Object slider) {

		cp.center[1] = getInteger(slider, "value") / 1000.0;
		setString(find("txtCenterY"), "text", cp.center[1] + "");
		drawPreview();
	}

	/*****************************************************************************/

	public void scrollAngleChange(Object slider) {

		cp.fangle = getInteger(slider, "value") * Math.PI / 18000.0;
		setString(find("txtAngle"), "text", "" + (cp.fangle * 180 / Math.PI));
		drawPreview();

	}

	/*****************************************************************************/

	public void scrollZoomChange(Object slider) {

		cp.zoom = getInteger(slider, "value") / 1000.0;
		setString(find("txtZoom"), "text", "" + cp.zoom);
		drawPreview();

	}

	/*****************************************************************************/

	public void scrollGammaChange(Object slider) {

		cp.gamma = getInteger(slider, "value") / 100.0;
		setString(find("txtGamma"), "text", "" + cp.gamma);
		drawPreview();

	}

	/*****************************************************************************/

	public void scrollBrightnessChange(Object slider) {

		cp.brightness = getInteger(slider, "value") / 100.0;
		setString(find("txtBrightness"), "text", "" + cp.brightness);
		drawPreview();

	}

	/*****************************************************************************/

	public void scrollVibrancyChange(Object slider) {

		cp.vibrancy = getInteger(slider, "value") / 100.0;
		setString(find("txtVibrancy"), "text", "" + cp.vibrancy);
		drawPreview();

	}

	/*****************************************************************************/

	public void editPPUValidate(Object field) {
		try {
			double value = Double.parseDouble(getString(field, "text"));
			Global.main.updateUndo();
			cp.pixels_per_unit = value / 100 * previewwidth;
			drawPreview();
			updateFlame();
		} catch (Exception ex) {
		}

	} // End of method editPPUValidate

	/*****************************************************************************/

	public void timeChanged(Object field) {
		try {
			double value = Double.parseDouble(getString(field, "text"));
			Global.main.updateUndo();
			cp.time = value;
			Global.mainCP.copy(cp, true);
		} catch (Exception ex) {
		}
	} // End of method timeChanged

	/*****************************************************************************/

	public void txtCameraChange() {
		try {
			cp.zoom = Double.parseDouble(getString(find("txtZoom"), "text"));
			cp.center[0] = Double.parseDouble(getString(find("txtCenterX"), "text"));
			cp.center[1] = Double.parseDouble(getString(find("txtCenterY"), "text"));
			cp.fangle = Double.parseDouble(getString(find("txtAngle"), "text")) * Math.PI / 180.0;

			setInteger(find("scrollZoom"), "value", (int) (cp.zoom * 1000));
			setInteger(find("scrollCenterX"), "value",
					(int) (cp.center[0] * 1000));
			setInteger(find("scrollCenterY"), "value",
					(int) (cp.center[1] * 1000));
			setInteger(find("scrollAngle"), "value",
					(int) (cp.fangle * 18000 / Math.PI));

			drawPreview();
			updateFlame();
		} catch (Exception ex) {
		}
	}

	/*****************************************************************************/

	public void mnuSmoothGradientClick() {
		Global.main.mnuSmoothGradientClick();
	}

	/*****************************************************************************/

	public void btnPresetClick(int index) {
		int w = Global.sizepresets[index - 1][2];
		int h = Global.sizepresets[index - 1][3];

		if (Global.maintainRatio) {
			int h2 = w * imageheight / imagewidth;
			if (h2 > h) {
				w = h * imagewidth / imageheight;
			} else {
				h = h2;
			}
			setString(find("txtWidth"), "text", "" + w);
			setString(find("txtHeight"), "text", "" + h);
		}

		imagewidth = w;
		imageheight = h;
		Global.main.setWindowSize(imagewidth, imageheight, Global.resizeMain);
	}

	/*****************************************************************************/

	public void btnSetClick(int index) {

		imagewidth = Integer.parseInt(getString(find("txtWidth"), "text"));
		Global.sizepresets[index - 1][2] = imagewidth;

		imageheight = Integer.parseInt(getString(find("txtHeight"), "text"));
		Global.sizepresets[index - 1][3] = imageheight;

		updateSizePresetButtons();
	}

	/*****************************************************************************/

	public void updateUndoControls(int undoindex, int undomax) {
		setBoolean(find("btnUndo"), "enabled", undoindex > 0);
		setBoolean(find("btnRedo"), "enabled", undoindex < undomax);
	}

	/*****************************************************************************/

	public void undo() {
		Global.main.undo();
	}

	/*****************************************************************************/

	public void redo() {
		Global.main.redo();
	}

	/*****************************************************************************/

	public void btnBackgroundClick() {
		Color color = getColor(find("btnBackground"), "background");

		Global.colordialog = new ColorDialog(this, color);
		Global.colordialog.setTask(new ColorTask());
		Global.colordialog.show();

	}

	void changeBackground() {
		Global.main.updateUndo();

		Color color = Global.colordialog.getColor();
		setColor(find("btnBackground"), "background", color);
		cp.background[0] = color.getRed();
		cp.background[1] = color.getGreen();
		cp.background[2] = color.getBlue();
		drawPreview();
		updateFlame(true);
	}

	/*****************************************************************************/

	public void btnApplySizeClick() {
		try {
			int w = Integer.parseInt(getString(find("txtWidth"), "text"));
			int h = Integer.parseInt(getString(find("txtHeight"), "text"));
			/*
			 * if(Global.maintainRatio) { int h2 = w*imageheight/imagewidth;
			 * if(h2>h) w = h*imagewidth/imageheight; else h = h2;
			 * setString(find("txtWidth"),"text",""+w);
			 * setString(find("txtHeight"),"text",""+h); }
			 */
			Global.main.setWindowSize(w, h, Global.resizeMain);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	} // End of method btnApplySizeClick

	/*****************************************************************************/

	public void widthChanged(Object combo, int option) {
		int index = getSelectedIndex(combo);
		if ((index < 0) && (option == 0)) {
			return;
		}

		try {
			if (Global.maintainRatio) {
				int w = Integer.parseInt(getString(combo, "text"));
				int h = cp.height * w / cp.width;
				setString(find("txtHeight"), "text", "" + h);
			}
		} catch (Exception ex) {
		}

	}

	/*****************************************************************************/

	public void heightChanged(Object combo, int option) {
		int index = getSelectedIndex(combo);
		if ((index < 0) && (option == 0)) {
			return;
		}

		try {
			if (Global.maintainRatio) {
				int h = Integer.parseInt(getString(combo, "text"));
				int w = cp.width * h / cp.height;
				setString(find("txtWidth"), "text", "" + w);
			}
		} catch (Exception ex) {
		}
	}

	/*****************************************************************************/

	public void chkResizeMainClick(Object button) {
		Global.resizeMain = getBoolean(button, "selected");
	}

	/*****************************************************************************/

	public void chkMaintainRatioClick(Object button) {
		Global.maintainRatio = getBoolean(button, "selected");
	}

	/*****************************************************************************/

	void getMainWindowSize() {
		imagewidth = Global.mainCP.width;
		imageheight = Global.mainCP.height;
		setString(find("txtWidth"), "text", "" + imagewidth);
		setString(find("txtHeight"), "text", "" + imageheight);

	} // End of method getMainWindowSize

	/*****************************************************************************/

	public void drawPreviewImage(Graphics g, Rectangle bounds) {

		g.setColor(gray80);
		g.fillRect(0, 0, bounds.width, bounds.height);

		if (pimage != null) {
			int wi = pimage.getWidth(null);
			int hi = pimage.getHeight(null);
			int x = bounds.width / 2 - wi / 2;
			int y = bounds.height / 2 - hi / 2;

		    Color bg = new Color(cp.background[0], cp.background[1],
		                cp.background[2]);
			g.setColor(bg);
			g.fillRect(x, y, wi, hi);
			g.drawImage(pimage, x, y, wi, hi, null);
		}

	} // End of method drawPreviewImage

	/*****************************************************************************/

	public void pressPreview(MouseEvent e, Object canvas, Rectangle bounds) {
		if (e.isPopupTrigger()) {
			Rectangle r = new Rectangle(bounds);
			setToAbsolutePosition(canvas, r);
			Object popup = find("PreviewPopup");
			popupPopup(popup, r.x + e.getX(), r.y + e.getY());
		} else {
		}

	} // End of method pressPreview

	/*****************************************************************************/

	public void mnuLowQualityClick(Object item) {
		previewdensity = Global.prevLowQuality;
		Global.adjustPrevQual = 0;
		drawPreview();
	} // End of method mnuQualityClick

	public void mnuMediumQualityClick(Object item) {
		previewdensity = Global.prevMediumQuality;
		Global.adjustPrevQual = 1;
		drawPreview();
	} // End of method mnuQualityClick

	public void mnuHighQualityClick(Object item) {
		previewdensity = Global.prevHighQuality;
		Global.adjustPrevQual = 2;
		drawPreview();
	} // End of method mnuQualityClick

	/*****************************************************************************/

	public void mnuRandomizeClick() {
		updateGradient(CMap.randomGradient());
		apply();
	} // End of method mnuRandomizeClick

	/*****************************************************************************/

	public void mnuInvertClick() {
		for (int i = 0; i < 256; i++) {
			palette[i][0] = 255 - palette[i][0];
			palette[i][1] = 255 - palette[i][1];
			palette[i][2] = 255 - palette[i][2];
		}

		updateGradient(palette);
		apply();

	} // End of method mnuInvertClick

	/*****************************************************************************/

	public void mnuReverseClick() {
		for (int i = 0; i < 256; i++) {
			tmpbackupal[i][0] = palette[255 - i][0];
			tmpbackupal[i][1] = palette[255 - i][1];
			tmpbackupal[i][2] = palette[255 - i][2];
		}

		updateGradient(tmpbackupal);
		apply();

	} // End of method mnuReverseClick

	/*****************************************************************************/

	public void mnuSmoothPaletteClick() {
		Global.main.mnuSmoothGradientClick();
	} // End of method mnuSmoothPaletteClick

	/*****************************************************************************/

	public void mnuOpenClick() {
		Global.browser.show();
	}

	/*****************************************************************************/

	public void mnuSaveGradientClick() {

		if (Global.gradientFile.length() == 0) {
			Global.gradientFile = Global.mainCP.name + ".ugr";
		}

		String ename = Global.mainCP.name;

		Task task = new SaveGradientTask();
		Global.entrydialog = new EntryDialog(this, Global.gradientFile, ename,
				task);
		Global.entrydialog.show();

	} // End of method mnuSaveGradientClick

	/*****************************************************************************/

	void saveGradient(String filename, String entryname) {
		String text = CMap.gradientFromPalette(palette, entryname);
		FileManager.saveEntry(text, entryname, filename);
	}

	/*****************************************************************************/

	public void mnuSaveMapClick() {

		Task task = new SaveMapTask();
		String name = Global.mainCP.name + ".map";
		Global.savedialog = new SaveDialog(this, Global.browserPath, name, task);
		Global.savedialog.show();

	}

	/*****************************************************************************/

	void saveMap(String filename) {
		try {
			PrintWriter w = new PrintWriter(new FileWriter(filename));
			String comment = "  Exported from Apophysis 2.0";
			for (int i = 0; i < 256; i++) {
				String p1 = "" + palette[i][0];
				while (p1.length() < 3) {
					p1 = "0" + p1;
				}
				String p2 = "" + palette[i][1];
				while (p2.length() < 3) {
					p2 = "0" + p2;
				}
				String p3 = "" + palette[i][2];
				while (p3.length() < 3) {
					p3 = "0" + p3;
				}
				w.println(" " + p1 + " " + p2 + " " + p3 + comment);
				comment = "";
			}
			w.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	} // End of method saveMap

	/*****************************************************************************/

	public void mnuSaveDefaultClick() {
	} // End of method mnuSaveDefaultClick

	/*****************************************************************************/

	public void mnuCopyClick() {
		String text = CMap.gradientFromPalette(palette, Global.mainCP.name);
		text = Global.mainCP.name + " {\n" + text + "}\n";
		Global.main.setClipboard(text);
	}

	/*****************************************************************************/

	public void mnuPasteClick() {
		String text = Global.main.getClipboard();

		BufferedReader r = new BufferedReader(new StringReader(text));
		palette = CMap.paletteFromGradient(r);

		updateGradient(palette);
		apply();

	}

	/*****************************************************************************/

	double round6(double x) {
		long l = (long) (x * 1000000);
		return l / 1000000.0;
	}

	/*****************************************************************************/

	void updateGradient(int pal[][]) {
		setInteger(find("ScrollBar"), "value", 0);

		CMap.copyPalette(pal, palette);
		CMap.copyPalette(pal, backupal);
		CMap.copyPalette(pal, cp.cmap);

		gimage = buildGradientImage(palette);

		repaint();

		drawPreview();

	} // End of method updateGradient

	/*****************************************************************************/

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
	/*****************************************************************************/

	class ColorTask implements Task {

		@Override
		public void execute() {
			changeBackground();
		}

	}

	/*****************************************************************************/
	/*****************************************************************************/

	class SaveGradientTask implements Task {

		@Override
		public void execute() {
			Global.gradientFile = Global.entrydialog.filename;
			saveGradient(Global.entrydialog.filename,
					Global.entrydialog.entryname);
		}

	}

	/*****************************************************************************/
	/*****************************************************************************/

	class SaveMapTask implements Task {

		@Override
		public void execute() {
			saveMap(Global.savedialog.filename);
		}

	}

	/*****************************************************************************/
	/*****************************************************************************/

} // End of class Adjust
