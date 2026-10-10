/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.PermissionDecision;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.approval.ApprovalRequest;
import com.top_logic.tl.ai.core.agent.approval.ApprovalResult;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Tests for the {@link SecurityContextFactory} seam: an application decides per call whether a write
 * needs a person, and the decision travels the real approval path (Gitea #381).
 */
public class TestSecurityContextFactory extends TestCase {

	/**
	 * Boots the thread-context module {@link AgentLoop} needs to execute a tool call.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestSecurityContextFactory.class)));
	}

	private static final Instant T0 = Instant.parse("2026-09-19T09:00:00Z");

	private static final String MODEL = "test-model";

	private static final TokenUsage USAGE = new TokenUsage(10, 5, 0, 0, 0);

	/** The schema of the one write tool these tests drive. */
	private static final ToolSchema WRITE_SECTION = new ToolSchema("writeSection", "Writes a section",
		"{\"type\":\"object\",\"properties\":{\"section\":{\"type\":\"string\"}}}");

	/** Stands in for the context this product would have built for the run. */
	private static final SecurityContext PERMITS = new SecurityContext() {
		@Override
		public boolean hasToolPermission(Tool tool) {
			return true;
		}

		@Override
		public ExecutionMode executionMode() {
			return ExecutionMode.DEVELOPMENT;
		}
	};

	/**
	 * A factory an application could write: the same {@code DATA_WRITE} tool runs unasked on a
	 * section the agent itself last wrote and is asked about on one a person wrote.
	 */
	private static final class SectionApprovalFactory implements SecurityContextFactory {

		/** What each call was told to do, in order. */
		final List<RunPrincipal> runs = new ArrayList<>();

		@Override
		public SecurityContext createSecurityContext(RunPrincipal run) {
			runs.add(run);
			// The shape an application writes: everything stays as this product decided it, and one
			// answer is the application's. In production the delegate is
			// DefaultSecurityContextFactory.defaultContext(run), which needs an account and a
			// knowledge base; here it is a stand-in that permits.
			return new SecurityContextProxy(PERMITS) {
				@Override
				public PermissionDecision checkApproval(Tool tool, ToolCall call) {
					if (call.arguments().contains("\"empty\"")) {
						return new PermissionDecision.Allowed();
					}
					return new PermissionDecision.NeedsApproval("A person wrote this section.", call);
				}
			};
		}
	}

	/**
	 * Two calls of one {@code DATA_WRITE} tool, one approved by the application and one handed to the
	 * user - which is what neither {@code ApprovalMode} setting could express.
	 */
	public void testFactoryDecidesPerCall() {
		SectionApprovalFactory factory = new SectionApprovalFactory();
		SecurityContext security = factory.createSecurityContext(
			new RunPrincipal(null, RunEntryPoint.CHAT, false, "conv-1"));

		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Write both sections"))));

		LLMProvider provider = mockProvider(
			List.of(new ToolCall("tc1", "writeSection", "{\"section\":\"empty\"}")),
			List.of(new ToolCall("tc2", "writeSection", "{\"section\":\"written by a person\"}")),
			List.of(new TextContent("Both done.")));

		List<String> executed = new ArrayList<>();
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(writeTool(executed));
		catalog.freeze();

		List<ApprovalRequest> asked = new ArrayList<>();
		ApprovalCallback approval = request -> {
			asked.add(request);
			return new ApprovalResult.Approved();
		};

		AgentLoop loop = new AgentLoop(params(), provider, catalog);
		loop.run(conversation, new ToolContext(security, approval), listener());

		assertEquals("Both calls ran.", 2, executed.size());
		assertEquals("Only the second call reached the user.", 1, asked.size());
		assertEquals("written by a person", asked.get(0).arguments().get("section"));
	}

	/**
	 * What the factory is told about the run.
	 */
	public void testFactoryIsToldAboutTheRun() {
		SectionApprovalFactory factory = new SectionApprovalFactory();
		factory.createSecurityContext(new RunPrincipal(null, RunEntryPoint.CHAT, false, "conv-7"));

		assertEquals(1, factory.runs.size());
		RunPrincipal run = factory.runs.get(0);
		assertEquals(RunEntryPoint.CHAT, run.entryPoint());
		assertFalse("A chat run has a human who can answer.", run.background());
		assertEquals("conv-7", run.conversationId());
	}

	/**
	 * A run must state how it was started; a factory that switches on it may not be handed
	 * <code>null</code>.
	 */
	public void testEntryPointIsMandatory() {
		try {
			new RunPrincipal(null, null, false);
			fail("A run with no entry point must be refused.");
		} catch (NullPointerException expected) {
			// Expected.
		}
	}

	/**
	 * The shipped default still refuses a run that cannot name its principal - the seam did not open a
	 * way past that.
	 */
	public void testDefaultStillNeedsAPrincipal() {
		try {
			DefaultSecurityContextFactory.INSTANCE.createSecurityContext(
				new RunPrincipal(null, RunEntryPoint.CHAT, false));
			fail("A run without an account must be refused.");
		} catch (IllegalStateException expected) {
			assertTrue(expected.getMessage().contains("authenticated user"));
		}
	}

	/**
	 * An installation that configures nothing gets the behaviour it had before the seam existed.
	 */
	public void testDefaultIsConfigured() {
		AgentRuntimeService.Config<?> config = TypedConfiguration.newConfigItem(AgentRuntimeService.Config.class);

		assertNotNull("The property has a default item.", config.getSecurityContext());
		assertEquals(DefaultSecurityContextFactory.class,
			config.getSecurityContext().getImplementationClass());
	}

	/**
	 * Every question {@link SecurityContext} asks is delegated.
	 *
	 * <p>
	 * A method added to the interface and forgotten here would silently answer with the interface's
	 * permissive default instead of with the context the proxy wraps - which is the whole thing this
	 * class exists to prevent.
	 * </p>
	 */
	public void testProxyDelegatesEveryAnswer() {
		for (Method method : SecurityContext.class.getDeclaredMethods()) {
			if (method.isSynthetic() || Modifier.isStatic(method.getModifiers())) {
				continue;
			}
			try {
				SecurityContextProxy.class.getDeclaredMethod(method.getName(),
					method.getParameterTypes());
			} catch (NoSuchMethodException ex) {
				fail(SecurityContextProxy.class.getSimpleName() + " does not delegate "
					+ method.getName() + "(), so an application's context would answer it with the"
					+ " interface's permissive default.");
			}
		}
	}

	private static AgentParams params() {
		return new AgentParams(MODEL, "You are a test assistant.", 10,
			AgentParams.DEFAULT_MAX_OUTPUT_TOKENS);
	}

	private static Tool writeTool(List<String> executed) {
		return ToolStubs.builder("writeSection").description("Writes a section").schema(WRITE_SECTION)
			.mode(ToolMode.DATA_WRITE).execute((args, context) -> {
				executed.add(String.valueOf(args.get("section")));
				return ToolResult.of("Written.");
			}).build();
	}

	@SafeVarargs
	private static LLMProvider mockProvider(List<ContentPart>... responses) {
		AtomicInteger callIndex = new AtomicInteger(0);
		List<List<ContentPart>> responseList = List.of(responses);

		return (request, handler) -> {
			int index = callIndex.getAndIncrement();
			if (index >= responseList.size()) {
				throw new ProviderException(ProviderException.Kind.INVALID_REQUEST, 400,
					"Mock provider: no more responses (call " + index + ")");
			}
			List<ContentPart> content = responseList.get(index);
			for (int i = 0; i < content.size(); i++) {
				if (content.get(i) instanceof TextContent text) {
					handler.onEvent(new StreamEvent.TextDelta(i, text.text()));
				} else if (content.get(i) instanceof ToolCall call) {
					handler.onEvent(new StreamEvent.ToolCallStart(i, call.id(), call.name()));
					handler.onEvent(new StreamEvent.ToolCallDelta(i, call.arguments()));
				}
			}
			String finishReason = content.stream().anyMatch(ToolCall.class::isInstance)
				? "tool_use" : "end_turn";
			handler.onEvent(new StreamEvent.MessageComplete(
				"msg-" + index, MODEL, finishReason, USAGE, content));
		};
	}

	private static AgentEventListener listener() {
		return event -> {
			// Nothing to observe here.
		};
	}

}
