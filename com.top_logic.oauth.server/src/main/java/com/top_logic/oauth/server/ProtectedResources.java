/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.oauth.server;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.base.discovery.DiscoveryContext;

/**
 * The resources whose access tokens the application accepts, each with the location of its
 * protected resource metadata (RFC 9728).
 *
 * <p>
 * An endpoint guarded by {@link OAuthAccessTokenAuthentication} registers its
 * {@link OAuthAccessTokenAuthentication.Config#getResource() resource} here when its authenticator
 * is created ({@link #register(String)}), whichever service builds it and whenever it starts. The
 * {@link OAuthServerServlet} answers the metadata of a registered resource, and a
 * <code>404</code> for any other path below {@link #METADATA_PATH}.
 * </p>
 *
 * <p>
 * The metadata of a resource is served at {@link #METADATA_PATH} followed by the path of the
 * resource relative to the application (<code>/.well-known/oauth-protected-resource/mcp</code> for
 * the resource <code>/mcp</code>), inside the context of the application. RFC 9728 derives the
 * location by inserting the well-known path between the host and the path of the resource, which
 * for an application with a context path lies outside of it; the location is therefore announced to
 * the client in the challenge of every refusal, see {@link OAuthAccessTokenAuthenticator}.
 * </p>
 */
public final class ProtectedResources {

	/**
	 * Context-relative path below which the protected resource metadata is served.
	 */
	public static final String METADATA_PATH = "/.well-known/oauth-protected-resource";

	/**
	 * The {@link ProtectedResourceMetadata#getBearerMethodsSupported() bearer method} of the
	 * <code>Authorization</code> request header (RFC 6750, section 2.1).
	 */
	public static final String BEARER_METHOD_HEADER = "header";

	/**
	 * Configured resources by the path of their metadata relative to {@link #METADATA_PATH}.
	 */
	private static final Map<String, String> RESOURCES = new ConcurrentHashMap<>();

	private ProtectedResources() {
		// Static registry.
	}

	/**
	 * Announces that access tokens for the given resource are accepted.
	 *
	 * @param resource
	 *        See {@link OAuthAccessTokenAuthentication.Config#getResource()}.
	 */
	public static void register(String resource) {
		RESOURCES.put(metadataSuffix(resource), resource);
	}

	/**
	 * The configured resource whose metadata is served at {@link #METADATA_PATH} followed by the
	 * given suffix.
	 *
	 * @param suffix
	 *        The rest of the requested path after {@link #METADATA_PATH}, empty or starting with a
	 *        slash.
	 * @return The resource as {@link #register(String) registered}, <code>null</code> if no
	 *         endpoint accepts tokens for such a resource.
	 */
	public static String lookup(String suffix) {
		return RESOURCES.get(suffix);
	}

	/**
	 * The context-relative path of the metadata of the given resource.
	 *
	 * @param resource
	 *        See {@link OAuthAccessTokenAuthentication.Config#getResource()}.
	 */
	public static String metadataPath(String resource) {
		return METADATA_PATH + metadataSuffix(resource);
	}

	/**
	 * The absolute URI of the given resource, for a client reaching the application in the given
	 * context.
	 *
	 * @param resource
	 *        See {@link OAuthAccessTokenAuthentication.Config#getResource()}: absolute, or a path
	 *        relative to the application.
	 */
	public static String resourceUri(String resource, DiscoveryContext context) {
		return isRelative(resource) ? context.url(resource) : resource;
	}

	/**
	 * The metadata of the given resource, for a client reaching the application in the given
	 * context.
	 *
	 * @param resource
	 *        See {@link OAuthAccessTokenAuthentication.Config#getResource()}.
	 */
	public static ProtectedResourceMetadata metadata(String resource, DiscoveryContext context) {
		return ProtectedResourceMetadata.create()
			.setResource(resourceUri(resource, context))
			.setAuthorizationServers(List.of(OAuthAuthorizationServer.issuer(context).getValue()))
			.setBearerMethodsSupported(List.of(BEARER_METHOD_HEADER));
	}

	private static String metadataSuffix(String resource) {
		String path = isRelative(resource) ? resource : URI.create(resource).getRawPath();
		if (path == null) {
			return "";
		}
		while (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}

	private static boolean isRelative(String resource) {
		return resource.startsWith("/");
	}

}
