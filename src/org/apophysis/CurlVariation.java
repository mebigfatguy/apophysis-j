
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

public class CurlVariation extends Variation {

/*****************************************************************************/

double c1;
double c2;

double c2x2;

int context = 0;

/*****************************************************************************/

CurlVariation()
{
pnames = new String[]{"curl_c1","curl_c2"};

c1 = Math.random();
c2 = Math.random();

int i = (int)(Math.random()*3);
if(i==0) {
	c1 = 0.0;
}
if(i==1) {
	c2 = 0.0;
}
}

/*****************************************************************************/

@Override
public String getName() { return "curl"; }

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
	case 0: return c1;
	case 1: return c2;
	}
return Double.NaN;
}

@Override
public void setParameterValue(int index, double value)
{
switch(index)
	{
	case 0: c1 = value; break;
	case 1: c2 = value; break;
	}
}

/*****************************************************************************/

@Override
public void prepare(XForm xform, double weight)
{
super.prepare(xform,weight);

c2x2 = 2*c2;

context = 0;

if(c1==0.0)
	{
	if(c2==0.0) {
		context = 21;
	} else {
		context = 1;
	}
	}
else
	{
	if(c2==0) {
		context = 2;
	} else {
		context = 0;
	}
	}

}

/*****************************************************************************/

@Override
public void compute(XForm xform)
{
double r,re,im;

switch(context)
	{
	case 0:
		re = 1 + c1*xform.ftx + c2*(xform.ftx*xform.ftx - xform.fty*xform.fty);
		im =     c1*xform.fty + c2x2*xform.ftx*xform.fty;

		r = weight/(re*re+im*im);

		xform.fpx += r*(xform.ftx*re + xform.fty*im);
		xform.fpy += r*(xform.fty*re - xform.ftx*im);
		break;

	case 2:
		re = 1 + c1*xform.ftx;
		im =     c1*xform.fty;

		r = weight /(re*re+im*im);

		xform.fpx += r*(xform.ftx*re + xform.fty*im);
		xform.fpy += r*(xform.fty*re - xform.ftx*im);
		break;

	case 1:
		re = 1 + c2*(xform.ftx*xform.ftx - xform.fty*xform.fty);
		im =     c2x2*xform.ftx*xform.fty;

		r = weight/(re*re+im*im);

		xform.fpx += r*(xform.ftx*re + xform.fty*im);
		xform.fpy += r*(xform.fty*re - xform.ftx*im);
		break;

	case 21:
		xform.fpx += weight*xform.ftx;
		xform.fpy += weight*xform.fty;
		break;
	}

}

/*****************************************************************************/

}	//	End of class	CurlVariation

