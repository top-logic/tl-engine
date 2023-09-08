/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Service to create and verify signatures.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public abstract class SignatureService extends ManagedClass {

    private static final byte[] NO_SIGNATURE = new byte[0];

	private static final Charset JVM_CHARTSET = Charset.defaultCharset();

	private final Charset _encoding;

	/**
	 * Configuration for {@link SignatureService}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends ServiceConfiguration<SignatureService> {
		/**
		 * Encoding for the signature.
		 */
		String getEncoding();
	}

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link SignatureService}.
	 */
	public SignatureService(InstantiationContext context, Config config) {
		String configuredEncoding = config.getEncoding();

		Charset encoding;
		if (StringServices.isEmpty(configuredEncoding)) {
			encoding = JVM_CHARTSET;
		} else{
			try {
				encoding = Charset.forName(configuredEncoding);
			} catch (RuntimeException ex) {
				context.error("Unable to get Charet for encoding " + configuredEncoding + ". Use " + JVM_CHARTSET.name()
					+ " instead.", ex);
				encoding = JVM_CHARTSET;
			}
		}
		_encoding = encoding;
	}

	/**
	 * Returns the crypted message. The message will be crypted using the "DSA" mechanism. The
	 * resulting message will be lead by a '#', if encryption succeeds or the given message, if
	 * fails.
	 * 
	 * @param aMessage
	 *        The message to be crypted (signed).
	 * @return The crypted (signed) message.
	 */
	public String sign(String aMessage) {
		try {
			return encode(createSignature(getBytes(aMessage)));
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Error ex) {
			throw ex;
		} catch (Exception ex) {
			throw error(ex);
		}
	}

	/**
	 * Returns the crypted message. The message will be crypted using the "DSA" mechanism. The
	 * resulting message will be lead by a '#', if encryption succeeds or the given message, if
	 * fails.
	 * 
	 * @param aMessage
	 *        The message to be crypted (signed).
	 * @return The crypted (signed) message.
	 */
	public String sign(char[] aMessage) {
		try {
			return encode(createSignature(getBytes(aMessage)));
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Error ex) {
			throw ex;
		} catch (Exception ex) {
			throw error(ex);
		}
	}

	private String encode(byte[] signature) {
		return '#' + StringServices.toHexString(signature);
	}

	/**
	 * Checks, whether the given message matches the given signature.
	 * 
	 * @param aMessage
	 *        The message to be checked.
	 * @param signature
	 *        The signature.
	 * @return Whether the message matches the signature.
	 */
	public boolean verify(String aMessage, String signature) {
		try {
			return verifySignature(getBytes(aMessage), decode(signature));
		} catch (Exception ex) {
			return false;
		}
    }

	/**
	 * Checks, whether the given message matches the given signature.
	 * 
	 * @param aMessage
	 *        The message to be checked.
	 * @param signature
	 *        The signature.
	 * @return Whether the message matches the signature.
	 */
	public boolean verify(char[] aMessage, String signature) {
		try {
			return verifySignature(getBytes(aMessage), decode(signature));
		} catch (Exception ex) {
			return false;
		}
	}

	private byte[] getBytes(char[] aMessage) {
		ByteBuffer buffer = _encoding.encode(CharBuffer.wrap(aMessage));
		byte[] result = buffer.array();
		if (buffer.limit() < result.length) {
			// Maybe not all slots in the array are actually used.
			result = Arrays.copyOfRange(result, 0, buffer.limit());
		}
		return result;
	}

	private byte[] getBytes(String aMessage) {
		return aMessage.getBytes(_encoding);
	}

	/**
	 * Computes, whether the given signature matches the given plain text.
	 * 
	 * @throws Exception
	 *         If the computation fails.
	 */
	protected abstract boolean verifySignature(byte[] plainText, byte[] signature) throws Exception;

	private byte[] decode(String encodedSignature) {
		if (!StringServices.startsWithChar(encodedSignature, '#')) {
			return NO_SIGNATURE;
		}
	
		try {
			return StringServices.hexStringToBytes(encodedSignature.substring(1));
		} catch (NumberFormatException ex) {
			return NO_SIGNATURE;
		}
	}

	/**
     * Generate a new key pair to be used for signing a message.
     *
     * @return    The fresh created key pair.
     */
    protected KeyPair createKeyPair () throws NoSuchAlgorithmException {
        KeyPairGenerator theGen    = KeyPairGenerator.getInstance ("DSA");
        SecureRandom     theRandom = SecureRandom.getInstance ("SHA1PRNG"); //, "SUN");

        theGen.initialize (1024, theRandom);

        return (theGen.generateKeyPair ());
    }

	/**
	 * Computes a signature of the given message.
	 * 
	 * @param plainText
	 *        The message to be signed.
	 * @return The signature.
	 * 
	 * @throws Exception
	 *         If the signature computation fails.
	 */
	protected abstract byte[] createSignature(byte[] plainText) throws Exception;

	private AssertionError error(Exception ex) {
		return (AssertionError) new AssertionError().initCause(ex);
	}

    /**
     * Returns the only instance of this class.
     *
     * @return    The only instance of this class.
     */
	public static SignatureService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
    }
    
	/**
	 * {@link BasicRuntimeModule} for the service {@link SignatureService}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<SignatureService> {

		/**
		 * Singleton {@link Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<SignatureService> getImplementation() {
			return SignatureService.class;
		}

	}
}

