/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.util;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;

/**
 * {@link SignatureService} based on a well-known public/private key pair.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class DefaultSignatureService extends SignatureService {

	/** The name for the signature file. */
	private static final String DEFAULT_SIGNATURE = "signature";

	/** The used key pair. */
	private KeyPair keyPair;

	/**
	 * Creates a {@link DefaultSignatureService}.
	 */
	@CalledByReflection
	public DefaultSignatureService(InstantiationContext context, Config config)
			throws NoSuchAlgorithmException, IOException {
		super(context, config);
		initKeys();
	}

	@Override
	protected byte[] createSignature(byte[] plainText) throws InvalidKeyException, NoSuchAlgorithmException,
			IOException, SignatureException {

		Signature signer = getSigner();
		signer.update(plainText);
		return signer.sign();
	}

	private Signature getSigner() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
		Signature theSign = Signature.getInstance("DSS");
		theSign.initSign(this.getPrivateKey());
		return theSign;
	}

	@Override
	protected boolean verifySignature(byte[] plainText, byte[] signature) throws InvalidKeyException,
			NoSuchAlgorithmException, IOException, SignatureException {

		Signature verifier = getVerifier();
		verifier.update(plainText);
		return (verifier.verify(signature));
	}

	private Signature getVerifier() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
		Signature theSign = Signature.getInstance("DSS");
		theSign.initVerify(this.getPublicKey());
		return theSign;
	}

	/**
	 * Tries to get public and private key for the given user.
	 * 
	 * First this method tries to load the keys from the harddisc. If this fails, a new pair of keys
	 * will be generated for the user and this keys will be stored, so the system can use them next
	 * time it starts.
	 */
	private void initKeys() throws NoSuchAlgorithmException, IOException {
		if (!this.loadKeys(DEFAULT_SIGNATURE)) {
			this.keyPair = this.createKeyPair();
			CryptLogger.debug("Created new key pair for user \"" + DEFAULT_SIGNATURE + "\"!", CryptSupport.class);

			KeyStore.saveKeyPair(DEFAULT_SIGNATURE, this.keyPair);
		}
	}

	/**
	 * Returns the private key to be used for signing.
	 * 
	 * @return The requested private key.
	 */
	private PrivateKey getPrivateKey() throws NoSuchAlgorithmException, IOException {
		return this.getKeyPair().getPrivate();
	}

	/**
	 * Returns the public key to be used for signing.
	 * 
	 * @return The requested public key.
	 */
	private PublicKey getPublicKey() throws NoSuchAlgorithmException, IOException {
		return (this.getKeyPair().getPublic());
	}

	/**
	 * Returns the key pair to be used for signing. This key pair contains the private and the
	 * public key.
	 * 
	 * @return The requested key pair.
	 */
	private KeyPair getKeyPair() throws NoSuchAlgorithmException, IOException {
		if (this.keyPair == null) {
			this.initKeys();
		}

		return (this.keyPair);
	}

	/**
	 * Load the keys for the given user from the harddisc. Both keys have to be on the disc,
	 * otherwise the method returns false.
	 * 
	 * @param anUserID
	 *        The ID of the user, whose keys should be loaded.
	 * @return true, if both keys have been loaded.
	 */
	private boolean loadKeys(String anUserID) {
		try {
			this.keyPair = KeyStore.loadKeyPair(anUserID);
		} catch (Exception ex) {
			CryptLogger.error("Unable to load keys for user \"" + anUserID +
							"\", reason is: " + ex, this);
		}

		return (this.keyPair != null);
	}

}
