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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

public class XmlTag {

	/******************************************************************************/
	// CONSTANTS

	static final int ST_WNAME = 0; // waiting for tag name
	static final int ST_NAME = 1; // building tag name
	static final int ST_WATTR = 2; // waiting for attribute name
	static final int ST_ATTR = 3; // building attribute name
	static final int ST_WEQUAL = 4; // waiting for =
	static final int ST_WVALUE = 5; // waiting for attribute value
	static final int ST_VALUE = 6; // building attribute value
	static final int ST_WCLOSE = 7; // waiting for closing bracket

	/******************************************************************************/
	// FIELDS

	String name = null;
	Map<String, String> attributes = new Hashtable<String, String>();
	boolean closed = false;

	StringBuffer data = new StringBuffer();

	/******************************************************************************/
	// CONSTRUCTOR

	public XmlTag(String line) throws Exception {
		String attr = null;
		String value = null;

		char[] c = line.toCharArray();

		int state = ST_WNAME;

		for (int i = 0; i < c.length; i++) {
			switch (state) {
			case ST_WNAME:
				if (c[i] == ' ') {
					continue;
				} else if (c[i] == '\t') {
					continue;
				} else if (c[i] != '<') {
					throw new Exception("Not a XML tag");
				} else {
					name = "";
					state = ST_NAME;
				}
				break;

			case ST_NAME:
				if (c[i] == '>') {
					i = c.length;
				} else if (c[i] == ' ') {
					state = ST_WATTR;
				} else {
					name = name + c[i];
				}
				break;

			case ST_WATTR:
				if (c[i] == '>') {
					i = c.length; // exit the loop
				} else if (c[i] == '/') {
					closed = true;
					state = ST_WCLOSE;
				} else if (c[i] == ' ') {
					continue;
				} else if (c[i] == '\t') {
					continue;
				} else {
					attr = "" + c[i];
					state = ST_ATTR;
				}
				break;

			case ST_ATTR:
				if (c[i] == ' ') {
					state = ST_WEQUAL;
				} else if (c[i] == '\t') {
					state = ST_WEQUAL;
				} else if (c[i] == '=') {
					state = ST_WVALUE;
				} else {
					attr = attr + c[i];
				}
				break;

			case ST_WEQUAL:
				if (c[i] == ' ') {
					continue;
				} else if (c[i] == '\t') {
					continue;
				} else if (c[i] == '=') {
					state = ST_WVALUE;
				} else {
					throw new Exception("Bad attribute " + attr);
				}
				break;

			case ST_WVALUE:
				if (c[i] == ' ') {
					continue;
				} else if (c[i] == '\t') {
					continue;
				} else if (c[i] == '"') {
					value = "";
					state = ST_VALUE;
				} else {
					throw new Exception("Bad attribute " + attr);
				}
				break;

			case ST_VALUE:
				if (c[i] == '"') {
					attributes.put(attr, value);
					state = ST_WATTR;
				} else {
					value = value + c[i];
				}
				break;

			case ST_WCLOSE:
				if (c[i] == '>') {
					i = c.length;
				} else {
					throw new Exception("Not closed");
				}
				break;
			}

			if (state == ST_VALUE) {
				attributes.put(attr, value);
			}
		}

	}

	/******************************************************************************/

	public void appendData(String data) {
		this.data.append(data);
	}

	/******************************************************************************/

	public String getData() {
		return data.toString();
	}

	/******************************************************************************/

	public String getName() {
		return name;
	}

	/******************************************************************************/

	public String getAttribute(String attr) {
		String s = attributes.get(attr);
		if (s != null) {
			attributes.remove(attr);
		}
		return s;
	}

	/******************************************************************************/

	public void putAttribute(String attr, String value) {
		attributes.put(attr, value);
	}

	/******************************************************************************/

	public void appendAttribute(String attr, String data) {
		String old = getAttribute(attr);
		if (old == null) {
			putAttribute(attr, data);
		} else {
			putAttribute(attr, old + data);
		}
	}

	/******************************************************************************/

	void print() {
		System.out.println("xml tag name = " + name);
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			System.out.println("     " + entry.getKey() + " = " + entry.getValue());
		}

	} // End of method print

	/******************************************************************************/

	double[] getDoubles(String name) {
		String s = getAttribute(name);
		if (s == null) {
			return null;
		}

		StringTokenizer tk = new StringTokenizer(s);
		int n = tk.countTokens();

		double[] dd = new double[n];
		for (int i = 0; i < n; i++) {
			dd[i] = Double.valueOf(tk.nextToken()).doubleValue();
		}

		return dd;
	}

	/*****************************************************************************/

	double getDouble(String name, double value) {
		String s = getAttribute(name);
		if (s == null) {
			return value;
		} else {
			return Double.valueOf(s).doubleValue();
		}
	}

	/*****************************************************************************/

	int getInt(String name, int value) {
		String s = getAttribute(name);
		if (s == null) {
			return value;
		} else {
			return Integer.parseInt(s);
		}
	}

	/*****************************************************************************/

	String getString(String name, String value) {
		String s = getAttribute(name);
		if (s == null) {
			return value;
		} else {
			return s;
		}
	}

	/*****************************************************************************/

	Iterator<String> getUnreclaimedKeys() {
		return attributes.keySet().iterator();
	}

	/*****************************************************************************/

	public boolean isClosed() {
		return closed;
	}

	/*****************************************************************************/

} // End of class XmlTag
