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
 *
 * <p>
 * Secrets are written to the first of the stores that is writable (see
 * {@link #getWritableStore()}), with the {@link Config#getPrefix() prefix} of this store. A
 * writable store is therefore placed at the end of the chain: the read-only stores in front of it
 * take precedence for the names they hold, and a secret the application writes cannot shadow
 * them.
 * </p>
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

	private final WritableSecretStore _writable;

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
		_writable = createWritable();
	}

	private WritableSecretStore createWritable() {
		for (SecretStore store : _stores) {
			WritableSecretStore writable = store.getWritableStore();
			if (writable != null) {
				return getConfig().getPrefix() == null ? writable : new PrefixedStore(writable);
			}
		}
		return null;
	}

	/**
	 * The writable store of the first of the {@link Config#getStores() stores} that has one, with
	 * the {@link Config#getPrefix() prefix} of this store put in front of each name.
	 */
	@Override
	public WritableSecretStore getWritableStore() {
		return _writable;
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

	/**
	 * View of a writable store of the chain that puts the prefix of the chain in front of each
	 * name, so that a written secret is found under the name the chain is asked for.
	 */
	private final class PrefixedStore implements WritableSecretStore {

		private final WritableSecretStore _inner;

		PrefixedStore(WritableSecretStore inner) {
			_inner = inner;
		}

		@Override
		public String lookup(String name) {
			return _inner.lookup(key(name));
		}

		@Override
		public String getSecret(String name) {
			return _inner.getSecret(key(name));
		}

		@Override
		public void store(String name, String secret) {
			_inner.store(key(name), secret);
		}

		@Override
		public boolean remove(String name) {
			return _inner.remove(key(name));
		}

	}

}
