/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;

/**
 * {@link SecretStore} holding the secrets in the configuration itself.
 *
 * <p>
 * Each value is an {@link Encrypted} configuration value: it is written encrypted and accepts a
 * plain value with the prefix <code>unencrypted:</code>. It is meant for test systems without
 * secret infrastructure; a production system keeps its secrets out of the configuration.
 * </p>
 *
 * <pre>
 * &lt;store class="com.top_logic.basic.credential.LiteralSecretStore"&gt;
 *   &lt;secrets&gt;
 *     &lt;secret name="db_password" value="unencrypted:test-password"/&gt;
 *   &lt;/secrets&gt;
 * &lt;/store&gt;
 * </pre>
 */
@Label("Secrets in the configuration")
public class LiteralSecretStore extends AbstractSecretStore<LiteralSecretStore.Config> {

	/**
	 * Configuration options for {@link LiteralSecretStore}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends AbstractSecretStore.Config<LiteralSecretStore> {

		/**
		 * Tag name of this configuration in a list of {@link SecretStore}s.
		 */
		String TAG_NAME = "literal-store";

		/**
		 * @see #getSecrets()
		 */
		String SECRETS = "secrets";

		/**
		 * The secrets by their names (including the {@link #getPrefix() prefix}).
		 */
		@Name(SECRETS)
		@Key(Secret.NAME_ATTRIBUTE)
		@EntryTag(Secret.TAG_NAME)
		Map<String, Secret> getSecrets();

	}

	/**
	 * A secret held in the configuration together with its name.
	 *
	 * @see LiteralSecretStore
	 */
	public interface Secret extends NamedConfigMandatory {

		/**
		 * Tag name of a {@link Secret} in {@link Config#getSecrets()}.
		 */
		String TAG_NAME = "secret";

		/**
		 * @see #getValue()
		 */
		String VALUE = "value";

		/**
		 * The secret.
		 */
		@Name(VALUE)
		@Encrypted
		String getValue();

		/**
		 * @see #getValue()
		 */
		void setValue(String value);

	}

	/**
	 * Creates a {@link LiteralSecretStore} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public LiteralSecretStore(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected String lookupKey(String key) {
		Secret secret = getConfig().getSecrets().get(key);
		if (secret == null) {
			return null;
		}
		String value = secret.getValue();
		if (StringServices.isEmpty(value)) {
			throw new I18NRuntimeException(I18NConstants.ERROR_SECRET_EMPTY__NAME.fill(key));
		}
		return value;
	}

	@Override
	protected ResKey missing(String key) {
		return I18NConstants.ERROR_SECRET_UNDEFINED__NAME.fill(key);
	}

}
