/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * {@link SecretStore} to which the application itself writes secrets, e.g. an access token a user
 * enters in the UI.
 *
 * <p>
 * A secret written with {@link #store(String, String)} is delivered by {@link #lookup(String)}
 * under the same name, so it is found by every {@link NamedSecretSource} naming it, as long as the
 * store is part of the store configured in the {@link SecretStoreService} and no override of the
 * service takes the name. A consumer cannot tell a written secret from one of the read-only stores.
 * </p>
 *
 * <p>
 * The same contract as for reading holds for writing: the store never keeps the plain value in
 * memory beyond the call, never logs it and never puts it into the message of an exception.
 * </p>
 *
 * <p>
 * A caller reaches the writable store of the installation through
 * {@link SecretStoreService#getWritableStore()}.
 * </p>
 *
 * @see SecretStore#getWritableStore()
 */
public interface WritableSecretStore extends SecretStore {

	/**
	 * Stores the given secret under the given name, replacing a secret this store already holds
	 * under the name.
	 *
	 * @param name
	 *        The name of the secret.
	 * @param secret
	 *        The secret, must not be <code>null</code> or empty.
	 *
	 * @throws I18NRuntimeException
	 *         If the secret cannot be stored. The message of the exception names the secret but
	 *         never contains it.
	 */
	void store(String name, String secret);

	/**
	 * Removes the secret with the given name from this store.
	 *
	 * @param name
	 *        The name of the secret.
	 * @return Whether this store held a secret with the given name.
	 *
	 * @throws I18NRuntimeException
	 *         If the secret cannot be removed. The message of the exception names the secret.
	 */
	boolean remove(String name);

	@Override
	default WritableSecretStore getWritableStore() {
		return this;
	}

}
