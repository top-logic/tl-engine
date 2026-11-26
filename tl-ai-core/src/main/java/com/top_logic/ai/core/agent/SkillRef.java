/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.agent;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.ai.core.security.ModelSecurity;
import com.top_logic.basic.func.Function0;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * {@link NameRef} subtype providing available skill names as options.
 *
 * @see TopLogicAgentDefinitions.AgentConfig#getSkills()
 */
public interface SkillRef extends NameRef {

	@Override
	@Options(fun = AllSkillNameOptions.class)
	String getName();

	/**
	 * Provides all available skill names for selection in the service editor.
	 */
	class AllSkillNameOptions extends Function0<List<String>> {
		@Override
		public List<String> apply() {
			if (!TopLogicAgentDefinitions.Module.INSTANCE.isActive()) {
				return List.of();
			}
			List<String> names = new ArrayList<>();
			// The configuration holds for every account, so it may name every skill.
			for (SkillDefinition sd : AgentDefinitionQuery.getInstance().getAllSkills(ModelSecurity.UNCHECKED)) {
				names.add(sd.name());
			}
			return names;
		}
	}
}
