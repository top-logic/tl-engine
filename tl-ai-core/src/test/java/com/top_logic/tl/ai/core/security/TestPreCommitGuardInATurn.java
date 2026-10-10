/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.locking.LockService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyUtil;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
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
import com.top_logic.tl.ai.core.security.PreCommitGuard.Violation;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.model.ObjectEditTool;
import com.top_logic.tl.ai.core.tools.script.ChangeDataTool;
import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * Tests the pre-commit guard where it runs: at the end of a real agent turn, over what a tool left in
 * the turn's transaction of a real knowledge base.
 *
 * <p>
 * The cases are about attributes whose values the engine keeps outside the row of the object holding
 * them, so that a change books nothing on that object: a reference stored as links (Gitea #631), the
 * translations of an internationalized attribute and a composition kept in the rows of its parts
 * (Gitea #638). The account's rights are a stand-in that withholds the one question a case is about,
 * because what is under test is that the guard asks it - what the engine answers for a real role
 * model is pinned against real accounts by {@code SecDemoPreCommitGuardTest} in tl-ai-demo. The model
 * is scripted: it calls one tool once and ends the turn. No LLM is involved.
 * </p>
 */
public class TestPreCommitGuardInATurn extends BasicTestCase {

	private static final String TOOL = "writeUnchecked";

	private static final Instant T0 = Instant.parse("2026-10-03T12:00:00Z");

	private static final TokenUsage USAGE = new TokenUsage(10, 5, 0, 0, 0);

	/**
	 * A tool that checks nothing changes a reference the engine stores as a link: the guard asks for
	 * the right to write that reference of the object holding it, is refused, and rolls the turn back.
	 */
	public void testAnUncheckedChangeOfALinkStoredReferenceIsRolledBack() {
		TLObject memory = memory("FACT", null);
		TLStructuredTypePart kind = part(memory, "kind");
		StandIn rights = StandIn.withholding(Question.write(memory, kind));

		Turn turn = runTurn(rights, unchecked(() -> memory.tUpdate(kind, kind("PREFERENCE"))), "{}");

		assertFalse("The turn must not commit: the account may not write the kind of this memory.",
			turn.committed());
		assertEquals("Pre-commit security check failed", turn.rollbackReason());
		assertEquals("FACT", kindOf(memory));
		assertTrue("The guard must ask about the reference itself, but was asked: " + rights.asked(),
			rights.asked().contains(Question.write(memory, kind)));
		assertContains("Write on objects of type 'tl.ai.execution:UserMemory'", turn.notice());
	}

	/**
	 * The violation the guard finds for that change names the reference, which the model's notice
	 * does not: the check the turn ran, over the same change, read before the turn ends.
	 */
	public void testTheViolationNamesTheReference() {
		TLObject memory = memory("FACT", null);
		TLStructuredTypePart kind = part(memory, "kind");
		KnowledgeBase kb = kb();

		List<Violation> violations;
		try (Transaction tx = kb.beginTransaction()) {
			memory.tUpdate(kind, kind("PREFERENCE"));
			violations = new ModelSecurityPreCommitGuard().check(
				security(StandIn.withholding(Question.write(memory, kind))), ToolChangeSet.pending(kb));
		}

		assertEquals(1, violations.size());
		Violation violation = violations.get(0);
		assertSame(memory, violation.object());
		assertEquals(SimpleBoundCommandGroup.WRITE, violation.operation());
		assertEquals(kind, violation.attribute());
		assertEquals("tl.ai.execution:UserMemory", violation.typeName());
	}

	/**
	 * Deleting an object others refer to is checked as the deletion: it needs no right to write the
	 * objects that referred to it, whose links the engine drops as a consequence.
	 */
	public void testDeletingAReferencedObjectNeedsNoWriteOnTheReferrers() {
		TLObject session = session();
		TLObject memory = memory("FACT", session);
		StandIn rights = StandIn.withholding(question -> question.object().equals(memory));

		Turn turn = runTurn(rights, unchecked(session::tDelete), "{}");

		assertTrue("The turn must commit: deleting the session is permitted, and nothing else was"
			+ " written. Asked: " + rights.asked(), turn.committed());
		assertFalse(session.tValid());
		assertNull(memory.tValue(part(memory, "session")));
		assertTrue("The guard must ask for the deletion, but was asked: " + rights.asked(),
			rights.asked().contains(Question.delete(session)));
	}

	/**
	 * A tool that checks before it writes and the guard ask the same question about a link-stored
	 * reference: a change both permit commits.
	 */
	public void testACheckingToolAndTheGuardAgreeOnAPermittedChange() {
		TLObject memory = memory("FACT", null);
		TLStructuredTypePart kind = part(memory, "kind");
		StandIn rights = StandIn.withholding(question -> false);

		Turn turn = runTurn(rights, updateObject(), arguments(memory, "kind", "PREFERENCE"));

		assertTrue("The turn must commit: " + turn.toolResult(), turn.committed());
		assertEquals("PREFERENCE", kindOf(memory));
		assertEquals("The tool asks before it writes, the guard asks again before the commit.", 2,
			rights.asked().stream().filter(Question.write(memory, kind)::equals).count());
	}

	/**
	 * A change the account may not make is refused by the checking tool, naming the attribute, before
	 * anything is written - so the guard finds nothing to refuse a second time.
	 */
	public void testACheckingToolRefusesFirstAndTheGuardFindsNothing() {
		TLObject memory = memory("FACT", null);
		TLStructuredTypePart kind = part(memory, "kind");
		StandIn rights = StandIn.withholding(Question.write(memory, kind));

		Turn turn = runTurn(rights, updateObject(), arguments(memory, "kind", "PREFERENCE"));

		assertContains("may not change 'kind'", turn.toolResult());
		assertNull("Nothing was written, so nothing is rolled back.", turn.rollbackReason());
		assertNull(turn.notice());
		assertEquals("FACT", kindOf(memory));
	}

	/**
	 * A tool that checks nothing changes a translation of an internationalized attribute, which the
	 * engine keeps in a row of a table of its own: the guard asks for the right to write that
	 * attribute of the object holding it, is refused, and rolls the turn back (Gitea #638).
	 */
	public void testAnUncheckedChangeOfATranslationIsRolledBack() {
		assertUncheckedCaptionChangeIsRolledBack(TestToolChangeSet.text("one", "eins"),
			TestToolChangeSet.text("one", "zwei"));
	}

	/** The same for a translation the tool adds, for which the engine creates a row. */
	public void testAnUncheckedNewTranslationIsRolledBack() {
		assertUncheckedCaptionChangeIsRolledBack(TestToolChangeSet.text("one", null),
			TestToolChangeSet.text("one", "eins"));
	}

	/** The same for a translation the tool drops, whose row the engine deletes. */
	public void testAnUncheckedDroppedTranslationIsRolledBack() {
		assertUncheckedCaptionChangeIsRolledBack(TestToolChangeSet.text("one", "eins"),
			TestToolChangeSet.text("one", null));
	}

	private static void assertUncheckedCaptionChangeIsRolledBack(ResKey before, ResKey after) {
		TLObject cover = cover(before);
		TLStructuredTypePart caption = part(cover, "caption");
		StandIn rights = StandIn.withholding(Question.write(cover, caption));

		Turn turn = runTurn(rights, unchecked(() -> cover.tUpdate(caption, after)), "{}");

		assertFalse("The turn must not commit: the account may not write the caption of this cover.",
			turn.committed());
		assertEquals("Pre-commit security check failed", turn.rollbackReason());
		assertEquals(translations(before), translations((ResKey) cover.tValue(caption)));
		assertTrue("The guard must ask about the attribute itself, but was asked: " + rights.asked(),
			rights.asked().contains(Question.write(cover, caption)));
		assertContains("Write on objects of type 'test.objectedit:Cover'", turn.notice());
	}

	/**
	 * The violation the guard finds for a dropped translation names the attribute: the check the
	 * turn ran, over the same change, read before the turn ends.
	 */
	public void testTheViolationNamesTheTranslatedAttribute() {
		TLObject cover = cover(TestToolChangeSet.text("one", "eins"));
		TLStructuredTypePart caption = part(cover, "caption");
		KnowledgeBase kb = kb();

		List<Violation> violations;
		try (Transaction tx = kb.beginTransaction()) {
			cover.tUpdate(caption, TestToolChangeSet.text("one", null));
			violations = new ModelSecurityPreCommitGuard().check(
				security(StandIn.withholding(Question.write(cover, caption))), ToolChangeSet.pending(kb));
		}

		assertEquals(1, violations.size());
		Violation violation = violations.get(0);
		assertSame(cover, violation.object());
		assertEquals(SimpleBoundCommandGroup.WRITE, violation.operation());
		assertEquals(caption, violation.attribute());
		assertEquals("test.objectedit:Cover", violation.typeName());
	}

	/**
	 * Deleting an object with a translated attribute needs no right beyond the deletion: the engine
	 * drops the rows of the translations with it, and they are no write of the attribute and no
	 * deletion of their own.
	 */
	public void testDeletingAnObjectWithATranslationNeedsOnlyTheDelete() {
		TLObject cover = cover(TestToolChangeSet.text("one", "eins"));
		StandIn rights = StandIn.withholding(question -> !question.equals(Question.delete(cover)));

		Turn turn = runTurn(rights, unchecked(cover::tDelete), "{}");

		assertTrue("The turn must commit: the account may delete the cover, and nothing else was written."
			+ " Asked: " + rights.asked(), turn.committed());
		assertFalse(cover.tValid());
		assertEquals(List.of(Question.delete(cover)), rights.asked());
	}

	/**
	 * A tool that checks nothing puts a part into a composition that keeps the container in the row
	 * of each part, as tl.doc keeps the children of a page: the row that changes is the part's, but
	 * the guard asks for the right to write the composition of the container, is refused, and rolls
	 * the turn back (Gitea #638).
	 */
	public void testAnUncheckedChangeOfACompositionKeptInItsPartsIsRolledBack() {
		TLObject shelf = newCommitted("test.objectedit:Shelf");
		TLObject book = newCommitted("test.objectedit:Book");
		TLStructuredTypePart books = part(shelf, "books");
		StandIn rights = StandIn.withholding(Question.write(shelf, books));

		Turn turn = runTurn(rights, unchecked(() -> shelf.tUpdate(books, List.of(book))), "{}");

		assertFalse("The turn must not commit: the account may not write the books of this shelf.",
			turn.committed());
		assertEquals("Pre-commit security check failed", turn.rollbackReason());
		assertEquals(List.of(), shelf.tValue(books));
		assertTrue("The guard must ask about the composition of the container, but was asked: " + rights.asked(),
			rights.asked().contains(Question.write(shelf, books)));
		assertContains("Write on objects of type 'test.objectedit:Shelf'", turn.notice());
	}

	/** One question the guard or a tool asked the account's rights. */
	// --- The initial values of a new object (#683) -------------------------------------------------

	/**
	 * A tool that checks nothing creates an object and gives it a value the account may not set on a
	 * new object: the guard asks what the create dialogs ask, and the turn is rolled back. The notice
	 * names the attribute, because the model wrote that value itself.
	 */
	public void testAnUncheckedInitialValueIsRolledBack() {
		StandIn rights = StandIn.withholding(writeOf("subject"));

		Turn turn = runTurn(rights, unchecked(() -> note("Unchecked initial value", null)), "{}");

		assertFalse("The turn must not commit: the account may not set the subject of a new note.",
			turn.committed());
		assertEquals("Pre-commit security check failed", turn.rollbackReason());
		assertContains("setting 'subject' of new objects of type 'test.objectedit:Note'", turn.notice());
		assertEquals(0, notesWithSubject("Unchecked initial value"));
	}

	/**
	 * The case of the issue: a {@code changeData} script creates the object and sets the value. The
	 * tool asks for the right to change data, not for this attribute of a new object, so the guard is
	 * what refuses it.
	 */
	public void testAChangeDataScriptCannotSetARestrictedInitialValue() {
		StandIn rights = StandIn.withholding(writeOf("subject"));

		Turn turn = runTurn(rights, changeData(), "{\"script\": \"new(`test.objectedit:Note`)"
			+ ".set(`test.objectedit:Note#subject`, 'Set by a script')\"}");

		assertFalse("The turn must not commit: the script set a value the account may not set.",
			turn.committed());
		assertContains("setting 'subject' of new objects of type 'test.objectedit:Note'", turn.notice());
		assertEquals(0, notesWithSubject("Set by a script"));
	}

	/**
	 * The value the model gives a new object needs no right: the engine sets it, as it does for an
	 * object a user creates in a dialog without being allowed to edit that field.
	 */
	public void testTheDefaultValueNeedsNoRight() {
		StandIn rights = StandIn.withholding(writeOf("status"));

		Turn turn = runTurn(rights, unchecked(() -> note("Default status", null)), "{}");

		assertTrue("A default the account did not set must not refuse the turn: " + turn.notice(),
			turn.committed());
		assertEquals(1, notesWithSubject("Default status"));
	}

	/** A value other than the default is given by the turn, and needs the right. */
	public void testAnotherValueThanTheDefaultNeedsTheRight() {
		StandIn rights = StandIn.withholding(writeOf("status"));

		Turn turn = runTurn(rights, unchecked(() -> note("Closed status", "closed")), "{}");

		assertFalse("The account may not set the status of a new note.", turn.committed());
		assertContains("setting 'status'", turn.notice());
	}

	/** A permitted initial value commits, and the guard asked about it. */
	public void testAPermittedInitialValueCommits() {
		StandIn rights = StandIn.withholding(question -> false);

		Turn turn = runTurn(rights, unchecked(() -> note("Permitted subject", null)), "{}");

		assertTrue(turn.notice(), turn.committed());
		assertTrue("The guard must ask about the subject of the new note: " + rights.asked(),
			rights.asked().stream().anyMatch(writeOf("subject")));
	}

	/** Every question about writing the attribute of the given name. */
	private static Predicate<Question> writeOf(String attribute) {
		return question -> question.operation() == SimpleBoundCommandGroup.WRITE && question.attribute() != null
			&& attribute.equals(question.attribute().getName());
	}

	/** Creates a note with the given subject and, unless <code>null</code>, the given status. */
	private static void note(String subject, String status) {
		TLClass type = (TLClass) TLModelUtil.findType("test.objectedit:Note");
		TLObject note = ModelService.getInstance().getFactory().createObject(type, null);
		note.tUpdate(type.getPart("subject"), subject);
		if (status != null) {
			note.tUpdate(type.getPart("status"), status);
		}
	}

	private static int notesWithSubject(String subject) {
		TLClass type = (TLClass) TLModelUtil.findType("test.objectedit:Note");
		int count = 0;
		for (TLObject note : MetaElementUtil.getAllInstancesOf(type, TLObject.class)) {
			if (subject.equals(note.tValue(type.getPart("subject")))) {
				count++;
			}
		}
		return count;
	}

	/** The tool that changes data with a TL-Script. */
	private static Tool changeData() {
		for (Tool tool : AnnotatedTools.from(new ChangeDataTool())) {
			if ("changeData".equals(tool.name())) {
				return tool;
			}
		}
		throw new AssertionError("ChangeDataTool has no changeData.");
	}

	private record Question(BoundCommandGroup operation, TLObject object, TLStructuredTypePart attribute) {

		static Question write(TLObject object, TLStructuredTypePart attribute) {
			return new Question(SimpleBoundCommandGroup.WRITE, object, attribute);
		}

		static Question delete(TLObject object) {
			return new Question(SimpleBoundCommandGroup.DELETE, object, null);
		}

		/**
		 * The object by its identifier: the engine's own text of a link that is deleted by now never
		 * returns (a {@code StackOverflowError}), and a failure message must not be one.
		 */
		@Override
		public String toString() {
			return operation.getID() + " " + ReadFilter.idOf(object)
				+ (attribute == null ? "" : "#" + attribute.getName());
		}
	}

	/**
	 * Rights that grant every question but the withheld ones, and record which they were asked.
	 *
	 * <p>
	 * An object-level question is withheld with every attribute of it: the engine grants no attribute
	 * of an object it does not grant.
	 * </p>
	 */
	private static final class StandIn implements ModelSecurity {

		private final Predicate<Question> _withheld;

		private final List<Question> _asked = new CopyOnWriteArrayList<>();

		private StandIn(Predicate<Question> withheld) {
			_withheld = withheld;
		}

		static StandIn withholding(Question question) {
			return new StandIn(question::equals);
		}

		static StandIn withholding(Predicate<Question> withheld) {
			return new StandIn(withheld);
		}

		List<Question> asked() {
			return _asked;
		}

		private boolean answer(Question question) {
			_asked.add(question);
			return !_withheld.test(question)
				&& (question.attribute() == null
					|| !_withheld.test(new Question(question.operation(), question.object(), null)));
		}

		@Override
		public boolean canRead(TLObject obj) {
			return true;
		}

		@Override
		public boolean canRead(TLObject obj, TLStructuredTypePart attribute) {
			return true;
		}

		@Override
		public boolean canWrite(TLObject obj) {
			return answer(new Question(SimpleBoundCommandGroup.WRITE, obj, null));
		}

		@Override
		public boolean canWrite(TLObject obj, TLStructuredTypePart attribute) {
			return answer(new Question(SimpleBoundCommandGroup.WRITE, obj, attribute));
		}

		@Override
		public boolean canDelete(TLObject obj) {
			return answer(new Question(SimpleBoundCommandGroup.DELETE, obj, null));
		}

		@Override
		public boolean canCreate(TLClass type, TLObject context) {
			return true;
		}

		@Override
		public boolean canCreate(TLObject parent, TLStructuredTypePart compositionAttribute, TLClass type) {
			return answer(new Question(SimpleBoundCommandGroup.CREATE, parent, compositionAttribute));
		}

		@Override
		public boolean canSetInitial(TLObject draft, TLStructuredTypePart attribute) {
			return answer(new Question(SimpleBoundCommandGroup.WRITE, draft, attribute));
		}

		@Override
		public boolean canPerform(TLObject obj, BoundCommandGroup operation) {
			return answer(new Question(operation, obj, null));
		}

		@Override
		public <T extends TLObject> List<T> filterReadable(Collection<T> objects) {
			return List.copyOf(objects);
		}

		@Override
		public Set<TLClass> accessibleTypes(BoundCommandGroup operation) {
			return Set.of();
		}
	}

	/** The outcome of one agent turn, as the loop's events and the conversation report it. */
	private record Turn(boolean committed, String rollbackReason, String notice, String toolResult) {
		// Intentionally empty.
	}

	/**
	 * Runs one agent turn, as an account with the given rights, in which the model calls the given
	 * tool once with the given arguments.
	 */
	private static Turn runTurn(ModelSecurity rights, Tool tool, String arguments) {
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(tool);
		catalog.freeze();

		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Change it."))));

		List<String> events = new ArrayList<>();
		AgentEventListener listener = event -> {
			if (event instanceof AgentEvent.TransactionCommitted) {
				events.add("commit");
			} else if (event instanceof AgentEvent.TransactionRolledBack rolledBack) {
				events.add("rollback:" + rolledBack.reason());
			}
		};

		TLContext context = TLContext.getContext();
		Person before = context.getPerson();
		String beforeId = context.getContextId();
		// An account, not a system context: a checking tool refuses to write in one.
		context.setPerson(PersonManager.getManager().getRoot());
		try (TransactionScope scope = new TransactionScope()) {
			AgentLoop loop = new AgentLoop(new AgentParams("test-model", "You change things.", 5,
				AgentParams.DEFAULT_MAX_OUTPUT_TOKENS), provider(tool.name(), arguments), catalog);
			loop.run(conversation, new ToolContext(security(rights), ApprovalCallback.AUTO_APPROVE, scope), listener);
		} finally {
			context.setPerson(before);
			if (before == null) {
				context.setContextId(beforeId);
			}
		}

		String rollbackReason = null;
		for (String event : events) {
			if (event.startsWith("rollback:")) {
				rollbackReason = event.substring("rollback:".length());
			}
		}
		return new Turn(events.contains("commit"), rollbackReason, notice(conversation), toolResult(conversation));
	}

	/**
	 * A provider that asks for the given tool once and then ends the turn.
	 */
	private static LLMProvider provider(String toolName, String arguments) {
		AtomicInteger call = new AtomicInteger();
		return (request, handler) -> {
			int index = call.getAndIncrement();
			if (index == 0) {
				ToolCall toolCall = new ToolCall("tc1", toolName, arguments);
				handler.onEvent(new StreamEvent.ToolCallStart(0, toolCall.id(), toolCall.name()));
				handler.onEvent(new StreamEvent.ToolCallDelta(0, toolCall.arguments()));
				handler.onEvent(new StreamEvent.MessageComplete("msg-0", "test-model", "tool_use",
					USAGE, List.of(toolCall)));
				return;
			}
			if (index == 1) {
				TextContent text = new TextContent("Done.");
				handler.onEvent(new StreamEvent.TextDelta(0, text.text()));
				handler.onEvent(new StreamEvent.MessageComplete("msg-1", "test-model", "end_turn",
					USAGE, List.of(text)));
				return;
			}
			throw new ProviderException(ProviderException.Kind.INVALID_REQUEST, 400,
				"The turn must be over after one tool call.");
		};
	}

	private static SecurityContext security(ModelSecurity rights) {
		return new SecurityContext() {

			@Override
			public boolean hasToolPermission(Tool tool) {
				return true;
			}

			@Override
			public ModelSecurity modelSecurity() {
				return rights;
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.DEVELOPMENT;
			}
		};
	}

	/**
	 * A tool that writes and asks nobody whether it may - a stand-in for a remote MCP tool or a Java
	 * tool whose author forgot the check. Its {@link ToolMode#DATA_WRITE} mode is what makes the loop
	 * open a transaction for the turn.
	 */
	private static Tool unchecked(Runnable write) {
		return new Tool() {

			@Override
			public String name() {
				return TOOL;
			}

			@Override
			public String description() {
				return "Writes without checking anything.";
			}

			@Override
			public ToolSchema schema() {
				return new ToolSchema(TOOL, description(), "{}");
			}

			@Override
			public ToolMode mode() {
				return ToolMode.DATA_WRITE;
			}

			@Override
			public String group() {
				return ToolGroups.DATA_WRITE;
			}

			@Override
			public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				// Deliberately no context.modelSecurity() check: that is the whole point.
				write.run();
				return ToolResult.of("Written.");
			}
		};
	}

	/** The object tool that asks the account's rights before it writes. */
	private static Tool updateObject() {
		for (Tool tool : AnnotatedTools.from(new ObjectEditTool())) {
			if ("updateObject".equals(tool.name())) {
				return tool;
			}
		}
		throw new AssertionError("ObjectEditTool has no updateObject.");
	}

	private static String arguments(TLObject object, String attribute, String value) {
		return "{\"id\": \"" + ReadFilter.idOf(object) + "\", \"values\": {\"" + attribute + "\": \"" + value
			+ "\"}}";
	}

	/** The {@code [Framework notice]} the loop added, or <code>null</code>. */
	private static String notice(Conversation conversation) {
		for (Message message : conversation.messages()) {
			if (message instanceof UserMessage user) {
				for (ContentPart part : user.content()) {
					if (part instanceof TextContent text && text.text().startsWith("[Framework notice]")) {
						return text.text();
					}
				}
			}
		}
		return null;
	}

	/** What the tool answered, a refusal included. */
	private static String toolResult(Conversation conversation) {
		for (Message message : conversation.messages()) {
			if (message instanceof ToolResultMessage result) {
				if (result.state() instanceof ToolState.Completed completed) {
					return completed.result();
				}
				if (result.state() instanceof ToolState.Failed failed) {
					return failed.error();
				}
			}
		}
		return null;
	}

	private static TLObject memory(String kind, TLObject session) {
		TLClass type = (TLClass) TLModelUtil.findType("tl.ai.execution:UserMemory");
		try (Transaction tx = kb().beginTransaction()) {
			TLObject memory = ModelService.getInstance().getFactory().createObject(type, null);
			memory.tUpdate(type.getPart("owner"), PersonManager.getManager().getRoot());
			memory.tUpdate(type.getPart("kind"), kind(kind));
			memory.tUpdate(type.getPart("text"), "Prefers short answers.");
			memory.tUpdate(type.getPart("createdAt"), new Date());
			memory.tUpdate(type.getPart("session"), session);
			tx.commit();
			return memory;
		}
	}

	private static TLObject session() {
		TLClass type = (TLClass) TLModelUtil.findType("tl.ai.execution:AgentSession");
		try (Transaction tx = kb().beginTransaction()) {
			TLObject session = ModelService.getInstance().getFactory().createObject(type, null);
			session.tUpdate(type.getPart("kind"), TLModelUtil.findPart("tl.ai.execution:SessionKind#CHAT"));
			session.tUpdate(type.getPart("createdAt"), new Date());
			tx.commit();
			return session;
		}
	}

	private static TLObject newCommitted(String typeName) {
		TLClass type = (TLClass) TLModelUtil.findType(typeName);
		try (Transaction tx = kb().beginTransaction()) {
			TLObject result = ModelService.getInstance().getFactory().createObject(type, null);
			tx.commit();
			return result;
		}
	}

	private static TLObject cover(ResKey caption) {
		TLClass type = (TLClass) TLModelUtil.findType("test.objectedit:Cover");
		try (Transaction tx = kb().beginTransaction()) {
			TLObject cover = ModelService.getInstance().getFactory().createObject(type, null);
			cover.tUpdate(type.getPart("caption"), caption);
			tx.commit();
			return cover;
		}
	}

	/** The English and the German text, which a translated attribute of this setup stores. */
	private static List<String> translations(ResKey text) {
		return Arrays.asList(ResKeyUtil.getTranslation(text, Locale.ENGLISH),
			ResKeyUtil.getTranslation(text, Locale.GERMAN));
	}

	private static Object kind(String name) {
		return TLModelUtil.findPart("tl.ai.execution:MemoryKind#" + name);
	}

	private static String kindOf(TLObject memory) {
		Object kind = memory.tValue(part(memory, "kind"));
		return kind == null ? null : ((TLClassifier) kind).getName();
	}

	private static TLStructuredTypePart part(TLObject object, String name) {
		return object.tType().getPart(name);
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/** The knowledge base, the model, real accounts and locks: these turns write real model objects. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestPreCommitGuardInATurn.class), ModelService.Module.INSTANCE,
				LockService.Module.INSTANCE));
	}
}
