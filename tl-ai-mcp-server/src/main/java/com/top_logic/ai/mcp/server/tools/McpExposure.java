/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.tools;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/**
 * Which tools of the {@link ToolRegistryService} the MCP server publishes, and how.
 *
 * <p>
 * Nothing is published that the configuration does not name: an empty {@code <exposed-tools>}
 * publishes no tool at all. A tool that is not published is not registered with the MCP server, so
 * {@code tools/list} does not show it and a {@code tools/call} that names it is answered as a call to
 * an unknown tool - by the SDK, before any code of this application runs.
 * </p>
 *
 * <p>
 * Publishing a tool decides nothing about who may call it. Every call is still authorized against the
 * calling account's roles and the background allowlist, exactly as before - see
 * {@link McpToolAdapter}.
 * </p>
 *
 * @see ExposedTool
 */
public final class McpExposure {

	/**
	 * What an MCP tool name may be.
	 *
	 * <p>
	 * The intersection of what the MCP specification allows and what the model APIs behind the
	 * clients accept: Anthropic's API takes {@code ^[a-zA-Z0-9_-]{1,64}$} for a tool name and refuses
	 * the dots the specification would permit, and the Claude connector directory asks for at most 64
	 * characters.
	 * </p>
	 */
	static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_-]{1,64}");

	/**
	 * One published tool.
	 *
	 * @param configName
	 *        The qualified name the tool is configured under, {@code factory.tool}.
	 * @param tool
	 *        The tool.
	 * @param name
	 *        The name an MCP client calls it by.
	 * @param title
	 *        The name a person reads.
	 * @param description
	 *        What the external model is told it does.
	 */
	public record Published(String configName, Tool tool, String name, String title, String description) {
		// Pure value.
	}

	/**
	 * The tools of the running application that the given configuration publishes.
	 *
	 * @param entries
	 *        The configured {@code <exposed-tools>}.
	 * @return The published tools, ordered by name; empty if the {@link ToolRegistryService} is not
	 *         running.
	 * @throws IllegalArgumentException
	 *         If the configuration is inconsistent - see {@link #resolve(List, Map, Function)}.
	 */
	public static List<Published> resolve(List<? extends ExposedTool> entries) {
		if (!ToolRegistryService.Module.INSTANCE.isActive()) {
			if (!entries.isEmpty()) {
				Logger.warn("ToolRegistryService is not active, so the MCP server publishes none of the"
					+ " configured <exposed-tools>.", McpExposure.class);
			}
			return List.of();
		}

		ToolRegistryService registry = ToolRegistryService.getInstance();
		Map<String, Tool> tools = new LinkedHashMap<>();
		for (String providerName : registry.getToolNames()) {
			String configName = registry.getConfigName(providerName);
			tools.put(configName != null ? configName : providerName, registry.getTool(providerName));
		}
		return resolve(entries, tools, McpExposure::groupOf);
	}

	/**
	 * The group a tool belongs to for an entry that names a group.
	 *
	 * <p>
	 * The same answer the agent tool access policy gives, so that publishing a group and granting it
	 * to a role mean the same set of tools: the policy's own assignment first, else the group the tool
	 * declares.
	 * </p>
	 */
	private static String groupOf(String configName) {
		if (AiToolAccessService.Module.INSTANCE.isActive()) {
			return AiToolAccessService.getInstance().getAccess().groupOf(configName);
		}
		Tool tool = ToolRegistryService.getInstance().getTool(configName);
		return tool == null ? null : StringServices.nonEmpty(tool.group());
	}

	/**
	 * The tools of the given set that the given configuration publishes.
	 *
	 * <p>
	 * A tool entry that names a tool the application does not have is reported and skipped: it widens
	 * nothing, and the tool may come from a module this deployment leaves out. Everything else that
	 * does not add up is refused, because a publication an administrator did not mean is the one
	 * mistake this configuration must not make quietly.
	 * </p>
	 *
	 * @param entries
	 *        The configured {@code <exposed-tools>}.
	 * @param tools
	 *        The tools that can be published, by their qualified name {@code factory.tool}.
	 * @param groupOf
	 *        The group of a tool, by its qualified name; <code>null</code> for an ungrouped tool.
	 * @return The published tools, ordered by name.
	 * @throws IllegalArgumentException
	 *         If an entry names both a tool and a group, or neither; if a group entry sets a name,
	 *         title or description; if a tool is named by two entries; if a name is not a valid MCP
	 *         tool name; or if two published tools would have the same name.
	 */
	public static List<Published> resolve(List<? extends ExposedTool> entries, Map<String, Tool> tools,
			Function<String, String> groupOf) {
		Map<String, ExposedTool> byTool = new LinkedHashMap<>();
		List<String> groups = new ArrayList<>();
		for (ExposedTool entry : entries) {
			String toolName = StringServices.nonEmpty(entry.getTool());
			String group = StringServices.nonEmpty(entry.getGroup());
			if ((toolName == null) == (group == null)) {
				throw new IllegalArgumentException("An <exposed-tool> names either a tool or a group,"
					+ " found tool='" + StringServices.nonNull(toolName) + "' and group='"
					+ StringServices.nonNull(group) + "'.");
			}
			if (group != null) {
				if (!StringServices.isEmpty(entry.getName()) || !StringServices.isEmpty(entry.getTitle())
					|| !StringServices.isEmpty(entry.getDescription())) {
					throw new IllegalArgumentException("The <exposed-tool group=\"" + group + "\"> sets a"
						+ " name, title or description, which only an entry for a single tool can do.");
				}
				groups.add(group);
				continue;
			}
			if (byTool.put(toolName, entry) != null) {
				throw new IllegalArgumentException("The tool '" + toolName + "' is listed twice in"
					+ " <exposed-tools>.");
			}
		}

		List<Published> result = new ArrayList<>();
		for (Map.Entry<String, ExposedTool> explicit : byTool.entrySet()) {
			String configName = explicit.getKey();
			Tool tool = tools.get(configName);
			if (tool == null) {
				Logger.error("The MCP server cannot publish the tool '" + configName + "' named in"
					+ " <exposed-tools>: the application has no such tool.", McpExposure.class);
				continue;
			}
			result.add(publish(configName, tool, explicit.getValue()));
		}
		for (String group : groups) {
			int before = result.size();
			for (Map.Entry<String, Tool> candidate : tools.entrySet()) {
				String configName = candidate.getKey();
				if (!byTool.containsKey(configName) && group.equals(groupOf.apply(configName))) {
					result.add(publish(configName, candidate.getValue(), null));
				}
			}
			if (result.size() == before) {
				Logger.warn("The tool group '" + group + "' named in <exposed-tools> has no tool"
					+ " that is not already listed on its own.", McpExposure.class);
			}
		}

		result.sort(Comparator.comparing(Published::name));
		Map<String, String> owners = new HashMap<>();
		for (Published published : result) {
			String clash = owners.put(published.name(), published.configName());
			if (clash != null) {
				throw new IllegalArgumentException("The tools '" + clash + "' and '"
					+ published.configName() + "' would both be published as '" + published.name()
					+ "'. Give one of them its own name in <exposed-tools>.");
			}
		}
		return result;
	}

	private static Published publish(String configName, Tool tool, ExposedTool entry) {
		String name = entry == null ? null : StringServices.nonEmpty(entry.getName());
		if (name == null) {
			name = ToolRegistryService.toProviderName(configName);
		}
		if (!VALID_NAME.matcher(name).matches()) {
			throw new IllegalArgumentException("The tool '" + configName + "' cannot be published as '"
				+ name + "': an MCP tool name has at most 64 characters out of letters, digits, '_' and"
				+ " '-'. Set a name for it in <exposed-tools>.");
		}

		String title = entry == null ? null : StringServices.nonEmpty(entry.getTitle());
		if (title == null) {
			title = titleOf(tool.name());
		}

		String description = entry == null ? null : StringServices.nonEmpty(entry.getDescription());
		if (description == null) {
			description = tool.description();
		}

		return new Published(configName, tool, name, title, StringServices.nonNull(description).strip());
	}

	/**
	 * A readable title for a camel-case tool name.
	 *
	 * <p>
	 * {@code listModules} becomes "List modules", and an acronym keeps its capitals:
	 * {@code getTLScriptReference} becomes "Get TL script reference".
	 * </p>
	 */
	static String titleOf(String toolName) {
		String[] words = toolName
			.split("(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])|[_\\-\\s]+");
		StringBuilder result = new StringBuilder();
		for (String word : words) {
			if (word.isEmpty()) {
				continue;
			}
			boolean acronym = word.length() > 1 && word.equals(word.toUpperCase());
			if (result.length() == 0) {
				result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
			} else {
				result.append(' ').append(acronym ? word : word.toLowerCase());
			}
		}
		return result.toString();
	}

	private McpExposure() {
		// Static utility.
	}
}
