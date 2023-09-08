/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.encryption;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Service that offers a seeded {@link SecureRandom} implementation.
 * 
 * @see #getRandom()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SecureRandomService extends ConfiguredManagedClass<SecureRandomService.Config> {

	/**
	 * Configuration interface for {@link SecureRandomService}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface Config extends ConfiguredManagedClass.Config<SecureRandomService> {

		/**
		 * {@link SecureRandom} algorithm to use.
		 */
		String getAlgorithm();

	}

	private SecureRandom _random;

	/**
	 * Creates a {@link SecureRandomService} from configuration.
	 */
	public SecureRandomService(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		String algorithm = getConfig().getAlgorithm();
		try {
			_random = algorithm.isEmpty() ? new SecureRandom() : SecureRandom.getInstance(algorithm);
		} catch (NoSuchAlgorithmException ex) {
			throw new ConfigurationError("Invalid secure random algorithm specified.", ex);
		}
	}

	@Override
	protected void shutDown() {
		_random = null;
		super.shutDown();
	}

	/**
	 * The read-to-use random number generator.
	 */
	public SecureRandom getRandom() {
		return _random;
	}

	/**
	 * Generates a (secure) random string.
	 * 
	 * <p>
	 * 16 bytes are used to generate the string.
	 * </p>
	 * 
	 * @see #getRandomString(int)
	 */
	public String getRandomString() {
		return getRandomString(16);
	}

	/**
	 * Generates a (secure) random string.
	 * 
	 * <p>
	 * The string consists of groups of eight characters from <blockquote> {@code 0123456789abcdef}
	 * </blockquote> separated by '-'.
	 * </p>
	 * 
	 * @param size
	 *        The number of random bytes to produce the string.
	 */
	public String getRandomString(int size) {
		byte[] buffer = new byte[size];
		getRandom().nextBytes(buffer);
		StringBuilder builder = new StringBuilder(2 * size + (size - 1) / 4);
		for (int i = 0; i < size; i++) {
			if (i > 0 && i % 4 == 0) {
				builder.append('-');
			}
			String hexString = Integer.toHexString(Byte.toUnsignedInt(buffer[i]));
			if (hexString.length() < 2) {
				builder.append('0');
			}
			builder.append(hexString);
		}
		return builder.toString();
	}

	/**
	 * {@link SecureRandomService} singleton.
	 */
	public static SecureRandomService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module for {@link SecureRandomService}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static final class Module extends TypedRuntimeModule<SecureRandomService> {

		/**
		 * Singleton {@link SecureRandomService.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<SecureRandomService> getImplementation() {
			return SecureRandomService.class;
		}

	}

}
