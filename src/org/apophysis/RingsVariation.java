
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

public class RingsVariation extends Variation {

double dx;

/*****************************************************************************/

public String getName() { return "rings"; }

/*****************************************************************************/

public boolean isSheepCompatible()
{
return true;
}

/*****************************************************************************/


public int getGroup()
{
return 2;
}

/*****************************************************************************/

public boolean needLength()
{
return true;
}

public boolean needSine()
{
return true;
}

/*****************************************************************************/

public void prepare(XForm xform, double weight)
{
super.prepare(xform,weight);

dx = xform.c20*xform.c20 +EPS;
}

/*****************************************************************************/

public void compute(XForm xform)
{
double dy = 2*(int)((xform.flength/dx+1)/2);
double r = weight*(2*xform.flength-dx*(dy+xform.flength));

xform.fpx += r*xform.fcosa;
xform.fpy += r*xform.fsina;
}

/*****************************************************************************/

}	//	End of class	RingsVariation

