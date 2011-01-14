/* Thinlet GUI toolkit - www.thinlet.com
 * Copyright (C) 2002-2005 Robert Bajzat (rbajzat@freemail.hu) */
package com.thinlet;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Label;

/**
 * <code>AppletLauncher</code> is a double buffered applet to launch any
 * <i>thinlet</i> component
 */
public class AppletLauncher extends Applet implements Runnable {

	private transient Thinlet content;
	private transient Image doublebuffer;

	/**
	 * Applet instance is created by the browser or applet viewer
	 */
	public AppletLauncher() {
		super(); // for javadoc
	}

	/**
	 * Called by the browser to inform this applet that it has been loaded into
	 * the system, it displays the <i>Loading...</i> label and starts the loader
	 * thread
	 */
	@Override
	public void init() {
		setBackground(Color.white);
		setForeground(Color.darkGray);
		setLayout(new BorderLayout());
		add(new Label("Loading...", Label.CENTER), BorderLayout.CENTER);
		new Thread(this).start();
	}

	/**
	 * Create a new <i>thinlet</i> instance of the class given as
	 * <code>class</code> applet parameter, and show it or the message of the
	 * thrown exception. First try a contructor with an applet parameter (thus
	 * you get this applet instance e.g. for the parameters of the applet HTML
	 * tag), then the empty constructor
	 */
	public void run() {
		try {
			Class thinletclass = Class.forName(getParameter("class"));
			try {
				content = (Thinlet) thinletclass.getConstructor(
						new Class[] { Applet.class }).newInstance(
						new Object[] { this });
			} catch (NoSuchMethodException nsme) {
				content = (Thinlet) thinletclass.newInstance();
			}
			removeAll();
			add(content, BorderLayout.CENTER);
		} catch (Throwable exc) {
			removeAll();
			add(new Label(exc.getClass().getName() + " " + exc.getMessage(),
					Label.CENTER), BorderLayout.CENTER);
		}
		doLayout();
		repaint();
	}

	/**
	 * Clear the double buffer image, the overriden method lays out its
	 * components (centers the <i>thinlet</i> component)
	 */
	@Override
	public void doLayout() {
		super.doLayout();
		if (doublebuffer != null) {
			doublebuffer.flush();
			doublebuffer = null;
		}
	}

	/**
	 * Called by the browser to inform this applet that it should stop its
	 * execution, it clears the double buffer image
	 */
	@Override
	public void stop() {
		if (doublebuffer != null) {
			doublebuffer.flush();
			doublebuffer = null;
		}
	}

	/**
	 * Call the paint method to redraw this component without painting a
	 * background rectangle
	 */
	@Override
	public void update(Graphics g) {
		paint(g);
	}

	/**
	 * Create a double buffer if needed, the <i>thinlet</i> component paints the
	 * content
	 */
	@Override
	public void paint(Graphics g) {
		if (doublebuffer == null) {
			Dimension d = getSize();
			doublebuffer = createImage(d.width, d.height);
		}
		Graphics dg = doublebuffer.getGraphics();
		dg.setClip(g.getClipBounds());
		super.paint(dg);
		dg.dispose();
		g.drawImage(doublebuffer, 0, 0, this);
	}

	/**
	 * Called by the browser to inform this applet that it is being reclaimed,
	 * it calls the <i>thinlet</i> component's <code>destroy</code> method (its
	 * return value is irrelevant)
	 */
	@Override
	public void destroy() {
		content.destroy();
	}
}
