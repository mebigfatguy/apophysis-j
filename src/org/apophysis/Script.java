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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;

public class Script extends MyThinlet implements Constants {

	/*****************************************************************************/
	// CONSTANTS

	/*****************************************************************************/
	// FIELDS

	static int nprefix = 0; // number of lines in prefix script

	Object console;

	public JSFlame flame = new JSFlame(this);
	public JSTransform transform = new JSTransform(this);
	public JSOptions options = new JSOptions();
	public JSRenderer renderer = new JSRenderer();
	public JSStringList stringlist = new JSStringList();

	int _at_ = 0; // active transform number

	Context cx = null;
	Scriptable scope = null;

	String scriptName = "NewScript";

	StringBuffer log = null;

	Runner runner = null;

	ControlPoint cp = new ControlPoint();
	Map<Integer, ControlPoint> cps = new ConcurrentHashMap<Integer, ControlPoint>();
	List<ControlPoint> cpf = new ArrayList<ControlPoint>();

	Triangle[] triangles = new Triangle[NXFORMS + 2];

	boolean updateflame = true;
	boolean resetlocation = false;

	/*****************************************************************************/

	Script(String title, String xmlfile, int width, int height)
			throws Exception {
		super(title, xmlfile, width, height);

		console = find("Console");

		for (int i = 0; i < triangles.length; i++) {
			triangles[i] = new Triangle();
		}
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

		launcher.setTitle(scriptName);

	} // End of method show

	/*****************************************************************************/

	public void btnNewClick() {

		loadScript("", "New Script");

	} // End of method btnnewClick

	/*****************************************************************************/

	public void btnOpenClick() {

		Task task = new OpenTask();
		Global.opendialog = new OpenDialog(this, Global.browserPath, task);

		Global.opendialog
				.addFilter("Apophysis-j script files (*.ajs)", "*.ajs");
		Global.opendialog.addFilter("Apophysis script files (*.asc)", "*.asc");

		Global.opendialog.show();

	} // End of method btnOpenClick

	/*****************************************************************************/

	void openFile(String filename, boolean mustconvert) {
		File file = new File(filename);
		loadScript(readScript(file, mustconvert), file.getName());
	} // End of method openFile

	/*****************************************************************************/

	String readScript(File file, boolean mustconvert) {
		StringBuffer sb = new StringBuffer();

		try (BufferedReader r = new BufferedReader(new FileReader(file))) {
			while (true) {
				String line = r.readLine();
				if (line == null) {
					break;
				}
				sb.append(line);
				sb.append('\n');
			}

			if (mustconvert) {
				ScriptConverter.convert(sb);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return sb.toString();

	} // End of method readScript

	/*****************************************************************************/

	void loadScript(String script, String title) {
		int i = title.lastIndexOf('.');
		if (i > 0) {
			title = title.substring(0, i);
		}
		scriptName = title;
		launcher.setTitle(scriptName);

		setString(find("Editor"), "text", script);

		setBoolean(find("btnRun"), "enabled", true);
		setBoolean(find("btnStop"), "enabled", false);

		requestFocus(find("Editor"));

	} // End of method loadScript

	/*****************************************************************************/

	public void btnSaveClick() {
		Task task = new SaveTask();
		Global.savedialog = new SaveDialog(this, Global.browserPath, scriptName
				+ ".ajs", task);
		Global.savedialog.show();

	}

	/*****************************************************************************/

	void saveFile(String filename) {
		String script = getString(find("Editor"), "text");

		File file = new File(filename);
		scriptName = file.getName();
		int i = scriptName.lastIndexOf('.');
		if (i >= 0) {
			scriptName = scriptName.substring(0, i);
		}

		try (PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(filename)))) {
			w.print(script);
			launcher.setTitle(scriptName);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	} // End of method saveFile

	/*****************************************************************************/

	public void btnRunClick() {
		if (runner != null) {
			return;
		}

		runner = new Runner();
		runner.start();

	} // End of method btnRunClick

	/*****************************************************************************/

	public void btnStopClick() {
		if (scope == null) {
			return;
		}

		runner = null;

		ScriptableObject.putProperty(scope, "Stopped",
				Context.javaToJS(Boolean.TRUE, scope));

	}

	/*****************************************************************************/

	public void btnHelpClick() {
		Global.helper.show();
		Global.helper.setTopicByName("scripting");
	}

	/*****************************************************************************/

	String getPrefixScript() {
		StringBuilder sb = new StringBuilder();

		// expose all the methods starting with _
		try {
			Method[] methods = this.getClass().getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				if (!methods[i].getName().startsWith("_")) {
					continue;
				}
				Class<?>[] params = methods[i].getParameterTypes();
				Class<?> answer = methods[i].getReturnType();

				String myname = methods[i].getName().substring(1);

				String paren;
				if (params.length == 0) {
					paren = "()";
				} else {
					paren = "(";
					for (int j = 1; j < params.length; j++) {
						paren += "a" + j + ",";
					}
					paren += "a" + params.length + ")";
				}
				sb.append("function ");
				sb.append(myname);
				sb.append(paren);
				sb.append(" { ");
				if (answer != null) {
					sb.append("return ");
				}
				sb.append("__me._");
				sb.append(myname);
				sb.append(paren);
				sb.append(" } \n");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		sb.append("\n");

		return sb.toString();

	}

	/*****************************************************************************/

	String getLibraryScript() {
		if (Global.defLibrary.equals("")) {
			return "";
		}

		String ext = "ajs";
		int i = Global.defLibrary.lastIndexOf('.');
		if (i > 0) {
			ext = Global.defLibrary.substring(i + 1).toLowerCase();
		}

		return readScript(new File(Global.defLibrary), ext.equals("asc"));

	} // End of method getLibraryScript

	/*****************************************************************************/

	int countLines(String s) {
		int n = 0;
		int l = s.length();
		for (int i = 0; i < l; i++) {
			if (s.charAt(i) == '\n') {
				n++;
			}
		}

		return n;
	}

	/*****************************************************************************/

	String getPostfixScript() {
		StringBuilder sb = new StringBuilder();

		/*
		 * sb.append("} \n"); // end of myrun function definition
		 * sb.append("\n"); sb.append("myrun() \n"); // myrun invokation
		 */

		return sb.toString();
	}

	/*****************************************************************************/
	/*****************************************************************************/
	// SCRIPT COMMANDS

	public void _RotateFlame(double a) {
		double radians = a * Math.PI / 180.0;

		js2java();

		cp.trianglesFromCP(triangles);
		for (int i = 0; i < cp.nxforms; i++) {
			triangles[i].rotate(radians);
		}
		triangles[M1].rotate(radians);
		cp.getFromTriangles(triangles, cp.nxforms);

		java2js();
	}

	/*****************************************************************************/

	public void _RotateReference(double a) {
		double radians = a * Math.PI / 180.0;

		js2java();

		XForm tx = new XForm();
		tx.copy(cp.xform[cp.nxforms]);
		cp.trianglesFromCP(triangles);
		triangles[M1].rotate(radians);
		cp.getFromTriangles(triangles, cp.nxforms);
		cp.xform[cp.nxforms].copy(tx);

		java2js();
	}

	/*****************************************************************************/

	public void _Rotate(double a) {
		js2java();
		if ((_at_ >= 0) && (_at_ < NXFORMS)) {
			cp.xform[_at_].rotate(a);
			java2js();
		}
	}

	/*****************************************************************************/

	public void _Multiply(double a, double b, double c, double d) {
		js2java();
		if ((_at_ < 0) || (_at_ >= cp.nxforms)) {
			return;
		}

		cp.xform[_at_].multiply(a, b, c, d);

		java2js();

	}

	/*****************************************************************************/

	public void _StoreFlame(int index) {
		js2java();

		ControlPoint tcp = new ControlPoint();
		tcp.copy(cp);
		cps.put(Integer.valueOf(index), tcp);
	}

	/*****************************************************************************/

	public void _GetFlame(int index) {
		ControlPoint tcp = cps.get(Integer.valueOf(index));
		if (tcp != null) {
			js2java();
			cp.copy(tcp);
			java2js();
		}
	}

	/*****************************************************************************/

	public void _LoadFlame(int index) {
		ControlPoint tcp = Global.main.cps.get(index);
		if (tcp != null) {
			js2java();
			cp.copy(tcp);
			java2js();
		}
	}

	/*****************************************************************************/

	public void _Scale(double a) {
		js2java();
		if ((_at_ < 0) || (_at_ >= cp.nxforms)) {
			return;
		}

		cp.xform[_at_].scale(a);

		java2js();
	}

	/*****************************************************************************/

	public void _Translate(double a, double b) {
		js2java();
		if ((_at_ < 0) || (_at_ >= cp.nxforms)) {
			return;
		}

		cp.xform[_at_].translate(a, b);

		java2js();
	}

	/*****************************************************************************/

	public void _SetActiveTransform(int index) {
		js2java();

		_at_ = index;

		if ((_at_ >= 0) && (_at_ <= cp.nxforms)) {
			transform.java2js(cp, _at_);
		}

		java2js();

	} // End of method scriptSetActiveTransform

	/*****************************************************************************/

	public int _FileCount() {
		return cpf.size();
	}

	/*****************************************************************************/

	public void _AddTransform() {
		js2java();
		if (cp.nxforms < NXFORMS) {
			_at_ = cp.nxforms;
			cp.nxforms++;

			// copy final xform if any
			cp.xform[cp.nxforms].copy(cp.xform[_at_]);

			cp.xform[_at_].clear();
			cp.xform[_at_].density = 0.5;

			transform.java2js(cp, _at_);
			java2js();
		}
	}

	/*****************************************************************************/

	public void _DeleteTransform() {
		js2java();
		if (cp.nxforms > 0) {
			for (int i = _at_; i <= cp.nxforms; i++) {
				cp.xform[i].copy(cp.xform[i + 1]);
			}
			cp.nxforms--;
			if (_at_ >= cp.nxforms) {
				_at_ = cp.nxforms - 1;
			}

			java2js();
		}
	}

	/*****************************************************************************/

	public JSFlame _CloneFlame(JSFlame fold) {
		js2java();
		JSFlame fnew = fold.Clone();
		java2js();
		return fnew;
	}

	/*****************************************************************************/

	public JSTransform _CloneTransform(JSTransform told) {
		js2java();
		JSTransform tnew = told.Clone();
		java2js();
		return tnew;
	}

	/*****************************************************************************/

	public void _CopyTransform() {
		js2java();
		if (cp.nxforms < NXFORMS) {
			int old = _at_;
			_at_ = cp.nxforms;
			cp.nxforms++;
			cp.xform[cp.nxforms].copy(cp.xform[_at_]);
			cp.xform[_at_].copy(cp.xform[old]);

			java2js();
		}
	}

	/*****************************************************************************/

	public void _Clear() {
		js2java();

		cp.nxforms = 0;
		_at_ = -1;
		cp.clear();
		cp.xform[0].symmetry = 1;

		java2js();
	}

	/*****************************************************************************/

	public void _Preview() {
		if (runner == null) {
			return;
		}

		if (cp.nxforms > 0) {
			js2java();
			Global.preview.cp.copy(cp);
			Global.preview.show();
			Global.preview.drawFlame();
		}
	}

	/*****************************************************************************/

	public void _Render() {
		js2java();

		try {
			ScriptRenderer srenderer = new ScriptRenderer("Rendering",
					"srenderer.xml", 270, 90, this, renderer);

			srenderer.show();
			srenderer.render();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/*****************************************************************************/

	public String _Format(String fmt, Object a1, Object a2, Object a3,
			Object a4, Object a5, Object a6, Object a7, Object a8, Object a9) {
		Object[] args = new Object[] { a1, a2, a3, a4, a5, a6, a7, a8, a9 };
		return StringFormatter.format(fmt, args);

	}

	/*****************************************************************************/

	public void _Print(Object o) {
		_print(o);
	}

	public void _print(Object o) {
		log.append(Context.toString(o));
		log.append('\n');
		setString(console, "text", log.toString());

		// position the widget to the end
		int n = log.length();
		setInteger(console, "start", n - 1);
		setInteger(console, "end", n - 1);
	}

	/*****************************************************************************/

	public void _AddSymmetry(int n) {
		js2java();
		cp.addSymmetry(n);
		java2js();
	}

	/*****************************************************************************/

	public void _Morph(int a, int b, double c) {
		ControlPoint cpa = cps.get(Integer.valueOf(a));
		ControlPoint cpb = cps.get(Integer.valueOf(b));

		if ((cpa != null) && (cpb != null)) {
			cp.interpolateX(cpa, cpb, c);
			java2js();
		}
	}

	/*****************************************************************************/

	public void _SetRenderBounds() {
	}

	/*****************************************************************************/

	public void _SetFlameFile(String filename) throws Exception {
		File file = new File(filename);
		if (!file.exists()) {
			throw new IOException("File not found");
		}

		BufferedReader r = null;
		try {
			r = new BufferedReader(new FileReader(filename));
			cpf = Global.main.readXML(r);
		} finally {
		    IOUtils.close(r);
		}
	}

	/*****************************************************************************/

	public void _ListFile(String filename) throws IOException {
		File file = new File(filename);
		if (file.exists()) {
			File[] files = new File[] { file };
			Global.main.openFiles(files);
			cp.copy(Global.mainCP);
			_at_ = 0;
			java2js();
		} else {
			throw new IOException("Cannot open " + filename);
		}
	}

	/*****************************************************************************/

	public void _SaveFlame(String filename) {
		// TODO - should not replace the file, but append the flame
		js2java();
		Global.main.saveXMLFile(cp, filename);
	}

	/*****************************************************************************/

	public String _GetFileName() {
		Global.opendialog = new OpenDialog(Global.main, Global.browserPath,
				null);
		Global.opendialog.addFilter("Flame files (*.flame)", "*.flame");
		Global.opendialog.addFilter("Fla files (*.fla)", "*.fla");
		Global.opendialog.addFilter("All files (*.*)", "*.*");
		Global.opendialog.show();

		Global.main.launcher.toFront();
		Global.main.requestFocus();

		while (true) {
			try {
				Thread.sleep(100);
			} catch (Exception ex) {
			}
			Object dialog = Global.main.find("opendialog");
			if (dialog == null) {
				break;
			}
		}

		return Global.opendialog.filename;
	}

	/*****************************************************************************/

	public void _ShowStatus(String msg) {
		setString(find("StatusBar"), "text", msg);
		Global.main.setStatus(msg);
	}

	/*****************************************************************************/

	public void _RandomFlame(int n) {
		cp = ControlPoint.randomFlame(cp, n);
		java2js();
	}

	/*****************************************************************************/

	public void _RandomGradient() {
		js2java();
		cp.cmap = CMap.randomGradient();
		java2js();
	}

	/*****************************************************************************/

	public void _SaveGradient(String filename, String title) {
		js2java();
		String text = CMap.gradientFromPalette(cp.cmap, title);
		FileManager.writeEntry(text, title, filename);
	}

	/*****************************************************************************/

	public int _SetVariation(int index) {
		return 0;
	}

	/*****************************************************************************/

	public int _VariationIndex(String name) {
		int nv = XForm.getNrVariations();
		for (int i = 0; i < nv; i++) {
			if (XForm.getVariation(i).getName().equals(name)) {
				return i;
			}
		}
		return -1;
	}

	/*****************************************************************************/

	public String _VariationName(int index) {
		int nv = XForm.getNrVariations();
		if ((index >= 0) && (index < nv)) {
			return XForm.getVariation(index).getName();
		} else {
			return "";
		}
	}

	/*****************************************************************************/

	public String _VariableName(int index) {
		return XForm.getParameterName(index);
	}

	/*****************************************************************************/

	public void _CalculateScale() {
		js2java();
		double x = cp.center[0];
		double y = cp.center[1];
		cp.calcBoundBox();
		cp.center[0] = x;
		cp.center[1] = y;
		java2js();
	}

	/*****************************************************************************/

	public void _CalculateBounds() {
		js2java();
		cp.calcBoundBox();
		java2js();
	}

	/*****************************************************************************/

	public void _NormalizeVars() {
		js2java();
		cp.normalizeVariations();
		java2js();
	}

	/*****************************************************************************/

	public String _GetSaveFileName(String filename) {
		if (filename == null) {
			filename = "";
		}
		Global.savedialog = new SaveDialog(Global.main, Global.browserPath,
				filename, null);
		Global.savedialog.show();

		Global.main.launcher.toFront();
		Global.main.requestFocus();

		while (true) {
			try {
				Thread.sleep(100);
			} catch (Exception ex) {
			}
			Object dialog = Global.main.find("savedialog");
			if (dialog == null) {
				break;
			}
		}

		return Global.savedialog.filename;
	}

	/*****************************************************************************/

	public void _ShowMessage(String msg) {
		Global.main.alertAndWait(msg);
	}

	/*****************************************************************************/
	// Pascal implementation

	public void _Exit() {
		_exit();
	}

	/*****************************************************************************/

	public void _exit() {
		throw new ExitException();
	}

	/*****************************************************************************/

	public String _IntToStr(int n) {
		return "" + n;
	}

	/*****************************************************************************/

	public int _StrToInt(String s) {
		return Integer.parseInt(s);
	}

	/*****************************************************************************/

	public int _Pos(String search, String text) {
		int i = text.indexOf(search);
		return i + 1;
	}

	/*****************************************************************************/

	public String _Delete(String s, int from, int len) {
		return s.substring(0, from - 1) + s.substring(from + len - 1);
	}

	/*****************************************************************************/

	public String _Copy(String s, int from, int len) {
		if (from < 0) {
			from = 0;
		}
		if (from + len > s.length()) {
			len = s.length() - from;
		}
		return s.substring(from, from + len);
	}

	/*****************************************************************************/

	public int _Length(String s) {
		return s.length();
	}

	/*****************************************************************************/

	public String _Lowercase(String s) {
		return s.toLowerCase();
	}

	public String _Uppercase(String s) {
		return s.toUpperCase();
	}

	/*****************************************************************************/

	public String _Trim(String s) {
		return s.trim();
	}

	/*****************************************************************************/

	public String _InputQuery(String title, String question, String value) {
		return Global.main.askAndWait(question, value);
	}

	/*****************************************************************************/

	public void _DeleteFile(String filename) {
		(new File(filename)).delete();
	}

	/*****************************************************************************/

	public boolean _DirectoryExists(String dirname) {
		File dir = new File(dirname);
		return dir.exists() && dir.isDirectory();
	}

	/*****************************************************************************/

	public boolean _FileExists(String filename) {
		return (new File(filename)).exists();
	}

	/*****************************************************************************/

	public String _ExtractFileName(String filename) {
		File file = new File(filename);
		return file.getName();
	}

	/*****************************************************************************/

	public String _ExtractFilePath(String filename) {
		File file = new File(filename);
		return file.getParent() + System.getProperty("file.separator");
	}

	/*****************************************************************************/

	public void _Mkdir(String dirname) {
		File dir = new File(dirname);
		dir.mkdir();
	}

	/*****************************************************************************/

	public boolean _RenameFile(String oldname, String newname) {
		File oldfile = new File(oldname);
		File newfile = new File(newname);
		return oldfile.renameTo(newfile);
	}

	/*****************************************************************************/

	public void _CopyFile(String src, String dst) throws IOException {
		File filesrc = new File(src);
		File filedst = new File(dst);
		Global.copyFile(filesrc, filedst);
	}

	/*****************************************************************************/
	/*****************************************************************************/
	// Math implementation

	public double _sin(double x) {
		return Math.sin(x);
	}

	public double _cos(double x) {
		return Math.cos(x);
	}

	public double _tan(double x) {
		return Math.tan(x);
	}

	public double _asin(double x) {
		return Math.asin(x);
	}

	public double _acos(double x) {
		return Math.acos(x);
	}

	public double _atan(double x) {
		return Math.atan(x);
	}

	public double _atan2(double x, double y) {
		return Math.atan2(x, y);
	}

	public double _exp(double x) {
		return Math.exp(x);
	}

	public double _log(double x) {
		return Math.log(x);
	}

	public long _round(double x) {
		return Math.round(x);
	}

	public double _trunc(double x) {
		return Math.floor(x);
	}

	public double _power(double x, double y) {
		return Math.pow(x, y);
	}

	public double _pow(double x, double y) {
		return Math.pow(x, y);
	}

	public double _random() {
		return Math.random();
	}

	public double _abs(double x) {
		return Math.abs(x);
	}

	public double _abs(int n) {
		return Math.abs(n);
	}

	public double _sqrt(double x) {
		return Math.sqrt(x);
	}

	public double _sqr(double x) {
		return x * x;
	}

	/*****************************************************************************/

	void java2js() {
		flame.cp.copy(cp);
		flame.java2js();
		if ((_at_ >= 0) && (_at_ <= cp.nxforms)) {
			transform.java2js(cp, _at_);
		}
		options.java2js();

		int iv = (Global.variation < 0) ? (int) (Math.random() * XForm.getNrVariations())
				: Global.variation;
		ScriptableObject.putProperty(scope, "Variation",
				Context.javaToJS(Integer.valueOf(iv), scope));

		ScriptableObject.putProperty(scope, "SelectedTransform", Context
				.javaToJS(Integer.valueOf(Global.editor.selectedTriangle), scope));

		ScriptableObject.putProperty(scope, "ActiveTransform",
				Context.javaToJS(Integer.valueOf(_at_), scope));

		ScriptableObject.putProperty(scope, "Transforms",
				Context.javaToJS(Integer.valueOf(cp.nxforms), scope));

		ScriptableObject.putProperty(scope, "BatchIndex",
				Context.javaToJS(Integer.valueOf(Global.randomIndex), scope));

		ScriptableObject.putProperty(scope, "Stopped",
				Context.javaToJS(Boolean.FALSE, scope));

		ScriptableObject.putProperty(scope, "CurrentFile",
				Context.javaToJS(Global.openFile, scope));

		ScriptableObject.putProperty(scope, "DateCode",
				Context.javaToJS(Global.randomDate, scope));

		ScriptableObject.putProperty(scope, "LimitVibrancy",
				Context.javaToJS(new Boolean(Global.limitVibrancy), scope));

		ScriptableObject.putProperty(scope, "ResetLocation",
				Context.javaToJS(new Boolean(resetlocation), scope));

		ScriptableObject.putProperty(scope, "UpdateFlame",
				Context.javaToJS(new Boolean(updateflame), scope));

	} // End of method java2js

	/*****************************************************************************/

	void js2java() {

		Object o;

		o = ScriptableObject.getProperty(scope, "UpdateFlame");
		if (o instanceof Boolean) {
			updateflame = ((Boolean) o).booleanValue();
		}

		o = ScriptableObject.getProperty(scope, "SelectedTransform");
		if (o instanceof Integer) {
			Global.editor.selectedTriangle = ((Integer) o).intValue();
		}

		o = ScriptableObject.getProperty(scope, "ActiveTransform");
		if (o instanceof Integer) {
			_at_ = ((Integer) o).intValue();
		}

		o = ScriptableObject.getProperty(scope, "BatchIndex");
		if (o instanceof Integer) {
			Global.randomIndex = ((Integer) o).intValue();
		}

		o = ScriptableObject.getProperty(scope, "DateCode");
		if (o instanceof String) {
			Global.randomDate = (String) o;
		}

		o = ScriptableObject.getProperty(scope, "LimitVibrancy");
		if (o instanceof Boolean) {
			Global.limitVibrancy = ((Boolean) o).booleanValue();
		}

		o = ScriptableObject.getProperty(scope, "ResetLocation");
		if (o instanceof Boolean) {
			resetlocation = ((Boolean) o).booleanValue();
		}

		o = ScriptableObject.getProperty(scope, "Transform");
		if (o instanceof JSTransform) {
			transform = (JSTransform) o;
		}

		flame.js2java();
		cp.copy(flame.cp);
		if ((_at_ >= 0) && (_at_ <= cp.nxforms)) {
			transform.js2java(cp, _at_);
		}

		options.js2java();
	} // End of method js2java

	/*****************************************************************************/

	public void executeScript() {
		int lineno;
		String errmsg;

		String userscript = getString(find("Editor"), "text");
		if (userscript.length() == 0) {
			runner = null;
			cx = null;
			scope = null;
			return;
		}

		try {
			setBoolean(find("btnRun"), "enabled", false);
			setBoolean(find("btnStop"), "enabled", true);

			cx = Context.enter();
			scope = cx.initStandardObjects();

			log = new StringBuffer();
			setString(console, "text", "");

			updateflame = true;
			resetlocation = false;

			cpf = Global.main.cps;

			cp.copy(Global.mainCP);
			_at_ = Global.editor.selectedTriangle;
			if ((_at_ < 0) || (_at_ >= Global.mainCP.nxforms)) {
				_at_ = 0;
			}

			renderer.Width = 320;
			renderer.Height = 240;
			renderer.Watermark = Global.watermark == 1;
			renderer.Encrypted = Global.encryptedComment == 1;
			renderer.Comment = Global.jpegComment == 1;

			createEnvironment();

			java2js();

			String s = getPrefixScript() + getLibraryScript();
			nprefix = countLines(s);
			s = s + userscript + getPostfixScript();

			cx.evaluateString(scope, s, "<cmd>", 1, null);

			// save javascript environment
			js2java();

			Global.main.stopThread();

			Global.main.updateUndo();
			Global.mainCP = new ControlPoint();
			Global.mainCP.copy(cp);
			Global.transforms = Global.mainCP.nxforms;
			Global.main.updateWindows();

			if (resetlocation) {
				Global.main.resetLocation();
			}

			if (updateflame) {
				Global.main.timer.enable();
			}

		} catch (WrappedException wex) {
			Throwable t = wex.getWrappedException();
			errmsg = getMessage(t);
			if (errmsg == null) {
				errmsg = getMessage(wex);
			}
			lineno = getLineNumber(t, nprefix);
			if (!(t instanceof ExitException)) {
				_print(errmsg + " (line " + lineno + ")");
			}
		} catch (RhinoException rex) {
			errmsg = getMessage(rex);
			lineno = getLineNumber(rex, nprefix);
			_print(errmsg + " (line " + lineno + ")");
		} catch (Exception ex) {
			System.out.println("THIS IS AN EXCEPTION");
			ex.printStackTrace();
			System.out.println("--------------------------");
			_print(ex.toString());
		}

		setBoolean(find("btnRun"), "enabled", true);
		setBoolean(find("btnStop"), "enabled", false);

		runner = null;
		cx = null;
		scope = null;

	} // End of method run

	/*****************************************************************************/

	int getLineNumber(Throwable t, int nprefix) {

		StackTraceElement[] s = t.getStackTrace();
		for (StackTraceElement element : s) {
			if (element.getFileName().equals("<cmd>")) {
				if (element.getLineNumber() > 0) {
					if (element.getLineNumber() > nprefix) {
						return element.getLineNumber() - nprefix;
					}
				}
			}
		}

		return 0;
	}

	/*****************************************************************************/

	String getMessage(Throwable t) {
		String msg = t.getMessage();
		if (msg == null) {
			return null;
		}

		int i = msg.indexOf('(');
		if (i > 0) {
			msg = msg.substring(0, i);
		}

		return msg;
	}

	/*****************************************************************************/

	void createEnvironment() {
		String curdir = System.getProperty("user.dir") + "/";
		ScriptableObject.putProperty(scope, "INSTALLPATH",
				Context.javaToJS(curdir, scope));

		ScriptableObject.putProperty(scope, "Options",
				Context.javaToJS(options, scope));

		ScriptableObject.putProperty(scope, "Flame",
				Context.javaToJS(flame, scope));

		ScriptableObject.putProperty(scope, "Transform",
				Context.javaToJS(transform, scope));

		ScriptableObject.putProperty(scope, "Renderer",
				Context.javaToJS(renderer, scope));

		ScriptableObject.putProperty(scope, "TStringList",
				Context.javaToJS(stringlist, scope));

		ScriptableObject.putProperty(scope, "PI",
				Context.javaToJS(new Double(Math.PI), scope));

		ScriptableObject.putProperty(scope, "NXFORMS",
				Context.javaToJS(Integer.valueOf(NXFORMS), scope));

		int nv = XForm.getNrVariations();
		ScriptableObject.putProperty(scope, "NVARS",
				Context.javaToJS(Integer.valueOf(nv), scope));

		int np = XForm.getNrParameters();
		ScriptableObject.putProperty(scope, "NumVariables",
				Context.javaToJS(Integer.valueOf(np), scope));

		ScriptableObject.putProperty(scope, "SYM_NONE",
				Context.javaToJS(Integer.valueOf(0), scope));

		ScriptableObject.putProperty(scope, "SYM_BILATERAL",
				Context.javaToJS(Integer.valueOf(1), scope));

		ScriptableObject.putProperty(scope, "SYM_ROTATIONAL",
				Context.javaToJS(Integer.valueOf(2), scope));

		ScriptableObject.putProperty(scope, "SYM_DIHEDRAL",
				Context.javaToJS(Integer.valueOf(3), scope));

		for (int i = 0; i < nv; i++) {
			String vname = XForm.getVariation(i).getName().toUpperCase();
			ScriptableObject.putProperty(scope, "V_" + vname,
					Context.javaToJS(Integer.valueOf(i), scope));
		}

		int ip = 0;
		for (int i = 0; i < nv; i++) {
			Variation variation = XForm.getVariation(i);
			np = variation.getNrParameters();
			for (int j = 0; j < np; j++) {
				String pname = variation.getParameterName(j).toUpperCase();
				ScriptableObject.putProperty(scope, "V_" + pname,
						Context.javaToJS(Integer.valueOf(ip), scope));
				ip++;
			}
		}

		ScriptableObject.putProperty(scope, "__me",
				Context.javaToJS(this, scope));

	} // End of method createEnvironment

	/*****************************************************************************/

	public void caretChange(Object textarea) {

		int pos = getInteger(textarea, "end");
		char[] c = getString(textarea, "text").toCharArray();
		int line = 1;
		for (int i = 0; i < pos; i++) {
			if (c[i] == '\n') {
				line++;
			}
		}

		showStatus("Line " + line);
	}

	/*****************************************************************************/

	public void showStatus(String msg) {
		setString(find("StatusBar"), "text", msg);
	}

	/*****************************************************************************/

	void clearTransform() {
		js2java();
		cp.xform[_at_].clear();
		if (_at_ < cp.nxforms) {
			cp.xform[_at_].density = 0.5;
		} else {
			cp.xform[_at_].density = 1.0;
		}
		java2js();
	}

	/*****************************************************************************/

	void rotateTransform(double degrees) {
		js2java();

		double radians = degrees * Math.PI / 180.0;

		SPoint pivot = Global.editor.getPivot();
		cp.trianglesFromCP(triangles);
		triangles[_at_].rotateAroundPoint(pivot.x, pivot.y, radians);
		cp.getFromTriangles(triangles, cp.nxforms);

		java2js();
	}

	/*****************************************************************************/

	void scaleTransform(double s) {
		js2java();

		SPoint pivot = Global.editor.getPivot();
		cp.trianglesFromCP(triangles);
		triangles[_at_].scaleAroundPoint(pivot.x, pivot.y, s);
		cp.getFromTriangles(triangles, cp.nxforms);

		java2js();
	}

	/*****************************************************************************/

	void rotateOriginTransform(double degrees) {
		js2java();

		double radians = degrees * Math.PI / 180.0;
		double cos = Math.cos(radians);
		double sin = Math.sin(radians);

		int oldmode = Global.editor.pivotMode;
		Global.editor.pivotMode = Editor.PIVOT_WORLD;
		SPoint pivot = Global.editor.getPivot();
		Global.editor.pivotMode = oldmode;

		XForm xform = cp.xform[_at_];
		double tx = pivot.x + (xform.c20 - pivot.x) * cos
				- (-xform.c21 - pivot.y) * sin;
		double ty = pivot.y + (xform.c20 - pivot.x) * sin
				+ (-xform.c21 - pivot.y) * cos;
		xform.c20 = tx;
		xform.c21 = -ty;

		java2js();
	}

	/*****************************************************************************/
	/*****************************************************************************/

	class OpenTask implements Task {

	    @Override
		public void execute() {
			Global.browserPath = Global.opendialog.getBrowserPath();

			String ext = "ajs";
			int i = Global.opendialog.filename.lastIndexOf('.');
			if (i > 0) {
				ext = Global.opendialog.filename.substring(i + 1);
			}

			boolean mustconvert = ext.equals("asc");
			openFile(Global.opendialog.filename, mustconvert);
		}

	} // End of class OpenTask

	/*****************************************************************************/
	/*****************************************************************************/

	class SaveTask implements Task {

	    @Override
		public void execute() {
			Global.browserPath = Global.savedialog.getBrowserPath();
			saveFile(Global.savedialog.filename);
		}

	} // End of class SaveTask

	/*****************************************************************************/
	/*****************************************************************************/

	class Runner extends Thread {

		@Override
		public void run() {
			executeScript();
		}

	} // End of class Runner

	/*****************************************************************************/
	/*****************************************************************************/

	static class ExitException extends RuntimeException {
	}

	/*****************************************************************************/
	/*****************************************************************************/

} // End of class Script

