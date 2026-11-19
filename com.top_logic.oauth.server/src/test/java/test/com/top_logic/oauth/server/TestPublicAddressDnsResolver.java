/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.oauth.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.hc.client5.http.DnsResolver;

import com.top_logic.oauth.server.HttpClientMetadataFetcher;
import com.top_logic.oauth.server.PublicAddressDnsResolver;

/**
 * Test of the {@link PublicAddressDnsResolver}, which keeps a client ID from making the
 * {@link HttpClientMetadataFetcher} send requests into the application's own network.
 */
public class TestPublicAddressDnsResolver extends TestCase {

	/**
	 * Resolution answering from a fixed table, standing in for the name service.
	 */
	private static final class TableResolver implements DnsResolver {

		private final Map<String, InetAddress[]> _table;

		TableResolver(Map<String, InetAddress[]> table) {
			_table = table;
		}

		@Override
		public InetAddress[] resolve(String host) throws UnknownHostException {
			InetAddress[] result = _table.get(host);
			if (result == null) {
				throw new UnknownHostException(host);
			}
			return result;
		}

		@Override
		public String resolveCanonicalHostname(String host) {
			return host;
		}
	}

	/**
	 * Loopback, private, link-local, unique local and shared addresses are not public.
	 */
	public void testNonPublicAddresses() throws Exception {
		for (String address : new String[] { "127.0.0.1", "10.1.2.3", "172.16.0.1", "192.168.1.1", "169.254.169.254",
			"0.0.0.0", "100.64.0.1", "224.0.0.1", "::1", "fe80::1", "fd00::1", "fc00::1", "::ffff:10.0.0.1" }) {
			assertFalse(address, PublicAddressDnsResolver.isPublic(InetAddress.getByName(address)));
		}
	}

	/**
	 * Addresses of the internet are public.
	 */
	public void testPublicAddresses() throws Exception {
		for (String address : new String[] { "8.8.8.8", "100.128.0.1", "2001:4860:4860::8888" }) {
			assertTrue(address, PublicAddressDnsResolver.isPublic(InetAddress.getByName(address)));
		}
	}

	/**
	 * A host resolving to a public address is answered with exactly the resolved addresses, which
	 * the client then connects to.
	 */
	public void testPublicHostResolvedUnchanged() throws Exception {
		InetAddress[] addresses = { InetAddress.getByName("93.184.215.14"), InetAddress.getByName("2606:2800:21f::1") };
		PublicAddressDnsResolver resolver =
			new PublicAddressDnsResolver(new TableResolver(Map.of("client.example.com", addresses)));

		assertSame(addresses, resolver.resolve("client.example.com"));
		List<InetSocketAddress> endpoints = resolver.resolve("client.example.com", 443);
		assertEquals(2, endpoints.size());
		assertEquals(new InetSocketAddress(addresses[0], 443), endpoints.get(0));
		assertEquals(new InetSocketAddress(addresses[1], 443), endpoints.get(1));
	}

	/**
	 * A host with a private address among its addresses is refused, also through the variant the
	 * connection operator uses.
	 */
	public void testPrivateHostRefused() throws Exception {
		PublicAddressDnsResolver resolver = new PublicAddressDnsResolver(new TableResolver(Map.of(
			"rebinding.example.com",
			new InetAddress[] { InetAddress.getByName("93.184.215.14"), InetAddress.getByName("10.0.0.5") })));

		try {
			resolver.resolve("rebinding.example.com");
			fail("A host with a private address must be refused.");
		} catch (PublicAddressDnsResolver.NonPublicAddressException ex) {
			// Expected.
		}
		try {
			resolver.resolve("rebinding.example.com", 443);
			fail("A host with a private address must be refused.");
		} catch (PublicAddressDnsResolver.NonPublicAddressException ex) {
			// Expected.
		}
	}

}
