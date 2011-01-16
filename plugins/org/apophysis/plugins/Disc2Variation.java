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

public class Disc2Variation extends Variation
{
  double rot = 0.5D * Math.random();
  double twist = 0.5D * Math.random();
  double rotpi;
  double sint;
  double cost;
  double twopi;

  public Disc2Variation()
  {
    this.pnames = new String[] { "disc2_rot", "disc2_twist" };
  }

  @Override
public int getGroup()
  {
    return 6;
  }

  @Override
public String getName()
  {
    return "disc2";
  }

  @Override
public double getParameterValue(int paramInt)
  {
    switch (paramInt) {
    case 0:
      return this.rot;
    case 1:
      return this.twist;
    }
    return (0.0D / 0.0D);
  }

  @Override
public void setParameterValue(int paramInt, double paramDouble)
  {
    switch (paramInt) {
    case 0:
      this.rot = paramDouble; break;
    case 1:
      this.twist = paramDouble;
    }
  }

  @Override
public void prepare(XForm paramXForm, double paramDouble)
  {
    super.prepare(paramXForm, paramDouble);

    this.rotpi = (this.rot * 3.141592653589793D);
    this.twopi = 6.283185307179586D;
    this.sint = Math.sin(this.twist);
    this.cost = (Math.cos(this.twist) - 1.0D);
    double d;
    if (this.twist > this.twopi)
    {
      d = 1.0D + this.twist - this.twopi;
      this.sint *= d;
      this.cost *= d;
    }

    if (this.twist < -this.twopi)
    {
      d = 1.0D + this.twist + this.twopi;
      this.sint *= d;
      this.cost *= d;
    }
  }

  @Override
public boolean needAngle()
  {
    return true;
  }

  @Override
public void compute(XForm paramXForm)
  {
    double d1 = this.rotpi * (paramXForm.ftx + paramXForm.fty);
    double d2 = Math.sin(d1);
    double d3 = Math.cos(d1);
    d1 = this.weight * paramXForm.fangle / 3.141592653589793D;

    paramXForm.fpx += (d2 + this.cost) * d1;
    paramXForm.fpy += (d3 + this.sint) * d1;
  }
}