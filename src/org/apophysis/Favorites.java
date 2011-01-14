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

import java.io.File;
import java.util.Vector;

public class Favorites extends MyThinlet implements Constants {

	/*****************************************************************************/
	// CONSTANTS

	/*****************************************************************************/
	// FIELDS

	Vector scripts = null;

	Object listview = null;

	/*****************************************************************************/

	Favorites(String title, String xmlfile, int width, int height)
			throws Exception {
		super(title, xmlfile, width, height);

		launcher.setResizable(false);

		listview = find("ListView");
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

		scripts = Global.readFavorites();
		updateList();

	} // End of method show

	/*****************************************************************************/

	void updateList() {

		removeAll(listview);

		int n = scripts.size();
		for (int i = 0; i < n; i++) {
			File f = (File) scripts.elementAt(i);
			String title = f.getName();
			int k = title.lastIndexOf('.');
			if (k >= 0)
				title = title.substring(0, k);

			Object item = createImpl("item");
			setString(item, "text", title);

			add(listview, item);
		}

	}

	/*****************************************************************************/

	public void btnAddClick() {
		Global.opendialog = new OpenDialog(this, Global.browserPath,
				new AddTask());
		Global.opendialog.addFilter("Apophysis-j scripts (*.ajs)", "*.ajs");
		Global.opendialog.addFilter("Apophysis scripts (*.asc)", "*asc");
		Global.opendialog.show();
	} // End of method btnAddClick

	/*****************************************************************************/

	void addScript(String filename) {
		scripts.addElement(new File(filename));
		updateList();
	}

	/*****************************************************************************/

	public void btnRemoveClick() {
		int index = getSelectedIndex(listview);
		if (index < 0)
			return;

		scripts.removeElementAt(index);
		updateList();
	}

	/*****************************************************************************/

	public void btnMoveUpClick() {
		int index = getSelectedIndex(listview);
		if (index <= 0)
			return;

		File f = (File) scripts.elementAt(index);
		scripts.removeElementAt(index);
		scripts.insertElementAt(f, index - 1);
		updateList();

		Object item = getItem(listview, index - 1);
		setBoolean(item, "selected", true);
	}

	/*****************************************************************************/

	public void btnMoveDownClick() {
		int index = getSelectedIndex(listview);

		System.out.println("down index=" + index);
		if (index == scripts.size() - 1)
			return;

		File f = (File) scripts.elementAt(index);
		scripts.removeElementAt(index);
		scripts.insertElementAt(f, index + 1);
		updateList();

		Object item = getItem(listview, index + 1);
		setBoolean(item, "selected", true);
	}

	/*****************************************************************************/

	public void btnCancelClick() {
		hide();
	}

	/*****************************************************************************/

	public void btnOKClick() {
		Global.writeFavorites(scripts);
		Global.main.updateFavorites();
		hide();
	}

	/*****************************************************************************/

	class AddTask implements Task {

		public void execute() {
			Global.browserPath = Global.opendialog.getBrowserPath();
			addScript(Global.opendialog.filename);
		}

	}

	/*****************************************************************************/
	/*****************************************************************************/

} // End of class Favorites

