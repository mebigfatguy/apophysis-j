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

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class Crypto {

	/*****************************************************************************/
	// CONSTANTS

	static byte[] defaultSalt = { (byte) 0xD2, (byte) 0x36, (byte) 0x51,
			(byte) 0x19, (byte) 0x77, (byte) 0x0C, (byte) 0x24, (byte) 0x2A, };

	/*****************************************************************************/
	// FIELDS

	private String algorithm;
	private final byte[] salt;
	private final int iterationCount;
	private final Cipher cipher;
	private final SecretKey key;
	private final PBEParameterSpec pbeParamSpec;

	public String password;

	/*****************************************************************************/
	// CONSTRUCTORS

	public Crypto(String password) throws GeneralSecurityException {
		this("PBEWithMD5AndDES", defaultSalt, 17, password.toCharArray());
		this.password = password;
	}

	public Crypto(String algorithm, byte[] salt, int iterationCount,
			char[] password) throws GeneralSecurityException {
		this.salt = salt;
		this.iterationCount = iterationCount;
		cipher = Cipher.getInstance(algorithm);
		pbeParamSpec = new PBEParameterSpec(salt, iterationCount);

		SecretKeyFactory keyFac = SecretKeyFactory.getInstance(algorithm);
		PBEKeySpec keySpec = new PBEKeySpec(password);
		key = keyFac.generateSecret(keySpec);
		// keySpec.clearPassword();
	}

	/*****************************************************************************/

	public byte[] decode(byte[] encrypted) {
		try {
			cipher.init(Cipher.DECRYPT_MODE, key, pbeParamSpec);
			return cipher.doFinal(encrypted);
		} catch (GeneralSecurityException ex) {
			throw new RuntimeException(ex);
		}

	} // End of method decode

	/*****************************************************************************/

	public byte[] encode(byte[] plainSource) {
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key, pbeParamSpec);
			return cipher.doFinal(plainSource);
		} catch (GeneralSecurityException ex) {
			throw new RuntimeException(ex);
		}

	} // End of method encode

	/*****************************************************************************/

} // End of class Crypto
