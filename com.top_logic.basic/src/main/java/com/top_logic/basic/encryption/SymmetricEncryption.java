/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.PrefixedInputStream;
import com.top_logic.basic.io.StreamUtilities;

/**
 * Utility for symmetric encryption.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SymmetricEncryption {

	/**
	 * Mode and padding for encryption that makes easy predicting the cipher text size.
	 */
	private static final String MODE_AND_PADDING = "/OFB/NoPadding";

	private SecretKey _key;

	private final String _modeAndPadding;

	private SecureRandom _random;

	private final int _ivLength;

	/**
	 * Creates a {@link SymmetricEncryption}.
	 *
	 * @param random The source for random salt bytes.
	 * @param key The encryption key.
	 */
	public SymmetricEncryption(SecureRandom random, SecretKey key) {
		_key = key;
		_modeAndPadding = MODE_AND_PADDING;
		_random = random;

		_ivLength = getEncryptionCipher().getIV().length;
	}

	/**
	 * The exact plain text size of the given cipher text size.
	 */
	public long getPlainTextSize(long chipherTextSize) {
		return chipherTextSize - _ivLength;
	}

	/**
	 * The exact cipher text size (including encoded salt) for the given plain text size.
	 */
	public long getCipherTextSize(long plainTextSize) {
		// Since there is no padding, the cipher text size is equal to the plain text size.
		return plainTextSize + _ivLength;
	}

	/**
	 * Encrypts the given plain text.
	 */
	public InputStream encrypt(InputStream in) {
		Cipher cipher = getEncryptionCipher();
		byte[] iv = cipher.getIV();
		CipherInputStream encryptedStream = new CipherInputStream(in, cipher);
		PrefixedInputStream result = new PrefixedInputStream(iv, encryptedStream);
		return result;
	}

	/**
	 * Encrypts content of the given {@link OutputStream}.
	 */
	public OutputStream encrypt(OutputStream out) throws IOException {
		Cipher cipher = getEncryptionCipher();
		byte[] iv = cipher.getIV();
		out.write(iv);
		CipherOutputStream encryptedStream = new CipherOutputStream(out, cipher);
		return encryptedStream;
	}

	/**
	 * Encrypts the given plain text.
	 */
	public byte[] encrypt(byte[] plainText) {
		try {
			return readFully(encrypt(new ByteArrayInputStream(plainText)));
		} catch (IOException ex) {
			throw new UnreachableAssertion("Buffered operation", ex);
		}
	}

	private static byte[] readFully(InputStream in) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		StreamUtilities.copyStreamContents(in, buffer);
		return buffer.toByteArray();
	}

	private Cipher getEncryptionCipher() {
		try {
			Cipher cipher = Cipher.getInstance(_key.getAlgorithm() + _modeAndPadding);
			cipher.init(Cipher.ENCRYPT_MODE, _key, _random);
			return cipher;
		} catch (InvalidKeyException ex) {
			throw errorInvalidKey(ex);
		} catch (NoSuchAlgorithmException ex) {
			throw errorUnsupportedEncryptionAlgorithm(ex);
		} catch (NoSuchPaddingException ex) {
			throw errorUnsupportedPaddingAlgorithm(ex);
		}
	}

	/**
	 * Decrypts the given cipher text.
	 */
	public byte[] decrypt(byte[] encryptedText) {
		try {
			return readFully(decrypt(new ByteArrayInputStream(encryptedText)));
		} catch (EOFException ex) {
			throw new IllegalArgumentException("Text already decrypted?", ex);
		} catch (IOException ex) {
			throw new UnreachableAssertion("Buffered operation", ex);
		}
	}

	/**
	 * Decrypts the given cipher text.
	 */
	public InputStream decrypt(InputStream in) throws IOException {
		byte[] iv = new byte[_ivLength];
		int direct = StreamUtilities.readFully(in, iv);
		if (direct < _ivLength) {
			throw new EOFException("Stream size too small.");
		}
		Cipher cipher = getDecryption(iv);
		CipherInputStream result = new CipherInputStream(in, cipher);
		return result;
	}

	private Cipher getDecryption(byte[] iv) {
		try {
			Cipher cipher = Cipher.getInstance(_key.getAlgorithm() + _modeAndPadding);
			cipher.init(Cipher.DECRYPT_MODE, _key, new IvParameterSpec(iv), _random);
			return cipher;
		} catch (InvalidKeyException ex) {
			throw errorInvalidKey(ex);
		} catch (NoSuchAlgorithmException ex) {
			throw errorUnsupportedEncryptionAlgorithm(ex);
		} catch (NoSuchPaddingException ex) {
			throw errorUnsupportedPaddingAlgorithm(ex);
		} catch (InvalidAlgorithmParameterException ex) {
			throw errorInvalidAlgorithmParameters(ex);
		}
	}

	private AssertionError errorUnsupportedEncryptionAlgorithm(NoSuchAlgorithmException ex) {
		return error("Unsupported encryption algorithm.", ex);
	}

	private AssertionError errorUnsupportedPaddingAlgorithm(NoSuchPaddingException ex) {
		return error("Unsupported padding algorithm.", ex);
	}

	private AssertionError errorInvalidAlgorithmParameters(InvalidAlgorithmParameterException ex) {
		return error("Invalid algorithm parameters.", ex);
	}

	private AssertionError errorInvalidKey(InvalidKeyException ex) {
		return error("Invalid encryption key.", ex);
	}

	private AssertionError error(String message, Throwable cause) {
		return (AssertionError) new AssertionError(message).initCause(cause);
	}

}
