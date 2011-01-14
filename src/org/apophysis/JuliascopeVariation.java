
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

public class JuliascopeVariation extends Variation {

/*****************************************************************************/


int N;
double c;

int rN;
double cn;

int context = 0;

/*****************************************************************************/

JuliascopeVariation()
{
pnames = new String[]{"juliascope_power","juliascope_dist"};


N = (int)(Math.random()*5) + 2;
c = 1.0;

}

/*****************************************************************************/

public String getName() { return "juliascope"; }

/*****************************************************************************/

public int getGroup()
{
return 3;
}

/*****************************************************************************/

public double getParameterValue(int index)
{
switch(index)
	{
	case 0: return N;
	case 1: return c;
	}
return Double.NaN;
}

public void setParameterValue(int index, double value)
{
switch(index)
	{
	case 0:
		N = (int)(value+0.5);
		if(N==0) N=1;
		break;
	
	case 1:
		c = value;
		break;
	}
}

/*****************************************************************************/

public void prepare(XForm xform, double weight)
{
super.prepare(xform,weight);

rN = Math.abs(N);
cn = c/N/2;

context = 0;
if(c==1) switch(N)
	{
	case -2: context = -2; break;
	case -1: context = -1; break;
	case  1: context =  1; break;	
	case  2: context =  2; break;
	}

}

/*****************************************************************************/

public boolean needLength()
{
return true;
}

/*****************************************************************************/

public void compute(XForm xform)
{
double a,sina,cosa,r;
int rnd;

switch(context)
	{
	case 0:
		rnd = (int)(Math.random()*rN);
		if((rnd&1)==0)
			a = (2*Math.PI*rnd + Math.atan2(xform.fty,xform.ftx+1e-50))/N;
		else
			a = (2*Math.PI*rnd - Math.atan2(xform.fty,xform.ftx+1e-50))/N;
		sina = Math.sin(a);
		cosa = Math.cos(a);

		r = weight*Math.pow(xform.ftx*xform.ftx+xform.fty*xform.fty,cn);
	
		xform.fpx += r*cosa;
		xform.fpy += r*sina;	
		break;

	case -2:
		rnd = (int)(Math.random()*2);
		if(rnd==0)
			a = Math.atan2(xform.fty,xform.ftx+1e-50)/2;
		else
			a = Math.PI-Math.atan2(xform.fty,xform.ftx+1e-50);
		sina = Math.sin(a);
		cosa = Math.cos(a);

		r = weight/Math.sqrt(xform.flength+1e-50);

		xform.fpx += r*cosa;
		xform.fpy -= r*sina;	
		break;


	case -1:
		r = weight/(xform.ftx*xform.ftx+xform.fty*xform.fty+1e-50);
		xform.fpx += r*xform.ftx;
		xform.fpy -= r*xform.fty;
		break;

	case 1:
		xform.fpx += weight*xform.ftx;
		xform.fpy += weight*xform.fty;
		break;

	case 2:
		rnd = (int)(Math.random()*2);
		if(rnd==0)
			a = Math.atan2(xform.fty,xform.ftx+1e-50)/2;
		else
			a = Math.PI - Math.atan2(xform.fty,xform.ftx+1e-50)/2;

		sina = Math.sin(a);
		cosa = Math.cos(a);
	
		r = weight*Math.sqrt(xform.flength);

		xform.fpx += r*cosa;
		xform.fpy += r*sina;	
		break;

		
	}
}

/*****************************************************************************/

}	//	End of class	JuliascopeVariation

