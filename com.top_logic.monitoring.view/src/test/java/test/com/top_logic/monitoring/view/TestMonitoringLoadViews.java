/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.monitoring.view;

import junit.framework.Test;

import test.com.top_logic.layout.view.TestLoadViews;

/**
 * Loads the views of this module, see {@link TestLoadViews}.
 */
public class TestMonitoringLoadViews {

	/**
	 * The test loading the views of this module.
	 */
	public static Test suite() {
		return TestLoadViews.suite(TestMonitoringLoadViews.class);
	}
}
