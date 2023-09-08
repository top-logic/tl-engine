/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.oauth;

import java.net.URI;
import java.net.URL;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.URLFormat;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;

/**
 * {@link TokenURIProvider} delivering configured {@link URL}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultURIProvider extends AbstractConfiguredInstance<DefaultURIProvider.Config> implements TokenURIProvider {

	/**
	 * Typed configuration interface definition for {@link DefaultURIProvider}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	@DisplayOrder({
		Config.TOKEN_URL,
		Config.INTROSPECTION_URL,
	})
	public interface Config extends PolymorphicConfiguration<DefaultURIProvider> {

		/** @see #getIntrospectionURL() */
		String INTROSPECTION_URL = "introspection-url";

		/** @see #getTokenURL() */
		String TOKEN_URL = "token-url";

		/**
		 * Endpoint {@link URL} to check that a given token is valid.
		 */
		@Format(URLFormat.class)
		@Name(INTROSPECTION_URL)
		URL getIntrospectionURL();

		/**
		 * Setter for {@link #getIntrospectionURL()}.
		 */
		void setIntrospectionURL(URL value);

		/**
		 * Endpoint {@link URL} to receive a valid token from.
		 */
		@Format(URLFormat.class)
		@Name(TOKEN_URL)
		@Mandatory
		URL getTokenURL();

		/**
		 * Setter for {@link #getTokenURL()}.
		 */
		void setTokenURL(URL value);
	}

	/**
	 * Create a {@link DefaultURIProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public DefaultURIProvider(InstantiationContext context, Config config) {
		super(context, config);
	}


	@Override
	public URI getIntrospectionEndpointURI() {
		return OAuthUtils.toURI(getConfig().getIntrospectionURL());
	}

	@Override
	public URI getTokenEndpointURI() {
		return OAuthUtils.toURI(getConfig().getTokenURL());
	}

	@Override
	public String toString() {
		return "DefaultURIProvider [IntrospectionURL=" + getConfig().getIntrospectionURL()
			+ ", TokenURL=" + getConfig().getTokenURL() + "]";
	}


}

