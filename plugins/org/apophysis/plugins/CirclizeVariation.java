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

public class CirclizeVariation extends Variation
{
  static final double PI34 = 2.356194490192345D;
  static final double PI4 = 0.7853981633974483D;
  double hole = 0.0D;

  double varpi = 0.0D;

  public CirclizeVariation()
  {
    this.pnames = new String[] { "circlize_hole" };
  }

  @Override
public int getGroup()
  {
    return 5;
  }

  @Override
public String getName()
  {
    return "circlize";
  }

  @Override
public double getParameterValue(int paramInt)
  {
    switch (paramInt) {
    case 0:
      return this.hole;
    }
    return (0.0D / 0.0D);
  }

  @Override
public void setParameterValue(int paramInt, double paramDouble)
  {
    switch (paramInt) {
    case 0:
      this.hole = paramDouble;
    }
  }

  @Override
public void prepare(XForm paramXForm, double paramDouble)
  {
    super.prepare(paramXForm, paramDouble);

    this.varpi = (paramDouble * 4.0D / 3.141592653589793D);
  }

  @Override
public void compute(XForm paramXForm)
  {
    double d4 = Math.abs(paramXForm.ftx);
    double d5 = Math.abs(paramXForm.fty);
    double d6 = Math.atan2(paramXForm.fty, paramXForm.ftx);
    double d3;
    if (d4 > d5) {
		d3 = d4;
	} else {
		d3 = d5;
	}
    double d1;
    if (d6 < -2.356194490192345D) {
		d1 = d5;
	} else if (d6 < -0.7853981633974483D) {
		d1 = 2.0D * d3 + paramXForm.ftx;
	} else if (d6 < 0.7853981633974483D) {
		d1 = 4.0D * d3 + paramXForm.fty;
	} else if (d6 < 2.356194490192345D) {
		d1 = 6.0D * d3 - paramXForm.ftx;
	} else {
      d1 = 8.0D * d3 - paramXForm.fty;
    }
    double d2 = this.varpi * d3 + this.hole;
    double d7 = 0.7853981633974483D * d1 / d3 - 3.141592653589793D;
    double d8 = Math.sin(d7);
    double d9 = Math.cos(d7);

    paramXForm.fpx += d2 * d9;
    paramXForm.fpy += d2 * d8;
  }
}