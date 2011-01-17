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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ControlPoint implements Constants {

	/*****************************************************************************/
	// STATIC FIELDS

	static int[] var_distrib = null;
	static int[] mixed_var_distrib = null;

	static int[] sym_distrib = { -4, -3, -2, -2, -2, -1, -1, -1, 2, 2, 2, 3, 3,
			4, 4 };

	/*****************************************************************************/
	// FIELDS

	public boolean finalXformEnabled;
	public boolean hasFinalXform;

	public boolean transparency;

	public XForm[] xform = new XForm[NXFORMS + 1];
	public int nxforms = 0; // number of xforms, not counting the final one

	public int variation;
	public int[][] cmap = new int[256][3];

	public int cmapindex = -1;
	public double time;
	public double brightness;
	public double contrast;
	public double gamma;

	public double vibrancy = 1.0;
	public double hue_rotation;

	public double zoom;

	public int width;
	public int height;
	public int spatial_oversample;

	public String name, nick, url;

	public double[] center = new double[2];

	public int[] background = new int[3];

	public double pixels_per_unit;
	public double spatial_filter_radius;

	public double sample_density;
	public double actual_density;

	public int nbatches;
	public int white_level;
	public int cmap_inter;

	public int symmetry;

	public double[][] pulse = new double[2][2];
	public double[][] wiggle = new double[2][2];

	public double estimator;
	public double estimator_min;
	public double estimator_curve;

	public double gamma_threshold;

	public double fangle;

	public List<String> unknown; // name of unknown variations

	private final XForm[] propTable = new XForm[PROP_TABLE_SIZE];

	/*****************************************************************************/
	// CONSTRUCTORS

	ControlPoint() {
		for (int i = 0; i <= NXFORMS; i++) {
			xform[i] = new XForm();
		}

		pulse[0][0] = 0;
		pulse[0][1] = 60;
		pulse[1][0] = 0;
		pulse[1][1] = 60;

		wiggle[0][0] = 0;
		wiggle[0][1] = 60;
		wiggle[1][0] = 0;
		wiggle[1][1] = 60;

		background[0] = 0;
		background[1] = 0;
		background[2] = 0;

		center[0] = 0;
		center[1] = 0;

		pixels_per_unit = 50;

		width = 100;
		height = 100;

		spatial_oversample = 1;
		spatial_filter_radius = 0.5;

		fangle = 0;
		gamma = 1;
		vibrancy = 1;
		contrast = 1;
		brightness = 1;

		sample_density = 50;
		zoom = 0;
		nbatches = 1;

		white_level = 200;

		estimator = 9.0;
		estimator_min = 0.0;
		estimator_curve = 0.4;
		gamma_threshold = 0.01;

		finalXformEnabled = Global.enableFinalXform;

		transparency = false;

	}

	/*****************************************************************************/

	ControlPoint(XmlTag tag) {
		this();

		name = tag.getString("name", "untitled");
		time = tag.getDouble("time", 0.0);
		cmapindex = tag.getInt("gradient", -1);
		hue_rotation = tag.getDouble("hue", -1.0);
		brightness = tag.getDouble("brightness", brightness);
		gamma = tag.getDouble("gamma", gamma);
		vibrancy = tag.getDouble("vibrancy", vibrancy);
		if (Global.limitVibrancy && (vibrancy > 1)) {
			vibrancy = 1.0;
		}
		zoom = tag.getDouble("zoom", zoom);
		pixels_per_unit = tag.getDouble("scale", pixels_per_unit);

		sample_density = tag.getDouble("quality", sample_density);

		spatial_oversample = tag.getInt("oversample", spatial_oversample);
		spatial_filter_radius = tag.getDouble("filter", spatial_filter_radius);

		fangle = tag.getDouble("rotate", 0.0) * (-Math.PI) / 180.0;
		fangle = tag.getDouble("angle", fangle);

		center = tag.getDoubles("center");
		if (center == null) {
			center = new double[] { 0, 0 };
		}

		estimator = tag.getDouble("estimator_radius", estimator);
		estimator_min = tag.getDouble("estimator_minimum", estimator_min);
		// plpl this variant also exists (oxydizer ?)
		estimator_min = tag.getDouble("estimator_min", estimator_min);

		estimator_curve = tag.getDouble("estimator_curve", estimator_curve);

		double[] size = tag.getDoubles("size");
		if (size != null) {
			width = (int) size[0];
			height = (int) size[1];
		}

		double[] back = tag.getDoubles("background");
		background = new int[3];
		if (back == null) {
			background[0] = 0;
			background[1] = 0;
			background[2] = 0;
		} else {
			background[0] = (int) (back[0] * 255);
			background[1] = (int) (back[1] * 255);
			background[2] = (int) (back[2] * 255);
		}

		nick = tag.getString("nick", "");
		url = tag.getString("url", "");

	}

	/*****************************************************************************/

	public ControlPoint(Map<String, String> h, List<int[]> v) {
		this();

		name = getHashString(h, "title", name);
		if (name.startsWith("\"")) {
			name = name.substring(1, name.length());
		}
		if (name.endsWith("\"")) {
			name = name.substring(0, name.length() - 1);
		}

		width = getHashInt(h, "width", width);
		height = getHashInt(h, "height", height);

		String s = getHashString(h, "center", "");
		int ii = s.indexOf('/');
		if (ii > 0) {
			center[0] = Double.parseDouble(s.substring(0, ii));
			center[1] = Double.parseDouble(s.substring(ii + 1));
		}

		spatial_filter_radius = getHashDouble(h, "p_spat_filt_rad",
				spatial_filter_radius);
		spatial_oversample = getHashInt(h, "p_overaample", spatial_oversample);
		contrast = getHashDouble(h, "p_contrast", contrast);
		brightness = getHashDouble(h, "p_brightness", brightness);
		gamma = getHashDouble(h, "p_gamma", gamma);
		white_level = getHashInt(h, "p_white_level", white_level);

		int nv = XForm.getNrVariations();

		nxforms = getHashInt(h, "p_xforms", nxforms);
		for (int i = 0; i < nxforms; i++) {
			String prefix = "p_xf" + i + "_";
			xform[i].color = getHashDouble(h, prefix + "c", xform[i].color);
			xform[i].density = getHashDouble(h, prefix + "p", xform[i].density);
			xform[i].symmetry = getHashDouble(h, prefix + "sym",
					xform[i].symmetry);
			xform[i].c00 = getHashDouble(h, prefix + "cfa", xform[i].c00);
			xform[i].c10 = getHashDouble(h, prefix + "cfb", xform[i].c10);
			xform[i].c01 = getHashDouble(h, prefix + "cfc", xform[i].c01);
			xform[i].c11 = getHashDouble(h, prefix + "cfd", xform[i].c11);
			xform[i].c20 = getHashDouble(h, prefix + "cfe", xform[i].c20);
			xform[i].c21 = getHashDouble(h, prefix + "cff", xform[i].c21);

			int ip = 0;

			for (int j = 0; j < nv; j++) {
				String key = prefix + "var_" + XForm.getVariation(j).getName();
				xform[i].vars[j] = getHashDouble(h, key, 0);

				int np = XForm.getVariation(j).getNrParameters();
				for (int k = 0; k < np; k++) {
					key = prefix + "par_"
							+ XForm.getVariation(j).getParameterName(k);
					xform[i].pvalues[ip++] = getHashDouble(h, key, 0);
				}
			}

		}

		// convert gradient to colormap

		for (int i = 0; i < v.size(); i++) {
			int[] x = v.get(i);
			int ind = x[0];
			int col = x[1];
			ind = (int) (ind * 255 / 399 + 0.5);
			cmap[ind][0] = (col) & 0xFF;
			cmap[ind][1] = (col >> 8) & 0xFF;
			cmap[ind][2] = (col >> 16) & 0xFF;
		}

	}

	/*****************************************************************************/

	int getHashInt(Map<String, String> h, String key, int value) {
		String s = h.get(key);
		if (s == null) {
			return value;
		} else {
			return Integer.parseInt(s);
		}
	}

	double getHashDouble(Map<String, String> h, String key, double value) {
		String s = h.get(key);
		if (s == null) {
			return value;
		} else {
			return Double.parseDouble(s);
		}
	}

	String getHashString(Map<String, String> h, String key, String value) {
		String s = h.get(key);
		if (s == null) {
			return value;
		} else {
			return s;
		}
	}

	/*****************************************************************************/

	public void addXForm(XmlTag tag) {
		xform[nxforms++] = new XForm(tag);

	} // End of method addXForm

	/*****************************************************************************/

	public void addFinalXForm(XmlTag tag) {
		hasFinalXform = true;
		finalXformEnabled = true;
		xform[nxforms] = new XForm(tag);
	}

	/*****************************************************************************/

	public void addColor(XmlTag tag) {
		int index = tag.getInt("index", 0);
		double[] rgb = tag.getDoubles("rgb");

		if ((index >= 0) && (index < 256) && (rgb.length == 3)) {
			cmap[index][0] = (int) rgb[0];
			cmap[index][1] = (int) rgb[1];
			cmap[index][2] = (int) rgb[2];
		}

	}

	/*****************************************************************************/

	public void setPalette(int count, String data) {

		if (data.length() == count * 6) {
			for (int i = 0; i < count; i++) {
				cmap[i][0] = Integer.parseInt(data.substring(i * 6, i * 6 + 2),
						16);
				cmap[i][1] = Integer.parseInt(
						data.substring(i * 6 + 2, i * 6 + 4), 16);
				cmap[i][2] = Integer.parseInt(
						data.substring(i * 6 + 4, i * 6 + 6), 16);
			}
		} else if (data.length() == count * 8) {
			for (int i = 0; i < count; i++) {
				cmap[i][0] = Integer.parseInt(
						data.substring(i * 8 + 2, i * 8 + 4), 16);
				cmap[i][1] = Integer.parseInt(
						data.substring(i * 8 + 4, i * 8 + 6), 16);
				cmap[i][2] = Integer.parseInt(
						data.substring(i * 8 + 6, i * 8 + 8), 16);
			}
		}

	}

	/*****************************************************************************/

	public int trianglesFromCP(Triangle triangles[]) {
		int i, j;
		double temp_x, temp_y, xset, yset;
		double left, top, bottom, right;
		int n;

		n = nxforms;

		if (Global.referenceMode > 0) {
			top = 0;
			bottom = 0;
			right = 0;
			left = 0;

			for (i = 0; i < n; i++) {
				xset = 1.0;
				yset = 1.0;
				for (j = 0; j <= 5; j++) {
					temp_x = xset * xform[i].c00 + yset * xform[i].c10
							+ xform[i].c20;
					temp_y = xset * xform[i].c01 + yset * xform[i].c11
							+ xform[i].c21;
					xset = temp_x;
					yset = temp_y;
				}
				if (i == 0) {
					left = xset;
					right = xset;
					top = yset;
					bottom = yset;
				} else {
					if (xset < left) {
						left = xset;
					}
					if (xset > right) {
						right = xset;
					}
					if (yset > top) {
						top = yset;
					}
					if (yset < bottom) {
						bottom = yset;
					}
				}
			}

			if (Global.referenceMode == 1) {
				triangles[M1].set(right - left, 0, 0, 0, 0, -(top - bottom));
			} else {
				triangles[M1].set(right, -bottom, left, -bottom, left, -top);
			}
		} else {
			triangles[M1].set(1, 0, 0, 0, 0, -1);
		}

		for (j = 0; j <= n; j++) {
			for (i = 0; i <= 2; i++) {
				if (xform[j].postXswap) {
					triangles[j].x[i] = triangles[M1].x[i] * xform[j].p00
							+ triangles[M1].y[i] * xform[j].p10 + xform[j].p20;
					triangles[j].y[i] = triangles[M1].x[i] * xform[j].p01
							+ triangles[M1].y[i] * xform[j].p11 + xform[j].p21;
				} else {
					triangles[j].x[i] = triangles[M1].x[i] * xform[j].c00
							+ triangles[M1].y[i] * xform[j].c10 + xform[j].c20;
					triangles[j].y[i] = triangles[M1].x[i] * xform[j].c01
							+ triangles[M1].y[i] * xform[j].c11 + xform[j].c21;
				}
			}
		}

		for (j = n + 1; j < M1; j++) {
			triangles[j].copy(triangles[M1]);
		}

		Global.enableFinalXform = finalXformEnabled;

		for (i = 0; i <= 2; i++) {
			for (j = 0; j <= n; j++) {
				triangles[j].y[i] = -triangles[j].y[i];
			}

			triangles[M1].y[i] = -triangles[M1].y[i];
		}

		return n;

	} // End of method trianglesFromCP

	/*****************************************************************************/

	public void clone(ControlPoint cp1) {
		if (cp1.cmapindex >= 0) {
			cmapindex = cp1.cmapindex;
		}

		zoom = cp1.zoom;
		fangle = cp1.fangle;

		width = cp1.width;
		height = cp1.height;
		center[0] = cp1.center[0];
		center[1] = cp1.center[1];

		sample_density = cp1.sample_density;

		pixels_per_unit = cp1.pixels_per_unit;

		spatial_oversample = cp1.spatial_oversample;
		spatial_filter_radius = cp1.spatial_filter_radius;
		nbatches = cp1.nbatches;

		background[0] = cp1.background[0];
		background[1] = cp1.background[1];
		background[2] = cp1.background[2];

		white_level = cp1.white_level;
		gamma = cp1.gamma;
		vibrancy = cp1.vibrancy;
		brightness = cp1.brightness;

		nxforms = cp1.nxforms;
		for (int i = 0; i < nxforms; i++) {
			xform[i] = new XForm();
			xform[i].copy(cp1.xform[i]);
		}

		for (int i = nxforms; i < xform.length; i++) {
			xform[i].clear();
		}

		if (cp1.hasFinalXform) {
			hasFinalXform = true;
			xform[nxforms] = new XForm();
			xform[nxforms].copy(cp1.xform[cp1.nxforms]);
		} else {
			hasFinalXform = false;
		}

		cmapindex = cp1.cmapindex;
		for (int i = 0; i < 256; i++) {
			cmap[i][0] = cp1.cmap[i][0];
			cmap[i][1] = cp1.cmap[i][1];
			cmap[i][2] = cp1.cmap[i][2];
		}

		name = cp1.name;
		nick = cp1.nick;
		url = cp1.url;

		finalXformEnabled = cp1.finalXformEnabled;

	} // End of method clone

	/*****************************************************************************/

	public void copy(ControlPoint cp1) {
		copy(cp1, false);
	}

	public void copy(ControlPoint cp1, boolean keepSizes) {

		width = cp1.width;
		height = cp1.height;

		time = cp1.time;
		zoom = cp1.zoom;
		fangle = cp1.fangle;
		center[0] = cp1.center[0];
		center[1] = cp1.center[1];

		gamma = cp1.gamma;
		vibrancy = cp1.vibrancy;
		brightness = cp1.brightness;

		pixels_per_unit = cp1.pixels_per_unit;

		spatial_oversample = cp1.spatial_oversample;
		spatial_filter_radius = cp1.spatial_filter_radius;

		sample_density = cp1.sample_density;

		nbatches = cp1.nbatches;
		white_level = cp1.white_level;

		background[0] = cp1.background[0];
		background[1] = cp1.background[1];
		background[2] = cp1.background[2];

		name = cp1.name;
		nick = cp1.nick;
		url = cp1.url;

		if (keepSizes) {
			adjustScale(width, height);
		}

		nxforms = cp1.nxforms;
		for (int i = 0; i < nxforms; i++) {
			xform[i] = new XForm();
			xform[i].copy(cp1.xform[i]);
		}

		for (int i = nxforms; i < xform.length; i++) {
			xform[i].clear();
		}

		if (cp1.hasFinalXform) {
			hasFinalXform = true;
			xform[nxforms] = new XForm();
			xform[nxforms].copy(cp1.xform[nxforms]);
		} else {
			hasFinalXform = false;
		}

		cmapindex = cp1.cmapindex;
		for (int i = 0; i < 256; i++) {
			cmap[i][0] = cp1.cmap[i][0];
			cmap[i][1] = cp1.cmap[i][1];
			cmap[i][2] = cp1.cmap[i][2];
		}

		finalXformEnabled = cp1.finalXformEnabled;

	} // End of method copy

	/*****************************************************************************/

	void adjustScale(int w, int h) {
		pixels_per_unit = pixels_per_unit * w / width;
		width = w;
		height = h;
	} // End of method adjustScale

	/*****************************************************************************/

	public XForm prepare(XForm propTable[]) {
		double propsum, loopvalue, totvalue;
		int n;
		XForm finalxform = null;

		n = nxforms;

		boolean useFinalXform = finalXformEnabled && hasFinalXform;

		if (useFinalXform) {
			finalxform = xform[nxforms];
			xform[nxforms].prepare();
		}

		totvalue = 0;
		for (int i = 0; i < n; i++) {
			if (xform[i].enabled) {
				xform[i].prepare();
				totvalue += xform[i].density;
			}
		}

		loopvalue = 0;
		for (int i = 0; i < PROP_TABLE_SIZE; i++) {
			propsum = 0;
			int j = -1;
			while (true) {
				j++;
				if (xform[j].enabled) {
					propsum += xform[j].density;
				}
				if ((propsum > loopvalue) || (j == n - 1)) {
					break;
				}
			}

			propTable[i] = xform[j];
			loopvalue += totvalue / PROP_TABLE_SIZE;
		}

		return finalxform;

	} // End of method prepare

	/*****************************************************************************/

	public void getFromTriangles(Triangle triangles[], int t) {
		double[] abe = new double[3];

		for (int i = 0; i <= t; i++) {
			if (xform[i].postXswap) {
				solve3(triangles[M1].x[0], -triangles[M1].y[0],
						triangles[i].x[0], triangles[M1].x[1],
						-triangles[M1].y[1], triangles[i].x[1],
						triangles[M1].x[2], -triangles[M1].y[2],
						triangles[i].x[2], abe);
				xform[i].p00 = abe[0];
				xform[i].p10 = abe[1];
				xform[i].p20 = abe[2];

				solve3(triangles[M1].x[0], -triangles[M1].y[0],
						-triangles[i].y[0], triangles[M1].x[1],
						-triangles[M1].y[1], -triangles[i].y[1],
						triangles[M1].x[2], -triangles[M1].y[2],
						-triangles[i].y[2], abe);
				xform[i].p01 = abe[0];
				xform[i].p11 = abe[1];
				xform[i].p21 = abe[2];
			} else {
				solve3(triangles[M1].x[0], -triangles[M1].y[0],
						triangles[i].x[0], triangles[M1].x[1],
						-triangles[M1].y[1], triangles[i].x[1],
						triangles[M1].x[2], -triangles[M1].y[2],
						triangles[i].x[2], abe);
				xform[i].c00 = abe[0];
				xform[i].c10 = abe[1];
				xform[i].c20 = abe[2];

				solve3(triangles[M1].x[0], -triangles[M1].y[0],
						-triangles[i].y[0], triangles[M1].x[1],
						-triangles[M1].y[1], -triangles[i].y[1],
						triangles[M1].x[2], -triangles[M1].y[2],
						-triangles[i].y[2], abe);
				xform[i].c01 = abe[0];
				xform[i].c11 = abe[1];
				xform[i].c21 = abe[2];
			}
		}

		finalXformEnabled = Global.enableFinalXform;

	} // End of method getFromTriangles

	/*****************************************************************************/

	public double getppux() {
		return pixels_per_unit * Math.pow(2, zoom);
	}

	public double getppuy() {
		return pixels_per_unit * Math.pow(2, zoom);
	}

	/*****************************************************************************/

	double det(double a, double b, double c, double d) {
		return (a * d - b * c);
	}

	/*****************************************************************************/

	double round6(double x) {
		long l = (long) (x * 1000000);
		return l / 1000000.0;
	}

	/*****************************************************************************/

	double solve3(double x1, double x2, double x1h, double y1, double y2,
			double y1h, double z1, double z2, double z1h, double abe[]) {
		double det1;

		det1 = x1 * det(y2, 1.0, z2, 1.0) - x2 * det(y1, 1.0, z1, 1.0) + 1
				* det(y1, y2, z1, z2);

		if (det1 != 0.0) {
			abe[0] = (x1h * det(y2, 1.0, z2, 1.0) - x2
					* det(y1h, 1.0, z1h, 1.0) + 1 * det(y1h, y2, z1h, z2))
					/ det1;
			abe[1] = (x1 * det(y1h, 1.0, z1h, 1.0) - x1h
					* det(y1, 1.0, z1, 1.0) + 1 * det(y1, y1h, z1, z1h))
					/ det1;
			abe[2] = (x1 * det(y2, y1h, z2, z1h) - x2 * det(y1, y1h, z1, z1h) + x1h
					* det(y1, y2, z1, z2))
					/ det1;
			abe[0] = round6(abe[0]);
			abe[1] = round6(abe[1]);
			abe[2] = round6(abe[2]);
		}

		return det1;

	} // End of method solve3

	/*****************************************************************************/

	public void getTriangle(Triangle t, int n) {
		for (int i = 0; i <= 2; i++) {
			t.x[i] = Global.mainTriangles[M1].x[i] * xform[n].c00
					- Global.mainTriangles[M1].y[i] * xform[n].c10
					+ xform[n].c20;

			t.y[i] = Global.mainTriangles[M1].x[i] * xform[n].c01
					+ Global.mainTriangles[M1].y[i] * xform[n].c11
					- xform[n].c21;
		}
	}

	/*****************************************************************************/

	public void getPostTriangle(Triangle t, int n) {
		for (int i = 0; i <= 2; i++) {
			t.x[i] = Global.mainTriangles[M1].x[i] * xform[n].p00
					- Global.mainTriangles[M1].y[i] * xform[n].p10
					+ xform[n].p20;

			t.y[i] = Global.mainTriangles[M1].x[i] * xform[n].p01
					+ Global.mainTriangles[M1].y[i] * xform[n].p11
					- xform[n].p21;
		}
	}

	/*****************************************************************************/

	public void save(PrintWriter w) {
		w.print("<flame ");
		w.print("name=\"" + name + "\" ");

		w.print("version=\"" + APPNAME + " " + VERSION + "\" ");

		if (time != 0) {
			w.print("time=\"" + time + "\" ");
		}

		// plpl rotate or angle is missing
		// but angle does not even appear in flam3 2.7.11 !
		if (fangle != 0) {
			w.print("rotate=\"" + (-fangle) * 180. / Math.PI + "\" ");
		}

		w.print("size=\"" + width + " " + height + "\" ");
		w.print("center=\"" + center[0] + " " + center[1] + "\" ");
		w.print("scale=\"" + pixels_per_unit + "\" ");

		if (zoom != 0) {
			w.print("zoom=\"" + zoom + "\" ");
		}

		w.print("oversample=\"" + spatial_oversample + "\" ");
		w.print("filter=\"" + spatial_filter_radius + "\" ");
		w.print("quality=\"" + sample_density + "\" ");
		w.print("background=\"" + (background[0] / 255.0) + " "
				+ (background[1] / 255.0) + " " + (background[2] / 255.0)
				+ "\" ");
		w.print("brightness=\"" + brightness + "\" ");
		w.print("gamma=\"" + gamma + "\" ");
		w.print("vibrancy=\"" + vibrancy + "\" ");

		if (estimator != 9.0) {
			w.print("estimator_radius=\"" + estimator + "\" ");
		}
		if (estimator_min != 0.0) {
			w.print("estimator_min=\"" + estimator_min + "\" ");
		}
		if (estimator_curve != 0.4) {
			w.print("estimator_curve=\"" + estimator_curve + "\" ");
		}

		// plpl gamma_threshold is also missing
		if (gamma_threshold != 0.01) {
			w.print("gamma_threshold=\"" + gamma_threshold + "\" ");
		}

		w.println(">");

		for (int i = 0; i < nxforms; i++) {
			xform[i].save(w, "xform");
		}

		if (hasFinalXform) {
			xform[nxforms].save(w, "finalxform");
		}

		if (Global.oldPaletteFormat) {
			savePaletteOld(w);
		} else {
			savePaletteNew(w);
		}

		w.println("</flame>");

	} // End of method save

	/*****************************************************************************/

	void savePaletteOld(PrintWriter w) {
		for (int i = 0; i < 256; i++) {
			w.print("   <color index=\"");
			w.print(i);
			w.print("\" rgb=\"");
			w.print(cmap[i][0]);
			w.print(" ");
			w.print(cmap[i][1]);
			w.print(" ");
			w.print(cmap[i][2]);
			w.println("\"/>");
		}

	} // End of method savePaletteOld

	/*****************************************************************************/

	void savePaletteNew(PrintWriter w) {
		w.println("   <palette count=\"256\" format=\"RGB\">");
		for (int i = 0; i < 256; i += 8) {
			w.print("      ");
			for (int j = 0; j < 8; j++) {
				for (int k = 0; k < 3; k++) {
					String s = Integer.toHexString(cmap[i + j][k])
							.toUpperCase();
					if (s.length() < 2) {
						s = "0" + s;
					}
					w.print(s);
				}
			}
			w.println("");
		}
		w.println("   </palette>");

	} // End of method savePaletteNew

	/*****************************************************************************/

	public void dump(String title) {
		System.out
				.println("-----------------------------------------------------");
		System.out.println("---------- " + title + " ------------");
		System.out.println("time=" + time);
		System.out.println("nxforms=" + nxforms);
		System.out.println("width=" + width + "  height=" + height);
		System.out.println("center = " + center[0] + " " + center[1]);
		System.out.println("spatial_oversample=" + spatial_oversample);
		System.out.println("zoom = " + zoom);
		System.out.println("pixels_per_unit=" + pixels_per_unit);
		System.out.println("spatial_filter_radius=" + spatial_filter_radius);
		System.out.println("sample_density=" + sample_density);
		System.out.println("actual_density=" + actual_density);
		System.out.println("white_level=" + white_level);
		System.out.println("fangle=" + fangle);
		System.out.println("pixels_per_unit=" + pixels_per_unit);
		System.out.println("brightness=" + brightness);
		System.out.println("contrast=" + contrast);
		System.out.println("gamma=" + gamma);
		System.out.println("vibrancy=" + vibrancy);

		for (int i = 0; i < 10; i++) {
			System.out.println("cmap" + i + " " + cmap[i][0] + " " + cmap[i][1]
					+ " " + cmap[i][2]);
		}
		System.out
				.println("-----------------------------------------------------");
	}

	/*****************************************************************************/

	static ControlPoint randomFlame(ControlPoint sourcecp) {
		return randomFlame(sourcecp, 0);
	}

	/*****************************************************************************/

	static ControlPoint randomFlame(ControlPoint sourcecp, int algorithm) {

		int ii, min, max, rnd;
		double r, s, theta, phi;

		ControlPoint cp = new ControlPoint();

		if (sourcecp != null) {
			cp.clone(sourcecp);
		}

		int nv = XForm.getNrVariations();

		min = Global.randMinTransforms;
		max = Global.randMaxTransforms;

		int transforms = (int) (Global.random() * (max - min)) + min;

		while (true) {
			cp.randomCP(transforms, transforms, false);
			cp.setVariation(Global.variation);

			rnd = 9;
			if (algorithm == 1) {
				rnd = 0;
			} else if (algorithm == 2) {
				rnd = 7;
			} else if (Global.variation == -1) {
				rnd = (int) (Global.random() * 10);
			} else if (Global.variation == 0) {
				rnd = (int) (Global.random() * 10);
			}

			if ((rnd >= 0) && (rnd <= 6)) {
				for (int i = 0; i < transforms; i++) {
					ii = (int) (Global.random() * 10);
					if (ii < 9) {
						cp.xform[i].c00 = 1;
					} else {
						cp.xform[i].c00 = -1;
					}
					cp.xform[i].c01 = 0;
					cp.xform[i].c10 = 0;
					cp.xform[i].c11 = 1;
					cp.xform[i].c20 = 0;
					cp.xform[i].c21 = 0;
					cp.xform[i].color = 0;
					cp.xform[i].symmetry = 0;
					cp.xform[i].vars[0] = 1;
					for (int j = 1; j < nv; j++) {
						cp.xform[i].vars[j] = 0;
					}

					cp.xform[i].translate(Global.random() * 2 - 1,
							Global.random() * 2 - 1);
					cp.xform[i].rotate(Global.random() * 360);
					if (i > 0) {
						cp.xform[i].scale(Global.random() * 0.8 + 0.2);
					} else {
						cp.xform[i].scale(Global.random() * 0.4 + 0.6);
					}
					ii = (int) (Global.random() * 2);
					if (ii == 0) {
						cp.xform[i].multiply(1, Global.random() - 0.5,
								Global.random() - 0.5, 1);
					}
					cp.setVariation(Global.variation);
				}
			} else if ((rnd >= 7) && (rnd <= 8)) {
				// for the source to chaos : the software
				for (int i = 0; i < transforms; i++) {
					r = Global.random() * 2 - 1;
					if ((0 <= r) && (r < 0.2)) {
						r += 0.2;
					}
					if ((r > -0.2) && (r <= 0)) {
						r -= 0.2;
					}
					s = Global.random() * 2 - 1;
					if ((0 <= s) && (s < 0.2)) {
						s += 0.2;
					}
					if ((s > -0.2) && (s <= 0)) {
						s -= 0.2;
					}
					theta = Math.PI * Global.random();
					phi = (2.0 + Global.random()) * Math.PI / 4.0;
					cp.xform[i].c00 = r * Math.cos(theta);
					cp.xform[i].c10 = s
							* (Math.cos(theta) * Math.cos(phi) - Math
									.sin(theta));
					cp.xform[i].c01 = r * Math.sin(theta);
					cp.xform[i].c11 = s
							* (Math.sin(theta) * Math.cos(phi) + Math
									.cos(theta));
					cp.xform[i].c20 = Global.random() * 2 - 1;
					cp.xform[i].c21 = Global.random() * 2 - 1;
					cp.xform[i].density = 1.0 / transforms;
				}
			} else {
				for (int i = 0; i < transforms; i++) {
					cp.xform[i].density = 1.0 / transforms;
				}
			}

			Triangle[] triangles = new Triangle[NXFORMS + 2];
			for (int i = 0; i < triangles.length; i++) {
				triangles[i] = new Triangle();
			}
			cp.trianglesFromCP(triangles);

			ii = (int) (Global.random() * 2);
			if (ii > 0) {
				cp.computeWeights(triangles, transforms);
			} else {
				cp.equalizeWeights();
			}

			for (int i = 0; i < transforms; i++) {
				cp.xform[i].color = 1.0 * i / (transforms - 1);
			}

			// if(cp.xform[0].density==1) continue;

			switch (Global.symmetryType) {
			case 1:
				cp.addSymmetry(-1);
				break;
			case 2:
				cp.addSymmetry(Global.symmetryOrder);
				break;
			case 3:
				cp.addSymmetry(-Global.symmetryOrder);
				break;
			}

			break;
		}

		cp.brightness = Global.defBrightness;
		cp.gamma = Global.defGamma;
		cp.vibrancy = Global.defVibrancy;
		cp.sample_density = Global.defSampleDensity;
		cp.spatial_oversample = Global.defOversample;
		cp.spatial_filter_radius = Global.defFilterRadius;

		cp.zoom = 0;

		cp.calcBoundBox();

		return cp;

	} // End of method randomFlame

	/*****************************************************************************/

	void computeWeights(Triangle t[], int nt) {

		double total = 0;

		for (int i = 0; i < nt; i++) {
			xform[i].density = t[i].getArea();
			total += xform[i].density;
		}

		for (int i = 0; i < nt; i++) {
			xform[i].density /= total;
		}

		normalizeWeights();

	} // End of method computeWeights

	/*****************************************************************************/

	void equalizeWeights() {

		for (int i = 0; i < nxforms; i++) {
			xform[i].density = 1.0 / nxforms;
		}

	} // End of method equalizeWeights

	/*****************************************************************************/

	void normalizeWeights() {
		double total = 0;

		for (int i = 0; i < nxforms; i++) {
			total += xform[i].density;
		}

		if (total < 0.001) {
			equalizeWeights();
		} else {
			for (int i = 0; i < nxforms; i++) {
				xform[i].density /= total;
			}
		}

	} // End of method normalizeWeights

	/*****************************************************************************/

	void randomizeWeights() {
		for (int i = 0; i < nxforms; i++) {
			xform[i].density = Global.random();
		}

	} // End of method randomizeWeights

	/*****************************************************************************/

	void randomCP(int min, int max, boolean calc) {
		int v, rv;
		boolean varpossible;

		finalXformEnabled = false;
		hasFinalXform = false;

		hue_rotation = 1;
		cmapindex = RANDOMCMAP;
		CMap.getCMap(cmapindex, hue_rotation, cmap);
		time = 0.0;

		nxforms = (int) (Global.random() * (max - min)) + min;

		fillVarDisturb();

		int nv = XForm.getNrVariations();

		// check if some variations are authorized in the options
		varpossible = false;
		for (int i = 0; i < nv; i++) {
			varpossible |= XForm.isAuthorized(i);
		}

		if (varpossible) {
			// pick random variation according to the distribution
			while (true) {
				int ii = (int) (Global.random() * var_distrib.length);
				rv = var_distrib[ii];
				if (rv < 0) {
					break;
				}
				if (XForm.isAuthorized(rv)) {
					break;
				}
			}
		} else {
			rv = 0;
		}

		for (int i = nxforms; i < xform.length; i++) {
			xform[i].clear();
		}

		for (int i = 0; i < nxforms; i++) {
			xform[i].density = 1.0 / nxforms;
			xform[i].color = i * 1.0 / (nxforms - 1);

			xform[i].c00 = 2 * Global.random() - 1;
			xform[i].c01 = 2 * Global.random() - 1;
			xform[i].c10 = 2 * Global.random() - 1;
			xform[i].c11 = 2 * Global.random() - 1;
			xform[i].c20 = 2 * Global.random() - 1;
			xform[i].c21 = 2 * Global.random() - 1;

			for (int j = 0; j < nv; j++) {
				xform[i].vars[j] = 0;
			}

			if (rv < 0) {
				if (varpossible) {
					while (true) {
						int ii = (int) (Global.random() * mixed_var_distrib.length);
						v = mixed_var_distrib[ii];
						if (XForm.isAuthorized(v)) {
							break;
						}
					}
				} else {
					v = 0;
				}
				xform[i].vars[v] = 1.0;
			} else {
				xform[i].vars[rv] = 1.0;
			}

			xform[i].updateParameterValues();
		}

		if (calc) {
			calcBoundBox();
		}

	} // End of method randomCP

	/*****************************************************************************/

	void calcBoundBox() {
		double[][] points = new double[SUB_BATCH_SIZE][2];
		double deltax, minx, maxx;
		double deltay, miny, maxy;
		int cntminx, cntmaxx, cntminy, cntmaxy;
		int limitoutside;

		JulianVariation.npt = 0;

		prepare(propTable);

		iterateXY(points);

		limitoutside = (int) (0.05 * SUB_BATCH_SIZE + 0.5);

		minx = 1e99;
		maxx = -1e99;
		miny = 1e99;
		maxy = -1e99;

		for (int i = 0; i < SUB_BATCH_SIZE; i++) {
			if (points[i][0] < minx) {
				minx = points[i][0];
			}
			if (points[i][0] > maxx) {
				maxx = points[i][0];
			}
			if (points[i][1] < miny) {
				miny = points[i][1];
			}
			if (points[i][1] > maxy) {
				maxy = points[i][1];
			}
		}

		deltax = (maxx - minx) * 0.25;
		maxx = (maxx + minx) / 2;
		minx = maxx;

		deltay = (maxy - miny) * 0.25;
		maxy = (maxy + miny) / 2;
		miny = maxy;

		for (int j = 0; j <= 10; j++) {
			cntminx = 0;
			cntmaxx = 0;
			cntminy = 0;
			cntmaxy = 0;
			for (int i = 0; i < SUB_BATCH_SIZE; i++) {
				// px = points[i][0]*cosa + points[i][1]*sina;
				// py = points[i][1]*cosa - points[i][0]*sina;
				if (points[i][0] < minx) {
					cntminx++;
				}
				if (points[i][0] > maxx) {
					cntmaxx++;
				}
				if (points[i][1] < miny) {
					cntminy++;
				}
				if (points[i][1] > maxy) {
					cntmaxy++;
				}
			}

			if (cntminx < limitoutside) {
				minx += deltax;
			} else {
				minx -= deltax;
			}

			if (cntmaxx < limitoutside) {
				maxx -= deltax;
			} else {
				maxx += deltax;
			}

			if (cntminy < limitoutside) {
				miny += deltay;
			} else {
				miny -= deltay;
			}

			if (cntmaxy < limitoutside) {
				maxy -= deltay;
			} else {
				maxy += deltay;
			}

			deltax /= 2;
			deltay /= 2;
		}

		center[0] = (minx + maxx) / 2;
		center[1] = (miny + maxy) / 2;
		if ((maxx - minx > 0.001) && (maxy - miny > 0.001)) {
			pixels_per_unit = 0.65 * Math.min(width * 1. / (maxx - minx),
					height * 1. / (maxy - miny));
		} else {
			pixels_per_unit = 10.0;
		}

		if (Global.debug) {
			System.out.println("total points = " + SUB_BATCH_SIZE);
			System.out.println("Julian points = " + JulianVariation.npt);
			System.out.println("center = " + center[0] + " " + center[1]);
			System.out.println("minx=" + minx + " maxx=" + maxx);
			System.out.println("miny=" + miny + " maxy=" + maxy);
			System.out.println("ppu = " + pixels_per_unit);
		}

	} // End of method calcBoundBox

	/*****************************************************************************/

	void iterateXY(double points[][]) {
		double[] xy = new double[2];

		xy[0] = 2 * Global.random() - 1;
		xy[1] = 2 * Global.random() - 1;

		for (int i = 0; i < FUSE; i++) {
			int ii = (int) (Global.random() * PROP_TABLE_SIZE);
			propTable[ii].nextPointXY(xy);
		}

		for (int i = 0; i < points.length; i++) {
			int ii = (int) (Global.random() * PROP_TABLE_SIZE);
			propTable[ii].nextPointXY(xy);
			points[i][0] = xy[0];
			points[i][1] = xy[1];
			if (hasFinalXform) {
				xform[nxforms].nextPointXY(points[i]);
			}
		}

	} // End of method iterateXY

	/*****************************************************************************/

	void fillVarDisturb() {
		if (var_distrib != null) {
			return;
		}

		int nv = XForm.getNrVariations();

		// set the distribution of variations

		List<Integer> v = new ArrayList<Integer>();
		for (int i = 0; i < 7; i++) {
			v.add(Integer.valueOf(-1)); // random variation
		}
		for (int i = 0; i < 4; i++) {
			v.add(Integer.valueOf(0)); // linear ...
		}
		for (int i = 0; i < 3; i++) {
			v.add(Integer.valueOf(1));
		}
		for (int i = 0; i < 3; i++) {
			v.add(Integer.valueOf(2));
		}
		for (int i = 0; i < 2; i++) {
			v.add(Integer.valueOf(3));
		}
		for (int i = 0; i < 2; i++) {
			v.add(Integer.valueOf(4));
		}
		for (int i = 0; i < 2; i++) {
			v.add(Integer.valueOf(5));
		}
		for (int i = 0; i < 2; i++) {
			v.add(Integer.valueOf(6));
		}
		for (int i = 0; i < 2; i++) {
			v.add(Integer.valueOf(7));
		}
		for (int i = 8; i < nv; i++) {
			v.add(Integer.valueOf(i)); // others variations
		}

		var_distrib = new int[v.size()];
		for (int i = 0; i < v.size(); i++) {
			var_distrib[i] = (v.get(i)).intValue();
		}

		v = new ArrayList<Integer>();
		for (int i = 0; i < 3; i++) {
			v.add(Integer.valueOf(0));
		}
		for (int i = 0; i < 3; i++) {
			v.add(Integer.valueOf(1));
		}
		for (int i = 0; i < 3; i++) {
			v.add(Integer.valueOf(2));
		}
		for (int i = 0; i < 2; i++) {
			v.add(Integer.valueOf(3));
		}
		for (int i = 0; i < 2; i++) {
			v.add(Integer.valueOf(4));
		}
		for (int i = 0; i < 2; i++) {
			v.add(Integer.valueOf(5));
		}
		for (int i = 0; i < 2; i++) {
			v.add(Integer.valueOf(6));
		}
		for (int i = 0; i < 1; i++) {
			v.add(Integer.valueOf(5));
		}
		for (int i = 8; i < nv; i++) {
			v.add(Integer.valueOf(i));
		}

		mixed_var_distrib = new int[v.size()];
		for (int i = 0; i < v.size(); i++) {
			mixed_var_distrib[i] = (v.get(i)).intValue();
		}

	} // End of method fillVarDisturb

	/*****************************************************************************/

	public void setVariation(int index) {
		if (index < 0) {
			randomVariation();
		} else {
			for (int i = 0; i < nxforms; i++) {
				int nv = XForm.getNrVariations();
				for (int j = 0; j < nv; j++) {
					xform[i].vars[j] = 0.0;
				}
				xform[i].vars[index] = 1.0;
				xform[i].updateParameterValues();
			}
		}

	} // End of method setVariation

	/*****************************************************************************/

	public void randomVariation() {
		int a, b;

		int nv = XForm.getNrVariations();

		// count the number of possible variations
		int ns = 0;
		for (int i = 0; i < nv; i++) {
			ns += Global.variations[i] ? 1 : 0;
		}

		boolean varpossible = ns != 0;

		for (int i = 0; i < nxforms; i++) {
			for (int j = 0; j < nv; j++) {
				xform[i].vars[j] = 0.0;
			}

			if (varpossible) {
				while (true) {
					a = (int) (Global.random() * nv);
					if (XForm.isAuthorized(a)) {
						break;
					}
				}
				while (true) {
					b = (int) (Global.random() * nv);
					if (XForm.isAuthorized(b)) {
						break;
					}
				}
			} else {
				a = 0;
				b = 0;
			}

			if (a == b) {
				xform[i].vars[a] = 1.0;
			} else {
				xform[i].vars[a] = Global.random();
				xform[i].vars[b] = 1 - xform[i].vars[a];
			}

			xform[i].updateParameterValues();
		}

	} // End of method randomVariation

	/*****************************************************************************/

	public void zoomToRect(SRect r) {
		double scale, ppu;
		double dx, dy;

		scale = Math.pow(2.0, zoom);
		ppu = pixels_per_unit * scale;

		dx = ((r.left + r.right) / 2.0 - width / 2.0) / ppu;
		dy = ((r.top + r.bottom) / 2.0 - height / 2.0) / ppu;

		center[0] = center[0] + Math.cos(fangle) * dx - Math.sin(fangle) * dy;
		center[1] = center[1] + Math.sin(fangle) * dx + Math.cos(fangle) * dy;

		if (Global.preserveQuality) {
			double z = scale * (width / (Math.abs(r.right - r.left) + 1));
			zoom = Math.log(z) / Math.log(2.0);
			if (zoom > 3) {
				zoom = 3;
			} else if (zoom < -3) {
				zoom = -3;
			}
		} else {
			pixels_per_unit = pixels_per_unit * width
					/ Math.abs(r.right - r.left);
		}

	} // End of method zoomToRect

	/*****************************************************************************/

	public void zoomOutToRect(SRect r) {
		double ppu, dx, dy;

		if (Global.preserveQuality) {
			double z = Math.pow(2, zoom)
					/ (width / (Math.abs(r.right - r.left) + 1));
			zoom = Math.log(z) / Math.log(2);
			if (zoom > 3) {
				zoom = 3;
			} else if (zoom < -3) {
				zoom = -3;
			}
		} else {
			pixels_per_unit = pixels_per_unit / width
					* Math.abs(r.right - r.left);
		}

		ppu = pixels_per_unit * Math.pow(2, zoom);

		dx = ((r.left + r.right) / 2.0 - width / 2.0) / ppu;
		dy = ((r.top + r.bottom) / 2.0 - height / 2.0) / ppu;

		center[0] = center[0] - Math.cos(fangle) * dx + Math.sin(fangle) * dy;
		center[1] = center[1] - Math.sin(fangle) * dx - Math.cos(fangle) * dy;

	} // End of method zoomOutToRect

	/*****************************************************************************/

	public void rotate(double angle) {
		fangle += angle;
	}

	/*****************************************************************************/

	public void translate(double dx, double dy) {

		double scale = Math.pow(2.0, zoom);
		double ppu = pixels_per_unit * scale;

		dx = dx / ppu;
		dy = dy / ppu;

		center[0] = center[0] - Math.cos(fangle) * dx + Math.sin(fangle) * dy;
		center[1] = center[1] - Math.sin(fangle) * dx - Math.cos(fangle) * dy;

	}

	/*****************************************************************************/

	public void clear() {
		cmapindex = -1;
		zoom = 0;
		for (int i = 0; i < NXFORMS; i++) {
			xform[i].clear();
		}
		finalXformEnabled = false;
	}

	/*****************************************************************************/

	public void addSymmetry(int sym) {
		double a;
		int i, j, k;
		int rnd;

		if (sym == 0) {
			rnd = (int) (Global.random() * 1);
			if (rnd != 0) {
				rnd = (int) (Global.random() * 14);
				sym = sym_distrib[rnd];
			} else {
				rnd = (int) (Global.random() * 32);
				if (rnd != 0) {
					sym = (int) (Global.random() * 13) - 6;
				} else {
					sym = (int) (Global.random() * 51) - 25;
				}
			}
		}

		if ((sym == 0) || (sym == 1)) {
			return;
		}

		if (nxforms == NXFORMS) {
			return;
		}

		i = nxforms;

		int nv = XForm.getNrVariations();

		if (sym < 0) {
			xform[i].density = 1.0;
			xform[i].symmetry = 1;
			xform[i].vars[0] = 1.0;
			for (j = 1; j < nv; j++) {
				xform[i].vars[j] = 0;
			}
			xform[i].color = 1;
			xform[i].c00 = -1;
			xform[i].c01 = 0;
			xform[i].c10 = 0;
			xform[i].c11 = 1;
			xform[i].c20 = 0;
			xform[i].c21 = 0;
			i++;
			sym = -sym;
		}

		a = 2 * Math.PI / sym;

		k = 1;
		while ((k < sym) && (i < Global.symmetryNVars)) {
			xform[i].density = 1;
			xform[i].vars[0] = 1;
			xform[i].symmetry = 1;
			for (j = 1; j < nv; j++) {
				xform[i].vars[j] = 0;
			}
			if (sym < 3) {
				xform[i].color = 0;
			} else {
				xform[i].color = (k - 1.0) / (sym - 2.0);
			}

			if (xform[i].color > 1) {
				while (true) {
					xform[i].color = xform[i].color - 1;
					if (xform[i].color <= 1) {
						break;
					}
				}
			}

			xform[i].c00 = Math.cos(k * a);
			xform[i].c01 = Math.sin(k * a);
			xform[i].c10 = -xform[i].c01;
			xform[i].c11 = xform[i].c00;
			xform[i].c20 = 0;
			xform[i].c21 = 0;

			i++;
			k++;
		}

		nxforms = i;

	}

	/*****************************************************************************/

	void interpolateX(ControlPoint cp1, ControlPoint cp2, double tm) {
		double c0, c1;
		float[] s = new float[3];
		float[] t = new float[3];
		int nxforms1, nxforms2;

		if ((cp2.time - cp1.time) > 1e-6) {
			c0 = (cp2.time - tm) / (cp2.time - cp1.time);
			c1 = 1 - c0;
		} else {
			c0 = 1;
			c1 = 0;
		}

		ControlPoint cpr = new ControlPoint();
		cpr.time = tm;

		// interpolate gradient

		for (int i = 0; i < 256; i++) {
			Color.RGBtoHSB(cp1.cmap[i][0], cp1.cmap[i][1], cp1.cmap[i][2], s);
			Color.RGBtoHSB(cp2.cmap[i][0], cp2.cmap[i][1], cp2.cmap[i][2], t);
			t[0] = (float) (c0 * s[0] + c1 * t[0]);
			t[1] = (float) (c0 * s[1] + c1 * t[1]);
			t[2] = (float) (c0 * s[2] + c1 * t[2]);
			Color color = Color.getHSBColor(t[0], t[1], t[2]);
			cpr.cmap[i][0] = color.getRed();
			cpr.cmap[i][1] = color.getGreen();
			cpr.cmap[i][2] = color.getBlue();
		}

		cpr.cmapindex = -1;

		cpr.brightness = c0 * cp1.brightness + c1 * cp2.brightness;
		cpr.contrast = c0 * cp1.contrast + c1 * cp2.contrast;
		cpr.gamma = c0 * cp1.gamma + c1 * cp2.gamma;
		cpr.vibrancy = c0 * cp1.vibrancy + c1 * cp2.vibrancy;
		cpr.width = cp1.width;
		cpr.height = cp1.height;
		cpr.spatial_oversample = (int) Math.round(c0 * cp1.spatial_oversample
				+ c1 * cp2.spatial_oversample);
		cpr.spatial_filter_radius = c0 * cp1.spatial_filter_radius + c1
				* cp2.spatial_filter_radius;

		cpr.center[0] = c0 * cp1.center[0] + c1 * cp2.center[0];
		cpr.center[1] = c0 * cp1.center[1] + c1 * cp2.center[1];

		cpr.pixels_per_unit = c0 * cp1.pixels_per_unit + c1
				* cp2.pixels_per_unit;
		cpr.zoom = c0 * cp1.zoom + c1 * cp2.zoom;

		cpr.nbatches = (int) Math.round(c0 * cp1.nbatches + c1 * cp2.nbatches);
		cpr.white_level = (int) Math.round(c0 * cp1.white_level + c1
				* cp2.white_level);

		for (int i = 0; i <= 3; i++) {
			cpr.pulse[i / 2][i % 2] = c0 * cp1.pulse[i / 2][i % 2] + c1
					* cp2.pulse[i / 2][i % 2];
			cpr.wiggle[i / 2][i % 2] = c0 * cp1.wiggle[i / 2][i % 2] + c1
					* cp2.wiggle[i / 2][i % 2];
		}

		// save final xform
		nxforms1 = cp1.nxforms;
		if (cp1.hasFinalXform) {
			cp1.xform[NXFORMS].copy(cp1.xform[nxforms1]);
			cp1.xform[nxforms1].clear();
		} else {
			cp1.xform[NXFORMS].clear();
			cp1.xform[NXFORMS].symmetry = 1;
		}

		nxforms2 = cp2.nxforms;
		if (cp2.hasFinalXform) {
			cp2.xform[NXFORMS].copy(cp2.xform[nxforms2]);
			cp2.xform[nxforms2].clear();
		} else {
			cp2.xform[NXFORMS].clear();
			cp2.xform[NXFORMS].symmetry = 1;
		}

		int nv = XForm.getNrVariations();
		int np = XForm.getNrParameters();

		for (int i = 0; i <= NXFORMS; i++) {
			cpr.xform[i].density = c0 * cp1.xform[i].density + c1
					* cp2.xform[i].density;
			cpr.xform[i].color = c0 * cp1.xform[i].color + c1
					* cp2.xform[i].density;
			for (int j = 0; j < nv; j++) {
				cpr.xform[i].vars[j] = c0 * cp1.xform[i].vars[j] + c1
						* cp2.xform[i].vars[j];
			}
			for (int j = 0; j < np; j++) {
				cpr.xform[i].pvalues[j] = c0 * cp1.xform[i].pvalues[j] + c1
						* cp2.xform[i].pvalues[j];
			}

			cpr.xform[i].c00 = c0 * cp1.xform[i].c00 + c1 * cp2.xform[i].c00;
			cpr.xform[i].c01 = c0 * cp1.xform[i].c01 + c1 * cp2.xform[i].c01;
			cpr.xform[i].c10 = c0 * cp1.xform[i].c10 + c1 * cp2.xform[i].c10;
			cpr.xform[i].c11 = c0 * cp1.xform[i].c11 + c1 * cp2.xform[i].c11;
			cpr.xform[i].c20 = c0 * cp1.xform[i].c20 + c1 * cp2.xform[i].c20;
			cpr.xform[i].c21 = c0 * cp1.xform[i].c21 + c1 * cp2.xform[i].c21;
		}

		cpr.nxforms = 0;
		for (int i = 0; i < NXFORMS; i++) {
			if (cpr.xform[i].density != 0) {
				cpr.nxforms = i + 1;
			}
		}

		if (cpr.nxforms < NXFORMS) {
			cpr.xform[cpr.nxforms].copy(cp1.xform[NXFORMS]);
			cpr.xform[NXFORMS].clear();
		}

		cpr.finalXformEnabled = cp1.finalXformEnabled;

		// restore final xform in cp1 and cp2
		if (nxforms1 < NXFORMS) {
			cp1.xform[nxforms1].copy(cp1.xform[NXFORMS]);
			cp1.xform[NXFORMS].clear();
		}

		if (nxforms2 < NXFORMS) {
			cp2.xform[nxforms2].copy(cp2.xform[NXFORMS]);
			cp2.xform[NXFORMS].clear();
		}

		copy(cpr);

		CMap.copyPalette(cpr.cmap, cmap);

	} // End of method interpolateX

	/*****************************************************************************/

	public void normalizeVariations() {
		double totvar = 0;

		int nv = XForm.getNrVariations();

		for (int i = 0; i < nxforms; i++) {
			totvar = 0;
			for (int j = 0; j < nv; j++) {
				if (xform[i].vars[j] < 0) {
					xform[i].vars[j] *= (-1);
				}
				totvar += xform[i].vars[j];
			}
			if (totvar == 0) {
				xform[i].vars[0] = 1;
			} else {
				for (int j = 0; j < nv; j++) {
					xform[i].vars[j] /= totvar;
				}
			}
		}

	} // End of method normalizeVariations

	/*****************************************************************************/
} // End of class ControlPoint

