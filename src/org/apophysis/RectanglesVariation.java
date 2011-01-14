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

public class RectanglesVariation extends Variation {

	/*****************************************************************************/

	double FX;
	double FY;

	int context = 0;

	/*****************************************************************************/

	RectanglesVariation() {
		pnames = new String[] { "rectangles_x", "rectangles_y" };

		FX = 1.0;
		FY = 1.0;

	}

	/*****************************************************************************/

	@Override
	public String getName() {
		return "rectangles";
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
			return FX;
		case 1:
			return FY;
		}
		return Double.NaN;
	}

	@Override
	public void setParameterValue(int index, double value) {
		switch (index) {
		case 0:
			FX = value;
			break;
		case 1:
			FY = value;
			break;
		}
	}

	/*****************************************************************************/

	@Override
	public void prepare(XForm xform, double weight) {
		super.prepare(xform, weight);

		if (FX == 0) {
			if (FY == 0)
				context = 1;
			else
				context = 2;
		} else if (FY == 0)
			context = 3;

	}

	/*****************************************************************************/

	public static int kount0 = 0;
	public static int kount1 = 0;
	public static int kount2 = 0;
	public static int kount3 = 0;

	@Override
	public void compute(XForm xform) {

		switch (context) {
		case 0:
			xform.fpx += weight
					* ((2 * Math.floor(xform.ftx / FX) + 1) * FX - xform.ftx);
			xform.fpy += weight
					* ((2 * Math.floor(xform.fty / FY) + 1) * FY - xform.fty);
			kount0++;
			break;

		case 1:
			xform.fpx += weight * xform.ftx;
			xform.fpy += weight * xform.fty;
			kount1++;
			break;

		case 2:
			xform.fpx += weight * xform.ftx;
			xform.fpy += weight
					* ((2 * Math.floor(xform.fty / FY) + 1) * FY - xform.fty);
			kount2++;
			break;

		case 3:
			xform.fpx += weight
					* ((2 * Math.floor(xform.ftx / FX) + 1) * FX - xform.ftx);
			xform.fpy += weight * xform.fty;
			kount3++;
			break;
		}

	}

	/*****************************************************************************/

} // End of class RectanglesVariation

