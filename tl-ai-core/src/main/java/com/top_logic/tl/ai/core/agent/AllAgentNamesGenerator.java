/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.ListGeneratorAdaptor;

/**
 * {@link ListGeneratorAdaptor} providing all available agent names as options.
 *
 * <p>
 * Used as {@code <options>} generator on the {@code delegations} attribute of
 * {@code tl.ai.core:Agent}.
 * </p>
 *
 * @see AgentDefinitionQuery#getAllAgents()
 */
@InApp
public class AllAgentNamesGenerator extends ListGeneratorAdaptor {

	/**
	 * Configuration for {@link AllAgentNamesGenerator}.
	 */
	@TagName("all-agent-names")
	public interface Config extends PolymorphicConfiguration<AllAgentNamesGenerator> {
		// No properties.
	}

	/** */
	@SuppressWarnings("unused")
	@CalledByReflection
	public AllAgentNamesGenerator(InstantiationContext context, Config config) {
		// No-op.
	}

	@Override
	public List<?> generateList(EditContext editContext) {
		List<String> names = new ArrayList<>();
		for (AgentDefinition ad : AgentDefinitionQuery.getInstance().getAllAgents()) {
			names.add(ad.name());
		}
		return names;
	}
}
