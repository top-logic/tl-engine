/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.encryption;

import javax.crypto.spec.SecretKeySpec;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;

/**
 * {@link EncryptionService} that is only for testing, since it uses a private key that is
 * publicly available in the configuration.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DummyEncryptionService extends EncryptionService {

	/**
	 * Configuration for {@link DummyEncryptionService}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends EncryptionService.Config {
		/**
		 * Algorithm to be used.
		 */
		String getAlgorithm();

		/**
		 * Key to be used.
		 */
		String getKey();
	}

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link DummyEncryptionService}.
	 */
	public DummyEncryptionService(InstantiationContext context, Config config) {
		super(context, config);

		String hexEncodedKey = config.getKey();
		if (!StringServices.isEmpty(hexEncodedKey)) {
			Logger.warn("Dummy encryption enabled, testing only.", DummyEncryptionService.class);
			String algorithm = config.getAlgorithm();
			SecretKeySpec key = hexDecodeKey(hexEncodedKey, algorithm);
			installEncryptionKey(key);
		}
	}

	private SecretKeySpec hexDecodeKey(String hexEncodedKey, String algorithm) {
		byte[] encodedKey = StringServices.hexStringToBytes(hexEncodedKey);
		SecretKeySpec key = decodeKey(algorithm, encodedKey);
		return key;
	}
	
    private SecretKeySpec decodeKey(String algorithm, byte[] encodedKey) {
		return new SecretKeySpec(encodedKey, algorithm);
	}

}
