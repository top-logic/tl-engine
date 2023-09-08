/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * {@link Base64Encoder} that encrypts with algorithm {@link Base64AESEncoder#AES_ALGORITHM AES}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Base64AESEncoder extends Base64Encoder {

	private static final String AES_ALGORITHM = "AES";

	private static final int AES_BLOCK_SIZE = 16;

	/**
	 * Creates a new {@link Base64AESEncoder}.
	 */
	public Base64AESEncoder(byte[] key) {
		super(adaptLength(key));
	}

	/**
	 * Adapts the key to length {@link #AES_BLOCK_SIZE}. AES needs block sizes that are multiple of
	 * {@link #AES_BLOCK_SIZE}.
	 * 
	 * For unknown reason only exact {@link #AES_BLOCK_SIZE} is allowed.
	 */
	private static byte[] adaptLength(byte[] bytes) {
		if (bytes.length != AES_BLOCK_SIZE) {
			bytes = Arrays.copyOf(bytes, AES_BLOCK_SIZE);
		}
		return bytes;
	}

	@Override
	protected SecretKey newKey(byte[] key) {
		return new SecretKeySpec(key, AES_ALGORITHM);
	}

}

