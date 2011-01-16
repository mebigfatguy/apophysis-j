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

public class EscherVariation extends Variation
{
  double beta = 0.0D;
  double c;
  double d;

  public EscherVariation()
  {
    this.pnames = new String[] { "escher_beta" };
  }

  @Override
public int getGroup()
  {
    return 5;
  }

  @Override
public String getName()
  {
    return "escher";
  }

  @Override
public double getParameterValue(int paramInt)
  {
    switch (paramInt) {
    case 0:
      return this.beta;
    }
    return (0.0D / 0.0D);
  }

  @Override
public void setParameterValue(int paramInt, double paramDouble)
  {
    switch (paramInt) {
    case 0:
      this.beta = Math.min(Math.max(paramDouble, -3.141592653589793D), 3.141592653589793D);
    }
  }

  @Override
public void prepare(XForm paramXForm, double paramDouble)
  {
    super.prepare(paramXForm, paramDouble);

    this.d = Math.sin(this.beta);
    this.c = Math.cos(this.beta);
    this.c = (0.5D * (1.0D + this.c));
    this.d = (0.5D * this.d);
  }

  @Override
public void compute(XForm paramXForm)
  {
    double d1 = Math.atan2(paramXForm.fty, paramXForm.ftx);
    double d2 = paramXForm.ftx * paramXForm.ftx + paramXForm.fty * paramXForm.fty;
    double d3 = 0.5D * Math.log(d2);

    double d4 = this.weight * Math.exp(this.c * d3 - this.d * d1);
    double d5 = this.c * d1 + this.d * d3;
    double d6 = Math.sin(d5);
    double d7 = Math.cos(d5);

    paramXForm.fpx += d4 * d7;
    paramXForm.fpy += d4 * d6;
  }
}
