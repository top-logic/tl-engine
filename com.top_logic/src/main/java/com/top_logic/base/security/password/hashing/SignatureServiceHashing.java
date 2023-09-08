/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.password.hashing;

import com.top_logic.base.security.util.SignatureService;

/**
 * {@link PasswordHashingAlgorithm} using {@link SignatureService}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SignatureServiceHashing implements PasswordHashingAlgorithm {

	/** Singleton {@link SignatureServiceHashing} instance. */
	public static final SignatureServiceHashing INSTANCE = new SignatureServiceHashing();

	private SignatureServiceHashing() {
		// singleton instance
	}

	private static SignatureService signatureService() {
		return SignatureService.getInstance();
	}

	@Override
	public String createHash(char[] password) {
		return signatureService().sign(password);
	}

	@Override
	public boolean verify(char[] password, String hash) {
		return signatureService().verify(password, hash);
	}

}

