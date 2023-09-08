/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.oauth;

import java.util.function.Predicate;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.service.openapi.common.authentication.AllAuthenticationDomains;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;

/**
 * {@link SecretConfiguration} to authenticate using the OAuth2 client credentials mechanism.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("Authentication data")
public interface CredentialSecret extends SecretConfiguration {

	/** Configuration name for {@link #getClientSecret()}. */
	String CLIENT_SECRET = "client-secret";

	/**
	 * {@link ClientSecret} to authenticate this client against the token server.
	 * 
	 * @return May be <code>null</code>, when no authentication is necessary to get or validate
	 *         token.
	 */
	@Name(CLIENT_SECRET)
	@Label("Access data")
	ClientSecret getClientSecret();

	/**
	 * All domains of configured {@link ClientCredentials}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class AllClientCredentialsDomains extends AllAuthenticationDomains {

		/**
		 * Creates a new {@link AllClientCredentialsDomains}.
		 */
		public AllClientCredentialsDomains(DeclarativeFormOptions options) {
			super(options);
		}

		@Override
		protected Predicate<? super AuthenticationConfig> filter() {
			return ClientCredentials.class::isInstance;
		}

	}
}

