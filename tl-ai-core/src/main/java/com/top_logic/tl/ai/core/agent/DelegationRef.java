/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.func.Function0;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * {@link NameRef} subtype providing available agent names as delegation options.
 *
 * @see TopLogicAgentDefinitions.AgentConfig#getDelegations()
 */
public interface DelegationRef extends NameRef {

	@Override
	@Options(fun = AllAgentNameOptions.class)
	String getName();

	/**
	 * Provides all available agent names for selection in the service editor.
	 */
	class AllAgentNameOptions extends Function0<List<String>> {
		@Override
		public List<String> apply() {
			if (!TopLogicAgentDefinitions.Module.INSTANCE.isActive()) {
				return List.of();
			}
			List<String> names = new ArrayList<>();
			for (AgentDefinition ad : AgentDefinitionQuery.getInstance().getAllAgents()) {
				names.add(ad.name());
			}
			return names;
		}
	}
}
