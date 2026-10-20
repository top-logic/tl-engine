/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/**
 * How a tool is named and classified when the access policy is asked about it.
 *
 * <p>
 * A tool has <b>more than one</b> name, and a per-tool grant must accept all of them. An administrator
 * writes {@code {factory}.{tool}} in the policy, which is also what an agent definition lists and what
 * a TL-Script caller types. The registry keys tools by the provider-safe {@code factory__tool} form of
 * that. The agent's own catalog keys them by the bare {@link Tool#name()}, which is what the model
 * calls and what a tool call carries. Matching only one of the three would make a correctly spelled
 * grant silently miss - the failure mode the design forbids - so a grant matches any of them.
 * </p>
 *
 * @param name
 *        The name the run uses for the tool: {@link Tool#name()}, i.e. what the model calls and what
 *        appears in denial messages. Never <code>null</code>.
 * @param qualifiedName
 *        The configured {@code {factory}.{tool}} name in dot notation - the name an administrator
 *        writes in the policy - or <code>null</code> for a tool that is not registered globally (a
 *        remote MCP tool, a delegation tool, a skill-injected tool).
 * @param declaredGroup
 *        The group the tool declares for itself ({@link Tool#group()}), or <code>null</code> if it
 *        declares none. The policy's own {@code <tool-groups>} assignment overrides this.
 * @see RoleToolAccess#check(java.util.Collection, ToolIdentity)
 */
public record ToolIdentity(String name, String qualifiedName, String declaredGroup) {

	/** */
	public ToolIdentity {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("A tool identity needs a name.");
		}
		qualifiedName = blankToNull(qualifiedName);
		declaredGroup = blankToNull(declaredGroup);
	}

	/**
	 * The identity of a tool that is known only by name - no registry entry, hence no group.
	 *
	 * <p>
	 * Ungrouped is the fail-closed reading: only an explicit per-tool grant reaches such a tool.
	 * </p>
	 *
	 * @param name
	 *        The tool name as the run knows it.
	 */
	public static ToolIdentity of(String name) {
		return new ToolIdentity(name, null, null);
	}

	/**
	 * The identity of a tool object, without a qualified name.
	 *
	 * <p>
	 * Use this where the registry is unavailable; {@code AiToolAccessService} resolves the qualified
	 * name where it is.
	 * </p>
	 *
	 * @param tool
	 *        The tool to identify.
	 */
	public static ToolIdentity of(Tool tool) {
		return new ToolIdentity(tool.name(), null, tool.group());
	}

	/**
	 * Every name by which a per-tool grant may refer to this tool, without duplicates.
	 *
	 * <p>
	 * Three forms, because all three occur in practice: the bare {@link Tool#name()} the model calls,
	 * the {@code {factory}.{tool}} name an administrator writes in configuration, and the
	 * provider-safe {@code factory__tool} form the registry keys tools by and an MCP-facing name uses.
	 * A grant naming any of them reaches the tool.
	 * </p>
	 */
	public List<String> names() {
		if (qualifiedName == null) {
			return List.of(name);
		}
		List<String> result = new ArrayList<>(3);
		result.add(name);
		addDistinct(result, qualifiedName);
		addDistinct(result, ToolRegistryService.toProviderName(qualifiedName));
		return result;
	}

	private static void addDistinct(List<String> names, String candidate) {
		if (candidate != null && !names.contains(candidate)) {
			names.add(candidate);
		}
	}

	/**
	 * The most specific name available, for log and denial messages.
	 */
	public String displayName() {
		return qualifiedName != null ? qualifiedName : name;
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value;
	}
}
