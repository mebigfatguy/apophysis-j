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

public class Mutate extends MyThinlet implements Constants, ThreadTarget {

	/*****************************************************************************/
	// CONSTANTS

	/*****************************************************************************/
	// FIELDS

	int imagewidth = 106;
	int imageheight = 78;

	String name, nick, url;
	double previewdensity = 0;
	boolean updating;
	ControlPoint cps[];
	ControlPoint mutants[];
	Renderer renderer;
	int time;
	boolean bstop;
	double brightness, gamma, vibrancy;

	double zoom;
	double[] center = new double[2];
	int[][] cmap = new int[256][3];

	boolean maintainsymmetry = true;
	boolean resetlocation = true;
	boolean samenumber = false;

	Image[] images = new Image[9];

	Updater updater = null;

	long initseed = 0;
	long seed = 0;

	/*****************************************************************************/

	Mutate(String title, String xmlfile, int width, int height)
			throws Exception {
		super(title, xmlfile, width, height);

		launcher.setResizable(false);

		buildVariationMenu();

		previewdensity = Global.prevLowQuality;

		cps = new ControlPoint[9];
		mutants = new ControlPoint[9];

		renderer = new Renderer(this);

		for (int i = 0; i <= 8; i++) {
			cps[i] = new ControlPoint();
			mutants[i] = new ControlPoint();
		}

		time = 40;
		setInteger(find("scrollTime"), "value", time);
		double xtime = time / 100.0;
		setString(find("lblTime"), "text", "" + xtime);

		switch (Global.mutatePrevQual) {
		case 0:
			previewdensity = Global.prevLowQuality;
			setBoolean(find("LowQuality"), "selected", true);
			break;

		case 1:
			previewdensity = Global.prevMediumQuality;
			setBoolean(find("MediumQuality"), "selected", true);
			break;

		case 2:
			previewdensity = Global.prevHighQuality;
			setBoolean(find("HighQuality"), "selected", true);
			break;
		}

		initseed = (long) (Math.random() * 1234567890L);
		seed = initseed;

		randomSet();

	}

	/*****************************************************************************/

	void buildVariationMenu() {
		Object choice = null;
		Object combo = find("cmbTrend");

		choice = createImpl("choice");
		setString(choice, "text", "Random");
		add(combo, choice);

		int nv = XForm.getNrVariations();
		for (int i = 0; i < nv; i++) {
			choice = createImpl("choice");
			setString(choice, "text", XForm.getVariation(i).getName());
			add(combo, choice);
		}

		setInteger(combo, "selected", 0);

	} // End of method buildVariationMenu

	/*****************************************************************************/

	void randomSet() {
		Global.randomGenerator.setSeed(seed);

		int min = samenumber ? Global.transforms : Global.mutantMinTransforms;
		int max = samenumber ? Global.transforms : Global.mutantMaxTransforms;
		int ivar = getInteger(find("cmbTrend"), "selected") - 1;

		for (int i = 1; i <= 8; i++) {
			cps[i].randomCP(min, max, false);
			cps[i].setVariation(ivar);
			if (!cps[0].hasFinalXform) {
				cps[i].xform[cps[i].nxforms].clear();
				cps[i].xform[cps[i].nxforms].symmetry = 1;
			}
		}

		interpolate();

	} // End of method randomSet

	/*****************************************************************************/

	void interpolate() {

		for (int i = 1; i <= 8; i++) {
			if (bstop)
				return;
			cps[0].time = 0;
			cps[i].time = 1;
			mutants[i].clear();
			mutants[i].interpolateX(cps[0], cps[i], time / 100.0);
			mutants[i].cmapindex = cps[0].cmapindex;
			CMap.copyPalette(cps[0].cmap, mutants[i].cmap);

			/*
			 * System.out.println("mutant"+i+" cmapindex="+mutants[i].cmapindex);
			 * System.out.print("  "); for(int j=0;j<5;j++)
			 * System.out.print(cps[0].cmap[j][0]+" "); System.out.print("  ");
			 * for(int j=0;j<5;j++) System.out.print(mutants[i].cmap[j][0]+" ");
			 * System.out.println("");
			 */

			for (int j = 0; j < 3; j++)
				mutants[i].background[j] = Global.mainCP.background[j];
			if (maintainsymmetry) {
				for (int j = 0; j < Global.transforms; j++)
					if (cps[i].xform[j].symmetry == 1)
						mutants[i].xform[j].copy(cps[0].xform[j]);
			}
		}

	} // End of method interpolate

	/*****************************************************************************/

	@Override
	public boolean destroy() {
		stopRendering();
		hide();
		return false;
	}

	/*****************************************************************************/

	@Override
	public void show() {
		super.show();

		setBoolean(find("chkMaintainSymmetry"), "selected", maintainsymmetry);
		setBoolean(find("chkResetLocation"), "selected", resetlocation);
		setBoolean(find("chkSameNumber"), "selected", samenumber);

	}

	/*****************************************************************************/

	public void drawImage(Graphics g, Rectangle bounds, int index) {
		if (index == 0) {
			imagewidth = bounds.width;
			imageheight = bounds.height;
		}

		Color bg = new Color(cps[0].background[0], cps[0].background[1],
				cps[0].background[2]);
		g.setColor(bg);
		g.fillRect(0, 0, bounds.width, bounds.height);

		if (images[index] != null)
			g.drawImage(images[index], 0, 0, null);

	} // End of method drawImage

	/*****************************************************************************/

	public void updateDisplay() {
		stopRendering();

		cps[0].copy(Global.mainCP);
		cps[0].adjustScale(imagewidth, imageheight);

		CMap.copyPalette(Global.mainCP.cmap, cps[0].cmap);
		CMap.copyPalette(Global.mainCP.cmap, cmap);

		name = Global.mainCP.name;
		nick = Global.mainCP.nick;
		url = Global.mainCP.url;

		System.out.println("name=" + name + " nick=" + nick + " url=" + url);

		zoom = Global.mainCP.zoom;
		center[0] = Global.mainCP.center[0];
		center[1] = Global.mainCP.center[1];

		vibrancy = cps[0].vibrancy;
		gamma = cps[0].gamma;
		brightness = cps[0].brightness;

		interpolate();
		showMain();
		showMutants();

	} // End of method updateDisplay

	/*****************************************************************************/

	void showMutants() {
		for (int i = 1; i < 9; i++)
			images[i] = null;

		updater = new Updater();
		updater.start();
	}

	/*****************************************************************************/

	public void scrollTimeChanging(Object slider) {
		time = getInteger(slider, "value");
		double xtime = time / 100.0;
		setString(find("lblTime"), "text", "" + xtime);
	}

	public void scrollTimeChanged(Object slider) {
		stopRendering();

		time = getInteger(slider, "value");
		interpolate();
		showMutants();
	}

	/*****************************************************************************/

	void showMain() {
		cps[0].width = imagewidth;
		cps[0].height = imageheight;
		cps[0].spatial_oversample = Global.defOversample;
		cps[0].spatial_filter_radius = Global.defFilterRadius;
		cps[0].sample_density = previewdensity;
		cps[0].brightness = brightness;
		cps[0].gamma = gamma;
		cps[0].vibrancy = vibrancy;

		CMap.copyPalette(cps[0].cmap, cmap);
		for (int i = 0; i < 3; i++)
			cps[0].background[i] = Global.mainCP.background[i];

		if (resetlocation) {
			cps[0].calcBoundBox();
			zoom = 0;
			center[0] = cps[0].center[0];
			center[1] = cps[0].center[1];
		}

		cps[0].zoom = zoom;
		cps[0].center[0] = center[0];
		cps[0].center[1] = center[1];

		renderer.setCP(cps[0]);
		renderer.render();
		images[0] = renderer.getImage();

		repaint();

	} // End of method showMain

	/*****************************************************************************/

	public void imageClick(MouseEvent e, Object canvas, Rectangle bounds) {
		if (e.isPopupTrigger()) {
			Rectangle r = new Rectangle(bounds);
			setToAbsolutePosition(canvas, r);
			Object popup = find("PreviewPopup");
			popupPopup(popup, r.x + e.getX(), r.y + e.getY());
		} else {
			seed++;
			stopRendering();
			randomSet();
			showMutants();
		}

	} // End of method imageCliek

	/*****************************************************************************/

	public void mutantClick(MouseEvent e, int index, Object canvas,
			Rectangle bounds) {
		if (e.isPopupTrigger()) {
			Rectangle r = new Rectangle(bounds);
			setToAbsolutePosition(canvas, r);
			Object popup = find("PreviewPopup");
			popupPopup(popup, r.x + e.getX(), r.y + e.getY());
		} else {
			stopRendering();

			ControlPoint cpt = new ControlPoint();
			cpt.copy(cps[0]);

			bstop = true;

			cps[0].time = 0;
			cps[index].time = 1;
			cps[0].interpolateX(cps[0], cps[index], time / 100.0);

			CMap.copyPalette(cpt.cmap, cps[0].cmap);

			if (maintainsymmetry) {
				for (int i = 0; i < Global.transforms; i++)
					if (cpt.xform[i].symmetry == 1)
						cps[0].xform[i].copy(cpt.xform[i]);
			}

			bstop = false;

			showMain();
			interpolate();
			showMutants();
			updateFlame();

			cpt = null;
		}

	} // End of method mutantClick

	/*****************************************************************************/

	void updateFlame() {
		Global.main.stopThread();
		Global.main.updateUndo();

		Global.mainCP.copy(cps[0]);
		Global.mainCP.name = name;
		Global.mainCP.nick = nick;
		Global.mainCP.url = url;

		Global.transforms = Global.mainCP.trianglesFromCP(Global.mainTriangles);

		CMap.copyPalette(cmap, Global.mainCP.cmap);

		if (resetlocation) {
			Global.main.center[0] = cps[0].center[0];
			Global.main.center[1] = cps[0].center[1];
		}

		Global.main.timer.enable();
		if (Global.editor.visible())
			Global.editor.updateDisplay();

	} // End of method updateFlame

	/*****************************************************************************/

	public void chkMaintainSymmetryChange(Object box) {
		maintainsymmetry = getBoolean(box, "selected");
	}

	public void chkResetLocationChange(Object box) {
		resetlocation = getBoolean(box, "selected");
	}

	public void chkSameNumberChange(Object box) {
		samenumber = getBoolean(box, "selected");
	}

	/*****************************************************************************/

	public void cmbTrendChange(Object combo) {
		stopRendering();
		randomSet();
		showMutants();
	} // End of method cmdTrendChange

	/*****************************************************************************/

	void stopRendering() {
		if (renderer != null)
			renderer.stop();

		if (updater != null)
			updater.kill();

		renderer = new Renderer(this);
	}

	/*****************************************************************************/

	public void mnuLowQualityClick(Object button) {
		previewdensity = Global.prevLowQuality;
		Global.mutatePrevQual = 0;
		showMain();
		showMutants();
	}

	public void mnuMediumQualityClick(Object button) {
		previewdensity = Global.prevMediumQuality;
		Global.mutatePrevQual = 1;
		showMain();
		showMutants();
	}

	public void mnuHighQualityClick(Object button) {
		previewdensity = Global.prevHighQuality;
		Global.mutatePrevQual = 2;
		showMain();
		showMutants();
	}

	/*****************************************************************************/

	public void mnuBackClick() {
		stopRendering();
		if (seed > initseed)
			seed--;
		randomSet();
		showMutants();
	}

	/*****************************************************************************/
	// ThreadTarget implementations

	public void message(int index) {
	}

	public void progress(double value) {
	}

	public void output(String msg) {
	}

	/*****************************************************************************/
	/*****************************************************************************/

	class Updater extends Thread {

		boolean cstop = false;

		@Override
		public void run() {
			cstop = false;
			updating = true;

			for (int i = 1; i <= 8; i++) {
				if (cstop)
					break;
				mutants[i].width = imagewidth;
				mutants[i].height = imageheight;
				mutants[i].spatial_filter_radius = Global.defFilterRadius;
				mutants[i].spatial_oversample = Global.defOversample;
				mutants[i].sample_density = previewdensity;
				mutants[i].brightness = brightness;
				mutants[i].gamma = gamma;
				mutants[i].vibrancy = vibrancy;

				if (resetlocation) {
					mutants[i].calcBoundBox();
					mutants[i].zoom = 0;
				} else {
					mutants[i].zoom = zoom;
					mutants[i].center[0] = center[0];
					mutants[i].center[1] = center[1];
				}

				if (cstop)
					break;
				renderer.setCP(mutants[i]);
				renderer.render();

				if (cstop)
					break;
				images[i] = renderer.getImage();

				repaint();
			}

			updating = false;
		}

		public void kill() {
			cstop = true;
		}

	}

	/*****************************************************************************/
	/*****************************************************************************/

} // End of class Mutate
