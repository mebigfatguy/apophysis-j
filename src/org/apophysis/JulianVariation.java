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

public class JulianVariation extends Variation {

	/*****************************************************************************/

	public static int npt = 0;

	int N;
	double c;

	int absN;
	double cN, vvar2;

	int context = 0;

	/*****************************************************************************/

	JulianVariation() {
		pnames = new String[] { "julian_power", "julian_dist" };

		N = (int) (Math.random() * 5) + 2;
		c = 1.0;

	}

	/*****************************************************************************/

	@Override
	public String getName() {
		return "julian";
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
			return N;
		case 1:
			return c;
		}
		return Double.NaN;
	}

	@Override
	public void setParameterValue(int index, double value) {
		switch (index) {
		case 0:
			N = (int) Math.round(value);
			if (N == 0)
				N = 1;
			break;

		case 1:
			c = value;
			break;
		}
	}

	/*****************************************************************************/

	@Override
	public void prepare(XForm xform, double weight) {
		super.prepare(xform, weight);

		absN = Math.abs(N);
		cN = c / N / 2;

		vvar2 = weight * Math.sqrt(2) / 2;

		context = 0;

		if (c == 1)
			switch (N) {
			case -2:
				context = -2;
				break;
			case -1:
				context = -1;
				break;
			case 1:
				context = 1;
				break;
			case 2:
				context = 2;
				break;
			}

	}

	/*****************************************************************************/

	@Override
	public boolean needLength() {
		return true;
	}

	/*****************************************************************************/

	static int kount1 = 0;
	static int kountm1 = 0;

	@Override
	public void compute(XForm xform) {
		double r, a, sina, cosa;

		switch (context) {
		case 0:
			a = (int) (Math.random() * absN);
			a = (Math.atan2(xform.fty, xform.ftx + 1e-50) + 2 * Math.PI * a)
					/ N;
			sina = Math.sin(a);
			cosa = Math.cos(a);
			r = weight
					* Math.pow(xform.ftx * xform.ftx + xform.fty * xform.fty,
							cN);
			xform.fpx += r * cosa;
			xform.fpy += r * sina;
			npt++;
			break;

		case -2:
			r = xform.flength;
			double xd = r + xform.ftx;

			r = weight / Math.sqrt(r * (xform.fty * xform.fty + xd * xd));

			int ii = (int) (Math.random() * 2);
			if (ii == 0) {
				xform.fpx += r * xd;
				xform.fpy -= r * xform.fty;
			} else {
				xform.fpx -= r * xd;
				xform.fpy += r * xform.fty;
			}
			break;

		case -1:
			r = weight
					/ (xform.ftx * xform.ftx + xform.fty * xform.fty + 1e-50);
			xform.fpx += r * xform.ftx;
			xform.fpy -= r * xform.fty;
			kountm1++;
			break;

		case 1:
			xform.fpx += weight * xform.ftx;
			xform.fpy += weight * xform.fty;
			kount1++;
			break;

		case 2:
			double d = Math.sqrt(xform.flength + xform.ftx);
			int jj = (int) (Math.random() * 2);
			if (jj == 0) {
				xform.fpx += vvar2 * d;
				xform.fpy += vvar2 / d * xform.fty;
			} else {
				xform.fpx -= vvar2 * d;
				xform.fpy -= vvar2 / d * xform.fty;
			}
			break;
		}

	}

	/*****************************************************************************/

} // End of class JulianVariation

