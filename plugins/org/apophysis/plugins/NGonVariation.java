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
package org.apophysis.plugins;

import org.apophysis.Variation;
import org.apophysis.XForm;

public class NGonVariation extends Variation
{
  double sides = Math.round(Math.random() * 3.0D + 3.0D);
  double power = Math.random() * 3.0D + 1.0D;
  double circle = Math.random() * 3.0D;
  double corners = 2.0D * Math.random() * this.circle;
  double cpower;
  double csides;

  public NGonVariation()
  {
    this.pnames = new String[] { "ngon_sides", "ngon_power", "ngon_circle", "ngon_corners" };
  }

  @Override
public int getGroup()
  {
    return 4;
  }

  @Override
public String getName()
  {
    return "ngon";
  }

  @Override
public double getParameterValue(int paramInt)
  {
    switch (paramInt) {
    case 0:
      return this.sides;
    case 1:
      return this.power;
    case 2:
      return this.circle;
    case 3:
      return this.corners;
    }
    return (0.0D / 0.0D);
  }

  @Override
public void setParameterValue(int paramInt, double paramDouble)
  {
    switch (paramInt) {
    case 0:
      this.sides = paramDouble; break;
    case 1:
      this.power = paramDouble; break;
    case 2:
      this.circle = paramDouble; break;
    case 3:
      this.corners = paramDouble;
    }
  }

  @Override
public boolean needAngle()
  {
    return true;
  }

  @Override
public void prepare(XForm paramXForm, double paramDouble)
  {
    super.prepare(paramXForm, paramDouble);

    this.cpower = (this.power / 2.0D);
    this.csides = (3.141592653589793D / this.sides);
  }

  @Override
public void compute(XForm paramXForm)
  {
    double d1 = paramXForm.ftx * paramXForm.ftx + paramXForm.fty * paramXForm.fty;
    double d2 = Math.pow(d1, this.cpower);

    double d3 = paramXForm.fangle;
    double d4 = d3 - 2.0D * this.csides * Math.floor(d3 / (2.0D * this.csides));
    if (d4 > this.csides) {
		d4 -= 2.0D * this.csides;
	}

    double d5 = (this.corners * (1.0D / (Math.cos(d4) + 1.0E-300D) - 1.0D) + this.circle) * this.weight / (d2 + 1.0E-300D);

    paramXForm.fpx += d5 * paramXForm.ftx;
    paramXForm.fpy += d5 * paramXForm.fty;
  }
}