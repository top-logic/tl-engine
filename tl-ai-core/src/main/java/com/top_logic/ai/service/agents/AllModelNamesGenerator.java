/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.ListGeneratorAdaptor;

/**
 * {@link ListGeneratorAdaptor} providing all available AI model names as options.
 *
 * <p>
 * Used as {@code <options>} generator on the {@code model} attribute of
 * {@code tl.ai.core:Agent}.
 * </p>
 *
 * @see LLMProviderRegistry#getModelNames()
 */
@InApp
public class AllModelNamesGenerator extends ListGeneratorAdaptor {

	/**
	 * Configuration for {@link AllModelNamesGenerator}.
	 */
	@TagName("all-model-names")
	public interface Config extends PolymorphicConfiguration<AllModelNamesGenerator> {
		// No properties.
	}

	/** */
	@CalledByReflection
	public AllModelNamesGenerator(InstantiationContext context, Config config) {
		// No-op.
	}

	@Override
	public List<?> generateList(EditContext editContext) {
		LLMProviderRegistry registry = LLMProviderRegistry.getInstance();
		if (registry == null) {
			return Collections.emptyList();
		}
		return new ArrayList<>(registry.getModelNames());
	}
}
