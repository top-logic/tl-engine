/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.discovery;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.mig.html.layout.LayoutUtils;

/**
 * The context in which an {@link AgentDiscoveryContribution} describes its part of the
 * application.
 *
 * <p>
 * Provides the absolute URL of the application as the requesting client reaches it: the configured
 * host alias {@link AliasManager#HOST} if it is set, else scheme, host and port of the current
 * request as the servlet container reports them (after applying proxy forwarding headers),
 * followed by the context path of the request.
 * </p>
 */
public class DiscoveryContext {

	private final String _applicationUrl;

	/**
	 * Creates a {@link DiscoveryContext}.
	 *
	 * @param applicationUrl
	 *        See {@link #applicationUrl()}.
	 */
	public DiscoveryContext(String applicationUrl) {
		_applicationUrl = stripTrailingSlash(applicationUrl);
	}

	/**
	 * Creates a {@link DiscoveryContext} for the application as the given request reaches it.
	 *
	 * @see LayoutUtils#appendHostURL(HttpServletRequest, Appendable)
	 */
	public static DiscoveryContext fromRequest(HttpServletRequest request) {
		StringBuilder url = new StringBuilder();
		try {
			LayoutUtils.appendHostURL(request, url);
		} catch (IOException ex) {
			// A StringBuilder does not throw.
			throw new UnreachableAssertion(ex);
		}
		url.append(request.getContextPath());
		return new DiscoveryContext(url.toString());
	}

	private static String stripTrailingSlash(String url) {
		return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}

	/**
	 * The absolute URL of the application, e.g. <code>https://host/context</code>, without a
	 * trailing slash.
	 */
	public String applicationUrl() {
		return _applicationUrl;
	}

	/**
	 * The absolute URL of a resource of the application.
	 *
	 * @param contextRelativePath
	 *        The path of the resource relative to the application's context path, e.g.
	 *        <code>/mcp</code>. A missing leading slash is added.
	 */
	public String url(String contextRelativePath) {
		if (contextRelativePath.startsWith("/")) {
			return _applicationUrl + contextRelativePath;
		}
		return _applicationUrl + '/' + contextRelativePath;
	}

}
