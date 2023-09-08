/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.authentication;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;

import com.top_logic.service.openapi.client.registry.impl.call.uri.UriBuilder;

/**
 * No-Op {@link SecurityEnhancer}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NoSecurityEnhancement implements SecurityEnhancer {

	/** Singleton {@link NoSecurityEnhancement} instance. */
	public static final NoSecurityEnhancement INSTANCE = new NoSecurityEnhancement();

	/**
	 * Creates a new {@link NoSecurityEnhancement}.
	 */
	protected NoSecurityEnhancement() {
		// singleton instance
	}

	@Override
	public void enhanceUrl(UriBuilder uriBuilder) {
		// No enhancement.
	}

	@Override
	public void enhanceRequest(HttpClient client, ClassicHttpRequest request) {
		// No enhancement.
	}

	@Override
	public SecurityEnhancer andThen(SecurityEnhancer after) {
		return after;
	}

}

