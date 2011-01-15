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

public class RadialblurVariation extends Variation {

	double angle = 2 * Math.random() - 1;
	double spin, zoom;
	double[] rnd = new double[4];
	int N;

	int context;

	/*****************************************************************************/

	RadialblurVariation() {
		pnames = new String[] { "radial_blur_angle" };
	}

	/*****************************************************************************/

	@Override
	public String getName() {
		return "radial_blur";
	}

	/*****************************************************************************/

	@Override
	public int getGroup() {
		return 3;
	}

	/*****************************************************************************/

	@Override
	public double getParameterValue(int index) {
		switch (index) {
		case 0:
			return angle;
		}
		return Double.NaN;
	}

	@Override
	public void setParameterValue(int index, double value) {
		switch (index) {
		case 0:
			angle = value;
		}
	}

	/*****************************************************************************/

	@Override
	public void prepare(XForm xform, double weight) {
		super.prepare(xform, weight);

		spin = weight * Math.sin(angle * Math.PI / 2);
		zoom = weight * Math.cos(angle * Math.PI / 2);

		if (spin == 0) {
			context = 0;
		} else if (zoom == 0) {
			context = 1;
		} else {
			context = 2;
		}

		N = 0;
		rnd[0] = Math.random();
		rnd[1] = Math.random();
		rnd[2] = Math.random();
		rnd[3] = Math.random();

	}

	/*****************************************************************************/

	@Override
	public boolean needAngle() {
		return true;
	}

	@Override
	public boolean needLength() {
		return true;
	}

	/*****************************************************************************/

	@Override
	public void compute(XForm xform) {

		switch (context) {
		case 0: {
			double r = zoom * (rnd[0] + rnd[1] + rnd[2] + rnd[3] - 2);

			rnd[N] = Math.random();
			N = (N + 1) & 0x03;

			xform.fpx += r * xform.ftx;
			xform.fpy += r * xform.fty;
			break;
		}

		case 1: {
			double r = xform.flength;

			rnd[N] = Math.random();
			N = (N + 1) & 0x03;

			xform.fpx += r * xform.ftx;
			xform.fpy += r * xform.fty;
			break;
		}

		case 2: {
			double g = rnd[0] + rnd[1] + rnd[2] + rnd[3] - 2;
			rnd[N] = Math.random();
			N = (N + 1) & 0x03;

			double ra = xform.flength;
			double a = Math.atan2(xform.fty, xform.ftx) + spin * g;
			double cosa = Math.cos(a);
			double sina = Math.sin(a);

			double rz = zoom * g - 1;

			xform.fpx += ra * cosa + rz * xform.ftx;
			xform.fpy += ra * sina + rz * xform.fty;
			break;
		}
		}
	}

	/*****************************************************************************/

} // End of class GaussianblurVariation

