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

public class PieVariation extends Variation
{
  int slices = 6;
  double thickness = 0.5D;
  double rotation = 0.0D;
  double pi2slices;

  public PieVariation()
  {
    this.pnames = new String[] { "pie_slices", "pie_thickness", "pie_rotation" };
  }

  @Override
public int getGroup()
  {
    return 5;
  }

  @Override
public String getName()
  {
    return "pie";
  }

  @Override
public double getParameterValue(int paramInt)
  {
    switch (paramInt) {
    case 0:
      return this.slices;
    case 1:
      return this.thickness;
    case 2:
      return this.rotation;
    }
    return (0.0D / 0.0D);
  }

  @Override
public void setParameterValue(int paramInt, double paramDouble)
  {
    switch (paramInt) {
    case 0:
      this.slices = Math.max(2, (int)paramDouble); break;
    case 1:
      this.thickness = Math.min(1.0D, Math.max(0.0D, paramDouble)); break;
    case 2:
      this.rotation = Math.min(6.283185307179586D, Math.max(0.0D, paramDouble));
    }
  }

  @Override
public void prepare(XForm paramXForm, double paramDouble)
  {
    super.prepare(paramXForm, paramDouble);

    this.pi2slices = (6.283185307179586D / this.slices);
  }

  @Override
public void compute(XForm paramXForm)
  {
    int i = (int)(Math.random() * this.slices);

    double d1 = this.rotation + this.pi2slices * (i + this.thickness * Math.random());

    double d2 = this.weight * Math.random();

    paramXForm.fpx += d2 * Math.cos(d1);
    paramXForm.fpy += d2 * Math.sin(d1);
  }
}
