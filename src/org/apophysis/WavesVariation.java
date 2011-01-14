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

public class WavesVariation extends Variation {

	double waves_f1, waves_f2;

	/*****************************************************************************/

	@Override
	public String getName() {
		return "waves";
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
	public void prepare(XForm xform, double weight) {
		super.prepare(xform, weight);

		waves_f1 = 1.0 / (xform.c20 * xform.c20 + EPS);
		waves_f2 = 1.0 / (xform.c21 * xform.c21 + EPS);
	}

	/*****************************************************************************/

	@Override
	public void compute(XForm xform) {
		xform.fpx += weight
				* (xform.ftx + xform.c10 * Math.sin(xform.fty * waves_f1));
		xform.fpy += weight
				* (xform.fty + xform.c11 * Math.sin(xform.ftx * waves_f2));
	}

	/*****************************************************************************/

} // End of class WavesVariation

