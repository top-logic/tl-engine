/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.encryption;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.SecretKey;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.ThreadContextManager;

/**
 * Service that provides a symmetric key for encrypting application data.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ServiceDependencies({
	ThreadContextManager.Module.class
})
public class EncryptionService extends ManagedClass {

	/**
	 * Option to configure the message digest algorithm to produce signatures with.
	 */
	public static final String SIGNATURE_DIGEST_ALGORITHM_PROPERTY = "signatureDigestAlgorithm";

	/**
	 * The {@link SecureRandom} generator for this service.
	 */
	private final SecureRandom _random;

	private SecretKey _encryptionKey;

	private SymmetricEncryption _encryption;

	private String _signatureDigestAlgorithm;

	/**
	 * Configuration for {@link EncryptionService}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends ServiceConfiguration<EncryptionService> {
		/**
		 * The digest algorithm to produce (symmetric) signatures with.
		 */
		String getSignatureDigestAlgorithm();
	}

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link EncryptionService}.
	 */
	public EncryptionService(InstantiationContext context, Config config) {
		super(context, config);
		
		_random = new SecureRandom();

		_signatureDigestAlgorithm = config.getSignatureDigestAlgorithm();

		try {
			internalCreateSignatureDigest();
		} catch (NoSuchAlgorithmException ex) {
			try {
				throw new ConfigurationException("Unsupported signature digest algorithm.", ex);
			} catch (ConfigurationException ex1) {
				throw new RuntimeException(ex1);
			}
		}
	}

	/**
	 * The {@link SecureRandom} number generator used for encryption.
	 */
	protected SecureRandom getRandom() {
		return _random;
	}

	@Override
	protected void shutDown() {
		clearEncryptionKey();
		super.shutDown();
	}

	private void clearEncryptionKey() {
		_encryptionKey = null;
		setEncryption(null);
	}

	/**
	 * The symmetric key for data encryption/decryption, or <code>null</code>, if the encryption key
	 * has not yet been initialized.
	 * 
	 * @see #getEncryption()
	 */
	public SecretKey getEncryptionKey() {
		return _encryptionKey;
	}
	
	/**
	 * The {@link SymmetricEncryption} utility for encrypting/decrypting data.
	 * 
	 * @throws IllegalStateException
	 *         If the encryption key has not yet been initialized.
	 */
	public SymmetricEncryption getEncryption() throws IllegalStateException {
		if (_encryption == null) {
			throw errorNotInitialized();
		}
		return _encryption;
	}

	private IllegalStateException errorNotInitialized() {
		return new IllegalStateException("Encryption key not initialized.");
	}
	
	private void setEncryption(SymmetricEncryption _encryption) {
		this._encryption = _encryption;
	}

	/**
	 * Creates a signature (encrypted hash) of the given plain text.
	 * 
	 * @throws IllegalStateException
	 *         If the encryption key has not yet been initialized.
	 * 
	 * @see #checkSignature(byte[], byte[])
	 */
	public byte[] createSignature(byte[] plainText) throws IllegalStateException {
		MessageDigest digest = createSignatureDigest();
		byte[] hash = digest.digest(plainText);
		return getEncryption().encrypt(hash);
	}

	/**
	 * Checks the given signature.
	 * 
	 * @param plainText
	 *        The signed plain text.
	 * @param signature
	 *        The signature produced with {@link #createSignature(byte[])}.
	 * @return Whether the signature is valid.
	 * 
	 * @throws IllegalStateException
	 *         If the encryption key has not yet been initialized.
	 * 
	 * @see #createSignature(byte[])
	 */
	public boolean checkSignature(byte[] plainText, byte[] signature) throws IllegalStateException {
		MessageDigest digest = createSignatureDigest();
		byte[] hash = digest.digest(plainText);
		return Arrays.equals(hash, getEncryption().decrypt(signature));
	}
	
	private MessageDigest createSignatureDigest() {
		try {
			return internalCreateSignatureDigest();
		} catch (NoSuchAlgorithmException ex) {
			throw new UnreachableAssertion("Existance checked in constructor.", ex);
		}
	}

	private MessageDigest internalCreateSignatureDigest() throws NoSuchAlgorithmException {
		return MessageDigest.getInstance(_signatureDigestAlgorithm);
	}

	protected void installEncryptionKey(SecretKey encryptionKey) {
		assert encryptionKey != null;

		_encryptionKey = encryptionKey;
		setEncryption(new SymmetricEncryption(getRandom(), encryptionKey));
	}

	/**
	 * The {@link EncryptionService} instance.
	 */
	public static EncryptionService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Shuts down the {@link EncryptionService}.
	 */
	public static void stop() {
		ModuleUtil.INSTANCE.shutDown(Module.INSTANCE);
	}

	/**
	 * {@link BasicRuntimeModule} for {@link EncryptionService}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static final class Module extends TypedRuntimeModule<EncryptionService> {
	
		/**
		 * Singleton {@link EncryptionService.Module} instance.
		 */
		public static final EncryptionService.Module INSTANCE = new EncryptionService.Module();
	
		@Override
		public Class<EncryptionService> getImplementation() {
			return EncryptionService.class;
		}
		
	}
}
