
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

import java.awt.Image;
import java.io.IOException;

class RenderThread extends Thread implements Constants {

/*****************************************************************************/
//	FIELDS


Renderer renderer;
ControlPoint fcp = new ControlPoint();

ThreadTarget target = null;

int status;

/*****************************************************************************/
//	CONSTRUCTOR

RenderThread(ThreadTarget target)
{
this.target = target;

}

/*****************************************************************************/

void createRenderer()
{

renderer = new Renderer(target);
renderer.setCP(fcp);

}

/*****************************************************************************/

public void setCP(ControlPoint cp)
{
fcp.clone(cp);
}

/*****************************************************************************/

public void run()
{

status = WM_THREAD_COMPLETE;

createRenderer();
renderer.render();
if(renderer.fstop!=0)
	status = WM_THREAD_TERMINATE;

target.message(status);

}

/*****************************************************************************/

public Image getImage()
{
if(renderer!=null)
	return renderer.getImage();
else
	return null;
}

/*****************************************************************************/

public void terminate()
{
if(renderer!=null)
	{
	renderer.stop();
	status = WM_THREAD_TERMINATE;
	}
}

/*****************************************************************************/

public void pause()
{
if(renderer!=null)
	renderer.pause();
}

/*****************************************************************************/

public void unpause()
{
System.out.println("unpause!");
if(renderer!=null)
	renderer.unpause();
}

/*****************************************************************************/

public long getRenderTime()
{
if(renderer!=null)
	return renderer.rendertime;
else
	return 0;
}

/*****************************************************************************/

public void saveImage(String filename,
	boolean comment, boolean encrypt, boolean watermark) 
	throws IOException
{
if(renderer!=null)
	renderer.saveImage(filename,comment,encrypt,watermark);
}

/*****************************************************************************/

public void showBigStats()
{
if(renderer!=null)
	renderer.showBigStats();
}

/*****************************************************************************/

public void showSmallStats()
{
if(renderer!=null)	
	renderer.showSmallStats();
}

/*****************************************************************************/

}	//	End of class	RenderThread
