/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.script;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.SessionContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.model.Agent;
import com.top_logic.tl.ai.core.model.TlAiCoreFactory;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.security.ToolChangeSet;
import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * Tests {@link ChangeDataTool} against real objects in a real knowledge base.
 *
 * <p>
 * Every refusal and every dry run ends on the stored value, not only on the answer: a tool that said
 * "nothing was saved" and saved would pass a test that only read what it said. The runs act as the
 * root account in super-user mode, because the engine checks the model access rights inside a
 * script and what is under test here is the tool, not the rights - those are pinned against real
 * accounts in tl-ai-demo.
 * </p>
 */
public class TestChangeDataTool extends BasicTestCase {

	private static final String DESCRIPTION = "`tl.ai.core:Agent#description`";

	private static final String KIND = "`tl.ai.execution:UserMemory#kind`";

	private static final String CAPTION = "`test.objectedit:Cover#caption`";

	private ChangeDataTool _tool;

	private Person _previousUser;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_tool = new ChangeDataTool();
		TLContext context = TLContext.getContext();
		_previousUser = context.getPerson();
		context.setCurrentPerson(PersonManager.getManager().getRoot());
		ThreadContext.pushSuperUser();
	}

	@Override
	protected void tearDown() throws Exception {
		ThreadContext.popSuperUser();
		TLContext.getContext().setCurrentPerson(_previousUser);
		super.tearDown();
	}

	/**
	 * A script that changes nothing is refused before anybody is asked to approve it, and pointed to
	 * the tools that read.
	 */
	public void testAScriptThatOnlyReadsIsRefusedAndPointedToQuery() {
		String read = "all(`tl.ai.core:Agent`).size()";

		String early = _tool.refusal(read, null, context());
		ToolResult result = _tool.changeData(read, null, context());

		assertNotNull("A read must be refused before the approval prompt.", early);
		assertContains("query", early);
		assertContains("present", early);
		assertTrue(result.failed());
		assertEquals(early, result.text());
		assertNotNull("A dry run of a read is refused as well.", _tool.refusal(read, true, context()));
	}

	/** A write hidden in a function's argument is still a write, and not refused as a read. */
	public void testAWriteInsideAFunctionIsAWrite() {
		Agent agent = agent("before");

		assertNull(_tool.refusal("toString(" + resolve(agent) + ".set(" + DESCRIPTION + ", 'after'))", true,
			context()));
	}

	/** The dry run answers with the change it would make, and makes none. */
	public void testTheDryRunReportsTheChangeAndSavesNothing() {
		Agent agent = agent("before");

		ToolResult result = _tool.changeData(set(agent, "after"), true, context());

		assertFalse(result.text(), result.failed());
		assertContains("nothing was saved", result.text());
		assertContains("description \"before\" -> \"after\"", result.text());
		assertEquals("before", agent.getDescription());
		assertTrue("A dry run leaves nothing in the caller's transaction context.",
			ToolChangeSet.pending(PersistencyLayer.getKnowledgeBase()).isEmpty());
	}

	/**
	 * A dry run in the middle of a turn that already wrote leaves the turn's changes intact: it runs
	 * in a transaction context of its own, so rolling it back cannot fail the turn's.
	 */
	public void testTheDryRunLeavesTheTurnsTransactionIntact() {
		Agent tried = agent("tried");
		Agent written = agent("old");
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		try (Transaction turn = kb.beginTransaction()) {
			written.setDescription("written by the turn");
			ToolResult result = _tool.changeData(set(tried, "never"), true, context());
			assertFalse(result.text(), result.failed());
			assertContains("description \"tried\" -> \"never\"", result.text());
			turn.commit();
		}

		assertEquals("written by the turn", written.getDescription());
		assertEquals("tried", tried.getDescription());
	}

	/**
	 * A script whose effect is not a write to the database is checked and not run: a rollback would
	 * not take that effect back. Run for real, the answer names that effect, which the report of the
	 * changed data cannot show.
	 */
	public void testTheDryRunDoesNotRunAnEffectOutsideTheDatabase() {
		Agent agent = agent("before");
		String script = "{ log('change data'); " + set(agent, "after") + " }";

		ToolResult dry = _tool.changeData(script, true, context());

		assertFalse(dry.text(), dry.failed());
		assertContains("Checked, not run", dry.text());
		assertContains("log", dry.text());
		assertEquals("before", agent.getDescription());

		ToolResult real = inTurn(() -> _tool.changeData(script, false, context()));

		assertContains("description \"before\" -> \"after\"", real.text());
		assertContains("It also called log", real.text());
	}

	/** A reference stored as a link is reported on the object that holds it, with both values. */
	public void testAChangedReferenceIsReportedWithItsValues() {
		TLObject memory = memory("FACT");

		ToolResult result = _tool.changeData(resolve(memory) + ".set(" + KIND
			+ ", `tl.ai.execution:MemoryKind#PREFERENCE`)", true, context());

		assertContains("kind \"FACT\" -> \"PREFERENCE\"", result.text());
		assertEquals("FACT", kindOf(memory));
	}

	/**
	 * A translated attribute, whose translations the engine keeps in rows of a table of their own, is
	 * reported on the object holding it, with both values: the report reads the transaction as the
	 * pre-commit guard does (Gitea #638).
	 */
	public void testAChangedTranslationIsReportedWithItsValues() {
		TLObject cover = cover("one");

		ToolResult result = _tool.changeData(resolve(cover) + ".set(" + CAPTION + ", #(\"two\"@en))", true,
			context());

		assertContains("changed 1 object", result.text());
		assertContains("caption \"one\" -> \"two\"", result.text());
	}

	/**
	 * Deleting an object another one refers to changes that other one too, and the report shows the
	 * reference as it was - naming an object that no longer exists - without failing the change.
	 */
	public void testDeletingAReferencedObjectIsMadeAndReported() {
		TLObject session = session();
		TLObject memory = memory("FACT");
		inTurn(() -> {
			memory.tUpdate(memory.tType().getPart("session"), session);
			return null;
		});

		ToolResult dry = _tool.changeData(resolve(session) + ".delete()", true, context());
		ToolResult made = inTurn(() -> _tool.changeData(resolve(session) + ".delete()", false, context()));

		assertContains("deleted", dry.text());
		assertFalse(made.text(), made.failed());
		assertContains("deleted", made.text());
		assertContains("session", made.text());
		assertFalse("The session is gone.", session.tValid());
		assertNull(memory.tValue(memory.tType().getPart("session")));
	}

	/** A violated constraint is named by the dry run, before anybody is asked to approve the change. */
	public void testTheDryRunNamesABrokenRule() {
		Agent agent = agent("before");

		ToolResult result =
			_tool.changeData(resolve(agent) + ".set(`tl.ai.core:Agent#name`, null)", true, context());

		assertContains("breaks the model's rules", result.text());
		assertContains("'name'", result.text());
	}

	/** The change is made in the turn's transaction, and the answer says what it changed. */
	public void testTheChangeIsMadeAndReported() {
		Agent agent = agent("before");

		ToolResult result = inTurn(() -> _tool.changeData(set(agent, "after"), false, context()));

		assertFalse(result.text(), result.failed());
		assertContains("changed 1 object", result.text());
		assertContains("description \"before\" -> \"after\"", result.text());
		assertEquals("after", agent.getDescription());
	}

	/**
	 * A change the model's rules refuse fails the call, so that the agent loop rolls the turn back -
	 * what a script wrote cannot be taken back on its own.
	 */
	public void testAChangeThatBreaksARuleFailsTheTurn() {
		Agent agent = agent("before");
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		try (Transaction turn = kb.beginTransaction()) {
			try {
				_tool.changeData(resolve(agent) + ".set(`tl.ai.core:Agent#name`, null)", false, context());
				fail("A change that breaks a rule must fail the call.");
			} catch (IllegalStateException ex) {
				assertContains("'name'", ex.getMessage());
				assertContains("rolled back", ex.getMessage());
			}
		}
		assertNotNull(agent.getName());
	}

	/** A script that fails while it runs fails the call, and is a dry run's answer instead. */
	public void testAFailingScriptFailsTheChangeAndIsTheDryRunsAnswer() {
		String failing = "all(`tl.ai.core:Agent`).filter(a -> false).singleElement().set(" + DESCRIPTION + ", 'x')";

		ToolResult dry = _tool.changeData(failing, true, context());
		assertContains("the script failed", dry.text());

		try (Transaction turn = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			_tool.changeData(failing, false, context());
			fail("A script that fails must fail the call.");
		} catch (IllegalStateException ex) {
			assertContains("rolled back", ex.getMessage());
		}
	}

	/** Making a change costs the right to change data; its dry run does not. */
	public void testAChangeNeedsTheRightToChangeData() {
		Agent agent = agent("before");
		ToolContext withoutDataWrite = context("Permission denied: no data.write.");

		assertEquals("Permission denied: no data.write.", _tool.refusal(set(agent, "after"), null, withoutDataWrite));
		assertNull(_tool.refusal(set(agent, "after"), true, withoutDataWrite));
		assertFalse(_tool.changeData(set(agent, "after"), true, withoutDataWrite).failed());
		assertEquals("before", agent.getDescription());
	}

	/** A system context checks no rights and records no author: no change is made there. */
	public void testASystemContextIsRefused() {
		Agent agent = agent("kept");
		TLContext context = TLContext.getContext();
		Person before = context.getPerson();
		String beforeId = context.getContextId();
		context.setPerson(null);
		context.setContextId(SessionContext.SYSTEM_ID_PREFIX + "test");
		try {
			assertContains("system context", _tool.refusal(set(agent, "x"), null, context()));
			assertContains("system context", _tool.refusal(set(agent, "x"), true, context()));
		} finally {
			// A context with a person is known by the person's id; only a person-less one needs its own.
			context.setPerson(before);
			if (before == null) {
				context.setContextId(beforeId);
			}
		}
		assertEquals("kept", agent.getDescription());
	}

	/** A script that does not compile is refused before it runs, with the engine's reason. */
	public void testAScriptThatDoesNotCompileIsRefused() {
		String refusal = _tool.refusal("all(`tl.ai.core:NoSuchType`).foreach(x -> $x.delete())", null, context());

		assertContains("does not compile", refusal);
	}

	/** The tool writes, needs the script capability, and its dry run is a read. */
	public void testTheDryRunOfTheWriteToolIsARead() {
		Tool tool = AnnotatedTools.from(_tool).get(0);

		assertEquals("changeData", tool.name());
		assertEquals(ToolMode.DATA_WRITE, tool.mode());
		assertEquals(ToolGroups.SCRIPT, tool.group());
		assertEquals(ToolMode.READ, tool.mode(Map.of("script", "x", "dryRun", true)));
		assertEquals(ToolMode.DATA_WRITE, tool.mode(Map.of("script", "x")));
		assertNotNull(tool.refusal(Map.of("script", "all(`tl.ai.core:Agent`)"), context()));
	}

	private static <T> T inTurn(Supplier<T> call) {
		try (Transaction turn = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			T result = call.get();
			turn.commit();
			return result;
		}
	}

	private static String set(Agent agent, String description) {
		return resolve(agent) + ".set(" + DESCRIPTION + ", '" + description + "')";
	}

	private static String resolve(TLObject object) {
		return "\"" + ReadFilter.idOf(object) + "\".objectResolveKey()";
	}

	private static Agent agent(String description) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			Agent agent = TlAiCoreFactory.getInstance().createAgent();
			agent.tUpdate(TlAiCoreFactory.getNameAgentAttr(), "change-data-" + System.nanoTime());
			agent.setDescription(description);
			tx.commit();
			return agent;
		}
	}

	private static TLObject memory(String kind) {
		TLClass type = (TLClass) TLModelUtil.findType("tl.ai.execution:UserMemory");
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			TLObject memory = ModelService.getInstance().getFactory().createObject(type, null);
			memory.tUpdate(type.getPart("owner"), PersonManager.getManager().getRoot());
			memory.tUpdate(type.getPart("kind"), TLModelUtil.findPart("tl.ai.execution:MemoryKind#" + kind));
			memory.tUpdate(type.getPart("text"), "Prefers short answers.");
			memory.tUpdate(type.getPart("createdAt"), new Date());
			tx.commit();
			return memory;
		}
	}

	private static TLObject cover(String caption) {
		TLClass type = (TLClass) TLModelUtil.findType("test.objectedit:Cover");
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			TLObject cover = ModelService.getInstance().getFactory().createObject(type, null);
			cover.tUpdate(type.getPart("caption"), ResKey.builder().add(Locale.ENGLISH, caption).build());
			tx.commit();
			return cover;
		}
	}

	private static TLObject session() {
		TLClass type = (TLClass) TLModelUtil.findType("tl.ai.execution:AgentSession");
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			TLObject session = ModelService.getInstance().getFactory().createObject(type, null);
			session.tUpdate(type.getPart("kind"), TLModelUtil.findPart("tl.ai.execution:SessionKind#CHAT"));
			session.tUpdate(type.getPart("createdAt"), new Date());
			tx.commit();
			return session;
		}
	}

	private static String kindOf(TLObject memory) {
		Object kind = memory.tValue(memory.tType().getPart("kind"));
		return kind == null ? null : ((com.top_logic.model.TLClassifier) kind).getName();
	}

	private static ToolContext context() {
		return context(null);
	}

	private static ToolContext context(String dataWriteDenial) {
		return new ToolContext(new SecurityContext() {

			@Override
			public boolean hasToolPermission(Tool tool) {
				return true;
			}

			@Override
			public String capabilityDenialReason(String capability, String operation) {
				return ToolGroups.DATA_WRITE.equals(capability) ? dataWriteDenial : null;
			}

			@Override
			public ModelSecurity modelSecurity() {
				return ModelSecurity.UNCHECKED;
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.DEVELOPMENT;
			}
		}, ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP);
	}

	/** The knowledge base, the model and real accounts: these tests work on real model objects. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestChangeDataTool.class), ModelService.Module.INSTANCE));
	}
}
