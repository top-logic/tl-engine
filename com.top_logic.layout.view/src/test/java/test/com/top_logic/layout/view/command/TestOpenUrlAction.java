/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.protocol.OpenUrlEvent;
import com.top_logic.layout.react.protocol.SSEEvent;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.view.command.ActionScript;
import com.top_logic.layout.view.command.OpenUrlAction;
import com.top_logic.layout.view.command.ViewActionChain;

/**
 * Tests {@link OpenUrlAction}, the action telling the browser to open an address.
 */
public class TestOpenUrlAction extends TestCase {

	/**
	 * A window queue remembering the events enqueued to it, without a browser connected.
	 */
	private static final class Window extends SSEUpdateQueue {

		final List<SSEEvent> _events = new ArrayList<>();

		@Override
		public void enqueue(SSEEvent event) {
			_events.add(event);
			super.enqueue(event);
		}
	}

	private final Window _window = new Window();

	private ReactContext context() {
		return new DefaultReactContext("", "test", _window, new ReactWindowRegistry("test"));
	}

	/**
	 * Tests that the address the script computes from the argument is opened in a window of its
	 * own, and that the chain goes on with its value.
	 */
	public void testOpensComputedAddress() {
		Object company = new Object();
		ActionScript url = (context, arguments, additional) -> {
			assertSame(company, arguments.value());
			return "https://example.com/company";
		};

		Object result = run(new OpenUrlAction(url, true), company);

		assertSame("The chain continues with its value.", company, result);
		OpenUrlEvent event = singleEvent();
		assertEquals("https://example.com/company", event.getUrl());
		assertTrue(event.isNewWindow());
	}

	/**
	 * Tests that an address is opened in the window of the application when configured so.
	 */
	public void testOpensInSameWindow() {
		run(new OpenUrlAction((context, arguments, additional) -> "docs/help.html", false), null);

		OpenUrlEvent event = singleEvent();
		assertEquals("docs/help.html", event.getUrl());
		assertFalse(event.isNewWindow());
	}

	/**
	 * Tests that without a script, the value the action processes is the address.
	 */
	public void testValueIsAddress() {
		run(new OpenUrlAction(null, true), " https://example.com/ ");

		assertEquals("https://example.com/", singleEvent().getUrl());
	}

	/**
	 * Tests that a chain computing no address opens nothing and goes on.
	 */
	public void testNoAddress() {
		Object value = new Object();
		assertSame(value, run(new OpenUrlAction((context, arguments, additional) -> null, true), value));
		assertSame(value, run(new OpenUrlAction((context, arguments, additional) -> "", true), value));
		assertSame(value, run(new OpenUrlAction((context, arguments, additional) -> "  ", false), value));
		assertNull(run(new OpenUrlAction(null, true), null));

		assertTrue("Nothing is opened.", _window._events.isEmpty());
	}

	private OpenUrlEvent singleEvent() {
		assertEquals(1, _window._events.size());
		return (OpenUrlEvent) _window._events.get(0);
	}

	/** Runs the given action on the given value, returning the value the chain goes on with. */
	private Object run(OpenUrlAction action, Object value) {
		Object[] result = new Object[1];
		ViewActionChain.run(context(), List.of(action), ActionArguments.of("company", value),
			done -> result[0] = done);
		return result[0];
	}
}
