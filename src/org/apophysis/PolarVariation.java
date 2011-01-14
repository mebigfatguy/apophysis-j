
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

public class PolarVariation extends Variation {

double polar_vpi;

/*****************************************************************************/

public String getName() { return "polar"; }

/*****************************************************************************/

public boolean isSheepCompatible()
{
return true;
}

/*****************************************************************************/


public boolean needAngle()
{
return true;
}

public boolean needLength()
{
return true;
}

/*****************************************************************************/

public void prepare(XForm xform, double weight)
{
super.prepare(xform,weight);

polar_vpi = weight/Math.PI;
}

/*****************************************************************************/

public void compute(XForm xform)
{

xform.fpx += polar_vpi*xform.fangle;
xform.fpy += weight*(xform.flength-1.0);
}

/*****************************************************************************/

}	//	End of class	PolarVariation

