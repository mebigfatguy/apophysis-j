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

public class CurveVariation extends Variation
{
  double xamp = 0.0D;
  double yamp = 0.0D;
  double xlength = 1.0D;
  double ylength = 1.0D;
  double pcxamp;
  double pcyamp;
  double pcxlen;
  double pcylen;

  public CurveVariation()
  {
    this.pnames = new String[] { "curve_xamp", "curve_yamp", "curve_xlength", "curve_ylength" };
  }

  @Override
public int getGroup()
  {
    return 5;
  }

  @Override
public String getName()
  {
    return "curve";
  }

  @Override
public double getParameterValue(int paramInt)
  {
    switch (paramInt) {
    case 0:
      return this.xamp;
    case 1:
      return this.yamp;
    case 2:
      return this.xlength;
    case 3:
      return this.ylength;
    }
    return (0.0D / 0.0D);
  }

  @Override
public void setParameterValue(int paramInt, double paramDouble)
  {
    switch (paramInt) {
    case 0:
      this.xamp = paramDouble; break;
    case 1:
      this.yamp = paramDouble; break;
    case 2:
      this.xlength = paramDouble; break;
    case 3:
      this.ylength = paramDouble;
    }
  }

  @Override
public void prepare(XForm paramXForm, double paramDouble)
  {
    super.prepare(paramXForm, paramDouble);

    this.pcxamp = (paramDouble * this.xamp);
    this.pcyamp = (paramDouble * this.yamp);
    this.pcxlen = (1.0D / Math.max(this.xlength * this.xlength, 1.0E-20D));
    this.pcylen = (1.0D / Math.max(this.ylength * this.ylength, 1.0E-20D));
  }

  @Override
public void compute(XForm paramXForm)
  {
    paramXForm.fpx += this.weight * paramXForm.ftx + this.pcxamp * Math.exp(-paramXForm.fty * paramXForm.fty * this.pcxlen);
    paramXForm.fpy += this.weight * paramXForm.fty + this.pcyamp * Math.exp(-paramXForm.ftx * paramXForm.ftx * this.pcylen);
  }
}
