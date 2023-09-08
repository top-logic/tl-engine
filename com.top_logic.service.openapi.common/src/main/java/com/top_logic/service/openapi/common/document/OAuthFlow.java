/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Supported OAuth flows.
 * 
 * @see "https://spec.openapis.org/oas/v3.0.3.html#oauth-flows-object"
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public enum OAuthFlow implements ExternallyNamed {

	/** Configuration for the OAuth Implicit flow. */
	IMPLICIT("implicit"),

	/** Configuration for the OAuth Resource Owner Password flow */
	PASSWORD("password"),

	/** Configuration for the OAuth Client Credentials flow. */
	CLIENT_CREDENTIALS("clientCredentials"),

	/** Configuration for the OAuth Authorization Code flow. */
	AUTHORIZATION_CODE("authorizationCode"),

	;

	private final String _externalName;

	/**
	 * Creates a new {@link OAuthFlow}.
	 */
	OAuthFlow(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}

