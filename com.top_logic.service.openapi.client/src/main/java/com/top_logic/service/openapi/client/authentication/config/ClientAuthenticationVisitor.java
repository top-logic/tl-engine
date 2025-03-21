/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.authentication.config;

import com.top_logic.service.openapi.client.authentication.oauth.user.ClientCredentials;

/**
 * Visitor for {@link ClientAuthentication}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ClientAuthenticationVisitor<R, A> {

	/**
	 * Visit case for {@link ClientCredentials}.
	 */
	R visitClientCredentials(ClientCredentials config, A arg);

}

