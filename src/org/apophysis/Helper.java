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

import java.io.InputStreamReader;

public class Helper extends MyThinlet implements Constants, Runnable {

	/*****************************************************************************/
	// CONSTANTS

	/*****************************************************************************/
	// FIELDS

	String titles[];
	String names[];

	/*****************************************************************************/

	Helper(String title, String xmlfile, int width, int height)
			throws Exception {
		super(title, xmlfile, width, height);

		buildTopicMenu();

	}

	/*****************************************************************************/

	@Override
	public boolean destroy() {
		hide();
		return false;
	}

	/*****************************************************************************/

	@Override
	public void show() {
		super.show();

	} // End of method show

	/*****************************************************************************/

	void buildTopicMenu() {
		Object combo = find("cmbTopic");
		Object[] choices = getItems(combo);

		int n = choices.length;

		titles = new String[n];
		names = new String[n];

		for (int i = 0; i < n; i++) {
			String t = getString(choices[i], "text");
			int k = t.indexOf(":");
			if (k < 0) {
				continue;
			}
			titles[i] = t.substring(0, k);
			names[i] = t.substring(k + 1);
			setString(choices[i], "text", "  " + titles[i] + "  ");
		}

	}

	/*****************************************************************************/

	public void changeTopic(Object combo) {

		int index = getInteger(combo, "selected");
		if (index < 0) {
			return;
		}
		if (names == null) {
			return;
		}

		setTopic(index);

	} // End of method changeTopic

	/*****************************************************************************/

	public void setTopicByName(String name) {
		for (int i = 0; i < names.length; i++) {
			if (names[i].equals(name)) {
				setTopic(i);
				break;
			}
		}
	} // End of method setTopicByName

	/*****************************************************************************/

	public void setTopic(int index) {
		String title = titles[index];
		String name = names[index];

		try {
			char[] buffer = new char[512];

			String rname = "/help/" + name + ".txt";

			InputStreamReader r = new InputStreamReader(Global.main.getClass()
					.getResourceAsStream(rname));

			StringBuilder sb = new StringBuilder();

			while (true) {
				int n = r.read(buffer, 0, buffer.length);
				if (n <= 0) {
					break;
				}
				sb.append(buffer, 0, n);
			}
			r.close();

			setString(find("Display"), "text", sb.toString());
			setInteger(find("cmbTopic"), "selected", index);
			setString(find("cmbTopic"), "text", title);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	} // End of method setTopic

	/*****************************************************************************/

	public void btnCloseClick() {
		hide();
	}

	/*****************************************************************************/

} // End of class Helper

