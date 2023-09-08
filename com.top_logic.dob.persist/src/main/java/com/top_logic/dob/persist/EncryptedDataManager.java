/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.persist;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;

/**
 * Add (symetrical) Encryption to the BLOBDataManger.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class EncryptedDataManager extends BLOBDataManager {

    private static final String ALGORITHM_PROPERTY = "alg";

	/** The Alogrithm used to encrypt the data (e.g. "AES") */
    private String alg;

    /** KeySpecification based on the key given by setKey() */
    private SecretKeySpec keySpec;

	/**
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends AbstractDataManager.Config {
		/**
		 * Algorithm which is used to encrypt.
		 */
		String getAlgorithm();

		/**
		 * Key which is used to encrypt.
		 */
		String getKey();
	}

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link EncryptedDataManager}.
	 */
	public EncryptedDataManager(InstantiationContext context, Config config)
			throws SQLException {
		super(context, config);

    	initChipher(config);
	}

	private void initChipher(Config config) {
    	SecureRandom srand = new SecureRandom();
		alg = config.getAlgorithm();
    	if (alg == null) 
    		throw new IllegalArgumentException(
    				"The encryption algorithm must be specified: Missing property '" + ALGORITHM_PROPERTY + 
    		"'.");
    	Thread.yield();
		String key = config.getKey();
    	if (!StringServices.isEmpty(key)) { // testing only !
    		Logger.warn("Setting the encryption key via Properties is a potential security hole!",
    				this);
    		this.setKey(key);
    	}
    	Thread.yield();
    	rand = new Random(srand.nextLong());
    }
    
    /** Set the encryption key using a String */
	public void setKey(String key) {
        setKey(StringServices.hexStringToBytes(key));
    }

    /** Set the encryption key using bytes */
	public void setKey(byte[] key) {
        keySpec = new SecretKeySpec(key, alg);
    }

    /**
     * Encrypt the bytes here.
     */
    @Override
	protected OutputStream handleOutput(OutputStream os) {
        try {
            Cipher encoder = Cipher.getInstance(alg);
            encoder.init(Cipher.ENCRYPT_MODE, keySpec);
            
            return new CipherOutputStream(os, encoder);
        }
        catch (Exception gsx) {
            Logger.error("failed to handleOutput() for " + alg, gsx, this);
        }
        return null;
    }
    
    /**
     * Decrypt the bytes encrypted by code above.
     */
    @Override
	protected InputStream handleInput(InputStream is) {
        try {
            Cipher decoder = Cipher.getInstance(alg);
            decoder.init(Cipher.DECRYPT_MODE, keySpec);
            
            return new CipherInputStream(is, decoder);
        }
        catch (Exception ex) {
            Logger.error("failed to handleInput()", ex, this);
        }
        return null;
    }

    /** Example how to generate a KEY */
    public static void main(String args[]) throws Exception {

        // See ...jdk1.4.2/docs/guide/security/jce/JCERefGuide.html#AppA
        // Availeable Methods AES, Blowfish, DES, DESede,RC2,RC4,RC5, RSA
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128); // 128 ,192 and 256 bits should be be available

        SecretKey skey = kgen.generateKey();
        byte[]    raw  = skey.getEncoded();
        
        System.out.println(
            StringServices.toHexString(raw));
            
    }
}
