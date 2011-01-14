
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



package org.apophysis;

public class BlobVariation extends Variation {

/*****************************************************************************/

double flow, fhigh, fwaves;
double vlow, vheight;

/*****************************************************************************/

BlobVariation()
{
pnames = new String[]{"blob_low","blob_high","blob_waves"};

fwaves = (int)(5*Math.random()+2+0.5);
flow = 0.2 + 0.5*Math.random();
fhigh = 0.8 + 0.4*Math.random();

}

/*****************************************************************************/

@Override
public String getName() { return "blob"; }

/*****************************************************************************/

@Override
public int getGroup()
{
return 3;
}

/*****************************************************************************/

@Override
public double getParameterValue(int index)
{
switch(index)
	{
	case 0: return flow;
	case 1: return fhigh;
	case 2: return fwaves;
	}
return Double.NaN;
}

@Override
public void setParameterValue(int index, double value)
{
switch(index)
	{
	case 0: flow = value; break;
	case 1: fhigh = value; break;
	case 2: fwaves = value; break;
	}
}

/*****************************************************************************/

@Override
public void prepare(XForm xform, double weight)
{
super.prepare(xform,weight);

vheight = weight*(fhigh-flow)/2;
vlow = weight*flow+vheight;
}

/*****************************************************************************/

@Override
public void compute(XForm xform)
{
double r = vlow + vheight*Math.sin(fwaves*xform.fangle);

xform.fpx += r*xform.ftx;
xform.fpy += r*xform.fty;
}

/*****************************************************************************/

}	//	End of class	BlobVariation

