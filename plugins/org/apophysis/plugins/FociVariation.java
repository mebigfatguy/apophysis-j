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

public class FociVariation extends Variation
{
  @Override
public int getGroup()
  {
    return 5;
  }

  @Override
public String getName()
  {
    return "foci";
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
public void compute(XForm paramXForm)
  {
    double d1 = 0.5D * Math.exp(paramXForm.ftx);
    double d2 = 0.25D / d1;
    double d3 = Math.sin(paramXForm.fty);
    double d4 = Math.cos(paramXForm.fty);
    double d5 = this.weight / (d1 + d2 - d4);

    paramXForm.fpx += (d1 - d2) * d5;
    paramXForm.fpy += d3 * d5;
  }
}