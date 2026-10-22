/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.top_logic.basic.func.Function0;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/**
 * The tool groups an administrator can pick from when writing a {@link ToolAccessPolicy}.
 *
 * <p>
 * Group names are an open string space by design, so this is a list of what is <em>known</em> rather
 * than of what is legal: the groups the framework ships, the groups the registered tools declare, and
 * the groups the policy in effect assigns tools to. A name outside all three is almost always a typo,
 * which is what {@code AiToolAccessService} warns about at boot.
 * </p>
 *
 * @see ToolGroups#ALL
 */
public class AllToolGroups extends Function0<List<String>> {

	@Override
	public List<String> apply() {
		Set<String> groups = new TreeSet<>(ToolGroups.ALL);
		if (ToolRegistryService.Module.INSTANCE.isActive()) {
			for (Tool tool : ToolRegistryService.getInstance().getTools()) {
				if (!tool.group().isBlank()) {
					groups.add(tool.group());
				}
			}
		}
		if (AiToolAccessService.Module.INSTANCE.isActive()) {
			groups.addAll(AiToolAccessService.policy().getToolGroups().keySet());
		}
		return new ArrayList<>(groups);
	}

}
