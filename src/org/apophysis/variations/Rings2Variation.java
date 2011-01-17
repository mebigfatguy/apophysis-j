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

package org.apophysis.variations;

import org.apophysis.Variation;
import org.apophysis.XForm;

public class Rings2Variation extends Variation {

	double fval;
	double dx;

	/*****************************************************************************/

	public Rings2Variation() {
		pnames = new String[] { "rings2_val" };

		fval = 2 * Math.random();
	}

	/*****************************************************************************/

	@Override
	public String getName() {
		return "rings2";
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
			return fval;
		}
		return Double.NaN;
	}

	@Override
	public void setParameterValue(int index, double value) {
		switch (index) {
		case 0:
			fval = value;
		}
	}

	/*****************************************************************************/

	@Override
	public void prepare(XForm xform, double weight) {
		super.prepare(xform, weight);

		dx = fval * fval + (1e-10);
	}

	/*****************************************************************************/

	@Override
	public boolean needLength() {
		return true;
	}

	/*****************************************************************************/

	@Override
	public void compute(XForm xform) {
		double r = weight
				* (2 - dx
						* ((int) ((xform.flength / dx + 1) / 2.0) * 2
								/ xform.flength + 1));
		xform.fpx += r * xform.ftx;
		xform.fpy += r * xform.fty;
	}

	/*****************************************************************************/

} // End of class Rings2Variation

