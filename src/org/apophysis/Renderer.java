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

import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Renderer implements Constants {

	/*****************************************************************************/
	// CONSTANTS

	static final int FUSE = 15;
	static final int PROP_TABLE_SIZE = 1024;

	static final int iRed = 0;
	static final int iGreen = 1;
	static final int iBlue = 2;
	static final int iCount = 3;

	/*****************************************************************************/
	// FIELDS

	XForm propTable[] = new XForm[PROP_TABLE_SIZE];
	XForm finalXform = null;

	boolean useFinalXform;

	double camX0, camX1, camY0, camY1, camH, camW;
	double bws, bhs, cosa, sina, rcx, rcy;
	double ppux, ppuy;

	int nrslices = 1;

	int bucketwidth, bucketheight;
	int bucketsize;
	int buckets[];
	int bucketfirst;
	int bucketslice;

	double sample_density;
	int oversample;
	int gutter_width;
	int max_gutter_width;

	ControlPoint fcp;
	int fstop;

	Imager imager;

	int colormap[][] = new int[256][3];

	int fmaxmem;
	int fslice;
	int fnumslices;

	long image_width, image_height;
	double image_center_x, image_center_y;

	int fcompatibility;
	int fnumbatches;

	int fminbatches;
	boolean frenderover;

	public long rendertime, pausetime;

	// PUBLIC

	public double fmindensity;

	double p[] = new double[3]; // used as a TCPoint
	double q[] = new double[3]; // used as a TCPoint

	boolean terminating = false;

	ThreadTarget target = null;

	boolean paused = false;
	boolean debug = false;
	int nu = 0;

	/*****************************************************************************/
	// CONSTRUCTOR

	Renderer(ThreadTarget target) {

		this.target = target;

		fnumslices = 1;
		fslice = 0;
		fstop = 0;

		imager = new Imager();
	}

	/*****************************************************************************/

	public void setCP(ControlPoint cp) {
		fcp = new ControlPoint();
		fcp.clone(cp);
	}

	/*****************************************************************************/

	public void stop() {
		fstop = 1;
	}

	/*****************************************************************************/

	public void breakRender() {
		fstop = -1;
	}

	/*****************************************************************************/

	public void setMinDensity(double q) {
		if (q < fcp.sample_density) {
			fmindensity = q;
		} else {
			fmindensity = fcp.sample_density;
		}
	}

	/*****************************************************************************/

	public boolean failed() {
		return fstop > 0;
	}

	/*****************************************************************************/

	public Image getTransparentImage() {
		if (fstop > 0) {
			return null;
		} else {
			return imager.getTransparentImage();
		}
	}

	/*****************************************************************************/

	public void updateImage(ControlPoint cp) {
		fcp.background = cp.background;
		fcp.spatial_filter_radius = cp.spatial_filter_radius;
		fcp.gamma = cp.gamma;
		fcp.vibrancy = cp.vibrancy;
		fcp.contrast = cp.contrast;
		fcp.brightness = cp.brightness;

		imager.setCP(fcp);
		imager.init();
		imager.createImage();
	}

	/*****************************************************************************/

	public void createColorMap() {

		for (int i = 0; i < 256; i++) {
			colormap[i][0] = (fcp.cmap[i][0] * fcp.white_level) / 256;
			colormap[i][1] = (fcp.cmap[i][1] * fcp.white_level) / 256;
			colormap[i][2] = (fcp.cmap[i][2] * fcp.white_level) / 256;
		}

	} // End of method createColorMap

	/*****************************************************************************/

	public void createCamera() {
		double scale, t0, t1, t2, t3;
		double corner_x, corner_y, xsize, ysize;
		int shift;

		scale = Math.pow(2.0, fcp.zoom);

		sample_density = fcp.sample_density * scale * scale;

		ppux = fcp.pixels_per_unit * scale;
		ppuy = fcp.pixels_per_unit * scale;

		shift = 0;

		corner_x = fcp.center[0] - fcp.width / ppux / 2.0;
		corner_y = fcp.center[1] - fcp.height / ppuy / 2.0;

		corner_y = corner_y - 0.00; // bzr

		t0 = gutter_width / (oversample * ppux);
		t1 = gutter_width / (oversample * ppuy);
		t2 = (2 * max_gutter_width - gutter_width) / (oversample * ppux);
		t3 = (2 * max_gutter_width - gutter_width) / (oversample * ppuy);

		camX0 = corner_x - t0;
		camX1 = corner_x + fcp.width / ppux + t2;

		camY0 = corner_y - t1 + shift;
		camY1 = corner_y + fcp.height / ppuy + t3;

		camW = camX1 - camX0;
		if (Math.abs(camW) > 0.01) {
			xsize = 1. / camW;
		} else {
			xsize = 1;
		}

		camH = camY1 - camY0;
		if (Math.abs(camH) > 0.01) {
			ysize = 1. / camH;
		} else {
			ysize = 1;
		}

		bws = (bucketwidth - 0.5) * xsize;
		bhs = (bucketheight - 0.5) * ysize;

		if (false) {
			System.out.println("scale = " + scale);
			System.out.println("fcp.sample_density = " + fcp.sample_density);
			System.out.println("fcp.center = " + fcp.center[0] + " "
					+ fcp.center[1]);
			System.out.println("fcp.width=" + fcp.width + "  fcp.height="
					+ fcp.height);
			System.out.println("ppux = " + ppux + " ppuy=" + ppuy);
			System.out.println("oversample=" + oversample);
			System.out.println("t0=" + t0 + " t1=" + t1 + " t2=" + t2 + " t3="
					+ t3);
			System.out.println("camX0=" + camX0 + "  camX1=" + camX1
					+ "  camW=" + camW);
			System.out.println("camY0=" + camY0 + "  camY1=" + camY1
					+ "  camH=" + camH);
			System.out.println("xsize=" + xsize + " ysize=" + ysize);
			System.out.println("bws=" + bws + "  bhs=" + bhs);
		}

		if (fcp.fangle != 0) {
			cosa = Math.cos(fcp.fangle);
			sina = Math.sin(fcp.fangle);
			rcx = fcp.center[0] * (1 - cosa) - fcp.center[1] * sina - camX0;
			rcy = fcp.center[1] * (1 - cosa) + fcp.center[0] * sina - camY0;
		}

	} // End of method createCamera

	/*****************************************************************************/

	public void computeBufferSize() {
		oversample = fcp.spatial_oversample;
		max_gutter_width = (MAX_FILTER_WIDTH - oversample) / 2;
		gutter_width = (imager.getFilterSize() - oversample) / 2;

		bucketwidth = oversample * fcp.width + 2 * max_gutter_width;
		bucketheight = oversample * fcp.height + 2 * max_gutter_width;
		bucketsize = bucketwidth * bucketheight;

		if (false) {
			System.out.println("fcp.width = " + fcp.width);
			System.out.println("fcp.height = " + fcp.height);
			System.out.println("fcp.spatial_oversample="
					+ fcp.spatial_oversample);
			System.out.println("max_gutter_width=" + max_gutter_width);
			System.out.println("gutter_width=" + gutter_width);
			System.out.println("bucketwidth=" + bucketwidth);
			System.out.println("bucketheight=" + bucketheight);
		}

	} // End of method computeBufferSize

	/*****************************************************************************/

	public void initBuffers() {

		try {
			if (buckets != null) {
				if (buckets.length != bucketsize * 4) {
					buckets = null;
					System.gc();
				} else {
					for (int i = 0; i < bucketsize; i++) {
						buckets[i] = 0;
					}
				}
			}
			if (buckets == null) {
				System.out.println("avail = " + availableMemory());
				System.out.println("needs = " + (bucketsize * 4 * 4));
				/*
				 * long usable = 80*availableMemory()/100;
				 * if(bucketsize*4*4>usable) { bucketslice =
				 * (int)(usable/bucketwidth/4/4); bucketslice =
				 * bucketslice/oversample*oversample; bucketsize =
				 * bucketslice*bucketwidth; buckets = new int[bucketsize*4]; }
				 * else
				 */
				{
					bucketslice = bucketheight;
					buckets = new int[bucketsize * 4];
				}
			}
		} catch (OutOfMemoryError err) {
			System.out.println("target is " + target);
			target.output("  Cannot allocate memory !\n");
			System.out.println("cannot allocate " + bucketsize + " buckets");
			buckets = null;
			fstop = 1;
			System.gc();
		}

		if (buckets == null) {
			fstop = 1;
		} else {
			imager.setBucketData(buckets, bucketwidth, bucketheight, 32);
		}

	} // End of method initBuffers

	/*****************************************************************************/

	long availableMemory() {
		System.gc();
		Runtime runtime = Runtime.getRuntime();
		long free = runtime.freeMemory();
		long total = runtime.totalMemory();
		long max = runtime.maxMemory();
		return max - (total - free);
	}

	/*****************************************************************************/

	public void render() {
		if (fcp.nxforms <= 0) {
			return;
		}
		fstop = 0;

		imager.setCP(fcp);
		imager.init();

		computeBufferSize();

		if (bucketwidth <= 0) {
			return;
		}
		if (bucketheight <= 0) {
			return;
		}

		initBuffers();
		if (fstop != 0) {
			return;
		}

		createColorMap();

		finalXform = fcp.prepare(propTable);
		useFinalXform = finalXform != null;

		createCamera();

		if (!frenderover) {
			clearBuckets();
		}

		long now = System.currentTimeMillis();
		setPixels();
		rendertime = System.currentTimeMillis() - now;

		if (fstop == 0) {
			target.output("  Creating image with quality = "
					+ fcp.actual_density + "\n");
			imager.createImage();
		}

		if (fstop == 1)
		 {
			freeBuckets();
		// freeBuckets();
		}

		return;

	} // End of method render

	/*****************************************************************************/

	public void clearBuckets() {
		for (int i = 0; i < bucketsize * 4; i++) {
			buckets[i] = 0;
		}

	} // End of method clearBuckets

	/*****************************************************************************/

	void freeBuckets() {
		buckets = null;
		System.gc();
	}

	/*****************************************************************************/

	void timeTrace(String msg) {
		System.out.println(msg);
	}

	/*****************************************************************************/

	void progress(double value) {
		target.progress(value);
	}

	/*****************************************************************************/

	public Image getImage() {
		return imager.getImage();
	}

	/*****************************************************************************/

	void iterateBatch(int incr) {
		double px, py;

		p[0] = 2 * Math.random() - 1; // x
		p[1] = 2 * Math.random() - 1; // y
		p[2] = Math.random(); // color

		for (int i = 0; i <= FUSE; i++) {
			int j = (int) (Math.random() * PROP_TABLE_SIZE);
			propTable[j].nextPoint(p);
		}

		// System.out.println("after FUSER p = "+p[0]+" "+p[1]+" "+p[2]);

		int nb = SUB_BATCH_SIZE;

		boolean firstp = true;

		for (int i = 0; i <= nb; i++) {
			int j = (int) (Math.random() * PROP_TABLE_SIZE);
			propTable[j].nextPoint(p);

			px = p[0] - camX0;
			if ((px < 0) || (px > camW)) {
				continue;
			}

			py = p[1] - camY0;
			if ((py < 0) || (py > camH)) {
				continue;
			}

			int ix = (int) (bhs * py + 0.5);
			int iy = (int) (bws * px + 0.5);
			int ic = (int) (p[2] * 255 + 0.5);

			int ind = (ix * bucketwidth + iy) * 4;
			buckets[ind] += incr * colormap[ic][0];
			buckets[ind + 1] += incr * colormap[ic][1];
			buckets[ind + 2] += incr * colormap[ic][2];
			buckets[ind + 3] += incr;

			if (false) {
				if (firstp) {
					System.out.println("p = " + p[0] + " " + p[1] + " " + p[2]
							+ " ix=" + ix + " iy=" + iy);
				}
			}
			firstp = false;
			nu++;
		}

	} // End of method iterateBatch

	/*****************************************************************************/

	void iterateBatchAngle(int incr) {
		double px, py;

		p[0] = 2 * Math.random() - 1;
		p[1] = 2 * Math.random() - 1;
		p[2] = Math.random();

		for (int i = 0; i <= FUSE; i++) {
			int j = (int) (Math.random() * PROP_TABLE_SIZE);
			propTable[j].nextPoint(p);
		}

		for (int i = 0; i <= SUB_BATCH_SIZE; i++) {
			int j = (int) (Math.random() * PROP_TABLE_SIZE);
			propTable[j].nextPoint(p);

			px = p[0] * cosa + p[1] * sina + rcx;
			if ((px < 0) || (px > camW)) {
				continue;
			}

			py = p[1] * cosa - p[0] * sina + rcy;
			if ((py < 0) || (py > camH)) {
				continue;
			}

			int ix = (int) (bhs * py + 0.5);
			int iy = (int) (bws * px + 0.5);
			int ic = (int) (p[2] * 255 + 0.5);

			int ind = (ix * bucketwidth + iy) * 4;
			buckets[ind] += incr * colormap[ic][0];
			buckets[ind + 1] += incr * colormap[ic][1];
			buckets[ind + 2] += incr * colormap[ic][2];
			buckets[ind + 3] += incr;
		}

	} // End of method iterateBatchAngle

	/*****************************************************************************/

	void iterateBatchFX(int incr) {
		double px, py;

		p[0] = 2 * Math.random() - 1;
		p[1] = 2 * Math.random() - 1;
		p[2] = Math.random();

		for (int i = 0; i <= FUSE; i++) {
			int j = (int) (Math.random() * PROP_TABLE_SIZE);
			propTable[j].nextPoint(p);
		}

		for (int i = 0; i < SUB_BATCH_SIZE; i++) {
			int j = (int) (Math.random() * PROP_TABLE_SIZE);
			propTable[j].nextPoint(p);

			finalXform.nextPointTo(p, q);

			px = q[0] - camX0;
			if ((px < 0) || (px > camW)) {
				continue;
			}

			py = q[1] - camY0;
			if ((py < 0) || (py > camH)) {
				continue;
			}

			int ix = (int) (bhs * py + 0.5);
			int iy = (int) (bws * px + 0.5);
			int ic = (int) (q[2] * 255 + 0.5);

			int ind = (ix * bucketwidth + iy) * 4;
			buckets[ind] += incr * colormap[ic][0];
			buckets[ind + 1] += incr * colormap[ic][1];
			buckets[ind + 2] += incr * colormap[ic][2];
			buckets[ind + 3] += incr;
		}

	} // End of method iterateBatchFX

	/*****************************************************************************/

	void iterateBatchAngleFX(int incr) {
		double px, py;

		p[0] = 2 * Math.random() - 1;
		p[1] = 2 * Math.random() - 1;
		p[2] = Math.random();

		for (int i = 0; i <= FUSE; i++) {
			int j = (int) (Math.random() * PROP_TABLE_SIZE);
			propTable[j].nextPoint(p);
		}

		for (int i = 0; i < SUB_BATCH_SIZE; i++) {
			int j = (int) (Math.random() * PROP_TABLE_SIZE);
			propTable[j].nextPoint(p);

			finalXform.nextPointTo(p, q);

			px = q[0] * cosa + q[1] * sina + rcx;
			if ((px < 0) || (px > camW)) {
				continue;
			}

			py = q[1] * cosa - q[0] * sina + rcy;
			if ((py < 0) || (py > camH)) {
				continue;
			}

			int ix = (int) (bhs * py + 0.5);
			int iy = (int) (bws * px + 0.5);
			int ic = (int) (q[2] * 255 + 0.5);

			int ind = (ix * bucketwidth + iy) * 4;
			buckets[ind] += incr * colormap[ic][0];
			buckets[ind + 1] += incr * colormap[ic][1];
			buckets[ind + 2] += incr * colormap[ic][2];
			buckets[ind + 3] += incr;
		}

	} // End of method iterateBatchAngleFX

	/*****************************************************************************/

	void setPixels() {
		int nsamples;
		int iproc;

		if (fcp.fangle == 0) {
			if (useFinalXform) {
				iproc = 0;
			} else {
				iproc = 1;
			}
		} else {
			if (useFinalXform) {
				iproc = 2;
			} else {
				iproc = 3;
			}
		}

		nsamples = (int) Math.round(sample_density * nrslices * bucketsize
				/ (oversample * oversample));

		fnumbatches = Math.round(nsamples / (fcp.nbatches * SUB_BATCH_SIZE));
		if (fnumbatches == 0) {
			fnumbatches = 1;
		}

		/*
		 * fminbatches =
		 * (int)Math.round(fnumbatches*fmindensity/fcp.sample_density);
		 * if(fminbatches==0) fminbatches = 1;
		 */

		if (debug) {
			System.out.println("scale = " + Math.pow(2.0, fcp.zoom));
			System.out.println("fcp.sample_density = " + fcp.sample_density);
			System.out.println("sample_density = " + sample_density);
			System.out.println("nrslices = " + nrslices);
			System.out.println("oversample = " + oversample);
			System.out.println("nsamples = " + nsamples);
			System.out.println("fcp.nbatches = " + fcp.nbatches);
			System.out.println("fnumbatches = " + fnumbatches);
		}

		double fbmax = 500 * Math.pow(2, fcp.zoom);
		fbmax = fbmax * fcp.width * fcp.height * fcp.width / 500 / 500 / 500;

		// System.out.println("fbmax = "+((int)fbmax));
		// System.out.println("fnumbatches = "+fnumbatches);

		// use increment to avoid too large fnumbatches
		int incr = 1;
		if (fcp.width < 150) {
			// preview
			incr = (int) (fnumbatches / 100.0);
			if (incr == 0) {
				incr = 1;
			}
			fnumbatches = fnumbatches / incr;
		} else if (fnumbatches > 50) {
			incr = 2;
			fnumbatches = fnumbatches / incr;
		} else if (fnumbatches < 1) {
			fnumbatches = 1;
		}

		// System.out.println("incr="+incr+" batches="+fnumbatches);

		nu = 0;

		bucketfirst = 0;
		FileOutputStream out = null;
		if (bucketslice < bucketheight) {
			try {
				out = new FileOutputStream(File.createTempFile("render",
						".swap"));
			} catch (IOException ex) {
			}
		}

		while (bucketfirst < bucketheight) {
			for (int i = 0; i < fnumbatches; i++) {
				if (fstop != 0) {
					fcp.actual_density += fcp.sample_density * i / fnumbatches;
					fnumbatches = i;
					return;
				}

				synchronized (this) {
					while (paused) {
						try {
							wait();
						} catch (Exception ex) {
						}
					}
				}

				if ((i % 0xFF) == 0) {
					progress(i * 1.0 / fnumbatches);
				}

				switch (iproc) {
				case 0:
					iterateBatchFX(incr);
					break;
				case 1:
					iterateBatch(incr);
					break;
				case 2:
					iterateBatchAngleFX(incr);
					break;
				case 3:
					iterateBatchAngle(incr);
					break;
				}
			}

			if (out != null) {
				try {
					writeBuckets(out, buckets, bucketslice);
				} catch (Exception ex) {
					ex.printStackTrace();
					fstop = 1;
					return;
				}
			}
			bucketfirst += bucketslice;
		}

		fcp.actual_density = fcp.actual_density + fcp.sample_density;

		progress(1);

		if (out != null) {
			try {
				out.close();
			} catch (IOException ex) {
			}
		}
		// System.out.println("number of buckets updated "+nu);

	} // End of method setPixels

	/*****************************************************************************/

	void writeBuckets(FileOutputStream out, int buckets[], int nrow)
			throws IOException {
		byte b[] = new byte[bucketwidth * 4 * 4];

		int offset = 0;
		for (int i = 0; i < nrow; i++) {
			int k = 0;
			for (int j = 0; j < bucketwidth * 4; j++) {
				b[k++] = (byte) (buckets[offset + j] & 0xFF);
				b[k++] = (byte) ((buckets[offset + j] >> 8) & 0xFF);
				b[k++] = (byte) ((buckets[offset + j] >> 16) & 0xFF);
				b[k++] = (byte) ((buckets[offset + j] >> 24) & 0xFF);
				offset++;
			}
			out.write(b, 0, k);
		}
	}

	/*****************************************************************************/

	public void setDebug(boolean debug) {
		if (false) {
			this.debug = debug;
		}
		if (false) {
			if (imager != null) {
				imager.setDebug(debug);
			}
		}
	}

	/*****************************************************************************/

	public void terminate() {
	}

	/*****************************************************************************/

	public synchronized void pause() {
		pausetime = System.currentTimeMillis();
		paused = true;
		System.out.println("paused is now " + paused);
	}

	/*****************************************************************************/

	public synchronized void unpause() {
		long now = System.currentTimeMillis();
		rendertime += (now - pausetime);
		paused = false;
		notify();
		System.out.println("paused is now " + paused);
	}

	/*****************************************************************************/
	/*****************************************************************************/

	public void saveImage(String filename, boolean comment, boolean encrypt,
			boolean watermark) throws IOException {
		try {
			imager.saveImage(filename, comment, encrypt, watermark);
			target.output("  Image saved onto " + filename + "\n");
		} catch (OutOfMemoryError err) {
			target.output("  *** Could not create image, not enough memory\n");
			err.printStackTrace();
			fstop = 1;
		}
	}

	/*****************************************************************************/

	public void showBigStats() {
		showSmallStats();
	}

	/*****************************************************************************/

	public void showSmallStats() {
		long totalsamples = fnumbatches * SUB_BATCH_SIZE;
		if (rendertime > 0) {
			long speed = totalsamples / rendertime * 1000;

			target.output("  Average speed: ");
			target.output("" + speed);
			target.output(" points per second");
			target.output("\n");

			target.output("  Pure rendering time : ");
			target.output(timeToString(rendertime));
			target.output("\n");
		}
	}

	/*****************************************************************************/

	String timeToString(long millis) {
		long mil = millis % 1000;
		millis /= 1000;
		long sec = millis % 60;
		millis /= 60;
		long min = millis % 60;
		millis /= 60;
		long hou = millis;

		return ((hou < 10) ? ("0" + hou) : ("" + hou))
				+ ":"
				+ ((min < 10) ? ("0" + min) : ("" + min))
				+ ":"
				+ ((sec < 10) ? ("0" + sec) : ("" + sec))
				+ ":"
				+ ((mil < 10) ? ("00" + mil) : ((mil < 100) ? ("0" + mil)
						: ("" + mil)));
	}

	/*****************************************************************************/

} // End of class Renderer

