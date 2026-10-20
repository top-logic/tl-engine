/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import junit.framework.TestCase;

/**
 * Tests for {@link ModelDiscoveryHelper} utility methods.
 */
public class TestProviderIntegrationDefaults extends TestCase {

	/** */
	public void testNormalizeBaseUrl() {
		assertEquals("https://api.openai.com",
			ModelDiscoveryHelper.normalizeBaseUrl("https://api.openai.com/v1"));
		assertEquals("http://localhost:11434",
			ModelDiscoveryHelper.normalizeBaseUrl("http://localhost:11434/v1/"));
		assertEquals("http://localhost:8080",
			ModelDiscoveryHelper.normalizeBaseUrl("http://localhost:8080/"));
		assertEquals("https://api.example.com",
			ModelDiscoveryHelper.normalizeBaseUrl("https://api.example.com"));
		assertNull(ModelDiscoveryHelper.normalizeBaseUrl(null));
	}
}
