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

public class WhorlVariation extends Variation
{
  double inside = 1.0D;
  double outside = 1.0D;

  public WhorlVariation()
  {
    this.pnames = new String[] { "whorl_inside", "whorl_outside" };
  }

  @Override
public int getGroup()
  {
    return 5;
  }

  @Override
public String getName()
  {
    return "whorl";
  }

  @Override
public double getParameterValue(int paramInt)
  {
    switch (paramInt) {
    case 0:
      return this.inside;
    case 1:
      return this.outside;
    }
    return (0.0D / 0.0D);
  }

  @Override
public void setParameterValue(int paramInt, double paramDouble)
  {
    switch (paramInt) {
    case 0:
      this.inside = paramDouble; break;
    case 1:
      this.outside = paramDouble;
    }
  }

  @Override
public boolean needLength()
  {
    return true;
  }

  @Override
public void compute(XForm paramXForm)
  {
    double d1 = 0.0D;
    double d2 = paramXForm.flength;

    if (d2 < this.weight) {
		d1 = Math.atan2(paramXForm.fty, paramXForm.ftx) + this.inside / (this.weight - d2);
	} else if (d2 > this.weight) {
		d1 = Math.atan2(paramXForm.fty, paramXForm.ftx) + this.outside / (this.weight - d2);
	} else {
      d2 = 0.0D;
    }
    paramXForm.fpx += this.weight * d2 * Math.cos(d1);
    paramXForm.fpy += this.weight * d2 * Math.sin(d1);
  }
}