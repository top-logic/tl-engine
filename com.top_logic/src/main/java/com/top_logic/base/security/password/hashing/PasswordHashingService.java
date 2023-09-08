/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.password.hashing;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.user.UserService;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;

/**
 * Service to create and verify a password hash using configured {@link PasswordHashingAlgorithm}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PasswordHashingService extends AbstractConfiguredInstance<PasswordHashingService.Config> {

	private static final char HASHING_ALGORITHM_SEPARATOR = '#';

	/**
	 * Configuration of the {@link PasswordHashingService}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<PasswordHashingService> {

		/**
		 * Known {@link PasswordHashingAlgorithm}.
		 */
		@DefaultContainer
		@Key(PasswordHashingAlgorithmConfig.NAME_ATTRIBUTE)
		Map<String, PasswordHashingAlgorithmConfig> getAlgorithms();

		/**
		 * Name of the default hashing algorithm.
		 */
		@Mandatory
		String getDefaultAlgorithm();

		/**
		 * Whether hash values which are not created with the {@link #getDefaultAlgorithm() default
		 * algorithm} must be converted automatically.
		 */
		boolean getAutomaticRehash();

	}

	/**
	 * Named configuration of {@link PasswordHashingAlgorithm}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface PasswordHashingAlgorithmConfig extends NamedConfigMandatory {

		/**
		 * The actual {@link PasswordHashingAlgorithm}.
		 */
		@Mandatory
		PolymorphicConfiguration<PasswordHashingAlgorithm> getImplementation();

	}

	private Map<String, PasswordHashingAlgorithm> _algorithms;

	private String _defaultAlgorithm;

	/**
	 * Create a {@link PasswordHashingService}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public PasswordHashingService(InstantiationContext context, Config config) {
		super(context, config);
		_algorithms = new HashMap<>();
		for (Entry<String, PasswordHashingAlgorithmConfig> algorithm : config.getAlgorithms().entrySet()) {
			String key = algorithm.getKey();
			if (key.indexOf(HASHING_ALGORITHM_SEPARATOR) >= 0) {
				context.error("Hashing algorithm name must not contain '" + HASHING_ALGORITHM_SEPARATOR + "'");
				continue;
			}
			_algorithms.put(key, context.getInstance(algorithm.getValue().getImplementation()));
		}
		_defaultAlgorithm = config.getDefaultAlgorithm();
		if (!_algorithms.containsKey(_defaultAlgorithm)) {
			context.error("No such algorithm: " + _defaultAlgorithm);
		}

	}

	/**
	 * Retrieves the sole application instance.
	 */
	public static PasswordHashingService getInstance() {
		return Login.getInstance().getPasswordHashingService();
	}

	/**
	 * Creates a new hash value for the given password.
	 * 
	 * @param password
	 *        The password to create hash for.
	 * 
	 * @return A hash value that can later be used for verification.
	 * 
	 * @see #verify(char[], String)
	 */
	public String createHash(char[] password) {
		PasswordHashingAlgorithm algorithm = _algorithms.get(_defaultAlgorithm);
		return _defaultAlgorithm + HASHING_ALGORITHM_SEPARATOR + algorithm.createHash(password);
	}

	/**
	 * Verifies the given password against the given hash value.
	 * 
	 * @param password
	 *        The password to check.
	 * @param hash
	 *        The expected hash of the given password.
	 * @return {@link VerificationResult} that can be used to check whether the given password could
	 *         be validated against the given hash value.
	 */
	public VerificationResult verify(char[] password, String hash) {
		if (UserService.INITIAL_PWD_HASH_PLACEHOLDER.equals(hash)) {
			/* do not try to parse the initial placeholder value. instead always return false as
			 * long as no explicit password has been set */
			return VerificationResult.FAILED;
		}
		int indexOf = hash.indexOf(HASHING_ALGORITHM_SEPARATOR);
		boolean hashedByDefaultAlgorithm;
		PasswordHashingAlgorithm algorithm;
		switch (indexOf) {
			case -1:
				throw new IllegalArgumentException("Not a password hash " + hash);
			case 0: {
				// compatibility
				algorithm = SignatureServiceHashing.INSTANCE;
				hashedByDefaultAlgorithm = false;
				break;
			}
			default: {
				String algorithmName = hash.substring(0, indexOf);
				algorithm = _algorithms.get(algorithmName);
				if (algorithm == null) {
					throw new IllegalArgumentException("Unknown algorithm " + algorithmName);
				}
				hashedByDefaultAlgorithm = algorithmName.equals(_defaultAlgorithm);
				hash = hash.substring(indexOf + 1, hash.length());
			}
		}
		boolean verified = algorithm.verify(password, hash);
		if (!verified) {
			return VerificationResult.FAILED;
		}
		if (!hashedByDefaultAlgorithm && getConfig().getAutomaticRehash()) {
			return VerificationResult.withRehash(createHash(password));
		}
		return VerificationResult.SUCCESS;
	}

}

