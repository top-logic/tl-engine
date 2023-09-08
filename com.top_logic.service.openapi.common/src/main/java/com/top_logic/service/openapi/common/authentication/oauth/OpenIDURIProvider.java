/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.oauth;

import java.net.URI;
import java.net.URL;

import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.URLFormat;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.service.openapi.common.authentication.openid.OpenIDUtils;

/**
 * {@link TokenURIProvider} retrieving informations from an <i>OpenID</i> server.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("OpenID server")
public class OpenIDURIProvider extends AbstractConfiguredInstance<OpenIDURIProvider.Config> implements TokenURIProvider {

	/**
	 * Typed configuration interface definition for {@link OpenIDURIProvider}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<OpenIDURIProvider> {

		/**
		 * The URL of the issuer for the <i>OpenID</i> meta data.
		 * 
		 * <p>
		 * The metadata is downloaded by HTTP GET from
		 * <i>[issuer-url]/.well-known/openid-configuration</i>.
		 * </p>
		 * 
		 */
		@Format(URLFormat.class)
		@Mandatory
		@Label("OpenID URL")
		URL getOpenIDIssuer();

		/**
		 * Setter for {@link #getOpenIDIssuer()}.
		 */
		void setOpenIDIssuer(URL value);
	}

	private volatile OIDCProviderMetadata _metaData;

	/**
	 * Create a {@link OpenIDURIProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public OpenIDURIProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	private OIDCProviderMetadata initMetaData() {
		if (_metaData == null) {
			URI openIDURI = OAuthUtils.toURI(getConfig().getOpenIDIssuer());
			_metaData = OpenIDUtils.openIDMetaData(openIDURI);
		}
		return _metaData;
	}


	@Override
	public URI getIntrospectionEndpointURI() {
		return initMetaData().getIntrospectionEndpointURI();
	}

	@Override
	public URI getTokenEndpointURI() {
		return initMetaData().getTokenEndpointURI();
	}

	@Override
	public String toString() {
		return "OpenIDURIProvider [issuer=" + getConfig().getOpenIDIssuer() + "]";
	}

}

