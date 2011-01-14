
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;

public class PngCommentOutputStream extends OutputStream implements Constants {

/*****************************************************************************/
//	CONSTANTS


/*****************************************************************************/
//	FIELDS

FileOutputStream os = null;
String comment = null;

boolean mustWriteComment = true;

/*****************************************************************************/

PngCommentOutputStream(File file, String comment) throws IOException
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
if(mustWriteComment)
	{
	// write png signature + iHDR chunk
	os.write(b,off,8+25);

	// write comment chunk
	writeCompressedComment();
	//writeComment();

	// write rest of buffer
	os.write(b,off+8+25,len-8-25);

	mustWriteComment = false;
	}
else
	{
	os.write(b,off,len);
	}
}

/*****************************************************************************/

void writeCompressedComment() throws IOException
{
byte b1[] = ("genome").getBytes();
int sep = 0;	// separator
int comp = 0;	// compression method
byte b2[] = deflate(comment);;

int len = b1.length + 2 + b2.length;

// write len
os.write((len>>24)&0xFF);
os.write((len>>16)&0xFF);
os.write((len>> 8)&0xFF);
os.write( len     &0xFF);

// write chunck type
os.write((int)'z');
os.write((int)'T');
os.write((int)'X');
os.write((int)'t');

// write data
os.write(b1);
os.write(sep);
os.write(comp);
os.write(b2);

// compute crc
CRC32 crc = new CRC32();
crc.update((int)'z');
crc.update((int)'T');
crc.update((int)'X');
crc.update((int)'t');
crc.update(b1);
crc.update(sep);
crc.update(comp);
crc.update(b2);
int value = (int)crc.getValue();

// write crc
os.write((value>>24)&0xFF);
os.write((value>>16)&0xFF);
os.write((value>> 8)&0xFF);
os.write( value     &0xFF);

}

/*****************************************************************************/

byte[] deflate(String s) throws IOException
{
ByteArrayOutputStream bos = new ByteArrayOutputStream();
DeflaterOutputStream dos = new DeflaterOutputStream(bos);
int n = s.length();
for(int i=0;i<n;i++)
	dos.write((int)s.charAt(i));
dos.close();
return bos.toByteArray();
}

/*****************************************************************************/

void writeComment() throws IOException
{
byte b[] = ("genome\000"+comment).getBytes();

int len = b.length;

// write len
os.write((len>>24)&0xFF);
os.write((len>>16)&0xFF);
os.write((len>> 8)&0xFF);
os.write( len     &0xFF);

// write chunck type
os.write((int)'t');
os.write((int)'E');
os.write((int)'X');
os.write((int)'t');

// write data
os.write(b);

// compute crc
CRC32 crc = new CRC32();
crc.update((int)'t');
crc.update((int)'E');
crc.update((int)'X');
crc.update((int)'t');
crc.update(b);
int value = (int)crc.getValue();

// write crc
os.write((value>>24)&0xFF);
os.write((value>>16)&0xFF);
os.write((value>> 8)&0xFF);
os.write( value     &0xFF);

}

/*****************************************************************************/

}	//	End of class	CommentOuputStream

