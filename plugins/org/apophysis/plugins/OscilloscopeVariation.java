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

public class OscilloscopeVariation extends Variation
{
  double separation = 1.0D;
  double frequency = 3.141592653589793D;
  double amplitude = 1.0D;
  double damping = 0.0D;
  double freq2pi;

  public OscilloscopeVariation()
  {
    this.pnames = new String[] { "oscope_separation", "oscope_frequency", "oscope_amplitude", "oscope_damping" };
  }

  @Override
public int getGroup()
  {
    return 5;
  }

  @Override
public String getName()
  {
    return "oscilloscope";
  }

  @Override
public double getParameterValue(int paramInt)
  {
    switch (paramInt) {
    case 0:
      return this.separation;
    case 1:
      return this.frequency;
    case 2:
      return this.amplitude;
    case 3:
      return this.damping;
    }
    return (0.0D / 0.0D);
  }

  @Override
public void setParameterValue(int paramInt, double paramDouble)
  {
    switch (paramInt) {
    case 0:
      this.separation = paramDouble; break;
    case 1:
      this.frequency = paramDouble; break;
    case 2:
      this.amplitude = paramDouble; break;
    case 3:
      this.damping = paramDouble;
    }
  }

  @Override
public void prepare(XForm paramXForm, double paramDouble)
  {
    super.prepare(paramXForm, paramDouble);

    this.freq2pi = (this.frequency * 2.0D * 3.141592653589793D);
  }

  @Override
public void compute(XForm paramXForm)
  {
    double d;
    if (this.damping == 0.0D)
    {
      d = this.amplitude * Math.cos(this.freq2pi * paramXForm.ftx) + this.separation;
    }
    else
    {
      d = this.amplitude * Math.exp(-Math.abs(paramXForm.ftx) * this.damping) * Math.cos(this.freq2pi * paramXForm.ftx) + this.separation;
    }

    paramXForm.fpx += this.weight * paramXForm.ftx;

    if (Math.abs(paramXForm.fty) <= d) {
		paramXForm.fpy -= this.weight * paramXForm.fty;
	} else {
		paramXForm.fpy += this.weight * paramXForm.fty;
	}
  }
}
