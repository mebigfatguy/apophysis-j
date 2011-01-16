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

public class RaysVariation extends Variation
{
  @Override
public int getGroup()
  {
    return 6;
  }

  @Override
public String getName()
  {
    return "rays";
  }

  @Override
public void compute(XForm paramXForm)
  {
    double d1 = this.weight * Math.random() * 3.141592653589793D;
    double d2 = this.weight / (paramXForm.ftx * paramXForm.ftx + paramXForm.fty * paramXForm.fty + 1.0E-300D);
    double d3 = this.weight * Math.tan(d1) * d2;

    paramXForm.fpx += d3 * Math.cos(paramXForm.ftx);
    paramXForm.fpy += d3 * Math.sin(paramXForm.fty);
  }
}