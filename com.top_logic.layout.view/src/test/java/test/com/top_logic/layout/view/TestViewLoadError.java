/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;

import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.common.ReactAlertControl;
import com.top_logic.layout.react.control.overlay.ReactSnackbarControl.Variant;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.ViewLoadError;

/**
 * Tests that a view that cannot be loaded is reported as an error message of the design system, naming the view and
 * the cause.
 */
public class TestViewLoadError extends TestCase {

	/** The message is an error, titled, with the view path and the root cause below each other. */
	public void testTheViewAndItsCauseAreAnError() {
		ReactAlertControl alert = ViewLoadError.createAlert(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")),
			"Not loaded", "app/broken.view.xml",
			new RuntimeException("wrap", new IllegalStateException("No such channel: 'x'")));

		assertEquals(Variant.ERROR, alert.getVariant());
		assertEquals("Not loaded", alert.getTitle());
		assertEquals("app/broken.view.xml\nNo such channel: 'x'", alert.getMessage());
	}

	/**
	 * The test suite: the alert resolves its theme icon, so the theme and the resources must be running.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(ServiceTestSetup.createSetup(TestViewLoadError.class,
			ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}
}
