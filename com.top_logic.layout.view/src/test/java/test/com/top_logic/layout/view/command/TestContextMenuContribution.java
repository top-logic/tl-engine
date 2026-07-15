/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import junit.framework.TestCase;

import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.overlay.ContextMenuContribution;

/**
 * Tests for {@link ContextMenuContribution}.
 */
public class TestContextMenuContribution extends TestCase {

	public void testHoldsTargetAndCommands() {
		AtomicReference<Object> target = new AtomicReference<>();
		Consumer<Object> setter = target::set;
		CommandModel cmd = FakeCommandModels.contextMenu("edit", "Edit", true, true);
		ContextMenuContribution contribution =
			new ContextMenuContribution(setter, List.of(cmd));

		assertSame(setter, contribution.setTarget());
		assertEquals(List.of(cmd), contribution.commands());

		contribution.setTarget().accept("value");
		assertEquals("value", target.get());
	}

	public void testExecutableCommandsFiltersDisabledAndInvisible() {
		AtomicReference<Object> target = new AtomicReference<>();
		Consumer<Object> setter = target::set;
		CommandModel visibleEnabled = FakeCommandModels.contextMenu("a", "A", true, true);
		CommandModel disabledVisible = FakeCommandModels.contextMenu("b", "B", true, false);
		CommandModel invisible = FakeCommandModels.contextMenu("c", "C", false, true);

		ContextMenuContribution contribution =
			new ContextMenuContribution(setter, List.of(visibleEnabled, disabledVisible, invisible));

		assertEquals(
			List.of(visibleEnabled, disabledVisible),
			contribution.visibleCommands());
	}
}
