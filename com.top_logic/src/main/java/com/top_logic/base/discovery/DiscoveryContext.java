/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.discovery;

import java.io.IOException;
import java.net.URI;

import jakarta.servlet.http.HttpServletRequest;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.base.discovery.catalog.CatalogEntry;
import com.top_logic.basic.thread.ThreadContextManager;
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

	/**
	 * The scheme of a catalog {@link CatalogEntry#getIdentifier() identifier}, see
	 * {@link #catalogIdentifier(String, String)}.
	 */
	public static final String CATALOG_IDENTIFIER_PREFIX = "urn:air:";

	/**
	 * The {@link #host()} of an application whose URL names no host.
	 */
	public static final String NO_HOST = "local";

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

	/**
	 * Creates a {@link DiscoveryContext} for the application as the request of the current
	 * interaction reaches it.
	 *
	 * <p>
	 * Without a request - in a scheduled task, say - the application URL is built from the aliases
	 * {@link AliasManager#HOST} and {@link AliasManager#APP_CONTEXT}; without a configured host it
	 * is only the context path.
	 * </p>
	 *
	 * @see #fromRequest(HttpServletRequest)
	 */
	public static DiscoveryContext current() {
		InteractionContext interaction = ThreadContextManager.getInteraction();
		HttpServletRequest request = interaction == null ? null : interaction.asRequest();
		if (request != null) {
			return fromRequest(request);
		}
		AliasManager aliases = AliasManager.getInstance();
		return new DiscoveryContext(StringServices.nonNull(aliases.getAlias(AliasManager.HOST))
			+ StringServices.nonNull(aliases.getAlias(AliasManager.APP_CONTEXT)));
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
	 * The host name of the {@link #applicationUrl() application URL}, or {@link #NO_HOST} if it
	 * names none.
	 */
	public String host() {
		String host;
		try {
			host = URI.create(_applicationUrl).getHost();
		} catch (IllegalArgumentException ex) {
			host = null;
		}
		return StringServices.isEmpty(host) ? NO_HOST : host;
	}

	/**
	 * The {@link CatalogEntry#getIdentifier() identifier} of an artifact this application publishes
	 * in its AI Catalog:
	 * <code>{@value #CATALOG_IDENTIFIER_PREFIX}&lt;host&gt;:&lt;namespace&gt;:&lt;name&gt;</code>,
	 * with the {@link #host()} as publisher.
	 *
	 * @param namespace
	 *        The kind of the artifact, e.g. <code>mcp</code>.
	 * @param name
	 *        The name of the artifact within the namespace.
	 */
	public String catalogIdentifier(String namespace, String name) {
		return CATALOG_IDENTIFIER_PREFIX + host() + ':' + namespace + ':' + name;
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
