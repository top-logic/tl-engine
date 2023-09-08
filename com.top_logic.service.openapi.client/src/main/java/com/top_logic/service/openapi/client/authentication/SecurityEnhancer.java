/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.authentication;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;

import com.top_logic.service.openapi.client.registry.impl.call.uri.UriBuilder;

/**
 * Modification of the request to allow an Open API server to authenticate the request.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface SecurityEnhancer {

	/**
	 * Enhances the request URL with security informations to access an Open API server.
	 * 
	 * @param urlBuilder
	 *        Builder of the URL.
	 */
	void enhanceUrl(UriBuilder urlBuilder);

	/**
	 * Enhances the final {@link HttpClient} sending the request to Open API server.
	 */
	default HttpClientBuilder enhanceClient(HttpClientBuilder client) {
		return client;
	}

	/**
	 * Enhances the request with security informations before sending to Open API server.
	 * 
	 * @param client
	 *        The {@link HttpClient} sending the request.
	 * @param request
	 *        The request to enhance.
	 */
	void enhanceRequest(HttpClient client, ClassicHttpRequest request);

	/**
	 * Returns a {@link SecurityEnhancer} that first calls methods in this {@link SecurityEnhancer}
	 * and then the methods in <code>after</code>. If evaluation of either method throws an
	 * exception, it is relayed to the caller of the composed enhancer.
	 *
	 * @param after
	 *        The {@link SecurityEnhancer} to apply after this enhancer is applied.
	 * @throws NullPointerException
	 *         if after is null
	 */
	default SecurityEnhancer andThen(SecurityEnhancer after) {
		if (after == NoSecurityEnhancement.INSTANCE) {
			return this;
		}
		return new CombinedSecurityEnhancer(this, after);
	}

}

