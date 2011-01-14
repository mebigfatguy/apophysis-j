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

public class FanVariation extends Variation {

	double fan_dx, fan_dx2;

	/*****************************************************************************/

	@Override
	public String getName() {
		return "fan";
	}

	/*****************************************************************************/

	@Override
	public boolean isSheepCompatible() {
		return true;
	}

	/*****************************************************************************/

	@Override
	public int getGroup() {
		return 2;
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
	public void prepare(XForm xform, double weight) {
		super.prepare(xform, weight);

		fan_dx = Math.PI * (xform.c20 * xform.c20 + EPS);
		fan_dx2 = fan_dx / 2.0;
	}

	/*****************************************************************************/

	@Override
	public void compute(XForm xform) {
		double a;
		double b = (xform.fangle + xform.c21) / fan_dx;

		if (b < 0)
			a = xform.fangle + fan_dx2;
		else if (b - Math.floor(b) > 0.5)
			a = xform.fangle - fan_dx2;
		else
			a = xform.fangle + fan_dx2;

		double cosa = Math.cos(a);
		double sina = Math.sin(a);
		double r = weight * xform.flength;

		xform.fpx += r * cosa;
		xform.fpy += r * sina;

	}

	/*****************************************************************************/

} // End of class FanVariation

