/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.AnswerMarking;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolArgumentCoercionException;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * A Java tool declaring a model-typed parameter receives the object, resolved from the identifier
 * the model wrote and read with the rights of the run's principal - the parity with a configured
 * script tool that Gitea #381 asked for.
 */
public class AnnotatedToolObjectParamTest extends BasicTestCase {

	/** A tool written the way an application writes one. */
	public static class RoomTools {

		// Marking is off here so that these tests assert the resolved object and nothing else;
		// what the framework puts around an answer is TestAnnotatedToolMarking's subject.
		@ToolMethod(description = "Reports a room's name", mode = ToolMode.READ,
			marking = AnswerMarking.NONE)
		public String roomName(@P("the room") TLObject room) {
			return String.valueOf(room.tValueByName("name"));
		}
	}

	private Person _previousUser;

	/**
	 * Runs each test as the root account in super-user mode - see {@code ScriptToolFactoryTest},
	 * which needs the same bypass for the same reason.
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
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
	 * The identifier a tool result reports is fed back and arrives as the object.
	 */
	public void testIdentifierArrivesAsTheObject() throws Exception {
		TLObject room = createRoom("Kaisaal");

		ToolResult result = tool().execute(Map.of("room", tid(room)), unchecked());

		assertEquals("Kaisaal", result.text());
	}

	/**
	 * The whole {@code label [id:TID]} entry a text result writes is accepted too, because that is
	 * what the model has in front of it.
	 */
	public void testLabelWithIdentifierIsAccepted() throws Exception {
		TLObject room = createRoom("Elbblick");

		ToolResult result = tool().execute(
			Map.of("room", "Elbblick [id:" + tid(room) + "]"), unchecked());

		assertEquals("Elbblick", result.text());
	}

	/**
	 * An object the principal may not read is refused, spelled exactly like one that does not exist -
	 * and refused before the method runs, so the turn's writes are not rolled back.
	 */
	public void testUnreadableObjectIsRefusedLikeAMissingOne() throws Exception {
		TLObject room = createRoom("Werkstatt");

		try {
			tool().execute(Map.of("room", tid(room)), refusingEveryRead());
			fail("An object the principal may not read must not reach the tool.");
		} catch (ToolArgumentCoercionException expected) {
			assertTrue("Says no more than 'not found': " + expected.getMessage(),
				expected.getMessage().contains("not found"));
			assertFalse("...and nothing about the object itself: " + expected.getMessage(),
				expected.getMessage().contains("Werkstatt"));
		}
	}

	/**
	 * A malformed identifier is a synthetic failure, not a tool failure: before this, the reflective
	 * call failed and the agent loop rolled the whole turn back.
	 */
	public void testMalformedIdentifierIsSynthetic() {
		try {
			tool().execute(Map.of("room", "not an id"), unchecked());
			fail("A string that is no identifier must be refused.");
		} catch (ToolArgumentCoercionException expected) {
			assertTrue(expected.getMessage().contains("not an id"));
		}
	}

	private static Tool tool() {
		List<Tool> tools = AnnotatedTools.from(new RoomTools());
		assertEquals("Expected exactly one tool", 1, tools.size());
		return tools.get(0);
	}

	private static String tid(TLObject object) {
		return object.tId().asString();
	}

	/** The context of a run whose principal may read everything. */
	private static ToolContext unchecked() {
		return ToolContext.EMPTY;
	}

	/** The context of a run whose principal may read nothing. */
	private static ToolContext refusingEveryRead() {
		SecurityContext security = new SecurityContext() {
			@Override
			public boolean hasToolPermission(Tool tool) {
				return true;
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.DEVELOPMENT;
			}

			@Override
			public ModelSecurity modelSecurity() {
				return REFUSES_EVERY_READ;
			}
		};
		return new ToolContext(security, ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP,
			CancellationToken.UNCANCELLABLE);
	}

	/**
	 * Rights that disclose nothing; everything else is the unchecked answer, because these tests are
	 * about the read that turns an identifier into an object.
	 */
	private static final ModelSecurity REFUSES_EVERY_READ = new ModelSecurity() {

		@Override
		public boolean canRead(TLObject obj) {
			return false;
		}

		@Override
		public boolean canRead(TLObject obj, TLStructuredTypePart attribute) {
			return false;
		}

		@Override
		public boolean canWrite(TLObject obj) {
			return ModelSecurity.UNCHECKED.canWrite(obj);
		}

		@Override
		public boolean canWrite(TLObject obj, TLStructuredTypePart attribute) {
			return ModelSecurity.UNCHECKED.canWrite(obj, attribute);
		}

		@Override
		public boolean canDelete(TLObject obj) {
			return ModelSecurity.UNCHECKED.canDelete(obj);
		}

		@Override
		public boolean canCreate(TLClass type, TLObject context) {
			return ModelSecurity.UNCHECKED.canCreate(type, context);
		}

		@Override
		public boolean canCreate(TLObject parent, TLStructuredTypePart compositionAttribute, TLClass type) {
			return ModelSecurity.UNCHECKED.canCreate(parent, compositionAttribute, type);
		}

		@Override
		public boolean canSetInitial(TLObject draft, TLStructuredTypePart attribute) {
			return ModelSecurity.UNCHECKED.canSetInitial(draft, attribute);
		}

		@Override
		public boolean canPerform(TLObject obj, BoundCommandGroup operation) {
			return ModelSecurity.UNCHECKED.canPerform(obj, operation);
		}

		@Override
		public Set<TLClass> accessibleTypes(BoundCommandGroup operation) {
			return ModelSecurity.UNCHECKED.accessibleTypes(operation);
		}

		@Override
		public <T extends TLObject> List<T> filterReadable(Collection<T> objects) {
			return List.of();
		}
	};

	private static TLObject createRoom(String name) throws Exception {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(ResKey.NONE)) {
			TLObject result = createObject(
				"new(`demo.events:Room`)"
					+ "..set(`demo.events:Named#name`, '" + name + "')"
					+ "..set(`demo.events:Room#capacity`, 40)");
			tx.commit();
			return result;
		}
	}

	private static TLObject createObject(String script) throws ParseException {
		SearchExpressionParser parser = new SearchExpressionParser(new StringReader(script));
		Expr parsed = parser.expr();
		SearchExpression expr = SearchBuilder.toSearchExpression(ModelService.getApplicationModel(), parsed);
		QueryExecutor executor =
			QueryExecutor.compile(PersistencyLayer.getKnowledgeBase(), ModelService.getApplicationModel(), expr);
		return (TLObject) executor.executeWith(null, null, Args.some());
	}

	/** */
	public static Test suite() {
		ArrayList<BasicRuntimeModule<?>> modules = new ArrayList<>(Arrays.asList(
			SearchBuilder.Module.INSTANCE,
			ModelService.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE,
			PersonManager.Module.INSTANCE));

		return KBSetup.getSingleKBTest(ServiceTestSetup.createSetup(
			new TestSuite(AnnotatedToolObjectParamTest.class),
			modules.toArray(new BasicRuntimeModule<?>[modules.size()])));
	}

}
