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
import java.util.StringTokenizer;
import java.util.Vector;

import com.thinlet.Thinlet;

public class OpenDialog {

	/******************************************************************************/
	// FIELDS

	public String filename = null;

	/******************************************************************************/

	private MyThinlet thinlet = null;
	private Task task = null;

	private File curdir = null;

	private Vector dirs = new Vector();
	private Vector filters = new Vector();
	private String patterns[] = { "*.*" };

	File roots[] = null;
	File root = null;

	/******************************************************************************/
	// CONSTRUCTOR

	OpenDialog(MyThinlet thinlet, String path, Task task) {
		this.thinlet = thinlet;
		this.task = task;

		roots = File.listRoots();
		if (roots != null)
			if (roots.length <= 1)
				roots = null;

		if (roots != null)
			root = new File("");

		File file = new File(path);
		if (file.exists())
			setDirectory(file);
		else
			setDirectory(new File(System.getProperty("user.dir")));

	}

	/******************************************************************************/

	public void addFilter(String title, String pattern) {
		filters.addElement(new String[] { title, pattern });
	}

	/******************************************************************************/

	void updateFilters() {

		Object combo = thinlet.find("opentypelist");
		thinlet.removeAll(combo);

		for (int i = 0; i < filters.size(); i++) {
			String s[] = (String[]) filters.elementAt(i);

			Object choice = Thinlet.createImpl("choice");
			thinlet.setString(choice, "text", s[0]);

			thinlet.add(combo, choice);
		}

		thinlet.setInteger(combo, "selected", 0);

	}

	/******************************************************************************/

	public void changeDirectory(Object list) {
		int index = thinlet.getSelectedIndex(list);
		File dir = (File) dirs.elementAt(index);
		setDirectory(dir);
		updateDirs();

		if (curdir == root)
			updateDrives();
		else
			updateFiles();
	} // End of method changeDirectory

	/******************************************************************************/

	void setDirectory(File dir) {
		dirs.removeAllElements();

		curdir = dir;

		while (true) {
			dirs.insertElementAt(dir, 0);
			String sparent = dir.getParent();
			if (sparent == null)
				break;
			dir = new File(sparent);
		}

		int i = getRootIndex(dir);
		if (i >= 0)
			dirs.insertElementAt(root, 0);

	} // End of method setDirectory

	/******************************************************************************/

	int getRootIndex(File dir) {
		if (roots == null)
			return -1;

		for (int i = 0; i < roots.length; i++)
			if (roots[i].equals(dir))
				return i;

		return -1;
	}

	/******************************************************************************/

	String getBrowserPath() {
		return curdir.getAbsolutePath();
	}

	/******************************************************************************/

	void updateDirs() {

		Object dirlist = thinlet.find("opendirlist");
		thinlet.removeAll(dirlist);

		for (int i = 0; i < dirs.size(); i++) {
			File dir = (File) dirs.elementAt(i);

			Object choice = Thinlet.createImpl("choice");

			int j = getRootIndex(dir);
			if (j < 0)
				thinlet.setString(choice, "text", dir.getName()
						+ File.separator);
			else
				thinlet.setString(choice, "text", dir.getAbsolutePath());

			thinlet.add(dirlist, choice);
		}

		thinlet.setInteger(dirlist, "selected", dirs.size() - 1);

	} // End of method updateDirs

	/******************************************************************************/

	void updateDrives() {
		Object filelist = thinlet.find("openfilelist");
		thinlet.removeAll(filelist);

		for (int i = 0; i < roots.length; i++) {
			Object item = Thinlet.createImpl("item");
			thinlet.setString(item, "text", roots[i].getAbsolutePath());
			thinlet.add(filelist, item);
		}

	}

	/******************************************************************************/

	void updateFiles() {

		Object filelist = thinlet.find("openfilelist");
		thinlet.removeAll(filelist);

		String filenames[] = curdir.list();
		if (filenames != null)
			for (int i = 0; i < filenames.length; i++) {
				if (filenames[i].startsWith("."))
					continue;

				File file = new File(curdir, filenames[i]);

				if (file.isDirectory()) {
					Object item = Thinlet.createImpl("item");
					thinlet.setString(item, "text", file.getName()
							+ File.separator);
					thinlet.add(filelist, item);
					continue;
				}

				for (int j = 0; j < patterns.length; j++) {
					if (match(file.getName().toLowerCase(), patterns[j])) {
						Object item = Thinlet.createImpl("item");
						thinlet.setString(item, "text", file.getName());
						thinlet.add(filelist, item);
						continue;
					}
				}
			}

	} // End of method updateFiles

	/******************************************************************************/

	public void cancel() {
		thinlet.remove(thinlet.find("opendialog"));
	}

	/******************************************************************************/

	public void ok() {
		openFile(thinlet.find("openfilelist"));
	}

	/******************************************************************************/

	public void openFile(Object list) {

		Object item = thinlet.getSelectedItem(list);
		if (item == null)
			return;

		String fname = thinlet.getString(item, "text");

		if (curdir == root) {
			int i = thinlet.getSelectedIndex(list);
			if (i >= 0) {
				setDirectory(roots[i]);
				updateDirs();
				updateFiles();
			}
		} else if (fname.endsWith(File.separator)) {
			fname = fname.substring(0, fname.length() - 1);
			File dir = new File(curdir, fname);
			setDirectory(dir);
			updateDirs();
			if (curdir == root)
				updateDrives();
			else
				updateFiles();
		} else {
			// close the dialog and return the name of the file

			thinlet.remove(thinlet.find("opendialog"));

			File file = new File(curdir, fname);
			filename = file.getAbsolutePath();
			if (task != null)
				task.execute();
		}

	}

	/******************************************************************************/

	public void changeFile(Object list) {
	}

	/******************************************************************************/

	public void changeType(Object list) {
		int index = thinlet.getSelectedIndex(list);

		String s[] = (String[]) filters.elementAt(index);

		StringTokenizer tk = new StringTokenizer(s[1], ";");

		patterns = new String[tk.countTokens()];
		for (int i = 0; i < patterns.length; i++)
			patterns[i] = tk.nextToken().toLowerCase();

		if (curdir == root)
			updateDrives();
		else
			updateFiles();

	}

	/******************************************************************************/

	public void show() {

		try {
			Object dialog = thinlet.parse("opendialog.xml", this);
			thinlet.add(dialog);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		updateFilters();
		updateDirs();
		changeType(thinlet.find("opentypelist"));

	} // End of method execute

	/******************************************************************************/

	boolean match(String name, String pattern) {
		return match(name.toCharArray(), pattern.toCharArray(), 0, 0);
	}

	/******************************************************************************/

	boolean match(char name[], char pattern[], int in, int ip) {

		if ((in == name.length) & (ip == pattern.length))
			return true;
		else if (ip == pattern.length)
			return false;
		else if (pattern[ip] == '*') {
			for (int i = in; i <= name.length; i++)
				if (match(name, pattern, i, ip + 1))
					return true;
			return false;
		} else if (in == name.length)
			return false;
		else if (name[in] == pattern[ip])
			return match(name, pattern, in + 1, ip + 1);
		else
			return false;
	}

	/******************************************************************************/

} // End of class OpenDialog
