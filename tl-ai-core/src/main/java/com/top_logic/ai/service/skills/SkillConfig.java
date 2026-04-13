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
import com.top_logic.layout.form.values.edit.annotation.LabelPositioning;
import com.top_logic.layout.form.values.edit.annotation.RenderWholeLine;
import com.top_logic.model.annotate.LabelPosition;

/**
 * Configuration for a single skill in the {@link AgentRegistry}.
 *
 * <p>
 * A skill is an instruction fragment that agents can load on demand via {@code loadSkill(id)}. The
 * skill's id and description appear in the system prompt catalog so the agent knows when to load it.
 * </p>
 */
@DisplayOrder({
	SkillConfig.NAME_ATTRIBUTE,
	SkillConfig.DESCRIPTION,
	SkillConfig.CONTENT,
})
public interface SkillConfig extends NamedConfigMandatory {

	/** @see #getDescription() */
	String DESCRIPTION = "description";

	/** @see #getContent() */
	String CONTENT = "content";

	/**
	 * Human-readable description of this skill's purpose.
	 */
	@Name(DESCRIPTION)
	@Mandatory
	@LabelPositioning(LabelPosition.ABOVE)
	String getDescription();

	/**
	 * The actual prompt text of this skill.
	 */
	@Name(CONTENT)
	@Mandatory
	@RenderWholeLine
	@ControlProvider(MultiLineText.class)
	@LabelPositioning(LabelPosition.ABOVE)
	String getContent();
}
