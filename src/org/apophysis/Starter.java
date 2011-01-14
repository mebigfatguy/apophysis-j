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

import java.net.URL;

public class Starter {

	public static void main(String args[]) {
		ControlPoint cp = new ControlPoint();
		URL url = cp.getClass().getResource("main.xml");
		String s = url.toString();

		String cmd = "java -Xmx200m apophysis.Apophysis";

		if (s.startsWith("jar:")) {
			String jarname = s.substring(4);
			if (jarname.startsWith("file:"))
				jarname = jarname.substring(5);
			int i = jarname.indexOf("!");
			if (i > 0)
				jarname = jarname.substring(0, i);
			cmd = "java -Xmx200m -jar " + jarname + " Apophysis";
		}

		Runtime runtime = Runtime.getRuntime();
		System.out.println(cmd);
		try {
			Process process = runtime.exec(cmd + " < /dev/tty > /dev/tty");
			System.out.println(process);
			process.waitFor();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
