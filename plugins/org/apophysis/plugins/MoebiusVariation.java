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

public class MoebiusVariation extends Variation
{
  double a = 1.0D;
  double b = 1.0D;
  double c = 1.0D;
  double d = 1.0D;

  public MoebiusVariation()
  {
    this.pnames = new String[] { "moebius_a", "moebius_b", "moebius_c", "moebius_d" };
  }

  @Override
public int getGroup()
  {
    return 6;
  }

  @Override
public String getName()
  {
    return "moebius";
  }

  @Override
public double getParameterValue(int paramInt)
  {
    switch (paramInt) {
    case 0:
      return this.a;
    case 1:
      return this.b;
    case 2:
      return this.c;
    case 3:
      return this.d;
    }
    return (0.0D / 0.0D);
  }

  @Override
public void setParameterValue(int paramInt, double paramDouble)
  {
    switch (paramInt) {
    case 0:
      this.a = paramDouble; break;
    case 1:
      this.b = paramDouble; break;
    case 2:
      this.c = paramDouble; break;
    case 3:
      this.d = paramDouble;
    }
  }

  @Override
public void compute(XForm paramXForm)
  {
    double d1 = this.c * paramXForm.ftx + this.d;
    double d2 = this.c * paramXForm.fty;
    double d3 = d1 * d1 + d2 * d2;
    if (d3 == 0.0D) {
		d3 = 1.0E-300D;
	}

    paramXForm.fpx = (((this.a * paramXForm.ftx + this.b) * d1 - this.a * paramXForm.fty * d2) / d3);
    paramXForm.fpy = ((this.a * paramXForm.fty * d1 - this.a * paramXForm.ftx * this.b * d2) / d3);
  }
}