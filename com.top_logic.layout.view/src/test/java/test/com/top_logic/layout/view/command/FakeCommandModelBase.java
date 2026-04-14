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
 * Minimal base {@link CommandModel} stub used by context-menu tests. Subclasses override behavior
 * as required.
 */
class FakeCommandModelBase implements CommandModel {

	private final String _name;

	FakeCommandModelBase(String name) {
		_name = name;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public String getLabel() {
		return _name;
	}

	@Override
	public ThemeImage getImage() {
		return null;
	}

	@Override
	public boolean isExecutable() {
		return true;
	}

	@Override
	public boolean isVisible() {
		return true;
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
}
