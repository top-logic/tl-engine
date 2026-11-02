/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.passkey;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.StringServices;

/**
 * The address of the relying party of the passkeys: the relying party ID the passkeys are bound to,
 * and the origins a browser may run a ceremony on.
 *
 * <p>
 * It is {@link #resolve(String, String, Collection) resolved} from the public address of the
 * application ({@link AliasManager#HOST %HOST%}) and the optional overrides of the configuration,
 * never from a request: a request only tells what a reverse proxy made of it, while the relying
 * party ID binds every passkey for good.
 * </p>
 */
public final class RelyingPartyAddress {

	/** The scheme of an origin served without TLS, accepted by browsers for <code>localhost</code>. */
	public static final String HTTP = "http";

	/** The scheme of an origin served with TLS. */
	public static final String HTTPS = "https";

	private static final int HTTP_DEFAULT_PORT = 80;

	private static final int HTTPS_DEFAULT_PORT = 443;

	private static final String SCHEME_SEPARATOR = "://";

	private final String _rpId;

	private final Set<String> _origins;

	private RelyingPartyAddress(String rpId, Set<String> origins) {
		_rpId = rpId;
		_origins = Collections.unmodifiableSet(origins);
	}

	/**
	 * The relying party ID: the host name the passkeys are bound to.
	 */
	public String getRpId() {
		return _rpId;
	}

	/**
	 * The origins a browser may run a ceremony on, in {@link #normalizeOrigin(String) normal form}.
	 */
	public Set<String> getOrigins() {
		return _origins;
	}

	/**
	 * Whether a ceremony may run on a page with the given origin.
	 *
	 * @param origin
	 *        The origin of the page, e.g. as a browser reports it; compared in
	 *        {@link #normalizeOrigin(String) normal form}. <code>null</code> or a malformed value is
	 *        not accepted.
	 */
	public boolean accepts(String origin) {
		if (StringServices.isEmpty(origin)) {
			return false;
		}
		try {
			return _origins.contains(normalizeOrigin(origin));
		} catch (IllegalArgumentException ex) {
			return false;
		}
	}

	@Override
	public String toString() {
		return "rp-id '" + _rpId + "', origins " + _origins;
	}

	/**
	 * Resolves the address of the relying party.
	 *
	 * <p>
	 * The accepted origins are the configured origins, if any, otherwise the origin of the public
	 * address of the application. The relying party ID is the configured one, if any, otherwise the
	 * host of the public address, otherwise the host of the first configured origin. It must be the
	 * host of every accepted origin or a parent domain of it.
	 * </p>
	 *
	 * @param host
	 *        The public address of the application, the value of {@link AliasManager#HOST}, e.g.
	 *        <code>https://app.example.com/</code>; <code>null</code> or empty if not set.
	 * @param rpId
	 *        The configured relying party ID, e.g. <code>example.com</code>; <code>null</code> or
	 *        empty if not set.
	 * @param origins
	 *        The configured origins; empty if not set.
	 * @return The resolved address.
	 * @throws IllegalArgumentException
	 *         If there is no origin, a value is malformed, or the relying party ID does not fit an
	 *         origin. The message describes the problem for the administrator.
	 */
	public static RelyingPartyAddress resolve(String host, String rpId, Collection<String> origins) {
		String publicAddress = host == null ? "" : host.trim();
		String publicOrigin = null;
		if (!publicAddress.isEmpty()) {
			try {
				publicOrigin = normalizeOrigin(publicAddress);
			} catch (IllegalArgumentException ex) {
				throw new IllegalArgumentException(
					"The public address of the application (" + AliasManager.HOST + ") is malformed: "
						+ ex.getMessage(), ex);
			}
		}

		Set<String> accepted = new LinkedHashSet<>();
		for (String origin : origins) {
			if (StringServices.isEmpty(origin) || origin.isBlank()) {
				continue;
			}
			accepted.add(normalizeOrigin(origin));
		}
		if (accepted.isEmpty()) {
			if (publicOrigin == null) {
				throw new IllegalArgumentException(
					"Neither the public address of the application (" + AliasManager.HOST
						+ ") nor the accepted origins are set.");
			}
			accepted.add(publicOrigin);
		}

		String resolvedRpId;
		if (!StringServices.isEmpty(rpId) && !rpId.isBlank()) {
			resolvedRpId = normalizeRpId(rpId);
		} else if (publicOrigin != null) {
			resolvedRpId = host(publicOrigin);
		} else {
			resolvedRpId = host(accepted.iterator().next());
		}

		for (String origin : accepted) {
			String originHost = host(origin);
			if (!originHost.equals(resolvedRpId) && !originHost.endsWith("." + resolvedRpId)) {
				throw new IllegalArgumentException("The relying party ID '" + resolvedRpId
					+ "' is neither the host nor a parent domain of the origin '" + origin + "'.");
			}
		}
		return new RelyingPartyAddress(resolvedRpId, accepted);
	}

	/**
	 * The origin of the given address as a browser reports it: the scheme and the host in lower
	 * case and the port, unless it is the default port of the scheme; a path, a trailing slash, a
	 * query or a fragment is dropped.
	 *
	 * @param address
	 *        An address of the form <code>scheme://host[:port][/path]</code> with the scheme
	 *        {@link #HTTP} or {@link #HTTPS}.
	 * @throws IllegalArgumentException
	 *         If the address has no such form.
	 */
	public static String normalizeOrigin(String address) {
		String value = address.trim();
		URI uri;
		try {
			uri = new URI(value);
		} catch (URISyntaxException ex) {
			throw new IllegalArgumentException("Not an address: '" + value + "'.", ex);
		}
		String scheme = uri.getScheme();
		String host = uri.getHost();
		if (scheme == null || host == null || !value.contains(SCHEME_SEPARATOR)) {
			throw new IllegalArgumentException(
				"Not an address of the form scheme://host[:port]: '" + value + "'.");
		}
		scheme = scheme.toLowerCase(Locale.ROOT);
		if (!HTTP.equals(scheme) && !HTTPS.equals(scheme)) {
			throw new IllegalArgumentException(
				"Neither " + HTTP + " nor " + HTTPS + " is the scheme of '" + value + "'.");
		}
		if (uri.getRawUserInfo() != null) {
			throw new IllegalArgumentException("An origin has no user information: '" + value + "'.");
		}
		int port = uri.getPort();
		boolean defaultPort = port < 0
			|| (HTTP.equals(scheme) && port == HTTP_DEFAULT_PORT)
			|| (HTTPS.equals(scheme) && port == HTTPS_DEFAULT_PORT);

		StringBuilder result = new StringBuilder();
		result.append(scheme).append(SCHEME_SEPARATOR).append(host.toLowerCase(Locale.ROOT));
		if (!defaultPort) {
			result.append(':').append(port);
		}
		return result.toString();
	}

	private static String normalizeRpId(String rpId) {
		String value = rpId.trim().toLowerCase(Locale.ROOT);
		if (value.startsWith(".") || value.endsWith(".")
			|| !value.matches("[a-z0-9]([a-z0-9.\\-]*[a-z0-9])?")) {
			throw new IllegalArgumentException("The relying party ID '" + rpId
				+ "' is no host name: it must be a domain such as example.com, without scheme, port or path.");
		}
		return value;
	}

	/**
	 * The host of an origin in normal form.
	 */
	private static String host(String origin) {
		return URI.create(origin).getHost();
	}

}
