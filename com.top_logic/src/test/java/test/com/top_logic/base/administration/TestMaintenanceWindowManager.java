/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.administration;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.administration.MaintenanceWindowManager;

/**
 * Test for {@link MaintenanceWindowManager}.
 *
 * @author <a href="mailto:jonathan.huesing@top-logic.com">Jonathan Hüsing</a>
 */
@SuppressWarnings("javadoc")
public class TestMaintenanceWindowManager extends BasicTestCase {

	/**
	 * The static {@link MaintenanceWindowManager#isMaintenanceActive()} convenience is null-safe: in
	 * a minimal setup where the manager's module is not started, it must report maintenance as
	 * inactive rather than fail.
	 */
	public void testMaintenanceInactiveWhenManagerAbsent() {
		assertFalse("The manager module is not started in this setup, so maintenance must be inactive.",
			MaintenanceWindowManager.isMaintenanceActive());
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestMaintenanceWindowManager}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestMaintenanceWindowManager.class);
	}
}
