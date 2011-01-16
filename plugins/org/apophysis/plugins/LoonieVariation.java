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

public class LoonieVariation extends Variation
{
  double weight2;

  @Override
public int getGroup()
  {
    return 5;
  }

  @Override
public String getName()
  {
    return "loonie";
  }

  @Override
public void prepare(XForm paramXForm, double paramDouble)
  {
    super.prepare(paramXForm, paramDouble);

    this.weight2 = (paramDouble * paramDouble);
  }

  @Override
public void compute(XForm paramXForm)
  {
    double d1 = paramXForm.ftx * paramXForm.ftx + paramXForm.fty * paramXForm.fty;

    if (d1 < this.weight2)
    {
      double d2 = Math.sqrt(this.weight2 / d1 - 1.0D);
      paramXForm.fpx += this.weight * d2 * paramXForm.ftx;
      paramXForm.fpy += this.weight * d2 * paramXForm.fty;
    }
    else
    {
      paramXForm.fpx += this.weight * paramXForm.ftx;
      paramXForm.fpy += this.weight * paramXForm.fty;
    }
  }
}