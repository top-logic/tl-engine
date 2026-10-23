/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import com.top_logic.tl.ai.core.agent.AgentCapabilities;
import com.top_logic.tl.ai.core.agent.AgentDefinition;
import com.top_logic.tl.ai.core.agent.AgentDefinitionQuery;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.SkillDefinition;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.ToolPermissionFilter;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.security.RoleToolAccess;
import com.top_logic.tl.ai.core.tools.ToolRegistry;
import com.top_logic.tl.ai.core.tools.introspection.ListToolsTool;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * What {@code listTools} tells a persona about the agent it is talking to.
 *
 * <p>
 * The tool the C3/C4 rows of the test bed were missing. Asked about a tool by name, the agent had no
 * source for "is that in my toolset?" - so its prompt sent it to the privileged authoring skill to
 * check the <em>application's</em> registry, that skill is withheld from every persona here, and the
 * model relayed the withheld-skill refusal as though {@code agent.meta} / {@code agent.authoring} were
 * the reason the <em>asked-about tool</em> was unreachable. This is the source it now has.
 * </p>
 *
 * <p>
 * Two things are asserted, and they are different claims:
 * </p>
 * <ul>
 * <li><b>It is always there.</b> {@code introspection} is in {@link RoleToolAccess#IMPLICIT_BASELINE},
 * and this fixture's policy writes an explicitly empty {@code <baseline/>} - so a persona reaching
 * {@code listTools} here reaches it through the implicit grant and nothing else. Unattended runs too,
 * where the {@code <background-allowlist>} admits neither {@code introspection} nor
 * {@code interaction} by name.</li>
 * <li><b>It is filtered, and it says why.</b> One catalog, two personas, two different answers about
 * the same tool - which is the pair that makes the reason attributable to the policy rather than to
 * the tool.</li>
 * </ul>
 *
 * <p>
 * The remaining half of the implicit-grant semantics - that an explicit {@code <deny>} still wins over
 * it - is not expressible here: this fixture ships no {@code <deny>} at all, deliberately, because
 * deny-by-default makes one unnecessary. {@code TestRoleToolAccess.testDenyBeatsBaseline} in
 * {@code tl-ai-core} pins it against a policy that writes one.
 * </p>
 *
 * @see ListToolsTool
 */
public class SecDemoListToolsTest extends SecDemoPersonaTest {

	/** The chat's default entry point, and the agent of the reported session. */
	private static final String ENTRY_AGENT = "TL_SingleAgent";

	/** The name the model calls this tool by. */
	private static final String LIST_TOOLS = "listTools";

	/** Every tool of it needs {@code agent.meta} or {@code agent.authoring}. */
	private static final String AUTHORING_SKILL = "TL_agenticAuthoring";

	/** Both read groups, which every persona holds. */
	private static final String READABLE_SKILL = "TL_modelExploration";

	/** Three tools, all {@code workspace}, which nobody holds. */
	private static final String WORKSPACE_SKILL = "TL_taskManagement";

	/** Precondition: the capability is implicit and the fixture grants it nowhere explicitly. */
	public void testTheCapabilityIsImplicitAndNotGrantedAnywhere() {
		assertTrue("listTools must not be gated behind a grantable capability.",
			RoleToolAccess.IMPLICIT_BASELINE.contains(ToolGroups.INTROSPECTION));
		assertEquals("The tool must declare exactly that group, or the implicit grant misses it.",
			ToolGroups.INTROSPECTION, listTools().group());
	}

	/**
	 * Every persona may call it, under a policy whose {@code <baseline/>} grants nothing.
	 *
	 * <p>
	 * The whole point of the implicit grant: this fixture's baseline is written and empty, no role
	 * grants {@code introspection}, and the answer is still yes. An administrator cannot lose this by
	 * omission.
	 * </p>
	 */
	public void testEveryPersonaMayCallIt() {
		for (String login : personaLogins()) {
			SecurityContext security = securityFor(login);
			String reason = security.toolDenialReason(listTools());
			assertNull(login + " must be allowed to call listTools, but: " + reason, reason);
		}
	}

	/** ...and its schema really reaches the provider, which is the other half of "available". */
	public void testItsSchemaReachesTheProviderForEveryPersona() {
		for (String login : personaLogins()) {
			SecurityContext security = securityFor(login);
			List<String> names = catalogFor(security).toolSchemas(new ToolPermissionFilter(security))
				.stream().map(ToolSchema::name).toList();
			assertTrue(login + " must be sent the listTools schema: " + names,
				names.contains(LIST_TOOLS));
		}
	}

	/**
	 * An unattended run keeps it, although the allowlist names neither implicit capability.
	 *
	 * <p>
	 * The failure mode this rules out: a scheduled or detached run losing the one tool that lets it
	 * answer honestly, and falling back to guessing exactly where nobody is watching.
	 * </p>
	 */
	public void testAnUnattendedRunKeepsIt() {
		for (String login : personaLogins()) {
			SecurityContext background = securityFor(login, true);
			assertNull("An unattended run of " + login + " must keep listTools: "
				+ background.toolDenialReason(listTools()),
				background.toolDenialReason(listTools()));
		}
	}

	/** The shipped entry agent really carries it - it is injected, not configured. */
	public void testTheShippedAgentCarriesItWithoutDeclaringIt() {
		AgentDefinition agent = agent();

		assertFalse("It must not be in <tools>, or an agent could be configured without it: "
			+ agent.tools(), agent.tools().contains(LIST_TOOLS));
		assertNotNull("...and must be in the catalog all the same.",
			catalogFor(securityFor(ANNA)).getTool(LIST_TOOLS));
	}

	/**
	 * The listing of the shipped agent names its own tools, its skills, and the reason for each
	 * withheld skill.
	 */
	public void testTheShippedAgentsListingIsComplete() {
		String listing = listToolsAs(ANNA);

		assertTrue("It must name the agent it describes: " + listing,
			listing.contains("'" + ENTRY_AGENT + "'"));
		assertTrue("It must list itself, or 'absent means not carried' is false of its own name: "
			+ listing, listing.contains("`" + LIST_TOOLS + "`"));
		for (String toolName : agent().tools()) {
			assertTrue("Own tool " + toolName + " must appear: " + listing,
				listing.contains("`" + tool(toolName).name() + "`"));
		}
		for (String skillName : agent().skills()) {
			assertTrue("Skill " + skillName + " must appear, offered or not: " + listing,
				listing.contains("`" + skillName + "`"));
		}
		assertTrue("loadSkill is a tool of this agent too, and must be listed: " + listing,
			callableTools(listing).contains("`loadSkill`"));
		assertTrue("A skill every persona can load must be offered: " + listing,
			listing.contains("`" + READABLE_SKILL + "` [loadable]"));
		assertTrue("...and the withheld authoring skill must be marked as such: " + listing,
			listing.contains("`" + AUTHORING_SKILL + "` [withheld]"));
		assertTrue("...with the capability an administrator would have to grant: " + listing,
			listing.contains("`" + ToolGroups.AGENT_META + "`"));
	}

	/** A loaded skill is reported as loaded, so the model does not spend a turn re-loading it. */
	public void testALoadedSkillIsReportedAsLoaded() {
		SecurityContext security = securityFor(ANNA);
		ToolRegistry catalog = catalogFor(security);

		assertTrue("Precondition: the skill loads for Anna.",
			call(catalog, security, "loadSkill", "{\"skillId\": \"" + READABLE_SKILL + "\"}")
				.contains("loaded."));

		String listing = call(catalog, security, LIST_TOOLS, "{}");
		assertTrue("The same catalog must now report it as loaded: " + listing,
			listing.contains("`" + READABLE_SKILL + "` [loaded]"));
	}

	/**
	 * One tool, two personas, two true answers - and neither of them a guess.
	 *
	 * <p>
	 * Against a catalog built here rather than an agent's, because what is under test is the filter and
	 * this fixture's shipped agents carry no {@code data.write} tool: without one, Anna's and Ben's
	 * listings would be identical and the pair would prove nothing. The tools and the policy are the
	 * real ones.
	 * </p>
	 */
	public void testTheSameToolIsAnsweredDifferentlyPerPersona() {
		String anna = listing(ANNA, mixedTools());
		String ben = listing(BEN, mixedTools());

		String writeTool = tool(CLASSIFIED_WRITE_SCRIPT_TOOL).name();
		assertTrue("Anna holds data.write, so the tool is in her callable list: " + anna,
			callableTools(anna).contains("`" + writeTool + "`"));
		assertFalse("...and must not be reported as withheld from her: " + anna,
			anna.contains("`" + writeTool + "` - withheld"));
		assertFalse("Ben holds no data.write, so it is not callable for him: " + ben,
			callableTools(ben).contains("`" + writeTool + "`"));
		assertTrue("...and he must be told why: " + ben,
			ben.contains("`" + writeTool + "` - withheld from the current user"));
		assertTrue("...naming the capability an administrator would grant: " + ben,
			ben.contains("`" + ToolGroups.DATA_WRITE + "`"));
	}

	/** The line listing the tools the run may call right now. */
	private static String callableTools(String listing) {
		int start = listing.indexOf("## Own tools - callable right now");
		assertTrue("The listing must have a callable section: " + listing, start >= 0);
		int end = listing.indexOf("\n##", start + 1);
		return end < 0 ? listing.substring(start) : listing.substring(start, end);
	}

	/** A capability nobody holds is named as such, and a tool nobody classified is not blamed on one. */
	public void testWithheldAndUnclassifiedToolsAreToldApart() {
		String listing = listing(ANNA, mixedTools());

		assertTrue("egress is granted to nobody here: " + listing,
			listing.contains("`" + tool(EGRESS_TOOL).name() + "` - withheld from the current user"));
		assertTrue(listing, listing.contains("`" + ToolGroups.EGRESS + "`"));
		assertTrue("An unclassified tool is denied to everyone, and has no capability to name: "
			+ listing, listing.contains("`" + tool(UNCLASSIFIED_SCRIPT_TOOL).name()
				+ "` - denied to everyone: it belongs to no capability"));
	}

	/**
	 * An unattended run is told that the run is why, not that the user's roles are.
	 *
	 * <p>
	 * Anna holds {@code data.write} and the {@code <background-allowlist>} does not admit it, so this
	 * is the one place the two remedies really differ: an administrator asked to grant a role Anna
	 * already has would find nothing to do.
	 * </p>
	 */
	public void testAnUnattendedRunBlamesTheRunAndNotTheRoles() {
		String listing = listing(securityFor(ANNA, true), mixedTools());

		String writeTool = tool(CLASSIFIED_WRITE_SCRIPT_TOOL).name();
		assertTrue("The run must be named as the reason: " + listing,
			listing.contains("`" + writeTool + "` - out of reach because this run is unattended"));
		assertTrue("...and what would change it: " + listing,
			listing.contains("background allowlist"));
		assertTrue("...and it must say the roles are not the reason: " + listing,
			listing.contains("not about the current user's roles"));
		assertTrue("listTools itself survives the ceiling: " + listing,
			listing.contains("`" + LIST_TOOLS + "`"));
	}

	/**
	 * A declared skill is listed even when nothing can load it, and is not mistaken for absent.
	 *
	 * <p>
	 * An agent every one of whose skills is withheld gets no {@code loadSkill} tool at all - the skill
	 * map is empty, so {@code AgentFactory} builds none. The skills must still appear: leaving them out
	 * would make "withheld from this user" indistinguishable from "this agent never had it", which is
	 * exactly the confusion this tool exists to end.
	 * </p>
	 */
	public void testDeclaredSkillsAreListedWithNoLoadSkillRoute() {
		SkillDefinition withheld = skill(WORKSPACE_SKILL);
		ListToolsTool tool = new ListToolsTool("FixtureAgent", List.of(), List.of(withheld), null);
		ToolContext context = new ToolContext(securityFor(ANNA), ApprovalCallback.AUTO_APPROVE,
			TransactionScope.NOOP);

		String listing = tool.execute(Map.of(), context).text();

		assertTrue("The skill must be listed although nothing can load it: " + listing,
			listing.contains("`" + WORKSPACE_SKILL + "` [withheld]"));
		assertTrue("...naming the capability that withheld it: " + listing,
			listing.contains("`" + ToolGroups.WORKSPACE + "`"));
	}

	/** The listing keeps its own subject apart from the application's registry. */
	public void testTheListingKeepsTheTwoQuestionsApart() {
		String listing = listToolsAs(BEN);

		assertTrue("It must say what it is about: " + listing,
			listing.contains("this agent's OWN catalog"));
		assertTrue("...that an absent name is not a permission problem: " + listing,
			listing.contains("is not in this agent's toolset"));
		assertTrue("...who could change that: " + listing, listing.contains("administrator"));
		assertTrue("...and that the application-wide question is a different one: " + listing,
			listing.contains("separate question that this listing does not answer"));
		assertTrue("Each reason must be scoped to its own entry: " + listing,
			listing.contains("Each reason above explains only the entry it stands on."));
	}

	// ===== Helpers =====

	/**
	 * One tool per capability the pair assertions need, all real and all classified by the shipped
	 * policy.
	 */
	private static List<Tool> mixedTools() {
		List<Tool> tools = new ArrayList<>();
		for (String name : List.of(MODEL_READ_TOOL, CLASSIFIED_READ_SCRIPT_TOOL,
			CLASSIFIED_WRITE_SCRIPT_TOOL, UNCLASSIFIED_SCRIPT_TOOL, EGRESS_TOOL)) {
			tools.add(tool(name));
		}
		return tools;
	}

	private String listing(String login, List<Tool> tools) {
		return listing(securityFor(login), tools);
	}

	/** The output of a {@link ListToolsTool} over the given tools, for the given run. */
	private static String listing(SecurityContext security, List<Tool> tools) {
		ListToolsTool tool =
			new ListToolsTool("FixtureAgent", tools, List.<SkillDefinition> of(), null);
		ToolContext context =
			new ToolContext(security, ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP);
		return tool.execute(Map.of(), context).text();
	}

	/** The listing of the shipped entry agent, out of a real tool call through its catalog. */
	private String listToolsAs(String login) {
		SecurityContext security = securityFor(login);
		return call(catalogFor(security), security, LIST_TOOLS, "{}");
	}

	/** The registered {@code listTools} of the shipped entry agent. */
	private Tool listTools() {
		Tool tool = catalogFor(securityFor(ANNA)).getTool(LIST_TOOLS);
		assertNotNull("Every agent's catalog must carry '" + LIST_TOOLS + "'.", tool);
		return tool;
	}

	/** Executes one tool call against the given catalog and returns the text the model receives. */
	private static String call(ToolRegistry catalog, SecurityContext security, String toolName,
			String arguments) {
		ToolContext context =
			new ToolContext(security, ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP);
		ToolResultMessage result = catalog.execute(new ToolCall("call-1", toolName, arguments), context);
		if (result.state() instanceof ToolState.Completed completed) {
			return completed.result();
		}
		return ((ToolState.Failed) result.state()).error();
	}

	/**
	 * The tool catalog of a run of {@link #ENTRY_AGENT} under the given security context, built exactly
	 * as the chat builds it.
	 */
	private static ToolRegistry catalogFor(SecurityContext security) {
		return AgentFactory.getInstance()
			.buildToolCatalog(agent(), AgentCapabilities.forRun(security));
	}

	private static SkillDefinition skill(String name) {
		SkillDefinition definition = AgentDefinitionQuery.getInstance().findSkillByName(name);
		assertNotNull("The framework skill '" + name + "' must exist.", definition);
		return definition;
	}

	private static AgentDefinition agent() {
		AgentDefinition agent = AgentDefinitionQuery.getInstance().findAgentByName(ENTRY_AGENT);
		assertNotNull("The framework agent '" + ENTRY_AGENT + "' must exist; this test bed assumes"
			+ " the agents shipped in 'tl-ai-core.conf.config.xml'.", agent);
		return agent;
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoListToolsTest.class, TopLogicAgentDefinitions.Module.INSTANCE);
	}

}
