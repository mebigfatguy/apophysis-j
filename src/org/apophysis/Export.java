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

import java.awt.Rectangle;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Export extends MyThinlet implements Constants {

	/*****************************************************************************/
	// CONSTANTS

	/*****************************************************************************/
	// FIELDS

	public String filename;

	private int imageWidth;
	private int imageHeight;
	private int oversample;
	private int batches;
	private int strips;

	private double sample_density;
	private double filter_radius;
	private double estimator;
	private double estimatorMin;
	private double estimatorCurve;
	private int jitters;

	private double gammaThreshold;

	private int depth;

	/*****************************************************************************/

	Export(String title, String xmlfile, int width, int height)
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
		super.show();

		// default values

		imageWidth = Global.exportWidth;
		imageHeight = Global.exportHeight;
		sample_density = Global.exportDensity;
		filter_radius = Global.exportFilter;
		oversample = Global.exportOversample;

		setString(find("txtFilename"), "text", filename);
		setString(find("cbWidth"), "text", "" + imageWidth);
		setString(find("cbHeight"), "text", "" + imageHeight);
		setString(find("txtDensity"), "text", "" + sample_density);
		setString(find("txtFilterRadius"), "text", "" + filter_radius);
		setString(find("udOversample"), "text", "" + oversample);

		batches = 1;
		estimator = 9.0;
		estimatorMin = 0.0;
		estimatorCurve = 0.4;
		jitters = 1;
		gammaThreshold = 0.01;

		setString(find("txtEstimator"), "text", "" + estimator);
		setString(find("txtEstimatorMin"), "text", "" + estimatorMin);
		setString(find("txtEstimatorCurve"), "text", "" + estimatorCurve);
		setString(find("txtGammaThreshold"), "text", "" + gammaThreshold);

	}

	/*****************************************************************************/

	public void btnBrowseClick() {

		filename = getString(find("txtFilename"), "text");
		File file = new File(filename);
		String dirname = file.getParent();

		if (dirname == null) {
			dirname = Global.browserPath;
		} else {
			filename = file.getName();
		}

		Task task = new FilenameTask();
		Global.savedialog = new SaveDialog(this, dirname, filename, task);
		Global.savedialog.show();

	} // End of method btnBrowseClick

	/*****************************************************************************/

	void setFilename() {
		setString(find("txtFilename"), "text", Global.savedialog.filename);
	}

	/*****************************************************************************/

	public void btnCancelClick() {
		hide();
	}

	/*****************************************************************************/

	public void btnOKClick() {

		try {
			filename = getString(find("txtFilename"), "text");
			if (filename.length() == 0) {
				beep();
				return;
			}

			imageWidth = Integer.parseInt(getString(find("cbWidth"), "text"));
			imageHeight = Integer.parseInt(getString(find("cbHeight"), "text"));
			sample_density = Double.parseDouble(
					getString(find("txtDensity"), "text"));
			filter_radius = Double.parseDouble(
					getString(find("txtFilterRadius"), "text"));
			oversample = Integer.parseInt(getString(find("udOversample"),
					"text"));

			depth = getSelectedIndex(find("cmbDepth"));
			System.out.println("depth=" + depth);

			strips = Integer.parseInt(getString(find("udStrips"), "text"));

			hide();
			exportFlame();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	} // End of method btnOKClick

	/*****************************************************************************/

	void exportFlame() {

		String ext = "";

		int k = Global.export.filename.lastIndexOf('.');
		if (k >= 0) {
			ext = Global.export.filename.substring(k + 1).toLowerCase();
		}

		if (ext.equals("ppm")) {
			Global.exportFileFormat = 2;
		} else if (ext.equals("png")) {
			Global.exportFileFormat = 3;
		} else {
			Global.exportFileFormat = 1;
		}
		switch (Global.exportFileFormat) {
		case 1:
			ext = "jpg";
			break;
		case 2:
			ext = "ppm";
			break;
		case 3:
			ext = "png";
			break;
		}

		int bits = 0;
		switch (depth) {
		case 0:
			bits = 16;
			break;
		case 1:
			bits = 32;
			break;
		case 2:
			bits = 33;
			break;
		case 3:
			bits = 64;
			break;
		}

		Global.exportWidth = imageWidth;
		Global.exportHeight = imageHeight;
		Global.exportDensity = sample_density;
		Global.exportFilter = filter_radius;
		Global.exportOversample = oversample;
		Global.exportBatches = batches;
		Global.exportEstimator = estimator;
		Global.exportEstimatorMin = estimatorMin;
		Global.exportEstimatorCurve = estimatorCurve;
		Global.exportJitters = jitters;

		ControlPoint cp1 = new ControlPoint();
		cp1.copy(Global.mainCP);

		cp1.sample_density = sample_density;
		cp1.spatial_oversample = oversample;
		cp1.nbatches = batches;

		if ((cp1.width != imageWidth) || (cp1.height != imageHeight)) {
			cp1.adjustScale(imageWidth, imageHeight);
		}

		cp1.estimator = estimator;
		cp1.estimator_min = estimatorMin;
		cp1.estimator_curve = estimatorCurve;
		cp1.gamma_threshold = gammaThreshold;

		Runtime runtime = Runtime.getRuntime();

		// create environment variables
		List<String> v = new ArrayList<String>();
		v.add("verbose=1");
		v.add("format=" + ext);
		v.add("bits=" + bits);
		v.add("nstrips=" + strips);
		v.add("transparency=0");
		v.add("out=" + filename);
		v.add("enable_jpeg_comments=" + Global.jpegComment);
		v.add("enable_png_comments=" + Global.jpegComment);

		String[] env = new String[v.size()];
		for (int i = 0; i < env.length; i++) {
			env[i] = v.get(i);
		}

		saveFlame(cp1, filename);

		try {
			Process process = runtime.exec(Global.flam3Path, env);

			// send the flame description as input to the process

			try (PrintWriter w = new PrintWriter(new OutputStreamWriter(
					process.getOutputStream()))) {
    			cp1.save(w);
			}

			String title = (new File(filename)).getName();
			int j = title.indexOf('.');
			if (j > 0) {
				title = title.substring(0, j);
			}
			Rectangle bounds = launcher.getBounds();
			new Terminal(title, process, bounds.x, bounds.y);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	} // End of method exportFlame

	/*****************************************************************************/

	void saveFlame(ControlPoint cp, String filename) {
		int i = filename.lastIndexOf('.');
		if (i > 0) {
			filename = filename.substring(0, i) + ".flame";
		} else {
			filename = filename + ".flame";
		}

		try (PrintWriter w = new PrintWriter(new FileWriter(filename))) {
			cp.save(w);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	} // End of method saveFlame

	/*****************************************************************************/
	/*****************************************************************************/

	class FilenameTask implements Task {

		@Override
		public void execute() {
			Global.browserPath = Global.savedialog.getBrowserPath();
			setFilename();
		}

	}

	/*****************************************************************************/
	/*****************************************************************************/

} // End of class Export
