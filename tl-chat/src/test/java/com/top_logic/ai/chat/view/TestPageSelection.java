/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * {@code <page-selection>} and {@code <chat-channels>} (#928): the shell channel names the selection
 * of a page that is on screen, and nothing else.
 */
public class TestPageSelection extends TestCase {

	private static final String SHELL = "pageSelection";

	private final AtomicInteger _ids = new AtomicInteger();

	private TypedAnnotatable _window;

	private ViewChannel _shell;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_window = new LazyTypedAnnotatable();
		_shell = new DefaultViewChannel(SHELL);
		ChatChannels.publish(_window, SHELL, _shell);
	}

	/**
	 * The first input that holds one object wins. A set of one object is that object, a set of
	 * several names no single object and is skipped - the shell channel never gets a list.
	 */
	public void testTheFirstInputThatHoldsOneObjectWins() {
		assertEquals("document", PageSelectionControl.selectionOf(Arrays.asList(null, "document")));
		assertEquals("section", PageSelectionControl.selectionOf(List.of("section", "document")));
		assertEquals("row", PageSelectionControl.selectionOf(List.of(Set.of("row"), "document")));
		assertEquals("document", PageSelectionControl.selectionOf(List.of(Set.of("a", "b"), "document")));
		assertNull(PageSelectionControl.selectionOf(Arrays.asList(Set.of(), null)));
	}

	/** The value comes when the page is displayed and goes when it leaves the screen. */
	public void testTheSelectionIsGoneWhenThePageLeavesTheScreen() {
		ViewChannel selected = new DefaultViewChannel("selected");
		selected.set("order 1");
		PageSelectionControl page = page(_window, selected);
		assertNull("Built, not displayed yet.", _shell.get());

		page.attach();
		assertEquals("order 1", _shell.get());

		selected.set("order 2");
		assertEquals("A new selection follows.", "order 2", _shell.get());

		selected.set(null);
		assertNull("Nothing selected on the page.", _shell.get());

		selected.set("order 3");
		page.detach();
		assertNull("Another nav item, or a frame over the page.", _shell.get());

		selected.set("order 4");
		assertNull("A page that is not on screen writes nothing.", _shell.get());

		page.attach();
		assertEquals("Back on screen.", "order 4", _shell.get());

		page.cleanupTree();
		assertNull("Disposed.", _shell.get());
	}

	/**
	 * Two pages of one window on screen: the most recent selection wins, and a page that leaves gives
	 * the channel back to the other one instead of clearing it.
	 */
	public void testTwoPagesInOneWindow() {
		ViewChannel left = new DefaultViewChannel("left");
		ViewChannel right = new DefaultViewChannel("right");
		PageSelectionControl leftPage = page(_window, left);
		PageSelectionControl rightPage = page(_window, right);
		leftPage.attach();
		rightPage.attach();

		left.set("venue");
		right.set("room");
		assertEquals("room", _shell.get());

		left.set("other venue");
		assertEquals("other venue", _shell.get());

		leftPage.detach();
		assertEquals("The page still on screen.", "room", _shell.get());

		rightPage.detach();
		assertNull(_shell.get());
	}

	/**
	 * A second browser window has its own channels. A page that is displayed before the shell
	 * publishes its channel is not lost.
	 */
	public void testEachWindowHasItsOwnChannel() {
		TypedAnnotatable otherWindow = new LazyTypedAnnotatable();
		ViewChannel selected = new DefaultViewChannel("selected");
		selected.set("invoice");
		page(otherWindow, selected).attach();
		assertNull("Not the first window's channel.", _shell.get());

		ViewChannel otherShell = new DefaultViewChannel(SHELL);
		ChatChannels.publish(otherWindow, SHELL, otherShell);
		assertEquals("invoice", otherShell.get());
	}

	private PageSelectionControl page(TypedAnnotatable window, ViewChannel input) {
		return new PageSelectionControl(context(), window, SHELL, List.of(input));
	}

	/** A context that allocates control ids and has no browser window to send updates to. */
	private ReactContext context() {
		return (ReactContext) Proxy.newProxyInstance(TestPageSelection.class.getClassLoader(),
			new Class<?>[] { ReactContext.class }, (proxy, method, args) -> switch (method.getName()) {
				case "allocateId" -> "c" + _ids.incrementAndGet();
				case "getSSEQueue" -> null;
				default -> throw new UnsupportedOperationException(method.getName());
			});
	}

}
