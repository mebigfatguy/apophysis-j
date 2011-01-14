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

import java.awt.Toolkit;
import java.io.File;
import java.util.List;

public class Render extends MyThinlet implements Constants, ThreadTarget {

	/*****************************************************************************/
	// CONSTANTS

	/*****************************************************************************/
	// FIELDS

	RenderThread renderthread = null;

	boolean canceled = false;

	long starttime, endtime, oldelapsed, edt;
	double oldprog;
	long approxsamples;

	long physicalmemory, approxmemory, totalphysicalmemory;
	int colormap[][] = new int[256][3];
	ControlPoint cp;
	String filename;
	int imagewidth, imageheight, oversample;
	int bitspersample;
	double zoom, sample_density, brightness, gamma, vibrancy, filter_radius;
	double center[] = new double[2];
	int maxmemory;
	boolean renderall = false;

	double ratio;

	boolean paused = false;

	int index = -1; // current cp being renderer

	StringBuffer sb = new StringBuffer();
	int nlines = 0;

	List<Preset> presets = null;

	/*****************************************************************************/

	Render(String title, String xmlfile, int width, int height)
			throws Exception {
		super(title, xmlfile, width, height);

		launcher.setResizable(false);

		cp = new ControlPoint();
		bitspersample = 0;

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

		if (renderall) {
			launcher.setTitle("Render all flames");
		}

		presets = Global.readPresets();

		listPresets();

		System.gc();

		Runtime runtime = Runtime.getRuntime();

		long maxmem = runtime.maxMemory()
				- (runtime.totalMemory() - runtime.freeMemory());
		maxmem = maxmem / 1024 / 1024;
		setString(find("lblPhysical"), "text", maxmem + " MB");

		setString(find("txtFilename"), "text", filename);

		sample_density = Math.max(cp.sample_density, Global.renderDensity);
		setString(find("txtDensity"), "text", "" + sample_density);

		oversample = Math.max(cp.spatial_oversample, Global.renderOversample);
		setString(find("txtOversample"), "text", "" + oversample);

		filter_radius = Math.max(cp.spatial_filter_radius,
				Global.renderFilterRadius);
		setString(find("txtFilterRadius"), "text", "" + filter_radius);

		imagewidth = cp.width;
		imageheight = cp.height;
		setString(find("cbWidth"), "text", "" + cp.width);
		setString(find("cbHeight"), "text", "" + cp.height);

		setBoolean(find("chkWatermark"), "selected", Global.watermark == 1);
		setBoolean(find("chkComment"), "selected", Global.jpegComment == 1);
		setBoolean(find("chkPassword"), "selected",
				Global.encryptedComment == 1);
		updatePasswordOption();

		showMemory();

		bitspersample = Global.renderBitsPerSample;

		ratio = imagewidth * 1.0 / imageheight;

		super.show();

	}

	/*****************************************************************************/

	void showMemory() {
		try {
			imagewidth = Integer.parseInt(getString(find("cbWidth"), "text"));
			imageheight = Integer.parseInt(getString(find("cbHeight"), "text"));

			/*
			 * Runtime runtime = Runtime.getRuntime(); long maxmem =
			 * runtime.maxMemory()-(runtime.totalMemory()-runtime.freeMemory());
			 * maxmem = maxmem/1024/1024;
			 * setString(find("lblPhysical"),"text",maxmem+" MB");
			 */

			long need = imageheight * imagewidth * oversample * oversample * 16
					/ 1000 / 1000;
			setString(find("lblApproxMem"), "text", need + " MB");

			double z = sample_density * Math.pow(2, cp.zoom)
					* Math.pow(2, cp.zoom) * imagewidth * imageheight
					/ oversample / oversample;

			z = Math.log(z) / Math.log(2);
			long maxbits = (long) (8 + z);
			setString(find("lblMaxbits"), "text", "" + maxbits);
		} catch (Exception ex) {
		}
	}

	/*****************************************************************************/

	public void setTab(int index) {
		setInteger(find("PageCtrl"), "selected", index);
	}

	/*****************************************************************************/

	void resetControls() {

		setBoolean(find("txtFilename"), "enabled", true);
		setBoolean(find("btnBrowse"), "enabled", true);
		setBoolean(find("cbWidth"), "enabled", true);
		setBoolean(find("cbHeight"), "enabled", true);
		setBoolean(find("txtDensity"), "enabled", true);
		setBoolean(find("txtFilterRadius"), "enabled", true);
		setBoolean(find("txtOversample"), "enabled", true);
		setBoolean(find("btnRender"), "enabled", true);
		setBoolean(find("cmbPreset"), "enabled", true);
		setBoolean(find("btnSavePreset"), "enabled", true);
		setBoolean(find("btnDeletePreset"), "enabled", true);
		setBoolean(find("btnPause"), "enabled", false);
		setString(find("btnCancel"), "text", "  Close  ");
		setInteger(find("ProgressBar"), "value", 0);

		showMemoryStatus();

	} // End of method resetControl

	/*****************************************************************************/

	void showMemoryStatus() {
	} // End of method showMemoryStatus

	/*****************************************************************************/

	void listPresets() {
		Object combo = find("cmbPreset");
		removeAll(combo);

		int np = presets.size();
		for (int i = 0; i < np; i++) {
			Preset preset = presets.get(i);

			Object choice = createImpl("choice");
			setString(choice, "text", preset.name);

			add(combo, choice);
		}

		setInteger(combo, "selected", -1);
		setString(combo, "text", "");

	} // End of method listPresets

	/*****************************************************************************/

	public void btnRenderClick() {

		Global.watermark = getBoolean(find("chkWatermark"), "selected") ? 1 : 0;
		Global.jpegComment = getBoolean(find("chkComment"), "selected") ? 1 : 0;
		Global.encryptedComment = getBoolean(find("chkPassword"), "selected") ? 1
				: 0;

		String s = getString(find("txtFilename"), "text");
		File file = new File(s);
		if (file.getParent() != null) {
			Global.renderPath = file.getParent();
		}

		sb = new StringBuffer();

		paused = false;
		canceled = false;

		starttime = System.currentTimeMillis();

		if (renderall) {
			index = -1;
			loop();
		} else {
			renderOne();
		}
	}

	/*****************************************************************************/

	void loop() {

		index++;
		if ((index < Global.main.cps.size()) && !canceled) {
			cp.copy(Global.main.cps.get(index));
			renderOne();
		} else {
			if (Global.playSoundOnRenderComplete) {
				playSound();
			}
			resetControls();
		}

	} // End of method loop

	/*****************************************************************************/

	void renderOne() {
		String ext;

		imagewidth = Integer.parseInt(getString(find("cbWidth"), "text"));
		imageheight = Integer.parseInt(getString(find("cbHeight"), "text"));

		oversample = Integer.parseInt(getString(find("txtOversample"), "text"));
		filter_radius = Double.valueOf(
				getString(find("txtFilterRadius"), "text")).doubleValue();
		sample_density = Double.valueOf(getString(find("txtDensity"), "text"))
				.doubleValue();

		filename = getString(find("txtFilename"), "text");
		if (filename.length() == 0) {
			alert("File name not specified");
			return;
		}

		File file = new File(filename);
		File dir = new File(file.getParent());

		if (renderall) {
			int k = filename.lastIndexOf('.');
			ext = (k > 0) ? filename.substring(k) : ".jpg";
			file = new File(dir, cp.name + ext);
			filename = file.getAbsolutePath();
		}

		if (sample_density <= 0) {
			alert("Invalid sample density");
			return;
		}

		if (filter_radius <= 0) {
			alert("Invalid filter radius");
			return;
		}

		if (oversample < 1) {
			alert("Invalid oversample");
			return;
		}

		if (imagewidth < 1) {
			alert("Invalid image width");
			return;
		}

		if (imageheight < 1) {
			alert("Invalid image height");
			return;
		}

		setBoolean(find("txtFilename"), "enabled", false);
		setBoolean(find("btnBrowse"), "enabled", false);
		setBoolean(find("cbWidth"), "enabled", false);
		setBoolean(find("cbHeight"), "enabled", false);
		setBoolean(find("txtDensity"), "enabled", false);
		setBoolean(find("txtFilterRadius"), "enabled", false);
		setBoolean(find("txtOversample"), "enabled", false);
		setBoolean(find("btnSavePreset"), "enabled", false);
		setBoolean(find("btnDeletePreset"), "enabled", false);

		setBoolean(find("btnRender"), "enabled", false);
		setBoolean(find("btnPause"), "enabled", true);
		setString(find("btnCancel"), "text", " Cancel ");
		setBoolean(find("btnCancel"), "enabled", true);

		if (nlines > 1000) {
			sb.setLength(0);
		}

		zoom = cp.zoom;
		center[0] = cp.center[0];
		center[1] = cp.center[1];

		output("\n");

		output("--- Rendering ");
		output(filename);
		output("\n");

		output("  Size : ");
		output("" + imagewidth);
		output(" x ");
		output("" + imageheight);
		output("\n");

		output("  Quality: ");
		output("" + sample_density);
		output("\n");

		output("  Oversample : ");
		output("" + oversample);
		output(", Filter: ");
		output("" + filter_radius);
		output("\n");

		output("  Buffer depth: ");
		output("\n");

		cp.sample_density = sample_density;
		cp.spatial_oversample = oversample;
		cp.spatial_filter_radius = filter_radius;
		cp.adjustScale(imagewidth, imageheight);
		cp.transparency = false;

		oldprog = 0;
		oldelapsed = 0;
		edt = 0;

		double pzoom = Math.pow(2, cp.zoom);
		double appnum = sample_density * pzoom * pzoom;
		double appden = imageheight * 1.0 * imagewidth
				/ (oversample * oversample);

		approxsamples = (long) (appnum / appden);

		renderthread = new RenderThread(this);

		renderthread.setCP(cp);
		renderthread.start();

		setString(find("output"), "text", sb.toString());
		setTab(1);

	} // End of method renderOne

	/*****************************************************************************/

	public void btnCancelClick() {
		if (renderthread == null) {
			hide();
		} else {
			canceled = true;
			renderthread.terminate();
		}

	} // End of method btnCancelClick

	/*****************************************************************************/

	public void message(int msg) {
		System.out.println("render received message = " + msg);

		if (msg == WM_THREAD_COMPLETE) {
			endtime = System.currentTimeMillis();

			try {
				System.out.println("saving " + filename);
				boolean watermark = getBoolean(find("chkWatermark"), "selected");
				boolean encrypt = getBoolean(find("chkPassword"), "selected");
				boolean comment = getBoolean(find("chkComment"), "selected");
				renderthread.saveImage(filename, comment, encrypt, watermark);
			} catch (Exception ex) {
				ex.printStackTrace();
				output("Error saving image ! \n");
			}

			if (!renderall) {
				if (Global.playSoundOnRenderComplete) {
					playSound();
				}
			}

			if (Global.showRenderStats) {
				renderthread.showBigStats();
			} else {
				renderthread.showSmallStats();
			}

			renderthread = null;

			if (!renderall) {
				resetControls();
			}
		}

		else if (msg == WM_THREAD_TERMINATE) {
			renderthread = null;

			output("Aborted !\n");

			if (!renderall) {
				resetControls();
			}
		}

		if (renderall) {
			loop();
		}

	} // End of method message

	/*****************************************************************************/

	public void cmbPresetChange(Object combo) {
		int index = getSelectedIndex(combo);
		if (index < 0) {
			return;
		}

		Preset preset = presets.get(index);

		oversample = preset.oversample;
		setString(find("txtOversample"), "text", "" + oversample);

		filter_radius = preset.filter_radius;
		setString(find("txtFilterRadius"), "text", "" + filter_radius);

		imagewidth = preset.width;
		imageheight = preset.height;
		setString(find("cbWidth"), "text", "" + imagewidth);
		setString(find("cbHeight"), "text", "" + imageheight);

		sample_density = preset.density;
		setString(find("txtDensity"), "text", "" + sample_density);

	} // End of method cmbPresetChange

	/*****************************************************************************/

	public void btnBrowseClick() {

		File file = new File(filename);
		String path = file.getParent();
		if (path == null) {
			path = Global.browserPath;
		}

		Task task = new FilenameTask();
		Global.savedialog = new SaveDialog(this, path, file.getName(), task);

		Global.savedialog.show();

	} // End of method btnBrowseClick

	/*****************************************************************************/

	public void btnSavePresetClick() {
		Task task = new PresetTask();
		ask("Preset name :", "", task);
	}

	/*****************************************************************************/

	void savePreset() {
		if (_answer.equals("")) {
			return;
		}

		Preset preset = new Preset();

		preset.name = _answer;
		preset.width = Integer.parseInt(getString(find("cbWidth"), "text"));
		preset.height = Integer.parseInt(getString(find("cbHeight"), "text"));
		preset.density = Double.valueOf(getString(find("txtDensity"), "text"))
				.doubleValue();
		preset.filter_radius = Double.valueOf(
				getString(find("txtFilterRadius"), "text")).doubleValue();
		preset.oversample = Integer.parseInt(getString(find("txtOversample"),
				"text"));
		preset.format = ".jpg";
		preset.limitmem = false;
		preset.indexmem = -1;
		preset.memory = 64;

		deletePreset(_answer);
		presets.add(preset);
		listPresets();

		setString(find("cmbPreset"), "text", preset.name);

		Global.writePresets(presets);

	} // End of method savePreset

	/*****************************************************************************/

	public void btnDeletePresetClick() {
		Object combo = find("cmbPreset");
		String name = getString(combo, "text");
		deletePreset(name);
		Global.writePresets(presets);
	}

	/*****************************************************************************/

	void deletePreset(String name) {

		int np = presets.size();
		for (int i = 0; i < np; i++) {
			Preset preset = presets.get(i);
			if (preset.name.equals(name)) {
				presets.remove(i);
				listPresets();
				return;
			}
		}

	} // End of method deletePreset

	/*****************************************************************************/

	public void widthChanged(Object combo, int option) {
		int index = getSelectedIndex(combo);
		if ((index < 0) && (option == 0)) {
			return;
		}

		try {
			int w = Integer.parseInt(getString(combo, "text"));
			if (getBoolean(find("chkMaintain"), "selected")) {
				int h = w * cp.height / cp.width;
				setString(find("cbHeight"), "text", "" + h);
			}
			setBoolean(find("btnRender"), "enabled", true);
			showMemory();
		} catch (Exception ex) {
			setBoolean(find("btnRender"), "enabled", false);
		}
	}

	/*****************************************************************************/

	public void heightChanged(Object combo, int option) {
		int index = getSelectedIndex(combo);
		System.out.println("index=" + index + " option=" + option);
		if ((index < 0) && (option == 0)) {
			return;
		}

		try {
			int h = Integer.parseInt(getString(combo, "text"));
			if (getBoolean(find("chkMaintain"), "selected")) {
				int w = h * cp.width / cp.height;
				setString(find("cbWidth"), "text", "" + w);
			}
			setBoolean(find("btnRender"), "enabled", true);
			showMemory();
		} catch (Exception ex) {
			setBoolean(find("btnRender"), "enabled", false);
		}
	}

	/*****************************************************************************/

	public void paramChanged() {
		String s;

		try {
			s = getString(find("txtDensity"), "text");
			sample_density = Double.valueOf(s).doubleValue();

			s = getString(find("txtFilterRadius"), "text");
			filter_radius = Double.valueOf(s).doubleValue();

			s = getString(find("txtOversample"), "text");
			oversample = Integer.parseInt(s);

			showMemory();
		} catch (Exception ex) {
		}

	}

	/*****************************************************************************/

	public void btnPauseClick(Object button) {
		if (renderthread == null) {
			return;
		}

		if (paused) {
			renderthread.unpause();
			paused = false;
			setString(button, "text", " Pause ");
		} else {
			renderthread.pause();
			paused = true;
			setString(button, "text", " Restart ");
		}

	}

	/*****************************************************************************/

	public void btnHelpClick() {
		Global.helper.show();
		Global.helper.setTopicByName("memory");
	}

	/*****************************************************************************/

	public void chkCommentClick(Object button) {
		updatePasswordOption();
	}

	/*****************************************************************************/

	void updatePasswordOption() {
		setBoolean(find("chkPassword"), "enabled",
				getBoolean(find("chkComment"), "selected")
						&& (Global.passwordText.length() > 0));
	}

	/*****************************************************************************/

	void setFilename() {
		filename = Global.savedialog.filename;
		setString(find("txtFilename"), "text", filename);
	}

	/*****************************************************************************/

	void playSound() {
		File file = new File(Global.renderCompleteSoundFile);
		if (file.exists()) {
			Sound sound = new Sound(file);
			sound.play();
		} else {
			Toolkit.getDefaultToolkit().beep();
		}
	}

	/*****************************************************************************/

	public void progress(double value) {

		if (renderall) {
			value = (index + value) / Global.main.cps.size();
		}

		int i = (int) (100 * value);
		if (i < 0) {
			i = 0;
		} else if (i > 100) {
			i = 100;
		}

		setInteger(find("ProgressBar"), "value", i);

		if (value > 0.05) {
			long elapsed = System.currentTimeMillis() - starttime;
			long remaining = (long) ((1 - value) * elapsed / value);
			String s = convertTime(remaining);
			setString(find("StatusBar"), "text", "Remaining :" + s);
		}

	}

	/*****************************************************************************/

	public void output(String msg) {
		sb.append(msg);

		Object output = find("output");
		setString(output, "text", sb.toString());
		setInteger(output, "start", sb.length());
		setInteger(output, "end", sb.length());
		repaint();
	}

	/*****************************************************************************/

	String convertTime(long millis) {
		long hr = millis / 3600000L;
		millis -= hr * 3600000L;

		long mn = millis / 60000L;
		millis -= mn * 60000L;

		long sc = millis / 1000L;

		return ((hr < 10) ? ("0" + hr) : ("" + hr)) + ":"
				+ ((mn < 10) ? ("0" + mn) : ("" + mn)) + ":"
				+ ((sc < 10) ? ("0" + sc) : ("" + sc));

	}

	/*****************************************************************************/
	/*****************************************************************************/

	class PresetTask implements Task {

		public void execute() {
			savePreset();
		}

	}

	/*****************************************************************************/
	/*****************************************************************************/

	class FilenameTask implements Task {

		public void execute() {
			setFilename();
		}
	}

	/*****************************************************************************/
	/*****************************************************************************/

} // End of class Render
