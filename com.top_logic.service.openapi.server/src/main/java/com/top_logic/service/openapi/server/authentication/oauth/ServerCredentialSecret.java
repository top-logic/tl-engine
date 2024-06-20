/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.oauth;

import java.util.function.Predicate;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;
import com.top_logic.service.openapi.common.authentication.oauth.CredentialSecret;
import com.top_logic.service.openapi.common.authentication.oauth.ServerCredentials;
import com.top_logic.service.openapi.server.OpenApiServer;
import com.top_logic.service.openapi.server.OpenApiServer.OpenAPIServerPart;
import com.top_logic.service.openapi.server.authentication.AllAuthenticationDomains;
import com.top_logic.service.openapi.server.authentication.ServerSecret;

/**
 * {@link CredentialSecret Authentication data} for an Open API server towards the <i>OpenID</i>
 * server.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	CredentialSecret.DOMAIN,
	CredentialSecret.CLIENT_SECRET
})
@TagName("client-credentials-server-secret")
@Label("OpenID authentication")
public interface ServerCredentialSecret extends CredentialSecret, ServerSecret {

	@Override
	@Options(fun = AllServerCredentialsDomains.class, args = {
		@Ref({ OpenAPIServerPart.SERVER_CONFIGURATION, OpenApiServer.Config.AUTHENTICATIONS }) })
	String getDomain();

	/**
	 * All domains of configured {@link ServerCredentials}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class AllServerCredentialsDomains extends AllAuthenticationDomains {

		/**
		 * Creates a new {@link AllServerCredentialsDomains}.
		 */
		public AllServerCredentialsDomains(DeclarativeFormOptions options) {
			super(options);
		}

		@Override
		protected Predicate<? super AuthenticationConfig> filter() {
			return ServerCredentials.class::isInstance;
		}
	}

}

