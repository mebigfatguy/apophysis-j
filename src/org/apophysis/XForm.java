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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apophysis.variations.BentVariation;
import org.apophysis.variations.BlobVariation;
import org.apophysis.variations.BlurVariation;
import org.apophysis.variations.BubbleVariation;
import org.apophysis.variations.CosineVariation;
import org.apophysis.variations.CurlVariation;
import org.apophysis.variations.CylinderVariation;
import org.apophysis.variations.DiamondVariation;
import org.apophysis.variations.DiscVariation;
import org.apophysis.variations.ExVariation;
import org.apophysis.variations.ExponentialVariation;
import org.apophysis.variations.EyefishVariation;
import org.apophysis.variations.Fan2Variation;
import org.apophysis.variations.FanVariation;
import org.apophysis.variations.FisheyeVariation;
import org.apophysis.variations.GaussianblurVariation;
import org.apophysis.variations.HandkerchiefVariation;
import org.apophysis.variations.HeartVariation;
import org.apophysis.variations.HorseshoeVariation;
import org.apophysis.variations.HyperbolicVariation;
import org.apophysis.variations.JuliaVariation;
import org.apophysis.variations.JulianVariation;
import org.apophysis.variations.JuliascopeVariation;
import org.apophysis.variations.LinearVariation;
import org.apophysis.variations.NoiseVariation;
import org.apophysis.variations.PDJVariation;
import org.apophysis.variations.PerspectiveVariation;
import org.apophysis.variations.PolarVariation;
import org.apophysis.variations.PopcornVariation;
import org.apophysis.variations.PowerVariation;
import org.apophysis.variations.RadialblurVariation;
import org.apophysis.variations.RectanglesVariation;
import org.apophysis.variations.Rings2Variation;
import org.apophysis.variations.RingsVariation;
import org.apophysis.variations.SinusoidalVariation;
import org.apophysis.variations.SphericalVariation;
import org.apophysis.variations.SpiralVariation;
import org.apophysis.variations.SupershapeVariation;
import org.apophysis.variations.SwirlVariation;
import org.apophysis.variations.WavesVariation;

public class XForm implements Constants {

	/****************************************************************************/
	// CONSTANTS

	static final double MAX_WEIGHT = 1000.0;
	static final double EPS = 1e-300;

	static List<Variation> registered_variations = new Vector<Variation>();

	static {
		registerVariation(new LinearVariation());
		registerVariation(new SinusoidalVariation());
		registerVariation(new SphericalVariation());
		registerVariation(new SwirlVariation());
		registerVariation(new HorseshoeVariation());
		registerVariation(new PolarVariation());
		registerVariation(new HandkerchiefVariation());
		registerVariation(new HeartVariation());
		registerVariation(new DiscVariation());
		registerVariation(new SpiralVariation());
		registerVariation(new HyperbolicVariation());
		registerVariation(new DiamondVariation());
		registerVariation(new ExVariation());
		registerVariation(new JuliaVariation());
		registerVariation(new BentVariation());
		registerVariation(new WavesVariation());
		registerVariation(new FisheyeVariation());
		registerVariation(new PopcornVariation());
		registerVariation(new ExponentialVariation());
		registerVariation(new PowerVariation());
		registerVariation(new CosineVariation());
		registerVariation(new RingsVariation());
		registerVariation(new FanVariation());

		registerVariation(new EyefishVariation());
		registerVariation(new BubbleVariation());
		registerVariation(new CylinderVariation());
		registerVariation(new NoiseVariation());
		registerVariation(new BlurVariation());
		registerVariation(new GaussianblurVariation());
		registerVariation(new RadialblurVariation());
		registerVariation(new Rings2Variation());
		registerVariation(new Fan2Variation());
		registerVariation(new BlobVariation());
		registerVariation(new PDJVariation());
		registerVariation(new PerspectiveVariation());
		registerVariation(new JulianVariation());
		registerVariation(new JuliascopeVariation());
		registerVariation(new CurlVariation());
		registerVariation(new RectanglesVariation());
		registerVariation(new SupershapeVariation());
	}

	static int nparams;
	static ClassLoader loader = null;

	static boolean[] sheep = null; // if variation is sheep compatible

	/****************************************************************************/
	// FIELDS

	public double density;
	public double color; // color coord for this function. 0 - 1
	public double color2; // Second color coord for this function. 0 - 1
	public double symmetry;

	public double c00, c01, c10, c11, c20, c21; // affine transformation
	public double p00, p01, p10, p11, p20, p21; // post processing

	public boolean postXswap;

	public int orientationtype;

	public double ftx, fty;
	public double fpx, fpy;
	public double fangle;
	public double fsina;
	public double fcosa;
	public double flength;
	public double colorC1, colorC2;

	public boolean enabled = true;

	/*****************************************************************************/
	// PRIVATE FIELDS

	public double vars[]; // variation parameters
	double pvalues[]; // parameters values
	Variation[] variations = null; // local variations for this xform

	int ncomp;
	Computation[] computations = null;

	// temp matrices
	double[][] MA = new double[3][3];
	double[][] MB = new double[3][3];
	double[][] MC = new double[3][3];

	/*****************************************************************************/
	// CONSTRUCOR

	XForm() {

		int nv = getNrVariations();

		// create the local variations for this transform

		variations = new Variation[nv];
		/*
		 * for(int i=0;i<nv;i++) variations[i] =
		 * getVariation(i).getNewInstance();
		 */

		vars = new double[nv];
		vars[0] = 1.0;
		for (int i = 1; i < nv; i++) {
			vars[i] = 0.0;
		}

		// compute the total number of parameters
		nparams = 0;
		for (int i = 0; i < nv; i++) {
			nparams += getVariation(i).getNrParameters();
		}

		pvalues = new double[nparams];
		/*
		 * int kp = 0; for(int i=0;i<nv;i++) { int np =
		 * getVariation(i).getNrParameters(); for(int j=0;j<np;j++)
		 * pvalues[kp++] = getVariation(i).getParameterValue(j); }
		 */

		c00 = 1;
		c01 = 0;
		c10 = 0;
		c11 = 1;
		c20 = 0;
		c21 = 0;

		p00 = 1;
		p01 = 0;
		p10 = 0;
		p11 = 1;
		p20 = 0;
		p21 = 0;

		density = 0;
		color = 0;
		symmetry = 0;
		postXswap = false;

	}

	/*****************************************************************************/
	// CONSTRUCTOR

	XForm(XmlTag tag) throws RuntimeException {
		this();

		density = tag.getDouble("weight", density);
		color = tag.getDouble("color", color);
		symmetry = tag.getDouble("symmetry", symmetry);

		double[] coefs = tag.getDoubles("coefs");
		if (coefs != null) {
			if (coefs.length == 6) {
				c00 = coefs[0];
				c01 = coefs[1];
				c10 = coefs[2];
				c11 = coefs[3];
				c20 = coefs[4];
				c21 = coefs[5];
			}
		}

		coefs = tag.getDoubles("post");
		if (coefs != null) {
			if (coefs.length == 6) {
				p00 = coefs[0];
				p01 = coefs[1];
				p10 = coefs[2];
				p11 = coefs[3];
				p20 = coefs[4];
				p21 = coefs[5];
			}
		}

		int nv = getNrVariations();
		for (int i = 0; i < nv; i++) {
			vars[i] = 0;
		}

		int ivar1 = tag.getInt("var1", -1);
		if ((ivar1 >= 0) && (ivar1 < nv)) {
			vars[ivar1] = 1.0;
		}

		coefs = tag.getDoubles("var");
		if (coefs != null) {
			int kv = coefs.length;
			if (kv <= nv) {
				for (int i = 0; i < kv; i++) {
					vars[i] = coefs[i];
				}
			}
		}

		int kp = 0;

		for (int i = 0; i < nv; i++) {
			// set weight
			vars[i] = tag.getDouble(getVariation(i).getName(), vars[i]);

			// set parameters
			int np = getVariation(i).getNrParameters();
			for (int j = 0; j < np; j++) {
				String pname = getVariation(i).getParameterName(j);
				pvalues[kp] = tag.getDouble(pname, getVariation(i)
						.getParameterValue(j));
				/*
				 * variations[i].setParameterValue(j,pvalues[kp]);
				 */
				kp++;
			}
		}

	}

	/*****************************************************************************/

	public static void registerPluginVariations(Main main) {
		if (Global.apopath == null) {
			return;
		}

		final File dplugin = new File(Global.apopath, PLUGNAME);
		if (!dplugin.exists()) {
			return;
		}

		List<Variation> vdup = registerJarPluginVariations(dplugin, main);

		sheep = new boolean[registered_variations.size()];
		for (int i = 0; i < sheep.length; i++) {
			sheep[i] = false;
		}

		int nd = vdup.size();
		if (nd > 0) {
			String msg = "Duplicate variations ";
			String sep = ": ";
			for (int i = 0; i < nd; i++) {
				Variation v = vdup.get(i);
				msg += sep + v.getName();
				sep = ", ";
			}

			main.alert(msg);
		}
	} // End of method registerPluginVariations

	/*****************************************************************************/

	private static List<Variation> registerJarPluginVariations(final File dplugin, Main main) {
		final File[] jars = dplugin.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".jar");
			}
		});

		if (jars.length == 0) {
			return Collections.<Variation>emptyList();
		}


		loader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
			public ClassLoader run() {
				try {
					URL[] urls = new URL[jars.length];
					for (int i = 0; i < jars.length; i++) {
						urls[i] = new URL("jar", "", "file://" + jars[i].getAbsolutePath() + "!/");
					}

					return new URLClassLoader(urls);
				} catch (Exception ex) {
					ex.printStackTrace();
					return null;
				}
			}
		});

		if (loader == null) {
			return Collections.<Variation>emptyList();
		}

		List<Variation> vdup = new ArrayList<Variation>();

		for (File jar : jars) {
			JarInputStream jis = null;
			try {
				jis = new JarInputStream(new BufferedInputStream(new FileInputStream(jar)));
				JarEntry entry = jis.getNextJarEntry();
				while (entry != null) {
					String clsName = entry.getName();
					if (clsName.endsWith(".class")) {
						clsName = clsName.substring(0, clsName.length() - ".class".length());
						try {
							clsName = clsName.replace('/', '.').replace('\\', '.');
							Class<?> klass = loader.loadClass(clsName);
							Object o = klass.newInstance();
							if (o instanceof Variation) {
								Variation v = (Variation) o;
								int ind = getVariationIndex(v.getName());
								if (ind >= 0) {
									vdup.add(v);
								} else {
									registerVariation(v);
								}
							}
						} catch (Exception err) {
							err.printStackTrace();
						}
					}

					entry = jis.getNextJarEntry();
				}
			} catch (Exception e) {
			} finally {
				IOCloser.close(jis);
			}
		}

		return vdup;
	}

	public static void installPlugin(File file) {
		int k = file.getName().indexOf('.');
		if (k < 0) {
			return;
		}
		String ext = file.getName().substring(k + 1);
		if (!ext.equals("class")) {
			return;
		}
		String cname = file.getName().substring(0, k);

		if (loader == null) {
			return;
		}

		if (Global.apopath == null) {
			return;
		}

		File dplugin = new File(Global.apopath, PLUGNAME);
		if (!dplugin.exists()) {
			return;
		}

		String pname = (new SPoint()).getClass().getPackage().getName();
		File dapo = new File(dplugin, pname);
		if (!dapo.exists()) {
			return;
		}

		File fdest = new File(dapo, file.getName());
		if (fdest.exists()) {
			Global.main.alert("The file is already in the plugin directory");
			return;
		}

		// copy the file to the plugin directory
		try {
			Global.copyFile(file, fdest);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// if we couldnt copy the file
		if (!fdest.exists()) {
			return;
		}

		Object o = null;
		try {
			Class<?> klass = loader.loadClass(pname + "." + cname);
			o = klass.newInstance();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (o instanceof Variation) {
			Variation v = (Variation) o;
			String vname = v.getName();

			int nv = getNrVariations();
			for (int i = 0; i < nv; i++) {
				if (getVariation(i).getName().equals(vname)) {
					fdest.delete();
					Global.main.alert("Variation " + vname + " already exists");
					return;
				}
			}

			Global.main.alert("Variation " + vname
					+ " installed, will be available after program restart");
		} else {
			fdest.delete();
		}

	} // End of method installPlugin

	/*****************************************************************************/

	public static int getNrVariations() {
		return registered_variations.size();
	}

	/****************************************************************************/

	public static int getVariationIndex(String name) {
		int n = registered_variations.size();
		for (int i = 0; i < n; i++) {
			if (getVariation(i).getName().equals(name)) {
				return i;
			}
		}
		return -1;
	}

	/****************************************************************************/

	public static void registerVariation(Variation variation) {
		registered_variations.add(variation);
	}

	/****************************************************************************/

	public static Variation getVariation(int index) {
		return registered_variations.get(index);
	}

	/****************************************************************************/

	public static int getNrParameters() {
		return nparams;
	}

	/****************************************************************************/

	public static String getParameterName(int index) {
		int k = 0;
		int nv = getNrVariations();
		for (int i = 0; i < nv; i++) {
			int np = XForm.getVariation(i).getNrParameters();
			if (index < k + np) {
				return XForm.getVariation(i).getParameterName(index - k);
			} else {
				k += np;
			}
		}

		return "";

	} // End of method getParameter ( was getVariable )

	/****************************************************************************/

	public void copy(XForm xform) {

		if (xform == null) {
			return;
		}

		vars = new double[xform.vars.length];
		System.arraycopy(xform.vars, 0, vars, 0, vars.length);

		System.arraycopy(xform.pvalues, 0, pvalues, 0, pvalues.length);

		density = xform.density;

		c00 = xform.c00;
		c01 = xform.c01;
		c10 = xform.c10;
		c11 = xform.c11;
		c20 = xform.c20;
		c21 = xform.c21;

		p00 = xform.p00;
		p01 = xform.p01;
		p10 = xform.p10;
		p11 = xform.p11;
		p20 = xform.p20;
		p21 = xform.p21;

		color = xform.color;
		color2 = xform.color2;
		symmetry = xform.symmetry;
		orientationtype = xform.orientationtype;
		postXswap = xform.postXswap;

		enabled = xform.enabled;

	} // End of method clone

	/****************************************************************************/

	public void prepare() {
		colorC1 = (1 + symmetry) / 2;
		colorC2 = color * (1 - symmetry) / 2;

		int nv = getNrVariations();

		boolean needAngle = false;
		boolean needLength = false;
		boolean needSine = false;

		int kp = 0;

		for (int i = 0; i < nv; i++) {
			if (vars[i] != 0) {
				needAngle = needAngle || getVariation(i).needAngle();
				needLength = needLength || getVariation(i).needLength();
				needSine = needSine || getVariation(i).needSine();

				if (variations[i] == null) {
					variations[i] = getVariation(i).getNewInstance();
				}
				int np = getVariation(i).getNrParameters();
				for (int j = 0; j < np; j++) {
					variations[i].setParameterValue(j, pvalues[kp++]);
				}

				variations[i].prepare(this, vars[i]);
			} else {
				variations[i] = null;
				kp += getVariation(i).getNrParameters();
			}
		}

		// build list of computations
		List<Computation> v = new ArrayList<Computation>();

		if (needAngle) {
			v.add(new AngleComputation());
		}
		if (needLength) {
			v.add(new LengthComputation());
		}
		if (needSine) {
			v.add(new SineComputation());
		}

		// pre-variations
		for (int i = 0; i < nv; i++) {
			if (vars[i] != 0) {
				if (getVariation(i).getName().startsWith("pre_")) {
					v.add(variations[i]);
				}
			}
		}

		// normal variations
		for (int i = 0; i < nv; i++) {
			if (vars[i] != 0) {
				if ((!getVariation(i).getName().startsWith("pre_"))
						&& (!getVariation(i).getName().startsWith("post_"))) {
					v.add(variations[i]);
				}
			}
		}

		// post-variations
		for (int i = 0; i < nv; i++) {
			if (vars[i] != 0) {
				if (getVariation(i).getName().startsWith("post_")) {
					v.add(variations[i]);
				}
			}
		}

		// post-computation
		if (needPost()) {
			v.add(new PostComputation());
		}

		// vector to array

		ncomp = v.size();
		computations = new Computation[ncomp];
		for (int i = 0; i < ncomp; i++) {
			computations[i] = v.get(i);
		}

	} // End of method prepare

	/****************************************************************************/

	public void dump(String msg) {

		System.out.println("dump of xform " + msg);
		System.out.println("c = " + c00 + " " + c10 + " " + c20 + " " + c01
				+ " " + c11 + " " + c21);

		if (computations != null) {
			int n = computations.length;
			for (int i = 0; i < n; i++) {
				System.out.println("computation" + i + " = "
						+ computations[i].getClass());
			}
		}

		if (vars != null) {
			for (int i = 0; i < 20; i++) {
				System.out.print(vars[i] + " ");
			}
		}
		System.out.println("");
	}

	/****************************************************************************/

	public void print(String msg) {
		System.out.println(msg + " " + c00 + " " + c10 + " " + c20 + " " + c01
				+ " " + c11 + " " + c21);
	}

	/****************************************************************************/

	final boolean needPost() {
		return (p00 != 1.0) || (p01 != 0.0) || (p10 != 0.0) || (p11 != 1.0)
				|| (p20 != 0.0) || (p21 != 0.0);
	}

	/****************************************************************************/

	public boolean isNotNull() {
		int nv = getNrVariations();

		for (int i = 1; i < nv; i++) {
			if (vars[i] != 0) {
				return true;
			}
		}

		if (vars[0] != 1) {
			return true;
		}

		return (c00 != 1) || (c01 != 0) || (c10 != 0) || (c11 != 0)
				|| (c20 != 0) || (c21 != 0);
	}

	/****************************************************************************/

	public void nextPoint(double xyc[]) {
		xyc[2] = xyc[2] * colorC1 + colorC2;

		ftx = c00 * xyc[0] + c10 * xyc[1] + c20;
		fty = c01 * xyc[0] + c11 * xyc[1] + c21;

		fpx = 0;
		fpy = 0;

		for (int i = 0; i < ncomp; i++) {
			computations[i].compute(this);
		}

		xyc[0] = fpx;
		xyc[1] = fpy;

	} // End of method nextPoint

	/****************************************************************************/

	public void nextPoint(double xyc[], double toxyc[]) {
		toxyc[2] = xyc[2] * colorC1 + colorC2;

		ftx = c00 * xyc[0] + c10 * xyc[1] + c20;
		fty = c01 * xyc[0] + c11 * xyc[1] + c21;

		fpx = 0;
		fpy = 0;

		for (int i = 0; i < ncomp; i++) {
			computations[i].compute(this);
		}

		toxyc[0] = fpx;
		toxyc[1] = fpy;

	}

	/****************************************************************************/

	public void nextPoint2(double xyc[]) {
		xyc[2] = xyc[2] * colorC1 + colorC2;
		xyc[3] = xyc[3] * colorC1 + colorC2;

		ftx = c00 * xyc[0] + c10 * xyc[1] + c20;
		fty = c01 * xyc[0] + c11 * xyc[1] + c21;

		fpx = 0;
		fpy = 0;

		for (int i = 0; i < ncomp; i++) {
			computations[i].compute(this);
		}

		xyc[0] = fpx;
		xyc[1] = fpy;

	}

	/****************************************************************************/

	public void nextPointXY(double xy[]) {

		ftx = c00 * xy[0] + c10 * xy[1] + c20;
		fty = c01 * xy[0] + c11 * xy[1] + c21;

		fpx = 0;
		fpy = 0;

		for (int i = 0; i < ncomp; i++) {
			computations[i].compute(this);
		}

		xy[0] = fpx;
		xy[1] = fpy;

	}

	/****************************************************************************/

	public void nextPointTo(double xyc1[], double xyc2[]) {
		xyc2[2] = xyc1[2] * colorC1 + colorC2;

		ftx = c00 * xyc1[0] + c10 * xyc1[1] + c20;
		fty = c01 * xyc1[0] + c11 * xyc1[1] + c21;

		fpx = 0;
		fpy = 0;

		for (int i = 0; i < ncomp; i++) {
			computations[i].compute(this);
		}

		xyc2[0] = fpx;
		xyc2[1] = fpy;

	} // End of method nextPointTo

	/****************************************************************************/

	void mult33(double M[][], double M1[][], double M2[][]) {
		M[0][0] = M1[0][0] * M2[0][0] + M1[0][1] * M2[1][0] + M1[0][2]
				* M2[2][0];
		M[0][1] = M1[0][0] * M2[0][1] + M1[0][1] * M2[1][1] + M1[0][2]
				* M2[2][1];
		M[0][2] = M1[0][0] * M2[0][2] + M1[0][1] * M2[1][2] + M1[0][2]
				* M2[2][2];

		M[1][0] = M1[1][0] * M2[0][0] + M1[1][1] * M2[1][0] + M1[1][2]
				* M2[2][0];
		M[1][1] = M1[1][0] * M2[0][1] + M1[1][1] * M2[1][1] + M1[1][2]
				* M2[2][1];
		M[1][2] = M1[1][0] * M2[0][2] + M1[1][1] * M2[1][2] + M1[1][2]
				* M2[2][2];

		M[2][0] = M1[2][0] * M2[0][0] + M1[2][1] * M2[1][0] + M1[2][2]
				* M2[2][0];
		M[2][0] = M1[2][0] * M2[0][1] + M1[2][1] * M2[1][1] + M1[2][2]
				* M2[2][1];
		M[2][0] = M1[2][0] * M2[0][2] + M1[2][1] * M2[1][2] + M1[2][2]
				* M2[2][2];

	}

	/****************************************************************************/

	void setMatrix(double M[][], double m00, double m01, double m02,
			double m10, double m11, double m12, double m20, double m21,
			double m22) {
		M[0][0] = m00;
		M[0][1] = m01;
		M[0][2] = m02;

		M[1][0] = m10;
		M[1][1] = m11;
		M[1][2] = m12;

		M[2][0] = m20;
		M[2][1] = m21;
		M[2][2] = m22;
	}

	/****************************************************************************/

	void apply() {

		setMatrix(MB, c00, c01, c20, c10, c11, c21, 0.0, 0.0, 1.0);

		mult33(MC, MB, MA);

		c00 = MC[0][0];
		c01 = MC[0][1];
		c10 = MC[1][0];
		c11 = MC[1][1];
		c20 = MC[0][2];
		c21 = MC[1][2];

		/*
		 * System.out.println("AFTER APPLY c= "+c00+" "+c10+" "+c20+" "+c01+" "+c11
		 * +" "+c21);
		 */

	} // End of method rotate

	/****************************************************************************/

	void rotate(double degrees) {
		double a = degrees * Math.PI / 180.0;

		setMatrix(MA, Math.cos(a), -Math.sin(a), 0.0, Math.sin(a), Math.cos(a),
				0.0, 0.0, 0.0, 1.0);

		apply();

	}

	/****************************************************************************/

	void translate(double x, double y) {
		setMatrix(MA, 1.0, 0.0, x, 0.0, 1.0, y, 0.0, 0.0, 1.0);

		apply();

	} // End of method translate

	/****************************************************************************/

	void multiply(double a, double b, double c, double d) {

		setMatrix(MA, a, b, 0.0, c, d, 0.0, 0.0, 0.0, 1.0);

		apply();

	}

	/****************************************************************************/

	void scale(double s) {

		setMatrix(MA, s, 0.0, 0.0, 0.0, s, 0.0, 0.0, 0.0, 1.0);

		apply();

	}

	/****************************************************************************/

	public void clear() {
		density = 0;
		color = 0;
		symmetry = 0;
		postXswap = false;

		c00 = 1;
		c01 = 0;
		c10 = 0;
		c11 = 1;
		c20 = 0;
		c21 = 0;

		p00 = 1;
		p01 = 0;
		p10 = 0;
		p11 = 1;
		p20 = 0;
		p21 = 0;

		vars[0] = 1;
		for (int i = 1; i < vars.length; i++) {
			vars[i] = 0;
		}

	} // End of method clear

	/****************************************************************************/

	public void save(PrintWriter w, String tagname) {
		w.print("   <" + tagname + " ");

		// plpl weight is forbidden for finalxform
		if (!"finalxform".equals(tagname)) {
			w.print("weight=\"" + density + "\" ");
		}

		w.print("color=\"" + color + "\" ");

		// plpl symmetry is missing
		if (symmetry != 0) {
			w.print("symmetry=\"" + symmetry + "\" ");
		}

		int kp = 0;

		for (int i = 0; i < vars.length; i++) {
			if (vars[i] != 0) {
				Variation variation = getVariation(i);
				w.print(variation.getName() + "=\"" + vars[i] + "\" ");
				int np = variation.getNrParameters();
				for (int j = 0; j < np; j++) {
					String pname = variation.getParameterName(j);
					w.print(pname + "=\"" + pvalues[kp] + "\" ");
					kp++;
				}
			} else {
				kp += getVariation(i).getNrParameters();
			}
		}

		w.print("coefs=\"" + c00 + " " + c01 + " " + c10 + " " + c11 + " "
				+ c20 + " " + c21 + "\" ");

		// plpl post is missing
		if (needPost()) {
			w.print("post=\"" + p00 + " " + p01 + " " + p10 + " " + p11 + " "
					+ p20 + " " + p21 + "\" ");
		}

		w.println(" />");
	}

	/****************************************************************************/

	static boolean isAuthorized(int index) {
		// true if this variation is authorized in the options
		return Global.variations[index];

	}

	/****************************************************************************/

	static void authorizeVariation(int index, boolean ok) {
		Global.variations[index] = ok;
	}

	/****************************************************************************/

	static int randomVariation() {
		int nv = getNrVariations();
		int ns = 0;
		for (int i = 0; i < nv; i++) {
			ns += Global.variations[i] ? 1 : 0;
		}

		// if not variation authorized, return linear
		if (ns == 0) {
			return 0;
		}

		int k = 0;
		while (true) {
			k = (int) (Math.random() * nv);
			if (isAuthorized(k)) {
				break;
			}
		}
		return k;
	}

	/****************************************************************************/

	public void updateParameterValues() {

		// get new variation instances and update parameter values

		int nv = XForm.getNrVariations();

		int np = 0;
		for (int i = 0; i < nv; i++) {
			if (vars[i] == 0) {
				variations[i] = null;
				np += XForm.getVariation(i).getNrParameters();
			} else {
				variations[i] = XForm.getVariation(i).getNewInstance();
				int kp = XForm.getVariation(i).getNrParameters();
				for (int k = 0; k < kp; k++) {
					pvalues[np++] = variations[i].getParameterValue(k);
				}
			}
		}

	} // End of method updateParameterValues

	/****************************************************************************/

} // End of class XForm

