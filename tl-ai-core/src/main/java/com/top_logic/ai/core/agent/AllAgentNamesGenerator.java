/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.ListGeneratorAdaptor;
import com.top_logic.ai.core.security.ModelSecurity;
import com.top_logic.util.TLContext;

/**
 * {@link ListGeneratorAdaptor} providing all available agent names as options.
 *
 * <p>
 * Provides the options for the {@code delegations} attribute of {@code tl.ai.core:Agent}.
 * </p>
 *
 * <p>
 * An agent marked as infrastructure is not offered: the application runs it for a user, and the
 * gate refuses a delegation to it, so an option naming one would be an option that never works.
 * </p>
 *
 * <p>
 * Only the agents the editing account may know are offered: a stored agent it may not read is not
 * an option (developer article {@code ai/security/run-access}). A name the record already holds
 * stays an option, so that saving the form does not drop what another editor set.
 * </p>
 *
 * @see AgentDefinition#infrastructure()
 * @see AgentDefinitionQuery#getAllAgents(com.top_logic.ai.core.security.ModelSecurity)
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
		ModelSecurity editor = ModelSecurity.forAccount(TLContext.currentUser());
		for (AgentDefinition ad : AgentDefinitionQuery.getInstance().getAllAgents(editor)) {
			if (ad.infrastructure()) {
				continue;
			}
			names.add(ad.name());
		}
		return withHeld(names, editContext.getCorrectValues());
	}

	/**
	 * The given options, plus the names the edited attribute holds already.
	 *
	 * <p>
	 * A value that is not an option is dropped when the form is saved. An editor who may not read a
	 * stored agent or skill would thus remove it from a record that another editor gave it.
	 * </p>
	 *
	 * @param held
	 *        The value of the edited attribute ({@link EditContext#getCorrectValues()}).
	 */
	static List<String> withHeld(List<String> options, Object held) {
		if (held instanceof Collection<?> values) {
			for (Object value : values) {
				if (value instanceof String name && !options.contains(name)) {
					options.add(name);
				}
			}
		}
		return options;
	}
}
