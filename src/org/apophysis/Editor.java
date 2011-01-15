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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.MemoryImageSource;

public class Editor extends MyThinlet implements Constants, ThreadTarget {

	/*****************************************************************************/
	// CONSTANTS

	static final int MODE_NONE = 0;
	static final int MODE_MOVE = 1;
	static final int MODE_ROTATE = 2;
	static final int MODE_SCALE = 3;
	static final int MODE_PICK = 4;

	static final int PIVOT_LOCAL = 0;
	static final int PIVOT_WORLD = 1;

	static final int ACTION_NONE = 0;
	static final int ACTION_SCROLL = 1;
	static final int ACTION_MOVE = 2;
	static final int ACTION_ROTATE = 3;
	static final int ACTION_SCALE = 4;
	static final int ACTION_ROTATE_EDGE = 5;
	static final int ACTION_SCALE_EDGE = 6;
	static final int ACTION_MOVE_CORNER = 7;
	static final int ACTION_WHIRL = 8;
	static final int ACTION_SLIDE = 10;
	static final int ACTION_SLIDE_CORNER = 12;

	static final int[] trgColors = { 0xFF0000, 0xFFFF00, 0x00FF00, 0x00FFFF,
			0x4040FF, 0xFF00FF, 0xFF7F00, 0xFF007F, 0xFFFF55, 0xCCFFCC,
			0xAAFFFF, 0xFF7F7F, 0xFFAAFF, 0xFFCC55 };

	static final int trgColorGray = Color.gray.getRGB();

	static final double PI2 = Math.PI / 2;

	/*****************************************************************************/
	// FIELDS

	int[][] cmap = new int[256][3];

	double[] fclick = new double[2];
	double[] fdrag = new double[2];
	double[] fmove = new double[2];

	double dclick; // distance to pivot when clicking
	double ddrag; // distance to pivot when dragging

	double aclick; // angle when clicking
	double adrag; // angle when dragging

	double cclick; // cos when clicking
	double sclick; // sin when clicking

	double[][][] widgets = new double[4][3][2];
	double xx, xy, yx, yy;

	SPoint rcenter = new SPoint(); // center of rotation
	double rradius; // radius of rotation

	boolean showhelpers = false;

	double p0x, p0y, p1x, p1y, p2x, p2y; // for scaling corners

	Triangle oldT = new Triangle();

	int action;

	int editMode, oldMode;

	boolean key_handled;
	boolean updating;
	Point mousePos;
	int mouseOverTriangle, mouseOverEdge, mouseOverCorner, mouseOverWidget;
	int selectedTriangle, selectedEdge, selectedCorner, selectedWidget;

	boolean selectMode = true;
	boolean extendedEdit = false;
	boolean axisLock = false;

	double graphZoom;

	boolean changed = false;
	boolean mustupdateflame = false;

	int[] polyx = new int[5];
	int[] polyy = new int[5];

	double gCenterX;
	double gCenterY;

	Triangle memTriangle;

	SPoint pivot = new SPoint();

	double[] tcenter = new double[2];
	boolean mustautozoom = false;

	// PUBLIC

	public ControlPoint cp = new ControlPoint();

	Color bgcolor;
	Color hcolor; // helpers color

	// public Renderer render;

	public int pivotMode = PIVOT_LOCAL;
	public SPoint localPivot = new SPoint(0, 0);
	public SPoint worldPivot = new SPoint(0, 0);

	int imagewidth = 0, imageheight = 0;

	// preview bounds
	int previewwidth, previewheight;

	// bounds for immediate painting
	Rectangle ibounds = new Rectangle();

	Renderer renderer = null;
	Image pimage = null;
	boolean showpreview = true;

	BasicStroke solid;
	BasicStroke dots;
	BasicStroke solid3;

	Font vfont = new Font("Helvetica", Font.PLAIN, 10);
	Font pfont = new Font("Helvetica", Font.PLAIN, 9);

	// status bar
	Color bg = new Color(0xD1CCC6);
	String status1 = "X:0";
	String status2 = "Y:0";
	String status3 = "Starting editor ...";
	Font sfont = new Font("Courier", Font.PLAIN, 12);
	int swidth;
	int sheight;
	static final int hstatus1 = 5;
	static final int hstatus2 = 85;
	static final int hstatus3 = 165;

	int dragcount = 0;

	// variation preview
	boolean showVarPreview = false;
	int varPreviewDensity = 2, varPreviewRange = 6, varPreviewDepth = 3;

	int[] pixels = null;
	MemoryImageSource source = null;
	Image vimage = null;
	int graphwidth;
	int graphheight;

	double previewdensity = 0;

	/*****************************************************************************/

	Editor(String title, String xmlfile, int width, int height)
			throws Exception {
		super(title, xmlfile, width, height);

		renderer = new Renderer(this);

		initStrokes();

		// buildVariationList();
		// buildParameterList();

		graphZoom = 1;

		editMode = MODE_MOVE;
		// axisLock = Global.transformAxisLock;
		// tbAxisLock down = axisLock

		selectMode = true;
		extendedEdit = Global.extEditEnabled;
		axisLock = Global.transformAxisLock;

		selectedTriangle = 0;

		mouseOverTriangle = -1;
		mouseOverCorner = -1;
		mouseOverEdge = -1;
		mouseOverWidget = -1;

		memTriangle = new Triangle(1, 0, 0, 0, 0, 1);

		reset();

		previewdensity = Global.prevLowQuality;

		switch (Global.editPrevQual) {
		case 0:
			setBoolean(find("LowQuality"), "selected", true);
			previewdensity = Global.prevLowQuality;
			break;

		case 1:
			setBoolean(find("MediumQuality"), "selected", true);
			previewdensity = Global.prevMediumQuality;
			break;

		case 2:
			setBoolean(find("HighQuality"), "selected", true);
			previewdensity = Global.prevHighQuality;
			break;
		}

		setBoolean(find("chkResetLocation"), "selected", Global.resetLocation);

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

		updateDisplay();

		setBoolean(find("tbEnableFinalXform"), "selected",
				Global.enableFinalXform);

		// requestFocus(find("TriangleView"));

		drawPreview();

	}

	/*****************************************************************************/

	void initStrokes() {
		float thickness = 1f;
		float miterLimit = 5f;
		float[] dashPattern = { 5f };
		float dashPhase = 2.5f;

		dots = new BasicStroke(thickness, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, miterLimit, dashPattern, dashPhase);

		solid = new BasicStroke(thickness);

		solid3 = new BasicStroke(3f);

	}

	/*****************************************************************************/

	int lastTriangle() {
		if (Global.enableFinalXform || cp.hasFinalXform) {
			return Global.transforms;
		} else {
			return Global.transforms - 1;
		}
	}

	/*****************************************************************************/

	final int XScreen(double x, double ix, double gCenterX, double sc) {
		return (int) (ix + (x - gCenterX) * sc + 0.5);
	}

	final int YScreen(double y, double iy, double gCenterY, double sc) {
		return (int) (iy - (y - gCenterY) * sc + 0.5);
	}

	/*****************************************************************************/

	public void triangleViewPaint(Graphics g, Rectangle bounds) {
		double ix, iy, sc;
		int width, height;
		int a_x, a_y, b_x, b_y, c_x, c_y;
		int n;
		int ax, ay;
		double gridX1, gridX2, gridY1, gridY2, gi, gstep;
		int gp;
		// double txx,txy,tyx,tyy;

		if (mustautozoom) {
			autoZoom(bounds);
		}

		graphwidth = bounds.width;
		graphheight = bounds.height;

		Color c1 = new Color(Global.gridColor1);
		Color c2 = new Color(Global.gridColor2);
		Color bg = new Color(Global.editorBkgColor);
		Color rf = new Color(Global.referenceTriangleColor);

		if (selectedTriangle < 0) {
			selectedTriangle = 0;
		}
		if (selectedTriangle > lastTriangle()) {
			selectedTriangle = lastTriangle();
		}

		width = bounds.width;
		height = bounds.height;
		ix = width / 2;
		iy = height / 2;
		sc = 50 * graphZoom;

		g.setColor(bg);
		g.fillRect(0, 0, width, height);

		if (lastTriangle() < 0) {
			return;
		}

		if (showVarPreview) {
			if (vimage != null) {
				g.drawImage(vimage, 0, 0, null);
			}
		}

		g.setColor(c2);
		gridX1 = gCenterX - ix / sc;
		gridX2 = gCenterX + (width - ix) / sc;
		gridY1 = gCenterY - iy / sc;
		gridY2 = gCenterY + (height - iy) / sc;

		gp = (int) (log10(Math.max(width, height) / sc) + 0.5) - 1;
		gstep = Math.pow(10.0, gp);

		gi = roundto(gridX1, gp);
		while (gi <= gridX2) {
			ax = XScreen(gi, ix, gCenterX, sc);
			g.drawLine(ax, 0, ax, height);
			gi += gstep;
		}

		gi = roundto(gridY1, gp);
		while (gi <= gridY2) {
			ay = YScreen(gi, iy, gCenterY, sc);
			g.drawLine(0, ay, width, ay);
			gi += gstep;
		}

		g.setColor(c1);
		ax = (int) Math.round(ix - gCenterX * sc);
		ay = (int) Math.round(iy + gCenterY * sc);
		g.drawLine(ax, 0, ax, height - 1);
		g.drawLine(0, ay, width - 1, ay);

		// reference triangle
		g.setColor(rf);
		a_x = XScreen(Global.mainTriangles[M1].x[0], ix, gCenterX, sc);
		a_y = YScreen(Global.mainTriangles[M1].y[0], iy, gCenterY, sc);
		b_x = XScreen(Global.mainTriangles[M1].x[1], ix, gCenterX, sc);
		b_y = YScreen(Global.mainTriangles[M1].y[1], iy, gCenterY, sc);
		c_x = XScreen(Global.mainTriangles[M1].x[2], ix, gCenterX, sc);
		c_y = YScreen(Global.mainTriangles[M1].y[2], iy, gCenterY, sc);
		g.drawLine(a_x, a_y, b_x, b_y);
		g.drawLine(b_x, b_y, c_x, c_y);
		g.drawLine(c_x, c_y, a_x, a_y);

		g.drawString("Y", c_x - 9, c_y - 12);
		g.drawString("X", a_x + 2, a_y + 1);
		g.drawString("O", b_x - 8, b_y + 1);

		if (showhelpers) {
			drawHelpers(g, ix, iy);
		}

		Graphics2D g2 = (Graphics2D) g;

		g2.setStroke(solid);

		// draw triangles
		for (int i = 0; i <= lastTriangle(); i++) {
			int kol = getTriangleColor(i);
			if (cp.xform[i].enabled) {
				kol += 0xFF000000;
			} else {
				kol += 0x80000000;
			}

			Color color = new Color(kol, true);

			BasicStroke stroke = solid;
			if (i != selectedTriangle) {
				stroke = dots;
			}
			g2.setStroke(stroke);

			g2.setColor(color);

			a_x = XScreen(Global.mainTriangles[i].x[0], ix, gCenterX, sc);
			a_y = YScreen(Global.mainTriangles[i].y[0], iy, gCenterY, sc);

			b_x = XScreen(Global.mainTriangles[i].x[1], ix, gCenterX, sc);
			b_y = YScreen(Global.mainTriangles[i].y[1], iy, gCenterY, sc);

			c_x = XScreen(Global.mainTriangles[i].x[2], ix, gCenterX, sc);
			c_y = YScreen(Global.mainTriangles[i].y[2], iy, gCenterY, sc);

			if (stroke != solid) {
				g2.drawLine(a_x, a_y, b_x, b_y);
				g2.drawLine(b_x, b_y, c_x, c_y);
				g2.drawLine(c_x, c_y, a_x, a_y);
			} else {
				g2.drawLine(a_x, a_y, b_x, b_y);
				g2.drawLine(b_x, b_y, c_x, c_y);
				g2.setStroke(dots);
				g2.drawLine(c_x, c_y, a_x, a_y);
			}

			g2.setStroke(solid);

			if ((mouseOverTriangle == i) && (mouseOverCorner == 0)) {
				g2.setColor(Color.white);
			} else {
				g2.setColor(color);
			}
			g2.drawOval(a_x - 4, a_y - 4, 8, 8);

			if ((mouseOverTriangle == i) && (mouseOverCorner == 1)) {
				g2.setColor(Color.white);
			} else {
				g2.setColor(color);
			}
			g2.fillOval(b_x - 4, b_y - 4, 8, 8);

			if ((mouseOverTriangle == i) && (mouseOverCorner == 2)) {
				g2.setColor(Color.white);
			} else {
				g2.setColor(color);
			}
			g2.drawOval(c_x - 4, c_y - 4, 8, 8);

			g2.setColor(color);

			if ((i == mouseOverTriangle) && (mouseOverEdge >= 0)) {
				g2.setStroke(solid3);

				if (mouseOverEdge == 0) {
					g2.drawLine(b_x, b_y, c_x, c_y);
				} else if (mouseOverEdge == 1) {
					g2.drawLine(c_x, c_y, a_x, a_y);
				} else if (mouseOverEdge == 2) {
					g2.drawLine(a_x, a_y, b_x, b_y);
				}

				g2.setStroke(solid);
			}

			if ((i == selectedTriangle) && extendedEdit) {
				if (mouseOverWidget >= 0) {
					g2.setStroke(solid3);
				} else {
					g2.setStroke(solid);
				}
				updateWidgets();
				drawWidgets(g2, ix, iy);
			}

		} // for i

		getPivot();
		a_x = XScreen(pivot.x, ix, gCenterX, sc);
		a_y = YScreen(pivot.y, iy, gCenterY, sc);
		g2.setStroke(solid);
		g.setColor(Color.white);

		if (pivotMode == PIVOT_LOCAL) {
			n = 2;
		} else {
			n = 3;
		}
		g.drawOval(a_x - n, a_y - n, 2 * n, 2 * n);

		if (mouseOverTriangle >= 0) {
			Triangle t = Global.mainTriangles[mouseOverTriangle];
			int rgba = getTriangleColor(mouseOverTriangle) + 0x50000000;
			g2.setColor(new Color(rgba, true));
			polyx[0] = XScreen(t.x[0], ix, gCenterX, sc);
			polyy[0] = YScreen(t.y[0], iy, gCenterY, sc);

			polyx[1] = XScreen(t.x[1], ix, gCenterX, sc);
			polyy[1] = YScreen(t.y[1], iy, gCenterY, sc);

			polyx[2] = XScreen(t.x[2], ix, gCenterX, sc);
			polyy[2] = YScreen(t.y[2], iy, gCenterY, sc);

			if (selectMode || (mouseOverTriangle == selectedTriangle)) {
				g2.fillPolygon(polyx, polyy, 3);
			} else {
				g2.drawPolygon(polyx, polyy, 3);
			}

			// display the variations used
			int yv = bounds.height - 5;

			Font tfont = g2.getFont();
			g2.setFont(vfont);
			FontMetrics fm = g2.getFontMetrics();

			XForm xform = cp.xform[mouseOverTriangle];
			g2.setColor(new Color(getTriangleColor(mouseOverTriangle)));
			int nv = XForm.getNrVariations();
			for (int i = nv - 1; i >= 0; i--) {
				if (xform.vars[i] != 0) {
					String vname = XForm.getVariation(i).getName();
					int l = fm.stringWidth(vname);
					g2.drawString(vname, bounds.width - l - 5, yv);
					yv -= 12;
				}
			}

			g2.setFont(tfont);
		}

	} // End of method triangleViewPaint

	/*****************************************************************************/

	void drawHelpers(Graphics g, double ix, double iy) {
		int ix1, iy1, ix2, iy2;
		double sc = 50 * graphZoom;
		Triangle t = Global.mainTriangles[selectedTriangle];

		g.setColor(hcolor);

		switch (action) {
		case ACTION_ROTATE:
		case ACTION_ROTATE_EDGE:
		case ACTION_WHIRL:
			ix1 = XScreen(rcenter.x - rradius, ix, gCenterX, sc);
			iy1 = YScreen(rcenter.y + rradius, iy, gCenterY, sc);
			ix2 = XScreen(rcenter.x + rradius, ix, gCenterX, sc);
			iy2 = YScreen(rcenter.y - rradius, iy, gCenterY, sc);
			g.drawOval(ix1, iy1, ix2 - ix1, iy2 - iy1);
			break;

		case ACTION_SLIDE:
		case ACTION_SLIDE_CORNER:
			ix1 = XScreen(rcenter.x + Math.cos(aclick) * sc, ix, gCenterX, sc);
			iy1 = YScreen(rcenter.y + Math.sin(aclick) * sc, iy, gCenterY, sc);
			ix2 = XScreen(rcenter.x - Math.cos(aclick) * sc, ix, gCenterX, sc);
			iy2 = YScreen(rcenter.y - Math.sin(aclick) * sc, iy, gCenterY, sc);
			g.drawLine(ix1, iy1, ix2, iy2);
			break;

		case ACTION_SCALE:
			for (int i = 0; i < 3; i++) {
				ix1 = XScreen(rcenter.x + (t.x[i] - rcenter.x) * sc, ix,
						gCenterX, sc);
				iy1 = YScreen(rcenter.y + (t.y[i] - rcenter.y) * sc, iy,
						gCenterY, sc);
				ix2 = XScreen(rcenter.x - (t.x[i] - rcenter.x) * sc, ix,
						gCenterX, sc);
				iy2 = YScreen(rcenter.y - (t.y[i] - rcenter.y) * sc, iy,
						gCenterY, sc);
				g.drawLine(ix1, iy1, ix2, iy2);
			}
			break;

		}

	} // End of method drawHelpers

	/*****************************************************************************/

	void drawWidgets(Graphics2D g, double ix, double iy) {
		double sc = 50 * graphZoom;

		for (int i = 0; i < 4; i++) {
			int ax = XScreen(widgets[i][0][0], ix, gCenterX, sc);
			int ay = YScreen(widgets[i][0][1], iy, gCenterY, sc);

			int bx = XScreen(widgets[i][1][0], ix, gCenterX, sc);
			int by = YScreen(widgets[i][1][1], iy, gCenterY, sc);

			int cx = XScreen(widgets[i][2][0], ix, gCenterX, sc);
			int cy = YScreen(widgets[i][2][1], iy, gCenterY, sc);

			g.drawLine(ax, ay, bx, by);
			g.drawLine(bx, by, cx, cy);
		}

	} // End of method drawWidgets

	/*****************************************************************************/

	void buildVarPreview() {
		if (vimage != null) {
			int wi = vimage.getWidth(null);
			int hi = vimage.getHeight(null);
			if ((wi != graphwidth) || (hi != graphheight)) {
				vimage = null;
				pixels = null;
			}
		}

		if (vimage == null) {
			pixels = new int[graphwidth * graphheight];
			source = new MemoryImageSource(graphwidth, graphheight, pixels, 0,
					graphwidth);
			source.setAnimated(true);
			vimage = createImage(source);
		}

		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = 0; // transparent pixels
		}

		double ix = graphwidth / 2;
		double iy = graphheight / 2;
		double sc = graphZoom * 50;

		XForm form = cp.xform[selectedTriangle];

		form.prepare();

		int n = varPreviewRange * varPreviewDensity * 5;
		double d1 = varPreviewDensity * 5;

		int rgb = getTriangleColor(selectedTriangle) + 0xFF000000;

		double[] txy = new double[2];

		for (int ax = -n; ax <= n; ax++) {
			for (int ay = -n; ay <= n; ay++) {
				txy[0] = ax / d1;
				txy[1] = ay / d1;
				for (int i = varPreviewDepth; i >= 1; i--) {
					form.nextPointXY(txy);
				}

				int a_x = XScreen(txy[0], ix, gCenterX, sc);
				int a_y = YScreen(-txy[1], iy, gCenterY, sc);
				if ((a_x >= 0) && (a_x < graphwidth) && (a_y >= 0)
						&& (a_y < graphheight)) {
					pixels[a_y * graphwidth + a_x] = rgb;
				}
			}
		}

		source.newPixels();

	} // End of method buildVarPreview

	/*****************************************************************************/

	public void updateDisplay() {
		updateDisplay(false);
	}

	public void updateDisplay(boolean previewonly) {

		cp.copy(Global.mainCP);
		bgcolor = new Color(cp.background[0], cp.background[1],
				cp.background[2]);
		hcolor = new Color(Global.helpersColor);

		if (previewheight != 0) {
			if ((cp.width * 1.0 / cp.height) > (previewwidth * 1.0 / previewheight)) {
				imagewidth = previewwidth;
				double r = cp.width * 1.0 / imagewidth;
				imageheight = (int) (cp.height / r + 0.5);
			} else {
				imageheight = previewheight;
				double r = cp.height * 1.0 / imageheight;
				imagewidth = (int) (cp.width / r + 0.5);
			}

			cp.adjustScale(imagewidth, imageheight);
		}

		if (selectedTriangle > lastTriangle()) {
			selectedTriangle = lastTriangle();
			// selectedTriangle = 0;
		}

		if (selectedTriangle > lastTriangle()) {
			mouseOverTriangle = -1;
		}

		Global.enableFinalXform = cp.finalXformEnabled;

		updatePreview();

		if (previewonly) {
			return;
		}

		updateXformsList();

		/*
		 * for(int i=0;i<cp.nxforms+1;i++) cp.xform[i].print("form"+i+" = ");
		 */

		setBoolean(find("tbEnableFinalXform"), "selected",
				Global.enableFinalXform);

		setBoolean(find("tbSelect"), "selected", selectMode);
		setBoolean(find("mnuSelect"), "selected", selectMode);

		setBoolean(find("tbVarPreview"), "selected", showVarPreview);
		setBoolean(find("mnuVarPreview"), "selected", showVarPreview);

		setBoolean(find("tbMove"), "selected", editMode == MODE_MOVE);
		setBoolean(find("tbRotate"), "selected", editMode == MODE_ROTATE);
		setBoolean(find("tbScale"), "selected", editMode == MODE_SCALE);

		setBoolean(find("btExtendedEdit"), "selected", extendedEdit);
		setBoolean(find("mnuExtendedEdit"), "selected", extendedEdit);

		setBoolean(find("btAxisLock"), "selected", axisLock);
		setBoolean(find("mnuAxisLock"), "selected", axisLock);

		cp.trianglesFromCP(Global.mainTriangles);

		showSelectedInfo();

		mustautozoom = (Global.main.undoindex == 0);

		repaint();

		requestFocus(find("dummymenu"));

	} // End of method updateDisplay

	/*****************************************************************************/

	void updateXformsList() {
		Object combo = find("cbTransforms");
		removeAll(combo);

		for (int i = 1; i <= Global.transforms; i++) {
			Object choice = createImpl("choice");
			setString(choice, "text", "" + i);
			setColor(choice, "foreground", Color.white);
			setColor(choice, "background", Color.black);
			setIcon(choice, "icon", buildTriangleIcon(getTriangleColor(i - 1)));
			add(combo, choice);
		}

		setInteger(combo, "selected", selectedTriangle);

		if (cp.hasFinalXform || Global.enableFinalXform) {
			Object choice = createImpl("choice");
			setString(choice, "text", "Final");
			setColor(choice, "foreground", Color.white);
			setColor(choice, "background", Color.black);
			add(combo, choice);
		}

	} // End of method updateXformsList

	/*****************************************************************************/

	void adjustPreview() {
		if (previewheight != 0) {
			if ((cp.width * 1.0 / cp.height) > (previewwidth * 1.0 / previewheight)) {
				imagewidth = previewwidth;
				double r = cp.width * 1.0 / imagewidth;
				imageheight = (int) (cp.height / r + 0.5);
			} else {
				imageheight = previewheight;
				double r = cp.height * 1.0 / imageheight;
				imagewidth = (int) (cp.width / r + 0.5);
			}

			cp.adjustScale(imagewidth, imageheight);
		}

	} // End of method adjustPreview

	/*****************************************************************************/

	void updatePreview() {
		adjustPreview();
		drawPreview(false);
	}

	/*****************************************************************************/

	void drawPreview() {
		drawPreview(false);
	}

	void drawPreview(boolean immediate) {
		if (imagewidth == 0) {
			return;
		}

		cp.sample_density = previewdensity;
		cp.spatial_oversample = Global.defOversample;
		cp.spatial_filter_radius = Global.defFilterRadius;

		if (Global.resetLocation) {
			cp.zoom = 0;
			cp.calcBoundBox();
		}

		System.arraycopy(Global.mainCP.cmap, 0, cmap, 0, cmap.length);

		if (showpreview) {
			renderer.setCP(cp);
			renderer.render();
			pimage = renderer.getImage();
		}

		if (immediate) {
			paintPreviewNow(find("PrevPnl"));
		} else {
			repaint(find("PrevPnl"));
		}

	} // End of method drawPreview

	/*****************************************************************************/

	void paintPreviewNow(Object component) {
		Graphics g = getGraphics();
		Rectangle r = getRectangle(component, "bounds");

		ibounds.x = 0;
		ibounds.y = 0;
		ibounds.width = r.width;
		ibounds.height = r.height;

		setToAbsolutePosition(component, ibounds);
		g.translate(ibounds.x, ibounds.y);
		drawPreviewPanel(g, ibounds);
		g.translate(-ibounds.x, -ibounds.y);

	}

	/*****************************************************************************/

	public void drawPreviewPanel(Graphics g, Rectangle bounds) {
		if (previewwidth != bounds.width) {
			previewwidth = bounds.width;
			previewheight = bounds.height;
			adjustPreview();
			drawPreview();
		}

		if (showpreview) {
			g.setColor(bgcolor);
			g.fillRect(0, 0, bounds.width, bounds.height);
			if (pimage != null) {
				int wi = pimage.getWidth(null);
				int hi = pimage.getHeight(null);
				int x = bounds.width / 2 - wi / 2;
				int y = bounds.height / 2 - hi / 2;
				g.drawImage(pimage, x, y, this);
			}
		} else {
			g.setColor(Color.black);
			g.fillRect(0, 0, bounds.width, bounds.height);
			g.setFont(pfont);
			g.setColor(Color.white);
			FontMetrics fm = g.getFontMetrics();
			String text = "Click to enable preview";
			int l = fm.stringWidth(text);
			g.drawString(text, bounds.width / 2 - l / 2, bounds.height / 2);
		}

	} // End of method drawPreviewPanel

	/*****************************************************************************/

	public void pressPreviewPanel(MouseEvent e, Object canvas, Rectangle bounds) {
		if (e.isPopupTrigger()) {
			Rectangle r = new Rectangle(bounds);
			setToAbsolutePosition(canvas, r);
			Object popup = find("PreviewPopup");
			popupPopup(popup, r.x + e.getX(), r.y + e.getY());
		} else {
			showpreview = !showpreview;
			repaint(find("PrevPnl"));
			if (showpreview) {
				updateFlame(true);
			}
		}

	} // End of method pressPreviewPanel

	/*****************************************************************************/

	public void mnuLowQualityClick(Object item) {
		previewdensity = Global.prevLowQuality;
		Global.editPrevQual = 0;
		drawPreview();
	} // End of method mnuQualityClick

	public void mnuMediumQualityClick(Object item) {
		previewdensity = Global.prevMediumQuality;
		Global.editPrevQual = 1;
		drawPreview();
	} // End of method mnuQualityClick

	public void mnuHighQualityClick(Object item) {
		previewdensity = Global.prevHighQuality;
		Global.editPrevQual = 2;
		drawPreview();
	} // End of method mnuQualityClick

	/*****************************************************************************/

	void reset() {

		Global.main.updateUndo();

		for (int i = 0; i <= Global.transforms; i++) {
			cp.xform[i].clear();
		}

		cp.xform[0].density = 0.5;
		cp.xform[1].symmetry = 1;

		cp.center[0] = 0;
		cp.center[1] = 0;
		cp.zoom = 0;
		cp.pixels_per_unit = 100 / 4;
		cp.fangle = 0;

		Global.transforms = 1;
		selectedTriangle = 0;

		Global.mainTriangles[0].copy(Global.mainTriangles[M1]);
		Global.mainTriangles[1].copy(Global.mainTriangles[M1]);

		// Global.enableFinalXform = false;

		updateXformsList();

		mustautozoom = true;

		repaint();

	} // End of method reset

	/*****************************************************************************/

	public void mnuResetAllClick() {

		reset();

		Global.main.clearUndo();

		updateFlame(true);

	} // End of method mnuResetAllClick

	/*****************************************************************************/

	void autoZoom(Rectangle bounds) {
		double xminz, yminz, xmaxz, ymaxz;
		double gxlength, gylength;

		mustautozoom = false;

		xminz = 0;
		xmaxz = 0;
		yminz = 0;
		ymaxz = 0;

		for (int j = 0; j < 3; j++) {
			xminz = Math.min(xminz, Global.mainTriangles[M1].x[j]);
			xmaxz = Math.max(xmaxz, Global.mainTriangles[M1].x[j]);
			yminz = Math.min(yminz, Global.mainTriangles[M1].y[j]);
			ymaxz = Math.max(ymaxz, Global.mainTriangles[M1].y[j]);
		}

		for (int i = 0; i <= lastTriangle(); i++) {
			for (int j = 0; j < 3; j++) {
				xminz = Math.min(xminz, Global.mainTriangles[i].x[j]);
				xmaxz = Math.max(xmaxz, Global.mainTriangles[i].x[j]);
				yminz = Math.min(yminz, Global.mainTriangles[i].y[j]);
				ymaxz = Math.max(ymaxz, Global.mainTriangles[i].y[j]);
			}
		}

		gxlength = xmaxz - xminz;
		gylength = ymaxz - yminz;
		gCenterX = xminz + gxlength / 2;
		gCenterY = yminz + gylength / 2;

		if (gxlength >= gylength) {
			graphZoom = bounds.width / 60.0 / gxlength;
		} else {
			graphZoom = bounds.height / 60.0 / gylength;
		}

		displayZoom();

	} // End of method autoZoom

	/*****************************************************************************/

	void displayZoom() {
		double gz = ((int) (graphZoom * 1000)) / 1000.0;
		status3 = "Zoom : " + gz;
		repaint();
	}

	/*****************************************************************************/

	void updateFlame(boolean drawmain) {
		displayZoom();

		cp.getFromTriangles(Global.mainTriangles, lastTriangle());

		drawPreview(false);
		showSelectedInfo();

		if (showpreview) {
			if (drawmain) {
				Global.main.stopThread();

				Global.mainCP.copy(cp, true);
				CMap.copyPalette(cmap, Global.mainCP.cmap);

				if (Global.resetLocation) {
					Global.mainCP.zoom = 0;
					Global.main.center[0] = cp.center[0];
					Global.main.center[1] = cp.center[1];
				}

				if (Global.adjust.visible()) {
					Global.adjust.updateDisplay();
				}
				if (Global.mutate.visible()) {
					Global.mutate.updateDisplay();
				}
				Global.main.timer.enable();
			}
		}

		repaint(find("TriangleView"));

	} // End of method updateFlame

	/*****************************************************************************/

	public void mnuAddClick() {
		int nt = Global.transforms;

		if (nt < NXFORMS) {
			Global.main.updateUndo();

			// move final triangle
			Global.mainTriangles[nt + 1].copy(Global.mainTriangles[nt]);
			cp.xform[nt + 1].copy(cp.xform[nt]);

			Global.mainTriangles[nt].copy(Global.mainTriangles[M1]);
			selectedTriangle = nt;
			cp.xform[nt].clear();
			cp.xform[nt].density = 0.5;

			cp.xform[nt].vars[0] = 1.0;
			int nv = XForm.getNrVariations();
			for (int i = 1; i < nv; i++) {
				cp.xform[nt].vars[i] = 0.0;
			}

			Global.mainCP.nxforms++;
			Global.transforms++;

			updateXformsList();
			updateFlame(true);
		}

	} // End of method mnuAddClick

	/*****************************************************************************/

	public void mnuDeleteClick() {
		int it = selectedTriangle;
		if (it >= 0) {
			deleteTriangle(it);
		}
	}

	/*****************************************************************************/

	void deleteTriangle(int it) {

		if (it == Global.transforms) {
			// delete final triangle
			Global.main.updateUndo();

			Global.enableFinalXform = false;
			cp.finalXformEnabled = false;
			cp.xform[Global.transforms].clear();
			cp.xform[Global.transforms].symmetry = 1;
			Global.mainTriangles[Global.transforms]
					.copy(Global.mainTriangles[M1]);

			// if the final triangle was selected
			if (selectedTriangle == Global.transforms) {
				selectedTriangle--;
			}
		} else {
			// delete normal triangle
			Global.main.updateUndo();

			if (it == Global.transforms - 1) {
				Global.mainTriangles[it]
						.copy(Global.mainTriangles[Global.transforms]);
				cp.xform[it].copy(cp.xform[Global.transforms]);
				selectedTriangle--;
			} else {
				for (int i = it; i <= Global.transforms - 1; i++) {
					Global.mainTriangles[i].copy(Global.mainTriangles[i + 1]);
					cp.xform[i].copy(cp.xform[i + 1]);
				}
			}
			cp.nxforms--;
			Global.transforms--;
		}

		updateXformsList();
		updateFlame(true);

	} // End of method deleteTriangle

	/*****************************************************************************/

	public void mnuDupClick() {
		int nt = Global.transforms;

		if (nt < NXFORMS) {
			Global.main.updateUndo();

			Global.mainTriangles[nt + 1].copy(Global.mainTriangles[nt]);
			cp.xform[nt + 1].copy(cp.xform[nt]);
			int it = selectedTriangle;
			if (it != nt) {
				Global.mainTriangles[nt].copy(Global.mainTriangles[it]);
				cp.xform[nt].copy(cp.xform[it]);
				selectedTriangle = nt;
			} else {
				cp.xform[nt].density = 0.5;
			}

			Global.transforms++;
			updateXformsList();
			updateFlame(true);
		}

	} // End of method mnuDupClick

	/*****************************************************************************/

	public void triangleViewMouseDown(MouseEvent e, Rectangle bounds) {
		double dx = 0, dy = 0;

		if (e.isPopupTrigger()) {
			Object popup;
			if (mouseOverTriangle >= 0) {
				if (mouseOverTriangle != selectedTriangle) {
					selectedTriangle = mouseOverTriangle;
					showSelectedInfo();
				}
				popup = find("TrianglePopup");
			} else {
				popup = find("EditorPopup");
			}
			Object canvas = find("TriangleView");
			Rectangle r = new Rectangle(bounds);
			setToAbsolutePosition(canvas, r);
			popupPopup(popup, r.x + e.getX(), r.y + e.getY());
			return;
		}

		mustupdateflame = false;

		if (e.getClickCount() == 2) {
			if (mouseOverTriangle >= 0) {
				selectedTriangle = mouseOverTriangle;
				Object button = find("chkEnabled");
				setBoolean(button, "selected", !getBoolean(button, "selected"));
				chkEnabledClick(button);
				return;
			} else {
				mustautozoom = true;
				repaint();
				return;
			}
		}

		if (Global.helpersEnabled) {
			showhelpers = true;
		}

		dragcount = 0;

		selectedEdge = -1;
		selectedCorner = -1;
		selectedWidget = -1;

		action = ACTION_NONE;

		scale(fclick, e.getX(), e.getY(), bounds);

		if (editMode == MODE_PICK) {
			selectPivot(fclick[0], fclick[1]);
			showPivotCoordinates();
			repaint();
			return;
		}

		Global.main.updateUndo();

		// check if click in a corner
		int imin = selectMode ? 0 : selectedTriangle;
		int imax = selectMode ? lastTriangle() : selectedTriangle;
		for (int i = imax; i >= imin; i--) {
			int j = getCorner(fclick[0], fclick[1], Global.mainTriangles[i]);
			if (j >= 0) {
				selectedTriangle = i;
				selectedCorner = j;
				break;
			}
		}

		if (selectedCorner >= 0) {
			Triangle t = Global.mainTriangles[selectedTriangle];
			if (editMode == MODE_MOVE) {
				if (axisLock && (selectedCorner == 1)) {
					oldT.copy(t);
					action = ACTION_MOVE;
				} else {
					oldT.copy(t);
					action = ACTION_MOVE_CORNER;
				}
			} else if (editMode == MODE_SCALE) {
				if (selectedCorner != 1) {
					rcenter.copy(getPivot());
					cclick = fclick[0] - pivot.x;
					sclick = fclick[1] - pivot.y;
					aclick = Math.atan2(sclick, cclick);
					oldT.copy(t);
					action = ACTION_SLIDE_CORNER;
				} else {
					if (pivotMode == PIVOT_LOCAL) {
						pivot.x = pivot.y = 0;
					} else {
						getPivot();
					}
					rcenter.copy(pivot);
					cclick = fclick[0] - rcenter.x;
					sclick = fclick[1] - rcenter.y;
					aclick = Math.atan2(sclick, cclick);
					oldT.copy(t);
					action = ACTION_SLIDE;
				}
			} else if (editMode == MODE_ROTATE) {
				if (selectedCorner != 1) {
					rcenter.copy(getPivot());
					dx = fclick[0] - rcenter.x;
					dy = fclick[1] - rcenter.y;
					rradius = Math.sqrt(dx * dx + dy * dy);
					aclick = Math.atan2(dy, dx);
					oldT.copy(t);
					action = ACTION_ROTATE_EDGE;
				} else if (pivotMode == PIVOT_LOCAL) {
					rcenter.x = rcenter.y = 0;
					dx = fclick[0];
					dy = fclick[1];
					rradius = Math.sqrt(dx * dx + dy * dy);
					aclick = Math.atan2(dy, dx);
					oldT.copy(t);
					action = ACTION_WHIRL;
				} else if (pivotMode == PIVOT_WORLD) {
					rcenter.copy(getPivot());
					dx = fclick[0] - rcenter.x;
					dy = fclick[1] - rcenter.y;
					rradius = Math.sqrt(dx * dx + dy * dy);
					aclick = Math.atan2(dy, dx);
					oldT.copy(t);
					action = ACTION_WHIRL;
				}
			}
			showSelectedInfo();
			if (showhelpers) {
				repaint(find("TriangleView"));
			}
			return;
		}

		// check if click on an edge

		if (selectMode && extendedEdit && (mouseOverCorner < 0)) {
			for (int i = imax; i >= imin; i--) {
				int j = getEdge(fclick[0], fclick[1], Global.mainTriangles[i]);
				if (j >= 0) {
					selectedTriangle = i;
					selectedEdge = j;
					showSelectedInfo();
					break;
				}
			}
		}

		if (selectedEdge == 1) {
			// click on XY, scale

			Triangle t = Global.mainTriangles[selectedTriangle];
			rcenter.copy(getPivot());
			cclick = fclick[0] - rcenter.x;
			sclick = fclick[1] - rcenter.y;
			oldT.copy(t);
			action = ACTION_SCALE;
			showSelectedInfo();
			if (showhelpers) {
				repaint(find("TriangleView"));
			}
			return;
		} else if ((selectedEdge == 0) || (selectedEdge == 2)) {
			// click on OX or OY, rotate

			Triangle t = Global.mainTriangles[selectedTriangle];
			selectedCorner = 2 - selectedEdge;

			int oldmode = pivotMode;
			pivotMode = PIVOT_LOCAL;
			rcenter.copy(getPivot());
			pivotMode = oldmode;

			dx = t.x[selectedCorner] - rcenter.x;
			dy = t.y[selectedCorner] - rcenter.y;
			rradius = Math.sqrt(dx * dx + dy * dy);
			aclick = Math.atan2(dy, dx);

			oldT.copy(Global.mainTriangles[selectedTriangle]);
			action = axisLock ? ACTION_ROTATE : ACTION_ROTATE_EDGE;
			showSelectedInfo();
			if (showhelpers) {
				repaint(find("TriangleView"));
			}
			return;
		}

		// check if click on a widget
		if (extendedEdit) {
			int j = getWidget(fclick[0], fclick[1]);

			if (j >= 0) {
				Triangle t = Global.mainTriangles[selectedTriangle];

				rcenter.copy(getPivot());
				dx = fclick[0] - rcenter.x;
				dy = fclick[1] - rcenter.y;
				aclick = Math.atan2(dy, dx);

				dx = t.x[0] - rcenter.x;
				dy = t.y[0] - rcenter.y;
				rradius = Math.sqrt(dx * dx + dy * dy);

				dx = t.x[1] - rcenter.x;
				dy = t.y[1] - rcenter.y;
				double r = Math.sqrt(dx * dx + dy * dy);
				if (r > rradius) {
					rradius = r;
				}

				dx = t.x[2] - rcenter.x;
				dy = t.y[2] - rcenter.y;
				r = Math.sqrt(dx * dx + dy * dy);
				if (r > rradius) {
					rradius = r;
				}

				oldT.copy(Global.mainTriangles[selectedTriangle]);
				action = ACTION_ROTATE;
				if (showhelpers) {
					repaint(find("TriangleView"));
				}
				return;
			}
		}

		int it = -1;
		if (selectMode) {
			it = insideTriangle(fclick[0], fclick[1]);
			if (it >= 0) {
				selectedTriangle = it;
			}
		} else {
			it = selectedTriangle;
		}

		if (it < 0) {
			action = ACTION_SCROLL;
		} else if (editMode == MODE_MOVE) {
			oldT.copy(Global.mainTriangles[selectedTriangle]);
			action = ACTION_MOVE;
		} else if (editMode == MODE_SCALE) {
			Triangle t = Global.mainTriangles[selectedTriangle];
			rcenter.copy(getPivot());
			cclick = fclick[0] - rcenter.x;
			sclick = fclick[1] - rcenter.y;
			dclick = Math.sqrt(cclick * cclick + sclick * sclick);
			oldT.copy(t);
			action = ACTION_SCALE;
		} else if (editMode == MODE_ROTATE) {
			Triangle t = Global.mainTriangles[selectedTriangle];
			rcenter.copy(getPivot());
			dx = fclick[0] - rcenter.x;
			dy = fclick[1] - rcenter.y;
			aclick = Math.atan2(dy, dx);

			dx = t.x[0] - rcenter.x;
			dy = t.y[0] - rcenter.y;
			rradius = Math.sqrt(dx * dx + dy * dy);

			dx = t.x[1] - rcenter.x;
			dy = t.y[1] - rcenter.y;
			double r = Math.sqrt(dx * dx + dy * dy);
			if (r > rradius) {
				rradius = r;
			}

			dx = t.x[2] - rcenter.x;
			dy = t.y[2] - rcenter.y;
			r = Math.sqrt(dx * dx + dy * dy);
			if (r > rradius) {
				rradius = r;
			}

			oldT.copy(Global.mainTriangles[selectedTriangle]);
			action = ACTION_ROTATE;
		}

		showSelectedInfo();

		if (showhelpers) {
			repaint(find("TriangleView"));
		}

		// requestFocus(find("TriangleView"));

	} // End of method triangleViewMouseDown

	/*****************************************************************************/

	void selectPivot(double fx, double fy) {
		if (pivotMode == PIVOT_LOCAL) {
			// pivot regarding the selected triangle
			Triangle t = Global.mainTriangles[selectedTriangle];
			double xx = t.x[0] - t.x[1];
			double xy = t.y[0] - t.y[1];
			double yx = t.x[2] - t.x[1];
			double yy = t.y[2] - t.y[1];
			double d = (xx * yy - yx * xy);
			if (d != 0) {
				localPivot.x = ((fx - t.x[1]) * yy - (fy - t.y[1]) * yx) / d;
				localPivot.y = (-(fx - t.x[1]) * xy + (fy - t.y[1]) * xx) / d;
			}
		} else {
			// world pivotS
			worldPivot.x = fx;
			worldPivot.y = fy;
		}

	} // End of method selectPivot

	/*****************************************************************************/

	public void triangleViewMouseMove(MouseEvent e, Rectangle bounds) {

		scale(fmove, e.getX(), e.getY(), bounds);

		showCoordinates(fmove[0], fmove[1]);

		int mt = mouseOverTriangle;

		mouseOverTriangle = -1;
		mouseOverEdge = -1;
		mouseOverCorner = -1;
		mouseOverWidget = -1;

		// check if over a corner
		int imin = selectMode ? 0 : selectedTriangle;
		int imax = selectMode ? lastTriangle() : selectedTriangle;
		for (int i = imax; i >= imin; i--) {
			int j = getCorner(fmove[0], fmove[1], Global.mainTriangles[i]);
			if (j >= 0) {
				mouseOverTriangle = i;
				mouseOverCorner = j;
				break;
			}
		}

		if (extendedEdit && (mouseOverCorner < 0)) {
			// check if over an edge
			for (int i = imax; i >= imin; i--) {
				int j = getEdge(fmove[0], fmove[1], Global.mainTriangles[i]);
				if (j >= 0) {
					mouseOverTriangle = i;
					mouseOverEdge = j;
					break;
				}
			}
		}

		if (extendedEdit && (mouseOverTriangle < 0)) {
			int j = getWidget(fmove[0], fmove[1]);
			if (j >= 0) {
				mouseOverTriangle = selectedTriangle;
				mouseOverWidget = j;
			}
		}

		if (mouseOverTriangle < 0) {
			mouseOverTriangle = insideTriangle(fmove[0], fmove[1]);
		}

		if (mouseOverTriangle != mt) {
			if (mouseOverTriangle >= 0) {
				status3 = "Transform #" + (mouseOverTriangle + 1);
			} else {
				status3 = "";
			}
			repaint(find("TriangleView"));
		}

		repaint(find("StatusBar"));

	} // End of method triangleViewMouseMove

	/*****************************************************************************/

	int xdrag = 0, ydrag = 0;

	public void triangleViewMouseDrag(MouseEvent e, Rectangle bounds) {
		double da, dg, dx, dy, det, percent;

		changed = false;

		if ((e.getX() == xdrag) && (e.getY() == ydrag)) {
			return;
		}
		if (action == ACTION_NONE) {
			return;
		}

		dragcount++;

		if (!selectMode) {
			mouseOverTriangle = selectedTriangle;
		}

		xdrag = e.getX();
		ydrag = e.getY();

		scale(fdrag, xdrag, ydrag, bounds);

		showCoordinates(fdrag[0], fdrag[1]);

		Triangle t = Global.mainTriangles[selectedTriangle];

		switch (action) {
		case ACTION_SCROLL:
			gCenterX -= fdrag[0] - fclick[0];
			gCenterY -= fdrag[1] - fclick[1];
			fdrag[0] = fclick[0];
			fdrag[1] = fclick[1];
			if (showVarPreview) {
				buildVarPreview();
			}
			break;

		case ACTION_MOVE:
			dx = fdrag[0] - fclick[0];
			dy = fdrag[1] - fclick[1];
			t.copy(oldT);
			t.move(dx, dy);
			changed = true;
			break;

		case ACTION_SCALE:
			det = cclick * cclick + sclick * sclick;
			if (det == 0) {
				det = 1e-20;
			}
			dy = (cclick * sclick * (fdrag[0] - rcenter.x) + sclick * sclick
					* fdrag[1] + cclick * cclick * rcenter.y)
					/ det;
			dy = dy - rcenter.y;
			dx = (cclick * sclick * (fdrag[1] - rcenter.y) + cclick * cclick
					* fdrag[0] + sclick * sclick * rcenter.x)
					/ det;
			dx = dx - rcenter.x;
			t.copy(oldT);
			if (cclick != 0) {
				t.scaleAroundPoint(rcenter.x, rcenter.y, dx / cclick);
				percent = dx * 100 / cclick;
				percent = (int) (percent * 100) / 100.0;
				status3 = "Scale: " + percent + " %";
			} else if (sclick != 0) {
				t.scaleAroundPoint(rcenter.x, rcenter.y, dy / sclick);
				percent = dy * 100 / sclick;
				percent = (int) (percent * 100) / 100.0;
				status3 = "Scale: " + percent + " %";
			}

			changed = true;
			break;

		case ACTION_ROTATE:
			dx = fdrag[0] - rcenter.x;
			dy = fdrag[1] - rcenter.y;
			adrag = Math.atan2(dy, dx);
			da = adrag - aclick;

			t.copy(oldT);
			t.rotateAroundPoint(rcenter.x, rcenter.y, da);

			dg = da * 180 / Math.PI;
			dg = (int) (dg * 100) / 100.0;
			status3 = "Rotate: " + dg + "\u00B0";
			changed = true;
			break;

		case ACTION_SCALE_EDGE:
			ddrag = distanceToLine(fdrag[0], fdrag[1], p1x, p1y, p2x, p2y);

			t.copy(oldT);
			t.scaleCornerAroundPoint(selectedCorner, pivot.x, pivot.y, ddrag
					/ dclick);

			percent = ddrag * 100 / dclick;

			ddrag = (int) (ddrag * 1000) / 1000.0;
			percent = (int) (percent * 100) / 100.0;
			status3 = "Distance: " + ddrag + "  Scale: " + percent + " %";
			changed = true;
			break;

		case ACTION_ROTATE_EDGE:
			dx = fdrag[0] - rcenter.x;
			dy = fdrag[1] - rcenter.y;
			ddrag = Math.sqrt(dx * dx + dy * dy);
			adrag = Math.atan2(dy, dx);
			da = adrag - aclick;

			t.copy(oldT);
			t.x[selectedCorner] = rcenter.x + (fdrag[0] - rcenter.x) * rradius
					/ ddrag;
			t.y[selectedCorner] = rcenter.y + (fdrag[1] - rcenter.y) * rradius
					/ ddrag;

			dg = da * 180 / Math.PI;
			dg = (int) (dg * 100) / 100.0;
			status3 = "Rotate: " + dg + "\u00B0";
			changed = true;
			break;

		case ACTION_MOVE_CORNER:
			t.x[selectedCorner] = fdrag[0];
			t.y[selectedCorner] = fdrag[1];
			changed = true;
			break;

		case ACTION_WHIRL:
			dx = fdrag[0] - rcenter.x;
			dy = fdrag[1] - rcenter.y;
			ddrag = Math.sqrt(dx * dx + dy * dy);
			adrag = Math.atan2(dy, dx);
			dx = rcenter.x + dx * rradius / ddrag;
			dy = rcenter.y + dy * rradius / ddrag;
			t.copy(oldT);
			t.move(dx - fclick[0], dy - fclick[1]);
			changed = true;
			break;

		case ACTION_SLIDE:
			det = cclick * cclick + sclick * sclick;
			if (det == 0) {
				det = 1e-20;
			}
			if (pivotMode == PIVOT_WORLD) {
				getPivot();
			} else {
				pivot.x = pivot.y = 0;
			}
			dy = (cclick * sclick * (fdrag[0] - pivot.x) + sclick * sclick
					* fdrag[1] + cclick * cclick * pivot.y)
					/ det;
			dx = (cclick * sclick * (fdrag[1] - pivot.y) + cclick * cclick
					* fdrag[0] + sclick * sclick * pivot.x)
					/ det;
			t.copy(oldT);
			t.move(dx - t.x[selectedCorner], dy - t.y[selectedCorner]);
			changed = true;
			break;

		case ACTION_SLIDE_CORNER:
			det = cclick * cclick + sclick * sclick;
			if (det == 0) {
				det = 1e-20;
			}
			getPivot();
			dy = (cclick * sclick * (fdrag[0] - pivot.x) + sclick * sclick
					* fdrag[1] + cclick * cclick * pivot.y)
					/ det;
			dx = (cclick * sclick * (fdrag[1] - pivot.y) + cclick * cclick
					* fdrag[0] + sclick * sclick * pivot.x)
					/ det;
			t.x[selectedCorner] = dx;
			t.y[selectedCorner] = dy;
			changed = true;
			break;
		}

		if (changed && showVarPreview) {
			buildVarPreview();
		}

		repaint(find("TriangleView"));

		if (changed) {
			cp.getFromTriangles(Global.mainTriangles, lastTriangle());
			showTriangleCoordinates();
			showTransformCoordinates();
			if ((dragcount & 0x3) == 0) {
				drawPreview(true);
			}
		}

		repaint(find("StatusBar"));

		mustupdateflame |= changed;

	} // End of method triangleViewMouseDrag

	/*****************************************************************************/

	public void triangleViewMouseUp(MouseEvent e, Rectangle bounds) {
		showhelpers = false;

		if (editMode == MODE_PICK) {
			editMode = oldMode;
		}

		if ((selectedTriangle == cp.nxforms) && mustupdateflame) {
			cp.hasFinalXform = true;
		}

		repaint(find("TriangleView"));

		if (mustupdateflame) {
			updateFlame(true);
		}

	}

	/*****************************************************************************/

	void scale(double fxy[], int x, int y, Rectangle bounds) {
		double sc = 50 * graphZoom;
		fxy[0] = (x - (bounds.width / 2)) / sc + gCenterX;
		fxy[1] = -((y - (bounds.height / 2)) / sc - gCenterY);
	}

	/*
	 * void unscale(int xy[], double fx, double fy, Rectangle bounds) { double
	 * sc = 50*graphZoom; xy[0] = bounds.width/2 + (fx-gCenterX)*sc ; xy[1] =
	 * bounds.height/2 - (fy+gCenterY)*sc ; }
	 */

	/*****************************************************************************/

	void buildVariationList() {
		Object list = find("varlist");

		int nv = XForm.getNrVariations();
		for (int i = 0; i < nv; i++) {
			/*
			 * Object label = createImpl("label");
			 * setString(label,"text",XForm.getVariation(i).getName()+" :  ");
			 * setInteger(label,"weightx",1);
			 * setChoice(label,"alignment","right");
			 */

			Object field = createImpl("textfield");
			setString(field, "name", "variation" + i);
			setInteger(field, "weightx", 1);
			setMethod(field, "perform", "updateVariations()", getDesktop(),
					this);

			Object button = createImpl("button");
			setString(button, "text", XForm.getVariation(i).getName());
			putProperty(button, "field", field);
			setMethod(button, "action", "toggleVariation(this)", getDesktop(),
					this);
			/*
			 * add(list,label);
			 */
			add(list, button);
			add(list, field);
		}

	} // End of method buildVariationList

	/*****************************************************************************/

	public void toggleVariation(Object button) {
		Object field = getProperty(button, "field");
		String s = getString(field, "text");
		if (s.length() == 0) {
			setString(field, "text", "1");
		} else {
			double value = 0.0;
			try {
				value = Double.parseDouble(s);
			} catch (Exception ex) {
			}
			if (value == 0.0) {
				setString(field, "text", "1");
			} else {
				setString(field, "text", "0");
			}
		}

		updateVariations();
	}

	/*****************************************************************************/

	public void updateVariations() {
		double value;

		if (selectedTriangle < 0) {
			return;
		}
		if (selectedTriangle > lastTriangle()) {
			return;
		}

		Global.main.updateUndo();

		int nv = XForm.getNrVariations();
		for (int i = 0; i < nv; i++) {
			try {
				String s = getString(find("variation" + i), "text");
				if (s.length() == 0) {
					value = 0.0;
				} else {
					value = Double.parseDouble(s);
				}
				cp.xform[selectedTriangle].vars[i] = value;
			} catch (Exception ex) {
			}
		}

		if (selectedTriangle == cp.nxforms) {
			cp.hasFinalXform = cp.xform[selectedTriangle].isNotNull();
		}

		updateParameterList();

		updateFlame(true);

	} // End of method updateVariation

	/*****************************************************************************/

	void buildParameterList() {
		Object list = find("paramlist");

		int kp = 0;

		int nv = XForm.getNrVariations();
		for (int i = 0; i < nv; i++) {
			Variation variation = XForm.getVariation(i);
			int np = variation.getNrParameters();
			for (int j = 0; j < np; j++) {
				Object label = createImpl("label");
				setString(label, "text", variation.getParameterName(j) + " :  ");
				setString(label, "name", "lblParameter" + kp);
				setInteger(label, "weightx", 1);
				setChoice(label, "alignment", "right");

				Object field = createImpl("textfield");
				setString(field, "name", "txtParameter" + kp);
				setInteger(field, "weightx", 1);

				setMethod(field, "perform", "updateParameters()", getDesktop(),
						this);

				add(list, label);
				add(list, field);

				kp++;
			}
		}

	} // End of method buildParameterList

	/*****************************************************************************/

	void updateParameterList() {
		int kp = 0;

		int nv = XForm.getNrVariations();
		for (int i = 0; i < nv; i++) {
			Variation variation = XForm.getVariation(i);
			int np = variation.getNrParameters();
			for (int j = 0; j < np; j++) {
				boolean ok = cp.xform[selectedTriangle].vars[i] != 0;
				setColor(find("lblParameter" + kp), "foreground",
						ok ? Color.black : Color.gray);
				setBoolean(find("txtParameter" + kp), "enabled", ok);
				kp++;
			}
		}

	} // End of method updateParameterList

	/*****************************************************************************/

	public void updateParameters() {
		if (selectedTriangle < 0) {
			return;
		}
		if (selectedTriangle > lastTriangle()) {
			return;
		}

		Global.main.updateUndo();

		int np = XForm.getNrParameters();
		for (int i = 0; i < np; i++) {
			try {
				String s = getString(find("txtParameter" + i), "text");
				double value = Double.parseDouble(s);
				cp.xform[selectedTriangle].pvalues[i] = value;
			} catch (Exception ex) {
			}
		}

		updateFlame(true);

	} // End of method updateParameter

	/*****************************************************************************/

	void rotateTriangle(int it, double radians) {
		getPivot(it);

		Global.main.updateUndo();

		Global.mainTriangles[it].rotateAroundPoint(pivot.x, pivot.y, radians);

		if (it == cp.nxforms) {
			cp.hasFinalXform = true;
		}

		changed = true;
		updateFlame(true);
	}

	/*****************************************************************************/

	public void btTrgRotateLeft90Click() {
		int it = selectedTriangle;
		rotateTriangle(it, Math.PI / 2);
	}

	/*****************************************************************************/

	public void btTrgRotateLeftClick() {
		try {
			String s = getString(find("txtTrgRotateValue"), "text");
			double angle = Double.parseDouble(s) * Math.PI / 180;
			rotateTriangle(selectedTriangle, angle);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/*****************************************************************************/

	public void btTrgRotateRightClick() {
		try {
			String s = getString(find("txtTrgRotateValue"), "text");
			double angle = Double.parseDouble(s) * Math.PI / 180;
			rotateTriangle(selectedTriangle, -angle);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/*****************************************************************************/

	public void btTrgRotateRight90Click() {
		int it = selectedTriangle;
		rotateTriangle(it, -Math.PI / 2);
	}

	/*****************************************************************************/

	public SPoint getPivot() {
		return getPivot(selectedTriangle);
	}

	public SPoint getPivot(int n) {

		if (pivotMode == PIVOT_LOCAL) {
			Triangle t = Global.mainTriangles[n];
			pivot.x = t.x[1] + (t.x[0] - t.x[1]) * localPivot.x
					+ (t.x[2] - t.x[1]) * localPivot.y;
			pivot.y = t.y[1] + (t.y[0] - t.y[1]) * localPivot.x
					+ (t.y[2] - t.y[1]) * localPivot.y;
		} else {
			pivot.x = worldPivot.x;
			pivot.y = worldPivot.y;
		}

		return pivot;

	} // End of method getPivot

	/*****************************************************************************/

	public void btTrgMoveLeftClick() {
		moveTriangle(-1, 0);
	}

	/*****************************************************************************/

	public void btTrgMoveRightClick() {
		moveTriangle(1, 0);
	}

	/*****************************************************************************/

	public void btTrgMoveUpClick() {
		moveTriangle(0, 1);
	}

	/*****************************************************************************/

	public void btTrgMoveDownClick() {
		moveTriangle(0, -1);
	}

	/*****************************************************************************/

	void moveTriangle(double dx, double dy) {
		try {
			String s = getString(find("txtTrgMoveValue"), "text");
			double offset = Double.parseDouble(s);

			Global.main.updateUndo();

			Global.mainTriangles[selectedTriangle].move(dx * offset, dy
					* offset);

			if (selectedTriangle == cp.nxforms) {
				cp.hasFinalXform = true;
			}

			updateFlame(true);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	} // End of method moveTriangle

	/*****************************************************************************/

	int getTriangleColor(int n) {
		if (n == Global.transforms) {
			return 0xFFFFFF;
		} else if (Global.useTransformColors) {
			return colorValToColor(Global.mainCP.cmap, cp.xform[n].color);
		} else {
			return trgColors[n % trgColors.length];
		}

	} // End of method getTriangleColor

	/*****************************************************************************/

	int colorValToColor(int cmap[][], double value) {
		int i = (int) (value * 255);
		if (i < 0) {
			i = 0;
		}
		if (i > 255) {
			i = 255;
		}
		return (cmap[i][0] << 16) | (cmap[i][1] << 8) | cmap[i][2];
	}

	/*****************************************************************************/

	void showSelectedInfo() {
		updating = true;

		if (selectedTriangle < 0) {
			selectedTriangle = 0;
		}
		if (selectedTriangle > lastTriangle()) {
			selectedTriangle = lastTriangle();
		}

		setInteger(find("cbTransforms"), "selected", selectedTriangle);

		showTriangleCoordinates();

		showTransformCoordinates();

		XForm xform = cp.xform[selectedTriangle];

		setBoolean(find("tbPostXswap"), "selected", xform.postXswap);
		setBoolean(find("mnuPostXswap"), "selected", xform.postXswap);

		setString(find("txtP"), "text", format(xform.density));

		setString(find("txtSymmetry"), "text", format(xform.symmetry));
		setInteger(find("scrollSymmetry"), "value",
				(int) (xform.symmetry * 1000));

		setString(find("txtColor"), "text", format(xform.color));
		setColor(find("pnlColor"), "background", valueToColor(xform.color));
		setInteger(find("scrollColor"), "value", (int) (xform.color * 1000));

		int nv = XForm.getNrVariations();
		for (int i = 0; i < nv; i++) {
			if (xform.vars[i] == 0) {
				setString(find("variation" + i), "text", "");
			} else {
				setString(find("variation" + i), "text", "" + xform.vars[i]);
			}
		}

		int np = XForm.getNrParameters();
		for (int i = 0; i < np; i++) {
			setString(find("txtParameter" + i), "text", "" + xform.pvalues[i]);
		}

		updateParameterList();

		showPivotCoordinates();

		showPreviewParameters();

		updateWidgets();

		if (showVarPreview) {
			buildVarPreview();
		}

		setBoolean(find("chkEnabled"), "selected", xform.enabled);

		updating = false;

		repaint();

	} // End of method showSelectedInfo

	/*****************************************************************************/

	void showTriangleCoordinates() {
		if (selectedTriangle < 0) {
			return;
		}

		Triangle t = Global.mainTriangles[selectedTriangle];

		setString(find("txtAx"), "text", format(t.x[0]));
		setString(find("txtAy"), "text", format(t.y[0]));
		setString(find("txtBx"), "text", format(t.x[1]));
		setString(find("txtBy"), "text", format(t.y[1]));
		setString(find("txtCx"), "text", format(t.x[2]));
		setString(find("txtCy"), "text", format(t.y[2]));

	} // End of method showTriangleCoordinates

	/*****************************************************************************/

	void showTransformCoordinates() {
		if (selectedTriangle < 0) {
			return;
		}

		XForm xform = cp.xform[selectedTriangle];

		if (getBoolean(find("btnCoefsRect"), "selected")) {
			setString(find("txtA"), "text", format(xform.c00));
			setString(find("txtB"), "text", format(-xform.c01));
			setString(find("txtC"), "text", format(-xform.c10));
			setString(find("txtD"), "text", format(xform.c11));
			setString(find("txtE"), "text", format(xform.c20));
			setString(find("txtF"), "text", format(-xform.c21));

			setString(find("txtPost00"), "text", format(xform.p00));
			setString(find("txtPost01"), "text", format(-xform.p01));
			setString(find("txtPost10"), "text", format(-xform.p10));
			setString(find("txtPost11"), "text", format(xform.p11));
			setString(find("txtPost20"), "text", format(xform.p20));
			setString(find("txtPost21"), "text", format(-xform.p21));
		} else {
			setString(find("txtA"), "text", format(hypot(xform.c00, xform.c01)));
			setString(find("txtB"), "text",
					format(arctan2(-xform.c01, xform.c00)));
			setString(find("txtC"), "text", format(hypot(xform.c10, xform.c11)));
			setString(find("txtD"), "text",
					format(arctan2(xform.c11, -xform.c10)));
			setString(find("txtE"), "text", format(hypot(xform.c20, xform.c21)));
			setString(find("txtF"), "text",
					format(arctan2(-xform.c21, xform.c20)));

			setString(find("txtPost00"), "text",
					format(hypot(xform.p00, xform.p01)));
			setString(find("txtPost01"), "text",
					format(arctan2(-xform.p01, xform.p00)));
			setString(find("txtPost10"), "text",
					format(hypot(xform.p10, xform.p11)));
			setString(find("txtPost11"), "text",
					format(arctan2(xform.p11, -xform.p10)));
			setString(find("txtPost20"), "text",
					format(hypot(xform.p20, xform.p21)));
			setString(find("txtPost21"), "text",
					format(arctan2(-xform.p21, xform.p20)));
		}

	} // End of method showTransformCoordinates

	/*****************************************************************************/

	void showPivotCoordinates() {
		if (pivotMode == PIVOT_LOCAL) {
			setString(find("editPivotX"), "text", "" + localPivot.x);
			setString(find("editPivotY"), "text", "" + localPivot.y);
			setString(find("btnPivotMode"), "text", " Local Pivot ");
			setBoolean(find("tbPivotMode"), "selected", false);
		} else {
			setString(find("editPivotX"), "text", "" + worldPivot.x);
			setString(find("editPivotY"), "text", "" + worldPivot.y);
			setString(find("btnPivotMode"), "text", "World Pivot");
			setBoolean(find("tbPivotMode"), "selected", true);
		}

	} // End of method showPivotCoordinates

	/*****************************************************************************/

	void showPreviewParameters() {
		setInteger(find("trkVarPreviewDensity"), "value", varPreviewDensity);
		setString(find("trkVarPreviewDensity"), "tooltip", "Density: "
				+ varPreviewDensity);

		setInteger(find("trkVarPreviewRange"), "value", varPreviewRange);
		setString(find("trkVarPreviewRange"), "tooltip", "Range: "
				+ varPreviewRange);

		setInteger(find("trkVarPreviewDepth"), "value", varPreviewDepth);
		setString(find("trkVarPreviewDepth"), "tooltip", "Depth: "
				+ varPreviewDepth);

	}

	/*****************************************************************************/

	double hypot(double x, double y) {
		return Math.sqrt(x * x + y * y);
	}

	/*****************************************************************************/

	double arctan2(double x, double y) {
		return Math.atan2(x, y) * 180 / Math.PI;
	}

	/*****************************************************************************/

	String format(double x) {
		int k = (int) (x * 1000000);
		return "" + (k / 1000000.0);
	}

	/*****************************************************************************/

	public void cbTransformsChange(Object combo) {
		int index = getSelectedIndex(combo);

		if ((index != selectedTriangle) && (index >= 0)
				&& (index <= lastTriangle())) {
			selectedTriangle = index;
			showSelectedInfo();
		}
	} // End of method cbTransformsChange

	/*****************************************************************************/

	public void triangleViewKeyPress(KeyEvent e) {
		int keycode = e.getKeyCode();
		char keychar = e.getKeyChar();

		if (keychar == '-') {
			graphZoom = graphZoom * 0.8;
			displayZoom();
			repaint(find("TriangleView"));
		}

		else if (keychar == '+') {
			graphZoom = graphZoom * 1.25;
			displayZoom();
			repaint(find("TriangleView"));
		}

		else if (keycode == KeyEvent.VK_LEFT) {
			moveTriangle(-1, 0);
		} else if (keycode == KeyEvent.VK_RIGHT) {
			moveTriangle(1, 0);
		} else if (keycode == KeyEvent.VK_UP) {
			moveTriangle(0, 1);
		} else if (keycode == KeyEvent.VK_DOWN) {
			moveTriangle(0, -1);
		}

	}

	/*****************************************************************************/

	public void cornerEditExit() {

		try {
			Global.mainTriangles[selectedTriangle].x[0] = Double.parseDouble(
					getString(find("txtAx"), "text"));

			Global.mainTriangles[selectedTriangle].y[0] = Double.parseDouble(
					getString(find("txtAy"), "text"));

			Global.mainTriangles[selectedTriangle].x[1] = Double.parseDouble(
					getString(find("txtBx"), "text"));

			Global.mainTriangles[selectedTriangle].y[1] = Double.parseDouble(
					getString(find("txtBy"), "text"));

			Global.mainTriangles[selectedTriangle].x[2] = Double.parseDouble(
					getString(find("txtCx"), "text"));

			Global.mainTriangles[selectedTriangle].y[2] = Double.parseDouble(
					getString(find("txtCy"), "text"));

			Global.main.updateUndo();
			updateFlame(true);
		} catch (Exception ex) {
			System.out.println(ex);
		}
	} // End of method cornerEditExit

	/*****************************************************************************/

	public void cornerEditKeyPress() {
		cornerEditExit();
	} // End of method cornerEditKeyPress

	/*****************************************************************************/

	public void coefValidate() {

		String s00 = getString(find("txtA"), "text");
		String s01 = getString(find("txtB"), "text");
		String s10 = getString(find("txtC"), "text");
		String s11 = getString(find("txtD"), "text");
		String s20 = getString(find("txtE"), "text");
		String s21 = getString(find("txtF"), "text");

		Global.main.updateUndo();

		if (getBoolean(find("btnCoefsRect"), "selected")) {
			// rectangular coordinates

			try {
				cp.xform[selectedTriangle].c00 = Double.parseDouble(s00);
			} catch (Exception ex) {
			}

			try {
				cp.xform[selectedTriangle].c01 = -Double.parseDouble(s01);
			} catch (Exception ex) {
			}

			try {
				cp.xform[selectedTriangle].c10 = -Double.parseDouble(s10);
			} catch (Exception ex) {
			}

			try {
				cp.xform[selectedTriangle].c11 = Double.parseDouble(s11);
			} catch (Exception ex) {
			}

			try {
				cp.xform[selectedTriangle].c20 = Double.parseDouble(s20);
			} catch (Exception ex) {
			}

			try {
				cp.xform[selectedTriangle].c21 = -Double.parseDouble(s21);
			} catch (Exception ex) {
			}

		} else {
			// polar coordinates

			double x = 0, y = 0, r = 0, a = 0;

			try {
				r = Double.parseDouble(s00);
				a = Double.parseDouble(s01) * Math.PI / 180.0;
				x = r * Math.cos(a);
				y = r * Math.sin(a);
				cp.xform[selectedTriangle].c00 = x;
				cp.xform[selectedTriangle].c01 = -y;
			} catch (Exception ex) {
			}

			try {
				r = Double.parseDouble(s10);
				a = Double.parseDouble(s11) * Math.PI / 180.0;
				x = r * Math.cos(a);
				y = r * Math.sin(a);
				cp.xform[selectedTriangle].c10 = -x;
				cp.xform[selectedTriangle].c11 = y;
			} catch (Exception ex) {
			}

			try {
				r = Double.parseDouble(s20);
				a = Double.parseDouble(s21) * Math.PI / 180.0;
				x = r * Math.cos(a);
				y = r * Math.sin(a);
				cp.xform[selectedTriangle].c20 = x;
				cp.xform[selectedTriangle].c21 = -y;
			} catch (Exception ex) {
			}
		}

		cp.trianglesFromCP(Global.mainTriangles);

		showSelectedInfo();

		updateFlame(true);

	} // End of method coefValidate

	/*****************************************************************************/

	public void postValidate() {

		String s00 = getString(find("txtPost00"), "text");
		String s01 = getString(find("txtPost01"), "text");
		String s10 = getString(find("txtPost10"), "text");
		String s11 = getString(find("txtPost11"), "text");
		String s20 = getString(find("txtPost20"), "text");
		String s21 = getString(find("txtPost21"), "text");

		Global.main.updateUndo();

		if (getBoolean(find("btnCoefsRect"), "selected")) {
			// rectangular coordinates

			try {
				cp.xform[selectedTriangle].p00 = Double.parseDouble(s00);
			} catch (Exception ex) {
			}

			try {
				cp.xform[selectedTriangle].p01 = -Double.parseDouble(s01);
			} catch (Exception ex) {
			}

			try {
				cp.xform[selectedTriangle].p10 = -Double.parseDouble(s10);
			} catch (Exception ex) {
			}

			try {
				cp.xform[selectedTriangle].p11 = Double.parseDouble(s11);
			} catch (Exception ex) {
			}

			try {
				cp.xform[selectedTriangle].p20 = Double.parseDouble(s20);
			} catch (Exception ex) {
			}

			try {
				cp.xform[selectedTriangle].p21 = -Double.parseDouble(s21);
			} catch (Exception ex) {
			}

		} else {
			// polar coordinates

			double x = 0, y = 0, r = 0, a = 0;

			try {
				r = Double.parseDouble(s00);
				a = Double.parseDouble(s01) * Math.PI / 180.0;
				x = r * Math.cos(a);
				y = r * Math.sin(a);
				cp.xform[selectedTriangle].p00 = x;
				cp.xform[selectedTriangle].p01 = -y;
			} catch (Exception ex) {
			}

			try {
				r = Double.parseDouble(s10);
				a = Double.parseDouble(s11) * Math.PI / 180.0;
				x = r * Math.cos(a);
				y = r * Math.sin(a);
				cp.xform[selectedTriangle].p10 = -x;
				cp.xform[selectedTriangle].p11 = y;
			} catch (Exception ex) {
			}

			try {
				r = Double.parseDouble(s20);
				a = Double.parseDouble(s21) * Math.PI / 180.0;
				x = r * Math.cos(a);
				y = r * Math.sin(a);
				cp.xform[selectedTriangle].p20 = x;
				cp.xform[selectedTriangle].p21 = -y;
			} catch (Exception ex) {
			}
		}

		cp.trianglesFromCP(Global.mainTriangles);

		showSelectedInfo();

		updateFlame(true);

	} // End of method coefValidate

	/*****************************************************************************/

	public void btnResetCoefsClick() {

		Global.main.updateUndo();

		cp.xform[selectedTriangle].c00 = 1;
		cp.xform[selectedTriangle].c01 = 0;
		cp.xform[selectedTriangle].c10 = 0;
		cp.xform[selectedTriangle].c11 = 1;
		cp.xform[selectedTriangle].c20 = 0;
		cp.xform[selectedTriangle].c21 = 0;

		showSelectedInfo();
		cp.trianglesFromCP(Global.mainTriangles);
		updateFlame(true);

	} // End of method btnResetCoefsClick

	/*****************************************************************************/

	public void btnResetPostCoefsClick() {

		Global.main.updateUndo();

		cp.xform[selectedTriangle].p00 = 1;
		cp.xform[selectedTriangle].p01 = 0;
		cp.xform[selectedTriangle].p10 = 0;
		cp.xform[selectedTriangle].p11 = 1;
		cp.xform[selectedTriangle].p20 = 0;
		cp.xform[selectedTriangle].p21 = 0;

		showSelectedInfo();
		cp.trianglesFromCP(Global.mainTriangles);
		updateFlame(true);

	} // End of method btnResetCoefsClick

	/*****************************************************************************/

	public void btTrgScaleClick(int option) {

		try {
			double scale = 0;

			String s = getString(find("txtTrgScaleValue"), "text");
			double value = Double.parseDouble(s);

			if (option == -1) {
				scale = 100.0 / value;
			} else if (option == 1) {
				scale = value / 100.0;
			}

			if (scale == 0) {
				scale = 1e-6;
			}

			getPivot();
			Global.main.updateUndo();
			Global.mainTriangles[selectedTriangle].scaleAroundPoint(pivot.x,
					pivot.y, scale);

			if (selectedTriangle == cp.nxforms) {
				cp.hasFinalXform = true;
			}

			updateFlame(true);
		} catch (Exception ex) {
		}

	} // End of method btTrgScaleDownClick

	/*****************************************************************************/

	public void updateUndoControls(int undoindex, int undomax) {
		setBoolean(find("btUndo"), "enabled", undoindex > 0);
		setBoolean(find("mnuUndo"), "enabled", undoindex > 0);
		setBoolean(find("mnuDummyUndo"), "enabled", undoindex > 0);
		setBoolean(find("btRedo"), "enabled", undoindex < undomax);
		setBoolean(find("mnuRedo"), "enabled", undoindex < undomax);
		setBoolean(find("mnuDummyRedo"), "enabled", undoindex < undomax);
	}

	/*****************************************************************************/

	public void undo() {
		Global.main.undo();
	}

	/*****************************************************************************/

	public void redo() {
		Global.main.redo();
	}

	/*****************************************************************************/

	public void tbEnableFinalXformClick(Object button) {
		Object combo = find("cbTransforms");

		Global.main.updateUndo();

		Global.enableFinalXform = getBoolean(button, "selected");
		if (!cp.hasFinalXform) {
			if (Global.enableFinalXform) {
				Object choice = createImpl("choice");
				setString(choice, "text", "Final");
				setColor(choice, "foreground", Color.white);
				setColor(choice, "background", Color.black);
				add(combo, choice);

				selectedTriangle = Global.transforms;
			} else {
				int n = getCount(combo);
				if (n == Global.transforms + 1) {
					remove(getItem(combo, n - 1));
				}
				if (selectedTriangle >= Global.transforms) {
					selectedTriangle = Global.transforms - 1;
				}
			}
		}

		cp.finalXformEnabled = Global.enableFinalXform;
		updateFlame(true);

		repaint();

	} // End of method tbEnableFinalXformClick

	/*****************************************************************************/

	public void btnPivotModeClick() {
		if (pivotMode != PIVOT_LOCAL) {
			pivotMode = PIVOT_LOCAL;
		} else {
			pivotMode = PIVOT_WORLD;
		}

		repaint();

		showSelectedInfo();

	} // End of method btnPivotModeClick

	/*****************************************************************************/

	public void btnPickPivotClick() {
		oldMode = editMode;
		editMode = MODE_PICK;
	}

	/*****************************************************************************/

	public void pivotValidate() {
		double value;

		try {
			value = Double.parseDouble(getString(find("editPivotX"), "text"));
			if (pivotMode == PIVOT_LOCAL) {
				localPivot.x = value;
			} else {
				worldPivot.x = value;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			value = Double.parseDouble(getString(find("editPivotY"), "text"));
			if (pivotMode == PIVOT_LOCAL) {
				localPivot.y = value;
			} else {
				worldPivot.y = value;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		showSelectedInfo();

	} // End of method pivotValidate

	/*****************************************************************************/

	public void btnResetPivotClick() {
		if (pivotMode == PIVOT_LOCAL) {
			localPivot.x = localPivot.y = 0;
		} else {
			worldPivot.x = worldPivot.y = 0;
		}

		showSelectedInfo();

	} // End of method btnResetPivotClick

	/*****************************************************************************/

	public void btCopyTriangleClick() {
		int it = selectedTriangle;
		memTriangle.copy(Global.mainTriangles[it]);
	} // End of method btCopyTriangleClick

	/*****************************************************************************/

	public void btPasteTriangleClick() {
		int it = selectedTriangle;
		if (!memTriangle.equals(Global.mainTriangles[it])) {
			Global.main.updateUndo();
			Global.mainTriangles[it].copy(memTriangle);
			updateFlame(true);
		}
	}

	/*****************************************************************************/

	public void mnuFlipHorizontalClick() {
		int it = selectedTriangle;

		Global.main.updateUndo();

		getPivot(it);

		Global.mainTriangles[it].x[0] = 2 * pivot.x
				- Global.mainTriangles[it].x[0];
		Global.mainTriangles[it].x[1] = 2 * pivot.x
				- Global.mainTriangles[it].x[1];
		Global.mainTriangles[it].x[2] = 2 * pivot.x
				- Global.mainTriangles[it].x[2];

		updateFlame(true);
	} // End of method mnuFlipHorizontalClick

	/*****************************************************************************/

	public void mnuFlipVerticalClick() {
		int it = selectedTriangle;

		Global.main.updateUndo();

		getPivot(it);

		Global.mainTriangles[it].y[0] = 2 * pivot.y
				- Global.mainTriangles[it].y[0];
		Global.mainTriangles[it].y[1] = 2 * pivot.y
				- Global.mainTriangles[it].y[1];
		Global.mainTriangles[it].y[2] = 2 * pivot.y
				- Global.mainTriangles[it].y[2];

		updateFlame(true);
	} // End of method mnuFlipVerticalClick

	/*****************************************************************************/

	public void txtColorChange(Object field) {
		try {
			double value = Double.parseDouble(getString(field, "text"));

			if (cp.xform[selectedTriangle].color != value) {
				if (value < 0) {
					value = 0;
				} else if (value > 1) {
					value = 1;
				}

				cp.xform[selectedTriangle].color = value;

				setColor(find("pnlColor"), "background", valueToColor(value));
				setInteger(find("scrollColor"), "value", (int) (1000 * value));
				setString(find("txtColor"), "text", "" + value);

				updateColor();
			}
		} catch (Exception ex) {
		}

	} // End of method txtColorChange

	/*****************************************************************************/

	public void scrollColorChange(Object slider) {
		int k = getInteger(slider, "value");
		double value = k / 1000.0;

		if (cp.xform[selectedTriangle].color != value) {
			cp.xform[selectedTriangle].color = value;
			setColor(find("pnlColor"), "background", valueToColor(value));
			setString(find("txtColor"), "text", ""
					+ cp.xform[selectedTriangle].color);
		}

	} // End of method scrollColorChange

	/*****************************************************************************/

	public void updateColor() {
		changed = true;
		updateFlame(true);
	}

	/*****************************************************************************/

	Color valueToColor(double value) {
		int ic = (int) (value * 255);
		if (ic < 0) {
			ic = 0;
		} else if (ic > 255) {
			ic = 255;
		}

		return new Color(Global.mainCP.cmap[ic][0], Global.mainCP.cmap[ic][1],
				Global.mainCP.cmap[ic][2]);
	}

	/*****************************************************************************/

	public void txtSymmetryChange(Object field) {
		try {
			double value = Double.parseDouble(getString(field, "text"));

			if (cp.xform[selectedTriangle].symmetry != value) {
				if (value < -1) {
					value = -1;
				} else if (value > 1) {
					value = 1;
				}

				cp.xform[selectedTriangle].symmetry = value;

				setInteger(find("scrollSymmetry"), "value",
						(int) (1000 * value));
				setString(find("txtSymmetry"), "text", "" + value);

				updateSymmetry();
			}
		} catch (Exception ex) {
		}

	} // End of method txtSymmetryChange

	/*****************************************************************************/

	public void scrollSymmetryChange(Object slider) {
		int k = getInteger(slider, "value");
		double value = k / 1000.0;

		if (cp.xform[selectedTriangle].symmetry != value) {
			cp.xform[selectedTriangle].symmetry = value;
			setString(find("txtSymmetry"), "text", ""
					+ cp.xform[selectedTriangle].symmetry);
		}

	} // End of method scrollColorChange

	/*****************************************************************************/

	public void updateSymmetry() {
		changed = true;
		updateFlame(true);
	}

	/*****************************************************************************/

	Image buildTriangleIcon(int color) {

		int rgb0 = 0xFF000000;
		int rgb1 = 0xFF | color;
		int rgb2 = (new Color(color)).darker().getRGB();

		int[] pixels = new int[] { rgb0, rgb0, rgb0, rgb0, rgb0, rgb0, rgb0,
				rgb0, rgb0, rgb0, rgb0, rgb0, rgb0, rgb0, rgb0, rgb0, rgb0,
				rgb0, rgb0, rgb0, rgb0, rgb0, rgb1, rgb0, rgb0, rgb0, rgb0,
				rgb0, rgb0, rgb0, rgb0, rgb0, rgb0, rgb1, rgb1, rgb0, rgb0,
				rgb0, rgb0, rgb0, rgb0, rgb0, rgb0, rgb0, rgb1, rgb2, rgb1,
				rgb0, rgb0, rgb0, rgb0, rgb0, rgb0, rgb0, rgb0, rgb1, rgb2,
				rgb2, rgb1, rgb0, rgb0, rgb0, rgb0, rgb0, rgb0, rgb0, rgb1,
				rgb2, rgb2, rgb2, rgb1, rgb0, rgb0, rgb0, rgb0, rgb0, rgb0,
				rgb1, rgb2, rgb2, rgb2, rgb2, rgb1, rgb0, rgb0, rgb0, rgb0,
				rgb0, rgb1, rgb2, rgb2, rgb2, rgb2, rgb2, rgb1, rgb0, rgb0,
				rgb0, rgb0, rgb1, rgb2, rgb2, rgb2, rgb2, rgb2, rgb2, rgb1,
				rgb0, rgb0, rgb0, rgb1, rgb2, rgb2, rgb2, rgb2, rgb2, rgb2,
				rgb2, rgb1, rgb0, rgb0, rgb1, rgb1, rgb1, rgb1, rgb1, rgb1,
				rgb1, rgb1, rgb1, rgb1, rgb0, rgb0, rgb0, rgb0, rgb0, rgb0,
				rgb0, rgb0, rgb0, rgb0, rgb0, rgb0, rgb0 };

		MemoryImageSource source = new MemoryImageSource(12, 12, pixels, 0, 12);
		return createImage(source);

	} // End of method buildTriangleIcon

	/*****************************************************************************/

	double log10(double x) {
		return Math.log(x) / Math.log(10);
	}

	/*****************************************************************************/

	double roundto(double x, int n) {
		double p10 = Math.pow(10.0, n);
		int k = (int) (x / p10);
		return k * p10;
	}

	/*****************************************************************************/

	public void drawStatusBar(Graphics g, Rectangle bounds) {
		swidth = bounds.width;
		sheight = bounds.height;
		g.setColor(bg);
		g.fillRect(0, 0, bounds.width, bounds.height);
		g.setColor(Color.black);
		g.setFont(sfont);
		g.drawString(status1, hstatus1, bounds.height - 5);
		g.drawString(status2, hstatus2, bounds.height - 5);
		g.drawString(status3, hstatus3, bounds.height - 5);
	}

	/*****************************************************************************/

	int getWidget(double x, double y) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 2; j++) {
				double d = distanceToSegment(fmove[0], fmove[1],
						widgets[i][j][0], widgets[i][j][1],
						widgets[i][j + 1][0], widgets[i][j + 1][1]);
				if (Math.abs(d) * graphZoom * 50 < 3) {
					return i;
				}
			}
		}

		return -1;

	} // End of method getWidget

	/*****************************************************************************/

	int getEdge(double x, double y, Triangle t) {
		double d;

		d = distanceToSegment(x, y, t.x[0], t.y[0], t.x[2], t.y[2]);
		if (Math.abs(d) * graphZoom * 50 < 3) {
			return 1;
		}

		d = distanceToSegment(x, y, t.x[1], t.y[1], t.x[2], t.y[2]);
		if (Math.abs(d) * graphZoom * 50 < 3) {
			return 0;
		}

		d = distanceToSegment(x, y, t.x[0], t.y[0], t.x[1], t.y[1]);
		if (Math.abs(d) * graphZoom * 50 < 3) {
			return 2;
		}

		return -1;
	}

	/*****************************************************************************/

	int getCorner(double x, double y, Triangle t) {
		for (int j = 0; j < 3; j++) {
			double dx = t.x[j] - x;
			double dy = t.y[j] - y;
			double d = Math.sqrt(dx * dx + dy * dy);
			if (d * graphZoom * 50 < 4) {
				return j;
			}
		}

		return -1;

	} // getCorner

	/*****************************************************************************/

	int insideTriangle(double x, double y) {
		int j = 2, k;
		boolean inside = false;

		for (k = lastTriangle(); k >= 0; k--) {
			Triangle t = Global.mainTriangles[k];
			for (int i = 0; i <= 2; i++) {
				if ((((t.y[i] <= y) && (y < t.y[j])) || ((t.y[j] <= y) && (y < t.y[i])))
						&& (x < (t.x[j] - t.x[i]) * (y - t.y[i])
								/ (t.y[j] - t.y[i]) + t.x[i])) {
					inside = !inside;
				}
				j = i;
			}
			if (inside) {
				break;
			}
		}

		if (inside) {
			return k;
		} else {
			return -1;
		}
	} // End of method insideTriangle

	/*****************************************************************************/

	public void tbVarPreviewClick(Object button) {
		showVarPreview = getBoolean(button, "selected");
		setBoolean(find("mnuVarPreview"), "selected", showVarPreview);

		if (showVarPreview) {
			buildVarPreview();
		} else {
			vimage = null;
			source = null;
			System.gc();
		}
		repaint();
	}

	/*****************************************************************************/

	public void tbSelectClick(Object button) {
		selectMode = getBoolean(button, "selected");
		setBoolean(find("mnuSelect"), "selected", selectMode);
	}

	public void tbMoveClick() {
		editMode = MODE_MOVE;
	}

	public void tbRotateClick() {
		editMode = MODE_ROTATE;
	}

	public void tbScaleClick() {
		editMode = MODE_SCALE;
	}

	/*****************************************************************************/

	void showCoordinates(double fx, double fy) {
		fx = ((int) (fx * 1000) / 1000.0);
		fy = ((int) (fy * 1000) / 1000.0);

		status1 = "X: " + fx;
		status2 = "Y: " + fy;

	} // End of method showCoordinates

	/*****************************************************************************/

	public void trkVarPreviewChange(Object slider, int option) {
		int value = getInteger(slider, "value");

		switch (option) {
		case 0:
			varPreviewRange = value;
			break;
		case 1:
			varPreviewDensity = value;
			break;
		case 2:
			varPreviewDepth = value;
			break;
		}

		showPreviewParameters();

		if (showVarPreview) {
			buildVarPreview();
			repaint();
		}

	}

	/*****************************************************************************/

	public void btExtendedEditClick(Object button) {
		extendedEdit = getBoolean(button, "selected");
		setBoolean(find("mnuExtendedEdit"), "selected", extendedEdit);
		repaint(find("TriangleView"));
	}

	/*****************************************************************************/

	public void btAxisLockClick(Object button) {
		axisLock = getBoolean(button, "selected");
		setBoolean(find("mnuAxisLock"), "selected", axisLock);
	}

	/*****************************************************************************/

	public void mnuResetLocationClick(Object button) {
		Global.resetLocation = getBoolean(button, "selected");
		if (Global.resetLocation) {
			cp.width = Global.mainCP.width;
			cp.height = Global.mainCP.height;
			cp.pixels_per_unit = Global.mainCP.pixels_per_unit;
			cp.adjustScale(previewwidth, previewheight);
			cp.zoom = Global.mainCP.zoom;
			cp.center[0] = Global.mainCP.center[0];
			cp.center[1] = Global.mainCP.center[1];
		}

		drawPreview();

	} // End of method mnuResetLocationClick

	/*****************************************************************************/

	public void mnuAutoZoomClick() {
		mustautozoom = true;
		repaint();
	}

	/*****************************************************************************/

	public void mnuVarPreviewClick(Object button) {
		showVarPreview = getBoolean(button, "selected");
		setBoolean(find("tbVarPreview"), "selected", showVarPreview);

		if (showVarPreview) {
			buildVarPreview();
		} else {
			vimage = null;
			source = null;
			System.gc();
		}
		repaint();
	}

	/*****************************************************************************/

	public void mnuSelectClick(Object button) {
		selectMode = getBoolean(button, "selected");
		setBoolean(find("tbSelect"), "selected", selectMode);
	}

	/*****************************************************************************/

	public void mnuExtendedEditClick(Object button) {
		extendedEdit = getBoolean(button, "selected");
		setBoolean(find("btExtendedEdit"), "selected", extendedEdit);
		repaint(find("TriangleView"));
	}

	/*****************************************************************************/

	public void mnuAxisLockClick(Object button) {
		axisLock = getBoolean(button, "selected");
		setBoolean(find("btAxisLock"), "selected", axisLock);
	}

	/*****************************************************************************/

	public void mnuFlipAllVerticalClick() {
		Global.main.updateUndo();

		for (int i = 0; i <= Global.transforms; i++) {
			Global.mainTriangles[i].flipVertical();
		}
		Global.mainTriangles[M1].flipVertical();

		cp.getFromTriangles(Global.mainTriangles, Global.transforms);
		cp.trianglesFromCP(Global.mainTriangles);
		mustautozoom = true;
		updateFlame(true);

	} // End of method mnuFlipAllVerticalClick

	/*****************************************************************************/

	public void mnuFlipAllHorizontalClick() {
		Global.main.updateUndo();

		for (int i = 0; i <= Global.transforms; i++) {
			Global.mainTriangles[i].flipHorizontal();
		}
		Global.mainTriangles[M1].flipHorizontal();

		cp.getFromTriangles(Global.mainTriangles, Global.transforms);
		cp.trianglesFromCP(Global.mainTriangles);
		mustautozoom = true;
		updateFlame(true);

	} // End of method mnuFlipAllHorizontalClick

	/*****************************************************************************/

	public void mnuPostXswapClick() {
		XForm xform = cp.xform[selectedTriangle];

		xform.postXswap = !xform.postXswap;
		setBoolean(find("tbPostXswap"), "selected", xform.postXswap);
		setBoolean(find("mnuPostXswap"), "selected", xform.postXswap);
	}

	/*****************************************************************************/

	public void mnuResetTriangleClick() {
		int it = mouseOverTriangle;
		if (it < 0) {
			return;
		}
		if (Global.mainTriangles[it].equals(Global.mainTriangles[M1])) {
			return;
		}

		Global.main.updateUndo();
		Global.mainTriangles[it].copy(Global.mainTriangles[M1]);
		updateFlame(true);

	} // End of method mnuResetTriangleClick

	/*****************************************************************************/

	public void btnXcoefsClick() {
		if ((cp.xform[selectedTriangle].c00 == 1)
				&& (cp.xform[selectedTriangle].c01 == 0)) {
			return;
		}

		Global.main.updateUndo();
		cp.xform[selectedTriangle].c00 = 1;
		cp.xform[selectedTriangle].c01 = 0;
		cp.trianglesFromCP(Global.mainTriangles);
		updateFlame(true);
	}

	/*****************************************************************************/

	public void btnYcoefsClick() {
		if ((cp.xform[selectedTriangle].c10 == 0)
				&& (cp.xform[selectedTriangle].c11 == 1)) {
			return;
		}

		Global.main.updateUndo();
		cp.xform[selectedTriangle].c10 = 0;
		cp.xform[selectedTriangle].c11 = 1;
		cp.trianglesFromCP(Global.mainTriangles);
		updateFlame(true);
	}

	/*****************************************************************************/

	public void btnOcoefsClick() {
		int it = selectedTriangle;

		if ((cp.xform[it].c20 == 0) && (cp.xform[it].c21 == 0)) {
			return;
		}

		Global.main.updateUndo();
		cp.xform[it].c20 = 0;
		cp.xform[it].c21 = 0;
		cp.trianglesFromCP(Global.mainTriangles);
		updateFlame(true);
	}

	/*****************************************************************************/

	public void mnuResetRotationClick() {
		double dx, dy, ax, ay, da;
		double nx0, ny0, nx2, ny2;

		Triangle t = Global.mainTriangles[selectedTriangle];

		ax = Math.round(Math.atan2(xy, xx) / PI2);
		ay = Math.round(Math.atan2(yy, yx) / PI2);
		dx = hypot(xx, xy);
		dy = hypot(yx, yy);
		if (xx * yy - yx * xy >= 0) {
			da = 1;
		} else {
			da = -1;
		}
		if (ax == ay) {
			ay = ay + da;
		} else if (Math.abs(ax - ay) == 2) {
			ay = ay - da;
		}

		nx0 = t.x[1] + dx * Math.cos(ax * PI2);
		ny0 = t.y[1] + dx * Math.sin(ax * PI2);
		nx2 = t.x[1] + dy * Math.cos(ay * PI2);
		ny2 = t.y[1] + dy * Math.sin(ay * PI2);

		if ((t.x[0] == nx0) && (t.y[0] == ny0) && (t.x[2] == nx2)
				&& (t.y[2] == ny2)) {
			return;
		}

		Global.main.updateUndo();

		t.x[0] = nx0;
		t.y[0] = ny0;
		t.x[2] = nx2;
		t.y[2] = ny2;

		updateFlame(true);

	} // End of method mnuResetRotationClick

	/*****************************************************************************/

	public void mnuResetScaleClick() {
		double dx, dy;
		double nx0, ny0, nx2, ny2;

		Triangle t = Global.mainTriangles[selectedTriangle];

		dx = hypot(xx, xy);
		dy = hypot(yx, yy);
		if (dx != 0) {
			nx0 = t.x[1] + (t.x[0] - t.x[1]) / dx;
			ny0 = t.y[1] + (t.y[0] - t.y[1]) / dx;
		} else {
			nx0 = t.x[1] + 1;
			ny0 = t.y[1];
		}

		if (dy != 0) {
			nx2 = t.x[1] + (t.x[2] - t.x[1]) / dy;
			ny2 = t.y[1] + (t.y[2] - t.y[1]) / dy;
		} else {
			nx2 = t.x[1];
			ny2 = t.y[1] + 1;
		}

		if ((t.x[0] == nx0) && (t.y[0] == ny0) && (t.x[2] == nx2)
				&& (t.y[2] == ny2)) {
			return;
		}

		Global.main.updateUndo();

		t.x[0] = nx0;
		t.y[0] = ny0;
		t.x[2] = nx2;
		t.y[2] = ny2;

		updateFlame(true);

	} // End of method mnuResetScaleClick

	/*****************************************************************************/

	public void chkEnabledClick(Object button) {
		cp.xform[selectedTriangle].enabled = getBoolean(button, "selected");

		// check that at least one transform is enabled

		if (countEnabledTransforms() == 0) {
			cp.xform[selectedTriangle].enabled = true;
			setBoolean(button, "selected", true);
		}

		updateFlame(true);
	}

	/*****************************************************************************/

	int countEnabledTransforms() {
		int n = 0;

		for (int i = 0; i < cp.nxforms; i++) {
			n += cp.xform[i].enabled ? 1 : 0;
		}

		return n;
	}

	/*****************************************************************************/

	public void btnXpostClick() {
		XForm xform = cp.xform[selectedTriangle];
		if ((xform.p00 == 1) && (xform.p01 == 0)) {
			return;
		}

		Global.main.updateUndo();
		xform.p00 = 1;
		xform.p01 = 0;
		cp.trianglesFromCP(Global.mainTriangles);
		updateFlame(true);

	} // End of method btnXpostClick

	/*****************************************************************************/

	public void btnYpostClick() {
		XForm xform = cp.xform[selectedTriangle];
		if ((xform.p10 == 0) && (xform.p11 == 1)) {
			return;
		}

		Global.main.updateUndo();
		xform.p10 = 0;
		xform.p11 = 1;
		cp.trianglesFromCP(Global.mainTriangles);
		updateFlame(true);

	} // End of method btnYpostClick

	/*****************************************************************************/

	public void btnOpostClick() {
		XForm xform = cp.xform[selectedTriangle];
		if ((xform.p20 == 0) && (xform.p21 == 0)) {
			return;
		}

		Global.main.updateUndo();
		xform.p20 = 0;
		xform.p21 = 0;
		cp.trianglesFromCP(Global.mainTriangles);
		updateFlame(true);

	} // End of method btnOpostClick

	/*****************************************************************************/

	void updateWidgets() {
		Triangle t = Global.mainTriangles[selectedTriangle];
		xx = t.x[0] - t.x[1];
		xy = t.y[0] - t.y[1];
		yx = t.x[2] - t.x[1];
		yy = t.y[2] - t.y[1];
		widgets[0][0][0] = t.x[1] + 0.8 * xx + yx;
		widgets[0][0][1] = t.y[1] + 0.8 * xy + yy;

		widgets[0][1][0] = t.x[1] + xx + yx;
		widgets[0][1][1] = t.y[1] + xy + yy;

		widgets[0][2][0] = t.x[1] + xx + 0.8 * yx;
		widgets[0][2][1] = t.y[1] + xy + 0.8 * yy;

		widgets[1][0][0] = t.x[1] - 0.8 * xx + yx;
		widgets[1][0][1] = t.y[1] - 0.8 * xy + yy;

		widgets[1][1][0] = t.x[1] - xx + yx;
		widgets[1][1][1] = t.y[1] - xy + yy;

		widgets[1][2][0] = t.x[1] - xx + 0.8 * yx;
		widgets[1][2][1] = t.y[1] - xy + 0.8 * yy;

		widgets[2][0][0] = t.x[1] - 0.8 * xx - yx;
		widgets[2][0][1] = t.y[1] - 0.8 * xy - yy;

		widgets[2][1][0] = t.x[1] - xx - yx;
		widgets[2][1][1] = t.y[1] - xy - yy;

		widgets[2][2][0] = t.x[1] - xx - 0.8 * yx;
		widgets[2][2][1] = t.y[1] - xy - 0.8 * yy;

		widgets[3][0][0] = t.x[1] + 0.8 * xx - yx;
		widgets[3][0][1] = t.y[1] + 0.8 * xy - yy;

		widgets[3][1][0] = t.x[1] + xx - yx;
		widgets[3][1][1] = t.y[1] + xy - yy;

		widgets[3][2][0] = t.x[1] + xx - 0.8 * yx;
		widgets[3][2][1] = t.y[1] + xy - 0.8 * yy;

	} // End of method updateWidgets

	/*****************************************************************************/

	public void btnCoefsModeClick() {
		showSelectedInfo();
	}

	/*****************************************************************************/

	public void txtPValidate(Object field) {
		try {
			double newval = Double.parseDouble(getString(field, "text"));
			cp.xform[selectedTriangle].density = newval;
			updateFlame(true);
		} catch (Exception ex) {
		}

	}

	/*****************************************************************************/

	static double dist(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	/*****************************************************************************/

	static double distanceToLine(double x, double y, double x1, double y1,
			double x2, double y2) {
		double n = (x2 - x1) * (y1 - y) - (x1 - x) * (y2 - y1);
		double d = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
		if (d == 0) {
			d = 1e-50;
		}
		return n / d;
	} // End of method distanceToLine

	/*****************************************************************************/

	static double distanceToSegment(double x, double y, double x1, double y1,
			double x2, double y2) {
		double a, b, e, c;

		if ((x == x1) && (y == y1)) {
			a = 0;
		} else {
			a = Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1));
		}

		if ((x == x2) && (y == y2)) {
			b = 0;
		} else {
			b = Math.sqrt((x - x2) * (x - x2) + (y - y2) * (y - y2));
		}

		if ((x1 == x2) && (y1 == y2)) {
			e = 0;
		} else {
			e = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		}

		if ((a * a + e * e) < b * b) {
			return a;
		} else if ((b * b + e * e) < a * a) {
			return b;
		} else if (e != 0) {
			c = (b * b - a * a - e * e) / (-2 * e);
			if (a * a - c * c < 0) {
				return 0;
			} else {
				return Math.sqrt(a * a - c * c);
			}
		} else {
			return a;
		}

	}

	/*****************************************************************************/
	// ThreadTarget implementation

	public void message(int msg) {
	}

	public void progress(double value) {
	}

	public void output(String msg) {
	}

	/*****************************************************************************/

} // End of class Editor
