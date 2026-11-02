/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.servlet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.tools.CollectingLogListener;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.DeliveryScope;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests the work a {@link DeliveryScope} runs when the outermost interaction completes, registered
 * with {@link DeliveryScope#afterInteraction(Runnable)}.
 */
public class TestAfterInteraction extends TestCase {

	private final List<String> _log = new ArrayList<>();

	/** Work registered in a nested interaction waits for the outermost one to complete. */
	public void testRunsAtOutermostExit() {
		DeliveryScope.enter();
		try {
			DeliveryScope.enter();
			try {
				assertTrue(DeliveryScope.isOpen());
				DeliveryScope.afterInteraction(() -> _log.add("work"));
			} finally {
				DeliveryScope.exit();
			}
			assertEquals("The inner interaction is part of the outer one.", List.of(), _log);
		} finally {
			DeliveryScope.exit();
		}
		assertEquals(List.of("work"), _log);
		assertFalse(DeliveryScope.isOpen());

		DeliveryScope.enter();
		DeliveryScope.exit();
		assertEquals("Work runs once.", List.of("work"), _log);
	}

	/** Without an interaction there is nothing to wait for. */
	public void testRunsAtOnceOutsideAScope() {
		assertFalse(DeliveryScope.isOpen());
		DeliveryScope.afterInteraction(() -> _log.add("work"));

		assertEquals(List.of("work"), _log);
	}

	/**
	 * A failing work item is logged and keeps neither later work nor the delivery from happening;
	 * the updates of the work are settled with the interaction.
	 */
	public void testFailureIsolation() throws Exception {
		SSEUpdateQueue queue = new SSEUpdateQueue();
		CollectingLogListener errors = new CollectingLogListener(Set.of(Level.ERROR), true);
		try {
			ReactContext context = new DefaultReactContext("", "test", queue, new ReactWindowRegistry("test"));
			ReactControl displayed = new ReactControl(context, null, "TLPanel");
			displayed.write(new TagWriter());
			ReactControl removed = new ReactControl(context, null, "TLPanel");
			removed.write(new TagWriter());
			int before = queue.pendingEventCount();

			DeliveryScope.enter();
			try {
				DeliveryScope.afterInteraction(() -> {
					throw new IllegalStateException("failing work");
				});
				DeliveryScope.afterInteraction(() -> {
					_log.add("later");
					displayed.setHidden(true);
					removed.setHidden(true);
					removed.detach();
				});
			} finally {
				DeliveryScope.exit();
			}

			assertEquals("The later work ran.", List.of("later"), _log);
			assertEquals(
				"The update of the work is delivered with the interaction, settled like its own updates.",
				before + 1, queue.pendingEventCount());
			List<LogEntry> entries = errors.getLogEntries();
			assertEquals("The failure is logged.", 1, entries.size());
			assertTrue(entries.get(0).getException().get() instanceof IllegalStateException);
		} finally {
			errors.deactivate();
			queue.shutdown();
		}
	}

	/** Work registered while the queued work runs - also from a nested interaction - runs as well. */
	public void testWorkAddedDuringTheRun() {
		DeliveryScope.enter();
		try {
			DeliveryScope.afterInteraction(() -> {
				_log.add("first");
				DeliveryScope.afterInteraction(() -> _log.add("second"));
				DeliveryScope.enter();
				try {
					DeliveryScope.afterInteraction(() -> _log.add("third"));
				} finally {
					DeliveryScope.exit();
				}
				assertEquals("A nested interaction does not run the work.", List.of("first"), _log);
			});
		} finally {
			DeliveryScope.exit();
		}

		assertEquals(List.of("first", "second", "third"), _log);
	}

}
