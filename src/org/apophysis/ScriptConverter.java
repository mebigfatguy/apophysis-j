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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

public class ScriptConverter {

	/*****************************************************************************/
	// CONSTANTS

	/*****************************************************************************/
	// FIELDS

	static Map<String, String> sub; // substitution table
	static StringBuffer sb;

	static List<String> functions;

	/*****************************************************************************/

	static void convert(StringBuffer stringbuffer) {
		sb = stringbuffer;
		sub = new Hashtable<String, String>();
		functions = new Vector<String>();

		// save comments
		while (true) {
			int i = sb.indexOf("{");
			if (i < 0) {
				break;
			}
			int j = sb.indexOf("}", i);
			if (j < 0) {
				break;
			}
			String comment = sb.substring(i + 1, j);
			substitute(sb, i, j + 1, "/*" + comment + "*/");
		}

		while (true) {
			int i = sb.indexOf("//");
			if (i < 0) {
				break;
			}
			int j = sb.indexOf("\n", i);
			if (j < 0) {
				break;
			}
			String comment = sb.substring(i + 2, j);
			substitute(sb, i, j, "/* " + comment + " */");
		}

		while (processFormat(sb)) {
			;
		}

		// save literals
		while (true) {
			int i = sb.indexOf("'");
			if (i < 0) {
				break;
			}
			int j = sb.indexOf("'", i + 1);
			if (j < 0) {
				break;
			}

			StringBuffer sb2 = new StringBuffer(sb.substring(i, j + 1));
			while (true) {
				int k = sb2.indexOf("\\");
				if (k < 0) {
					break;
				}
				substitute(sb2, k, k + 1, "\\\\");
			}
			while (true) {
				int k = sb2.indexOf("\n");
				if (k < 0) {
					break;
				}
				substitute(sb2, k, k + 1, "\\n");
			}
			String newlit = sb2.toString();
			substitute(sb, i, j + 1, newlit);
		}

		while (processInputQuery(sb)) {
			;
		}
		while (processProcedure(sb)) {
			;
		}
		while (processWith(sb)) {
			;
		}

		processStructure(sb, "Flame", JSFlame.class);
		processStructure(sb, "Transform", JSTransform.class);
		processStructure(sb, "Options", JSOptions.class);
		processStructure(sb, "Renderer", JSRenderer.class);
		processStructure(sb, "TStringList", JSStringList.class);

		replaceWord(sb, "Transforms", "Transforms");

		replaceWord(sb, "True", "true");
		replaceWord(sb, "False", "false");
		replaceWord(sb, "and", "&&");
		replaceWord(sb, "or", "||");
		replaceWord(sb, "not", "!");
		replaceWord(sb, "mod", "%");
		replaceWord(sb, "div", "/");

		// process IF statements
		while (true) {
			int i = find(sb, "if");
			if (i < 0) {
				break;
			}
			int j = find(sb, "then");
			if (i < 0) {
				break;
			}
			int k = sb.indexOf("\n", j + 1);
			int l = find(sb, "else", j + 1);
			if ((l < k) && (l >= 0)) {
				k = l;
			}

			// end of line
			String stmt = sb.substring(j + 4, k).trim();
			if (stmt.length() > 0) {
				int kk = sb.indexOf("//", j + 1);
				if ((kk >= 0) && (kk < l)) {
					k = kk;
				}
				sb.replace(j + 4, k, "{ " + stmt + " }");
			}

			substitute(sb, j, j + 4, ")");
			substitute(sb, i, i + 3, "if(");
		}

		// process ELSE statements
		while (true) {
			int i = find(sb, "else");
			if (i < 0) {
				break;
			}

			int j = sb.indexOf("\n", i + 1);

			String stmt = sb.substring(i + 4, j).trim();
			if (stmt.length() > 0) {
				sb.replace(i + 4, j, "{ " + stmt + " }");
			}

			substitute(sb, i, i + 4, "else");
		}

		// process WHILE statements
		while (true) {
			int i = find(sb, "while");
			if (i < 0) {
				break;
			}
			int j = sb.indexOf("do", i);
			substitute(sb, j, j + 2, ")");
			substitute(sb, i, i + 5, "while(");
		}

		// process FOR statements
		while (true) {
			int i = find(sb, "for");
			if (i < 0) {
				break;
			}

			int j = sb.indexOf(":=", i);
			if (j < 0) {
				break;
			}
			String index = sb.substring(i + 4, j).trim();

			int k = find(sb, "to", j);
			if (k < 0) {
				break;
			}
			String start = sb.substring(j + 2, k).trim();

			int l = find(sb, "do", k);
			if (l < 0) {
				break;
			}

			String limit = sb.substring(k + 2, l).trim();

			String stmt = "(" + index + ":=" + start + ";" + index + "<="
					+ limit + ";" + index + "++)";

			sb.replace(i + 3, l + 2, stmt);
			substitute(sb, i, i + 3, "for");
		}

		// process CASE statements
		while (processCaseStatement(sb)) {
			;
		}

		// process DELETE statements
		while (processDeleteStatement(sb)) {
			;
		}

		replaceWord(sb, "begin", "{");
		replaceWord(sb, "end", "}");

		while (true) {
			int i = find(sb, "inc");
			if (i < 0) {
				break;
			}
			if (i + 3 >= sb.length()) {
				break;
			}
			if (sb.charAt(i + 3) == '(') {
				int j = sb.indexOf(")", i + 3);
				if (j < 0) {
					substitute(sb, i, i + 3, "inc");
				} else {
					String index = sb.substring(i + 4, j);
					substitute(sb, i, j + 1, index + "++");
				}
			} else {
				substitute(sb, i, i + 3, "inc");
			}
		}

		while (true) {
			int i = find(sb, "dec");
			if (i < 0) {
				break;
			}
			if (i + 3 >= sb.length()) {
				break;
			}
			if (sb.charAt(i + 3) == '(') {
				int j = sb.indexOf(")", i + 3);
				if (j < 0) {
					substitute(sb, i, i + 3, "dec");
				} else {
					String index = sb.substring(i + 4, j);
					substitute(sb, i, j + 1, index + "--");
				}
			} else {
				substitute(sb, i, i + 3, "dec");
			}
		}

		replaceString(sb, "<=", "<=");
		replaceString(sb, ">=", ">=");
		replaceString(sb, ":=", "=");
		replaceString(sb, "=", "==");
		replaceString(sb, "<>", "!=");
		replaceString(sb, ";\n", "\n");

		// getNiladicFunctions();

		processFunctionCalls(sb);

		// check calls to no-arg functions
		int nf = functions.size();
		for (int i = 0; i < nf; i++) {
			String funcname = functions.get(i);
			replaceWord(sb, funcname, funcname + "()");
		}

		// put final substitutions back
		while (true) {
			int i = sb.indexOf("<@");
			if (i < 0) {
				break;
			}
			int j = sb.indexOf("@>", i + 2);
			if (j < 0) {
				break;
			}
			String key = sb.substring(i, j + 2);
			String val = sub.get(key);
			sb.replace(i, j + 2, val);
		}

		sub = null;

	} // End of method convertScript

	/*****************************************************************************/

	static void substitute(StringBuffer sb, int i, int j, String s) {
		String key = "<@" + sub.size() + "@>";
		sub.put(key, s);
		sb.replace(i, j, key);
	}

	/*****************************************************************************/

	static void replaceWord(StringBuffer sb, String s1, String s2) {

		while (true) {
			int i = find(sb, s1);
			if (i < 0) {
				break;
			}
			substitute(sb, i, i + s1.length(), s2);
		}

	} // End of method replace

	/*****************************************************************************/

	static void replaceString(StringBuffer sb, String s1, String s2) {

		while (true) {
			int i = sb.indexOf(s1);
			if (i < 0) {
				break;
			}
			substitute(sb, i, i + s1.length(), s2);
		}

	} // End of method replaceString

	/*****************************************************************************/

	static void processStructure(StringBuffer sb, String sname, Class<?> klass) {

		try {
			Field fields[] = klass.getDeclaredFields();
			int nf = fields.length;
			for (int i = 0; i < nf; i++) {
				String fname = sname + "." + fields[i].getName();
				replaceWord(sb, fname, fname);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		replaceWord(sb, sname, sname);

	} // End of method processStructure

	/*****************************************************************************/

	static boolean processFormat(StringBuffer sb) {
		int i = find(sb, "Format");
		System.out.println("format i=" + i);
		if (i < 0) {
			return false;
		}

		int j = immediateIndexOf(sb, "(", i + 6);
		System.out.println("format j=" + j);
		if (j < 0) {
			return false;
		}

		int k = immediateIndexOf(sb, "'", j + 1);
		System.out.println("format k=" + k);
		if (k < 0) {
			return false;
		}

		int l = sb.indexOf("'", k + 1);
		System.out.println("format l=" + l);
		if (l < 0) {
			return false;
		}

		String fmt = sb.substring(k + 1, l);

		int p = immediateIndexOf(sb, ",", l + 1);
		if (p < 0) {
			return false;
		}

		int m = immediateIndexOf(sb, "[", p + 1);
		System.out.println("format m=" + m);
		if (m < 0) {
			return false;
		}

		int n = sb.indexOf("]", m + 1);
		System.out.println("format n=" + n);
		if (n < 0) {
			return false;
		}

		int o = immediateIndexOf(sb, ")", n + 1);
		System.out.println("format o=" + o);
		if (o < 0) {
			return false;
		}

		String args = sb.substring(m + 1, n);

		StringBuffer snew = new StringBuffer("Format('" + fmt + "'," + args
				+ ")");

		System.out.println("new : " + snew);

		replaceWord(snew, "Format", "Format");
		sb.replace(i, o + 1, snew.toString());

		return true;
	}

	/*****************************************************************************/

	static int immediateIndexOf(StringBuffer sb, String what, int start) {
		int i = sb.indexOf(what, start);
		if (i < 0) {
			return i;
		}

		// if something between start and i
		if (sb.substring(start, i).trim().length() > 0) {
			return -1;
		}
		return i;
	}

	/*****************************************************************************/

	static boolean processInputQuery(StringBuffer sb) {
		int i = find(sb, "InputQuery");
		if (i < 0) {
			return false;
		}

		int j = sb.indexOf(",", i + 1);
		if (j < 0) {
			return false;
		}

		int k = sb.indexOf(",", j + 1);
		if (k < 0) {
			return false;
		}

		int l = sb.indexOf(")", k + 1);
		if (k < 0) {
			return false;
		}

		String call = sb.substring(i, l + 1);
		String varname = sb.substring(k + 1, l).trim();

		StringBuffer sbnew = new StringBuffer(varname + ":=" + call);

		replaceWord(sbnew, "InputQuery", "InputQuery");
		sb.replace(i, l + 1, sbnew.toString());

		return true;

	} // End of method processInputQuery

	/*****************************************************************************/

	static boolean processProcedure(StringBuffer sb) {
		int i = find(sb, "procedure");
		if (i < 0) {
			return false;
		}

		int j = find(sb, "begin", i + 1);
		if (j < 0) {
			return false;
		}

		StringBuilder sdef = new StringBuilder(sb.substring(i + 9, j).trim());
		int k = sdef.indexOf("(");
		int l = sdef.indexOf(")", k + 1);

		if ((k >= 0) && (l >= 0)) {
			// arguments
			StringTokenizer tk = new StringTokenizer(sdef.substring(k + 1, l),
					" :;");
			int n = tk.countTokens();
			String newargs = "";
			String sep = "";
			for (int ii = 0; ii < n; ii += 2) {
				newargs += sep;
				newargs += tk.nextToken();
				sep = ",";
				tk.nextToken();
			}
			sdef.replace(k + 1, l, newargs);
			sb.replace(i + 9, j, sdef.toString() + "\n");
		} else {
			k = i + 9;
			while (sb.charAt(k) == ' ') {
				k++;
			}

			// beginning of the name
			l = k;
			k++;
			while (isInName(sb.charAt(k)))
			 {
				k++;
			// end of the name
			}

			// save name of no-arg function to check calls later
			String funcname = sb.substring(l, k).trim();
			functions.add(funcname);
		}

		sb.replace(i, i + 9, "function ");

		return true;

	} // End of method processProcedure

	/*****************************************************************************/

	static boolean isInName(char c) {
		if ((c >= 'a') && (c <= 'z')) {
			return true;
		}
		if ((c >= 'A') && (c <= 'Z')) {
			return true;
		}
		if ((c >= '0') && (c <= '9')) {
			return true;
		}
		if (c == '_') {
			return true;
		}
		return false;
	}

	/*****************************************************************************/

	static void processArguments(StringBuffer s) {
		int j, k;

		while (true) {
			j = s.indexOf(":");
			if (j < 0) {
				break;
			}
			k = s.indexOf(";", j + 1);
			if (k < 0) {
				// last argument
				k = s.indexOf(")", j + 1);
				s.delete(j, k);
			} else {
				s.replace(j, k, ",");
			}
		}

	} // End of method processArguments

	/*****************************************************************************/

	static boolean processWith(StringBuffer sb) {
		int i = find(sb, "with");
		if (i < 0) {
			return false;
		}

		int j = find(sb, "do", i);
		if (j < 0) {
			return false;
		}

		int k = find(sb, "begin", j);
		if (k < 0) {
			return false;
		}

		int l = find(sb, "end", k);
		if (l < 0) {
			return false;
		}

		String var = sb.substring(i + 4, j).trim();
		StringBuffer sblock = new StringBuffer(sb.substring(k + 5, l));
		String block = sb.substring(k + 5, l);

		if (var.equals("Flame")) {
			processFields(sblock, var, JSFlame.class);
		} else if (var.equals("Transform")) {
			processFields(sblock, var, JSTransform.class);
		} else if (var.equals("Options")) {
			processFields(sblock, var, JSOptions.class);
		} else if (var.equals("Renderer")) {
			processFields(sblock, var, JSRenderer.class);
		} else if (var.equals("TStringList")) {
			processFields(sblock, var, JSStringList.class);
		}

		sb.replace(k + 5, l, sblock.toString());
		sb.replace(i, j + 2, "");

		return true;

	} // End of method processWithStatements

	/*****************************************************************************/

	static void processFields(StringBuffer sb, String varname, Class<?> klass) {
		try {
			Field fields[] = klass.getDeclaredFields();
			int nf = fields.length;
			for (int i = 0; i < nf; i++) {
				String name = fields[i].getName();
				replaceWord(sb, varname + "." + name, varname + "." + name);
				replaceWord(sb, name, varname + "." + name);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	} // End of method processFields

	/*****************************************************************************/

	static boolean processCaseStatement(StringBuffer sb) {
		int i = find(sb, "case");
		if (i < 0) {
			return false;
		}

		int j = find(sb, "of");
		if (j < 0) {
			return false;
		}

		String control = sb.substring(i + 4, j).trim().toString();

		int k = find(sb, "end", j + 1);
		if (k < 0) {
			return false;
		}

		StringBuffer sblock = new StringBuffer(sb.substring(j + 2, k));
		int l = 0;
		String sbreak = "";
		while (true) {
			int i1 = sblock.indexOf("\n", l);
			if (i1 < 0) {
				break;
			}
			int i2 = sblock.indexOf(":", i1);
			if (i2 < 0) {
				break;
			}
			sblock.replace(i1, i1 + 1, sbreak + "\ncase ");
			sbreak = "break";
			l = i2 + 4;
		}

		replaceWord(sblock, "case", "case");

		sb.replace(i, k + 3, "switch(" + control + ")\n{\n" + sblock.toString()
				+ "}");

		return true;

	} // End of method processCaseStatement

	/*****************************************************************************/

	static boolean processDeleteStatement(StringBuffer sb) {
		int i = find(sb, "delete");
		if (i < 0) {
			return false;
		}

		int j = sb.indexOf("(", i + 1);
		if (j < 0) {
			return false;
		}

		int k = sb.indexOf(",", j + 1);
		if (k < 0) {
			return false;
		}

		int l = sb.indexOf(",", k + 1);
		if (l < 0) {
			return false;
		}

		int m = sb.indexOf(")", l + 1);
		if (m < 0) {
			return false;
		}

		String varname = sb.substring(j + 1, k).trim();
		String from = sb.substring(k + 1, l).trim();
		String len = sb.substring(l + 1, m).trim();

		StringBuffer sbnew = new StringBuffer(varname + " := " + "Delete("
				+ varname + "," + from + "," + len + ")");
		replaceWord(sbnew, "delete", "Delete");

		sb.replace(i, m + 1, sbnew.toString());

		return true;

	} // End of method processDeleteStatement

	/*****************************************************************************/

	static void getNiladicFunctions() {
		Method m[] = Script.class.getDeclaredMethods();
		for (Method element : m) {
			String name = element.getName();
			if (!name.startsWith("_")) {
				continue;
			}
			Class<?>[] c = element.getParameterTypes();
			if (c.length > 0) {
				continue;
			}
			System.out.println("NILADIC " + element.getName());
			name = name.substring(1);
			functions.add(name);
		}

	}

	/*****************************************************************************/

	static int find(StringBuffer sb, String word) {
		return find(sb, word, 0);
	}

	static int find(StringBuffer sb, String word, int istart) {
		int l = word.length();
		int i = -1;

		String word1 = word.toLowerCase();
		String word2 = word.toUpperCase();
		String word3 = word2.substring(0, 1) + word1.substring(1);
		String word4 = word1.substring(0, 1) + word.substring(1);
		String word5 = word2.substring(0, 1) + word.substring(1);
		while (true) {
			i = 999999;
			int i0 = sb.indexOf(word, istart);
			int i1 = sb.indexOf(word1, istart);
			int i2 = sb.indexOf(word2, istart);
			int i3 = sb.indexOf(word3, istart);
			int i4 = sb.indexOf(word4, istart);
			int i5 = sb.indexOf(word5, istart);
			if ((i0 >= 0) && (i0 < i)) {
				i = i0;
			}
			if ((i1 >= 0) && (i1 < i)) {
				i = i1;
			}
			if ((i2 >= 0) && (i2 < i)) {
				i = i2;
			}
			if ((i3 >= 0) && (i3 < i)) {
				i = i3;
			}
			if ((i4 >= 0) && (i4 < i)) {
				i = i4;
			}
			if ((i5 >= 0) && (i5 < i)) {
				i = i5;
			}
			if (i == 999999) {
				i = -1;
				break;
			}

			if (i > 0) {
				if (Character.isLetterOrDigit(sb.charAt(i - 1))) {
					istart = i + l;
					continue;
				}
			}

			if (i + l < sb.length()) {
				if (Character.isLetterOrDigit(sb.charAt(i + l))) {
					istart = i + l;
					continue;
				}
			}

			break;
		}

		return i;
	}

	/*****************************************************************************/

	static void processFunctionCalls(StringBuffer sb) {

		// check function calls for spelling and parentheses

		try {
			Method methods[] = Script.class.getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				if (!methods[i].getName().startsWith("_")) {
					continue;
				}
				Class<?>[] params = methods[i].getParameterTypes();
				String myname = methods[i].getName().substring(1);
				if (params.length == 0) {
					replaceWord(sb, myname, myname + "()");
				} else {
					replaceWord(sb, myname, myname);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	} // End of method checkFunctionCalls

	/*****************************************************************************/

} // End of class ScriptConverter

