/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import junit.framework.TestCase;

import test.com.top_logic.basic.util.AbstractBasicTestAll;

/**
 * Test in the package of the tested class {@link ResKey}, outside the package prefix
 * <code>test</code>.
 * 
 * <p>
 * The test accesses package-private members of {@link ResKey} and {@link ResKeyEncoding}. It runs
 * as part of the <code>TestAll</code> of the module, since {@link AbstractBasicTestAll} collects the
 * whole test source tree.
 * </p>
 */
public class TestTestAllCollectsSamePackage extends TestCase {

	/**
	 * Tests that the encoded form of a {@link ResKey} with fallback separates the keys with
	 * {@link ResKeyEncoding#FALLBACK_SEPARATOR}.
	 */
	public void testPackagePrivateAccess() {
		ResKey key = ResKey.fallback(ResKey.create("test.direct"), ResKey.create("test.fallback"));

		String encoded = ResKey.encode(key);
		assertEquals("test.direct" + ResKeyEncoding.FALLBACK_SEPARATOR + "test.fallback", encoded);
		assertEquals(encoded, ResKey.encode(ResKey.decode(encoded)));
	}

}
