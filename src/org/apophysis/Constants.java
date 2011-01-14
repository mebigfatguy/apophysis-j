
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

import java.awt.Color;

public interface Constants {

/****************************************************************************/

static final String VERSION = "2.7";
static final String APPNAME = "apophysis-j";

static final String DIRNAME = ".apophysis-j";
static final String CONFNAME = "settings";
static final String PRSTNAME = "render presets";
static final String FAVNAME = "favorites";
static final String PLUGNAME = "plugins";

/****************************************************************************/

static final int prefilter_white = 1024;
static final double eps = 1e-10;
static final int white_level = 200;
static final Color clyellow1 = new Color(0x17,0xFC,0xFF);
static final Color clplum2 = new Color(0xEC,0xA9,0xE6);
static final Color clslategray = new Color(0x83,0x73,0x65);

static final int MAX_FILTER_WIDTH = 25;

static final int crEditArrow  = 20;
static final int crEditMove   = 21;
static final int crEditRotate = 22;
static final int crEditScale  = 23;


/****************************************************************************/
// Bucket constants

static final int iRed = 0;
static final int iGreen = 1;
static final int iBlue = 2;
static final int iCount = 3;

/****************************************************************************/
// MainForm constants

public static final int PixelCountMax = 32768;

public static final int RS_A1 = 0;
public static final int RS_DR = 1;
public static final int RS_X0 = 2;
public static final int RS_V0 = 3;

/****************************************************************************/
// Mouse move states

public final static int msUsual = 0;
public final static int msZoomWindow = 1;
public final static int msZoomOutWindow = 2;
public final static int msZoomWindowMove = 3;
public final static int msZoomOutWindowMove = 4;
public final static int msDrag = 5;
public final static int msDragMove = 6;
public final static int msRotate = 7;
public final static int msRotateMove = 8;

/****************************************************************************/
// ControlPoint constants

public final static int NXFORMS = 100;
public final static int M1 = NXFORMS;  // M1 is -1 in delphi (ref triangle)

//public final static int SUB_BATCH_SIZE = 10000;
public final static int SUB_BATCH_SIZE = 2000;
public final static int PROP_TABLE_SIZE = 1024;
public final static int PREFILTER_WHITE = (1<<26);
public final static double FILTER_CUTOFF = 1.8;
public final static double BRIGHT_ADJUST = 2.3;
public final static int FUSE = 15;

/****************************************************************************/

public final static int RANDOMCMAP = -1;

/****************************************************************************/
// file types

public final static int ftIfs = 0;
public final static int ftFla = 1;
public final static int ftXML = 2;
public final static int ftUPR = 3;

/****************************************************************************/
// image file type

public static final int FT_BMP = 1;
public static final int FT_PNG = 2;
public static final int FT_JPG = 3;

/****************************************************************************/
// ScriptForm constants

public final static int NCPS = 10;

/****************************************************************************/
//	Thread constants

public final static int WM_THREAD_COMPLETE = 1;
public final static int WM_THREAD_TERMINATE = 2;

/****************************************************************************/
//	JPEG constants

public final static int M_SOF0  = 0xC0;
public final static int M_SOF1  = 0xC1;
public final static int M_SOF2  = 0xC2;
public final static int M_SOF3  = 0xC3;
public final static int M_SOF5  = 0xC5;
public final static int M_SOF6  = 0xC6;
public final static int M_SOF7  = 0xC7;
public final static int M_SOF9  = 0xC9;
public final static int M_SOF10 = 0xCA;
public final static int M_SOF11 = 0xCB;
public final static int M_SOF13 = 0xCD;
public final static int M_SOF14 = 0xCE;
public final static int M_SOF15 = 0xCF;

// Start Of Image (beginning of datastream)
public int M_SOI   = 0xD8;

// End Of Image (end of datastream)
public int M_EOI   = 0xD9;

// Start Of Scan (begins compressed data)
public int M_SOS   = 0xDA;

// Application-specific marker, type N
public int M_APP0  = 0xE0;
public int M_APP1  = 0xE1;
public int M_APP2  = 0xE2;
public int M_APP3  = 0xE3;
public int M_APP4  = 0xE4;
public int M_APP5  = 0xE5;
public int M_APP6  = 0xE6;
public int M_APP7  = 0xE7;
public int M_APP8  = 0xE8;
public int M_APP9  = 0xE9;
public int M_APP10 = 0xEA;
public int M_APP11 = 0xEB;
public int M_APP12 = 0xEC;
public int M_APP13 = 0xED;
public int M_APP14 = 0xEE;
public int M_APP15 = 0xEF;

// comment
public int M_COM   = 0xFE;

/* The maximal comment length */
public int M_MAX_COM_LENGTH = 65500;

/****************************************************************************/

}	//	End of interface	Constants
