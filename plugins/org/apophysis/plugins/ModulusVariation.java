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

public class ModulusVariation extends Variation
{
  double x = 1.0D;
  double y = 1.0D;
  double x2;
  double y2;

  public ModulusVariation()
  {
    this.pnames = new String[] { "modulus_x", "modulus_y" };
  }

  @Override
public int getGroup()
  {
    return 5;
  }

  @Override
public String getName()
  {
    return "modulus";
  }

  @Override
public double getParameterValue(int paramInt)
  {
    switch (paramInt) {
    case 0:
      return this.x;
    case 1:
      return this.y;
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
      this.y = paramDouble;
    }
  }

  @Override
public void prepare(XForm paramXForm, double paramDouble)
  {
    super.prepare(paramXForm, paramDouble);

    this.x2 = (2.0D * this.x);
    this.y2 = (2.0D * this.y);
  }

  @Override
public void compute(XForm paramXForm)
  {
    if (paramXForm.ftx > this.x) {
		paramXForm.fpx += this.weight * (-this.x + mod(paramXForm.ftx + this.x, this.x2));
	} else if (paramXForm.ftx < -this.x) {
		paramXForm.fpx += this.weight * (this.x - mod(this.x - paramXForm.ftx, this.x2));
	} else {
      paramXForm.fpx += this.weight * paramXForm.ftx;
    }
    if (paramXForm.fty > this.y) {
		paramXForm.fpy += this.weight * (-this.y + mod(paramXForm.fty + this.y, this.y2));
	} else if (paramXForm.fty < -this.y) {
		paramXForm.fpy += this.weight * (this.y - mod(this.y - paramXForm.fty, this.y2));
	} else {
		paramXForm.fpy += this.weight * paramXForm.fty;
	}
  }

  double mod(double paramDouble1, double paramDouble2)
  {
    if (paramDouble2 == 0.0D) {
		return 0.0D;
	}
    int i = (int)(paramDouble1 / paramDouble2);
    return paramDouble1 - i * paramDouble2;
  }
}
