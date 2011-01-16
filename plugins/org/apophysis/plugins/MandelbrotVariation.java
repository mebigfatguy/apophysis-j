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

public class MandelbrotVariation extends Variation
{
  int maxiter = 100;
  double xmin = -1.0D;
  double xmax = 1.0D;
  double ymin = -1.0D;
  double ymax = 1.0D;
  double cx = 0.0D;
  double cy = 0.0D;
  double invertprob = 0.0D;
  double skin = 1.0D;

  double x0 = 0.0D;
  double y0 = 0.0D;

  public MandelbrotVariation()
  {
    this.pnames = new String[] { "mandelbrot_iter", "mandelbrot_xmin", "mandelbrot_xmax", "mandelbrot_ymin", "mandelbrot_ymax", "mandelbrot_invert", "mandelbrot_skin", "mandelbrot_cx", "mandelbrot_cy" };
  }

  @Override
public int getGroup()
  {
    return 4;
  }

  @Override
public String getName()
  {
    return "mandelbrot";
  }

  @Override
public double getParameterValue(int paramInt)
  {
    switch (paramInt) {
    case 0:
      return this.maxiter;
    case 1:
      return this.xmin;
    case 2:
      return this.xmax;
    case 3:
      return this.ymin;
    case 4:
      return this.ymax;
    case 5:
      return this.invertprob;
    case 6:
      return this.skin;
    case 7:
      return this.cx;
    case 8:
      return this.cy;
    }
    return (0.0D / 0.0D);
  }

  @Override
public void setParameterValue(int paramInt, double paramDouble)
  {
    switch (paramInt) {
    case 0:
      this.maxiter = (int)Math.max(5L, Math.round(paramDouble)); break;
    case 1:
      this.xmin = paramDouble; break;
    case 2:
      this.xmax = paramDouble; break;
    case 3:
      this.ymin = paramDouble; break;
    case 4:
      this.ymax = paramDouble; break;
    case 5:
      this.invertprob = Math.max(0.0D, Math.min(1.0D, paramDouble)); break;
    case 6:
      this.skin = Math.max(0.0D, Math.min(1.0D, paramDouble)); break;
    case 7:
      this.cx = paramDouble; break;
    case 8:
      this.cy = paramDouble;
    }
  }

  @Override
public void compute(XForm paramXForm)
  {
    double d2 = this.x0; double d3 = this.x0; double d4 = this.y0; double d5 = this.y0;
    int i = Math.random() < this.invertprob ? 1 : 0;
    int j = i != 0 ? 0 : this.maxiter;

    while (((i != 0) && (j < this.maxiter)) || ((i == 0) && ((j >= this.maxiter) || ((this.skin < 1.0D) && (j < 0.1D * this.maxiter * (1.0D - this.skin))))))
    {
      if ((this.x0 == 0.0D) && (this.y0 == 0.0D))
      {
        this.x0 = ((this.xmax - this.xmin) * Math.random() + this.xmin);
        this.y0 = ((this.ymax - this.ymin) * Math.random() + this.ymin);
      }
      else
      {
        this.x0 = ((this.skin + 0.001D) * (Math.random() - 0.5D) + this.x0);
        this.y0 = ((this.skin + 0.001D) * (Math.random() - 0.5D) + this.y0);
      }
      d3 = this.x0;
      d5 = this.y0;
      d2 = this.x0;
      d4 = this.y0;
      j = 0;
      while ((d2 * d2 + d4 * d4 < 4.0D) && (j < this.maxiter))
      {
        double d1 = d2 * d2 - d4 * d4 + this.x0;
        d4 = 2.0D * d2 * d4 + this.y0;
        d2 = d1;
        j++;
      }
      if ((j <= this.maxiter) && (this.skin != 1.0D) && (j >= 0.1D * (this.maxiter * (1.0D - this.skin)))) {
        continue;
      }
      this.x0 = 0.0D;
      this.y0 = 0.0D;
    }

    paramXForm.fpx += this.weight * (d3 + this.cx * d2);
    paramXForm.fpy += this.weight * (d5 + this.cy * d4);
  }
}