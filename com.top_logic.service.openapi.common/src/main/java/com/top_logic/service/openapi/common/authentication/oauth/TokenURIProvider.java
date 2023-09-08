/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.oauth;

import java.net.URI;

/**
 * Provider of {@link URI}s to use for authentication of requests.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TokenURIProvider {

	/**
	 * Endpoint {@link URI} to check that a given token is valid.
	 * 
	 * @return May be <code>null</code> when no such endpoint {@link URI} exists.
	 */
	URI getIntrospectionEndpointURI();

	/**
	 * Endpoint {@link URI} to receive a valid token from.
	 * 
	 * @return May be <code>null</code> when no such endpoint {@link URI} exists.
	 */
	URI getTokenEndpointURI();

}

