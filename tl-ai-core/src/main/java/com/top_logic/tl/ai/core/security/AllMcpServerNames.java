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
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.McpGrant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.McpRoleAccess;

/**
 * The MCP servers an administrator can pick from when writing a {@link ToolAccessPolicy}.
 *
 * <p>
 * Every server this application has configured - whether it is connected right now or not - plus every
 * server name the policy in effect already stores, so that opening the editor and saving it cannot
 * drop an entry written ahead of the server.
 * </p>
 *
 * @see AllAgentNames
 * @see AllModelNames
 */
public class AllMcpServerNames extends Function0<List<String>> {

	@Override
	public List<String> apply() {
		return union(configuredServerNames(), AiToolAccessService.policy());
	}

	/** The names of the servers the application has configured, empty while no registry runs. */
	static Collection<String> configuredServerNames() {
		if (!McpServerRegistry.Module.INSTANCE.isActive()) {
			return List.of();
		}
		return McpServerRegistry.getInstance().getConfiguredServerNames();
	}

	/**
	 * The servers to offer: the configured ones, and the ones the policy already names.
	 *
	 * @param configuredServers
	 *        The names of the servers the application has configured.
	 * @param policy
	 *        The policy in effect, may be <code>null</code> if the service is not running.
	 * @return The option list, alphabetically and without duplicates.
	 */
	static List<String> union(Collection<String> configuredServers, ToolAccessPolicy policy) {
		Set<String> names = new TreeSet<>();
		for (String name : configuredServers) {
			if (name != null && !name.isBlank()) {
				names.add(name);
			}
		}
		if (policy != null) {
			for (McpRoleAccess role : policy.getMcpAccess().getRoles().values()) {
				addNames(names, role.getGrants());
				addNames(names, role.getDenies());
			}
		}
		return new ArrayList<>(names);
	}

	private static void addNames(Set<String> names, Collection<McpGrant> entries) {
		for (McpGrant entry : entries) {
			names.addAll(entry.getServers());
		}
	}

}
