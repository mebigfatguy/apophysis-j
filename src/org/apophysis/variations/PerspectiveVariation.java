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

public class PerspectiveVariation extends Variation {

	/*****************************************************************************/

	double angle, focus;
	double vsin, vf, vfcos;
	int context;

	/*****************************************************************************/

	public PerspectiveVariation() {
		pnames = new String[] { "perspective_angle", "perspective_dist" };

		angle = Math.random();
		focus = 2 * Math.random() + 1;

	}

	/*****************************************************************************/

	@Override
	public String getName() {
		return "perspective";
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
		case 1:
			return focus;
		}
		return Double.NaN;
	}

	@Override
	public void setParameterValue(int index, double value) {
		switch (index) {
		case 0:
			angle = value;
			break;
		case 1:
			focus = value;
			break;
		}
	}

	/*****************************************************************************/

	@Override
	public void prepare(XForm xform, double weight) {
		super.prepare(xform, weight);

		vsin = Math.sin(angle * Math.PI / 2);
		vf = weight * focus;
		vfcos = vf * Math.cos(angle * Math.PI / 2);

		if (angle == 0) {
			context = 0;
		} else {
			context = 1;
		}
	}

	/*****************************************************************************/

	@Override
	public boolean needAngle() {
		return true;
	}

	/*****************************************************************************/

	@Override
	public void compute(XForm xform) {

		if (context == 0) {
			xform.fpx += weight * xform.ftx;
			xform.fpy += weight * xform.fty;
		} else {
			double t = (focus - vsin * xform.fty);
			xform.fpx += vf * xform.ftx / t;
			xform.fpy += vfcos * xform.fty / t;
		}

	}

	/*****************************************************************************/

} // End of class PerspectiveVariation

