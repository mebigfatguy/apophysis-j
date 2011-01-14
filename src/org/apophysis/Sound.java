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

import java.io.File;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

class Sound {

	Clip clip = null;

	/*****************************************************************************/

	Sound(File file) {

		try {
			URL url = file.toURL();
			AudioInputStream stream = AudioSystem.getAudioInputStream(url);
			AudioFormat format = stream.getFormat();

			DataLine.Info info = new DataLine.Info(Clip.class,
					stream.getFormat(), (int) stream.getFrameLength()
							* format.getFrameSize());

			clip = (Clip) AudioSystem.getLine(info);

			clip.open(stream);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	} // End of method openClip

	/*****************************************************************************/

	void play() {
		if (clip == null)
			return;

		try {
			clip.setFramePosition(0);
			clip.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/*****************************************************************************/

	void stop() {
		if (clip == null)
			return;

		try {
			clip.stop();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/*****************************************************************************/

	void close() {
		if (clip == null)
			return;

		try {
			clip.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/*****************************************************************************/

} // End of class Sound
