/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication;

import com.top_logic.service.openapi.common.authentication.apikey.APIKeyAuthentication;
import com.top_logic.service.openapi.common.authentication.http.basic.BasicAuthentication;
import com.top_logic.service.openapi.common.authentication.oauth.ClientCredentials;

/**
 * Visitor for {@link ServerAuthentication}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ServerAuthenticationVisitor<R, A> {

	/**
	 * Visit case for {@link APIKeyAuthentication}.
	 */
	R visitAPIKeyAuthentication(APIKeyAuthentication config, A arg);

	/**
	 * Visit case for {@link ClientCredentials}.
	 */
	R visitClientCredentials(ClientCredentials config, A arg);

	/**
	 * Visit case for {@link BasicAuthentication}.
	 */
	R visitBasicAuthentication(BasicAuthentication config, A arg);

}

