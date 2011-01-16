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

public class FlowerVariation extends Variation
{
  double petals = 2.0D;
  double holes = 1.0D;

  public FlowerVariation()
  {
    this.pnames = new String[] { "flower_petals", "flower_holes" };
  }

  @Override
public int getGroup()
  {
    return 4;
  }

  @Override
public String getName()
  {
    return "flower";
  }

  @Override
public double getParameterValue(int paramInt)
  {
    switch (paramInt) {
    case 0:
      return this.petals;
    case 1:
      return this.holes;
    }
    return (0.0D / 0.0D);
  }

  @Override
public void setParameterValue(int paramInt, double paramDouble)
  {
    switch (paramInt) {
    case 0:
      this.petals = paramDouble; break;
    case 1:
      this.holes = paramDouble;
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
    double d = (Math.random() - this.holes) * Math.cos(this.petals * paramXForm.fangle);
    paramXForm.fpx += this.weight * d * Math.cos(paramXForm.fangle);
    paramXForm.fpy += this.weight * d * Math.sin(paramXForm.fangle);
  }
}