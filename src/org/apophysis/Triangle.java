
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

public class Triangle {
public double x[] = new double[3];
public double y[] = new double[3];

Triangle()
{
this(0,0,0,0,0,0);
}

Triangle(double x0, double y0, double x1, double y1, double x2, double y2)
{
x[0] = x0;
y[0] = y0;
x[1] = x1;
y[1] = y1;
x[2] = x2;
y[2] = y2;
}

/*****************************************************************************/

public void copy(Triangle t)
{
for(int i=0;i<3;i++)
	{
	this.x[i] = t.x[i];
	this.y[i] = t.y[i];
	}
}

/*****************************************************************************/

public boolean equals(Triangle t)
{
for(int i=0;i<3;i++)
	{
	if(this.x[i]!=t.x[i]) return false;
	if(this.y[i]!=t.y[i]) return false;
	}
return true;
}

/*****************************************************************************/

public void set(double x0, double y0, double x1, double y1, double x2, double y2)
{
x[0] = x0;
y[0] = y0;
x[1] = x1;
y[1] = y1;
x[2] = x2;
y[2] = y2;
}

/*****************************************************************************/

double[] getCentroid()
{
double xy[] = new double[2];

xy[0] = (x[0]+x[1]+x[2])/3.0;
xy[1] = (y[0]+y[1]+y[2])/3.0;

return xy;

}

/*****************************************************************************/

double getArea()
{
double base = dist(x[0],y[0],x[1],y[1]);
double height = line_dist(x[2],y[2],x[1],y[1],x[0],y[0]);
if(base<1.0)
	return height;
else if(height<1.0)
	return base;
else
	return 0.5*base*height;
}

/*****************************************************************************/

double dist(double xa, double ya, double xb, double yb)
{
return Math.sqrt((xa-xb)*(xa-xb)+(ya-yb)*(ya-yb));
}

/*****************************************************************************/

double line_dist(double x, double y, double x1, double y1,
	double x2, double y2)
{
double a,b,e,c;
a = dist(x,y,x1,y1);
b = dist(x,y,x2,y2);
e = dist(x1,y1,x2,y2);
if((a*a+e*e)<(b*b))
	return a;
else if((b*b+e*e)<(a*a))
	return b;
else if(e!=0)
	{
	c = (b*b-a*a-e*e)/(-2*e);
	if((a*a-c*c)<0)
		return 0;
	else
		return Math.sqrt(a*a-c*c);
	}
else
	return a;
	
}

/*****************************************************************************/

public void rotate(double radians)
{
double cosa = Math.cos(radians);
double sina = Math.sin(radians);

for(int i=0;i<3;i++)
	{
	double xx = x[i]*cosa - y[i]*sina;
	double yy = x[i]*sina + y[i]*cosa;
	x[i] = xx;
	y[i] = yy;
	}
}

/*****************************************************************************/

public void rotateCornerAroundPoint(int index, double xr, double yr,
	double radians)
{
double cosa = Math.cos(radians);
double sina = Math.sin(radians);

double xx = xr + (x[index]-xr)*cosa - (y[index]-yr)*sina;
double yy = yr + (x[index]-xr)*sina + (y[index]-yr)*cosa;
x[index] = xx;
y[index] = yy;

}	//	End of method	rotateCornerAroundPoint

/*****************************************************************************/

public void rotateAroundPoint(double xr, double yr, double radians)
{
double cosa = Math.cos(radians);
double sina = Math.sin(radians);

for(int i=0;i<3;i++)
	{
	double xx = xr + (x[i]-xr)*cosa - (y[i]-yr)*sina;
	double yy = yr + (x[i]-xr)*sina + (y[i]-yr)*cosa;
	x[i] = xx;
	y[i] = yy;
	}

}	//	End of method	rotateAroundPoint

/*****************************************************************************/

public void scaleCornerAroundPoint(int index, double xs, double ys,
	double scale)
{
if(scale==0) scale=1e-64;

x[index] = scale*(x[index]-xs)+xs;
y[index] = scale*(y[index]-ys)+ys;

}	//	End of method	scaleCornerAroundPoint

/*****************************************************************************/

public void scaleAroundPoint(double xs, double ys, double scale)
{
if(scale==0) scale=1e-64;

for(int i=0;i<3;i++)
	{
	x[i] = scale*(x[i]-xs)+xs;
	y[i] = scale*(y[i]-ys)+ys;
	}

}	//	End of method	scaleAroundPoint

/*****************************************************************************/

public void move(double dx, double dy)
{
for(int i=0;i<3;i++)
	{	
	x[i] += dx;
	y[i] += dy;
	}
}

/*****************************************************************************/

public void flipVertical()
{
y[0] = -y[0];
y[1] = -y[1];
y[2] = -y[2];	
}

/*****************************************************************************/

public void flipHorizontal()
{
x[0] = -x[0];
x[1] = -x[1];
x[2] = -x[2];
}

/*****************************************************************************/

public void print()
{
System.out.println(this+"["+
	x[0]+","+y[0]+" "+
	x[1]+","+y[1]+" "+
	x[2]+","+y[2]+"]");
}

/*****************************************************************************/

}
