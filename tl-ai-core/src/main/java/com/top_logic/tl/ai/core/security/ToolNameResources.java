/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/**
 * How a tool presents itself where an administrator has to pick one for a {@link ToolAccessPolicy}.
 *
 * <p>
 * The label is the tool's qualified name, unchanged: it is what the policy stores, what an operator
 * reads in a denial message and in the log, and therefore the only label that lets the two be matched
 * up. What the list adds is the tooltip - the tool's own description, the same sentence the agent is
 * given for it, which is what says whether this is the tool an administrator meant.
 * </p>
 *
 * <p>
 * A tool that is not in the registry keeps its name and explains nothing: an entry authored ahead of its
 * tool, and a tool of an MCP server that is currently offline, are both legal and both undescribed.
 * </p>
 *
 * @see AllToolNames
 */
public class ToolNameResources extends AbstractResourceProvider {

	@Override
	public String getLabel(Object object) {
		return toolName(object);
	}

	@Override
	public String getTooltip(Object object) {
		String name = toolName(object);
		if (name == null || !ToolRegistryService.Module.INSTANCE.isActive()) {
			return null;
		}
		Tool tool = ToolRegistryService.getInstance().getTool(name);
		if (tool == null || StringServices.isEmpty(tool.description())) {
			return null;
		}
		// A tool description is plain text written for a language model, so it may well contain
		// characters that the editor's safe-HTML renderer would reject as markup.
		return TagUtil.encodeXML(tool.description());
	}

	private static String toolName(Object object) {
		return object instanceof String name ? name : null;
	}

}
