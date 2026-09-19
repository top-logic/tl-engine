/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.agent.skills.Skill;
import com.top_logic.tl.ai.core.memory.ByteBasedTokenEstimator;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.memory.TruncatingStrategy;
import com.top_logic.tl.ai.core.message.CompactionMarker;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.StreamHandler;
import com.top_logic.tl.ai.core.tools.DynamicToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.core.tools.interaction.SkillTool;
import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * What a provider request carries before and after a skill is loaded mid-conversation.
 *
 * <p>
 * The contract of Gitea #346: a {@code loadSkill} may append to the tool array, and it may not do
 * anything else. The system prompt does not move, the tools already advertised keep their names and
 * their positions, and the skill's own instructions travel as a tool result - a message, which every
 * provider's cache treats as conversation rather than as prefix.
 * </p>
 *
 * <p>
 * Why that matters: Anthropic serialises a request as tools, then system, then messages, and caches
 * the prefix up to an explicit breakpoint; OpenAI-compatible endpoints cache the longest common
 * prefix automatically. Under both rules an <em>appended</em> tool leaves everything before it
 * byte-identical, while a reordered or rewritten prefix does not.
 * </p>
 */
public class TestSkillOverlayRequestShape extends TestCase {

	/** Tool execution needs the thread-context module - see {@code TestAgentLoop.suite()}. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestSkillOverlayRequestShape.class)));
	}

	private static final Instant T0 = Instant.parse("2026-09-19T12:00:00Z");

	private static final String MODEL = "test-model";

	private static final TokenUsage USAGE = new TokenUsage(100, 50, 0, 0, 0);

	/** Records every request a run sends, answering with a script of pre-built responses. */
	private static final class Recorder implements LLMProvider {

		final List<ProviderRequest> requests = new ArrayList<>();

		private final List<List<ContentPart>> _responses;

		private int _next;

		Recorder(List<List<ContentPart>> responses) {
			_responses = responses;
		}

		@Override
		public void stream(ProviderRequest request, StreamHandler handler) {
			requests.add(request);
			List<ContentPart> content = _next < _responses.size()
				? _responses.get(_next++)
				: List.of(new TextContent("Done."));
			for (int i = 0; i < content.size(); i++) {
				if (content.get(i) instanceof ToolCall call) {
					handler.onEvent(new StreamEvent.ToolCallStart(i, call.id(), call.name()));
					handler.onEvent(new StreamEvent.ToolCallDelta(i, call.arguments()));
				}
			}
			String finish = content.stream().anyMatch(ToolCall.class::isInstance) ? "tool_use" : "end_turn";
			handler.onEvent(new StreamEvent.MessageComplete("msg", MODEL, finish, USAGE, content));
		}

		List<String> toolNames(int requestIndex) {
			return requests.get(requestIndex).tools().stream().map(ToolSchema::name).toList();
		}
	}

	/** A recorder that answers the first turn with a {@code loadSkill} and then finishes. */
	private static Recorder loadingThenDone(String skillId) {
		return new Recorder(List.of(
			List.of(new ToolCall("tc1", "loadSkill", "{\"skillId\":\"" + skillId + "\"}")),
			List.of(new TextContent("Done."))));
	}

	private static AgentParams params() {
		return new AgentParams(MODEL, "You are a test assistant.",
			AgentParams.DEFAULT_MAX_TURNS, AgentParams.DEFAULT_MAX_OUTPUT_TOKENS);
	}

	private static AgentEventListener ignore() {
		return e -> {
			/* Nothing to observe here. */
		};
	}

	// ===== Helpers building an agent that carries skills =====

	private static Tool stub(String name) {
		return ToolStubs.builder(name).description("Stub tool: " + name).build();
	}

	private static final Map<String, Skill> SKILLS = Map.of(
		"alpha", new Skill("alpha", "The alpha skill", "Alpha instructions.", List.of("alphaTool")),
		"beta", new Skill("beta", "The beta skill", "Beta instructions.", List.of("betaTool")));

	private static final Function<String, Tool> RESOLVER = name -> switch (name) {
		case "alphaTool", "betaTool" -> stub(name);
		default -> null;
	};

	/**
	 * A catalog shaped the way {@code AgentFactory} shapes one: a frozen base of the agent's own
	 * tools, then the framework overlay carrying {@code loadSkill}.
	 */
	private static DynamicToolCatalog agentCatalog(Set<String> preloaded) {
		ToolCatalog base = new ToolCatalog();
		base.register(stub("readModel"));
		base.register(stub("searchModel"));
		DynamicToolCatalog catalog = new DynamicToolCatalog(base.freeze());

		SkillTool skillTool = new SkillTool(SKILLS, catalog, RESOLVER, null, preloaded);
		ToolCatalog framework = new ToolCatalog();
		for (Tool tool : AnnotatedTools.from(skillTool)) {
			framework.register(tool);
		}
		catalog.addOverlay(framework.freeze());
		catalog.setReachableBySkill(skillTool.declaredToolNames());
		return catalog;
	}

	// ===== The evidence =====

	/**
	 * Loading a skill appends to the tool array and changes nothing else about the prefix.
	 *
	 * <p>
	 * The dump this prints is the cache-invalidation evidence of #346: whatever grows, the leading
	 * names must be the same sequence in both requests.
	 * </p>
	 */
	public void testLoadingASkillOnlyAppendsToTheToolArray() {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Do the alpha thing."))));

		Recorder provider = loadingThenDone("alpha");
		new AgentLoop(params(), provider, agentCatalog(null)).run(conversation, ignore());

		assertEquals(2, provider.requests.size());
		List<String> before = provider.toolNames(0);
		List<String> after = provider.toolNames(1);

		System.out.println("[#346] tools before load: " + before);
		System.out.println("[#346] tools after  load: " + after);
		System.out.println("[#346] system prompt identical: "
			+ provider.requests.get(0).systemPrompt().equals(provider.requests.get(1).systemPrompt()));

		assertEquals("The system prompt must not move when a skill is loaded.",
			provider.requests.get(0).systemPrompt(), provider.requests.get(1).systemPrompt());
		assertEquals("A loaded skill's tools must be appended, leaving the prefix in place.",
			before, after.subList(0, before.size()));
		assertTrue("The skill's tool must have arrived: " + after, after.contains("alphaTool"));
	}

	/**
	 * A skill restored from the session lands where a skill loaded in the run lands.
	 *
	 * <p>
	 * Otherwise the second run of a session sends the same tools in a different order, and every
	 * provider's cache misses on a conversation in which nothing happened.
	 * </p>
	 */
	public void testARestoredSkillKeepsThePositionItWasLoadedIn() {
		Conversation first = new Conversation();
		first.add(new UserMessage("u1", T0, List.of(new TextContent("Do the alpha thing."))));
		Recorder loading = loadingThenDone("alpha");
		new AgentLoop(params(), loading, agentCatalog(null)).run(first, ignore());

		Conversation second = new Conversation();
		second.add(new UserMessage("u2", T0, List.of(new TextContent("And again."))));
		Recorder restored = new Recorder(List.of());
		new AgentLoop(params(), restored, agentCatalog(new LinkedHashSet<>(List.of("alpha"))))
			.run(second, ignore());

		System.out.println("[#346] tools after load    : " + loading.toolNames(1));
		System.out.println("[#346] tools after restore : " + restored.toolNames(0));

		assertEquals("A restored skill must produce the tool array its load produced.",
			loading.toolNames(1), restored.toolNames(0));
	}

	/** The skill's instructions reach the model as a tool result, never as a prompt rewrite. */
	public void testTheInstructionsTravelAsAMessage() {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Do the alpha thing."))));

		Recorder provider = loadingThenDone("alpha");
		new AgentLoop(params(), provider, agentCatalog(null)).run(conversation, ignore());

		assertFalse("Skill content must not be written into the system prompt.",
			provider.requests.get(1).systemPrompt().contains("Alpha instructions."));
		assertTrue("Skill content must arrive as a message.",
			provider.requests.get(1).messages().toString().contains("Alpha instructions."));
	}

	/** The run reports which skills its request carried, so a reader can attribute the tool tokens. */
	public void testTheCompositionNamesTheLoadedSkills() {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Do the alpha thing."))));

		List<AgentEvent.ContextComposition> compositions = new ArrayList<>();
		Recorder provider = loadingThenDone("alpha");
		new AgentLoop(params(), provider, agentCatalog(null)).run(conversation, event -> {
			if (event instanceof AgentEvent.ContextComposition composition) {
				compositions.add(composition);
			}
		});

		assertEquals(2, compositions.size());
		assertEquals(List.of(), compositions.get(0).loadedSkills());
		assertEquals(List.of("alpha"), compositions.get(1).loadedSkills());
	}

	/**
	 * The request names where the tool array stops being the same bytes.
	 *
	 * <p>
	 * What the Anthropic and Bedrock builders place their second cache breakpoint on. Before a load
	 * there is nothing behind the boundary, so it is the whole array; after one it is the array
	 * minus the skill's tools.
	 * </p>
	 */
	public void testTheRequestNamesTheCacheBoundaryOfTheToolArray() {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Do the alpha thing."))));

		Recorder provider = loadingThenDone("alpha");
		new AgentLoop(params(), provider, agentCatalog(null)).run(conversation, ignore());

		ProviderRequest before = provider.requests.get(0);
		ProviderRequest after = provider.requests.get(1);
		assertEquals(before.tools().size(), before.stableToolCount());
		assertEquals("The boundary must not move when a skill is loaded.",
			before.stableToolCount(), after.stableToolCount());
		assertEquals(after.tools().size() - 1, after.stableToolCount());
	}

	/** A compaction does not take a loaded skill's tools away. */
	public void testACompactionLeavesTheToolArrayAlone() {
		DynamicToolCatalog catalog = agentCatalog(null);
		ConversationMemory memory = new ConversationMemory(new Conversation(),
			new ByteBasedTokenEstimator(), new TruncatingStrategy(1), 200, 0);
		memory.add(new UserMessage("u1", T0, List.of(new TextContent("Do the alpha thing."))));

		Recorder loading = loadingThenDone("alpha");
		new AgentLoop(params(), loading, catalog, memory).run(ToolContext.EMPTY, ignore());
		List<String> afterLoad = loading.toolNames(1);

		for (int i = 0; i < 8; i++) {
			memory.add(new UserMessage("u" + i, T0, List.of(new TextContent("Question " + i
				+ " with enough words in it to push this conversation past the budget."))));
		}
		Recorder later = new Recorder(List.of());
		new AgentLoop(params(), later, catalog, memory).run(ToolContext.EMPTY, ignore());

		assertTrue("This test is only about the case in which a compaction really ran.",
			later.requests.get(0).messages().stream().anyMatch(CompactionMarker.class::isInstance));
		assertEquals("A compaction must not change what the agent can call.",
			afterLoad, later.toolNames(0));
		assertEquals(List.of("alpha"), catalog.loadedSkills());
	}
}
