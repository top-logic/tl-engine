/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.passkey;

import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.security.passkey.RelyingPartyAddress;

/**
 * Tests for {@link RelyingPartyAddress}: the relying party ID and the accepted origins resolved
 * from the public address of the application and the overrides of the configuration.
 */
public class TestRelyingPartyAddress extends TestCase {

	/** The public address alone gives the origin and the relying party ID. */
	public void testFromPublicAddress() {
		RelyingPartyAddress address = RelyingPartyAddress.resolve("https://apps.top-logic.com", null, List.of());
		assertEquals("apps.top-logic.com", address.getRpId());
		assertEquals(Set.of("https://apps.top-logic.com"), address.getOrigins());
	}

	/** Path, trailing slash, default port and case are normalized away; another port is kept. */
	public void testNormalization() {
		assertEquals("https://app.example.com", RelyingPartyAddress.normalizeOrigin("https://app.example.com/"));
		assertEquals("https://app.example.com", RelyingPartyAddress.normalizeOrigin("https://app.example.com/tl/"));
		assertEquals("https://app.example.com", RelyingPartyAddress.normalizeOrigin("https://app.example.com:443"));
		assertEquals("http://app.example.com", RelyingPartyAddress.normalizeOrigin("http://app.example.com:80/"));
		assertEquals("http://localhost:8080", RelyingPartyAddress.normalizeOrigin(" HTTP://LocalHost:8080/x?y#z "));
		assertEquals("https://app.example.com:8443",
			RelyingPartyAddress.normalizeOrigin("https://app.example.com:8443/"));
		assertEquals("Port 80 is no default port for https.", "https://app.example.com:80",
			RelyingPartyAddress.normalizeOrigin("https://app.example.com:80"));

		RelyingPartyAddress address = RelyingPartyAddress.resolve("http://localhost:8080/tl-demo/", "", List.of());
		assertEquals("localhost", address.getRpId());
		assertEquals(Set.of("http://localhost:8080"), address.getOrigins());
	}

	/** A page origin is compared in normal form; another scheme, host or port is not accepted. */
	public void testAccepts() {
		RelyingPartyAddress address = RelyingPartyAddress.resolve("https://app.example.com/", null, List.of());
		assertTrue(address.accepts("https://app.example.com"));
		assertTrue(address.accepts("https://APP.example.com:443"));
		assertFalse(address.accepts("http://app.example.com"));
		assertFalse(address.accepts("https://app.example.com:8443"));
		assertFalse(address.accepts("https://other.example.com"));
		assertFalse(address.accepts(null));
		assertFalse(address.accepts(""));
		assertFalse(address.accepts("no address"));
	}

	/** Configured origins replace the origin of the public address. */
	public void testOriginsOverride() {
		RelyingPartyAddress address = RelyingPartyAddress.resolve("https://app.example.com", null,
			List.of("https://app.example.com/", "https://login.app.example.com"));
		assertEquals("app.example.com", address.getRpId());
		assertEquals(Set.of("https://app.example.com", "https://login.app.example.com"), address.getOrigins());

		RelyingPartyAddress replaced = RelyingPartyAddress.resolve("https://app.example.com", "example.com",
			List.of("https://other.example.com"));
		assertEquals(Set.of("https://other.example.com"), replaced.getOrigins());
		assertFalse(replaced.accepts("https://app.example.com"));
	}

	/** Without a public address, the origins alone suffice; the ID is the host of the first. */
	public void testOriginsWithoutPublicAddress() {
		RelyingPartyAddress address =
			RelyingPartyAddress.resolve("", null, List.of("https://app.example.com:8443", ""));
		assertEquals("app.example.com", address.getRpId());
		assertEquals(Set.of("https://app.example.com:8443"), address.getOrigins());
	}

	/** An ID override may name a parent domain of the origins. */
	public void testRpIdOverride() {
		RelyingPartyAddress address = RelyingPartyAddress.resolve("https://app.example.com", "Example.com", List.of());
		assertEquals("example.com", address.getRpId());
		assertEquals(Set.of("https://app.example.com"), address.getOrigins());
	}

	/** An ID override that is no suffix of every origin host is refused. */
	public void testInvalidRpIdOverride() {
		assertInvalid("https://app.example.com", "other.com", List.of());
		assertInvalid("https://app.example.com", "ple.com", List.of());
		assertInvalid("https://app.example.com", "sub.app.example.com", List.of());
		assertInvalid("https://app.example.com", "example.com",
			List.of("https://app.example.com", "https://app.example.org"));
		assertInvalid("https://app.example.com", "https://app.example.com", List.of());
		assertInvalid("https://app.example.com", "app.example.com:443", List.of());
		assertInvalid("https://app.example.com", ".example.com", List.of());
		assertInvalid("https://app.example.com", null, List.of("https://other.example.org"));
	}

	/** Without public address and origins, or with a malformed value, nothing is resolved. */
	public void testMissingOrMalformed() {
		assertInvalid(null, null, List.of());
		assertInvalid("", null, List.of());
		assertInvalid("  ", "example.com", List.of());
		assertInvalid("app.example.com", null, List.of());
		assertInvalid("ftp://app.example.com", null, List.of());
		assertInvalid("https://user@app.example.com", null, List.of());
		assertInvalid("https://app.example.com", null, List.of("app.example.com"));
	}

	private static void assertInvalid(String host, String rpId, List<String> origins) {
		try {
			RelyingPartyAddress address = RelyingPartyAddress.resolve(host, rpId, origins);
			fail("Expected refusal of host '" + host + "', rp-id '" + rpId + "', origins " + origins + ": " + address);
		} catch (IllegalArgumentException ex) {
			assertNotNull(ex.getMessage());
		}
	}

}
