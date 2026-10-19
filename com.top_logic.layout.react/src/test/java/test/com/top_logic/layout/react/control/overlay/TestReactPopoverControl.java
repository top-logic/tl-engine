/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.overlay;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.overlay.ReactPopoverControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.ChildControl;
import com.top_logic.layout.react.state.PopoverState;
import com.top_logic.layout.react.state.PopoverState.Mode;
import com.top_logic.layout.react.state.PopoverState.Placement;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests {@link ReactPopoverControl}: the content is created on the first opening only, detached
 * while the popover is closed and attached again when it opens, and disposed with the popover.
 */
public class TestReactPopoverControl extends TestCase {

	/** The React module of trigger and content. */
	private static final String TEST_MODULE = "TestContent";

	private ReactContext _context;

	private ReactControl _trigger;

	/** How often the content was created. */
	private int _created;

	/** The content last created. */
	private ReactControl _content;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_trigger = new ReactControl(_context, null, TEST_MODULE);
		_created = 0;
		_content = null;
	}

	private ReactPopoverControl popover() {
		return new ReactPopoverControl(_context, _trigger, () -> {
			_created++;
			_content = new ReactControl(_context, null, TEST_MODULE);
			return _content;
		}, Placement.BOTTOM_START, Mode.CLICK);
	}

	/**
	 * Tests that a popover that has not been opened has no content, and that displaying it creates
	 * none.
	 */
	public void testNoContentBeforeFirstOpen() {
		ReactPopoverControl popover = popover();
		popover.attach();

		assertFalse(popover.isOpen());
		assertEquals(0, _created);
		Map<?, ?> state = state(popover);
		assertEquals(Boolean.FALSE, state.get(PopoverState.OPEN__PROP));
		assertNull(state.get(PopoverState.CONTENT__PROP));
		assertNotNull("The trigger is displayed while the popover is closed.", state.get(PopoverState.TRIGGER__PROP));
		assertTrue(_trigger.isAttached());
	}

	/**
	 * Tests that the content is created once across opening, closing and opening again, is attached
	 * while open and detached while closed.
	 */
	public void testContentCreatedOnceDetachedWhileClosed() {
		ReactPopoverControl popover = popover();
		popover.attach();

		popover.open();
		assertTrue(popover.isOpen());
		assertEquals(1, _created);
		ReactControl content = _content;
		assertTrue("The content of an open popover is displayed.", content.isAttached());
		assertEquals(content.getID(), contentId(popover));

		popover.close();
		assertFalse(popover.isOpen());
		assertFalse("The content of a closed popover is not displayed.", content.isAttached());
		assertFalse("A closed popover is kept.", content.isDisposed());
		assertNull(state(popover).get(PopoverState.CONTENT__PROP));
		assertTrue("The trigger stays displayed.", _trigger.isAttached());

		popover.open();
		assertEquals("Opening again reuses the content.", 1, _created);
		assertSame(content, _content);
		assertTrue(content.isAttached());
		assertEquals(content.getID(), contentId(popover));
	}

	/**
	 * Tests that the commands of the client open and close the popover.
	 */
	public void testCommands() {
		ReactPopoverControl popover = popover();
		popover.attach();

		command(popover, ReactPopoverControl.CMD_OPEN);
		assertTrue(popover.isOpen());
		assertEquals(Boolean.TRUE, state(popover).get(PopoverState.OPEN__PROP));

		command(popover, ReactPopoverControl.CMD_CLOSE);
		assertFalse(popover.isOpen());
		assertEquals(Boolean.FALSE, state(popover).get(PopoverState.OPEN__PROP));
		assertEquals(1, _created);
	}

	/**
	 * Tests that opening an open popover and closing a closed one change nothing.
	 */
	public void testRepeatedOpenAndCloseAreNoOps() {
		ReactPopoverControl popover = popover();
		popover.attach();

		popover.close();
		assertFalse(popover.isOpen());
		assertEquals("Closing a popover never opened creates no content.", 0, _created);

		popover.open();
		popover.open();
		assertEquals(1, _created);
		assertTrue(_content.isAttached());

		popover.close();
		popover.close();
		assertFalse(popover.isOpen());
		assertFalse(_content.isAttached());
		assertFalse(_content.isDisposed());
	}

	/**
	 * Tests that a popover opened before it is displayed attaches its content only when it is
	 * displayed itself.
	 */
	public void testOpenBeforeAttach() {
		ReactPopoverControl popover = popover();
		popover.open();
		assertEquals(1, _created);
		assertFalse("A popover not displayed displays no content.", _content.isAttached());

		popover.attach();
		assertTrue(_content.isAttached());

		popover.detach();
		assertFalse(_content.isAttached());
		assertTrue("Hiding the popover keeps it open.", popover.isOpen());
	}

	/**
	 * Tests that disposing an open popover disposes trigger and content.
	 */
	public void testCleanupWhileOpen() {
		ReactPopoverControl popover = popover();
		popover.attach();
		popover.open();
		ReactControl content = _content;

		popover.cleanupTree();

		assertTrue(content.isDisposed());
		assertTrue(_trigger.isDisposed());
	}

	/**
	 * Tests that disposing a closed popover disposes the content it keeps, although the content is
	 * no part of its state.
	 */
	public void testCleanupWhileClosed() {
		ReactPopoverControl popover = popover();
		popover.attach();
		popover.open();
		ReactControl content = _content;
		popover.close();

		popover.cleanupTree();

		assertTrue(content.isDisposed());
		assertTrue(_trigger.isDisposed());
	}

	/**
	 * Tests that disposing a popover never opened creates no content.
	 */
	public void testCleanupNeverOpened() {
		ReactPopoverControl popover = popover();
		popover.attach();

		popover.cleanupTree();

		assertEquals(0, _created);
		assertTrue(_trigger.isDisposed());
	}

	/**
	 * Tests a popover whose trigger button shows label and icon: it has no trigger content, opens
	 * and closes like any other, and disposes its content.
	 */
	public void testLabelAndIconTrigger() {
		ReactPopoverControl popover = new ReactPopoverControl(_context, () -> {
			_created++;
			_content = new ReactControl(_context, null, TEST_MODULE);
			return _content;
		}, Placement.RIGHT, Mode.HOVER);
		popover.setLabel("Details");
		popover.setIcon(ThemeImage.cssIcon("fas fa-circle-info"));
		popover.attach();

		Map<?, ?> state = state(popover);
		assertFalse("No trigger content.", state.containsKey(PopoverState.TRIGGER__PROP));
		assertEquals("Details", state.get(PopoverState.LABEL__PROP));
		assertEquals("css:fas fa-circle-info", state.get(PopoverState.ICON__PROP));
		assertEquals("right", state.get(PopoverState.PLACEMENT__PROP));
		assertEquals("hover", state.get(PopoverState.MODE__PROP));

		popover.open();
		assertEquals(1, _created);
		assertTrue(_content.isAttached());
		popover.close();
		assertFalse(_content.isAttached());

		popover.setLabel(null);
		popover.setIcon(null);
		state = state(popover);
		assertNull(state.get(PopoverState.LABEL__PROP));
		assertNull(state.get(PopoverState.ICON__PROP));

		ReactControl content = _content;
		popover.cleanupTree();
		assertTrue(content.isDisposed());
	}

	private static void command(ReactPopoverControl popover, String command) {
		HandlerResult result = popover.executeClientCommand(command, Map.of());
		assertTrue(result.isSuccess());
	}

	private static Object contentId(ReactPopoverControl popover) {
		return ((Map<?, ?>) state(popover).get(PopoverState.CONTENT__PROP)).get(ChildControl.CONTROL_ID__PROP);
	}

	private static Map<?, ?> state(ReactControl control) {
		try {
			return (Map<?, ?>) JSON.fromString(control.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError(ex);
		}
	}

	/**
	 * The suite, with the services the command dispatch needs.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestReactPopoverControl.class, ResourcesModule.Module.INSTANCE));
	}

}
