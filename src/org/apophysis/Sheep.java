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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sheep extends Thread {

	/******************************************************************************/
	// CONSTANTS

	static final String eol = "\015\012";

	/******************************************************************************/
	// FIELDS

	/******************************************************************************/

	static void send(ControlPoint sourcecp) {
		// save palette format
		boolean format = Global.oldPaletteFormat;
		Global.oldPaletteFormat = true;

		doSend(sourcecp);

		// restore palette format
		Global.oldPaletteFormat = format;
	}

	/******************************************************************************/

	static void doSend(ControlPoint sourcecp) {
		if (Global.sheepNick.trim().length() == 0) {
			Global.main.alert("Nickname is not defined, check your options");
			return;
		}

		ControlPoint cp = new ControlPoint();
		cp.copy(sourcecp);

		// set fixed parameters

		cp.spatial_oversample = 2;
		cp.spatial_filter_radius = 1;
		cp.sample_density = 500;
		cp.nbatches = 50;
		cp.zoom = 0;
		cp.adjustScale(1280, 960);
		cp.hasFinalXform = false;

		List<Variation> vbad = new ArrayList<Variation>();

		int nv = XForm.getNrVariations();

		// check the number of variations used by each transform
		for (int i = 0; i < cp.nxforms; i++) {
			int kv = 0;
			for (int j = 0; j < nv; j++) {
				if (cp.xform[i].vars[j] != 0) {
					kv++;
				}
				if (kv > 5) {
					cp.xform[i].vars[j] = 0;
				}

				if (cp.xform[i].vars[j] != 0) {
					Variation variation = XForm.getVariation(j);
					if (!variation.isSheepCompatible()) {
						if (!vbad.contains(variation)) {
							vbad.add(variation);
						}
					}
				}
			}
		}

		int nbad = vbad.size();
		if (nbad > 0) {
			String msg = "The flame contains variations not compatible ";
			String sep = " : ";
			for (int i = 0; i < nbad; i++) {
				Variation variation = vbad.get(i);
				msg += sep + variation.getName();
				sep = ", ";
			}
			Global.main.alert(msg);
			return;
		}

		String boundary = "----------" + System.currentTimeMillis();

		String data = buildData(cp, boundary, Global.sheepNick,
				Global.sheepURL, Global.sheepPW);

		int id = -1;

		try {
			id = sendToServer(data, boundary, Global.sheepServer);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		if (id > 0) {
			Global.main.alert("Sheep succesfully posted to the server, id = "
					+ id);
		}

	} // End of method send

	/******************************************************************************/

	static String buildData(ControlPoint cp, String boundary, String nick,
			String url, String pw) {
		StringWriter sw = new StringWriter();
		PrintWriter w = new PrintWriter(sw);

		w.print("--");
		w.print(boundary);
		w.print(eol);
		w.print("Content-Disposition: form-data; name=\"type\"");
		w.print(eol);
		w.print(eol);
		w.print("upload");
		w.print(eol);

		w.print("--");
		w.print(boundary);
		w.print(eol);
		w.print("Content-Disposition: form-data; name=\"file\"; filename=\"a.flame\"");
		w.print(eol);
		w.print("Content-Type: text/plain");
		w.print(eol);
		w.print(eol);
		cp.save(w);
		w.print(eol);

		w.print("--");
		w.print(boundary);
		w.print(eol);
		w.print("Content-Disposition: form-data; name=\"nick\"");
		w.print(eol);
		w.print(eol);
		w.print(nick);
		w.print(eol);

		w.print("--");
		w.print(boundary);
		w.print(eol);
		w.print("Content-Disposition: form-data; name=\"url\"");
		w.print(eol);
		w.print(eol);
		w.print(url);
		w.print(eol);

		w.print("--");
		w.print(boundary);
		w.print(eol);
		w.print("Content-Disposition: form-data; name=\"pw\"");
		w.print(eol);
		w.print(eol);
		w.print(pw);
		w.print(eol);

		w.print("--");
		w.print(boundary);
		w.print(eol);
		w.print("Content-Disposition: form-data; name=\"ok\"");
		w.print(eol);
		w.print(eol);
		w.print("upload");
		w.print(eol);

		w.print("--");
		w.print(boundary);
		w.print("--");
		w.print(eol);

		w.flush();

		return sw.toString();

	} // End of method buildData

	/******************************************************************************/

	static int sendToServer(String data, String boundary, String server)
			throws IOException {
		int id = -1;

		int port = 80;
		int i = server.indexOf(':');
		if (i > 0) {
			port = Integer.parseInt(server.substring(i + 1));
			server = server.substring(0, i);
		}

		String cmd = "/v2d6/cgi/apophysis.cgi";

		Socket socket = new Socket(server, port);

		BufferedReader r = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		PrintWriter w = new PrintWriter(socket.getOutputStream());

		w.print("POST " + cmd + " HTTP/1.1");
		w.print(eol);

		w.print("Host: " + server + ":" + port);
		w.print(eol);

		w.print("Connection: close");
		w.print(eol);

		w.print("Accept: text/plain; q=0.8");
		w.print(eol);

		w.print("Content-type: multipart/form-data; boundary=" + boundary);
		w.print(eol);

		w.print("Content-Length: " + data.length());
		w.print(eol);
		w.print(eol);

		w.print(data);

		w.flush();

		// read response header
		String line = null;

		while (true) {
			line = r.readLine();
			if (line == null) {
				break;
			}

			line = line.trim();
			if (line.length() == 0) {
				break;
			}

			if (line.startsWith("Location:")) {
				i = line.indexOf("id=");
				if (i > 0) {
					id = Integer.parseInt(line.substring(i + 3).trim());
				}
			}
		}

		// read response data, should be two lines
		line = r.readLine();
		line = r.readLine();

		if (line != null) {
			line = line.trim();
			if (line.length() > 0) {
				// error message
				Global.main.alert(line);
			}
		}

		socket.close();

		return id;

	} // End of method sendToServer

	/******************************************************************************/

	static void getSheepVariations() {
		Map<String, Boolean> h = new HashMap<String, Boolean>();

		try {

			String cookies = "";
			String sep = "";
			URL url = null;
			URLConnection con = null;

			String loc = "http://electricsheep.wikispaces.com/ReadMe";
			while (true) {
				url = new URL(loc);
				con = url.openConnection();

				if (cookies.length() > 0) {
					con.setRequestProperty("Cookie", cookies);
				}
				con.connect();

				cookies = "";
				sep = "";
				// get cookies
				for (int i = 1; i < 999; i++) {
					String header = con.getHeaderFieldKey(i);
					if (header == null) {
						break;
					}
					if (header.equals("Set-Cookie")) {
						String cookie = con.getHeaderField(i);
						int j = cookie.indexOf(';');
						if (j > 0) {
							cookie = cookie.substring(0, j);
						}
						cookies += sep + cookie;
						sep = ";";
					}
				}

				// System.out.println("cookies1 : "+cookies);
				loc = con.getHeaderField("Location");
				// System.out.println("loc1 : "+loc);

				if (loc == null) {
					break;
				}
				if (loc.length() == 0) {
					break;
				}
			}

			BufferedReader r = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			while (true) {
				String line = r.readLine();
				if (line == null) {
					break;
				}
				int i = line.indexOf("The complete list of");
				if (i < 0) {
					continue;
				}
				int j = line.indexOf("variations", i);
				if (j < 0) {
					continue;
				}
				break;
			}

			while (true) {
				String line = r.readLine();
				if (line == null) {
					break;
				}
				int i = line.indexOf("<ul>");
				if (i >= 0) {
					break;
				}
			}

			while (true) {
				String line = r.readLine();
				if (line == null) {
					break;
				}
				int i = line.indexOf("</ul>");
				if (i >= 0) {
					break;
				}

				int j = line.indexOf("<li>");
				if (j < 0) {
					continue;
				}
				int k = line.indexOf("</li>", j);
				if (k < 0) {
					continue;
				}

				String vname = line.substring(j + 4, k);
				if (vname.equals("gaussian")) {
					vname = "gaussian_blur";
				}

				h.put(vname, Boolean.TRUE);
			}

			r.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		System.out.println(h.size() + " sheep variations found");

		// update the compatibility table

		for (int i = 0; i < XForm.getNrVariations(); i++) {
			if (h.get(XForm.getVariation(i).getName()) != null) {
				XForm.sheep[i] = true;
			}
		}

		// unknown variations
		for (String key : h.keySet()) {
			if (XForm.getVariationIndex(key) < 0) {
				System.out.println("unknown sheep variation " + key);
			}
		}

	} // End of method getSheepVariations

	/*****************************************************************************/

	@Override
	public void run() {
		getSheepVariations();
	}

	/*****************************************************************************/

} // End of class Sheep
