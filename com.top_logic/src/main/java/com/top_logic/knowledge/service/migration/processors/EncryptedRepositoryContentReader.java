/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.encryption.EncryptionService;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;

/**
 * {@link RepositoryContentReader} decrypting the content of a repository whose files were
 * encrypted with the key of the {@link EncryptionService}.
 *
 * <p>
 * The encrypted content is read through the configured reader and decrypted with the encryption
 * key and its algorithm. The {@link EncryptionService} must be started before the migration reads
 * the first document.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EncryptedRepositoryContentReader
		extends AbstractConfiguredInstance<EncryptedRepositoryContentReader.Config<?>>
		implements RepositoryContentReader {

	/**
	 * Configuration options of {@link EncryptedRepositoryContentReader}.
	 */
	public interface Config<I extends EncryptedRepositoryContentReader> extends PolymorphicConfiguration<I> {

		/** Configuration name of {@link #getImpl()}. */
		String IMPL = "impl";

		/**
		 * Access to the encrypted content.
		 */
		@Name(IMPL)
		@Mandatory
		PolymorphicConfiguration<? extends RepositoryContentReader> getImpl();

		/**
		 * @see #getImpl()
		 */
		void setImpl(PolymorphicConfiguration<? extends RepositoryContentReader> value);

	}

	private final RepositoryContentReader _impl;

	/**
	 * Creates a {@link EncryptedRepositoryContentReader} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public EncryptedRepositoryContentReader(InstantiationContext context, Config<?> config) {
		super(context, config);
		_impl = context.getInstance(config.getImpl());
	}

	@Override
	public boolean isVersioned() {
		return _impl.isVersioned();
	}

	@Override
	public BinaryData read(String path, int version) throws IOException {
		BinaryData encrypted = _impl.read(path, version);
		if (encrypted == null) {
			return null;
		}

		SecretKey key = EncryptionService.getInstance().getEncryptionKey();
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(key.getAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, key);
		} catch (GeneralSecurityException ex) {
			throw new IOException("Cannot decrypt document '" + path + "': " + ex.getMessage(), ex);
		}
		try (InputStream in = new CipherInputStream(encrypted.getStream(), cipher)) {
			return BinaryDataFactory.createFileBasedBinaryData(in);
		}
	}

}
