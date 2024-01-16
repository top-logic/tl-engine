
/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.util;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Random;

/**
 * Diese Klasse generiert aus zwei Bits-stelligen Primzahlen
 * einen öffentlichen oder privaten Schlüssel für eine RSA-Codierung.
 */
class RSAKey implements Serializable, PrivateKey, PublicKey {

    /** serialVersionUID changed when adding equals() method */
    private static final long serialVersionUID = -4814010923288433781L;

    /** Stellen der Primzahlen in Bits. */
    public static int BITS = 340;

    /** Klassenintern Modulus und Schlüssel. */
    private BigInteger modulo     = new BigInteger("0");

    private BigInteger privateKey = new BigInteger("0");

    /**
     * Constructs the public key.
     *
     * @param    p    The first number.
     * @param    q    The second number.
     */
    public RSAKey(BigInteger p, BigInteger q) {
        BigInteger n = p.multiply (q);
        BigInteger e = new BigInteger (BITS, new Random());

        if (e.compareTo (n)!= (-1)) {
            e = new BigInteger (n.toString ());

			e = e.subtract(BigInteger.valueOf(1));
        }
        // Sicherstellen, daß e mit phi den ggT 1 hat.
        for(;!e.gcd (this.phi (p,q)).equals(BigInteger.valueOf (1));) {
            e = e.subtract(BigInteger.valueOf(1));
            // Sicherstellen, daß e größer 1 ist.
            if (e.compareTo(BigInteger.valueOf(1))!=(1)) {
                e = new BigInteger(n.toString());
				e = e.subtract(BigInteger.valueOf(1));
            }
        }

        this.modulo     = n;
        this.privateKey = e;
    }

    /** 
     * Standard equals() method, mostly for testing.
     */
    @Override
	public boolean equals(Object anObj) {
		if (anObj == this) {
			return true;
		}
        if (!(anObj instanceof RSAKey)) 
            return false;
        RSAKey other = (RSAKey) anObj;
        return this.modulo    .equals(other.modulo) 
            && this.privateKey.equals(other.privateKey);
    }

    /**
     * Constructs the private key.
     *
     * @param    aNumber1    The first number.
     * @param    aNumber2    The second number.
     * @param    aKey        The public key to generate private key from.
     */
    public RSAKey (BigInteger aNumber1, BigInteger aNumber2, RSAKey aKey) {
        BigInteger thePhi = this.phi (aNumber1, aNumber2);

        this.modulo     = aNumber1.multiply (aNumber2);
        this.privateKey = (aKey.getKey ()).modInverse (thePhi);
    }

    @Override
	public String getAlgorithm () {
        return ("RSA");
    }

    @Override
	public String getFormat () {
        return (null);
    }

    @Override
	public byte [] getEncoded () {
        return (null);
    }

    /**
     * Returns the private key.
     *
     * @return   The private key.
     */
    public BigInteger getKey () {
        return (this.privateKey);
    }

    /**
     * Returns the modulo value.
     *
     * @return    The modulo value.
     */
    public BigInteger getN () {
        return (this.modulo);
    }

    /**
     * Calculates phi out of the given values.
     *
     * @param    p    The first value.
     * @param    q    The second value.
     * @return   The value phi.
     */
    private BigInteger phi (BigInteger p, BigInteger q) {
        return (p.subtract (BigInteger.valueOf (1)).
                multiply (q.subtract (BigInteger.valueOf (1))));
    }
}

