/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link CredentialProvider} sending requests without credentials.
 *
 * <p>
 * Used when the requests go through a gateway or proxy that adds the credentials of the external
 * service, or when the service does not require authentication.
 * </p>
 *
 * <pre>
 * &lt;credentials class="com.top_logic.basic.credential.NoCredentials"/&gt;
 * </pre>
 */
public class NoCredentials extends AbstractConfiguredInstance<NoCredentials.Config> implements CredentialProvider {

	/**
	 * Configuration options for {@link NoCredentials}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<NoCredentials> {

		/**
		 * Tag name of this configuration in a list of {@link CredentialProvider}s.
		 */
		String TAG_NAME = "no-credentials";

	}

	/**
	 * Creates a {@link NoCredentials} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public NoCredentials(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void authenticate(RequestAuthentication request) {
		// Credentials are added by a gateway, if at all.
	}

}
