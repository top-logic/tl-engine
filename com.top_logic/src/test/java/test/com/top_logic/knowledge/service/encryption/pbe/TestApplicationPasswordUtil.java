/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.encryption.pbe;

import junit.framework.TestCase;

import com.top_logic.knowledge.service.encryption.pbe.ApplicationPasswordUtil;

/**
 * Test case for {@link ApplicationPasswordUtil}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestApplicationPasswordUtil extends TestCase {

	public void testCheckKey() {
		assertTrue(ApplicationPasswordUtil.checkApplicationKey("Demo", "BUTHJ-GUJLK-E5M9X-H69YL-XA5JQ-S"));
	}

	public void testCheckWrongChar() {
		assertFalse(ApplicationPasswordUtil.checkApplicationKey("Demo", "CUTHJ-GUJLK-E5M9X-H69YL-XA5JQ-S"));
	}

	public void testCheckShortKey() {
		assertFalse(ApplicationPasswordUtil.checkApplicationKey("Demo", "BUTHJ-GUJLK-E5M9X-H"));
	}

	public void testCheckArbitraryKey() {
		assertFalse(ApplicationPasswordUtil.checkApplicationKey("Demo", "XXX"));
	}

}
