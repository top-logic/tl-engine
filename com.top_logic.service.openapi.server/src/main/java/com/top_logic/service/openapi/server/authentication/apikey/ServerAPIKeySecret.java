/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.apikey;

import java.util.function.Predicate;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.ValueInitializer;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.initializer.SecretInitializer;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeyAuthentication;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeySecret;
import com.top_logic.service.openapi.server.OpenApiServer;
import com.top_logic.service.openapi.server.OpenApiServer.OpenAPIServerPart;
import com.top_logic.service.openapi.server.authentication.AllAuthenticationDomains;
import com.top_logic.service.openapi.server.authentication.ServerSecret;

/**
 * {@link APIKeySecret} for the <i>OpenAPI</i> server.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	APIKeySecret.DOMAIN,
	APIKeySecret.API_KEY,
	APIKeySecret.DESCRIPTION
})
@TagName("api-key-server-secret")
@Label("API key")
public interface ServerAPIKeySecret extends ServerSecret, APIKeySecret {

	@Override
	@ValueInitializer(SecretInitializer.class)
	String getAPIKey();

	@Override
	@Options(fun = AllAPIKeyDomains.class, args = {
		@Ref({ OpenAPIServerPart.SERVER_CONFIGURATION, OpenApiServer.Config.AUTHENTICATIONS }) })
	String getDomain();

	/**
	 * All domains of configured {@link APIKeyAuthentication}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class AllAPIKeyDomains extends AllAuthenticationDomains {

		/**
		 * Creates a new {@link AllAPIKeyDomains}.
		 */
		public AllAPIKeyDomains(DeclarativeFormOptions options) {
			super(options);
		}

		@Override
		protected Predicate<? super AuthenticationConfig> filter() {
			return APIKeyAuthentication.class::isInstance;
		}

	}

}

