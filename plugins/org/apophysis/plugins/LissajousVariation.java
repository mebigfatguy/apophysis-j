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

public class LissajousVariation extends Variation
{
  double a = 3.0D;
  double b = 2.0D;
  double c = 0.0D;
  double d = 0.0D;
  double e = 0.0D;
  double tmin = -3.141592653589793D;
  double tmax = 3.141592653589793D;

  public LissajousVariation()
  {
    this.pnames = new String[] { "lissajous_a", "lissajous_b", "lissajous_c", "lissajous_d", "lissajous_e", "lissajous_tmin", "lissajous_tmax" };
  }

  @Override
public int getGroup()
  {
    return 4;
  }

  @Override
public String getName()
  {
    return "lissajous";
  }

  @Override
public double getParameterValue(int paramInt)
  {
    switch (paramInt) {
    case 0:
      return this.a;
    case 1:
      return this.b;
    case 2:
      return this.c;
    case 3:
      return this.d;
    case 4:
      return this.e;
    case 5:
      return this.tmin;
    case 6:
      return this.tmax;
    }
    return (0.0D / 0.0D);
  }

  @Override
public void setParameterValue(int paramInt, double paramDouble)
  {
    switch (paramInt) {
    case 0:
      this.a = paramDouble; break;
    case 1:
      this.b = paramDouble; break;
    case 2:
      this.c = paramDouble; break;
    case 3:
      this.d = paramDouble; break;
    case 4:
      this.e = paramDouble; break;
    case 5:
      this.tmin = paramDouble; break;
    case 6:
      this.tmax = paramDouble;
    }
  }

  @Override
public void compute(XForm paramXForm)
  {
    double d1 = (this.tmax - this.tmin) * Math.random() + this.tmin;
    double d2 = Math.random() - 0.5D;
    double d3 = Math.sin(this.a * d1 + this.d);
    double d4 = Math.sin(this.b * d1);

    paramXForm.fpx += this.weight * (d3 + this.c * d1 + this.e * d2);
    paramXForm.fpy += this.weight * (d4 + this.c * d1 + this.e * d2);
  }
}