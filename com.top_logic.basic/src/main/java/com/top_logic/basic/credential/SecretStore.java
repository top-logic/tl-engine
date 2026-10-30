/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * Storage of the secrets of an installation, from which a secret is fetched by its name at the
 * moment of use.
 *
 * <p>
 * A {@link NamedSecretSource} names a secret, the store configured in the {@link SecretStoreService}
 * decides where the secrets live: in environment variables ({@link EnvironmentSecretStore}), in
 * the files of a directory ({@link DirectorySecretStore}), in the configuration itself
 * ({@link LiteralSecretStore}) or in several of them ({@link ChainSecretStore}).
 * </p>
 *
 * <p>
 * Like a {@link SecretSource}, a store is asked for the secret each time it is used, so that a
 * rotated secret is used without restarting the application. A store that is expensive to ask,
 * e.g. a remote vault, keeps a cache of its own.
 * </p>
 *
 * @see SecretStoreService
 */
@ImplementationClassDefault(EnvironmentSecretStore.class)
public interface SecretStore {

	/**
	 * The current value of the secret with the given name, if this store holds it.
	 *
	 * @param name
	 *        The name of the secret.
	 * @return The secret, never empty, or <code>null</code> if this store does not hold a secret
	 *         with the given name.
	 *
	 * @throws I18NRuntimeException
	 *         If the store holds the secret but cannot deliver it, e.g. an unreadable file. The
	 *         message of the exception never contains the secret.
	 */
	String lookup(String name);

	/**
	 * The current value of the secret with the given name.
	 *
	 * <p>
	 * A consumer calls this method each time it uses the secret and does not keep the result.
	 * </p>
	 *
	 * @param name
	 *        The name of the secret.
	 * @return The secret, never <code>null</code> and never empty.
	 *
	 * @throws I18NRuntimeException
	 *         If the secret is not available. The message of the exception names the secret but
	 *         never contains it.
	 */
	default String getSecret(String name) {
		String secret = lookup(name);
		if (secret == null) {
			throw new I18NRuntimeException(I18NConstants.ERROR_SECRET_UNDEFINED__NAME.fill(name));
		}
		return secret;
	}

	/**
	 * The store to which secrets are written that this store delivers afterwards.
	 *
	 * <p>
	 * A secret written to the result is found by {@link #lookup(String)} of this store under the
	 * same name.
	 * </p>
	 *
	 * @return This store, if it is a {@link WritableSecretStore}, a writable store this store
	 *         consists of (e.g. one of the stores of a {@link ChainSecretStore}), or
	 *         <code>null</code> if secrets cannot be written to this store.
	 */
	default WritableSecretStore getWritableStore() {
		return null;
	}

}
