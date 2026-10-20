/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.top_logic.basic.func.Function0;
import com.top_logic.tl.ai.core.mcp.McpServerRegistry;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.BackgroundAllowlist;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Grant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.GroupMembership;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.RoleAccess;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/**
 * The tools an administrator can pick from when writing a {@link ToolAccessPolicy}.
 *
 * <p>
 * Everything the application can call right now - the annotated Java tools, the TL-Script tools of every
 * configured factory as {@code {factory}.{tool}}, and the tools of every connected MCP server under
 * their runtime names - plus every tool name the policy in effect already stores.
 * </p>
 *
 * <p>
 * That second half is not a nicety. A tool of an MCP server that happens to be offline is absent from
 * the first half, and so is a tool an administrator wrote the policy for before creating it. Offering
 * only what is reachable would let an open-and-save of the editor drop those entries, quietly narrowing
 * a policy that was correct.
 * </p>
 *
 * @see ToolNameResources
 */
public class AllToolNames extends Function0<List<String>> {

	@Override
	public List<String> apply() {
		return union(callableToolNames(), AiToolAccessService.policy());
	}

	/** Every tool the application can call, by the name a policy refers to it with. */
	private static Collection<String> callableToolNames() {
		List<String> names = new ArrayList<>();
		if (ToolRegistryService.Module.INSTANCE.isActive()) {
			ToolRegistryService registry = ToolRegistryService.getInstance();
			for (String providerName : registry.getToolNames()) {
				// The dot-notation name, not the provider-safe registry key: it is the name an
				// administrator writes and the one a denial message shows.
				String configName = registry.getConfigName(providerName);
				names.add(configName != null ? configName : providerName);
			}
		}
		if (McpServerRegistry.Module.INSTANCE.isActive()) {
			McpServerRegistry mcp = McpServerRegistry.getInstance();
			for (String serverName : mcp.getServerNames()) {
				// The cached tool list of a connected server; a disconnected one contributes nothing
				// here and relies on the policy's own entries below.
				List<Tool> tools = mcp.getTools(serverName);
				if (tools != null) {
					for (Tool tool : tools) {
						names.add(tool.name());
					}
				}
			}
		}
		return names;
	}

	/**
	 * The tools to offer: the ones that can be called, and the ones the policy already names.
	 *
	 * @param callableTools
	 *        The names of the tools the application can call.
	 * @param policy
	 *        The policy in effect, may be <code>null</code> if the service is not running.
	 * @return The option list, alphabetically and without duplicates.
	 */
	static List<String> union(Collection<String> callableTools, ToolAccessPolicy policy) {
		Set<String> names = new TreeSet<>(callableTools);
		if (policy != null) {
			for (GroupMembership group : policy.getToolGroups().values()) {
				names.addAll(group.getTools());
			}
			if (policy.getBaseline() != null) {
				addNamedTools(names, policy.getBaseline().getGrants());
			}
			BackgroundAllowlist allowlist = policy.getBackgroundAllowlist();
			if (allowlist != null) {
				addNamedTools(names, allowlist.getGrants());
			}
			for (RoleAccess role : policy.getToolAccess().values()) {
				addNamedTools(names, role.getGrants());
				addNamedTools(names, role.getDenies());
			}
		}
		return new ArrayList<>(names);
	}

	private static void addNamedTools(Set<String> names, Collection<Grant> entries) {
		for (Grant entry : entries) {
			names.addAll(entry.getTools());
		}
	}

}
