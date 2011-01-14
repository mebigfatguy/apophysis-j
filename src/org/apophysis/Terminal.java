
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

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.InputStream;

class Terminal extends Frame implements Runnable, WindowListener {

/*****************************************************************************/
//  CONSTANTS

static final int iWhite = 0;
static final int iGreen = 1;
static final int iRed = 2;

/*****************************************************************************/
//	FIELDS

Process process = null;

Thread thread = null;

Font font = new Font("Courier",Font.PLAIN,12);

char screen[][];
int icolor[];
Color colors[];

int cheight = 12;	// character height
int iline = 0;
int icol = 0;

char debug[] = {'d','e','b','u','g'};

/*****************************************************************************/

Terminal(String title, Process process, int xpos, int ypos)
{
super(title);

this.process = process;

screen = new char[24][80];
for(int i=0;i<24;i++)
	for(int j=0;j<80;j++)
		screen[i][j] = ' ';

icolor = new int[24];
for(int i=0;i<24;i++)
	icolor[i] = iWhite;

colors = new Color[3];
colors[0] = Color.white;
colors[1] = new Color(10,255,10);
colors[2] = new Color(255,10,10);


setBounds(xpos+20,ypos+20,562,24+cheight*24+4);

setResizable(false);
setVisible(true);

addWindowListener(this);
 
thread = new Thread(this);
thread.start();

}

/*****************************************************************************/

public void update(Graphics g)
{
paint(g);
}

/*****************************************************************************/

public void paint(Graphics g)
{
Rectangle bounds = getBounds();

int w = bounds.width;
int h = bounds.height;

g.setColor(Color.black);
g.fillRect(0,0,w,h);

g.setColor(Color.white);
g.setFont(font);

for(int i=0;i<24;i++)
	{
	g.setColor(colors[icolor[i]]);
	g.drawChars(screen[i],0,80,4,24+cheight+cheight*i);
	}

}

/*****************************************************************************/

public void run()
{

try	{
	InputStream r = process.getErrorStream();
	while(true)
		{
		int k = r.read();
		if(k<0) break;


		char c = (char)k;
		//System.out.print(c);

		if(c=='\n')
			{
			iline++;
			icol = 0;
			if(iline==24)
				{
				scroll();
				iline = 23;
				}	
			}
		else if(c=='\r')
			icol = 0;
		else if((iline<24)&&(icol<80))
			{
			screen[iline][icol] = c;
			icol++;
			if(icol>=80)
				{
				iline++;
				if(iline==24)
					{
					scroll();
					iline = 23;
					}
				icol = 0;
				}
			repaint();
			}
		}
	r.close();
	}
catch(Exception ex)
	{
	ex.printStackTrace();
	}

try	{
	process.waitFor();
	int ix = process.exitValue();
	if(ix==0)
		addLine(iGreen,"Completed");
	else
		addLine(iRed,"Aborted");
	}
catch(Exception ex)
	{
	}

}

/*****************************************************************************/

void scroll()
{
for(int i=0;i<23;i++)
	{
	icolor[i] = icolor[i+1];
	for(int j=0;j<80;j++)
		screen[i][j] = screen[i+1][j];
	}

icolor[23] = iWhite;
for(int j=0;j<80;j++)
	screen[23][j] = ' ';

}

/*****************************************************************************/

void addLine(int ic, String line)
{
if(icol!=0)
	{
	iline++;
	icol = 0;
	if(iline==24)
		{
		scroll();
		iline = 23;
		}
	}	

icolor[iline] = ic;
char cc[] = line.toCharArray();
for(int i=0;i<cc.length;i++)
	screen[iline][i] = cc[i];

repaint();
}

/*****************************************************************************/

public void windowActivated(WindowEvent e) {}
public void windowClosed(WindowEvent e) {}
public void windowDeactivated(WindowEvent e) {}
public void windowDeiconified(WindowEvent e) {}
public void windowIconified(WindowEvent e) {}
public void windowOpened(WindowEvent e) {}

public void windowClosing(WindowEvent e)
{
// kill the process if any
try	{
	process.destroy();
	}
catch(Exception ex)
	{
	}

setVisible(false);
dispose();
}

/*****************************************************************************/

}	//	End of class	Terminal
