/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.func.Function0;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/**
 * {@link NameRef} subtype providing available tool names as options.
 *
 * @see TopLogicAgentDefinitions.AgentConfig#getTools()
 * @see TopLogicAgentDefinitions.SkillConfig#getTools()
 */
public interface ToolRef extends NameRef {

	@Override
	@Options(fun = AllToolNameOptions.class)
	String getName();

	/**
	 * Provides all registered tool names for selection in the service editor.
	 */
	class AllToolNameOptions extends Function0<List<String>> {
		@Override
		public List<String> apply() {
			if (!ToolRegistryService.Module.INSTANCE.isActive()) {
				return Collections.emptyList();
			}
			return new ArrayList<>(ToolRegistryService.getInstance().getToolNames());
		}
	}
}
