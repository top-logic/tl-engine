/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import com.top_logic.ai.service.agents.AgentRegistry;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;

/**
 * Configuration for a single skill in the {@link AgentRegistry}.
 *
 * <p>
 * A skill is an instruction fragment that can be injected into an AI agent's context, either
 * automatically via the system prompt auto or on demand via a tool call on demand.
 * </p>
 */
@DisplayOrder({
	SkillConfig.NAME_ATTRIBUTE,
	SkillConfig.DESCRIPTION,
	SkillConfig.MODE,
	SkillConfig.CONTENT,
})
public interface SkillConfig extends NamedConfigMandatory {

	/** @see #getDescription() */
	String DESCRIPTION = "description";

	/** @see #getMode() */
	String MODE = "mode";

	/** @see #getContent() */
	String CONTENT = "content";

	/**
	 * Human-readable description of this skill's purpose.
	 */
	@Name(DESCRIPTION)
	@Mandatory
	String getDescription();

	/**
	 * Whether this skill is injected automatically into the system prompt auto or loaded on demand
	 * via tool call on demand.
	 */
	@Name(MODE)
	@Mandatory
	Skill.Mode getMode();

	/**
	 * The actual prompt text of this skill.
	 */
	@Name(CONTENT)
	@Mandatory
	@ControlProvider(MultiLineText.class)
	String getContent();
}
