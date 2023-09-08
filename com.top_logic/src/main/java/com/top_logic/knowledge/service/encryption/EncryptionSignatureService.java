/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.encryption;

import com.top_logic.base.security.util.SignatureService;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.encryption.EncryptionService;
import com.top_logic.basic.module.ServiceDependencies;

/**
 * Adapter {@link SignatureService} that delegates to the {@link EncryptionService} signature
 * functionality.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ServiceDependencies({ EncryptionService.Module.class })
public class EncryptionSignatureService extends SignatureService {

	private final EncryptionService _encryptionService;

	/**
	 * Creates a {@link EncryptionSignatureService} for the current {@link EncryptionService}.
	 */
	@CalledByReflection
	public EncryptionSignatureService(Config config) throws ConfigurationException {
		this(config, EncryptionService.getInstance());
	}

	/**
	 * Creates a {@link EncryptionSignatureService}.
	 * 
	 * @param encryptionService
	 *        The {@link EncryptionService} to use.
	 */
	public EncryptionSignatureService(Config config, EncryptionService encryptionService) {
		super(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);

		_encryptionService = encryptionService;
	}

	@Override
	protected boolean verifySignature(byte[] plainText, byte[] signature) throws Exception {
		return _encryptionService.checkSignature(plainText, signature);
	}

	@Override
	protected byte[] createSignature(byte[] plainText) throws Exception {
		try {
			return _encryptionService.createSignature(plainText);
		} catch (IllegalStateException ex) {
			return new byte[0];
		}
	}

}
