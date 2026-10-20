/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
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
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/**
 * {@link ListGeneratorAdaptor} providing all available tool names as options.
 *
 * <p>
 * Provides the options for the {@code tools} attribute of {@code tl.ai.core:Agent}.
 * </p>
 *
 * @see ToolRegistryService#getToolNames()
 */
@InApp
public class AllToolNamesGenerator extends ListGeneratorAdaptor {

	/**
	 * Configuration for {@link AllToolNamesGenerator}.
	 */
	@TagName("all-tool-names")
	public interface Config extends PolymorphicConfiguration<AllToolNamesGenerator> {
		// No properties.
	}

	/** */
	@SuppressWarnings("unused")
	@CalledByReflection
	public AllToolNamesGenerator(InstantiationContext context, Config config) {
		// No-op.
	}

	@Override
	public List<?> generateList(EditContext editContext) {
		if (!ToolRegistryService.Module.INSTANCE.isActive()) {
			return Collections.emptyList();
		}
		return new ArrayList<>(ToolRegistryService.getInstance().getToolNames());
	}
}
