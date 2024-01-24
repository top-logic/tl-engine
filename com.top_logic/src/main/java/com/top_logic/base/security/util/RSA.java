
/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
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
     * @param    aKey          The public key to be used for encoding.
     * @param    aMessage      The message to be encoded.
     * @return   The encoded message.
     */
    public BigInteger [] encode (RSAKey aKey, String aMessage) {
        if (aMessage == null) {
            return (null);
        }

        ArrayList  theCode   = new ArrayList ();
        int        theLength = RSAKey.BITS - 20;
        BigInteger theTemp   = new BigInteger (aMessage.getBytes ());
        BigInteger theBlock  = new BigInteger ("0");

        while (BigInteger.valueOf (2).
                          pow (theLength).
                          compareTo (aKey.getN ()) != -1) {
            theLength--;
        }

        while (!theTemp.equals (BigInteger.valueOf (0))) {
            for (int i = 1; i <= theLength; i++) {
                theBlock = theBlock.shiftLeft (1);

                if (theTemp.testBit (theLength - i))  {
                    theBlock = theBlock.setBit (0);
                }
            }

            theTemp = theTemp.shiftRight (theLength);

            theCode.add (theBlock.modPow (aKey.getKey (),aKey.getN()));
        }

        BigInteger[] theResult = new BigInteger [theCode.size ()];
        Iterator     theEnum   = theCode.iterator ();

        for (int thePos = 0; theEnum.hasNext (); thePos++) {
            theResult [thePos] = (BigInteger) theEnum.next ();
        }

        return (theResult);
    }

    /**
     * Decode the given message using the given private key.
     *
     * @param    aPrivateKey    The private key to be used for decoding.
     * @param    aCode          The message to be decoded (?)
     * @return   The decoded message.
     */
    public String decode (RSAKey aPrivateKey, BigInteger[] aCode) {
        if (aCode == null) {
            return (null);
        }

        int        theLength = RSAKey.BITS - 20;
        BigInteger theCode     = new BigInteger("0");

        while (BigInteger.valueOf (2).pow (theLength).
                                      compareTo (aPrivateKey.getN ()) != -1) {
            theLength--;
        }

        for (int thePos = 0; thePos < aCode.length; thePos++) {
            theCode=theCode.add (aCode [thePos].modPow (aPrivateKey.getKey (),
                                                        aPrivateKey.getN ()));
        }

        return (new String (theCode.toByteArray ()));
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

