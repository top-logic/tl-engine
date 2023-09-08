/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Supported security scheme types.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public enum SecuritySchemeType implements ExternallyNamed {

	/**
	 * Security using an API key (either as a header, a cookie parameter or as a query parameter).
	 */
	API_KEY("apiKey"),

	/**
	 * HTTP authentication.
	 */
	HTTP("http"),

	/**
	 * OAuth2's common flows (implicit, password, client credentials and authorization code) as
	 * defined in [RFC6749].
	 */
	OAUTH2("oauth2"),

	/**
	 * <i>OpenID Connect Discovery</i>.
	 * 
	 * @see "https://tools.ietf.org/html/draft-ietf-oauth-discovery-06"
	 */
	OPEN_ID_CONNECT("openIdConnect"),

	;

	private final String _externalName;

	/**
	 * Creates a new {@link SecuritySchemeType}.
	 */
	SecuritySchemeType(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}

