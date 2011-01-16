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

public class BoardersVariation extends Variation
{
  @Override
public int getGroup()
  {
    return 5;
  }

  @Override
public String getName()
  {
    return "boarders";
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
    double d1 = Math.round(paramXForm.ftx);
    double d2 = Math.round(paramXForm.fty);
    double d3 = paramXForm.ftx - d1;
    double d4 = paramXForm.fty - d2;

    if (Math.random() >= 0.75D)
    {
      paramXForm.fpx += this.weight * (d3 * 0.5D + d1);
      paramXForm.fpy += this.weight * (d4 * 0.5D + d2);
    }
    else if (Math.abs(d3) >= Math.abs(d4))
    {
      if (d3 > 0.0D)
      {
        paramXForm.fpx += this.weight * (d3 * 0.5D + d1 + 0.25D);
        paramXForm.fpy += this.weight * (d4 * 0.5D + d2 + 0.25D * d4 / d3);
      }
      else if (d3 < 0.0D)
      {
        paramXForm.fpx += this.weight * (d3 * 0.5D + d1 - 0.25D);
        paramXForm.fpy += this.weight * (d4 * 0.5D + d2 - 0.25D * d4 / d3);
      }

    }
    else if (d4 > 0.0D)
    {
      paramXForm.fpx += this.weight * (d3 * 0.5D + d1 + 0.25D * d3 / d4);
      paramXForm.fpy += this.weight * (d4 * 0.5D + d2 + 0.25D);
    }
    else if (d4 < 0.0D)
    {
      paramXForm.fpx += this.weight * (d3 * 0.5D + d1 - 0.25D * d3 / d4);
      paramXForm.fpy += this.weight * (d4 * 0.5D + d2 - 0.25D);
    }
  }
}
