/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.oauth.server;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.client5.http.DnsResolver;

/**
 * {@link DnsResolver} that resolves a host once and answers only if every address is an address
 * of the public internet.
 *
 * <p>
 * The HTTP client connects to exactly the addresses this resolver returns, so the addresses that
 * were checked are the ones connected to: a host whose name resolves to a public address for the
 * check and to a private one a moment later (DNS rebinding) cannot reach the private network. The
 * host name itself is unchanged, so TLS server name indication and host name verification work as
 * without this resolver.
 * </p>
 */
public class PublicAddressDnsResolver implements DnsResolver {

	/**
	 * Refusal of a host that resolves to an address that is not public.
	 */
	public static class NonPublicAddressException extends UnknownHostException {

		/**
		 * Creates a {@link NonPublicAddressException}.
		 *
		 * @param host
		 *        The refused host.
		 */
		public NonPublicAddressException(String host) {
			super("Host '" + host + "' resolves to an address that is not public.");
		}
	}

	private final DnsResolver _delegate;

	/**
	 * Creates a {@link PublicAddressDnsResolver}.
	 *
	 * @param delegate
	 *        The resolution whose result is checked, e.g.
	 *        {@link org.apache.hc.client5.http.SystemDefaultDnsResolver#INSTANCE}.
	 */
	public PublicAddressDnsResolver(DnsResolver delegate) {
		_delegate = delegate;
	}

	@Override
	public InetAddress[] resolve(String host) throws UnknownHostException {
		InetAddress[] addresses = _delegate.resolve(host);
		if (addresses == null || addresses.length == 0) {
			throw new UnknownHostException(host);
		}
		for (InetAddress address : addresses) {
			if (!isPublic(address)) {
				throw new NonPublicAddressException(host);
			}
		}
		return addresses;
	}

	@Override
	public List<InetSocketAddress> resolve(String host, int port) throws UnknownHostException {
		InetAddress[] addresses = resolve(host);
		List<InetSocketAddress> result = new ArrayList<>(addresses.length);
		for (InetAddress address : addresses) {
			result.add(new InetSocketAddress(address, port));
		}
		return result;
	}

	@Override
	public String resolveCanonicalHostname(String host) throws UnknownHostException {
		return _delegate.resolveCanonicalHostname(host);
	}

	/**
	 * Whether {@code address} is an address of the public internet: not loopback, wildcard,
	 * link-local, private, multicast, IPv6 unique local (<code>fc00::/7</code>) or shared address
	 * space of carrier-grade NAT (<code>100.64.0.0/10</code>).
	 */
	public static boolean isPublic(InetAddress address) {
		if (address.isLoopbackAddress() || address.isAnyLocalAddress() || address.isLinkLocalAddress()
			|| address.isSiteLocalAddress() || address.isMulticastAddress()) {
			return false;
		}
		byte[] bytes = address.getAddress();
		if (address instanceof Inet6Address) {
			return (bytes[0] & 0xFE) != 0xFC;
		}
		return !((bytes[0] & 0xFF) == 100 && (bytes[1] & 0xC0) == 64);
	}

}
