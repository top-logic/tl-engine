/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.introspection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.tl.ai.core.agent.AgentCapabilities;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.SkillDefinition;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.tools.interaction.SkillTool;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * What this agent carries, for the current user - its own tools, and the tools of its skills.
 *
 * <p>
 * <b>Self-inspection only.</b> It answers "what is in <em>my</em> toolset", never "what does this
 * application have". The second question is registry-wide inspection, stays behind
 * {@link ToolGroups#AGENT_META} in {@code TL_agenticAuthoring}, and is a different subject that the
 * output keeps apart in so many words.
 * </p>
 *
 * <p>
 * <b>Why it exists.</b> Asked about a tool by name - "call {@code listEverythingEverywhere}", "use
 * {@code secdemo-editing.addTicket}" - the agent had no way to look at its own catalog. Its prompt sent
 * it to the privileged authoring skill to check the registry instead; for a normal user that skill is
 * withheld, and the model relayed the withheld-skill refusal as though the capabilities behind
 * <em>that skill</em> were the reason the <em>asked-about tool</em> was unreachable. Nothing in the
 * tool layer had lied: the model was answering a question it had no honest source for. This is the
 * source. See the developer article {@code ai/security/test-bed}, rows C3/C4.
 * </p>
 *
 * <p>
 * <b>Never withheld.</b> {@link ToolGroups#INTROSPECTION} is in
 * {@code RoleToolAccess.IMPLICIT_BASELINE}: every role, every run, unattended runs included, without a
 * {@code <baseline>} entry. A capability that can be forgotten would put the guessing back one level
 * down - the agent would lose the honest answer exactly where the policy was written carelessly. An
 * explicit {@code <deny>} still wins, as it does for {@link ToolGroups#INTERACTION}.
 * </p>
 *
 * <p>
 * <b>It discloses nothing.</b> Every entry is filtered for the acting user, and an entry that is
 * withheld from them is named with the capability it costs - which is what {@code AgentCapabilities}
 * already tells the model in the system prompt, and what a denial at the gate already says. The listing
 * is the same information, asked for rather than stumbled into.
 * </p>
 *
 * @see AgentCapabilities#unusableReason(Tool)
 */
public class ListToolsTool implements Tool {

	private static final String INPUT_SCHEMA_JSON = """
		{
			"type": "object",
			"properties": {}
		}
		""";

	private final String _agentName;

	private final List<Tool> _ownTools;

	private final List<SkillDefinition> _skills;

	private final SkillTool _skillTool;

	/**
	 * Creates the self-inspection tool of one agent.
	 *
	 * @param agentName
	 *        The name of the agent whose catalog this describes.
	 * @param ownTools
	 *        The tools the agent carries without loading anything - its own {@code <tools>}, its MCP
	 *        servers' tools and its delegation tools - already resolved, unfiltered. Filtering happens
	 *        per call, against the acting run.
	 * @param skills
	 *        Every skill the agent declares, offered or withheld. A withheld one is listed with the
	 *        reason, because "this agent has no such skill" and "you may not use it" are different
	 *        answers.
	 * @param skillTool
	 *        The agent's {@code loadSkill} tool, which decides which of the declared skills are
	 *        offered and which are loaded already, or <code>null</code> if this agent has no
	 *        {@code loadSkill} route - in which case no declared skill is loadable and each is listed
	 *        with the reason.
	 */
	public ListToolsTool(String agentName, List<Tool> ownTools, List<SkillDefinition> skills,
			SkillTool skillTool) {
		_agentName = agentName;
		_ownTools = List.copyOf(ownTools);
		_skills = List.copyOf(skills);
		_skillTool = skillTool;
	}

	@Override
	public String name() {
		return "listTools";
	}

	@Override
	public String description() {
		return "List what THIS agent carries: its own tools, the tools of each of its skills, and for"
			+ " each entry whether the current user may use it and if not, why. Call this before"
			+ " telling a user that a tool they named is unavailable. It does not answer whether the"
			+ " application has a tool of that name somewhere else.";
	}

	@Override
	public ToolSchema schema() {
		return new ToolSchema(name(), description(), INPUT_SCHEMA_JSON);
	}

	@Override
	public ToolMode mode() {
		return ToolMode.READ;
	}

	/** {@link ToolGroups#INTROSPECTION}, which is granted implicitly - see the class comment. */
	@Override
	public String group() {
		return ToolGroups.INTROSPECTION;
	}

	@Override
	public boolean isConcurrencySafe() {
		return true;
	}

	@Override
	public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
		AgentCapabilities capabilities = AgentCapabilities.forRun(security(context));

		StringBuilder out = new StringBuilder();
		out.append("Tools and skills of agent '").append(_agentName)
			.append("', filtered for the current user.\n\n");
		out.append("This is this agent's OWN catalog and nothing else. A name that appears nowhere below"
			+ " is not in this agent's toolset: not a permission problem, and nothing you or the user"
			+ " can do about it - an administrator would have to add it. Whether the application has a"
			+ " tool of that name somewhere else is a separate question that this listing does not"
			+ " answer.\n");

		appendOwnTools(out, capabilities, context);
		appendSkills(out, capabilities);

		out.append("\nEach reason above explains only the entry it stands on.\n");
		return ToolResult.of(out.toString());
	}

	/**
	 * The tools that need no {@code loadSkill}, split into the ones the user may call, the ones
	 * withheld from them and the ones that cannot act in this run.
	 *
	 * <p>
	 * This tool lists itself, so that "a name that appears nowhere below is not in this agent's
	 * toolset" is true of every name including its own.
	 * </p>
	 *
	 * <p>
	 * A tool that cannot act in this run - {@code present} on a page with no place to show anything -
	 * is not offered to the model, so listing it as callable would send the model to a call that is
	 * only refused. It is not withheld either: nothing an administrator grants changes it, and its
	 * reason says what would.
	 * </p>
	 */
	private void appendOwnTools(StringBuilder out, AgentCapabilities capabilities, ToolExecutionContext context) {
		List<String> usable = new ArrayList<>();
		usable.add(name());
		List<String> withheld = new ArrayList<>();
		List<String> idle = new ArrayList<>();
		for (Tool tool : _ownTools) {
			String reason = capabilities.unusableReason(tool);
			if (reason != null) {
				withheld.add("- `" + tool.name() + "` - " + reason + ".");
				continue;
			}
			String unavailable = tool.unavailableReason(context);
			if (unavailable != null) {
				idle.add("- `" + tool.name() + "` - " + unavailable);
			} else {
				usable.add(tool.name());
			}
		}

		out.append("\n## Own tools - callable right now\n");
		out.append(join(usable)).append("\n");
		if (!withheld.isEmpty()) {
			out.append("\n## Own tools - carried by this agent, withheld from this user\n");
			for (String line : withheld) {
				out.append(line).append("\n");
			}
		}
		if (!idle.isEmpty()) {
			out.append("\n## Own tools - carried by this agent, unable to act in this run\n");
			for (String line : idle) {
				out.append(line).append("\n");
			}
		}
	}

	/**
	 * Every skill the agent declares, with what loading it would hand over.
	 *
	 * <p>
	 * Every declared skill is listed, offered or not. A skill withheld from this user is the one the
	 * model is most likely to be asked about, and leaving it out would make it indistinguishable from a
	 * skill this agent never had - the very confusion this tool exists to end.
	 * </p>
	 *
	 * <p>
	 * Which state a skill is in is read off the agent's own {@code loadSkill} tool rather than
	 * recomputed, so the two cannot disagree about what a load would do:
	 * </p>
	 * <ul>
	 * <li><b>loaded</b> - its tools are already in the tool list;</li>
	 * <li><b>loadable</b> - call {@code loadSkill};</li>
	 * <li><b>withheld</b> - every tool in it needs a capability this user does not hold, so a load would
	 * hand over nothing, which is why it is not offered in the skill catalog either;</li>
	 * <li><b>not loadable</b> - offered by nothing and denied by nothing: a definition without content,
	 * which is a configuration fault and is logged as one where the skill map is built.</li>
	 * </ul>
	 */
	private void appendSkills(StringBuilder out, AgentCapabilities capabilities) {
		if (_skills.isEmpty()) {
			return;
		}
		Set<String> offered = _skillTool == null ? Set.of() : _skillTool.offeredSkills();
		Set<String> loaded = _skillTool == null ? Set.of() : _skillTool.loadedSkills();

		out.append("\n## Skills - call loadSkill(\"<id>\") for the ones marked loadable\n");
		for (SkillDefinition skill : _skills) {
			out.append("- `").append(skill.name()).append("` ");
			if (offered.contains(skill.name())) {
				out.append(loaded.contains(skill.name()) ? "[loaded]\n" : "[loadable]\n");
				appendSkillTools(out, skill, capabilities);
				continue;
			}
			AgentCapabilities.Unusable unusable = capabilities.unusableReason(skill);
			if (unusable != null) {
				out.append("[withheld] - ").append(withheldSkill(skill, capabilities, unusable))
					.append("\n");
				continue;
			}
			out.append("[not loadable] - this agent declares it and nothing withholds it, so its"
				+ " definition is incomplete in this application. Not a permission problem: an"
				+ " administrator would have to fix it.\n");
		}
	}

	/**
	 * What a loadable skill would hand this user, and what of it stays behind - grouped by the reason.
	 *
	 * <p>
	 * Grouped rather than annotated per tool because a skill can hold a dozen tools of one capability
	 * and two of another: repeating the same clause thirteen times buries the one line that differs,
	 * and dropping the clause altogether leaves the model unable to say which capability a user would
	 * have to be granted.
	 * </p>
	 */
	private static void appendSkillTools(StringBuilder out, SkillDefinition skill,
			AgentCapabilities capabilities) {
		List<String> usable = new ArrayList<>();
		Map<String, List<String>> withheldByReason = new LinkedHashMap<>();
		for (String toolName : skill.tools()) {
			Tool tool = capabilities.resolveTool(toolName);
			if (tool == null) {
				// A name the registry does not resolve - a typo in the skill definition. Not a
				// denial, and not something to report as one; it is logged where the skill loads.
				continue;
			}
			String reason = capabilities.unusableReason(tool);
			if (reason == null) {
				usable.add(tool.name());
			} else {
				withheldByReason.computeIfAbsent(reason, key -> new ArrayList<>()).add(tool.name());
			}
		}
		if (usable.isEmpty() && withheldByReason.isEmpty()) {
			out.append("  instructions only, this skill carries no tools\n");
			return;
		}
		if (!usable.isEmpty()) {
			out.append("  hands over: ").append(join(usable)).append("\n");
		}
		for (Map.Entry<String, List<String>> entry : withheldByReason.entrySet()) {
			out.append("  not handed over: ").append(join(entry.getValue()))
				.append(" - ").append(entry.getKey()).append(".\n");
		}
	}

	/**
	 * Why a declared skill is not offered, naming the capabilities it needs.
	 *
	 * <p>
	 * The same three remedies {@code AgentCapabilities} tells apart everywhere else: an unattended run
	 * lost the skill to the {@code <background-allowlist>}, which no role grant changes; an attended
	 * one lost it to the acting user's roles; or every tool in it works on model types the user may
	 * not read, which is a Read grant on a type and not a capability at all (#258). The third names no
	 * capability, because the one its tools cost is one this user already holds.
	 * </p>
	 */
	private static String withheldSkill(SkillDefinition skill, AgentCapabilities capabilities,
			AgentCapabilities.Unusable unusable) {
		if (!unusable.aboutCapabilities()) {
			return unusable.message() + ". Loading it would hand over nothing, so it is not offered.";
		}
		Set<String> lacking = capabilities.lacking(skill.tools());
		String needs = lacking.isEmpty()
			? "every tool in it belongs to no capability, so no grant reaches it"
			: "every tool in it needs a capability this user does not hold: " + join(lacking);
		if (capabilities.withheldByBackgroundCeiling(skill)) {
			return "this run is unattended, so only the capabilities on the background allowlist are"
				+ " reachable, and " + needs + ". The same request in the chat would have this skill.";
		}
		return needs + ". Loading it would hand over nothing, so it is not offered.";
	}

	/** The names as a comma-separated list, each in backticks. */
	private static String join(Iterable<String> names) {
		StringBuilder list = new StringBuilder();
		boolean first = true;
		for (String name : names) {
			if (!first) {
				list.append(", ");
			}
			first = false;
			list.append('`').append(name).append('`');
		}
		return list.toString();
	}

	/**
	 * The security context of the run this call belongs to.
	 *
	 * <p>
	 * No permissive fallback: every line of the output is a statement about one principal, and a
	 * listing that cannot name the principal would report a catalog that belongs to nobody.
	 * </p>
	 */
	private static SecurityContext security(ToolExecutionContext context) {
		if (context instanceof ToolContext toolContext) {
			return toolContext.security();
		}
		throw new IllegalStateException("The tool listing is filtered for the run's principal, and this"
			+ " call has no run to ask. Execute the tool with the ToolContext of an agent run.");
	}
}
