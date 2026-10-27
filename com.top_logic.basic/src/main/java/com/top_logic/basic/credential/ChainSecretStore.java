/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;

/**
 * {@link SecretStore} asking several stores in turn: the first store holding the secret delivers
 * it.
 *
 * <p>
 * A secret held by a store but not deliverable, e.g. an unreadable file, is an error and does not
 * fall through to the next store.
 * </p>
 *
 * <pre>
 * &lt;store class="com.top_logic.basic.credential.ChainSecretStore"&gt;
 *   &lt;stores&gt;
 *     &lt;directory-store directory="/run/secrets"/&gt;
 *     &lt;env-store/&gt;
 *   &lt;/stores&gt;
 * &lt;/store&gt;
 * </pre>
 */
@Label("Several secret stores")
public class ChainSecretStore extends AbstractSecretStore<ChainSecretStore.Config> {

	/**
	 * Configuration options for {@link ChainSecretStore}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends AbstractSecretStore.Config<ChainSecretStore> {

		/**
		 * Tag name of this configuration in a list of {@link SecretStore}s.
		 */
		String TAG_NAME = "chain-store";

		/**
		 * @see #getStores()
		 */
		String STORES = "stores";

		/**
		 * The stores asked for a secret, in this order.
		 *
		 * <p>
		 * Each store gets the name of the secret with the {@link #getPrefix() prefix} of this
		 * store.
		 * </p>
		 */
		@Name(STORES)
		List<PolymorphicConfiguration<? extends SecretStore>> getStores();

	}

	private final List<SecretStore> _stores;

	/**
	 * Creates a {@link ChainSecretStore} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public ChainSecretStore(InstantiationContext context, Config config) {
		super(context, config);
		_stores = TypedConfiguration.getInstanceListReadOnly(context, config.getStores());
	}

	@Override
	protected String lookupKey(String key) {
		for (SecretStore store : _stores) {
			String secret = store.lookup(key);
			if (secret != null) {
				return secret;
			}
		}
		return null;
	}

	@Override
	protected ResKey missing(String key) {
		return I18NConstants.ERROR_SECRET_UNDEFINED__NAME.fill(key);
	}

}
