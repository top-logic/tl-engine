/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.http.basic;

import java.util.function.Predicate;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;
import com.top_logic.service.openapi.common.authentication.http.HTTPSecret;
import com.top_logic.service.openapi.common.authentication.http.basic.BasicAuthentication;
import com.top_logic.service.openapi.server.OpenApiServer;
import com.top_logic.service.openapi.server.OpenApiServer.OpenAPIServerPart;
import com.top_logic.service.openapi.server.authentication.AllAuthenticationDomains;
import com.top_logic.service.openapi.server.authentication.ServerSecret;

/**
 * {@link HTTPSecret} for the Open API server.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	HTTPSecret.DOMAIN,
	HTTPSecret.LOGIN,
})
@TagName("basic-auth-server-secret")
@Label("HTTP authentication (BasicAuth)")
public interface ServerBasicAuthSecret extends ServerSecret, HTTPSecret {
	
	@Override
	@Options(fun = AllBasicAuthDomains.class, args = {
		@Ref({ OpenAPIServerPart.SERVER_CONFIGURATION, OpenApiServer.Config.AUTHENTICATIONS }) })
	String getDomain();

	/**
	 * All domains of configured {@link BasicAuthentication}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class AllBasicAuthDomains extends AllAuthenticationDomains {

		/**
		 * Creates a new {@link AllBasicAuthDomains}.
		 */
		public AllBasicAuthDomains(DeclarativeFormOptions options) {
			super(options);
		}

		@Override
		protected Predicate<? super AuthenticationConfig> filter() {
			return BasicAuthentication.class::isInstance;
		}

	}


}

