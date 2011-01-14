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

public class Fan2Variation extends Variation {

	/*****************************************************************************/

	double fx;
	double fy;

	double dx, dx2, dy;

	/*****************************************************************************/

	Fan2Variation() {
		pnames = new String[] { "fan2_x", "fan2_y" };

		fx = 2 * Math.random() - 1;
		fy = 2 * Math.random() - 1;
	}

	/*****************************************************************************/

	@Override
	public String getName() {
		return "fan2";
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
			return fx;
		case 1:
			return fy;
		}
		return Double.NaN;
	}

	@Override
	public void setParameterValue(int index, double value) {
		switch (index) {
		case 0:
			fx = value;
			break;
		case 1:
			fy = value;
			break;
		}
	}

	/*****************************************************************************/

	@Override
	public void prepare(XForm xform, double weight) {
		super.prepare(xform, weight);

		dy = fy;
		dx = Math.PI * (fx * fx + 1e-10);
		dx2 = dx / 2;
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
		double a = xform.fangle;
		double r = weight * xform.flength;
		double t = a + dy - dx * (int) ((a + dy) / dx);

		if (t > dx2)
			a = a - dx2;
		else
			a = a + dx2;

		xform.fpx += r * Math.sin(a);
		xform.fpy += r * Math.cos(a);

	}

	/*****************************************************************************/

} // End of class Fan2Variation

