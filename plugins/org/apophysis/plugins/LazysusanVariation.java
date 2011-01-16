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

public class LazysusanVariation extends Variation
{
  double spin = 3.141592653589793D;
  double space = 0.0D;
  double twist = 0.0D;
  double x = 0.0D;
  double y = 0.0D;

  public LazysusanVariation()
  {
    this.pnames = new String[] { "lazysusan_spin", "lazysusan_space", "lazysusan_twist", "lazysusan_x", "lazysusan_y" };
  }

  @Override
public int getGroup()
  {
    return 5;
  }

  @Override
public String getName()
  {
    return "lazysusan";
  }

  @Override
public double getParameterValue(int paramInt)
  {
    switch (paramInt) {
    case 0:
      return this.spin;
    case 1:
      return this.space;
    case 2:
      return this.twist;
    case 3:
      return this.x;
    case 4:
      return this.y;
    }
    return (0.0D / 0.0D);
  }

  @Override
public void setParameterValue(int paramInt, double paramDouble)
  {
    switch (paramInt) {
    case 0:
      this.spin = Math.min(Math.max(paramDouble, 0.0D), 6.283185307179586D); break;
    case 1:
      this.space = paramDouble; break;
    case 2:
      this.twist = paramDouble; break;
    case 3:
      this.x = paramDouble; break;
    case 4:
      this.y = paramDouble;
    }
  }

  @Override
public void compute(XForm paramXForm)
  {
    double d1 = paramXForm.ftx - this.x;
    double d2 = paramXForm.fty + this.y;

    double d3 = Math.sqrt(d1 * d1 + d2 * d2);

    if (d3 < this.weight)
    {
      double d4 = Math.atan2(d2, d1) + this.spin + this.twist * (this.weight - d3);
      double d5 = Math.sin(d4);
      double d6 = Math.cos(d4);
      paramXForm.fpx += d3 * d6 + this.x;
      paramXForm.fpy += d3 * d5 - this.y;
    }
    else
    {
      d3 = this.weight * (1.0D + this.space / d3);
      paramXForm.fpx += d3 * d1 + this.x;
      paramXForm.fpy += d3 * d2 - this.y;
    }
  }
}