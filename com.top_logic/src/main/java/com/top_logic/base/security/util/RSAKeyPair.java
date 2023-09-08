
/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.util;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;

/**
 * Manager for the matching key pairs of a user. The keys are RSA keys. First,
 * this manager tries to load the keys from the harddisc. If this fails, new
 * keys will be generated and stored.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class RSAKeyPair {

    /** The held key pair. */
    private KeyPair keyPair;

    /** The held ID of the user. */
	private final String userID;

    /**
     * This constructor generates the key pair for the given user. It checks,
     * whether there are serialized versions of the keys on the harddisc or 
     * not. If not, a new key pair will be generated and stored to the harddisc.
     *
     * @param    anUserID    The ID of the user.
     * @throws   Exception   If the storing of new generated keys fails.
     */
    public RSAKeyPair (String anUserID) throws Exception {
        if (anUserID == null) {
            throw new IllegalArgumentException ("The user ID cannot be empty!");
        }

        this.initKeys (anUserID);

        this.userID = anUserID;
    }

    /**
     * Constructor for existing public and private keys. Note that no
     * parameter is null, otherwise an IllegalArgumentException will be
     * thrown.
     *
     * @param    anUserID       The ID of the user.
     * @param    aPublicKey     The public key to be held.
     * @param    aPrivateKey    The private key to be held.
     * @throws   IllegalArgumentException    If one parameter is null.
     */
    /*
      
    Never used since RSAKey is a pacakge private class
     
    public RSAKeyPair (String anUserID, RSAKey aPublicKey, RSAKey aPrivateKey) 
                                            throws IllegalArgumentException {
        if (anUserID == null) {
            throw new IllegalArgumentException ("User ID cannot be empty!");
        }

        if (aPublicKey == null) {
            throw new IllegalArgumentException ("Public key cannot be empty!");
        }

        if (aPrivateKey == null) {
            throw new IllegalArgumentException ("Private key cannot be empty!");
        }

        this.keyPair = new KeyPair (aPublicKey, aPrivateKey);
        this.userID  = anUserID;
    }
    */
    
    /** 
     * Standard equals method (mostly for testing)
     */
    @Override
	public boolean equals(Object anObj) {
		if (anObj == this) {
			return true;
		}
        if (!(anObj instanceof RSAKeyPair)) 
            return false;
        RSAKeyPair other = (RSAKeyPair) anObj;
        return this.getPrivateKey().equals(other.getPrivateKey()) 
            && this.getPublicKey() .equals(other.getPublicKey())
            && this.userID         .equals(other.userID);
    }

	@Override
    public int hashCode() {
		final int prime = 91957;
		int result = 156874;
		result = prime * result + getPublicKey().hashCode();
		result = prime * result + getPrivateKey().hashCode();
		result = prime * result + userID.hashCode();
		return result;
    }

    /**
     * Returns the ID of the user of this key pair. This value cannot be null!
     *
     * @return    The user ID.
     */
    public String getUserID () {
        return (this.userID);
    }

    /**
     * Returns the public key of this key pair. This value cannot be null!
     *
     * @return    The public key.
     */
    public RSAKey getPublicKey () {
        return ((RSAKey) this.keyPair.getPublic ());
    }

    /**
     * Returns the private key of this key pair. This value cannot be null!
     *
     * @return    The private key.
     */
    public RSAKey getPrivateKey () {
        return ((RSAKey) this.keyPair.getPrivate ());
    }

    /**
     * Triesd to get public and private key for the given user. First this
     * method tries to load the keys from the harddisc. If this fails, a new
     * pair of keys will be generated for the user and this keys will be
     * stored, so the system can use them next time it starts.
     *
     * @param    anUserID    The name of the user.
     * @throws   Exception   If something fails while init keys.
     */
    private  void initKeys (String anUserID) throws Exception {
        synchronized (RSAKeyPair.class) { // Avoid lost upate on key file
            if (!this.loadKeys (anUserID)) {
                this.generateKeys (anUserID);
            }
        }
    }

    /**
     * Load the keys for the given user from the harddisc. Both keys have
     * to be on the disc, otherwise the method returns false.
     *
     * @param    anUserID    The ID of the user, whose keys should be loaded.
     * @return   true, if both keys have been loaded.
     */
    private boolean loadKeys (String anUserID) {
        try {
            this.keyPair = KeyStore.loadKeyPair (anUserID);
        } 
        catch (Exception ex) {
            CryptLogger.info ("Unable to load keys for user \"" + anUserID +
                         "\", reason is: " + ex, this);
        }

        return (this.keyPair != null);
    }

    /**
     * Generates the keys and store this keys for the given user.
     *
     * @param    anUserID    The user ID for the keys.
     * @throws   Exception   If something fails while generating keys or 
     *                       storing them.
     */
    private void generateKeys (String anUserID) throws Exception {
        CryptLogger.info ("Generating new key pair for user \"" + anUserID + "\"!", this);
        SecureRandom     rand = new SecureRandom();
        BigInteger p          = new BigInteger (RSAKey.BITS, 25, rand);
        BigInteger q          = new BigInteger (RSAKey.BITS, 25, rand);
        RSAKey     thePublic  = new RSAKey (p, q);
        RSAKey     thePrivate = new RSAKey (p, q, thePublic);
        this.keyPair = new KeyPair (thePublic,thePrivate);

        KeyStore.saveKeyPair (anUserID, this.keyPair);

        CryptLogger.info ("Stored new key pair for user \"" + anUserID +
                     "\"!", this);
    }

}

