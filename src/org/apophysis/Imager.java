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
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.imageio.ImageIO;

public class Imager implements Constants {

	/*****************************************************************************/
	// FIELDS

	private final int[] rbits = new int[256];
	private final int[] gbits = new int[256];
	private final int[] bbits = new int[256];
	private final int[] abits = new int[256];

	private double filter[][];
	private int filtersize;

	private int foversample;

	private int bucketwidth;
	private int buckets[];

	private ControlPoint fcp;

	// image

	private int pixels[];
	private int width;
	private int height;

	public boolean debug = false;

	/*****************************************************************************/

	Imager() {

		// to avoid bit shifting later

		for (int i = 0; i < 256; i++) {
			rbits[i] = i << 16;
			gbits[i] = i << 8;
			bbits[i] = i;
			abits[i] = i << 24;
		}
	}

	/*****************************************************************************/

	public void init() {

		width = fcp.width;
		height = fcp.height;
		pixels = new int[width * height];

		createFilter();

	}

	/*****************************************************************************/

	public void setCP(ControlPoint cp) {
		fcp = cp;
	}

	/*****************************************************************************/

	void createFilter() {
		int i, j, fw;
		double adjust, ii, jj;

		foversample = fcp.spatial_oversample;
		fw = (int) (2 * FILTER_CUTOFF * foversample * fcp.spatial_filter_radius);

		filtersize = fw + 1;

		// make sure same parity
		if ((filtersize + foversample) % 2 == 1) {
			filtersize++;
		}

		if (fw > 0.0) {
			adjust = (1.0 * FILTER_CUTOFF * filtersize) / fw;
		} else {
			adjust = 1.0;
		}

		filter = new double[filtersize][filtersize];

		for (i = 0; i < filtersize; i++) {
			for (j = 0; j < filtersize; j++) {
				ii = ((2.0 * i + 1.0) / filtersize - 1.0) * adjust;
				jj = ((2.0 * j + 1.0) / filtersize - 1.0) * adjust;
				filter[i][j] = Math.exp(-2.0 * (ii * ii + jj * jj));
			}
		}

		normalizeFilter();

	} // End of method createFilter

	/*****************************************************************************/

	void normalizeFilter() {
		double t = 0;
		for (int i = 0; i < filtersize; i++) {
			for (int j = 0; j < filtersize; j++) {
				t += filter[i][j];
			}
		}

		for (int i = 0; i < filtersize; i++) {
			for (int j = 0; j < filtersize; j++) {
				filter[i][j] /= t;
			}
		}

	} // End of method normalizeFilter

	/*****************************************************************************/

	public int getFilterSize() {
		return filtersize;
	}

	/*****************************************************************************/

	public void createImage() {
		createImage(0);
	}

	public void createImage(int yoffset) {
		double alpha, gamma;
		int i, j, ii, jj;
		int ri, gi, bi;
		int ai;
		double ls;
		double[] fp = new double[4];
		int vib, notvib;
		int[] bgi = new int[3];
		double filtervalue;
		double[] lsa = new double[1025];
		double sample_density;
		double k1, k2;
		double area;
		int bx, by;

		int pixeltot, pixelzero;

		if (fcp.gamma == 0) {
			gamma = fcp.gamma;
		} else {
			gamma = 1 / fcp.gamma;
		}

		vib = (int) (fcp.vibrancy * 256 + 0.5);
		notvib = 256 - vib;

		bgi[0] = (int) (fcp.background[0] + 0.5);
		bgi[1] = (int) (fcp.background[1] + 0.5);
		bgi[2] = (int) (fcp.background[2] + 0.5);

		pixeltot = 0xFF000000 | rbits[bgi[0]] | gbits[bgi[1]] | bbits[bgi[2]];

		pixelzero = 0;

		double p2 = Math.pow(2.0, fcp.zoom);
		sample_density = fcp.actual_density * p2 * p2;
		if (sample_density == 0) {
			sample_density = 0.001;
		}

		k1 = (fcp.contrast * BRIGHT_ADJUST * fcp.brightness * 268 * PREFILTER_WHITE) / 256.0;

		area = width * height / (fcp.getppux() * fcp.getppuy());
		k2 = (foversample * foversample)
				/ (fcp.contrast * area * fcp.white_level * sample_density);

		if (debug) {
			System.out.println("k1 = " + k1);
			System.out.println("area=" + area);
			System.out.println("k2=" + k2);
			System.out.println("ppux=" + fcp.getppux());
			System.out.println("ppuy=" + fcp.getppuy());
			System.out.println("width=" + width);
			System.out.println("height=" + height);
			System.out.println("foversample=" + foversample);
			System.out.println("fcp.contrast=" + fcp.contrast);
			System.out.println("fcp.white_level=" + fcp.white_level);
			System.out.println("sample_density=" + sample_density);
		}

		lsa[0] = 0;
		for (i = 1; i <= 1024; i++) {
			lsa[i] = (k1 * Math.log(1 + fcp.white_level * i * k2) / Math
					.log(10)) / (fcp.white_level * i);
		}

		ls = 0;
		ai = 0;
		by = 0;

		if (debug) {
			System.out.println("fcp.white_level=" + fcp.white_level);
			System.out.println("filtersize = " + filtersize);
			System.out.println("fcp.width=" + fcp.width + " fcp.height="
					+ fcp.height);
			System.out.println("foversample = " + foversample);
			System.out.println("transparency = " + fcp.transparency);
			System.out.println("notvib = " + notvib);
			System.out.println("gamma = " + gamma);
			System.out.println("END OF DEBUG");
		}

		for (i = 0; i < fcp.height; i++) {
			bx = 0;
			if ((i % 7) == 0) {
				progress(i * 1.0 / fcp.height);
			}

			int irow = (yoffset + i) * width;

			for (j = 0; j < fcp.width; j++) {
				if (filtersize > 1) {
					fp[0] = fp[1] = fp[2] = fp[3] = 0;
					for (ii = 0; ii < filtersize; ii++) {
						for (jj = 0; jj < filtersize; jj++) {
							filtervalue = filter[ii][jj];

							int ind = ((by + ii) * bucketwidth + bx + jj) * 4;
							ls = lsa[Math.min(1023, buckets[ind + iCount])];

							fp[0] += filtervalue * ls * buckets[ind + iRed];
							fp[1] += filtervalue * ls * buckets[ind + iGreen];
							fp[2] += filtervalue * ls * buckets[ind + iBlue];
							fp[3] += filtervalue * ls * buckets[ind + iCount];
						}
					}

					fp[0] /= PREFILTER_WHITE;
					fp[1] /= PREFILTER_WHITE;
					fp[2] /= PREFILTER_WHITE;
					fp[3] = fcp.white_level * fp[3] / PREFILTER_WHITE;

				} else {
					int ind = (by * bucketwidth + bx) * 4;
					ls = lsa[Math.min(1023, buckets[ind + iCount])]
							/ PREFILTER_WHITE;

					fp[0] = ls * buckets[ind + iRed];
					fp[1] = ls * buckets[ind + iGreen];
					fp[2] = ls * buckets[ind + iBlue];
					fp[3] = ls * buckets[ind + iCount] * fcp.white_level;
				}

				bx += foversample;

				if (fcp.transparency) {
					// transparency

					if (fp[3] > 0) {
						alpha = Math.pow(fp[3], gamma);
						ls = vib * alpha / fp[3];
						ai = (int) (alpha * 256);
						if (ai <= 0) {
							pixels[irow + j] = pixelzero;
							continue;
						} else if (ai > 255) {
							ai = 255;
						}
					} else {
						pixels[irow + j] = pixelzero;
						continue;
					}

					if (notvib > 0) {
						ri = (int) (ls * fp[0] + notvib
								* Math.pow(fp[0], gamma));
						gi = (int) (ls * fp[1] + notvib
								* Math.pow(fp[1], gamma));
						bi = (int) (ls * fp[2] + notvib
								* Math.pow(fp[2], gamma));
					} else {
						ri = (int) (ls * fp[0]);
						gi = (int) (ls * fp[1]);
						bi = (int) (ls * fp[2]);
					}

					// ignoring background color in transparent renders

					ri = (ri * 255) / ai;
					if (ri < 0) {
						ri = 0;
					} else if (ri > 255) {
						ri = 255;
					}

					gi = (gi * 255) / ai;
					if (gi < 0) {
						gi = 0;
					} else if (gi > 255) {
						gi = 255;
					}

					bi = (bi * 255) / ai;
					if (bi < 0) {
						bi = 0;
					} else if (bi > 255) {
						bi = 255;
					}

					pixels[irow + j] = rbits[ri] | gbits[gi] | bbits[bi]
							| abits[ai];
				} else {
					// no transparency

					if (fp[3] > 0) {
						alpha = Math.pow(fp[3], gamma);
						ls = vib * alpha / fp[3];
						ai = (int) (alpha * 256 + 0.5);
						if (ai < 0) {
							ai = 0;
						} else if (ai > 255) {
							ai = 255;
						}
					} else {
						pixels[irow + j] = pixeltot;
						continue;
					}

					if (notvib > 0) {
						ri = (int) (ls * fp[0] + notvib
								* Math.pow(fp[0], gamma) + 0.5);
						gi = (int) (ls * fp[1] + notvib
								* Math.pow(fp[1], gamma) + 0.5);
						bi = (int) (ls * fp[2] + notvib
								* Math.pow(fp[2], gamma) + 0.5);
					} else {
						ri = (int) (ls * fp[0] + 0.5);
						gi = (int) (ls * fp[1] + 0.5);
						bi = (int) (ls * fp[2] + 0.5);
					}

					if (ri < 0) {
						ri = 0;
					} else if (ri > 255) {
						ri = 255;
					}

					if (gi < 0) {
						gi = 0;
					} else if (gi > 255) {
						gi = 255;
					}

					if (bi < 0) {
						bi = 0;
					} else if (bi > 255) {
						bi = 255;
					}

					pixels[irow + j] = rbits[ri] | gbits[gi] | bbits[bi]
							| abits[ai];
				}
			} // for j

			by += foversample;
		} // for i

		// System.out.println("nb of pixels updated = "+nu);

	} // End of method createImage

	/*****************************************************************************/

	public void setBucketData(int buckets[], int bucketwidth, int bucketheight) {
		this.buckets = buckets;
		this.bucketwidth = bucketwidth;
	}

	/*****************************************************************************/

	public void saveImage(String filename, boolean comment, boolean encrypt,
			boolean watermark) throws IOException {

		String format = "jpg";
		int i = filename.lastIndexOf('.');
		if (i > 0) {
			format = filename.substring(i + 1);
		}

		Color bg = new Color(fcp.background[0], fcp.background[1],
				fcp.background[2]);

		BufferedImage bimage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics g = bimage.createGraphics();
		g.setColor(bg);
		g.fillRect(0, 0, width, height);
		g.drawImage(getImage(), 0, 0, null);
		if (watermark) {
			drawWatermark(g, width, height);
		}
		g.dispose();

		// ImageIO.write(bimage,format,new File(filename));

		if (format.equals("jpg") && (comment)) {
			String scomment = buildComment(encrypt);
			JpegCommentOutputStream os = new JpegCommentOutputStream(new File(
					filename), scomment);
			ImageIO.write(bimage, format, os);
			os.close();
		} else if (format.equals("png") && (comment)) {
			String scomment = buildComment(encrypt);
			PngCommentOutputStream os = new PngCommentOutputStream(new File(
					filename), scomment);
			ImageIO.write(bimage, format, os);
			os.close();
		} else {
			ImageIO.write(bimage, format, new File(filename));
		}

		bimage = null;

	} // End of method saveImage

	/*****************************************************************************/

	String buildComment(boolean encrypt) {
		String s = null;

		StringWriter sw = new StringWriter();
		PrintWriter w = new PrintWriter(sw);

		fcp.save(w);
		w.flush();

		if ((encrypt) && (Global.passwordText.length() > 0)) {
			try {
				if ((Global.crypto == null)
						|| !Global.crypto.password.equals(Global.passwordText)) {
					Global.crypto = new Crypto(Global.passwordText);
				}
				byte[] e = Global.crypto.encode(sw.toString().getBytes());
				sw.close();

				try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
					 BASE64EncoderStream b = new BASE64EncoderStream(bos)) {
					b.write(e);
					b.flush();
					s = "encryptedflame:" + (new String(bos.toByteArray()));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			s = sw.toString();
		}

		return s;

	} // End of method buildComment

	/*****************************************************************************/

	void drawWatermark(Graphics g, int width, int height) {
		if (Global.watermarkFile.length() == 0) {
			return;
		}
		File file = new File(Global.watermarkFile);
		if (!file.exists()) {
			return;
		}

		Image wimage = null;

		try {
			wimage = ImageIO.read(file);
		} catch (Exception ex) {
		}

		if (wimage == null) {
			return;
		}

		wimage = makeImageTransparent(wimage);

		int w = wimage.getWidth(null);
		int h = wimage.getHeight(null);

		int x = 0;
		int y = 0;

		switch (Global.watermarkPosition) {
		case 0:
			x = 0;
			y = 0;
			break;

		case 1:
			x = width / 2 - w / 2;
			y = 0;
			break;

		case 2:
			x = width - w;
			y = 0;
			break;

		case 3:
			x = 0;
			y = height / 2 - h / 2;
			break;

		case 4:
			x = width / 2 - w / 2;
			y = height / 2 - h / 2;
			break;

		case 5:
			x = width - w;
			y = height / 2 - h / 2;
			break;

		case 6:
			x = 0;
			y = height - h;
			break;

		case 7:
			x = width / 2 - w / 2;
			y = height - h;
			break;

		case 8:
			x = width - w;
			y = height - h;
			break;
		}

		g.drawImage(wimage, x, y, null);

	}

	/*****************************************************************************/

	public Image getTransparentImage() {
		return null;
	}

	/*****************************************************************************/

	void progress(double fraction) {
	}

	/*****************************************************************************/

	public Image getImage() {

		if (pixels == null) {
			return null;
		}

		MemoryImageSource source = new MemoryImageSource(width, height, pixels,
				0, width);
		return Toolkit.getDefaultToolkit().createImage(source);
	}

	/*****************************************************************************/

	Image makeImageTransparent(Image oldimage) {
		Image newimage = null;

		int w = oldimage.getWidth(null);
		int h = oldimage.getHeight(null);

		int[] pixels = new int[w * h];
		PixelGrabber pg = new PixelGrabber(oldimage, 0, 0, w, h, pixels, 0, w);

		try {
			pg.grabPixels();

			for (int i = 0; i < pixels.length; i++) {
				if (pixels[i] == 0xFF000000) {
					pixels[i] = 0;
				}
			}

			newimage = Toolkit.getDefaultToolkit().createImage(
					new MemoryImageSource(w, h, pg.getColorModel(), pixels, 0,
							w));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (newimage != null) {
			return newimage;
		} else {
			return oldimage;
		}

	} // End of method makeImageTransparent

	/*****************************************************************************/

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/*****************************************************************************/

} // End of class Imager

