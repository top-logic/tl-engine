/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import junit.framework.Test;

/**
 * Test of a failing application start deferred by the {@link DeferredBootService}.
 */
public class TestDeferredStartupFailure extends StartupFailureTestCase {

	@Override
	protected void startApplication(AbstractStartStopListener application) {
		initializeContext(application);
		assertTrue("Boot not deferred.", DeferredBootService.Module.INSTANCE.isActive());
		try {
			DeferredBootService.getInstance().boot();
			fail("Deferred boot expected to fail.");
		} catch (BootFailure ex) {
			// Expected.
		}
	}

	/**
	 * Suite of {@link TestDeferredStartupFailure}.
	 */
	public static Test suite() {
		return suite(TestDeferredStartupFailure.class);
	}

}
