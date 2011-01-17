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

public class PDJVariation extends Variation {

	/*****************************************************************************/

	double fa, fb, fc, fd;

	/*****************************************************************************/

	public PDJVariation() {
		pnames = new String[] { "pdj_a", "pdj_b", "pdj_c", "pdj_d" };

		fa = 6 * Math.random() - 3;
		fb = 6 * Math.random() - 3;
		fc = 6 * Math.random() - 3;
		fd = 6 * Math.random() - 3;
	}

	/*****************************************************************************/

	@Override
	public String getName() {
		return "pdj";
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
			return fa;
		case 1:
			return fb;
		case 2:
			return fc;
		case 3:
			return fd;
		}
		return Double.NaN;
	}

	/*****************************************************************************/

	@Override
	public void setParameterValue(int index, double value) {
		switch (index) {
		case 0:
			fa = value;
			break;
		case 1:
			fb = value;
			break;
		case 2:
			fc = value;
			break;
		case 3:
			fd = value;
			break;
		}
	}

	/*****************************************************************************/

	@Override
	public void prepare(XForm xform, double weight) {
		super.prepare(xform, weight);

	}

	/*****************************************************************************/

	@Override
	public void compute(XForm xform) {
		xform.fpx += weight
				* (Math.sin(fa * xform.fty) - Math.cos(fb * xform.ftx));
		xform.fpy += weight
				* (Math.sin(fc * xform.ftx) - Math.cos(fd * xform.fty));
	}

	/*****************************************************************************/

} // End of class NoiseVariation

