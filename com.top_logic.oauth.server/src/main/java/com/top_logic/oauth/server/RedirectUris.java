/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.oauth.server;

import java.net.URI;
import java.util.Collection;
import java.util.Objects;

import com.top_logic.basic.util.ResKey;

/**
 * Which redirect URIs a client may register, and which redirect URI of a request matches a
 * registered one.
 *
 * <p>
 * A redirect URI is an absolute <code>https</code> URI, or an <code>http</code> URI of the
 * loopback interface (<code>localhost</code>, <code>127.0.0.1</code>, <code>[::1]</code>) for a
 * native application (RFC 8252, section 7.3). It has no fragment.
 * </p>
 *
 * <p>
 * A request matches a registered URI only if both are the same string, except for a loopback URI:
 * there the port is ignored, because a native client opens an ephemeral port at the time of the
 * request (RFC 8252, section 7.3; OAuth 2.1, section 8.4.2).
 * </p>
 */
public final class RedirectUris {

	/** The scheme of a redirect URI on the network. */
	public static final String HTTPS = "https";

	/** The scheme of a redirect URI on the loopback interface. */
	public static final String HTTP = "http";

	/** Host name of the loopback interface. */
	public static final String LOCALHOST = "localhost";

	/** IPv4 address of the loopback interface. */
	public static final String LOOPBACK_IPV4 = "127.0.0.1";

	/** IPv6 address of the loopback interface, as it appears as host of a URI. */
	public static final String LOOPBACK_IPV6 = "[::1]";

	private RedirectUris() {
		// Static utility.
	}

	/**
	 * Why {@code uri} cannot be registered as redirect URI.
	 *
	 * @return The reason, or <code>null</code> if it can.
	 */
	public static ResKey checkRegistered(URI uri) {
		if (uri == null || !uri.isAbsolute() || uri.isOpaque() || uri.getHost() == null) {
			return I18NConstants.ERROR_REDIRECT_URI_NOT_ABSOLUTE__URI.fill(String.valueOf(uri));
		}
		if (uri.getRawFragment() != null) {
			return I18NConstants.ERROR_REDIRECT_URI_WITH_FRAGMENT__URI.fill(uri.toString());
		}
		String scheme = uri.getScheme().toLowerCase();
		if (HTTPS.equals(scheme)) {
			return null;
		}
		if (HTTP.equals(scheme) && isLoopback(uri)) {
			return null;
		}
		return I18NConstants.ERROR_REDIRECT_URI_NOT_HTTPS__URI.fill(uri.toString());
	}

	/**
	 * The first reason why one of {@code uris} cannot be registered.
	 *
	 * @return The reason, or <code>null</code> if all of them can be registered and there is at
	 *         least one.
	 */
	public static ResKey checkRegistered(Collection<URI> uris) {
		if (uris == null || uris.isEmpty()) {
			return I18NConstants.ERROR_NO_REDIRECT_URI;
		}
		for (URI uri : uris) {
			ResKey problem = checkRegistered(uri);
			if (problem != null) {
				return problem;
			}
		}
		return null;
	}

	/**
	 * Whether the redirect URI of a request matches one of the registered ones.
	 */
	public static boolean matches(Collection<URI> registered, URI requested) {
		if (requested == null) {
			return false;
		}
		for (URI candidate : registered) {
			if (matches(candidate, requested)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether the redirect URI of a request matches a registered one.
	 */
	public static boolean matches(URI registered, URI requested) {
		if (registered.toString().equals(requested.toString())) {
			return true;
		}
		if (!isLoopback(registered) || !isLoopback(requested)) {
			return false;
		}
		return HTTP.equalsIgnoreCase(registered.getScheme())
			&& HTTP.equalsIgnoreCase(requested.getScheme())
			&& registered.getHost().equalsIgnoreCase(requested.getHost())
			&& Objects.equals(registered.getRawPath(), requested.getRawPath())
			&& Objects.equals(registered.getRawQuery(), requested.getRawQuery())
			&& registered.getRawUserInfo() == null && requested.getRawUserInfo() == null
			&& requested.getRawFragment() == null;
	}

	/**
	 * Whether {@code uri} names the loopback interface.
	 */
	public static boolean isLoopback(URI uri) {
		String host = uri.getHost();
		if (host == null) {
			return false;
		}
		return LOCALHOST.equalsIgnoreCase(host) || LOOPBACK_IPV4.equals(host) || LOOPBACK_IPV6.equals(host);
	}

}
