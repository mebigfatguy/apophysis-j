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

public class ScriptRenderer extends MyThinlet implements Constants,
		ThreadTarget {

	/*****************************************************************************/
	// CONSTANTS

	/*****************************************************************************/
	// FIELDS

	double pixels_per_unit;
	long starttime;

	ControlPoint cp = new ControlPoint();
	Renderer renderer = null;
	int[][] colormap = new int[256][3];
	String filename;
	int imagewidth, imageheight;
	int oversample;
	double zoom, sample_density, brightness, gamma, vibrancy, filter_radius;
	double[] center = new double[2];

	boolean cancelled = false;

	Script script = null;
	JSRenderer jsrenderer = null;

	/*****************************************************************************/

	ScriptRenderer(String title, String xmlfile, int width, int height,
			Script script, JSRenderer jsrenderer) throws Exception {
		super(title, xmlfile, width, height);

		this.script = script;
		this.jsrenderer = jsrenderer;

		launcher.setResizable(false);

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

	} // End of method show

	/*****************************************************************************/

	public void setRenderBounds() {
		cp.copy(Global.script.cp);
		cp.adjustScale(Global.script.renderer.Width,
				Global.script.renderer.Height);
		cp.center[0] = Global.script.cp.center[0];
		cp.center[1] = Global.script.cp.center[1];
		cp.zoom = Global.script.cp.zoom;

	} // End of method setRenderBounds

	/*****************************************************************************/

	public void render() {
		if (renderer != null)
			return;

		renderer = new Renderer(this);

		cp.copy(Global.script.cp);
		filename = Global.script.renderer.Filename;
		cp.adjustScale(Global.script.renderer.Width,
				Global.script.renderer.Height);

		renderer.setCP(cp);
		starttime = System.currentTimeMillis();

		renderer.render();

		renderer.freeBuckets();
		cp = null;
		colormap = null;

		if (renderer.fstop == 0) {
			try {
				System.out.println("Saving image to " + filename);
				renderer.saveImage(filename, jsrenderer.Comment,
						jsrenderer.Encrypted, jsrenderer.Watermark);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		hide();

		launcher.dispose();

	} // End of method render

	/*****************************************************************************/

	public void output(String msg) {
		script._print(msg);
		System.out.println(msg);
	}

	public void progress(double value) {
		int k = (int) (value * 100);
		setInteger(find("ProgressBar"), "value", k);

		if (value > 0.05) {
			long elapsed = System.currentTimeMillis() - starttime;
			long remaining = (long) ((1 - value) * elapsed / value);
			String s = convertTime(remaining);
			setString(find("ProgressMessage"), "text", "Remaining : " + s);
		}

	} // End of method progress

	/*****************************************************************************/

	public void message(int index) {
		System.out.println("message " + index);
	}

	/*****************************************************************************/

	public void btnCancelClick() {
		System.out.println("cancelling " + renderer);
		if (renderer != null)
			renderer.stop();

	}

	/*****************************************************************************/

	String convertTime(long millis) {
		long hr = millis / 3600000L;
		millis -= hr * 3600000L;

		long mn = millis / 60000L;
		millis -= mn * 60000L;

		long sc = millis / 1000L;

		return ((hr < 10) ? ("0" + hr) : ("" + hr)) + ":"
				+ ((mn < 10) ? ("0" + mn) : ("" + mn)) + ":"
				+ ((sc < 10) ? ("0" + sc) : ("" + sc));

	}

	/*****************************************************************************/

} // End of class ScriptRenderer

