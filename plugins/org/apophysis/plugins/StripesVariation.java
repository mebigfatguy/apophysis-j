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

public class StripesVariation extends Variation
{
  double space = 0.5D;
  double warp = 0.0D;

  public StripesVariation()
  {
    this.pnames = new String[] { "stripes_space", "stripes_warp" };
  }

  @Override
public int getGroup()
  {
    return 5;
  }

  @Override
public String getName()
  {
    return "stripes";
  }

  @Override
public double getParameterValue(int paramInt)
  {
    switch (paramInt) {
    case 0:
      return this.space;
    case 1:
      return this.warp;
    }
    return (0.0D / 0.0D);
  }

  @Override
public void setParameterValue(int paramInt, double paramDouble)
  {
    switch (paramInt) {
    case 0:
      this.space = Math.max(0.0D, Math.min(1.0D, paramDouble)); break;
    case 1:
      this.warp = paramDouble;
    }
  }

  @Override
public void compute(XForm paramXForm)
  {
    double d1 = Math.round(paramXForm.ftx);
    double d2 = paramXForm.ftx - d1;

    paramXForm.fpx += this.weight * (d2 * (1.0D - this.space) + d1);
    paramXForm.fpy += this.weight * (paramXForm.fty + d2 * d2 * this.warp);
  }
}