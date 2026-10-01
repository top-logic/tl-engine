
/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

import com.top_logic.basic.StringServices;

/**
 * Support class for en- and decoding a given message using the RSA
 * algorythm.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
class RSA implements MessageCoder {

    /** The name of the default user. */
    private static final String DEFAULT_USER = "top-logic";

    /** The key pair to be used in this instance. */
    private RSAKeyPair keyPair;

    /**
     * Encode the given message. If the given message is null, null will be
     * returned. The key will be taken from the defined key pair in this
     * instance.
     *
     * @param    aMessage      The message to be encoded.
     * @return   The encoded message.
     */
    @Override
	public String encodeString (String aMessage) {
        return (this.encodeString (this.getPublicKey (), aMessage));
    }

    /**
     * Decode the given message. If the given message is null, null will be
     * returned. The key will be taken from the defined key pair in this
     * instance.
     *
     * @param    aMessage      The message to be decoded.
     * @return   The decoded message.
     */
    @Override
	public String decodeString (String aMessage) {
        return (this.decodeString (this.getPrivateKey (), aMessage));
    }

    /**
     * Encode the given message using the given key. If the given message is
     * null, null will be returned.
     *
     * @param    aKey          The public key to be used for encoding.
     * @param    aMessage      The message to be encoded.
     * @return   The encoded message.
     */
    @Override
	public String encodeString (Object aKey, String aMessage) {
        return (this.toString (this.encode ((RSAKey) aKey, aMessage)));
    }

    /**
     * Decode the given message using the given key. If the given message is
     * null, null will be returned.
     *
     * @param    aKey          The private key to be used for decoding.
     * @param    aMessage      The message to be decoded.
     * @return   The decoded message.
     */
    @Override
	public String decodeString (Object aKey, String aMessage) {
        return (this.decode ((RSAKey) aKey, this.toBigIntegerArray (aMessage)));
    }

    /**
     * Encode the given message using the given public key.
     *
     * <p>
     * The UTF-8 bytes of the message are interpreted as an unsigned (big-endian) number. This
     * number is split into blocks of {@link #blockBits(RSAKey)} bits, starting with the least
     * significant bits. Each block is encrypted independently; the encrypted blocks are returned
     * in that order (least significant block first).
     * </p>
     *
     * <p>
     * A number has no leading zeros, therefore leading U+0000 characters of the message are not
     * preserved.
     * </p>
     *
     * @param    aKey          The public key to be used for encoding.
     * @param    aMessage      The message to be encoded.
     * @return   The encoded message.
     */
    public BigInteger [] encode (RSAKey aKey, String aMessage) {
        if (aMessage == null) {
            return (null);
        }

		byte[] bytes = aMessage.getBytes(StandardCharsets.UTF_8);
		int blockBits = blockBits(aKey);

		// Skip leading zero bytes, the number representation does not preserve them.
		int start = 0;
		while (start < bytes.length && bytes[start] == 0) {
			start++;
		}
		int valueBits = (bytes.length - start) * 8;
		if (valueBits > 0) {
			valueBits -= Integer.numberOfLeadingZeros(bytes[start] & 0xFF) - 24;
		}

		int blockCount = (valueBits + blockBits - 1) / blockBits;
		BigInteger[] result = new BigInteger[blockCount];
		for (int block = 0; block < blockCount; block++) {
			int lowBit = block * blockBits;
			int highBit = Math.min(lowBit + blockBits, valueBits);
			BigInteger chunk = bits(bytes, lowBit, highBit);
			result[block] = chunk.modPow(aKey.getKey(), aKey.getN());
		}
		return result;
    }

    /**
     * Decode the given message using the given private key.
     *
     * <p>
     * Inverse of {@link #encode(RSAKey, String)}: The decrypted blocks are concatenated (the first
     * block forming the least significant bits) and the resulting unsigned number is interpreted as
     * the UTF-8 bytes of the message.
     * </p>
     *
     * @param    aPrivateKey    The private key to be used for decoding.
     * @param    aCode          The encrypted blocks as created by {@link #encode(RSAKey, String)}.
     * @return   The decoded message.
     */
    public String decode (RSAKey aPrivateKey, BigInteger[] aCode) {
        if (aCode == null) {
            return (null);
        }

		int blockBits = blockBits(aPrivateKey);
		int totalBits = aCode.length * blockBits;
		byte[] bytes = new byte[(totalBits + 7) / 8];
		for (int block = 0; block < aCode.length; block++) {
			BigInteger chunk = aCode[block].modPow(aPrivateKey.getKey(), aPrivateKey.getN());
			orBits(bytes, chunk, block * blockBits);
		}

		int start = 0;
		while (start < bytes.length && bytes[start] == 0) {
			start++;
		}
		return new String(bytes, start, bytes.length - start, StandardCharsets.UTF_8);
    }

	/**
	 * The number of message bits encrypted in a single block with the given key.
	 *
	 * <p>
	 * The largest bit count not exceeding {@link RSAKey#BITS} - 20 for which every block value is
	 * smaller than the modulus of the key.
	 * </p>
	 */
	private static int blockBits(RSAKey aKey) {
		int result = RSAKey.BITS - 20;
		BigInteger n = aKey.getN();
		while (BigInteger.ONE.shiftLeft(result).compareTo(n) >= 0) {
			result--;
		}
		return result;
	}

	/**
	 * The bits in the range [lowBit, highBit) of the unsigned big-endian number in the given bytes.
	 *
	 * <p>
	 * Bit 0 is the least significant bit of the last byte.
	 * </p>
	 */
	private static BigInteger bits(byte[] bytes, int lowBit, int highBit) {
		int lowByte = lowBit / 8;
		int highByte = (highBit - 1) / 8;
		int last = bytes.length - 1;
		BigInteger slice = new BigInteger(1, bytes, last - highByte, highByte - lowByte + 1);
		int width = highBit - lowBit;
		return slice.shiftRight(lowBit % 8).and(BigInteger.ONE.shiftLeft(width).subtract(BigInteger.ONE));
	}

	/**
	 * Combines the given non-negative value shifted left by the given bit offset into the unsigned
	 * big-endian number in the given bytes.
	 *
	 * <p>
	 * Bits of the shifted value beyond the range of the given bytes are dropped.
	 * </p>
	 */
	private static void orBits(byte[] bytes, BigInteger value, int offset) {
		byte[] shifted = value.shiftLeft(offset % 8).toByteArray();
		int last = bytes.length - 1 - offset / 8;
		for (int n = shifted.length - 1, target = last; n >= 0 && target >= 0; n--, target--) {
			byte b = shifted[n];
			if (b != 0) {
				bytes[target] |= b;
			}
		}
	}

    /**
     * Returns the new created key pair to be used in this instance.
     *
     * @return    The key pair.
     */
    protected RSAKeyPair createKeyPair () {
        try {
            return (new RSAKeyPair (DEFAULT_USER));
        } 
        catch (Exception ex) {
            CryptLogger.error ("Unable to create key pair in RSA, reason is; " , 
                          ex, this);
        }

        return (null);
    }

    /**
     * Returns the public key from the key pair.
     *
     * @return    The public key.
     */
    private RSAKey getPublicKey () {
        return (this.getKeyPair ().getPublicKey ());
    }

    /**
     * Returns the private key from the key pair.
     *
     * @return    The private key.
     */
    private RSAKey getPrivateKey () {
        return (this.getKeyPair ().getPrivateKey ());
    }

    /**
     * Returns the key pair to be used in this instance.
     *
     * @return    The key pair.
     */
    private RSAKeyPair getKeyPair () {
        if (this.keyPair == null) {
            this.keyPair = this.createKeyPair ();
        }

        return (this.keyPair);
    }

    /**
     * Convert the given message to an array of BigIntegers. This is needed,
     * if the encoded message is given as string. The separator in the message
     * is a space. If the message is null, null will be returned, if it's 
     * empty, an empty array will be returned.
     *
     * @param    aMessage    The message to be converted.
     * @return   The array of BigIntegers in the message.
     */
    private BigInteger [] toBigIntegerArray (String aMessage) {
        if (aMessage == null) {
            return (null);
        }

        int             thePos    = 0;
        StringTokenizer theToken  = new StringTokenizer (aMessage, " ");
        BigInteger []   theResult = new BigInteger [theToken.countTokens ()];

        while (theToken.hasMoreTokens ()) {
            theResult [thePos++] = this.toBigInteger (theToken.nextToken ());
        }

        return (theResult);
    }

    /**
     * Convert the given array of BigIntegers to a message. This is needed,
     * to use the encoded message as string. The separator in the message
     * is a space. If the array is null, null will be returned, if it's 
     * empty, an empty string will be returned.
     *
     * @param    aMessage    The message to be converted.
     * @return   The array of BigIntegers in the message.
     */
    private String toString (BigInteger [] aMessage) {
        if (aMessage == null) {
            return (null);
        }
		int len = aMessage.length;
        StringBuffer theBuffer = new StringBuffer (len << 5);

		if (len > 0)
            theBuffer.append (this.toString (aMessage [0]));
        for (int thePos = 1; thePos < len; thePos++) {
            theBuffer.append (' ');
            theBuffer.append (this.toString (aMessage [thePos]));
        }

        return theBuffer.toString ();
    }

    /**
     * Convert a BigInteger to a string. This method uses the toString() method
     * of the BigInteger to get the result.
     *
     * @param    aNumber    The number to be converted.
     * @return   The string representation of the number.
     */
    private String toString (BigInteger aNumber) {
        return StringServices.toHexString (aNumber.toByteArray ());
    }

    /**
     * Convert a BigInteger to a string. This method uses the toString() method
     * of the BigInteger to get the result.
     *
     * @param    aString    The String to be converted.
     * @return   The string representation of the number.
     */
    private BigInteger toBigInteger (String aString) {
        return (new BigInteger (StringServices.hexStringToBytes (aString)));
    }
}

