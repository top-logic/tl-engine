/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorUtil;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ViewExecutabilityRule} that shows a command only while the application runs with UI
 * inspection switched on, hiding it otherwise.
 *
 * <p>
 * The gate of developer tooling - the UI inspector, and anything else that exposes how the UI is
 * built rather than what it shows. It is a deployment switch, not a permission: whoever works on a
 * system that has it on sees the tooling regardless of their roles, and nobody sees it on a system
 * that has it off.
 * </p>
 *
 * @implNote Asks {@link GuiInspectorUtil#isGuiInspectorEnabled()}, so a command gated by this rule
 *           appears under exactly the configuration that enables the inspection of the classic UI.
 */
public class InspectEnabled implements ViewExecutabilityRule {

	/**
	 * Configuration for {@link InspectEnabled}.
	 */
	@TagName("inspect-enabled")
	public interface Config extends ViewExecutabilityRule.Config {

		@Override
		@ClassDefault(InspectEnabled.class)
		Class<? extends ViewExecutabilityRule> getImplementationClass();
	}

	/**
	 * Creates a new {@link InspectEnabled} from configuration.
	 */
	@CalledByReflection
	public InspectEnabled(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public ExecutableState isExecutable(Object input) {
		return GuiInspectorUtil.isGuiInspectorEnabled() ? ExecutableState.EXECUTABLE
			: ExecutableState.NOT_EXEC_HIDDEN;
	}
}
