/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.authentication;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;

import com.top_logic.service.openapi.client.registry.impl.call.uri.UriBuilder;

/**
 * {@link SecurityEnhancer} executing multiple {@link SecurityEnhancer}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CombinedSecurityEnhancer implements SecurityEnhancer {

	private final SecurityEnhancer _first;

	private final SecurityEnhancer _second;

	/**
	 * Creates a new {@link CombinedSecurityEnhancer}.
	 */
	public CombinedSecurityEnhancer(SecurityEnhancer before, SecurityEnhancer after) {
		_first = before;
		_second = after;
	}

	@Override
	public void enhanceUrl(UriBuilder urlBuilder) {
		_first.enhanceUrl(urlBuilder);
		_second.enhanceUrl(urlBuilder);
	}

	@Override
	public void enhanceRequest(HttpClient client, ClassicHttpRequest request) {
		_first.enhanceRequest(client, request);
		_second.enhanceRequest(client, request);
	}

	@Override
	public HttpClientBuilder enhanceClient(HttpClientBuilder client) {
		return _second.enhanceClient(_first.enhanceClient(client));
	}

}

