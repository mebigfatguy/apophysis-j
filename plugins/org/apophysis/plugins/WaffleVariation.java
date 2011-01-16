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

public class WaffleVariation extends Variation
{
  int slices = 6;
  double xthickness = 0.5D;
  double ythickness = 0.5D;
  double rotation = 0.0D;

  double vcosr = 1.0D;
  double vsinr = 0.0D;

  public WaffleVariation()
  {
    this.pnames = new String[] { "waffle_slices", "waffle_xthickness", "waffle_ythickness", "waffle_rotation" };
  }

  @Override
public int getGroup()
  {
    return 4;
  }

  @Override
public String getName()
  {
    return "waffle";
  }

  @Override
public double getParameterValue(int paramInt)
  {
    switch (paramInt) {
    case 0:
      return this.slices;
    case 1:
      return this.xthickness;
    case 2:
      return this.ythickness;
    case 3:
      return this.rotation;
    }
    return (0.0D / 0.0D);
  }

  @Override
public void setParameterValue(int paramInt, double paramDouble)
  {
    switch (paramInt) {
    case 0:
      this.slices = (int)Math.max(1L, Math.round(paramDouble)); break;
    case 1:
      this.xthickness = Math.max(0.0D, Math.min(1.0D, paramDouble)); break;
    case 2:
      this.ythickness = Math.max(0.0D, Math.min(1.0D, paramDouble)); break;
    case 3:
      this.rotation = paramDouble;
    }
  }

  @Override
public void prepare(XForm paramXForm, double paramDouble)
  {
    super.prepare(paramXForm, paramDouble);

    this.vcosr = (paramDouble * Math.cos(this.rotation));
    this.vsinr = (paramDouble * Math.sin(this.rotation));
  }

  @Override
public void compute(XForm paramXForm)
  {
    double d1 = 0.0D; double d2 = 0.0D;
    int i = (int)(Math.random() * 5.0D);
    switch (i)
    {
    case 0:
      d1 = ((int)(Math.random() * this.slices) + Math.random() * this.xthickness) / this.slices;
      d2 = ((int)(Math.random() * this.slices) + Math.random() * this.ythickness) / this.slices;
      break;
    case 1:
      d1 = ((int)(Math.random() * this.slices) + Math.random()) / this.slices;
      d2 = ((int)(Math.random() * this.slices) + this.ythickness) / this.slices;
      break;
    case 2:
      d1 = ((int)(Math.random() * this.slices) + this.xthickness) / this.slices;
      d2 = ((int)(Math.random() * this.slices) + Math.random()) / this.slices;
      break;
    case 3:
      d1 = Math.random();
      d2 = ((int)(Math.random() * this.slices) + this.ythickness + Math.random() * (1.0D - this.ythickness)) / this.slices;
      break;
    case 4:
      d1 = ((int)(Math.random() * this.slices) + this.xthickness + Math.random() * (1.0D - this.xthickness)) / this.slices;
      d2 = Math.random();
    }

    paramXForm.fpx += this.vcosr * d1 + this.vsinr * d2;
    paramXForm.fpy += this.vcosr * d2 - this.vsinr * d1;
  }
}