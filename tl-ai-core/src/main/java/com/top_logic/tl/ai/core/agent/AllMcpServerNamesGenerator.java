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
import com.top_logic.tl.ai.core.mcp.McpServerRegistry;

/**
 * {@link ListGeneratorAdaptor} providing all configured MCP server names as options.
 *
 * <p>
 * Used as {@code <options>} generator on the {@code mcpServers} attribute of
 * {@code tl.ai.core:Agent}.
 * </p>
 *
 * @see McpServerRegistry#getServerNames()
 */
@InApp
public class AllMcpServerNamesGenerator extends ListGeneratorAdaptor {

	/**
	 * Configuration for {@link AllMcpServerNamesGenerator}.
	 */
	@TagName("all-mcp-server-names")
	public interface Config extends PolymorphicConfiguration<AllMcpServerNamesGenerator> {
		// No properties.
	}

	/** */
	@SuppressWarnings("unused")
	@CalledByReflection
	public AllMcpServerNamesGenerator(InstantiationContext context, Config config) {
		// No-op.
	}

	@Override
	public List<?> generateList(EditContext editContext) {
		if (!McpServerRegistry.Module.INSTANCE.isActive()) {
			return Collections.emptyList();
		}
		return new ArrayList<>(McpServerRegistry.getInstance().getServerNames());
	}
}
