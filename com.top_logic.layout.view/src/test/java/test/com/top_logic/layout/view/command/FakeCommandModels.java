/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Test helper producing simple {@link CommandModel} stubs for context-menu tests.
 */
final class FakeCommandModels {

	static CommandModel contextMenu(String name, String label, boolean visible, boolean executable) {
		return new CommandModel() {
			@Override
			public String getName() {
				return name;
			}

			@Override
			public String getLabel() {
				return label;
			}

			@Override
			public ThemeImage getImage() {
				return null;
			}

			@Override
			public boolean isExecutable() {
				return executable;
			}

			@Override
			public boolean isVisible() {
				return visible;
			}

			@Override
			public HandlerResult executeCommand(ReactContext context) {
				return HandlerResult.DEFAULT_RESULT;
			}

			@Override
			public String getPlacement() {
				return PLACEMENT_CONTEXT_MENU;
			}

			@Override
			public void addStateChangeListener(Runnable listener) {
				// No-op for fake.
			}

			@Override
			public void removeStateChangeListener(Runnable listener) {
				// No-op for fake.
			}
		};
	}

	private FakeCommandModels() {
		// Utility class.
	}
}
