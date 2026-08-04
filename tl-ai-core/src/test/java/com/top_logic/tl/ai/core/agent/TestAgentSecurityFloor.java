/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.security.AiSecurityMode;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Tests that the security mode an agent definition declares for itself reaches the run.
 *
 * <p>
 * The composition rule itself is tested in {@code TestAiSecurityMode} and
 * {@link TestTopLogicSecurityContext}; what is tested here is that the {@link AgentLoop} applies it -
 * once, before the first provider call - so that the mode a tool sees is the resolved one. That the
 * loop is the point of application is what makes a delegated child run inherit its parent's mode and
 * still honour a stricter declaration of its own, without any entry point having to know.
 * </p>
 */
public class TestAgentSecurityFloor extends TestCase {

	/**
	 * Tool execution establishes a TopLogic interaction context, so the {@code ThreadContextManager}
	 * module has to be running - and instantiating a {@link ToolAccessPolicy} resolves nested config
	 * types through the {@code TypeIndex}.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestAgentSecurityFloor.class)));
	}

	private static final String MODEL = "test-model";

	private static final Instant T0 = Instant.parse("2026-08-04T12:00:00Z");

	private static final TokenUsage USAGE = new TokenUsage(10, 5, 0, 0, 0);

	private static final ToolSchema OBSERVE_SCHEMA =
		new ToolSchema("observeMode", "Reports the security mode of the run it executes in.", "{}");

	/** A run of an agent that declares nothing carries the mode its caller's context carried. */
	public void testWithoutAFloorTheCallersModeReachesTheTools() {
		assertEquals(AiSecurityMode.LEGACY, observedMode(contextFor(null), null));
	}

	/**
	 * A run of an agent that declares a stricter mode carries that one - visibly, in the context the
	 * tools of the run receive.
	 */
	public void testAStricterFloorReachesTheTools() {
		assertEquals(AiSecurityMode.SYSTEM, observedMode(contextFor(null), AiSecurityMode.SYSTEM));
	}

	/** A run of an agent that declares a laxer mode than the deployment's carries the deployment's. */
	public void testALaxerFloorDoesNotReachTheTools() {
		assertEquals(AiSecurityMode.SYSTEM,
			observedMode(contextFor(AiSecurityMode.SYSTEM), AiSecurityMode.LEGACY));
	}

	/**
	 * A delegated child run is never laxer than its parent, whatever the child agent declares.
	 *
	 * <p>
	 * The child context is the real one {@code DelegationTool} builds - the parent's, with a fresh
	 * transaction scope - and the child's own loop is what applies the child agent's declaration to
	 * it. Since that can only narrow, the child of an enforced parent cannot come out laxer.
	 * </p>
	 */
	public void testAChildRunIsNeverLaxerThanItsParent() {
		ToolContext parent = contextFor(AiSecurityMode.SYSTEM);
		ToolContext child = parent.forDelegation(TransactionScope.NOOP);

		assertEquals(AiSecurityMode.SYSTEM, observedMode(child, AiSecurityMode.LEGACY));
		assertEquals(AiSecurityMode.SYSTEM, observedMode(child, null));
	}

	/** A child agent that declares a stricter mode than its parent's run keeps it. */
	public void testAChildKeepsItsOwnStricterFloor() {
		ToolContext parent = contextFor(null);
		assertEquals(AiSecurityMode.LEGACY, parent.securityMode());

		ToolContext child = parent.forDelegation(TransactionScope.NOOP);

		assertEquals(AiSecurityMode.SYSTEM, observedMode(child, AiSecurityMode.SYSTEM));
		assertEquals("The parent's own context is untouched: the mode is resolved per run.",
			AiSecurityMode.LEGACY, parent.securityMode());
	}

	/**
	 * A run whose context cannot be tightened to the declared mode is refused before it starts.
	 *
	 * <p>
	 * {@link ToolContext#EMPTY} has no principal, so there is nothing to authorize an
	 * {@link AiSecurityMode#ENFORCED} run against. Running it anyway would be the exact failure this
	 * feature must not have: an agent that asked to be checked and silently was not. The provider is
	 * never reached, so no round trip is wasted either.
	 * </p>
	 */
	public void testAnEnforcedFloorRefusesAContextThatCannotCarryIt() {
		LLMProvider mustNotBeCalled = (request, handler) -> {
			fail("The run must be refused before the first provider call.");
		};
		AgentLoop loop = new AgentLoop(params(AiSecurityMode.ENFORCED), mustNotBeCalled, emptyRegistry());

		try {
			loop.run(conversation(), ToolContext.EMPTY, NO_EVENTS);
			fail("A run of an agent declaring ENFORCED must not fall back to an unrestricted context.");
		} catch (IllegalStateException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("no principal"));
			assertTrue(expected.getMessage(),
				expected.getMessage().contains(AiSecurityMode.ENFORCED.name()));
		}
	}

	/**
	 * The TL-Script entry point resolves the declared mode itself rather than falling back to an
	 * unrestricted context.
	 *
	 * <p>
	 * It has to: the fallback has no principal, so a floor the entry point ignored could not be
	 * applied later by the loop - only refused. No {@code <tool-access>} policy is configured here,
	 * so the deployment ceiling is {@link AiSecurityMode#LEGACY} and the agent's declaration is the
	 * only thing that can ask for enforcement.
	 * </p>
	 */
	public void testForCurrentUserHonoursADeclaredMode() {
		ToolContext unenforced = ToolContext.forCurrentUser(null);
		try {
			assertEquals("Nothing asks for enforcement: unchanged behaviour.",
				AiSecurityMode.SYSTEM, unenforced.securityMode());
		} finally {
			unenforced.transaction().close();
		}

		try {
			ToolContext.forCurrentUser(AiSecurityMode.ENFORCED).transaction().close();
			fail("An agent declaring ENFORCED must not be started unrestricted just because the"
				+ " deployment does not enforce.");
		} catch (IllegalStateException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("authenticated user"));
		}
	}

	/**
	 * The mode a tool of the run observes, when the run's agent declares {@code floor}.
	 *
	 * @param context
	 *        The context the caller of the run hands to the loop.
	 * @param floor
	 *        The security mode the agent definition declares, or <code>null</code> for none.
	 * @return The {@link ToolContext#securityMode()} the tool saw.
	 */
	private static AiSecurityMode observedMode(ToolContext context, AiSecurityMode floor) {
		AtomicReference<AiSecurityMode> observed = new AtomicReference<>();
		ToolCatalog registry = new ToolCatalog();
		registry.register(observingTool(observed));
		registry.freeze();

		AgentLoop loop = new AgentLoop(params(floor),
			mockProvider(
				List.of(new ToolCall("call-1", OBSERVE_SCHEMA.name(), "{}")),
				List.of(new TextContent("Done."))),
			registry);

		AgentResult result = loop.run(conversation(), context, NO_EVENTS);

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertNotNull("The tool of the run was never executed, so nothing was observed.",
			observed.get());
		return observed.get();
	}

	/** A {@link ToolContext} carrying the mode the given policy resolves to. */
	private static ToolContext contextFor(AiSecurityMode deploymentMode) {
		SecurityContext security = TopLogicSecurityContext.forRun(policy(deploymentMode), null);
		return new ToolContext(security, ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP);
	}

	private static ToolAccessPolicy policy(AiSecurityMode mode) {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		if (mode != null) {
			policy.setSecurityMode(mode);
		}
		return policy;
	}

	private static AgentParams params(AiSecurityMode floor) {
		return new AgentParams(MODEL, "You are a test assistant.", AgentParams.DEFAULT_MAX_TURNS,
			AgentParams.DEFAULT_MAX_OUTPUT_TOKENS, Map.of(),
			AgentParams.DEFAULT_CONTEXT_WINDOW_TOKENS, AgentParams.DEFAULT_MAX_RETRIES,
			AgentParams.DEFAULT_TIMEOUT_SECONDS, null, msg -> true, floor);
	}

	private static Conversation conversation() {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Which mode is this?"))));
		return conversation;
	}

	private static ToolCatalog emptyRegistry() {
		ToolCatalog registry = new ToolCatalog();
		registry.freeze();
		return registry;
	}

	/** A read-only tool that records the security mode of the context it is executed with. */
	private static Tool observingTool(AtomicReference<AiSecurityMode> observed) {
		return new Tool() {

			@Override
			public String name() {
				return OBSERVE_SCHEMA.name();
			}

			@Override
			public String description() {
				return OBSERVE_SCHEMA.description();
			}

			@Override
			public ToolSchema schema() {
				return OBSERVE_SCHEMA;
			}

			@Override
			public ToolMode mode() {
				return ToolMode.READ;
			}

			@Override
			public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				observed.set(((ToolContext) context).securityMode());
				return ToolResult.of("ok");
			}
		};
	}

	/** A provider that answers each turn with the next of the given content lists. */
	@SafeVarargs
	private static LLMProvider mockProvider(List<ContentPart>... responses) {
		List<List<ContentPart>> turns = List.of(responses);
		int[] turn = { 0 };

		return (request, handler) -> {
			List<ContentPart> content = turns.get(turn[0]++);
			for (int n = 0; n < content.size(); n++) {
				if (content.get(n) instanceof ToolCall call) {
					handler.onEvent(new StreamEvent.ToolCallStart(n, call.id(), call.name()));
					handler.onEvent(new StreamEvent.ToolCallDelta(n, call.arguments()));
				} else if (content.get(n) instanceof TextContent text) {
					handler.onEvent(new StreamEvent.TextDelta(n, text.text()));
				}
			}
			String finishReason =
				content.stream().anyMatch(ToolCall.class::isInstance) ? "tool_use" : "end_turn";
			handler.onEvent(new StreamEvent.MessageComplete("msg", MODEL, finishReason, USAGE, content));
		};
	}

	private static final AgentEventListener NO_EVENTS = event -> {
		/* The events are not what these tests observe. */
	};
}
