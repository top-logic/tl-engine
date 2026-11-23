/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import jakarta.servlet.ServletContextEvent;

import junit.framework.Test;

/**
 * Test of a failing immediate application start through
 * {@link AbstractStartStopListener#contextInitialized(ServletContextEvent)}.
 */
public class TestStartupFailure extends StartupFailureTestCase {

	@Override
	protected void startApplication(AbstractStartStopListener application) {
		try {
			initializeContext(application);
			fail("Application start expected to fail.");
		} catch (RuntimeException | Error ex) {
			// Expected.
		}
	}

	/**
	 * Suite of {@link TestStartupFailure}.
	 */
	public static Test suite() {
		return suite(TestStartupFailure.class);
	}

}
