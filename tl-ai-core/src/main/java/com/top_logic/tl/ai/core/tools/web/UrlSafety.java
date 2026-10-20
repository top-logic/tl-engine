/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;

/**
 * Shared SSRF (server-side request forgery) protection for the web tools - the single source of
 * truth for "may this process open a connection to that destination at all".
 *
 * <p>
 * Rejects non-http(s) schemes, URLs carrying credentials, and hosts that resolve to any address
 * outside the globally routable public internet: loopback, unspecified, link-local (which covers the
 * {@code 169.254.169.254} cloud-metadata endpoint), private RFC 1918 ranges, carrier-grade NAT,
 * unique-local and site-local IPv6, multicast, and the reserved / documentation / benchmarking
 * ranges. IPv6 forms that <em>embed</em> an IPv4 address (IPv4-mapped, IPv4-compatible, 6to4,
 * Teredo, NAT64) are unwrapped and judged by the embedded IPv4 address, so
 * {@code http://[2002:7f00:1::]/} is refused exactly like {@code http://127.0.0.1/}.
 * </p>
 *
 * <p>
 * Used by {@link WebFetchTool} and {@link BrowserTool} / {@link BrowserSession} (headless Chromium)
 * as a pre-flight check, and - authoritatively, for <em>every</em> connection either of them makes -
 * by {@link FilteringProxy}, which connects to the address it checked, so all of them share exactly
 * one policy.
 * </p>
 */
public final class UrlSafety {

	/**
	 * How a host name becomes addresses. {@link #SYSTEM} in production; a test passes a scripted one to
	 * play a DNS server that answers differently on each query, which is what a rebinding attack is.
	 */
	@FunctionalInterface
	public interface NameLookup {

		/** The resolver of the JVM, {@link InetAddress#getAllByName(String)}. */
		NameLookup SYSTEM = InetAddress::getAllByName;

		/**
		 * All addresses {@code name} resolves to.
		 *
		 * @param name
		 *        A registered name or an IP literal, without IPv6 brackets.
		 * @throws UnknownHostException
		 *         When the name does not resolve.
		 */
		InetAddress[] lookup(String name) throws UnknownHostException;
	}

	/** The refusal for a URL that names no host. */
	private static final String NO_HOST = "the URL has no host.";

	private UrlSafety() {
		// Static utility.
	}

	/**
	 * The outcome of resolving a destination host and checking every address it resolves to.
	 *
	 * @param address
	 *        The address a connection to the host must be made to (the <em>pinned</em> address), or
	 *        {@code null} when the host was refused.
	 * @param refusal
	 *        A human-readable reason for refusing the host, or {@code null} when it is permitted.
	 */
	public record HostResolution(InetAddress address, String refusal) {

		/** Whether connecting to the host is permitted. */
		public boolean permitted() {
			return refusal == null;
		}
	}

	/**
	 * Normalises a destination host to the <b>single spelling</b> every policy layer compares against:
	 * trimmed, lower case, and without the trailing dot of the fully qualified form.
	 *
	 * <p>
	 * <b>Why this has to exist in exactly one place.</b> {@code blocked.example.com.} - the
	 * fully qualified name, with the explicit root label - is a perfectly ordinary host: it survives
	 * {@code URI.getHost()}, resolves through {@code InetAddress.getAllByName} and is served by the
	 * same site as {@code blocked.example.com}. It is however neither {@code equals} to nor a
	 * {@code endsWith(".")} sub-domain of the blocklist entry, so a domain-list check that compares raw
	 * hosts silently lets it through. The same argument applies to case. Every layer that decides
	 * "which host is this" - the tool's pre-flight check, the {@link FilteringProxy} and
	 * {@link #resolveHost(String)} - therefore normalises through this one method, so the three cannot
	 * drift apart and reopen the bypass in one of them.
	 * </p>
	 *
	 * @param host
	 *        The host as it was spelled in the URL or in the configuration; may be {@code null}.
	 * @return The normalised host, {@code null} if {@code host} was {@code null}, or the empty string
	 *         if nothing but dots and whitespace was given (which no layer may treat as a match).
	 */
	public static String normalizeHost(String host) {
		if (host == null) {
			return null;
		}
		String trimmed = host.trim();
		// Strip the root label. Repeated, because "example.com.." is refused by nobody's parser and
		// must not become a spelling that slips past the comparison either.
		int end = trimmed.length();
		while (end > 0 && trimmed.charAt(end - 1) == '.') {
			end--;
		}
		return trimmed.substring(0, end).toLowerCase(Locale.ROOT);
	}

	/**
	 * Validates a URL for outbound fetching: {@link #checkUrlShape(URI)} and then
	 * {@link #resolveHost(String)}.
	 *
	 * <p>
	 * A caller that also applies domain lists or a browsing policy runs {@link #checkUrlShape(URI)}
	 * first, then its lists, and this method last: the lists need no network, so a host they refuse is
	 * refused for that reason even when its name does not resolve, and without a lookup (#567).
	 * </p>
	 *
	 * @param uri
	 *        The URL to validate.
	 * @return {@code null} if the URL is acceptable, otherwise a human-readable reason for
	 *         rejection.
	 */
	public static String validateUrl(URI uri) {
		return validateUrl(uri, NameLookup.SYSTEM);
	}

	/**
	 * {@link #validateUrl(URI)} with an explicit {@link NameLookup}.
	 *
	 * @param uri
	 *        The URL to validate.
	 * @param names
	 *        How the host is resolved.
	 * @return {@code null} if the URL is acceptable, otherwise a human-readable reason for
	 *         rejection.
	 */
	public static String validateUrl(URI uri, NameLookup names) {
		String shape = checkUrlShape(uri);
		if (shape != null) {
			return shape;
		}
		return resolveHost(uri.getHost(), names).refusal();
	}

	/**
	 * The part of {@link #validateUrl(URI)} that needs no network: the scheme is http(s), the URL
	 * carries no credentials, and it names a host.
	 *
	 * <p>
	 * This is <em>not</em> the SSRF check - that is the resolution, which {@link #validateUrl(URI)}
	 * adds. It is what has to hold before a host can be compared against a domain list at all:
	 * {@code URI.getHost()} is {@code null} for an authority it cannot parse as a host (an underscore
	 * in the name, for example), and a list check handed {@code null} would refuse it for the wrong
	 * reason or, for a blocklist, not at all.
	 * </p>
	 *
	 * @param uri
	 *        The URL to check.
	 * @return {@code null} if the URL has an acceptable shape, otherwise a human-readable reason for
	 *         rejection.
	 */
	public static String checkUrlShape(URI uri) {
		String scheme = uri.getScheme();
		if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
			return "only http and https URLs are supported.";
		}
		if (uri.getUserInfo() != null) {
			return "URLs containing credentials are not allowed.";
		}
		String host = normalizeHost(uri.getHost());
		if (host == null || host.isEmpty()) {
			return NO_HOST;
		}
		return null;
	}

	/**
	 * Resolves {@code host}, checks <b>every</b> address it resolves to, and returns the address a
	 * connection must be pinned to.
	 *
	 * <p>
	 * All resolved addresses are checked, not just the one that will be used: a host answering with a
	 * mix of a public and a private record must not be reachable through the public one. The returned
	 * address is what the caller has to connect to - connecting by <em>name</em> again would re-run
	 * the lookup and could land on a different (private) answer, which is precisely the DNS-rebinding
	 * TOCTOU this method exists to close.
	 * </p>
	 *
	 * @param host
	 *        The destination host - a registered name or an IP literal. An IP literal is validated
	 *        without any name lookup, so an IP-literal target is refused just as reliably as a name.
	 *        It is {@link #normalizeHost(String) normalised} first, so the fully qualified spelling
	 *        with a trailing dot is judged as the host it actually is.
	 * @return The resolution outcome; never {@code null}.
	 */
	public static HostResolution resolveHost(String host) {
		return resolveHost(host, NameLookup.SYSTEM);
	}

	/**
	 * {@link #resolveHost(String)} with an explicit {@link NameLookup}.
	 *
	 * @param host
	 *        The destination host.
	 * @param names
	 *        How the host is resolved.
	 * @return The resolution outcome; never {@code null}.
	 */
	public static HostResolution resolveHost(String host, NameLookup names) {
		String normalized = normalizeHost(host);
		if (normalized == null || normalized.isEmpty()) {
			return new HostResolution(null, NO_HOST);
		}
		String name = stripIpv6Brackets(normalized);
		InetAddress[] addresses;
		try {
			addresses = names.lookup(name);
		} catch (UnknownHostException ex) {
			return new HostResolution(null, unresolvable(host));
		}
		if (addresses == null || addresses.length == 0) {
			return new HostResolution(null, unresolvable(host));
		}
		for (InetAddress address : addresses) {
			if (isForbidden(address)) {
				return new HostResolution(null,
					"access to internal or private addresses is blocked (" + host + ").");
			}
		}
		return new HostResolution(addresses[0], null);
	}

	/**
	 * The refusal for a host whose name did not resolve. It says both possible causes, because from
	 * here a name that does not exist and a resolver that did not answer look the same, and it says
	 * what to check first, so the model does not retry a misspelled URL in a loop.
	 */
	private static String unresolvable(String host) {
		return "could not resolve host: " + host + " (the name does not exist, or the server's DNS did "
			+ "not answer). Check the spelling of the URL before trying it again.";
	}

	/**
	 * Whether {@code address} is outside the globally routable public internet and must therefore
	 * never be connected to.
	 *
	 * <p>
	 * Deliberately a default-deny classification of everything the IANA special-purpose registries
	 * mark as not globally reachable, rather than a hand-picked list of "internal" ranges: an
	 * attacker only needs one range nobody thought of. IPv6 forms embedding an IPv4 address are
	 * unwrapped and judged by that address.
	 * </p>
	 *
	 * @param address
	 *        The address to classify.
	 * @return {@code true} if a connection to {@code address} must be refused.
	 */
	public static boolean isForbidden(InetAddress address) {
		if (address == null) {
			return true;
		}
		if (address.isLoopbackAddress() || address.isAnyLocalAddress() || address.isLinkLocalAddress()
				|| address.isSiteLocalAddress() || address.isMulticastAddress()) {
			return true;
		}
		byte[] bytes = address.getAddress();
		if (bytes.length == 16) {
			byte[] embedded = embeddedIpv4(bytes);
			if (embedded != null) {
				return isForbiddenIpv4(embedded);
			}
			return isForbiddenIpv6(bytes);
		}
		return isForbiddenIpv4(bytes);
	}

	/**
	 * The IPv4 address embedded in an IPv6 address by one of the transition mechanisms, or
	 * {@code null} when the address embeds none.
	 *
	 * <p>
	 * Each of these gives a second spelling for an IPv4 destination, so each is a way to smuggle a
	 * private IPv4 address past a check that only understands dotted quads: IPv4-mapped
	 * ({@code ::ffff:0:0/96}) and IPv4-compatible ({@code ::/96}) hold the address in the last four
	 * bytes, 6to4 ({@code 2002::/16}) in bytes 2-5, Teredo ({@code 2001:0::/32}) in the last four
	 * bytes obfuscated by a bitwise complement, and NAT64 ({@code 64:ff9b::/96}) in the last four
	 * bytes.
	 * </p>
	 */
	private static byte[] embeddedIpv4(byte[] bytes) {
		if (isPrefix(bytes, 0x20, 0x02)) {
			// 6to4: 2002:V4ADDR::/48
			return new byte[] { bytes[2], bytes[3], bytes[4], bytes[5] };
		}
		if (isPrefix(bytes, 0x20, 0x01, 0x00, 0x00)) {
			// Teredo: 2001:0000:<server>:<flags/port>:<client v4, complemented>
			return new byte[] { (byte) ~bytes[12], (byte) ~bytes[13], (byte) ~bytes[14], (byte) ~bytes[15] };
		}
		if (isPrefix(bytes, 0x00, 0x64, 0xFF, 0x9B, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)) {
			// NAT64 well-known prefix: 64:ff9b::/96
			return lastFour(bytes);
		}
		boolean zeroPrefix = true;
		for (int i = 0; i < 10; i++) {
			if (bytes[i] != 0) {
				zeroPrefix = false;
				break;
			}
		}
		if (zeroPrefix) {
			boolean mapped = (bytes[10] & 0xFF) == 0xFF && (bytes[11] & 0xFF) == 0xFF;
			boolean compatible = bytes[10] == 0 && bytes[11] == 0;
			if (mapped || compatible) {
				// ::ffff:a.b.c.d (IPv4-mapped) and ::a.b.c.d (IPv4-compatible).
				return lastFour(bytes);
			}
		}
		return null;
	}

	/**
	 * Whether the IPv4 address (four bytes) lies in a range that is not globally reachable. The
	 * checks {@link InetAddress} already answers (loopback, any-local, link-local, RFC 1918
	 * site-local, multicast) are repeated here because this method is also reached for an IPv4
	 * address unwrapped from an IPv6 spelling, where those flags describe the <em>IPv6</em> address.
	 */
	private static boolean isForbiddenIpv4(byte[] bytes) {
		int b0 = bytes[0] & 0xFF;
		int b1 = bytes[1] & 0xFF;
		int b2 = bytes[2] & 0xFF;
		if (b0 == 0) {
			// 0.0.0.0/8 - "this host on this network".
			return true;
		}
		if (b0 == 10 || b0 == 127) {
			// 10.0.0.0/8 private, 127.0.0.0/8 loopback.
			return true;
		}
		if (b0 == 100 && b1 >= 64 && b1 <= 127) {
			// 100.64.0.0/10 - carrier-grade NAT.
			return true;
		}
		if (b0 == 169 && b1 == 254) {
			// 169.254.0.0/16 link-local, which is where the cloud metadata endpoint lives.
			return true;
		}
		if (b0 == 172 && b1 >= 16 && b1 <= 31) {
			// 172.16.0.0/12 private.
			return true;
		}
		if (b0 == 192 && b1 == 168) {
			// 192.168.0.0/16 private.
			return true;
		}
		if (b0 == 192 && b1 == 0 && (b2 == 0 || b2 == 2)) {
			// 192.0.0.0/24 IETF protocol assignments, 192.0.2.0/24 documentation.
			return true;
		}
		if (b0 == 192 && b1 == 88 && b2 == 99) {
			// 192.88.99.0/24 - the deprecated 6to4 relay anycast prefix (RFC 7526). Deprecated is not
			// unreachable: a packet to it is still routed to whatever relay a network kept, so it is as
			// much a way out of the intended destination set as any other special-purpose range.
			return true;
		}
		if (b0 == 198 && (b1 == 18 || b1 == 19)) {
			// 198.18.0.0/15 - benchmarking.
			return true;
		}
		if (b0 == 198 && b1 == 51 && b2 == 100) {
			// 198.51.100.0/24 documentation.
			return true;
		}
		if (b0 == 203 && b1 == 0 && b2 == 113) {
			// 203.0.113.0/24 documentation.
			return true;
		}
		if (b0 >= 224) {
			// 224.0.0.0/4 multicast, 240.0.0.0/4 reserved (incl. 255.255.255.255 broadcast).
			return true;
		}
		return false;
	}

	/** Whether the IPv6 address (sixteen bytes, embedding no IPv4 address) is not globally reachable. */
	private static boolean isForbiddenIpv6(byte[] bytes) {
		if ((bytes[0] & 0xFE) == 0xFC) {
			// fc00::/7 - unique local addresses.
			return true;
		}
		if ((bytes[0] & 0xFF) == 0xFE && (bytes[1] & 0xC0) == 0xC0) {
			// fec0::/10 - deprecated site-local (InetAddress reports it, but be explicit).
			return true;
		}
		if (isPrefix(bytes, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)) {
			// 100::/64 - discard-only prefix.
			return true;
		}
		if (isPrefix(bytes, 0x20, 0x01, 0x0D, 0xB8)) {
			// 2001:db8::/32 - documentation.
			return true;
		}
		return false;
	}

	private static byte[] lastFour(byte[] bytes) {
		return new byte[] { bytes[12], bytes[13], bytes[14], bytes[15] };
	}

	private static boolean isPrefix(byte[] bytes, int... prefix) {
		for (int i = 0; i < prefix.length; i++) {
			if ((bytes[i] & 0xFF) != prefix[i]) {
				return false;
			}
		}
		return true;
	}

	/** Strips the brackets an IPv6 literal carries inside a URL authority ({@code [::1]}). */
	private static String stripIpv6Brackets(String host) {
		if (host.length() > 1 && host.charAt(0) == '[' && host.charAt(host.length() - 1) == ']') {
			return host.substring(1, host.length() - 1);
		}
		return host;
	}
}
