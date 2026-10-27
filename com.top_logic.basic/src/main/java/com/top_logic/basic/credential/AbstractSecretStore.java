/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;

/**
 * Base class for {@link SecretStore}s that look up a secret under its name with an optional
 * prefix.
 *
 * <p>
 * The prefix lets several installations share a store, e.g. the variables
 * <code>prod_db_password</code> and <code>test_db_password</code> for the secret
 * <code>db_password</code>.
 * </p>
 *
 * @param <C>
 *        The configuration type.
 */
public abstract class AbstractSecretStore<C extends AbstractSecretStore.Config<?>>
		extends AbstractConfiguredInstance<C> implements SecretStore {

	/**
	 * Configuration options for {@link AbstractSecretStore}.
	 *
	 * @param <I>
	 *        The store type.
	 */
	@Abstract
	public interface Config<I extends AbstractSecretStore<?>> extends PolymorphicConfiguration<I> {

		/**
		 * @see #getPrefix()
		 */
		String PREFIX = "prefix";

		/**
		 * Text put in front of the name of a secret to get the key under which this store holds
		 * it.
		 *
		 * <p>
		 * With the prefix <code>prod_</code>, the secret <code>db_password</code> is looked up as
		 * <code>prod_db_password</code>.
		 * </p>
		 */
		@Name(PREFIX)
		String getPrefix();

		/**
		 * @see #getPrefix()
		 */
		void setPrefix(String value);

	}

	/**
	 * Creates a {@link AbstractSecretStore} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public AbstractSecretStore(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public final String lookup(String name) {
		return lookupKey(key(name));
	}

	@Override
	public final String getSecret(String name) {
		String key = key(name);
		String secret = lookupKey(key);
		if (secret == null) {
			throw new I18NRuntimeException(missing(key));
		}
		return secret;
	}

	/**
	 * The key under which this store holds the secret with the given name.
	 */
	protected final String key(String name) {
		String prefix = getConfig().getPrefix();
		return prefix == null ? name : prefix + name;
	}

	/**
	 * The current value of the secret with the given key.
	 *
	 * @param key
	 *        The name of the secret with the configured prefix.
	 * @return The secret, never empty, or <code>null</code> if this store does not hold a secret
	 *         with the given key.
	 *
	 * @see SecretStore#lookup(String)
	 */
	protected abstract String lookupKey(String key);

	/**
	 * The error reported when this store holds no secret with the given key.
	 *
	 * @param key
	 *        The name of the secret with the configured prefix.
	 */
	protected abstract ResKey missing(String key);

}
