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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.InflaterInputStream;

public class CommentExtractor implements Constants {

    /******************************************************************************/
    // CONSTANTS

    /******************************************************************************/
    // FIELDS

    /******************************************************************************/

    static String readJpegComment(String filename) {
        String comment = "";

        try (BufferedInputStream is = new BufferedInputStream(Files.newInputStream(Paths.get(filename)))) {
            // read signature
            int h1 = is.read();
            int h2 = is.read();

            if (h1 != 0xFF) {
                throw new IOException("Bad header");
            }
            if (h2 != 0xD8) {
                throw new IOException("Bad header");
            }

            // read chunks

            while (true) {
                // read ID
                int m1 = is.read();
                if (m1 < 0) {
                    break;
                }
                int m2 = is.read();
                if (m2 < 0) {
                    break;
                }

                // read length
                int l1 = is.read();
                if (l1 < 0) {
                    break;
                }
                int l2 = is.read();
                if (l2 < 0) {
                    break;
                }
                int len = (l1 << 8) | l2;

                if ((m1 == 0xFF) && (m2 == M_COM)) {
                    // read comment
                    byte[] b = new byte[len - 2];
                    int k = len - 2;
                    int o = 0;
                    while (k > 0) {
                        int n = is.read(b, o, k);
                        if (n < 0) {
                            break;
                        }
                        k -= n;
                        o += n;
                    }
                    comment = new String(b);
                    if (comment.indexOf("encryptedflame") >= 0) {
                        comment = decrypt(comment);
                    }
                    if (comment.indexOf("<flame") >= 0) {
                        break;
                    }
                } else if ((m1 == 0xFF) && (m2 == M_SOS)) {
                    break;
                } else {
                    long k = len - 2;
                    while (k > 0) {
                        long n = is.skip(k);
                        if (n < 0) {
                            break;
                        }
                        k -= n;
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return comment;

    } // End of method readJpgComment

    /******************************************************************************/

    static String readPngComment(String filename) {
        String comment = "";
        DataInputStream is = null;
        byte[] chunk = new byte[4];

        try {
            is = new DataInputStream(new BufferedInputStream(Files.newInputStream(Paths.get(filename))));

            // read signature
            is.readFully(chunk);
            is.readFully(chunk);

            // read chunks

            while (true) {
                // read length
                int l1 = is.read();
                int l2 = is.read();
                int l3 = is.read();
                int l4 = is.read();
                if (l4 < 0) {
                    break;
                }

                int len = (l1 << 24) | (l2 << 16) | (l3 << 8) | l4;

                is.readFully(chunk);
                String schunk = new String(chunk);

                if (schunk.equals("tEXt")) {
                    // uncompressed comment
                    byte[] b = new byte[len];
                    int k = len;
                    int o = 0;
                    while (k > 0) {
                        int n = is.read(b, o, k);
                        if (n <= 0) {
                            break;
                        }
                        k -= n;
                        o += n;
                    }
                    comment = new String(b);
                    if (comment.indexOf("encryptedflame") >= 0) {
                        comment = decrypt(comment);
                    }
                    if (comment.indexOf("<flame") >= 0) {
                        break;
                    }
                } else if (schunk.equals("zTXt")) {
                    // compressed comment
                    byte[] b = new byte[len];
                    int k = len;
                    int o = 0;
                    while (k > 0) {
                        int n = is.read(b, o, k);
                        if (n <= 0) {
                            break;
                        }
                        k -= n;
                        o += n;
                    }
                    comment = inflate(b);
                    if (comment.indexOf("encryptedflame") >= 0) {
                        comment = decrypt(comment);
                    }
                    if (comment.indexOf("<flame") >= 0) {
                        break;
                    }
                } else {
                    // skip data
                    long k = len;
                    while (k > 0) {
                        long n = is.skip(k);
                        if (n < 0) {
                            break;
                        }
                        k -= n;
                    }
                }

                // read crc
                is.readFully(chunk);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            if (is != null) {
                is.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return comment;

    } // End of method readJpgComment

    /******************************************************************************/

    static String inflate(byte b[]) throws IOException {
        // look for the 0 byte separator and compression method
        int off = 0;
        while (b[off] != 0) {
            off++;
        }
        off++;
        off++;
        int len = b.length - off;

        ByteArrayInputStream bis = new ByteArrayInputStream(b, off, len);
        InflaterInputStream is = new InflaterInputStream(bis);

        byte[] a = new byte[20 * b.length];
        int o = 0;
        int k = a.length;
        while (true) {
            int n = is.read(a, o, k);
            if (n <= 0) {
                break;
            }
            o += n;
            k -= n;
        }

        return new String(a, 0, o);
    }

    /******************************************************************************/

    static String decrypt(String comment) {
        String s = null;
        comment = comment.substring("encryptedflame:".length());

        byte[] d = null;

        try (ByteArrayInputStream bis = new ByteArrayInputStream(comment.getBytes()); BASE64DecoderStream b = new BASE64DecoderStream(bis)) {

            byte[] c = new byte[comment.length() * 2];
            int off = 0;
            int len = c.length;
            while (true) {
                int n = b.read(c, off, len);
                if (n <= 0) {
                    break;
                }
                off += n;
                len -= n;
            }

            d = new byte[off];
            System.arraycopy(c, 0, d, 0, off);
            c = null;

            // if password not null, try it
            if (Global.passwordText.length() > 0) {
                try {
                    if ((Global.crypto == null) || (!Global.crypto.password.equals(Global.passwordText))) {
                        Global.crypto = new Crypto(Global.passwordText);
                    }

                    byte[] e = Global.crypto.decode(d);
                    s = new String(e);
                } catch (Exception ex) {
                }
            }

            if (s != null) {
                return s;
            }

            return comment;

        } catch (Exception ex) {
            return comment;
        }

    } // End of method decrypt

    /******************************************************************************/

} // End of class CommentExtractor
