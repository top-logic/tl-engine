/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.navigation;

import junit.framework.TestCase;

import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.navigation.ObjectNavigator;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.navigation.DisplayTargetNavigator;
import com.top_logic.layout.view.navigation.RevealPath;

/**
 * Tests that every context a view is displayed in leads to the places the application displays its
 * business objects at.
 */
public class TestDisplayTargetNavigator extends TestCase {

	private ViewContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultViewContext(new DefaultReactContext("", "test", new SSEUpdateQueue()));
	}

	/** A view context leads to the application's display targets. */
	public void testAViewLeadsToTheDisplayTargets() {
		assertEquals(DisplayTargetNavigator.INSTANCE, _context.getObjectNavigator());
	}

	/** So does every context derived from it. */
	public void testEveryDerivedContextLeadsThereToo() {
		assertNavigates(_context.childContext("child"));
		assertNavigates(_context.withIsolatedChannels());
		assertNavigates(_context.withScope(RevealPath.class, RevealPath.ROOT));
		assertNavigates(new DefaultViewContext(_context));
	}

	/** A plain rendering context displays no business objects. */
	public void testAPlainContextLeadsNowhere() {
		assertNull(new DefaultReactContext("", "test", new SSEUpdateQueue()).getObjectNavigator());
	}

	/**
	 * Without a catalog of places to display objects at, nothing is offered as a link - rather than
	 * offering a link that leads nowhere.
	 */
	public void testNothingIsShownWithoutTargets() {
		assertFalse(DisplayTargetNavigator.INSTANCE.canShow("some value"));
	}

	private void assertNavigates(ReactContext derived) {
		ObjectNavigator navigator = derived.getObjectNavigator();

		assertNotNull("A derived context must lead to the same places.", navigator);
		assertEquals(DisplayTargetNavigator.INSTANCE, navigator);
	}

}
