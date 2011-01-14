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

public class EntryDialog {

	/******************************************************************************/
	// FIELDS

	public String filename = null;
	public String entryname = null;

	/******************************************************************************/

	private MyThinlet thinlet = null;
	private Task task = null;

	/******************************************************************************/
	// CONSTRUCTOR

	EntryDialog(MyThinlet thinlet, String fname, String ename, Task task) {
		this.thinlet = thinlet;
		this.task = task;
		this.filename = fname;
		this.entryname = ename;
	}

	/******************************************************************************/

	public void show() {

		try {
			Object dialog = thinlet.parse("entrydialog.xml", this);
			thinlet.add(dialog);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		thinlet.setString(thinlet.find("FileField"), "text", filename);
		thinlet.setString(thinlet.find("EntryField"), "text", entryname);
		thinlet.setInteger(thinlet.find("EntryField"), "end",
				entryname.length());
		thinlet.requestFocus(thinlet.find("EntryField"));

	} // End of method shoe

	/******************************************************************************/

	public void chooseFile() {
		String path = (new File(filename)).getParent();
		String name = (new File(filename)).getName();

		Task savetask = new SaveTask();
		Global.savedialog = new SaveDialog(thinlet, path, name, savetask);
		Global.savedialog.warning = "Append to";
		Global.savedialog.show();

	}

	/******************************************************************************/

	void setFile(String filename) {
		this.filename = filename;
		thinlet.setString(thinlet.find("FileField"), "text", filename);
	}

	/******************************************************************************/

	public void cancel() {
		thinlet.remove(thinlet.find("entrydialog"));
	}

	/******************************************************************************/

	public void save() {
		filename = thinlet.getString(thinlet.find("FileField"), "text");
		if (filename.length() == 0) {
			return;
		}

		entryname = thinlet.getString(thinlet.find("EntryField"), "text");
		if (entryname.length() == 0) {
			return;
		}

		thinlet.remove(thinlet.find("entrydialog"));
		if (task != null) {
			task.execute();
		}
	}

	/******************************************************************************/
	/******************************************************************************/

	class SaveTask implements Task {

		public void execute() {
			setFile(Global.savedialog.filename);
		}

	}

	/******************************************************************************/
	/******************************************************************************/

} // End of class EntryDialog
