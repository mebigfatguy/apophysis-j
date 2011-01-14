
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



public abstract class Variation implements Computation {

/*****************************************************************************/
//	CONSTANTS

static public final double EPS = 1e-300;

/*****************************************************************************/
//	FIELDS

public double weight;

public String pnames[] = null;

/*****************************************************************************/

public Variation()
{
}

/*****************************************************************************/

public int getGroup()
{
return 1;
}

/*****************************************************************************/

public boolean isSheepCompatible()
{
int index = XForm.getVariationIndex(getName());
return XForm.sheep[index];
}

/*****************************************************************************/

public abstract String getName();

/*****************************************************************************/

public int getNrParameters()
{
if(pnames==null)
	return 0;
else 
	return pnames.length;
}

/*****************************************************************************/

public double getParameterValue(int index)
{
return Double.NaN;
}

/*****************************************************************************/

public void setParameterValue(int index, double value)
{
}

/*****************************************************************************/

public String getParameterName(int index)
{
if(pnames==null)
	return null;
else if((index<0)||(index>=pnames.length))
	return null;
else
	return pnames[index];
}

/*****************************************************************************/

public boolean needAngle()
{
return false;
}

/*****************************************************************************/

public boolean needLength()
{
return false;
}

/*****************************************************************************/

public boolean needSine()
{
return false;
}

/*****************************************************************************/

public void prepare(XForm xform, double weight)
{
this.weight = weight;
}

/*****************************************************************************/

public abstract void compute(XForm xform);

/*****************************************************************************/

public Variation getNewInstance()
{
Variation variation = null;

try	{
	variation = (Variation)getClass().newInstance();
	}
catch(Exception ex)
	{
	}

return variation;
	
}	//	End of method	getNewInstance

/*****************************************************************************/

}	//	End of class	Variation
