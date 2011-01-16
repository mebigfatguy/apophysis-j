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

public class Popcorn2Variation extends Variation
{
  double x = 0.1D;
  double y = 0.1D;
  double c = 3.0D;

  public Popcorn2Variation()
  {
    this.pnames = new String[] { "popcorn2_x", "popcorn2_y", "popcorn2_c" };
  }

  @Override
public int getGroup()
  {
    return 5;
  }

  @Override
public String getName()
  {
    return "popcorn2";
  }

  @Override
public double getParameterValue(int paramInt)
  {
    switch (paramInt) {
    case 0:
      return this.x;
    case 1:
      return this.y;
    case 2:
      return this.c;
    }
    return (0.0D / 0.0D);
  }

  @Override
public void setParameterValue(int paramInt, double paramDouble)
  {
    switch (paramInt) {
    case 0:
      this.x = paramDouble; break;
    case 1:
      this.y = paramDouble; break;
    case 2:
      this.c = paramDouble;
    }
  }

  @Override
public void compute(XForm paramXForm)
  {
    paramXForm.fpx += this.weight * (paramXForm.ftx + this.x * Math.sin(Math.tan(this.c * paramXForm.fty)));
    paramXForm.fpy += this.weight * (paramXForm.fty + this.y * Math.sin(Math.tan(this.c * paramXForm.ftx)));
  }
}