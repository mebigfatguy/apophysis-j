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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class ScriptConverter {

    /*****************************************************************************/

    static void convert(StringBuilder stringbuffer) {
        Map<String, String> sub = new HashMap<>();
        List<String> functions = new ArrayList<>();

        // save comments
        while (true) {
            int i = stringbuffer.indexOf("{");
            if (i < 0) {
                break;
            }
            int j = stringbuffer.indexOf("}", i);
            if (j < 0) {
                break;
            }
            String comment = stringbuffer.substring(i + 1, j);
            substitute(stringbuffer, sub, i, j + 1, "/*" + comment + "*/");
        }

        while (true) {
            int i = stringbuffer.indexOf("//");
            if (i < 0) {
                break;
            }
            int j = stringbuffer.indexOf("\n", i);
            if (j < 0) {
                break;
            }
            String comment = stringbuffer.substring(i + 2, j);
            substitute(stringbuffer, sub, i, j, "/* " + comment + " */");
        }

        while (processFormat(stringbuffer, sub)) {
        }

        // save literals
        while (true) {
            int i = stringbuffer.indexOf("'");
            if (i < 0) {
                break;
            }
            int j = stringbuffer.indexOf("'", i + 1);
            if (j < 0) {
                break;
            }

            StringBuilder stringbuffer2 = new StringBuilder(stringbuffer.substring(i, j + 1));
            while (true) {
                int k = stringbuffer2.indexOf("\\");
                if (k < 0) {
                    break;
                }
                substitute(stringbuffer2, sub, k, k + 1, "\\\\");
            }
            while (true) {
                int k = stringbuffer2.indexOf("\n");
                if (k < 0) {
                    break;
                }
                substitute(stringbuffer2, sub, k, k + 1, "\\n");
            }
            String newlit = stringbuffer2.toString();
            substitute(stringbuffer, sub, i, j + 1, newlit);
        }

        while (processInputQuery(stringbuffer, sub)) {
        }
        while (processProcedure(stringbuffer, functions)) {
        }
        while (processWith(stringbuffer, sub)) {
        }

        processStructure(stringbuffer, sub, "Flame", JSFlame.class);
        processStructure(stringbuffer, sub, "Transform", JSTransform.class);
        processStructure(stringbuffer, sub, "Options", JSOptions.class);
        processStructure(stringbuffer, sub, "Renderer", JSRenderer.class);
        processStructure(stringbuffer, sub, "TStringList", JSStringList.class);

        replaceWord(stringbuffer, sub, "Transforms", "Transforms");

        replaceWord(stringbuffer, sub, "True", "true");
        replaceWord(stringbuffer, sub, "False", "false");
        replaceWord(stringbuffer, sub, "and", "&&");
        replaceWord(stringbuffer, sub, "or", "||");
        replaceWord(stringbuffer, sub, "not", "!");
        replaceWord(stringbuffer, sub, "mod", "%");
        replaceWord(stringbuffer, sub, "div", "/");

        // process IF statements
        while (true) {
            int i = find(stringbuffer, "if");
            if (i < 0) {
                break;
            }
            int j = find(stringbuffer, "then");
            if (i < 0) {
                break;
            }
            int k = stringbuffer.indexOf("\n", j + 1);
            int l = find(stringbuffer, "else", j + 1);
            if ((l < k) && (l >= 0)) {
                k = l;
            }

            // end of line
            String stmt = stringbuffer.substring(j + 4, k).trim();
            if (stmt.length() > 0) {
                int kk = stringbuffer.indexOf("//", j + 1);
                if ((kk >= 0) && (kk < l)) {
                    k = kk;
                }
                stringbuffer.replace(j + 4, k, "{ " + stmt + " }");
            }

            substitute(stringbuffer, sub, j, j + 4, ")");
            substitute(stringbuffer, sub, i, i + 3, "if(");
        }

        // process ELSE statements
        while (true) {
            int i = find(stringbuffer, "else");
            if (i < 0) {
                break;
            }

            int j = stringbuffer.indexOf("\n", i + 1);

            String stmt = stringbuffer.substring(i + 4, j).trim();
            if (stmt.length() > 0) {
                stringbuffer.replace(i + 4, j, "{ " + stmt + " }");
            }

            substitute(stringbuffer, sub, i, i + 4, "else");
        }

        // process WHILE statements
        while (true) {
            int i = find(stringbuffer, "while");
            if (i < 0) {
                break;
            }
            int j = stringbuffer.indexOf("do", i);
            substitute(stringbuffer, sub, j, j + 2, ")");
            substitute(stringbuffer, sub, i, i + 5, "while(");
        }

        // process FOR statements
        while (true) {
            int i = find(stringbuffer, "for");
            if (i < 0) {
                break;
            }

            int j = stringbuffer.indexOf(":=", i);
            if (j < 0) {
                break;
            }
            String index = stringbuffer.substring(i + 4, j).trim();

            int k = find(stringbuffer, "to", j);
            if (k < 0) {
                break;
            }
            String start = stringbuffer.substring(j + 2, k).trim();

            int l = find(stringbuffer, "do", k);
            if (l < 0) {
                break;
            }

            String limit = stringbuffer.substring(k + 2, l).trim();

            String stmt = "(" + index + ":=" + start + ";" + index + "<=" + limit + ";" + index + "++)";

            stringbuffer.replace(i + 3, l + 2, stmt);
            substitute(stringbuffer, sub, i, i + 3, "for");
        }

        // process CASE statements
        while (processCaseStatement(stringbuffer, sub)) {
        }

        // process DELETE statements
        while (processDeleteStatement(stringbuffer, sub)) {
        }

        replaceWord(stringbuffer, sub, "begin", "{");
        replaceWord(stringbuffer, sub, "end", "}");

        while (true) {
            int i = find(stringbuffer, "inc");
            if (i < 0) {
                break;
            }
            if ((i + 3) >= stringbuffer.length()) {
                break;
            }
            if (stringbuffer.charAt(i + 3) == '(') {
                int j = stringbuffer.indexOf(")", i + 3);
                if (j < 0) {
                    substitute(stringbuffer, sub, i, i + 3, "inc");
                } else {
                    String index = stringbuffer.substring(i + 4, j);
                    substitute(stringbuffer, sub, i, j + 1, index + "++");
                }
            } else {
                substitute(stringbuffer, sub, i, i + 3, "inc");
            }
        }

        while (true) {
            int i = find(stringbuffer, "dec");
            if (i < 0) {
                break;
            }
            if ((i + 3) >= stringbuffer.length()) {
                break;
            }
            if (stringbuffer.charAt(i + 3) == '(') {
                int j = stringbuffer.indexOf(")", i + 3);
                if (j < 0) {
                    substitute(stringbuffer, sub, i, i + 3, "dec");
                } else {
                    String index = stringbuffer.substring(i + 4, j);
                    substitute(stringbuffer, sub, i, j + 1, index + "--");
                }
            } else {
                substitute(stringbuffer, sub, i, i + 3, "dec");
            }
        }

        replaceString(stringbuffer, sub, "<=", "<=");
        replaceString(stringbuffer, sub, ">=", ">=");
        replaceString(stringbuffer, sub, ":=", "=");
        replaceString(stringbuffer, sub, "=", "==");
        replaceString(stringbuffer, sub, "<>", "!=");
        replaceString(stringbuffer, sub, ";\n", "\n");

        // getNiladicFunctions();

        processFunctionCalls(stringbuffer, sub);

        // check calls to no-arg functions
        int nf = functions.size();
        for (int i = 0; i < nf; i++) {
            String funcname = functions.get(i);
            replaceWord(stringbuffer, sub, funcname, funcname + "()");
        }

        // put final substitutions back
        while (true) {
            int i = stringbuffer.indexOf("<@");
            if (i < 0) {
                break;
            }
            int j = stringbuffer.indexOf("@>", i + 2);
            if (j < 0) {
                break;
            }
            String key = stringbuffer.substring(i, j + 2);
            String val = sub.get(key);
            stringbuffer.replace(i, j + 2, val);
        }

        sub = null;

    } // End of method convertScript

    /**
     * @param sub
     *            TODO
     ***************************************************************************/

    private static void substitute(StringBuilder stringbuffer, Map<String, String> sub, int i, int j, String s) {
        String key = "<@" + sub.size() + "@>";
        sub.put(key, s);
        stringbuffer.replace(i, j, key);
    }

    /**
     * @param sub
     *            TODO
     ***************************************************************************/

    private static void replaceWord(StringBuilder stringbuffer, Map<String, String> sub, String s1, String s2) {

        while (true) {
            int i = find(stringbuffer, s1);
            if (i < 0) {
                break;
            }
            substitute(stringbuffer, sub, i, i + s1.length(), s2);
        }

    } // End of method replace

    /**
     * @param sub
     *            TODO
     ***************************************************************************/

    private static void replaceString(StringBuilder stringbuffer, Map<String, String> sub, String s1, String s2) {

        while (true) {
            int i = stringbuffer.indexOf(s1);
            if (i < 0) {
                break;
            }
            substitute(stringbuffer, sub, i, i + s1.length(), s2);
        }

    } // End of method replaceString

    /**
     * @param sub
     *            TODO
     ***************************************************************************/

    private static void processStructure(StringBuilder stringbuffer, Map<String, String> sub, String sname, Class<?> klass) {

        try {
            Field[] fields = klass.getDeclaredFields();
            int nf = fields.length;
            for (int i = 0; i < nf; i++) {
                String fname = sname + "." + fields[i].getName();
                replaceWord(stringbuffer, sub, fname, fname);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        replaceWord(stringbuffer, sub, sname, sname);

    } // End of method processStructure

    /**
     * @param sub
     *            TODO
     ***************************************************************************/

    private static boolean processFormat(StringBuilder stringbuffer, Map<String, String> sub) {
        int i = find(stringbuffer, "Format");
        System.out.println("format i=" + i);
        if (i < 0) {
            return false;
        }

        int j = immediateIndexOf(stringbuffer, "(", i + 6);
        System.out.println("format j=" + j);
        if (j < 0) {
            return false;
        }

        int k = immediateIndexOf(stringbuffer, "'", j + 1);
        System.out.println("format k=" + k);
        if (k < 0) {
            return false;
        }

        int l = stringbuffer.indexOf("'", k + 1);
        System.out.println("format l=" + l);
        if (l < 0) {
            return false;
        }

        String fmt = stringbuffer.substring(k + 1, l);

        int p = immediateIndexOf(stringbuffer, ",", l + 1);
        if (p < 0) {
            return false;
        }

        int m = immediateIndexOf(stringbuffer, "[", p + 1);
        System.out.println("format m=" + m);
        if (m < 0) {
            return false;
        }

        int n = stringbuffer.indexOf("]", m + 1);
        System.out.println("format n=" + n);
        if (n < 0) {
            return false;
        }

        int o = immediateIndexOf(stringbuffer, ")", n + 1);
        System.out.println("format o=" + o);
        if (o < 0) {
            return false;
        }

        String args = stringbuffer.substring(m + 1, n);

        StringBuilder snew = new StringBuilder("Format('").append(fmt).append("',").append(args).append(')');

        System.out.println("new : " + snew);

        replaceWord(snew, sub, "Format", "Format");
        stringbuffer.replace(i, o + 1, snew.toString());

        return true;
    }

    /*****************************************************************************/

    private static int immediateIndexOf(StringBuilder stringbuffer, String what, int start) {
        int i = stringbuffer.indexOf(what, start);
        if (i < 0) {
            return i;
        }

        // if something between start and i
        if (stringbuffer.substring(start, i).trim().length() > 0) {
            return -1;
        }
        return i;
    }

    /**
     * @param sub
     *            TODO
     ***************************************************************************/

    private static boolean processInputQuery(StringBuilder stringbuffer, Map<String, String> sub) {
        int i = find(stringbuffer, "InputQuery");
        if (i < 0) {
            return false;
        }

        int j = stringbuffer.indexOf(",", i + 1);
        if (j < 0) {
            return false;
        }

        int k = stringbuffer.indexOf(",", j + 1);
        if (k < 0) {
            return false;
        }

        int l = stringbuffer.indexOf(")", k + 1);
        if (k < 0) {
            return false;
        }

        String call = stringbuffer.substring(i, l + 1);
        String varname = stringbuffer.substring(k + 1, l).trim();

        StringBuilder stringbuffernew = new StringBuilder(varname).append(":=").append(call);

        replaceWord(stringbuffernew, sub, "InputQuery", "InputQuery");
        stringbuffer.replace(i, l + 1, stringbuffernew.toString());

        return true;

    } // End of method processInputQuery

    /**
     * @param functions
     *            TODO
     ***************************************************************************/

    private static boolean processProcedure(StringBuilder stringbuffer, List<String> functions) {
        int i = find(stringbuffer, "procedure");
        if (i < 0) {
            return false;
        }

        int j = find(stringbuffer, "begin", i + 1);
        if (j < 0) {
            return false;
        }

        StringBuilder sdef = new StringBuilder(stringbuffer.substring(i + 9, j).trim());
        int k = sdef.indexOf("(");
        int l = sdef.indexOf(")", k + 1);

        if ((k >= 0) && (l >= 0)) {
            // arguments
            StringTokenizer tk = new StringTokenizer(sdef.substring(k + 1, l), " :;");
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
            stringbuffer.replace(i + 9, j, sdef.toString() + "\n");
        } else {
            k = i + 9;
            while (stringbuffer.charAt(k) == ' ') {
                k++;
            }

            // beginning of the name
            l = k;
            k++;
            while (isInName(stringbuffer.charAt(k))) {
                k++;
                // end of the name
            }

            // save name of no-arg function to check calls later
            String funcname = stringbuffer.substring(l, k).trim();
            functions.add(funcname);
        }

        stringbuffer.replace(i, i + 9, "function ");

        return true;

    } // End of method processProcedure

    /*****************************************************************************/

    private static boolean isInName(char c) {
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

    /**
     * @param sub
     *            TODO
     ***************************************************************************/

    private static boolean processWith(StringBuilder stringbuffer, Map<String, String> sub) {
        int i = find(stringbuffer, "with");
        if (i < 0) {
            return false;
        }

        int j = find(stringbuffer, "do", i);
        if (j < 0) {
            return false;
        }

        int k = find(stringbuffer, "begin", j);
        if (k < 0) {
            return false;
        }

        int l = find(stringbuffer, "end", k);
        if (l < 0) {
            return false;
        }

        String var = stringbuffer.substring(i + 4, j).trim();
        StringBuilder stringbufferlock = new StringBuilder(stringbuffer.substring(k + 5, l));

        if (var.equals("Flame")) {
            processFields(stringbufferlock, sub, var, JSFlame.class);
        } else if (var.equals("Transform")) {
            processFields(stringbufferlock, sub, var, JSTransform.class);
        } else if (var.equals("Options")) {
            processFields(stringbufferlock, sub, var, JSOptions.class);
        } else if (var.equals("Renderer")) {
            processFields(stringbufferlock, sub, var, JSRenderer.class);
        } else if (var.equals("TStringList")) {
            processFields(stringbufferlock, sub, var, JSStringList.class);
        }

        stringbuffer.replace(k + 5, l, stringbufferlock.toString());
        stringbuffer.replace(i, j + 2, "");

        return true;

    } // End of method processWithStatements

    /**
     * @param sub
     *            TODO
     ***************************************************************************/

    private static void processFields(StringBuilder stringbuffer, Map<String, String> sub, String varname, Class<?> klass) {
        try {
            Field[] fields = klass.getDeclaredFields();
            int nf = fields.length;
            for (int i = 0; i < nf; i++) {
                String name = fields[i].getName();
                replaceWord(stringbuffer, sub, varname + "." + name, varname + "." + name);
                replaceWord(stringbuffer, sub, name, varname + "." + name);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    } // End of method processFields

    /**
     * @param sub
     *            TODO
     ***************************************************************************/

    private static boolean processCaseStatement(StringBuilder stringbuffer, Map<String, String> sub) {
        int i = find(stringbuffer, "case");
        if (i < 0) {
            return false;
        }

        int j = find(stringbuffer, "of");
        if (j < 0) {
            return false;
        }

        String control = stringbuffer.substring(i + 4, j).trim();

        int k = find(stringbuffer, "end", j + 1);
        if (k < 0) {
            return false;
        }

        StringBuilder stringbufferlock = new StringBuilder(stringbuffer.substring(j + 2, k));
        int l = 0;
        String stringbufferreak = "";
        while (true) {
            int i1 = stringbufferlock.indexOf("\n", l);
            if (i1 < 0) {
                break;
            }
            int i2 = stringbufferlock.indexOf(":", i1);
            if (i2 < 0) {
                break;
            }
            stringbufferlock.replace(i1, i1 + 1, stringbufferreak + "\ncase ");
            stringbufferreak = "break";
            l = i2 + 4;
        }

        replaceWord(stringbufferlock, sub, "case", "case");

        stringbuffer.replace(i, k + 3, "switch(" + control + ")\n{\n" + stringbufferlock.toString() + "}");

        return true;

    } // End of method processCaseStatement

    /**
     * @param sub
     *            TODO
     ***************************************************************************/

    private static boolean processDeleteStatement(StringBuilder stringbuffer, Map<String, String> sub) {
        int i = find(stringbuffer, "delete");
        if (i < 0) {
            return false;
        }

        int j = stringbuffer.indexOf("(", i + 1);
        if (j < 0) {
            return false;
        }

        int k = stringbuffer.indexOf(",", j + 1);
        if (k < 0) {
            return false;
        }

        int l = stringbuffer.indexOf(",", k + 1);
        if (l < 0) {
            return false;
        }

        int m = stringbuffer.indexOf(")", l + 1);
        if (m < 0) {
            return false;
        }

        String varname = stringbuffer.substring(j + 1, k).trim();
        String from = stringbuffer.substring(k + 1, l).trim();
        String len = stringbuffer.substring(l + 1, m).trim();

        StringBuilder stringbuffernew = new StringBuilder(varname).append(" := ").append("Delete(").append(varname).append(',').append(from).append(',')
                .append(len).append(')');
        replaceWord(stringbuffernew, sub, "delete", "Delete");

        stringbuffer.replace(i, m + 1, stringbuffernew.toString());

        return true;

    } // End of method processDeleteStatement

    /**
     * @param functions
     *            TODO
     ***************************************************************************/

    private static void getNiladicFunctions(List<String> functions) {
        Method[] m = Script.class.getDeclaredMethods();
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

    private static int find(StringBuilder stringbuffer, String word) {
        return find(stringbuffer, word, 0);
    }

    private static int find(StringBuilder stringbuffer, String word, int istart) {
        int l = word.length();
        int i = -1;

        String word1 = word.toLowerCase();
        String word2 = word.toUpperCase();
        String word3 = word2.substring(0, 1) + word1.substring(1);
        String word4 = word1.substring(0, 1) + word.substring(1);
        String word5 = word2.substring(0, 1) + word.substring(1);
        while (true) {
            i = 999999;
            int i0 = stringbuffer.indexOf(word, istart);
            int i1 = stringbuffer.indexOf(word1, istart);
            int i2 = stringbuffer.indexOf(word2, istart);
            int i3 = stringbuffer.indexOf(word3, istart);
            int i4 = stringbuffer.indexOf(word4, istart);
            int i5 = stringbuffer.indexOf(word5, istart);
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
                if (Character.isLetterOrDigit(stringbuffer.charAt(i - 1))) {
                    istart = i + l;
                    continue;
                }
            }

            if ((i + l) < stringbuffer.length()) {
                if (Character.isLetterOrDigit(stringbuffer.charAt(i + l))) {
                    istart = i + l;
                    continue;
                }
            }

            break;
        }

        return i;
    }

    /**
     * @param sub
     *            TODO
     ***************************************************************************/

    private static void processFunctionCalls(StringBuilder stringbuffer, Map<String, String> sub) {

        // check function calls for spelling and parentheses

        try {
            Method[] methods = Script.class.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                if (!methods[i].getName().startsWith("_")) {
                    continue;
                }
                Class<?>[] params = methods[i].getParameterTypes();
                String myname = methods[i].getName().substring(1);
                if (params.length == 0) {
                    replaceWord(stringbuffer, sub, myname, myname + "()");
                } else {
                    replaceWord(stringbuffer, sub, myname, myname);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    } // End of method checkFunctionCalls

    /*****************************************************************************/

} // End of class ScriptConverter
