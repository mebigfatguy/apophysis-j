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

public class StringFormatter {

	/******************************************************************************/

	/*****************************************************************************/

	static String format(String fmt, Object args[]) {
		long xl;
		double xd;
		String s;

		StringBuffer sb = new StringBuffer();

		int iarg = 0;

		int istart = 0;

		while (true) {
			int ipct = fmt.indexOf('%', istart);
			if (ipct < 0)
				break;

			if ((ipct < fmt.length() - 1) && (fmt.charAt(ipct + 1) == '%')) {
				sb.append("%");
				istart = ipct + 2;
				continue;
			}

			int icode = findCode(fmt, ipct + 1);
			if (icode < 0)
				break;

			int just = 1;
			String attr = fmt.substring(ipct + 1, icode);
			if (attr.startsWith("-")) {
				just = -1;
				attr = attr.substring(1);
			}

			String dec = "";
			int i = attr.indexOf(".");
			if (i >= 0) {
				dec = attr.substring(i + 1);
				attr = attr.substring(0, i);
			}

			int w = -1;
			if (attr.length() > 0)
				w = Integer.parseInt(attr);

			int d = -1;
			if (dec.length() > 0)
				d = Integer.parseInt(dec);

			switch (fmt.charAt(icode)) {
			case 'b':
				xl = getLong(args[iarg++]);
				s = adjust(Long.toString(xl, 2), just, w, d);
				sb.append(s);
				break;

			case 'd':
				xl = getLong(args[iarg++]);
				s = adjust(Long.toString(xl), just, w, d);
				sb.append(s);
				break;

			case 'f':
				xd = getDouble(args[iarg++]);
				if (d < 0)
					s = adjust("" + xd, just, d, -w);
				else
					s = formatDouble(xd, just, w, d);
				sb.append(s);
				break;

			case 'g':
				xd = getDouble(args[iarg++]);
				s = adjust("" + xd, just, w, -1);
				sb.append(s);
				break;

			case 'o':
				xl = getLong(args[iarg++]);
				s = adjust(Long.toString(xl, 8), just, w, d);
				sb.append(s);
				break;

			case 's':
				s = adjust(args[iarg++].toString(), just, w, -1);
				sb.append(s);
				break;

			case 'x':
			case 'X':
				xl = getLong(args[iarg++]);
				s = adjust(Long.toString(xl, 16), just, w, d);
				if (fmt.charAt(icode) == 'X')
					s = s.toUpperCase();
				else
					s = s.toLowerCase();
				sb.append(s);
				break;
			}

			istart = icode + 1;
		}

		if (istart < fmt.length())
			sb.append(fmt.substring(istart));

		return sb.toString();

	} // End of method format

	/*****************************************************************************/

	static int findCode(String fmt, int ifrom) {
		int j;
		int i = 999999;

		j = fmt.indexOf("b", ifrom);
		if ((j >= 0) && (j < i))
			i = j;

		j = fmt.indexOf("d", ifrom);
		if ((j >= 0) && (j < i))
			i = j;

		j = fmt.indexOf("f", ifrom);
		if ((j >= 0) && (j < i))
			i = j;

		j = fmt.indexOf("g", ifrom);
		if ((j >= 0) && (j < i))
			i = j;

		j = fmt.indexOf("o", ifrom);
		if ((j >= 0) && (j < i))
			i = j;

		j = fmt.indexOf("s", ifrom);
		if ((j >= 0) && (j < i))
			i = j;

		j = fmt.indexOf("x", ifrom);
		if ((j >= 0) && (j < i))
			i = j;

		j = fmt.indexOf("X", ifrom);
		if ((j >= 0) && (j < i))
			i = j;

		if (i == 999999)
			return -1;
		else
			return i;
	}

	/*****************************************************************************/

	static long getLong(Object o) {
		if (o instanceof Long)
			return ((Long) o).longValue();
		else if (o instanceof Integer)
			return ((Integer) o).intValue();
		else if (o instanceof Short)
			return ((Short) o).shortValue();
		else if (o instanceof Double)
			return (long) (((Double) o).doubleValue());
		else if (o instanceof Float)
			return (long) (((Float) o).floatValue());
		else
			return 0;
	}

	/*****************************************************************************/

	static double getDouble(Object o) {
		if (o instanceof Double)
			return ((Double) o).doubleValue();
		else if (o instanceof Float)
			return ((Float) o).floatValue();
		else if (o instanceof Long)
			return ((Long) o).longValue();
		else if (o instanceof Integer)
			return ((Integer) o).intValue();
		else if (o instanceof Short)
			return ((Short) o).shortValue();
		else
			return 0;
	}

	/*****************************************************************************/

	static String adjust(String s, int just, int width, int dec) {
		if (dec > 0)
			while (s.length() < dec)
				s = "0" + s;

		if (width > 0)
			while (s.length() < width)
				if (just > 0)
					s = " " + s;
				else
					s = s + " ";

		return s;
	}

	/*****************************************************************************/

	static String formatDouble(double x, int just, int width, int dec) {
		int sign = 1;
		if (x < 0) {
			sign = -1;
			x = -x;
		}

		double pow10 = 1;
		for (int i = 0; i < dec; i++)
			pow10 *= 10;

		double y = Math.round(pow10 * x) / pow10;

		String s = "" + y;
		int i = s.indexOf(".");
		int l = s.length();
		if (i < 0)
			s = s + ".";
		i = s.indexOf(".");

		if (l - i - 1 < dec) {
			for (int j = l - i; j <= dec; j++)
				s = s + "0";
		} else if (l - i - 1 > dec)
			s = s.substring(0, i + dec + 1);

		if (sign < 0)
			s = "-" + s;

		while (s.length() < width)
			if (just < 0)
				s = s + " ";
			else
				s = " " + s;

		return s;
	}

	/*****************************************************************************/

} // End of class StringFormatter
