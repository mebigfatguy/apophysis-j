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

public class SpirographVariation extends Variation
{
  double a = 3.0D;
  double b = 2.0D;
  double d = 0.0D;
  double tmin = -1.0D;
  double tmax = 1.0D;
  double ymin = -1.0D;
  double ymax = 1.0D;
  double c1 = 0.0D;
  double c2 = 0.0D;

  public SpirographVariation()
  {
    this.pnames = new String[] { "spirograph_a", "spirograph_b", "spirograph_d", "spirograph_tmin", "spirograph_tmax", "spirograph_ymin", "spirograph_ymax", "spirograph_c1", "spirograph_c2" };
  }

  @Override
public int getGroup()
  {
    return 4;
  }

  @Override
public String getName()
  {
    return "spirograph";
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
      return this.d;
    case 3:
      return this.tmin;
    case 4:
      return this.tmax;
    case 5:
      return this.ymin;
    case 6:
      return this.ymax;
    case 7:
      return this.c1;
    case 8:
      return this.c2;
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
      this.d = paramDouble; break;
    case 3:
      this.tmin = paramDouble; break;
    case 4:
      this.tmax = paramDouble; break;
    case 5:
      this.ymin = paramDouble; break;
    case 6:
      this.ymax = paramDouble; break;
    case 7:
      this.c1 = paramDouble; break;
    case 8:
      this.c2 = paramDouble;
    }
  }

  @Override
public void compute(XForm paramXForm)
  {
    double d1 = (this.tmax - this.tmin) * Math.random() + this.tmin;
    double d2 = (this.ymax - this.ymin) * Math.random() + this.ymin;
    double d3 = (this.a + this.b) * Math.cos(d1) - this.c1 * Math.cos((this.a + this.b) / this.b * d1);
    double d4 = (this.a + this.b) * Math.sin(d1) - this.c2 * Math.cos((this.a + this.b) / this.b + d1);

    paramXForm.fpx += this.weight * (d3 + this.d * Math.cos(d1) + d2);
    paramXForm.fpy += this.weight * (d4 + this.d * Math.sin(d1) + d2);
  }
}