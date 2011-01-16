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

public class WedgejuliaVariation extends Variation
{
  double angle = 1.570796326794897D;
  int count = 2;
  int power = 2;
  double dist = 1.0D;
  double compfac;
  double c12pi;
  int rn;
  double cn;

  public WedgejuliaVariation()
  {
    this.pnames = new String[] { "wedge_julia_angle", "wedge_julia_count", "wedge_julia_power", "wedge_julia_dist" };
  }

  @Override
public int getGroup()
  {
    return 5;
  }

  @Override
public String getName()
  {
    return "wedge_julia";
  }

  @Override
public double getParameterValue(int paramInt)
  {
    switch (paramInt) {
    case 0:
      return this.angle;
    case 1:
      return this.count;
    case 2:
      return this.power;
    case 3:
      return this.dist;
    }
    return (0.0D / 0.0D);
  }

  @Override
public void setParameterValue(int paramInt, double paramDouble)
  {
    switch (paramInt) {
    case 0:
      this.angle = paramDouble; break;
    case 1:
      this.count = Math.max(1, (int)paramDouble); break;
    case 2:
      this.power = (int)paramDouble;
      if (this.power != 0) {
		break;
	} this.power = 1; break;
    case 3:
      this.dist = paramDouble;
    }
  }

  @Override
public void prepare(XForm paramXForm, double paramDouble)
  {
    super.prepare(paramXForm, paramDouble);

    this.c12pi = 0.1591549430918954D;
    this.compfac = (1.0D - this.angle * this.count * this.c12pi);
    this.rn = Math.abs(this.power);
    this.cn = (this.dist / this.power / 2.0D);
  }

  @Override
public void compute(XForm paramXForm)
  {
    double d1 = this.weight * Math.pow(paramXForm.ftx * paramXForm.ftx + paramXForm.fty * paramXForm.fty, this.cn);

    int i = (int)(Math.random() * this.rn);
    double d2 = (Math.atan2(paramXForm.fty, paramXForm.ftx) + 6.283185307179586D * i) / this.power;
    double d3 = Math.floor((this.count * d2 + 3.141592653589793D) * this.c12pi);

    d2 = d2 * this.compfac + d3 * this.angle;

    paramXForm.fpx += d1 * Math.cos(d2);
    paramXForm.fpy += d1 * Math.sin(d2);
  }
}