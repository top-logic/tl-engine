/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * Shared SSRF (server-side request forgery) protection for the web tools.
 *
 * <p>
 * Rejects non-http(s) schemes, URLs carrying credentials, and hosts that resolve to
 * loopback, any-local, link-local, site-local (RFC 1918), unique-local IPv6 (fc00::/7) or
 * multicast addresses. Used by both {@link WebFetchTool} (plain {@code HttpClient}) and
 * {@link BrowserTool} (headless Chromium), so the two share exactly one policy.
 * </p>
 */
public final class UrlSafety {

	private UrlSafety() {
		// Static utility.
	}

	/**
	 * Validates a URL for outbound fetching.
	 *
	 * @param uri
	 *        The URL to validate.
	 * @return {@code null} if the URL is acceptable, otherwise a human-readable reason for
	 *         rejection.
	 */
	public static String validateUrl(URI uri) {
		String scheme = uri.getScheme();
		if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
			return "only http and https URLs are supported.";
		}
		if (uri.getUserInfo() != null) {
			return "URLs containing credentials are not allowed.";
		}
		String host = uri.getHost();
		if (host == null || host.isEmpty()) {
			return "the URL has no host.";
		}
		try {
			for (InetAddress address : InetAddress.getAllByName(host)) {
				if (address.isLoopbackAddress() || address.isAnyLocalAddress() || address.isLinkLocalAddress()
						|| address.isSiteLocalAddress() || address.isMulticastAddress()
						|| isUniqueLocalIpv6(address)) {
					return "access to internal or private addresses is blocked (" + host + ").";
				}
			}
		} catch (UnknownHostException ex) {
			return "could not resolve host: " + host;
		}
		return null;
	}

	private static boolean isUniqueLocalIpv6(InetAddress address) {
		byte[] bytes = address.getAddress();
		return bytes.length == 16 && (bytes[0] & 0xFE) == 0xFC; // fc00::/7
	}
}
