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
import java.io.File;

public class Options extends MyThinlet implements Constants {

	/*****************************************************************************/
	// CONSTANTS

	/*****************************************************************************/
	// FIELDS

	/*****************************************************************************/

	Options(String title, String xmlfile, int width, int height)
			throws Exception {
		super(title, xmlfile, width, height);

		launcher.setResizable(false);

		buildVariationList();

	}

	/*****************************************************************************/

	public boolean destroy() {
		hide();
		return false;
	}

	/*****************************************************************************/

	void buildVariationList() {

		int nv = XForm.getNrVariations();

		SortableVariation[] svar = new SortableVariation[nv];
		for (int i = 0; i < nv; i++)
			svar[i] = new SortableVariation(XForm.getVariation(i).getName(), i,
					XForm.getVariation(i).getGroup());

		QuickSort.qsort(svar);

		Object panel = find("VariationPanel");

		Color color = new Color(0xDEDEDE);

		for (int i = 0; i < nv; i++) {
			Object checkbox = createImpl("checkbox");
			setString(checkbox, "name", "variation" + svar[i].index);
			setString(checkbox, "text", svar[i].name);
			setInteger(checkbox, "weightx", 1);
			if (i % 2 == 0)
				setColor(checkbox, "background", color);
			add(panel, checkbox);

			Object label = createImpl("label");
			setString(label, "text", "Group " + svar[i].group + "  ");
			setChoice(label, "alignment", "right");
			if (i % 2 == 0)
				setColor(label, "background", color);
			add(panel, label);
		}

		setString(find("varTotal"), "text", "Total : " + nv);

	} // End of method buildVariationList

	/*****************************************************************************/

	public void show() {
		super.show();

		setBoolean(find("chkConfirmDel"), "selected", Global.confirmDelete);
		setBoolean(find("chkConfirmExit"), "selected", Global.confirmExit);
		setBoolean(find("chkConfirmStopRender"), "selected",
				Global.confirmStopRender);

		setBoolean(find("chkOldPaletteFormat"), "selected",
				Global.oldPaletteFormat);

		setBoolean(find("chkShowRenderStats"), "selected",
				Global.showRenderStats);

		setBoolean(find("chkPlaysound"), "selected",
				Global.playSoundOnRenderComplete);
		setString(find("txtSoundFile"), "text", Global.renderCompleteSoundFile);

		setString(find("txtRenderer"), "text", Global.flam3Path);

		setString(find("udMinXForms"), "text", "" + Global.randMinTransforms);
		setString(find("udMaxXForms"), "text", "" + Global.randMaxTransforms);

		setString(find("udMinMutate"), "text", "" + Global.mutantMinTransforms);
		setString(find("udMaxMutate"), "text", "" + Global.mutantMaxTransforms);

		setInteger(find("cmbSymType"), "selected", Global.symmetryType);
		setString(find("udSymOrder"), "text", "" + Global.symmetryOrder);
		setString(find("udSymNVars"), "text", "" + Global.symmetryNVars);
		setBoolean(find("udSymOrder"), "enabled", Global.symmetryType > 1);
		setBoolean(find("udSymNVars"), "enabled", Global.symmetryType > 1);

		setString(find("udBatchSize"), "text", "" + Global.batchSize);

		setString(find("txtRandomPrefix"), "text", Global.randomPrefix);

		setString(find("txtSampleDensity"), "text", ""
				+ Global.defSampleDensity);
		setString(find("txtGamma"), "text", "" + Global.defGamma);
		setString(find("txtBrightness"), "text", "" + Global.defBrightness);
		setString(find("txtVibrancy"), "text", "" + Global.defVibrancy);
		setString(find("txtOversample"), "text", "" + Global.defOversample);
		setString(find("txtFilterRadius"), "text", "" + Global.defFilterRadius);

		setString(find("txtLowQuality"), "text", "" + Global.prevLowQuality);
		setString(find("txtMediumQuality"), "text", ""
				+ Global.prevMediumQuality);
		setString(find("txtHighQuality"), "text", "" + Global.prevHighQuality);

		// public static int gridColor1;
		// public static int gridColor2;

		setColor(find("btnBackgroundColor"), "background", new Color(
				Global.editorBkgColor));
		setColor(find("btnReferenceColor"), "background", new Color(
				Global.referenceTriangleColor));
		setColor(find("btnHelperColor"), "background", new Color(
				Global.helpersColor));
		setColor(find("btnGridColor1"), "background", new Color(
				Global.gridColor1));
		setColor(find("btnGridColor2"), "background", new Color(
				Global.gridColor2));

		setBoolean(find("chkUseXFormColor"), "selected",
				Global.useTransformColors);
		setBoolean(find("chkHelpers"), "selected", Global.helpersEnabled);
		setBoolean(find("chkShowAllXforms"), "selected", Global.showAllXforms);

		setBoolean(find("referencemode" + Global.referenceMode), "selected",
				true);

		setBoolean(find("chkExtendedEdit"), "selected", Global.extEditEnabled);
		setBoolean(find("chkAxisLock"), "selected", Global.transformAxisLock);

		if (Global.doubleClickSetVars)
			setBoolean(find("doubleclick1"), "selected", true);
		else
			setBoolean(find("doubleclick0"), "selected", true);

		if (Global.rotationMode == 1)
			setBoolean(find("rotationmode1"), "selected", true);
		else
			setBoolean(find("rotationmode0"), "selected", true);

		if (Global.preserveQuality)
			setBoolean(find("zoomingmode0"), "selected", true);
		else
			setBoolean(find("zoomingmode1"), "selected", true);

		if (Global.nrThreads == 0) {
			setInteger(find("cbNrThreads"), "selected", 0);
			setString(find("cbNrThreads"), "text", "Off");
		} else
			setString(find("cbNrThreads"), "text", "" + Global.nrThreads);

		setString(find("txtJPEGQuality"), "text", "" + Global.jpegQuality);

		setString(find("txtCommentPassword"), "text", Global.passwordText);
		setInteger(find("cmbWatermark"), "selected", Global.watermarkPosition);
		setString(find("txtWatermark"), "text", Global.watermarkFile);

		setString(find("udMinNodes"), "text", "" + Global.minNodes);
		setString(find("udMaxNodes"), "text", "" + Global.maxNodes);
		setString(find("udMinHue"), "text", "" + Global.minHue);
		setString(find("udMaxHue"), "text", "" + Global.maxHue);
		setString(find("udMinSat"), "text", "" + Global.minSat);
		setString(find("udMaxSat"), "text", "" + Global.maxSat);
		setString(find("udMinLum"), "text", "" + Global.minLum);
		setString(find("udMaxLum"), "text", "" + Global.maxLum);

		setBoolean(find("grpGradient0"), "selected", true);
		for (int i = 1; i < 5; i++)
			if (Global.randGradient == i) {
				setBoolean(find("grpGradient" + Global.randGradient),
						"selected", true);
				setBoolean(find("grpGradient0"), "selected", false);
			}

		setString(find("txtNumTries"), "text", "" + Global.numTries);
		setString(find("txtTryLength"), "text", "" + Global.tryLength);

		setString(find("txtLibrary"), "text", Global.defLibrary);
		setString(find("txtParameter"), "text", Global.defFlameFile);
		setString(find("txtSmooth"), "text", Global.defSmoothPaletteFile);
		setString(find("txtGradients"), "text", Global.randGradientFile);

		int nv = XForm.getNrVariations();
		for (int i = 0; i < nv; i++)
			setBoolean(find("variation" + i), "selected", Global.variations[i]);

		updateEnabledCount();

		setString(find("txtURL"), "text", Global.sheepURL);
		setString(find("txtNick"), "text", Global.sheepNick);
		setString(find("txtPassword"), "text", Global.sheepPW);
		setString(find("txtAddress"), "text", Global.sheepServer);

	} // End of method show

	/*****************************************************************************/

	void updateEnabledCount() {
		int nv = XForm.getNrVariations();
		int ne = 0;
		for (int i = 0; i < nv; i++)
			if (getBoolean(find("variation" + i), "selected"))
				ne++;

		setString(find("varEnabled"), "text", "Enabled : " + ne);

	}

	/*****************************************************************************/

	public void setTab(int index) {
		setInteger(find("Tabs"), "selected", index);
	}

	/*****************************************************************************/

	public void btnLibraryClick() {

		Task task = new FilenameTask(find("txtLibrary"));
		Global.opendialog = new OpenDialog(this, Global.browserPath, task);
		Global.opendialog.addFilter("Script files (*.asc)", "*.asc");
		Global.opendialog.addFilter("Javascript files (*.ajs)", "*.ajs");
		Global.opendialog.show();

	}

	/*****************************************************************************/

	public void btnRendererClick() {

		Task task = new FilenameTask(find("txtRenderer"));
		Global.opendialog = new OpenDialog(this, Global.browserPath, task);
		Global.opendialog
				.addFilter("Renderer (flam3-render*)", "flam3-render*");
		Global.opendialog.show();

	} // End of method btnRendererClick

	/*****************************************************************************/

	public void btnSmoothClick() {

		Task task = new FilenameTask(find("txtSmooth"));
		Global.opendialog = new OpenDialog(this, Global.browserPath, task);
		Global.opendialog.addFilter("Gradient files (*.ugr)", "*.ugr");
		Global.opendialog.show();

	}

	/*****************************************************************************/

	public void btnParameterClick() {

		Task task = new FilenameTask(find("txtParameter"));
		Global.opendialog = new OpenDialog(this, Global.browserPath, task);
		Global.opendialog.addFilter("Flame files (*.flame)", "*.flame");
		Global.opendialog.addFilter("Apophysis 1.0 files (*.apo,*.fla)",
				"*.apo;*.fla");
		Global.opendialog.show();

	}

	/*****************************************************************************/

	public void btnGradientsClick() {

		Task task = new FilenameTask(find("txtGradients"));
		Global.opendialog = new OpenDialog(this, Global.browserPath, task);
		Global.opendialog.addFilter("Gradient files (*.ugr)", "*.ugr");
		Global.opendialog.show();

	}

	/*****************************************************************************/

	void setFilename(Object field) {
		Global.browserPath = Global.opendialog.getBrowserPath();
		setString(field, "text", Global.opendialog.filename);
	}

	/*****************************************************************************/

	public void btnOKClick() {

		Global.confirmDelete = getBoolean(find("chkConfirmDel"), "selected");
		Global.confirmExit = getBoolean(find("chkConfirmExit"), "selected");
		Global.confirmStopRender = getBoolean(find("chkConfirmStopRender"),
				"selected");
		Global.oldPaletteFormat = getBoolean(find("chkOldPaletteFormat"),
				"selected");

		Global.showRenderStats = getBoolean(find("chkShowRenderStats"),
				"selected");
		Global.playSoundOnRenderComplete = getBoolean(find("chkPlaysound"),
				"selected");
		Global.renderCompleteSoundFile = getString(find("txtSoundFile"), "text");

		Global.flam3Path = getString(find("txtRenderer"), "text");

		Global.randMinTransforms = Integer.parseInt(getString(
				find("udMinXForms"), "text"));
		Global.randMaxTransforms = Integer.parseInt(getString(
				find("udMaxXForms"), "text"));

		Global.mutantMinTransforms = Integer.parseInt(getString(
				find("udMinMutate"), "text"));
		Global.mutantMaxTransforms = Integer.parseInt(getString(
				find("udMaxMutate"), "text"));

		Global.symmetryType = getInteger(find("cmbSymType"), "selected");
		Global.symmetryOrder = Integer.parseInt(getString(find("udSymOrder"),
				"text"));
		Global.symmetryNVars = Integer.parseInt(getString(find("udSymNVars"),
				"text"));

		Global.batchSize = Integer.parseInt(getString(find("udBatchSize"),
				"text"));

		Global.randomPrefix = getString(find("txtRandomPrefix"), "text");

		Global.defSampleDensity = Double.valueOf(
				getString(find("txtSampleDensity"), "text")).doubleValue();
		Global.defGamma = Double.valueOf(getString(find("txtGamma"), "text"))
				.doubleValue();
		Global.defBrightness = Double.valueOf(
				getString(find("txtBrightness"), "text")).doubleValue();
		Global.defVibrancy = Double.valueOf(
				getString(find("txtVibrancy"), "text")).doubleValue();
		Global.defFilterRadius = Double.valueOf(
				getString(find("txtFilterRadius"), "text")).doubleValue();

		Global.defOversample = Integer.parseInt(getString(
				find("txtOversample"), "text"));

		Global.prevLowQuality = Double.valueOf(
				getString(find("txtLowQuality"), "text")).doubleValue();
		Global.prevMediumQuality = Double.valueOf(
				getString(find("txtMediumQuality"), "text")).doubleValue();
		Global.prevHighQuality = Double.valueOf(
				getString(find("txtHighQuality"), "text")).doubleValue();

		Global.editorBkgColor = getColor(find("btnBackgroundColor"),
				"background").getRGB();
		Global.referenceTriangleColor = getColor(find("btnReferenceColor"),
				"background").getRGB();
		Global.helpersColor = getColor(find("btnHelperColor"), "background")
				.getRGB();
		Global.gridColor1 = getColor(find("btnGridColor1"), "background")
				.getRGB();
		Global.gridColor2 = getColor(find("btnGridColor2"), "background")
				.getRGB();

		Global.useTransformColors = getBoolean(find("chkUseXFormColor"),
				"selected");
		Global.helpersEnabled = getBoolean(find("chkHelpers"), "selected");
		Global.showAllXforms = getBoolean(find("chkShowAllXforms"), "selected");

		for (int i = 0; i < 3; i++)
			if (getBoolean(find("referencemode" + i), "selected"))
				Global.referenceMode = i;

		Global.extEditEnabled = getBoolean(find("chkExtendedEdit"), "selected");
		Global.transformAxisLock = getBoolean(find("chkAxisLock"), "selected");

		if (getInteger(find("cbNrThreads"), "selected") == 0)
			Global.nrThreads = 0;
		else
			Global.nrThreads = Integer.parseInt(getString(find("cbNrThreads"),
					"text"));

		Global.doubleClickSetVars = getBoolean(find("doubleclick1"), "selected");

		Global.jpegQuality = Integer.parseInt(getString(find("txtJPEGQuality"),
				"text"));

		Global.passwordText = getString(find("txtCommentPassword"), "text");
		Global.watermarkPosition = getSelectedIndex(find("cmbWatermark"));
		Global.watermarkFile = getString(find("txtWatermark"), "text");

		if (getBoolean(find("rotationmode0"), "selected"))
			Global.rotationMode = 0;
		else
			Global.rotationMode = 1;

		Global.preserveQuality = getBoolean(find("zoomingmode0"), "selected");

		Global.minNodes = Integer
				.parseInt(getString(find("udMinNodes"), "text"));
		Global.maxNodes = Integer
				.parseInt(getString(find("udMaxNodes"), "text"));

		Global.minHue = Integer.parseInt(getString(find("udMinHue"), "text"));
		Global.maxHue = Integer.parseInt(getString(find("udMaxHue"), "text"));

		Global.minSat = Integer.parseInt(getString(find("udMinSat"), "text"));
		Global.maxSat = Integer.parseInt(getString(find("udMaxSat"), "text"));

		Global.minLum = Integer.parseInt(getString(find("udMinLum"), "text"));
		Global.maxLum = Integer.parseInt(getString(find("udMaxLum"), "text"));

		Global.randGradient = 0;
		for (int i = 1; i < 5; i++)
			if (getBoolean(find("grpGradient" + i), "selected"))
				Global.randGradient = i;

		int nv = XForm.getNrVariations();
		for (int i = 0; i < nv; i++)
			Global.variations[i] = getBoolean(find("variation" + i), "selected");

		Global.defLibrary = getString(find("txtLibrary"), "text");
		Global.defFlameFile = getString(find("txtParameter"), "text");
		Global.defSmoothPaletteFile = getString(find("txtSmooth"), "text");
		Global.randGradientFile = getString(find("txtGradients"), "text");

		Global.numTries = Integer.parseInt(getString(find("txtNumTries"),
				"text"));
		Global.tryLength = Integer.parseInt(getString(find("txtTryLength"),
				"text"));

		Global.sheepURL = getString(find("txtURL"), "text");
		Global.sheepNick = getString(find("txtNick"), "text");
		Global.sheepPW = getString(find("txtPassword"), "text");
		Global.sheepServer = getString(find("txtAddress"), "text");

		Global.writeSettings();

		hide();

		Global.main.updateWindows();

	}

	/*****************************************************************************/

	public void btnCancelClick() {
		hide();
	}

	/*****************************************************************************/

	public void btnBrowseSoundClick() {

		Task task = new FilenameTask(find("txtSoundFile"));
		Global.opendialog = new OpenDialog(this, Global.browserPath, task);
		Global.opendialog.addFilter("Audio interchange files (*.aif,*.aiff)",
				"*.aif;*.aiff");
		Global.opendialog.addFilter("Sound files (*.snd)", "*.snd");
		Global.opendialog.addFilter("Waveform files (*.wav)", "*.wav");
		Global.opendialog.show();
	}

	/*****************************************************************************/

	public void btnBrowseWatermarkClick() {

		Task task = new FilenameTask(find("txtWatermark"));
		Global.opendialog = new OpenDialog(this, Global.browserPath, task);
		Global.opendialog
				.addFilter("JPEG files (*.jpg,*.jpeg)", "*.jpg;*.jpeg");
		Global.opendialog.addFilter("PNG files (*.png)", "*.png");
		Global.opendialog.addFilter("GIF files (*.gif)", "*.gif");
		Global.opendialog.show();
	}

	/*****************************************************************************/

	public void btnPlayClick() {
		String filename = getString(find("txtSoundFile"), "text");

		if (filename.length() == 0)
			beep();
		else {
			File file = new File(filename);
			System.out.println("file exists");
			if (file.exists()) {
				Sound sound = new Sound(file);
				sound.play();
				System.out.println("after play");
			}
		}

	} // End of method btnPlayClick

	/*****************************************************************************/

	public void udMinChange(Object minbox, Object maxbox) {
		int min = Integer.parseInt(getString(minbox, "text"));
		int max = Integer.parseInt(getString(maxbox, "text"));
		if (max < min)
			setString(maxbox, "text", "" + min);
	}

	public void udMaxChange(Object minbox, Object maxbox) {
		int min = Integer.parseInt(getString(minbox, "text"));
		int max = Integer.parseInt(getString(maxbox, "text"));
		if (max < min)
			setString(minbox, "text", "" + max);
	}

	/*****************************************************************************/

	public void minMutateChange() {
		int min = Integer.parseInt(getString(find("udMinMutate"), "text"));
		int max = Integer.parseInt(getString(find("udMaxMutate"), "text"));
		if (max < min)
			setString(find("udMaxMutate"), "text", "" + min);
	}

	public void maxMutateChange() {
		int min = Integer.parseInt(getString(find("udMinMutate"), "text"));
		int max = Integer.parseInt(getString(find("udMaxMutate"), "text"));
		if (max < min)
			setString(find("udMinMutate"), "text", "" + max);
	}

	/*****************************************************************************/

	public void btnSetAllClick() {
		int nv = XForm.getNrVariations();
		for (int i = 0; i < nv; i++)
			setBoolean(find("variation" + i), "selected", true);

		updateEnabledCount();

		repaint();
	}

	/*****************************************************************************/

	public void btnClearAllClick() {
		int nv = XForm.getNrVariations();
		for (int i = 0; i < nv; i++)
			setBoolean(find("variation" + i), "selected", false);

		updateEnabledCount();

		repaint();
	}

	/*****************************************************************************/

	public void btnSheepClick() {
		int n = 0;

		int nv = XForm.getNrVariations();
		for (int i = 0; i < nv; i++) {
			boolean b = XForm.getVariation(i).isSheepCompatible();
			if (b)
				n++;
			setBoolean(find("variation" + i), "selected", b);
		}

		updateEnabledCount();

		repaint();
	}

	/*****************************************************************************/

	public void cmbSymTypeChange() {
		Global.symmetryType = getInteger(find("cmbSymType"), "selected");
		setBoolean(find("udSymOrder"), "enabled", Global.symmetryType > 1);
		setBoolean(find("udSymNVars"), "enabled", Global.symmetryType > 1);
	} // End of method cmbSymTypeChange

	/*****************************************************************************/

	public void changeColor(Object button) {
		Color color = getColor(button, "background");

		Global.colordialog = new ColorDialog(this, color);
		Global.colordialog.setTask(new ColorTask(button));
		Global.colordialog.show();

	}

	void changeColor2(Object button) {
		setColor(button, "background", Global.colordialog.getColor());

	}

	/*****************************************************************************/
	/*****************************************************************************/

	class ColorTask implements Task {

		Object button = null;

		ColorTask(Object button) {
			this.button = button;
		}

		public void execute() {
			changeColor2(button);
		}

	} // End of class ColorTask

	/*****************************************************************************/

	class FilenameTask implements Task {

		Object field = null;

		FilenameTask(Object field) {
			this.field = field;
		}

		public void execute() {
			setFilename(field);
		}

	} // End of class FilenameTask

	/*****************************************************************************/
	/*****************************************************************************/

	class SortableVariation implements MySortable {

		String name;
		int index;
		int group;

		SortableVariation(String name, int index, int group) {
			this.name = name;
			this.index = index;
			this.group = group;
		}

		public long compare(MySortable s) {
			SortableVariation sv = (SortableVariation) s;
			return name.compareTo(sv.name);
		}

	}

	/*****************************************************************************/

} // End of class Options

