/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

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
import com.top_logic.tl.ai.core.provider.LLMProviderService;

/**
 * {@link ListGeneratorAdaptor} providing explicitly configured AI model names as options.
 *
 * <p>
 * Returns only models that an admin has added to a provider's {@code models} allowlist,
 * excluding models that were merely discovered via provider API. Provides the options for
 * the {@code model} attribute of {@code tl.ai.core:Agent}.
 * </p>
 *
 * @see LLMProviderService#getConfiguredModelNames()
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
	@SuppressWarnings("unused")
	@CalledByReflection
	public AllModelNamesGenerator(InstantiationContext context, Config config) {
		// No-op.
	}

	@Override
	public List<?> generateList(EditContext editContext) {
		LLMProviderService registry = LLMProviderService.getInstance();
		if (registry == null) {
			return Collections.emptyList();
		}
		return new ArrayList<>(registry.getConfiguredModelNames());
	}
}
