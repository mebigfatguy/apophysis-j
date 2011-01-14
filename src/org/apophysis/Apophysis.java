
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


import java.util.Random;

public class Apophysis  implements Constants {

/*****************************************************************************/
//	CONSTANTS

public static void main( String args[] )
{

System.setProperty("com.apple.mrj.application.apple.menu.about.name",
	"Apophysis-j");

try	{
	String title = APPNAME+" "+VERSION;

	Global.randomGenerator = new Random();

	Global.main = new Main(title,"main.xml",660,530);
	Global.editor = new Editor("Editor","editor.xml",760,556);
	Global.adjust = new Adjust("Adjust","adjust.xml",450,380);
	Global.browser = new Browser("Browser","browser.xml",500,350);
	Global.mutate = new Mutate("Mutate","mutate.xml",370,400);
	Global.options = new Options("Options","options.xml",520,400);
	Global.export = new Export("Export Flame","export.xml",420,390);
	Global.script = new Script("Script Editor","script.xml",540,490);
	Global.helper = new Helper("Help","helper.xml",520,520);
	Global.preview = new Preview("Preview","preview.xml",212,180);
	Global.favorites = new Favorites("Favorite Scripts","favorites.xml",400,400);
	Global.fullscreen = new Fullscreen("Full Screen","fullscreen.xml",100,100);
	Global.render = new Render("Render","render.xml",470,470);

	Global.main.show();

	}
catch(Exception ex)
	{
	ex.printStackTrace();
	}

}	//	End of method	main

/*****************************************************************************/


}	//	End of class	Apophysis
