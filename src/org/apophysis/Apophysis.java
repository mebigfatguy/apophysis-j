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

public class Apophysis implements Constants {

    /*****************************************************************************/
    // CONSTANTS

    public static void main(String args[]) {

        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Apophysis-j");

        try {
            String title = APPNAME + " " + VERSION;

            Global.randomGenerator = new Random();

            Global.main = new Main(title, "/org/apophysis/thinletxml/main.xml", 800, 600);
            Global.editor = new Editor("Editor", "/org/apophysis/thinletxml/editor.xml", 760, 556);
            Global.adjust = new Adjust("Adjust", "/org/apophysis/thinletxml/adjust.xml", 450, 380);
            Global.browser = new Browser("Browser", "/org/apophysis/thinletxml/browser.xml", 500, 350);
            Global.mutate = new Mutate("Mutate", "/org/apophysis/thinletxml/mutate.xml", 370, 400);
            Global.options = new Options("Options", "/org/apophysis/thinletxml/options.xml", 520, 400);
            Global.export = new Export("Export Flame", "/org/apophysis/thinletxml/export.xml", 420, 390);
            Global.script = new Script("Script Editor", "/org/apophysis/thinletxml/script.xml", 540, 490);
            Global.helper = new Helper("Help", "/org/apophysis/thinletxml/helper.xml", 520, 520);
            Global.preview = new Preview("Preview", "/org/apophysis/thinletxml/preview.xml", 212, 180);
            Global.favorites = new Favorites("Favorite Scripts", "/org/apophysis/thinletxml/favorites.xml", 400, 400);
            Global.fullscreen = new Fullscreen("Full Screen", "/org/apophysis/thinletxml/fullscreen.xml", 100, 100);
            Global.render = new Render("Render", "/org/apophysis/thinletxml/render.xml", 470, 470);

            Global.main.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    } // End of method main

    /*****************************************************************************/

} // End of class Apophysis
