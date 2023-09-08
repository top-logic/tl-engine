/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.call;

import org.apache.hc.core5.http.ClassicHttpRequest;

import com.top_logic.service.openapi.client.registry.impl.call.uri.UriBuilder;

/**
 * Operation on the HTTP request before the request is sent.
 * 
 * @see CallBuilderFactory#createRequestModifier(MethodSpec)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CallBuilder {

	/**
	 * Builds a part of the service URL by operating on the given {@link UriBuilder}.
	 *
	 * @param builder
	 *        The builder for the URL to invoke.
	 * @param call
	 *        Arguments of the current invocation.
	 */
	default void buildUrl(UriBuilder builder, Call call) {
		// Ignore.
	}

	/**
	 * Modifies the given request before sending.
	 * 
	 * @param request
	 *        The request being constructed.
	 * @param call
	 *        Arguments of the current method call.
	 */
	default void buildRequest(ClassicHttpRequest request, Call call) {
		// Ignore.
	}

}
