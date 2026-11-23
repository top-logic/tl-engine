/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.agent;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.ListGeneratorAdaptor;
import com.top_logic.ai.core.security.ModelAccessRightsSecurity;
import com.top_logic.util.TLContext;

/**
 * {@link ListGeneratorAdaptor} providing all available skill names as options.
 *
 * <p>
 * Provides the options for the {@code skills} attribute of {@code tl.ai.core:Agent}.
 * </p>
 *
 * <p>
 * Only the skills the editing account may know are offered: a stored skill it may not read is not
 * an option (developer article {@code ai/security/run-access}). A name the record already holds
 * stays an option, so that saving the form does not drop what another editor set.
 * </p>
 *
 * @see AgentDefinitionQuery#getAllSkills(com.top_logic.ai.core.security.ModelSecurity)
 */
@InApp
public class AllSkillNamesGenerator extends ListGeneratorAdaptor {

	/**
	 * Configuration for {@link AllSkillNamesGenerator}.
	 */
	@TagName("all-skill-names")
	public interface Config extends PolymorphicConfiguration<AllSkillNamesGenerator> {
		// No properties.
	}

	/** */
	@SuppressWarnings("unused")
	@CalledByReflection
	public AllSkillNamesGenerator(InstantiationContext context, Config config) {
		// No-op.
	}

	@Override
	public List<?> generateList(EditContext editContext) {
		List<String> names = new ArrayList<>();
		ModelAccessRightsSecurity editor = new ModelAccessRightsSecurity(TLContext.currentUser());
		for (SkillDefinition sd : AgentDefinitionQuery.getInstance().getAllSkills(editor)) {
			names.add(sd.name());
		}
		return AllAgentNamesGenerator.withHeld(names, editContext.getCorrectValues());
	}
}
