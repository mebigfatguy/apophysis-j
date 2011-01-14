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

import java.awt.Rectangle;
import java.awt.Toolkit;

import com.thinlet.FrameLauncher;
import com.thinlet.Thinlet;

public class MyThinlet extends Thinlet {

	/*****************************************************************************/
	// CONSTANTS

	static final int ivigibg = 0xD1CCC6;
	// static final int ivigitxt = 0x30444F;
	static final int ivigitxt = 0x000000;
	static final int ivigitxtbg = 0xFFFFFF;
	static final int ivigiborder = 0x587F3B;
	static final int ivigidisable = 0xB0B0B0;
	static final int ivigihover = 0xEdEDED;
	static final int ivigipress = 0x898989;
	static final int ivigifocus = 0x89899A;
	static final int ivigiselect = 0xC5C5DD;

	/*****************************************************************************/
	// FIELDS

	FrameLauncher launcher = null;

	String _answer = null;
	Task _endtask = null;

	/*****************************************************************************/

	public MyThinlet(String title, String xmlfile, int width, int height)
			throws Exception {

		add(parse(xmlfile));

		setColors(ivigibg, ivigitxt, ivigitxtbg, ivigiborder, ivigidisable,
				ivigihover, ivigipress, ivigifocus, ivigiselect);

		launcher = new FrameLauncher(title, this, width, height, false);
	}

	/*****************************************************************************/

	@Override
	public void show() {
		launcher.setVisible(true);
	}

	/*****************************************************************************/

	@Override
	public void hide() {
		launcher.setVisible(false);
	}

	/*****************************************************************************/

	public boolean visible() {
		return launcher.isVisible();
	}

	/*****************************************************************************/

	void beep() {
		Toolkit.getDefaultToolkit().beep();
	}

	/*****************************************************************************/

	void tilt(String msg) {
		System.out.println(msg);
	}

	/*****************************************************************************/

	public void alert(String msg) {
		alert(msg, null);
	}

	public void alert(String msg, Task task) {
		try {
			_endtask = task;
			Object dialog = parse("alert.xml");
			add(dialog);
			setString(find("alertmessage"), "text", msg);
			launcher.toFront();
			requestFocus();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	} // End of method showAlert

	/*****************************************************************************/

	public void closeAlert() {
		remove(find("alertdialog"));

		if (_endtask != null) {
			_endtask.execute();
		}

		_endtask = null;

	} // End of method closeAlert

	/*****************************************************************************/

	public void alertAndWait(String msg) {
		try {
			Object dialog = parse("alert.xml");
			add(dialog);
			setString(find("alertmessage"), "text", msg);
			launcher.toFront();
			requestFocus();

			while (true) {
				try {
					Thread.sleep(100);
				} catch (Exception rex) {
				}
				if (find("alertdialog") == null) {
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	} // End of method alertAndWait

	/*****************************************************************************/

	public void confirm(String text, Task task) {
		try {
			_endtask = task;
			Object dialog = parse("confirm.xml");
			add(dialog);
			setString(find("question"), "text", text);
			launcher.toFront();
			requestFocus();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/*****************************************************************************/

	public void cancelConfirm() {
		remove(find("confirmdialog"));

		_endtask = null;
	}

	/*****************************************************************************/

	public void acceptConfirm() {
		remove(find("confirmdialog"));

		if (_endtask != null) {
			_endtask.execute();
		}

		_endtask = null;

	}

	/*****************************************************************************/

	public void ask(String question, String value, Task task) {
		try {
			_endtask = task;
			Object dialog = parse("ask.xml");
			add(dialog);
			setString(find("question"), "text", question);
			setString(find("answer"), "text", value);
			setInteger(find("answer"), "end", value.length());
			launcher.toFront();
			requestFocus();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/*****************************************************************************/

	public void cancelAsk() {
		remove(find("askdialog"));

		_endtask = null;
	}

	/*****************************************************************************/

	public void acceptAsk() {

		_answer = getString(find("answer"), "text");

		remove(find("askdialog"));

		if (_endtask != null) {
			_endtask.execute();
		}

		_endtask = null;
	}

	/*****************************************************************************/

	public String askAndWait(String question, String value) {
		_answer = null;

		try {
			_endtask = null;
			Object dialog = parse("ask.xml");
			add(dialog);
			setString(find("question"), "text", question);
			setString(find("answer"), "text", value);
			setInteger(find("answer"), "end", value.length());
			launcher.toFront();

			while (true) {
				try {
					Thread.sleep(100);
				} catch (Exception runex) {
				}
				if (find("askdialog") == null) {
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return _answer;
	}

	/*****************************************************************************/

	void setToAbsolutePosition(Object component, Rectangle bounds) {
		int x, y;

		Rectangle r = getRectangle(component, "bounds");
		x = r.x;
		y = r.y;

		while (true) {
			component = getParent(component);
			if (component == null) {
				break;
			}
			r = getRectangle(component, "bounds");
			x += r.x;
			y += r.y;
		}

		bounds.x = x;
		bounds.y = y;

	} // End of method setToAbsolutePosition

	/*****************************************************************************/

} // End of class MyThinlet
