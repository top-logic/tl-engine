/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.basic.Logger;
import com.top_logic.basic.listener.Listener;
import com.top_logic.basic.logging.Level;
import com.top_logic.tl.ai.core.agent.AgentCapabilities.AgentGraph;
import com.top_logic.tl.ai.core.agent.AgentFactory.DelegateOffer;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Tests the delegate filter: which sub-agents an agent is offered, and what its prompt says about the
 * ones it keeps.
 *
 * <p>
 * The recursive form of {@link TestAgentCapabilities}. A delegate's effective tool set is its own
 * {@code <tools>} plus its skills' plus its MCP servers' plus - transitively - its own delegates', so
 * the predicate is not the skill one-liner; what it shares with the skill filter is the rule it
 * applies to what the walk finds, and that rule is exercised here in the delegate's terms.
 * </p>
 *
 * <p>
 * All three surfaces are asserted, because filtering only the prompt text would be cosmetic: the
 * generated catalog ({@link AgentFactory#delegateCatalog}) and the {@code delegate_to_*} registration
 * ({@link AgentFactory#registerDelegationTools}). The third,
 * {@link com.top_logic.tl.ai.core.tools.interaction.ListAgentsTool}, needs the real agent definitions
 * and is asserted in {@code SecDemoDelegateFilterTest}.
 * </p>
 */
public class TestDelegateFilter extends TestCase {

	private static final String READ_TOOL = "data.readInstances";

	private static final String WRITE_TOOL = "data.createInstance";

	private static final String SCRIPT_TOOL = "script.evaluate";

	private static final String UNGROUPED_TOOL = "custom.mystery";

	private static final String MCP_TOOL = "mcp__remote__doSomething";

	/** The agent under test. Delegates to one of each interesting kind. */
	private static final String PARENT = "parent";

	/** One {@code data.read} tool of its own. */
	private static final String READER_DELEGATE = "reader";

	/** One {@code data.write} tool of its own, and nothing else. */
	private static final String WRITER_DELEGATE = "writer";

	/** A {@code data.read} tool and a {@code data.write} one - the partial case. */
	private static final String MIXED_DELEGATE = "mixed";

	/** No tools, no skills, no delegates. */
	private static final String TOOLLESS_DELEGATE = "toolless";

	/** No tools of its own; reaches everything through the agents it delegates to. */
	private static final String ROUTER_DELEGATE = "router";

	/** Declares a skill whose only tool is denied to the narrow principal. */
	private static final String SKILLED_DELEGATE = "skilled";

	/** Declares one tool name that resolves to nothing. */
	private static final String TYPO_DELEGATE = "typo";

	/** Reaches a {@code data.write} tool through an MCP server rather than through its tool list. */
	private static final String MCP_DELEGATE = "mcpUser";

	/** Delegates to {@link #CYCLE_B}, which delegates back to it. */
	private static final String CYCLE_A = "cycleA";

	/** Delegates to {@link #CYCLE_A}. Its own tool is the only thing the pair can reach. */
	private static final String CYCLE_B = "cycleB";

	// ===== The predicate =====

	/** A principal who may use every tool keeps every delegate, in declaration order. */
	public void testPrincipalWhoMayUseEverythingKeepsEveryDelegate() {
		AgentCapabilities capabilities = capabilities(everyTool());

		assertNull(capabilities.unusableReason(agent(WRITER_DELEGATE)));
		assertEquals(Set.of(), capabilities.unreachableCapabilities(agent(WRITER_DELEGATE)));
		assertEquals(List.of(READER_DELEGATE, WRITER_DELEGATE, MIXED_DELEGATE, TOOLLESS_DELEGATE,
			ROUTER_DELEGATE, SKILLED_DELEGATE, TYPO_DELEGATE, MCP_DELEGATE, CYCLE_A),
			AgentFactory.offeredDelegates(agent(PARENT), capabilities));
	}

	/** A delegate every one of whose tools is denied is dropped, with a reason worth logging. */
	public void testFullyDeniedDelegateIsDropped() {
		String reason = readOnlyPrincipal().unusableReason(agent(WRITER_DELEGATE));

		assertNotNull(reason);
		assertTrue("The reason must name the capability an operator has to grant: " + reason,
			reason.contains(ToolGroups.DATA_WRITE));
	}

	/** One usable tool keeps the delegate: it can still do that part of its job. */
	public void testPartiallyReachableDelegateSurvives() {
		AgentCapabilities capabilities = readOnlyPrincipal();

		assertNull(capabilities.unusableReason(agent(MIXED_DELEGATE)));
		assertEquals("What it cannot be asked for must be nameable.",
			Set.of(ToolGroups.DATA_WRITE), capabilities.unreachableCapabilities(agent(MIXED_DELEGATE)));
	}

	/** A delegate the principal can use in full is not reported as limited. */
	public void testFullyReachableDelegateIsNotLimited() {
		assertEquals(Set.of(), readOnlyPrincipal().unreachableCapabilities(agent(READER_DELEGATE)));
	}

	/**
	 * A delegate with no tools of its own survives - "all zero of its tools are denied" is vacuously
	 * true and must not delete it.
	 */
	public void testToollessDelegateSurvives() {
		assertNull(capabilities(Set.of())
			.unusableReason(agent(TOOLLESS_DELEGATE)));
	}

	/**
	 * A delegation-only agent is judged by what its own delegates can reach, not by its empty tool
	 * list.
	 */
	public void testDelegationOnlyAgentIsJudgedByItsDelegates() {
		assertNull("It reaches the reader's data.read tool, so it can do something.",
			readOnlyPrincipal().unusableReason(agent(ROUTER_DELEGATE)));

		String reason = capabilities(Set.of()).unusableReason(agent(ROUTER_DELEGATE));
		assertNotNull("With everything its delegates reach denied, it can do nothing.", reason);
	}

	/** A delegate reaches the tools of the skills it declares. */
	public void testSkillToolsCountTowardsReachability() {
		assertNull("Its skill's tool is permitted, so the delegate is usable.",
			capabilities(Set.of(SCRIPT_TOOL))
				.unusableReason(agent(SKILLED_DELEGATE)));

		assertNotNull("With the skill's only tool denied, nothing is left.",
			readOnlyPrincipal().unusableReason(agent(SKILLED_DELEGATE)));
	}

	/** A delegate reaches the tools of the MCP servers it names. */
	public void testMcpToolsCountTowardsReachability() {
		assertNotNull("Its only tool is an MCP tool in data.write.",
			readOnlyPrincipal().unusableReason(agent(MCP_DELEGATE)));
		assertNull(capabilities(Set.of(MCP_TOOL))
			.unusableReason(agent(MCP_DELEGATE)));
	}

	/** A typo in a delegate's tool list is not a denial and must not remove the delegate. */
	public void testDelegateOfOnlyUnresolvableToolsSurvives() {
		assertNull(capabilities(Set.of())
			.unusableReason(agent(TYPO_DELEGATE)));
	}

	/** A delegation naming an agent that does not exist is not a denial either. */
	public void testUnknownDelegateSurvives() {
		assertNull(capabilities(Set.of()).unusableReason((AgentDefinition) null));
	}

	/** An ungrouped tool keeps a delegate alive for nobody, and names no capability. */
	public void testUngroupedToolNamesNoCapability() {
		AgentDefinition delegate = new AgentDefinition("ungrouped", "ungrouped", null, "", List.of(UNGROUPED_TOOL),
			List.of(), List.of(), List.of(), AgentSource.CONFIG);
		AgentCapabilities capabilities = readOnlyPrincipal();

		assertNotNull(capabilities.unusableReason(delegate));
		assertEquals(Set.of(), capabilities.unreachableCapabilities(delegate));
	}

	/**
	 * A delegation cycle terminates, and the revisit contributes nothing beyond what the first visit
	 * found.
	 */
	public void testDelegationCycleTerminates() {
		AgentCapabilities capabilities = readOnlyPrincipal();

		assertNotNull("cycleB's write tool is all the pair can reach, and it is denied.",
			capabilities.unusableReason(agent(CYCLE_A)));
		assertEquals(Set.of(ToolGroups.DATA_WRITE), capabilities.unreachableCapabilities(agent(CYCLE_A)));
		assertEquals("Each agent of the cycle is walked once.",
			List.of(WRITE_TOOL), toolNames(capabilities.reachableTools(agent(CYCLE_A))));
	}

	/** A principal who may use everything sees every delegate, unlimited. */
	public void testPrincipalWhoMayUseEverythingSeesEveryDelegate() {
		AgentCapabilities capabilities = capabilities(
			Set.of(READ_TOOL, WRITE_TOOL, SCRIPT_TOOL, UNGROUPED_TOOL, MCP_TOOL));

		assertEquals(List.of(READER_DELEGATE, WRITER_DELEGATE, MIXED_DELEGATE, TOOLLESS_DELEGATE,
			ROUTER_DELEGATE, SKILLED_DELEGATE, TYPO_DELEGATE, MCP_DELEGATE, CYCLE_A),
			AgentFactory.offeredDelegates(agent(PARENT), capabilities));
		assertEquals(Set.of(), capabilities.unreachableCapabilities(agent(MIXED_DELEGATE)));
	}

	// ===== Surface 1: the generated catalog =====

	/**
	 * A principal who may use every tool is offered every delegate, in declaration order, and nothing
	 * is appended about capabilities it lacks.
	 */
	public void testCatalogOfAPrincipalWhoMayUseEverything() {
		AgentDefinition parent = new AgentDefinition(PARENT, "p", null, "", List.of(), List.of(), List.of(),
			List.of(READER_DELEGATE, WRITER_DELEGATE), AgentSource.CONFIG);

		assertEquals("\n\n### Available Delegates\n\n"
			+ "You can delegate tasks to these specialized agents:\n\n"
			+ "- **reader** (`delegate_to_reader`): reader\n"
			+ "- **writer** (`delegate_to_writer`): writer\n",
			catalogOf(parent, capabilities(everyTool())));
	}

	/** Under enforcement the catalog keeps what is usable and drops what is not. */
	public void testEnforcedCatalogDropsTheUnusableDelegate() {
		String catalog = catalogOf(agent(PARENT), readOnlyPrincipal());

		assertTrue(catalog, catalog.contains("`delegate_to_reader`"));
		assertTrue(catalog, catalog.contains("`delegate_to_mixed`"));
		assertTrue(catalog, catalog.contains("`delegate_to_toolless`"));
		assertTrue(catalog, catalog.contains("`delegate_to_typo`"));
		assertFalse("The fully denied delegate is not offered: " + catalog,
			catalog.contains("`delegate_to_writer`"));
		assertFalse(catalog, catalog.contains("`delegate_to_mcpUser`"));
		assertFalse(catalog, catalog.contains("`delegate_to_cycleA`"));
	}

	/** A partly reachable delegate is offered and said to be limited, by capability name. */
	public void testCatalogSaysWhichCapabilitiesALimitedDelegateLacks() {
		String catalog = catalogOf(agent(PARENT), readOnlyPrincipal());

		assertTrue("The limitation must be stated where the delegate is offered: " + catalog,
			catalog.contains("Limited for this user"));
		assertTrue("The capability it cannot use must be named: " + catalog,
			catalog.contains("`" + ToolGroups.DATA_WRITE + "`"));
		assertTrue("The agent must be told not to discover the limit by failing: " + catalog,
			catalog.contains("would come back refused"));
	}

	/** A catalog in which nothing is limited says nothing about limits. */
	public void testCatalogIsSilentWhenNothingIsLimited() {
		String catalog = catalogOf(agent(PARENT), capabilities(
			Set.of(READ_TOOL, WRITE_TOOL, SCRIPT_TOOL, UNGROUPED_TOOL, MCP_TOOL)));

		assertFalse(catalog, catalog.contains("Limited for this user"));
	}

	/** With every delegate dropped there is no section at all, rather than an empty heading. */
	public void testCatalogIsEmptyWhenEveryDelegateIsDropped() {
		AgentDefinition parent = new AgentDefinition(PARENT, "p", null, "", List.of(), List.of(), List.of(),
			List.of(WRITER_DELEGATE, MCP_DELEGATE), AgentSource.CONFIG);

		assertEquals("", catalogOf(parent, readOnlyPrincipal()));
	}

	/** Dropping a delegate is logged at INFO, naming the delegate and the capabilities it needed. */
	public void testDropIsLoggedAtInfo() {
		List<Logger.LogEntry> entries = new ArrayList<>();
		Listener<Logger.LogEntry> listener = entries::add;
		Logger.getListenerRegistry().register(listener);
		try {
			catalogOf(agent(PARENT), readOnlyPrincipal());
		} finally {
			Logger.getListenerRegistry().unregister(listener);
		}

		List<String> messages = new ArrayList<>();
		for (Logger.LogEntry entry : entries) {
			if (entry.getPriority() == Level.INFO) {
				messages.add(entry.getMessage());
			}
		}
		String logged = String.join("\n", messages);
		assertTrue("Capability must not vanish silently - the drop must be logged: " + logged,
			logged.contains("'" + WRITER_DELEGATE + "' is not offered"));
		assertTrue("The log line must name the capability an operator has to grant: " + logged,
			logged.contains(ToolGroups.DATA_WRITE));
		assertFalse("An offered delegate must not be logged as dropped: " + logged,
			logged.contains("'" + READER_DELEGATE + "' is not offered"));
	}

	// ===== Surface 2: the delegate_to_* registration =====

	/** The registered tools are exactly the offered delegates - the prompt cannot promise more. */
	public void testRegistrationDropsTheUnusableDelegate() {
		ToolCatalog catalog = registrationOf(agent(PARENT), readOnlyPrincipal());

		assertNotNull(catalog.getTool("delegate_to_reader"));
		assertNotNull(catalog.getTool("delegate_to_mixed"));
		assertNull("A delegate the prompt does not offer must not be callable either.",
			catalog.getTool("delegate_to_writer"));
		assertNull(catalog.getTool("delegate_to_mcpUser"));
	}

	/** A principal who may use every tool gets every declared delegate registered. */
	public void testPrincipalWhoMayUseEverythingGetsEveryDelegateRegistered() {
		ToolCatalog catalog = registrationOf(agent(PARENT), capabilities(everyTool()));

		for (String targetName : agent(PARENT).delegations()) {
			assertNotNull(targetName, catalog.getTool(AgentFactory.delegationToolName(targetName)));
		}
	}

	/** The await tool is wired in alongside the delegation tools. */
	public void testAwaitToolAccompaniesTheOfferedDelegates() {
		assertNotNull(registrationOf(agent(PARENT), readOnlyPrincipal()).getTool("wait_for_subagent"));
	}

	/** With nothing left to delegate to there is nothing to await either. */
	public void testAwaitToolIsAbsentWhenEveryDelegateIsDropped() {
		AgentDefinition parent = new AgentDefinition(PARENT, "p", null, "", List.of(), List.of(), List.of(),
			List.of(WRITER_DELEGATE), AgentSource.CONFIG);

		assertNull(registrationOf(parent, readOnlyPrincipal()).getTool("wait_for_subagent"));
	}

	// ===== Helpers =====

	/** Holds {@link #READ_TOOL} and nothing else. */
	private static AgentCapabilities readOnlyPrincipal() {
		return capabilities(Set.of(READ_TOOL));
	}

	private static String catalogOf(AgentDefinition parent, AgentCapabilities capabilities) {
		List<DelegateOffer> offers = AgentFactory.delegateOffers(parent, capabilities);
		return AgentFactory.delegateCatalog(parent.name(), offers);
	}

	private static ToolCatalog registrationOf(AgentDefinition parent, AgentCapabilities capabilities) {
		ToolCatalog catalog = new ToolCatalog();
		AgentFactory.getInstance().registerDelegationTools(catalog, parent, capabilities);
		return catalog.freeze();
	}

	private static List<String> toolNames(Collection<Tool> tools) {
		List<String> names = new ArrayList<>();
		for (Tool tool : tools) {
			names.add(tool.name());
		}
		return names;
	}

	private static AgentDefinition agent(String name) {
		AgentDefinition definition = graph().agent(name);
		assertNotNull("No such agent in the test graph: " + name, definition);
		return definition;
	}

	/**
	 * The agent graph every test works over: one parent delegating to one of each interesting kind.
	 */
	private static AgentGraph graph() {
		Map<String, AgentDefinition> agents = new LinkedHashMap<>();
		agents.put(PARENT, delegating(PARENT, READER_DELEGATE, WRITER_DELEGATE, MIXED_DELEGATE,
			TOOLLESS_DELEGATE, ROUTER_DELEGATE, SKILLED_DELEGATE, TYPO_DELEGATE, MCP_DELEGATE, CYCLE_A));
		agents.put(READER_DELEGATE, withTools(READER_DELEGATE, READ_TOOL));
		agents.put(WRITER_DELEGATE, withTools(WRITER_DELEGATE, WRITE_TOOL));
		agents.put(MIXED_DELEGATE, withTools(MIXED_DELEGATE, READ_TOOL, WRITE_TOOL));
		agents.put(TOOLLESS_DELEGATE, withTools(TOOLLESS_DELEGATE));
		agents.put(ROUTER_DELEGATE, delegating(ROUTER_DELEGATE, READER_DELEGATE, WRITER_DELEGATE));
		agents.put(SKILLED_DELEGATE, new AgentDefinition(SKILLED_DELEGATE, SKILLED_DELEGATE, null, "",
			List.of(), List.of(), List.of("scripting"), List.of(), AgentSource.CONFIG));
		agents.put(TYPO_DELEGATE, withTools(TYPO_DELEGATE, "no.suchTool"));
		agents.put(MCP_DELEGATE, new AgentDefinition(MCP_DELEGATE, MCP_DELEGATE, null, "",
			List.of(), List.of("remote"), List.of(), List.of(), AgentSource.CONFIG));
		agents.put(CYCLE_A, delegating(CYCLE_A, CYCLE_B));
		agents.put(CYCLE_B, new AgentDefinition(CYCLE_B, CYCLE_B, null, "", List.of(WRITE_TOOL), List.of(),
			List.of(), List.of(CYCLE_A), AgentSource.CONFIG));

		Map<String, SkillDefinition> skills = Map.of("scripting",
			new SkillDefinition("scripting", "scripting", "content", List.of(SCRIPT_TOOL),
				AgentSource.CONFIG));

		return new AgentGraph() {
			@Override
			public AgentDefinition agent(String name) {
				return agents.get(name);
			}

			@Override
			public SkillDefinition skill(String name) {
				return skills.get(name);
			}

			@Override
			public List<Tool> mcpTools(String serverName) {
				return "remote".equals(serverName)
					? List.of(tool(MCP_TOOL, ToolGroups.DATA_WRITE))
					: null;
			}
		};
	}

	private static AgentDefinition withTools(String name, String... tools) {
		return new AgentDefinition(name, name, null, "", List.of(tools), List.of(), List.of(), List.of(),
			AgentSource.CONFIG);
	}

	private static AgentDefinition delegating(String name, String... delegations) {
		return new AgentDefinition(name, name, null, "", List.of(), List.of(), List.of(), List.of(delegations),
			AgentSource.CONFIG);
	}

	/** Every tool the stub registry knows, plus the one it deliberately does not. */
	private static Set<String> everyTool() {
		return Set.of(READ_TOOL, WRITE_TOOL, SCRIPT_TOOL, UNGROUPED_TOOL, MCP_TOOL);
	}

	private static AgentCapabilities capabilities(Set<String> permittedTools) {
		Map<String, Tool> registry = new HashMap<>();
		registry.put(READ_TOOL, tool(READ_TOOL, ToolGroups.DATA_READ));
		registry.put(WRITE_TOOL, tool(WRITE_TOOL, ToolGroups.DATA_WRITE));
		registry.put(SCRIPT_TOOL, tool(SCRIPT_TOOL, ToolGroups.SCRIPT));
		registry.put(UNGROUPED_TOOL, tool(UNGROUPED_TOOL, ""));

		SecurityContext security = new SecurityContext() {
			@Override
			public boolean hasToolPermission(Tool tool) {
				return permittedTools.contains(tool.name());
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.DEVELOPMENT;
			}
		};
		return new AgentCapabilities(registry::get, security, graph());
	}

	private static Tool tool(String name, String group) {
		return new Tool() {
			@Override
			public String name() {
				return name;
			}

			@Override
			public String description() {
				return "Stub: " + name;
			}

			@Override
			public ToolSchema schema() {
				return new ToolSchema(name, description(), "{\"type\":\"object\",\"properties\":{}}");
			}

			@Override
			public String group() {
				return group;
			}

			@Override
			public ToolMode mode() {
				return ToolMode.READ;
			}

			@Override
			public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				return ToolResult.of("ok");
			}
		};
	}
}
