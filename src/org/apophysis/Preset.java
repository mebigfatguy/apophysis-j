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

import java.io.PrintWriter;
import java.util.Vector;

public class Preset implements Constants {

	/*****************************************************************************/
	// CONSTANTS

	/*****************************************************************************/
	// FIELDS

	String name;
	int width;
	int height;
	double density;
	double filter_radius;
	int oversample;
	String format;
	boolean limitmem;
	int indexmem;
	int memory;

	/*****************************************************************************/
	// CONSTRUCTORS

	Preset() {
	}

	Preset(Vector v) {
		String line;

		name = (String) v.elementAt(0);

		line = (String) v.elementAt(1);
		width = Integer.parseInt(line);

		line = (String) v.elementAt(2);
		height = Integer.parseInt(line);

		line = (String) v.elementAt(3);
		density = Double.valueOf(line).doubleValue();

		line = (String) v.elementAt(4);
		filter_radius = Double.valueOf(line).doubleValue();

		line = (String) v.elementAt(5);
		oversample = Integer.parseInt(line);

		line = (String) v.elementAt(6);
		format = line;

		line = (String) v.elementAt(7);
		limitmem = line.equals("true");

		line = (String) v.elementAt(8);
		indexmem = Integer.parseInt(line);

		line = (String) v.elementAt(9);
		memory = Integer.parseInt(line);

	}

	/*****************************************************************************/

	public void write(PrintWriter w) {
		w.println(name + " {");
		w.println(width);
		w.println(height);
		w.println(density);
		w.println(filter_radius);
		w.println(oversample);
		w.println(format);
		w.println(limitmem ? "true" : "false");
		w.println(indexmem);
		w.println(memory);
		w.println("}");
		w.println("");
	}

	/*****************************************************************************/

} // End of class Preset
