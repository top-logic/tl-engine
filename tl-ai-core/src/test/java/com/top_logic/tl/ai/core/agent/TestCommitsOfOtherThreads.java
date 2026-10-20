/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.approval.ApprovalResult;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.util.model.ModelService;

/**
 * What an agent run sees of the commits other threads make while it runs (Gitea #647).
 *
 * <p>
 * A run holds one interaction for all of its turns - the chat's worker enters the window's
 * sub-session once - and an interaction reads the database at the revision it read first. Unless the
 * run moves that view, a commit made by anybody else during the run is invisible to it, and a write
 * to an object that commit changed is a write on an outdated version, which the knowledge base
 * refuses when the turn commits. The cases below are the ways another commit lands in a run: a
 * tool's own helpers, a person saving while the model thinks, a person saving while an approval is
 * open.
 * </p>
 *
 * <p>
 * Real agent turns against a real knowledge base: the model is scripted, the other threads are real
 * threads committing real transactions, and the object they all write is a
 * {@code test.objectedit:Cover}, whose text stands for the drafted section of the ticket. No LLM is
 * involved.
 * </p>
 */
public class TestCommitsOfOtherThreads extends BasicTestCase {

	private static final Instant T0 = Instant.parse("2026-10-03T12:00:00Z");

	private static final TokenUsage USAGE = new TokenUsage(10, 5, 0, 0, 0);

	/**
	 * Every tool is permitted; a call that writes needs an approval, as in the chat - and the
	 * approval callback of the test decides how long that takes.
	 */
	private static final SecurityContext SECURITY = new SecurityContext() {

		@Override
		public boolean hasToolPermission(Tool tool) {
			return true;
		}

		@Override
		public ExecutionMode executionMode() {
			return ExecutionMode.DEVELOPMENT;
		}
	};

	/** The text of the cover each test writes. */
	private TLStructuredTypePart _text;

	/** The object the run and the other threads write: a section, in the words of the ticket. */
	private TLObject _section;

	/** What the tools read, in the order they read it. */
	private List<String> _seen;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TLClass type = (TLClass) TLModelUtil.findType("test.objectedit:Cover");
		_text = type.getPart("text");
		_seen = new CopyOnWriteArrayList<>();
		try (Transaction tx = kb().beginTransaction(ResKey.text("Set up"))) {
			_section = ModelService.getInstance().getFactory().createObject(type, null);
			_section.tUpdate(_text, "empty");
			tx.commit();
		}
	}

	/**
	 * The case of the ticket: a tool lets helpers draft the section on threads of their own, in the
	 * run's sub-session, and waits for them. The next turn reads the draft and edits it.
	 */
	public void testTheNextTurnSeesWhatAToolsHelpersCommitted() {
		Tool draft = writeTool("draftSections", context -> {
			SubSessionContext runSession = ThreadContextManager.getSubSession();
			inSession(runSession, () -> _section.tUpdate(_text, "drafted"));
			return ToolResult.of("Drafted.");
		});
		Tool review = appendTool("reviewSection", ", reviewed");

		Run run = run(model(call("draftSections"), call("reviewSection"), answer("Done.")),
			ApprovalCallback.AUTO_APPROVE, draft, review);

		assertEquals("The review must read the draft its helpers committed, not the section as it was"
			+ " before.", List.of("drafted"), _seen);
		assertEquals(List.of("turn 1 committed", "turn 2 committed"), run.transactions());
		assertNull(run.notice());
		assertEquals("drafted, reviewed", text());
	}

	/**
	 * A person saves the section while the model thinks about its next turn, between two turns of the
	 * run - as somebody in the browser does, or a scheduled job. The next turn reads what was saved and
	 * builds on it.
	 */
	public void testATurnSeesWhatAnotherSessionSavedWhileTheModelThought() {
		Tool read = readTool("readSection");
		Tool append = appendTool("appendToSection", ", appended");

		Run run = run(model(
			call("readSection"),
			call("appendToSection").meanwhile(() -> elsewhere(() -> _section.tUpdate(_text, "saved by a person"))),
			answer("Done.")), ApprovalCallback.AUTO_APPROVE, read, append);

		assertEquals(List.of("empty", "saved by a person"), _seen);
		assertEquals(List.of("turn 2 committed"), run.transactions());
		assertNull(run.notice());
		assertEquals("saved by a person, appended", text());
	}

	/**
	 * A person saves the section while the approval of the call that is to change it is open - the
	 * moment in a run at which the run waits for a person. The approved call reads what was saved and
	 * builds on it.
	 */
	public void testAnApprovedCallSeesWhatWasSavedWhileItsApprovalWasOpen() {
		Tool append = appendTool("appendToSection", ", appended");
		ApprovalCallback personEditsFirst = request -> {
			elsewhere(() -> _section.tUpdate(_text, "edited in the browser"));
			return new ApprovalResult.Approved();
		};

		Run run = run(model(call("appendToSection"), answer("Done.")), personEditsFirst, append);

		assertEquals(List.of("edited in the browser"), _seen);
		assertEquals(List.of("turn 1 committed"), run.transactions());
		assertNull(run.notice());
		assertEquals("edited in the browser, appended", text());
	}

	/**
	 * The calls of one turn read one state of the database, whatever is committed while they run - an
	 * approval included once a call of the turn has run. The next turn reads the newer state.
	 */
	public void testTheCallsOfOneTurnReadOneState() {
		Tool first = writeTool("readAndLetOthersSave", context -> {
			_seen.add(text());
			elsewhere(() -> _section.tUpdate(_text, "saved meanwhile"));
			return ToolResult.of("Read.");
		});
		Tool second = writeTool("readAgain", context -> {
			_seen.add(text());
			return ToolResult.of("Read.");
		});
		Tool third = readTool("readSection");

		run(model(call("readAndLetOthersSave", "readAgain"), call("readSection"), answer("Done.")),
			ApprovalCallback.AUTO_APPROVE, first, second, third);

		assertEquals("Both calls of the first turn read the state the turn began with; the next turn"
			+ " reads what was saved meanwhile.", List.of("empty", "empty", "saved meanwhile"), _seen);
	}

	/**
	 * The calls of a concurrent batch run on threads of their own; they read the state their turn
	 * began with as well.
	 */
	public void testAConcurrentBatchReadsTheStateItsTurnBeganWith() {
		Tool one = readTool("readSection");
		Tool two = readTool("readSectionAgain");

		run(model(
			call("readSection"),
			call("readSection", "readSectionAgain")
				.meanwhile(() -> elsewhere(() -> _section.tUpdate(_text, "saved by a person"))),
			answer("Done.")), ApprovalCallback.AUTO_APPROVE, one, two);

		assertEquals(List.of("empty", "saved by a person", "saved by a person"), _seen);
	}

	/**
	 * Somebody saves the section after the turn read it and while the turn changes it: a real
	 * conflict. The turn's commit still fails, the turn is rolled back, and the model is told so
	 * (Gitea #305).
	 */
	public void testASaveAfterTheTurnReadTheSectionStillFailsItsCommit() {
		Tool rewrite = writeTool("rewriteSection", context -> {
			_seen.add(text());
			_section.tUpdate(_text, "rewritten");
			elsewhere(() -> _section.tUpdate(_text, "saved by a person"));
			return ToolResult.of("Rewritten.");
		});

		Run run = run(model(call("rewriteSection"), answer("Done.")), ApprovalCallback.AUTO_APPROVE, rewrite);

		assertEquals(List.of("empty"), _seen);
		assertEquals(1, run.transactions().size());
		assertTrue(run.transactions().get(0), run.transactions().get(0).startsWith("turn 1 rolled back: "));
		assertContains("could NOT be saved", run.notice());
		assertEquals("saved by a person", text());
	}

	/**
	 * A run started inside a transaction of its caller - a script run in a command that has one, a
	 * delegated run inside a turn that has written - must not move the caller's view: that would merge
	 * other threads' commits into a transaction that is not the run's to merge into. It reads what the
	 * caller reads, and the caller's transaction commits as before.
	 */
	public void testARunInsideACallersTransactionLeavesItsViewAlone() {
		TLObject other = cover("untouched");
		Tool read = readTool("readSection");

		try (Transaction callers = kb().beginTransaction(ResKey.text("Caller"))) {
			other.tUpdate(_text, "changed by the caller");

			run(model(
				call("readSection"),
				call("readSection").meanwhile(() -> elsewhere(() -> _section.tUpdate(_text, "saved by a person"))),
				answer("Done.")), ApprovalCallback.AUTO_APPROVE, read);

			callers.commit();
		}

		assertEquals(List.of("empty", "empty"), _seen);
		assertEquals("changed by the caller", other.tValue(_text));
		assertEquals("saved by a person", text());
	}

	/**
	 * A tool that waits for its helpers and goes on to work with what they wrote catches up with
	 * their commits itself: within its turn the framework does not move the view. What the turn has
	 * changed before is kept.
	 */
	public void testAToolThatCatchesUpReadsWhatItsHelpersCommitted() {
		TLObject other = cover("untouched");
		Tool draftAndCheck = writeTool("draftAndCheck", context -> {
			other.tUpdate(_text, "changed before the drafts");
			SubSessionContext runSession = ThreadContextManager.getSubSession();
			inSession(runSession, () -> _section.tUpdate(_text, "drafted"));
			_seen.add(text());

			context.catchUpWithCommits();

			String text = text();
			_seen.add(text);
			_section.tUpdate(_text, text + ", checked");
			return ToolResult.of("Drafted and checked.");
		});

		Run run = run(model(call("draftAndCheck"), answer("Done.")), ApprovalCallback.AUTO_APPROVE,
			draftAndCheck);

		assertEquals("Before catching up the tool reads the state its turn began with, after it what its"
			+ " helper committed.", List.of("empty", "drafted"), _seen);
		assertEquals(List.of("turn 1 committed"), run.transactions());
		assertEquals("drafted, checked", text());
		assertEquals("changed before the drafts", other.tValue(_text));
	}

	/**
	 * Catching up with a commit of somebody else that changed what the turn has changed fails the
	 * call: the turn could not be saved anyway, and it is rolled back.
	 */
	public void testCatchingUpWithAConflictingCommitFailsTheCall() {
		Tool rewrite = writeTool("rewriteAndWait", context -> {
			_section.tUpdate(_text, "mine");
			elsewhere(() -> _section.tUpdate(_text, "theirs"));
			context.catchUpWithCommits();
			return ToolResult.of("Rewritten.");
		});

		Run run = run(model(call("rewriteAndWait"), answer("Done.")), ApprovalCallback.AUTO_APPROVE, rewrite);

		assertEquals(List.of("turn 1 rolled back: Tool execution failed"), run.transactions());
		assertContains("changed and saved by somebody else in the meantime", run.toolResults().get(0));
		assertContains("were rolled back", run.notice());
		assertEquals("theirs", text());
	}

	/**
	 * The same conflict with a helper in the run's own sub-session: the knowledge base sees it only
	 * when the turn commits, and the commit fails on it. The turn is rolled back either way.
	 */
	public void testAConflictWithAHelperInTheRunsSessionFailsTheCommit() {
		Tool rewrite = writeTool("rewriteAndWait", context -> {
			_section.tUpdate(_text, "mine");
			SubSessionContext runSession = ThreadContextManager.getSubSession();
			inSession(runSession, () -> _section.tUpdate(_text, "theirs"));
			context.catchUpWithCommits();
			return ToolResult.of("Rewritten.");
		});

		Run run = run(model(call("rewriteAndWait"), answer("Done.")), ApprovalCallback.AUTO_APPROVE, rewrite);

		assertEquals(1, run.transactions().size());
		assertContains("preventing lost update", run.transactions().get(0));
		assertContains("could NOT be saved", run.notice());
		assertEquals("theirs", text());
	}

	/** One agent run, and what became of its turns' transactions. */
	private record Run(List<String> transactions, Conversation conversation) {

		/** The {@code [Framework notice]} the loop added, or <code>null</code>. */
		String notice() {
			for (Message message : conversation.messages()) {
				if (message instanceof UserMessage user) {
					for (ContentPart part : user.content()) {
						if (part instanceof TextContent content && content.text().startsWith("[Framework notice]")) {
							return content.text();
						}
					}
				}
			}
			return null;
		}

		/** What the tools answered, failures included, in the order they answered. */
		List<String> toolResults() {
			List<String> result = new ArrayList<>();
			for (Message message : conversation.messages()) {
				if (message instanceof ToolResultMessage answer) {
					if (answer.state() instanceof ToolState.Completed completed) {
						result.add(completed.result());
					} else if (answer.state() instanceof ToolState.Failed failed) {
						result.add(failed.error());
					}
				}
			}
			return result;
		}
	}

	/** Runs the scripted model with the given tools, until it ends its turn with text. */
	private static Run run(LLMProvider model, ApprovalCallback approval, Tool... tools) {
		ToolCatalog catalog = new ToolCatalog();
		for (Tool tool : tools) {
			catalog.register(tool);
		}
		catalog.freeze();

		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Draft the section."))));

		List<String> transactions = new CopyOnWriteArrayList<>();
		AgentEventListener listener = event -> {
			if (event instanceof AgentEvent.TransactionCommitted committed) {
				transactions.add("turn " + committed.turnNumber() + " committed");
			} else if (event instanceof AgentEvent.TransactionRolledBack rolledBack) {
				transactions.add("turn " + rolledBack.turnNumber() + " rolled back: " + rolledBack.reason());
			}
		};
		try (TransactionScope scope = new TransactionScope()) {
			AgentLoop loop = new AgentLoop(new AgentParams("test-model", "You write sections.", 5,
				AgentParams.DEFAULT_MAX_OUTPUT_TOKENS), model, catalog);
			loop.run(conversation, new ToolContext(SECURITY, approval, scope), listener);
		}
		return new Run(transactions, conversation);
	}

	/** One answer of the scripted model, and what happens elsewhere while the model is thinking. */
	private record Reply(List<String> calls, String text, Runnable meanwhile) {

		Reply meanwhile(Runnable elsewhere) {
			return new Reply(calls, text, elsewhere);
		}
	}

	private static Reply call(String... tools) {
		return new Reply(List.of(tools), null, () -> {
			// Nobody else does anything.
		});
	}

	private static Reply answer(String text) {
		return new Reply(List.of(), text, () -> {
			// Nobody else does anything.
		});
	}

	/** A model that gives the given answers, one per request, and fails a request beyond them. */
	private static LLMProvider model(Reply... replies) {
		AtomicInteger asked = new AtomicInteger();
		return (request, handler) -> {
			int index = asked.getAndIncrement();
			if (index >= replies.length) {
				throw new ProviderException(ProviderException.Kind.INVALID_REQUEST, 400,
					"The script has no answer for request " + (index + 1) + ".");
			}
			Reply reply = replies[index];
			reply.meanwhile().run();
			if (reply.text() != null) {
				TextContent text = new TextContent(reply.text());
				handler.onEvent(new StreamEvent.TextDelta(0, text.text()));
				handler.onEvent(new StreamEvent.MessageComplete("msg-" + index, "test-model", "end_turn",
					USAGE, List.of(text)));
				return;
			}
			List<ContentPart> calls = new ArrayList<>();
			for (int n = 0; n < reply.calls().size(); n++) {
				ToolCall call = new ToolCall("tc" + index + "-" + n, reply.calls().get(n), "{}");
				handler.onEvent(new StreamEvent.ToolCallStart(n, call.id(), call.name()));
				handler.onEvent(new StreamEvent.ToolCallDelta(n, call.arguments()));
				calls.add(call);
			}
			handler.onEvent(new StreamEvent.MessageComplete("msg-" + index, "test-model", "tool_use",
				USAGE, calls));
		};
	}

	/** A tool that reads the section and reports what it read. */
	private Tool readTool(String name) {
		return ToolStubs.builder(name).mode(ToolMode.READ).execute((args, context) -> {
			String text = text();
			_seen.add(text);
			return ToolResult.of(text);
		}).build();
	}

	/** A tool that reads the section and writes it back with the given text appended. */
	private Tool appendTool(String name, String suffix) {
		return writeTool(name, context -> {
			String text = text();
			_seen.add(text);
			_section.tUpdate(_text, text + suffix);
			return ToolResult.of("Appended.");
		});
	}

	/**
	 * A tool that writes in the turn's transaction, and therefore runs alone and asks for an approval
	 * first.
	 */
	private static Tool writeTool(String name, Function<ToolExecutionContext, ToolResult> body) {
		return ToolStubs.builder(name).mode(ToolMode.DATA_WRITE).concurrencySafe(false)
			.execute((args, context) -> body.apply(context)).build();
	}

	/** The section's text, as the current thread reads it. */
	private String text() {
		return (String) _section.tValue(_text);
	}

	private TLObject cover(String text) {
		TLClass type = (TLClass) _text.getOwner();
		try (Transaction tx = kb().beginTransaction(ResKey.text("Set up"))) {
			TLObject result = ModelService.getInstance().getFactory().createObject(type, null);
			result.tUpdate(_text, text);
			tx.commit();
			return result;
		}
	}

	/**
	 * Commits the given change on a thread and in a session of its own: somebody else - a person
	 * saving in the browser, a scheduled job.
	 */
	static void elsewhere(Runnable change) {
		onAnotherThread(() -> ThreadContextManager.inSystemInteraction(TestCommitsOfOtherThreads.class,
			() -> commit(change)));
	}

	/**
	 * Commits the given change on a thread of its own in the given sub-session, as a helper a tool
	 * starts does - {@code draftSections} enters the run's sub-session for each section it drafts.
	 */
	static void inSession(SubSessionContext session, Runnable change) {
		onAnotherThread(() -> ThreadContextManager.inContext(session, () -> commit(change)));
	}

	private static void commit(Runnable change) {
		try (Transaction tx = kb().beginTransaction(ResKey.text("Elsewhere"))) {
			change.run();
			tx.commit();
		}
	}

	/** Runs the given work on another thread and waits for it, as a tool waits for its helpers. */
	private static void onAnotherThread(Runnable work) {
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread thread = new Thread(() -> {
			try {
				work.run();
			} catch (Throwable ex) {
				failure.set(ex);
			}
		}, "other-thread");
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new AssertionError("Interrupted while waiting for the other thread.", ex);
		}
		if (failure.get() != null) {
			throw new AssertionError("The other thread failed: " + failure.get(), failure.get());
		}
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/** The knowledge base and the model: these runs write real model objects. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestCommitsOfOtherThreads.class),
				ModelService.Module.INSTANCE));
	}
}
