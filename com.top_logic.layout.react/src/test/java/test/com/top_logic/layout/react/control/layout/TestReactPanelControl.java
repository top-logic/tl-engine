/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.layout;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactPanelControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;

/**
 * Tests that {@link ReactPanelControl} propagates {@link ReactControl#attach() attach} and
 * {@link ReactControl#detach() detach} to its content child (like the other composite controls do),
 * so content lifecycle hooks ({@code onAttach}/{@code onDetach}, e.g. form model-listener
 * registration) fire while the panel is displayed.
 */
public class TestReactPanelControl extends TestCase {

	/**
	 * The panel's content child is attached while the panel is attached, and detached again on
	 * detach.
	 */
	public void testPropagatesAttachToContent() {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue());
		ReactControl child = new ReactStackControl(context, List.of());
		ReactPanelControl panel =
			new ReactPanelControl(context, "title", child, null, null, false, false, false);

		assertFalse("Content is not attached before the panel is attached.", child.isAttached());

		panel.attach();
		assertTrue("Panel propagates attach to its content.", child.isAttached());

		panel.detach();
		assertFalse("Panel propagates detach to its content.", child.isAttached());
	}

	/**
	 * Test suite. The {@link TypeIndex} module keeps the setup consistent with the other tests; the
	 * exercised code paths themselves need no application services.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestReactPanelControl.class, TypeIndex.Module.INSTANCE);
	}
}
