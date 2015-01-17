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
import java.util.ArrayList;
import java.util.List;

import com.thinlet.Thinlet;

public class SaveDialog {

	/******************************************************************************/
	// FIELDS

	MyThinlet thinlet = null;

	Task task = null;

	File curdir = null;

	public String filename = null;

	List<File> dirs = new ArrayList<File>();

	String defaultname;

	String exts = ""; // current extension

	File[] roots = null;
	File root = null;

	public String warning = "Replace";

	/******************************************************************************/
	// CONSTRUCTOR

	SaveDialog(MyThinlet thinlet, String path, String defaultname, Task task) {
		this.thinlet = thinlet;
		this.defaultname = defaultname;
		this.task = task;

		roots = File.listRoots();
		if (roots != null) {
			if (roots.length <= 1) {
				roots = null;
			}
		}

		if (roots != null) {
			root = new File("");
		}

		setDirectory(new File(path));
	}

	/******************************************************************************/

	public void changeDirectory(Object list) {
		int index = thinlet.getSelectedIndex(list);
		System.out.println("index=" + index);
		File dir = dirs.get(index);
		System.out.println("dir=" + dir);

		setDirectory(dir);
		updateDirs();

		if (curdir == root) {
			updateDrives();
		} else {
			updateFiles();
		}

	} // End of method changeDirectory

	/******************************************************************************/

	void setDirectory(File dir) {
		dirs.clear();

		curdir = dir;

		while (true) {
			dirs.add(0, dir);
			String sparent = dir.getParent();
			if (sparent == null) {
				break;
			}
			dir = new File(sparent);
		}

		int i = getRootIndex(dir);

		if (i >= 0) {
			dirs.add(0, root);
		}

	} // End of method setDirectory

	/******************************************************************************/

	int getRootIndex(File dir) {
		if (roots == null) {
			return -1;
		}

		for (int i = 0; i < roots.length; i++) {
			if (roots[i].equals(dir)) {
				return i;
			}
		}

		return -1;
	}

	/******************************************************************************/

	public String getBrowserPath() {
		return curdir.getAbsolutePath();
	}

	/******************************************************************************/

	void updateDirs() {
		Object dirlist = thinlet.find("savedirlist");
		thinlet.removeAll(dirlist);

		System.out.println("updatedirs dirssize=" + dirs.size());

		for (int i = 0; i < dirs.size(); i++) {
			File dir = dirs.get(i);

			Object choice = Thinlet.createImpl("choice");

			int j = getRootIndex(dir);
			if (j < 0) {
				thinlet.setString(choice, "text", dir.getName()
						+ File.separator);
			} else {
				thinlet.setString(choice, "text", dir.getAbsolutePath());
			}
			thinlet.add(dirlist, choice);
		}

		thinlet.setInteger(dirlist, "selected", dirs.size() - 1);

	} // End of method updateDirs

	/******************************************************************************/

	void updateDrives() {
		Object filelist = thinlet.find("savefilelist");
		thinlet.removeAll(filelist);

		for (File root2 : roots) {
			Object item = Thinlet.createImpl("item");
			thinlet.setString(item, "text", root2.getAbsolutePath());
			thinlet.add(filelist, item);
		}

	}

	/******************************************************************************/

	void updateFiles() {
		String savename = thinlet.getString(thinlet.find("savefield"), "text");
		int k = savename.lastIndexOf('.');
		exts = (k >= 0) ? savename.substring(k) : "";

		Object filelist = thinlet.find("savefilelist");
		thinlet.removeAll(filelist);

		String[] filenames = curdir.list();

		for (String filename2 : filenames) {
			if (filename2.startsWith(".")) {
				continue;
			}

			File file = new File(curdir, filename2);
			if (file.isDirectory()) {
				Object item = Thinlet.createImpl("item");
				thinlet.setString(item, "text", file.getName() + File.separator);
				thinlet.add(filelist, item);
			} else if (filename2.endsWith(exts)) {
				Object item = Thinlet.createImpl("item");
				thinlet.setString(item, "text", file.getName());
				thinlet.setBoolean(item, "enabled", false);
				thinlet.add(filelist, item);
			}
		}

		thinlet.setString(thinlet.find("saveok"), "text", "  Save  ");

	} // End of method updateFiles

	/******************************************************************************/

	public void cancel() {
		thinlet.remove(thinlet.find("savedialog"));
	}

	/******************************************************************************/

	public void ok() {
		Object list = thinlet.find("savefilelist");
		Object item = thinlet.getSelectedItem(list);
		if (item != null) {
			String dname = thinlet.getString(item, "text");
			if (dname.endsWith(File.separator)) {
				listDoubleClick(list);
				return;
			}
		}

		Object field = thinlet.find("savefield");
		String s = thinlet.getString(field, "text");
		if (s.length() == 0) {
			thinlet.beep();
			return;
		}

		File file = new File(curdir, s);
		String path = file.getAbsolutePath();

		if (file.exists()) {
			thinlet.confirm(warning + " " + file.getName() + " ?",
					new SaveTask(path));
		} else {
			finish(path);
		}

	} // End of method ok

	/******************************************************************************/

	void finish(String path) {
		thinlet.remove(thinlet.find("savedialog"));

		filename = path;

		if (task != null) {
			task.execute();
		}
	}

	/******************************************************************************/

	public void listClick(Object list) {
		Object item = thinlet.getSelectedItem(list);
		if (item == null)
		 {
			return;
		// if(!thinlet.getBoolean(item,"enabled")) return;
		}

		String dname = thinlet.getString(item, "text");
		if (dname.endsWith(File.separator)) {
			thinlet.setString(thinlet.find("saveok"), "text", "  Open  ");
		} else {
			int i = dname.lastIndexOf('.');
			String extd = (i >= 0) ? dname.substring(i + 1) : "";
			String sname = thinlet.getString(thinlet.find("savefield"), "text");
			int j = sname.lastIndexOf('.');
			String exts = (j >= 0) ? sname.substring(j + 1) : "";

			if ((extd.length() > 0) && extd.equals(exts)) {
				thinlet.setString(thinlet.find("savefield"), "text", dname);
				thinlet.setInteger(thinlet.find("savefield"), "start", 0);
				if (i >= 0) {
					thinlet.setInteger(thinlet.find("savefield"), "end", i);
				}
			}
			thinlet.setString(thinlet.find("saveok"), "text", "  Save  ");
			thinlet.requestFocus(thinlet.find("savefield"));
		}
	}

	/******************************************************************************/

	public void listDoubleClick(Object list) {
		Object item = thinlet.getSelectedItem(list);
		if (item == null) {
			return;
		}

		if (!thinlet.getBoolean(item, "enabled")) {
			return;
		}

		File dir = null;

		if (curdir == root) {
			int i = thinlet.getSelectedIndex(list);
			dir = roots[i];
		} else {
			String dname = thinlet.getString(item, "text");
			dname = dname.substring(0, dname.length() - 1);
			dir = new File(curdir, dname);
		}

		setDirectory(dir);
		updateDirs();

		if (curdir == root) {
			updateDrives();
		} else {
			updateFiles();
		}

	}

	/******************************************************************************/

	public void show() {

		try {
			Object dialog = thinlet.parse("savedialog.xml", this);
			thinlet.add(dialog);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Object field = thinlet.find("savefield");

		thinlet.setString(field, "text", defaultname);
		int i = defaultname.lastIndexOf('.');
		if (i > 0) {
			thinlet.setInteger(field, "end", i);
		}
		thinlet.requestFocus(field);

		updateDirs();

		if (curdir == root) {
			updateDrives();
		} else {
			updateFiles();
		}

	} // End of method execute

	/******************************************************************************/

	boolean match(String name, String pattern) {
		return match(name.toCharArray(), pattern.toCharArray(), 0, 0);
	}

	/******************************************************************************/

	boolean match(char name[], char pattern[], int in, int ip) {

		if ((in == name.length) & (ip == pattern.length)) {
			return true;
		} else if (in == name.length) {
			return false;
		} else if (ip == pattern.length) {
			return false;
		} else if (pattern[ip] == '*') {
			for (int i = in; i < name.length; i++) {
				if (match(name, pattern, i, ip + 1)) {
					return match(name, pattern, i + 1, ip + 2);
				}
			}
			return false;
		} else if (name[in] == pattern[ip]) {
			return match(name, pattern, in + 1, ip + 1);
		} else {
			return false;
		}
	}

	/******************************************************************************/

	public void savefieldChanged() {
		String savename = thinlet.getString(thinlet.find("savefield"), "text");
		int k = savename.lastIndexOf('.');
		String newexts = (k >= 0) ? savename.substring(k) : "";
		if (!newexts.equals(exts)) {
			if (curdir == root) {
				updateDrives();
			} else {
				updateFiles();
			}
		}
	}

	/******************************************************************************/

	class SaveTask implements Task {

		String path = null;

		SaveTask(String path) {
			this.path = path;
		}

		@Override
		public void execute() {
			finish(path);
		}

	}

	/******************************************************************************/
	/******************************************************************************/

} // End of class SaveDialog
