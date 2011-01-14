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

public class SupershapeVariation extends Variation {

	/*****************************************************************************/

	double m, n1, n2, n3, rnd, holes;

	/*****************************************************************************/

	SupershapeVariation() {
		pnames = new String[] { "super_shape_m", "super_shape_n1",
				"super_shape_n2", "super_shape_n3", "super_shape_rnd",
				"super_shape_holes" };

		m = 5.0;
		n1 = 2.0;
		n2 = 0.3;
		n3 = 0.3;
		rnd = 0.0;
		holes = 1.0;

	}

	/*****************************************************************************/

	@Override
	public String getName() {
		return "super_shape";
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
			return m;
		case 1:
			return n1;
		case 2:
			return n2;
		case 3:
			return n3;
		case 4:
			return rnd;
		case 5:
			return holes;
		}
		return Double.NaN;
	}

	@Override
	public void setParameterValue(int index, double value) {
		switch (index) {
		case 0:
			m = value;
			break;
		case 1:
			n1 = value;
			break;
		case 2:
			n2 = value;
			break;
		case 3:
			n3 = value;
			break;
		case 4:
			rnd = value;
			break;
		case 5:
			holes = value;
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
	public boolean needLength() {
		return true;
	}

	/*****************************************************************************/

	@Override
	public void compute(XForm xform) {

		double r, theta, dist, t1a, t2a, t1, t2, a;

		theta = 0.0;
		if (n1 == 0)
			r = 0;
		else {
			theta = Math.atan2(xform.fty, xform.ftx + 1e-50);
			a = (m * theta + Math.PI) / 4;
			t2 = Math.sin(a);
			t1 = Math.cos(a);

			t1 = Math.pow(Math.abs(t1), n2);
			t2 = Math.pow(Math.abs(t2), n3);

			if (rnd < 1.0)
				dist = xform.flength;
			else
				dist = 0;

			r = (rnd * Math.random() + (1 - rnd) * dist - holes)
					* Math.pow(t1 + t2, -1.0 / n1);
		}

		if (Math.abs(r) != 0) {
			t2a = Math.sin(theta);
			t1a = Math.cos(theta);

			xform.fpx += weight * r * t1a;
			xform.fpy += weight * r * t2a;
		}
	}

	/*****************************************************************************/

} // End of class SupershapeVariation

