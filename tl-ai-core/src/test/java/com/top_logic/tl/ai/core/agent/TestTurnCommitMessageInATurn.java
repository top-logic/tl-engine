/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.locking.LockService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.model.TlAiCoreFactory;
import com.top_logic.tl.ai.core.provider.ScriptedModel;
import com.top_logic.tl.ai.core.provider.ScriptedModel.Reply;
import com.top_logic.tl.ai.core.provider.ScriptedModel.Script;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * The revision an agent turn commits is logged with what the turn did, see
 * {@link TurnCommitMessage}.
 *
 * <p>
 * Each case runs one scripted turn of a real {@link AgentLoop} against a real knowledge base and
 * reads the log of the revision it committed. No LLM is involved.
 * </p>
 */
public class TestTurnCommitMessageInATurn extends BasicTestCase {

	private static final String AGENT = "Planner";

	/** A writing tool that describes its call. */
	private static final String CREATE = "createProbe";

	/** A writing tool that leaves its message to the default. */
	private static final String TOUCH = "touchProbe";

	/** A tool that only reads. */
	private static final String LOOK = "lookProbe";

	/** The argument naming the object {@link #CREATE} creates. */
	private static final String NAME_ARG = "name";

	private static final Instant T0 = Instant.parse("2026-11-11T12:00:00Z");

	private static final TokenUsage USAGE = new TokenUsage(10, 5, 0, 0, 0);

	/**
	 * The first paragraph of what the model wrote alongside its calls is the description, after the
	 * agent's name.
	 */
	public void testTheModelsTextDescribesTheTurn() {
		String name = unique();
		Revision revision = runTurn(Reply.of(
			new TextContent("I am creating the agent " + name + ".\n\nAfter that I check it."),
			call("c1", CREATE, name)));

		assertNotNull("The turn wrote, so it committed.", revision);
		assertEquals(AGENT + ": I am creating the agent " + name + ".", english(revision.getLog()));
	}

	/** A long paragraph is shortened at a word boundary. */
	public void testALongTextIsShortened() {
		String text = "Creating " + "the probe agent again ".repeat(20);
		Revision revision = runTurn(Reply.of(new TextContent(text), call("c1", CREATE, unique())));

		String log = english(revision.getLog());
		String description = log.substring((AGENT + ": ").length());
		assertTrue("Shortened: " + log, description.endsWith(TurnCommitMessage.ELLIPSIS));
		assertTrue("At most the limit: " + description.length(),
			description.length() <= TurnCommitMessage.MAX_TEXT_LENGTH);
		assertTrue("At a word boundary: " + description,
			text.startsWith(description.substring(0, description.length() - 1) + " "));
	}

	/**
	 * Without text of the model, the messages of the writing calls describe the turn: each once, in
	 * the order the model called them, a tool's translated message translated.
	 */
	public void testWithoutTextTheToolsDescribeTheTurn() {
		String name = unique();
		Revision revision = runTurn(Reply.of(
			call("c1", CREATE, name),
			call("c2", TOUCH, null),
			call("c3", LOOK, null),
			call("c4", CREATE, name)));

		assertEquals(AGENT + ": Created " + name + "; Tool call " + TOUCH, english(revision.getLog()));
		assertEquals(AGENT + ": Created " + name + "; Werkzeugaufruf " + TOUCH,
			Resources.getInstance(Locale.GERMAN).getString(revision.getLog()));
	}

	/** A turn that only reads commits nothing. */
	public void testAReadingTurnCommitsNothing() {
		List<AgentEvent> events = new ArrayList<>();
		Revision revision = runTurn(Reply.of(new TextContent("Let me look."), call("c1", LOOK, null)), events);

		assertNull(revision);
		assertFalse(events.stream().anyMatch(AgentEvent.TransactionCommitted.class::isInstance));
	}

	private static Revision runTurn(Reply reply) {
		return runTurn(reply, new ArrayList<>());
	}

	/**
	 * Runs one turn of the agent {@link #AGENT} answering with the given reply, then ends the run.
	 *
	 * @return The revision the run committed last, or <code>null</code>.
	 */
	private static Revision runTurn(Reply reply, List<AgentEvent> events) {
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(createProbe());
		catalog.register(probe(TOUCH, ToolMode.DATA_WRITE));
		catalog.register(probe(LOOK, ToolMode.READ));
		catalog.freeze();
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Do it."))));
		Revision[] result = new Revision[1];
		TLContextManager.inPersonContext(PersonManager.getManager().getRoot(), () -> {
			try (TransactionScope scope = new TransactionScope()) {
				ToolContext context = new ToolContext(SecurityContext.UNRESTRICTED, ApprovalCallback.AUTO_APPROVE,
					scope);
				context.executionHandle().setAgentName(AGENT);
				AgentLoop loop = new AgentLoop(new AgentParams("test-model", "You change things.", 5,
					AgentParams.DEFAULT_MAX_OUTPUT_TOKENS),
					new ScriptedModel(Script.of(reply.withUsage(USAGE), Reply.text("Done.").withUsage(USAGE))),
					catalog);
				loop.run(conversation, context, events::add);
				result[0] = scope.commitRevision();
			}
		});
		return result[0];
	}

	private static ToolCall call(String id, String tool, String name) {
		return new ToolCall(id, tool, name == null ? "{}" : "{\"" + NAME_ARG + "\": \"" + name + "\"}");
	}

	private static String english(ResKey message) {
		return Resources.getInstance(Locale.ENGLISH).getString(message);
	}

	private static String unique() {
		return "probe-" + UUID.randomUUID();
	}

	/**
	 * Creates an agent and names the argument {@link #NAME_ARG} in its commit message. The agent
	 * gets a name of its own, so that a call repeated with the same argument creates no duplicate.
	 */
	private static Tool createProbe() {
		return new Probe(CREATE, ToolMode.DATA_WRITE) {
			@Override
			public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				TlAiCoreFactory.getInstance().createAgent().tUpdate(TlAiCoreFactory.getNameAgentAttr(), unique());
				return ToolResult.of("Created.");
			}

			@Override
			public ResKey commitMessage(Map<String, Object> arguments) {
				return ResKey.text("Created " + arguments.get(NAME_ARG));
			}
		};
	}

	/** A tool of the given mode; a writing one creates an agent with a name of its own. */
	private static Tool probe(String name, ToolMode mode) {
		return new Probe(name, mode);
	}

	private static class Probe implements Tool {

		private final String _name;

		private final ToolMode _mode;

		Probe(String name, ToolMode mode) {
			_name = name;
			_mode = mode;
		}

		@Override
		public String name() {
			return _name;
		}

		@Override
		public String description() {
			return "A probe.";
		}

		@Override
		public ToolSchema schema() {
			return new ToolSchema(_name, description(), "{}");
		}

		@Override
		public ToolMode mode() {
			return _mode;
		}

		@Override
		public String group() {
			return _mode == ToolMode.READ ? ToolGroups.DATA_READ : ToolGroups.DATA_WRITE;
		}

		@Override
		public boolean isConcurrencySafe() {
			// One after the other, each in the turn's transaction on the loop's thread.
			return false;
		}

		@Override
		public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
			if (_mode != ToolMode.READ) {
				TlAiCoreFactory.getInstance().createAgent().tUpdate(TlAiCoreFactory.getNameAgentAttr(), unique());
			}
			return ToolResult.of("Done.");
		}
	}

	/** The knowledge base, the model, real accounts and locks: these turns write real model objects. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestTurnCommitMessageInATurn.class),
				ModelService.Module.INSTANCE, LockService.Module.INSTANCE));
	}
}
