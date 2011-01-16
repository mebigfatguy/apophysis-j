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

public class EdiscVariation extends Variation
{
  double evar;

  @Override
public int getGroup()
  {
    return 5;
  }

  @Override
public String getName()
  {
    return "edisc";
  }

  @Override
public double getParameterValue(int paramInt)
  {
    return (0.0D / 0.0D);
  }

  @Override
public void setParameterValue(int paramInt, double paramDouble)
  {
  }

  @Override
public void prepare(XForm paramXForm, double paramDouble)
  {
    super.prepare(paramXForm, paramDouble);

    this.evar = (paramDouble / 11.570346320000001D);
  }

  @Override
public void compute(XForm paramXForm)
  {
    double d1 = paramXForm.ftx * paramXForm.ftx + paramXForm.fty * paramXForm.fty + 1.0D;
    double d2 = 2.0D * paramXForm.ftx;
    double d3 = Math.sqrt(d1 + d2);
    double d4 = Math.sqrt(d1 - d2);
    double d5 = (d3 + d4) * 0.5D;

    double d6 = Math.log(d5 + Math.sqrt(d5 - 1.0D));
    double d7 = Math.sin(d6);
    double d8 = Math.cos(d6);

    double d9 = -Math.acos(paramXForm.ftx / d5);
    double d10 = Math.exp(d9);
    double d11 = Math.exp(-d9);
    double d12 = (d10 - d11) * 0.5D;
    double d13 = (d10 + d11) * 0.5D;

    if (paramXForm.fty > 0.0D) {
      d7 = -d7;
    }
    paramXForm.fpx += this.evar * d13 * d8;
    paramXForm.fpy += this.evar * d12 * d7;
  }
}