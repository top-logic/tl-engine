/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.authentication.oauth;

import java.util.function.Predicate;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.service.openapi.client.authentication.AllAuthenticationDomains;
import com.top_logic.service.openapi.client.authentication.ClientSecret;
import com.top_logic.service.openapi.client.registry.ServiceMethodRegistry;
import com.top_logic.service.openapi.client.registry.ServiceMethodRegistry.ServiceRegistryPart;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;
import com.top_logic.service.openapi.common.authentication.oauth.ClientCredentials;
import com.top_logic.service.openapi.common.authentication.oauth.CredentialSecret;

/**
 * {@link CredentialSecret Authentication data} for an Open API client towards the <i>OpenID</i>
 * server.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	CredentialSecret.DOMAIN,
	CredentialSecret.CLIENT_SECRET
})
@TagName("client-credentials-client-secret")
@Label("OpenID authentication")
public interface ClientCredentialSecret extends CredentialSecret, ClientSecret {

	@Override
	@Options(fun = AllClientCredentialsDomains.class, args = {
		@Ref({ ServiceRegistryPart.SERVICE_REGISTRY, ServiceMethodRegistry.Config.AUTHENTICATIONS }) })
	String getDomain();

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

