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

public class ExVariation extends Variation {

	/*****************************************************************************/

	@Override
	public String getName() {
		return "ex";
	}

	/*****************************************************************************/

	@Override
	public boolean isSheepCompatible() {
		return true;
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
		double r = xform.flength;
		double n0 = Math.sin(xform.fangle + r);
		double n1 = Math.cos(xform.fangle - r);
		double m0 = n0 * n0 * n0;
		double m1 = n1 * n1 * n1;
		r = r * weight;

		xform.fpx += r * (m0 + m1);
		xform.fpy += r * (m0 - m1);
	}

	/*****************************************************************************/

} // End of class ExVariation

