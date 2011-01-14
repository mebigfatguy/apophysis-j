
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class JpegCommentOutputStream extends OutputStream implements Constants {

/*****************************************************************************/
//	CONSTANTS

static final int STATE_BOF1 = 1;
static final int STATE_BOF2 = 2;
static final int STATE_MARKER1 = 11;
static final int STATE_MARKER2 = 12;
static final int STATE_LEN1 = 21;
static final int STATE_LEN2 = 22;
static final int STATE_DATA = 30;

/*****************************************************************************/
//	FIELDS

FileOutputStream os = null;
String comment = null;

int nextbyte = 0;
int state = STATE_BOF1;
int markerlen = 0;	// marker length
int marker = 0;
boolean mustWriteComment = false;
boolean commentWritten = false;

/*****************************************************************************/

JpegCommentOutputStream(File file, String comment) throws IOException
{
os = new FileOutputStream(file);
this.comment = comment;
}

/*****************************************************************************/

public void close() throws IOException
{
os.close();
}

public void flush() throws IOException
{
os.flush();
}

public void write(byte b[]) throws IOException
{
write(b,0,b.length);
}

public void write(int b) throws IOException 
{
os.write(b);
}

public void write(byte b[], int off, int len) throws IOException
{

// if rest of previous chunk still to be written
if(nextbyte>0)
	os.write(b,off,Math.min(nextbyte,len));

// check data
while(nextbyte<len)
	{
	int by = b[off+nextbyte];
	if(by<0) by += 256;

	switch(state)
		{
		case STATE_BOF1:	
			if(by!=0xFF) throw new IOException("Bad header");
			os.write(by);
			state = STATE_BOF2;
			nextbyte++;
			break;

		case STATE_BOF2:
			if(by!=0xD8) throw new IOException("Bad header");
			os.write(by);
			state = STATE_MARKER1;
			nextbyte++;
			break;

		case STATE_MARKER1:
			if(mustWriteComment&&!commentWritten)
				writeComment();
			os.write(by);
			if(by==0xFF)
				state = STATE_MARKER2;
			nextbyte++;
			break;

		case STATE_MARKER2:
			os.write(by);
			if(by!=0xFF)
				{
				marker = by;
				mustWriteComment = checkMarker(marker);
				state = STATE_LEN1;
				}
			nextbyte++;
			break;

		case STATE_LEN1:
			os.write(by);
			markerlen = by<<8;			
			state = STATE_LEN2;
			nextbyte++;
			break;

		case STATE_LEN2:
			os.write(by);
			markerlen |= by;
			state = STATE_MARKER1;
			nextbyte ++;
			if(nextbyte+markerlen-2<=len)
				os.write(b,nextbyte,markerlen-2);
			else
				os.write(b,nextbyte,len-nextbyte);
			nextbyte += markerlen-2;
			break;		
		}		
	}

nextbyte -= len;

//os.write(b,off,len);
}

/*****************************************************************************/

void writeComment() throws IOException
{
byte b[] = comment.getBytes();

os.write(0xFF);
os.write(M_COM);

int n = b.length + 2;
os.write((n>>8)&0xFF);
os.write(n&0xFF);

os.write(b);

commentWritten = true;
}

/*****************************************************************************/

boolean checkMarker(int marker)
{
if(marker==M_SOF0) return true;
if(marker==M_SOF1) return true;
if(marker==M_SOF2) return true;
if(marker==M_SOF3) return true;
if(marker==M_SOF5) return true;
if(marker==M_SOF6) return true;
if(marker==M_SOF7) return true;
if(marker==M_SOF9) return true;
if(marker==M_SOF10) return true;
if(marker==M_SOF11) return true;
if(marker==M_SOF13) return true;
if(marker==M_SOF14) return true;
if(marker==M_SOF15) return true;
if(marker==M_EOI) return true;
return false;
}

/*****************************************************************************/

}	//	End of class	CommentOuputStream

