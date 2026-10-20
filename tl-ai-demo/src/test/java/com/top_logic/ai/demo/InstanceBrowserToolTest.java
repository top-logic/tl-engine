/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import static com.top_logic.model.search.expr.query.QueryExecutor.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.tools.model.InstanceBrowserTool;
import com.top_logic.tl.ai.core.tools.script.QueryTool;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * Test cases for {@link InstanceBrowserTool}.
 */
public class InstanceBrowserToolTest extends BasicTestCase {

	private static final String EVENTS_MODULE = "demo.events";
	private static final String EVENT_CLASS = "Event";
	private static final String ROOM_CLASS = "Room";

	private Person _previousUser;

	/**
	 * Runs each test as the root account in super-user mode.
	 *
	 * <p>
	 * Since engine {@code 8.0.0-alpha7}, TL-Script applies the model access rights of
	 * {@code TLContext.currentUser()}, which deny by default. The fixtures of these tests are
	 * created with TL-Script {@code new(...)}, so without a bypass they cannot be created at all.
	 * What these tests are about is instance listing and pagination, not authorization.
	 * </p>
	 *
	 * @see ScriptToolFactoryTest#setUp()
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
	 * Tests listing instances of a type without pagination.
	 */
	public void testListInstancesBasic() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances(EVENTS_MODULE, EVENT_CLASS, null, null, null, ToolContext.EMPTY);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain module", result.contains("\"module\""));
			assertTrue("Result should contain module name", result.contains(EVENTS_MODULE));
			assertTrue("Result should contain type", result.contains("\"type\""));
			assertTrue("Result should contain type name", result.contains(EVENT_CLASS));
			assertTrue("Result should contain totalCount", result.contains("\"totalCount\""));
			assertTrue("Result should contain instances array", result.contains("\"instances\""));
			assertTrue("Result should contain limit", result.contains("\"limit\""));
			assertTrue("Result should contain offset", result.contains("\"offset\""));
			assertTrue("Result should contain hasMore", result.contains("\"hasMore\""));

			// Should have created 3 events
			assertTrue("Should contain 3 events", result.contains("\"totalCount\":3"));
		});
	}

	/**
	 * Tests listing instances with custom limit.
	 */
	public void testListInstancesWithLimit() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances(EVENTS_MODULE, EVENT_CLASS, null, 2, null, ToolContext.EMPTY);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain limit", result.contains("\"limit\":2"));
			// With 3 total events and limit 2, there should be more
			assertTrue("Should indicate more results available", result.contains("\"hasMore\":true"));
			assertTrue("Should contain nextCursor", result.contains("\"nextCursor\""));
		});
	}

	/**
	 * Tests pagination using cursor.
	 */
	public void testListInstancesWithPagination() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();

			// Get first page with limit 2
			String firstPage = tool.listInstances(EVENTS_MODULE, EVENT_CLASS, null, 2, null, ToolContext.EMPTY);
			assertNotNull("First page should not be null", firstPage);

			// Extract next cursor (simple string matching since we don't want to parse JSON)
			int cursorStart = firstPage.indexOf("\"nextCursor\":\"") + 14;
			int cursorEnd = firstPage.indexOf("\"", cursorStart);
			String cursor = firstPage.substring(cursorStart, cursorEnd);

			// Get second page using cursor
			String secondPage = tool.listInstances(EVENTS_MODULE, EVENT_CLASS, null, 2, cursor, ToolContext.EMPTY);
			assertNotNull("Second page should not be null", secondPage);
			assertTrue("Second page should have offset 2", secondPage.contains("\"offset\":2"));
			// With 3 total, offset 2 should have no more results
			assertTrue("Should not have more results", secondPage.contains("\"hasMore\":false"));
		});
	}

	/**
	 * Tests that limit is clamped to maximum allowed value.
	 */
	public void testListInstancesLimitClamping() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances(EVENTS_MODULE, EVENT_CLASS, null, 10000, null, ToolContext.EMPTY);

			assertNotNull("Result should not be null", result);
			// Limit should be clamped to 200 (MAX_LIMIT)
			assertTrue("Limit should be clamped to 200", result.contains("\"limit\":200"));
		});
	}

	/**
	 * Tests getting a specific instance by TID.
	 */
	public void testGetInstanceBasic() throws Exception {
		withTestObjects(objects -> {
			// Get the TID of the first event
			TLObject event = objects.getEvent(0);
			assertNotNull("Event1 should exist", event);

			String tid = event.tId().asString();

			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.getInstance(List.of(tid), ToolContext.EMPTY);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain tid", result.contains("\"tid\""));
			assertTrue("Result should contain type info", result.contains("\"type\""));
			assertTrue("Result should contain attributes", result.contains("\"attributes\""));
			assertTrue("Result should contain metadata", result.contains("\"metadata\""));

			// Should contain Event type information
			assertTrue("Should contain Event type", result.contains(EVENT_CLASS));
			assertTrue("Should contain module", result.contains(EVENTS_MODULE));

			// Should contain attribute values
			assertTrue("Should contain name attribute", result.contains("name"));
			assertTrue("Should contain maxParticipants attribute", result.contains("maxParticipants"));
		});
	}

	/**
	 * Tests that instance attributes include their values.
	 */
	public void testGetInstanceAttributeValues() throws Exception {
		withTestObjects(objects -> {
			TLObject event = objects.getEvent(0);
			String tid = event.tId().asString();

			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.getInstance(List.of(tid), ToolContext.EMPTY);

			assertNotNull("Result should not be null", result);
			// Event1 has name="Herbstkonferenz"
			assertTrue("Should contain event name", result.contains("Herbstkonferenz"));
			// Event1 has max participants=100
			assertTrue("Should contain max participants", result.contains("100"));
		});
	}

	/**
	 * Tests that instance metadata includes container information.
	 */
	public void testGetInstanceMetadata() throws Exception {
		withTestObjects(objects -> {
			TLObject event = objects.getEvent(0);
			String tid = event.tId().asString();

			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.getInstance(List.of(tid), ToolContext.EMPTY);

			assertNotNull("Result should not be null", result);
			assertTrue("Should contain metadata", result.contains("\"metadata\""));
			assertTrue("Should contain transient flag", result.contains("\"transient\""));
		});
	}

	/**
	 * Every entry of a listing says what the object is and how to name it again, and carries the
	 * attributes asked for - so a table of twenty rows is one call, not twenty-one (#616).
	 */
	public void testListInstancesCarriesTypeAndRequestedAttributes() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances(EVENTS_MODULE, EVENT_CLASS, List.of("name", " maxParticipants ", "noSuchAttribute"),
				null, null, ToolContext.EMPTY);

			String tid = objects.getEvent(0).tId().asString();
			assertTrue(result, result.contains("\"id\":\"" + tid + "\""));
			assertTrue(result, result.contains("\"type\":\"" + EVENTS_MODULE + ":" + EVENT_CLASS + "\""));
			assertTrue(result, result.contains("Herbstkonferenz"));
			assertTrue(result, result.contains("\"maxParticipants\":\"100\""));
			assertFalse("An attribute the type does not have is left out: " + result,
				result.contains("noSuchAttribute"));
			assertFalse("An attribute nobody asked for is left out: " + result, result.contains("ticketPrice"));
		});
	}

	/** Several identifiers are one call, and each of them is answered. */
	public void testGetInstanceReadsSeveral() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String first = objects.getEvent(0).tId().asString();
			String second = objects.getEvent(1).tId().asString();

			String result = tool.getInstance(List.of(first, second, "Event:99999"), ToolContext.EMPTY);

			assertTrue(result, result.contains("\"tid\":\"" + first + "\""));
			assertTrue(result, result.contains("\"tid\":\"" + second + "\""));
			// One id that names nothing does not hide the ones that do.
			assertTrue(result, result.contains("\"errors\":[\"Event:99999: "));
		});
	}

	/**
	 * A page that does not hold every instance says how to get the answer without paging: a query
	 * the server computes (#616).
	 */
	public void testListInstancesPointsToAQueryWhenThereAreMore() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String partial = tool.listInstances(EVENTS_MODULE, EVENT_CLASS, null, 2, null, ToolContext.EMPTY);
			String complete = tool.listInstances(EVENTS_MODULE, EVENT_CLASS, null, null, null, ToolContext.EMPTY);

			assertTrue(partial, partial.contains("\"hint\":\"There are 3 instances."));
			assertTrue(partial, partial.contains("query"));
			assertFalse("A complete page needs no hint: " + complete, complete.contains("There are"));
		});
	}

	// --- The query tool reads the same data, and is tested on the same fixture. ---

	/** A count is computed on the server: the model reads one number, not the rows. */
	public void testQueryCounts() throws Exception {
		withTestObjects(objects -> {
			String result = new QueryTool().query("all(`demo.events:Event`).size()", ToolContext.EMPTY);

			assertTrue(result, result.matches("(?s).*>\\s*3\\s*</.*"));
		});
	}

	/** The largest one is one object, named the way every tool names one. */
	public void testQuerySelectsTheLargest() throws Exception {
		withTestObjects(objects -> {
			String result = new QueryTool().query(
				"all(`demo.events:Event`).sort((e -> $e.get(`demo.events:Event#maxParticipants`)).comparator().desc()).firstElement()",
				ToolContext.EMPTY);

			assertTrue(result, result.contains("Herbstkonferenz [type:demo.events:Event] [id:"
				+ objects.getEvent(0).tId().asString() + "]"));
		});
	}

	/** An expression that writes is refused before it runs, and nothing changes. */
	public void testQueryRefusesToWrite() throws Exception {
		withTestObjects(objects -> {
			String result = new QueryTool().query(
				"all(`demo.events:Event`).foreach(e -> $e.set(`demo.events:Named#name`, 'renamed'))",
				ToolContext.EMPTY);

			assertTrue(result, result.startsWith("Error:"));
			assertTrue(result, result.contains("changes data"));
			assertEquals("Herbstkonferenz", objects.getEvent(0).tValueByName("name"));
		});
	}

	/** A long list is cut, and the answer says how long it was. */
	public void testQueryCutsALongList() throws Exception {
		withTestObjects(objects -> {
			StringBuilder sixty = new StringBuilder("list(");
			for (int n = 1; n <= 60; n++) {
				sixty.append(n == 1 ? "" : ", ").append(n);
			}
			String result = new QueryTool().query(sixty.append(")").toString(), ToolContext.EMPTY);

			assertTrue(result, result.contains("... and 10 more (60 in all)"));
		});
	}

	/**
	 * Tests error case: null module name.
	 */
	public void testListInstancesNullModule() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances(null, EVENT_CLASS, null, null, null, ToolContext.EMPTY);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain error", result.contains("\"error\""));
			assertTrue("Error should mention module required",
				result.contains("required") || result.contains("Module name"));
		});
	}

	/**
	 * Tests error case: empty module name.
	 */
	public void testListInstancesEmptyModule() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances("", EVENT_CLASS, null, null, null, ToolContext.EMPTY);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain error", result.contains("\"error\""));
			assertTrue("Error should mention module required",
				result.contains("required") || result.contains("Module name"));
		});
	}

	/**
	 * Tests error case: null type name.
	 */
	public void testListInstancesNullType() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances(EVENTS_MODULE, null, null, null, null, ToolContext.EMPTY);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain error", result.contains("\"error\""));
			assertTrue("Error should mention type required",
				result.contains("required") || result.contains("Type name"));
		});
	}

	/**
	 * Tests error case: empty type name.
	 */
	public void testListInstancesEmptyType() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances(EVENTS_MODULE, "", null, null, null, ToolContext.EMPTY);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain error", result.contains("\"error\""));
			assertTrue("Error should mention type required",
				result.contains("required") || result.contains("Type name"));
		});
	}

	/**
	 * Tests error case: type not found.
	 */
	public void testListInstancesTypeNotFound() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances(EVENTS_MODULE, "NonExistentType", null, null, null, ToolContext.EMPTY);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain error", result.contains("\"error\""));
			assertTrue("Error should mention type not found",
				result.contains("not found") || result.contains("NonExistentType"));
		});
	}

	/**
	 * Tests error case: invalid limit (negative).
	 */
	public void testListInstancesInvalidLimitNegative() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances(EVENTS_MODULE, EVENT_CLASS, null, -1, null, ToolContext.EMPTY);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain error", result.contains("\"error\""));
			assertTrue("Error should mention limit must be positive",
				result.contains("positive") || result.contains("Limit"));
		});
	}

	/**
	 * Tests error case: invalid limit (zero).
	 */
	public void testListInstancesInvalidLimitZero() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances(EVENTS_MODULE, EVENT_CLASS, null, 0, null, ToolContext.EMPTY);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain error", result.contains("\"error\""));
			assertTrue("Error should mention limit must be positive",
				result.contains("positive") || result.contains("Limit"));
		});
	}

	/**
	 * Tests error case: null TID.
	 */
	public void testGetInstanceNullTid() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.getInstance(null, ToolContext.EMPTY);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain error", result.contains("\"error\""));
			assertTrue("Error should mention TID required",
				result.contains("required") || result.contains("TID"));
		});
	}

	/**
	 * Tests error case: empty TID.
	 */
	public void testGetInstanceEmptyTid() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.getInstance(List.of(""), ToolContext.EMPTY);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain error", result.contains("\"error\""));
			assertTrue("Error should mention TID required",
				result.contains("required") || result.contains("TID"));
		});
	}

	/**
	 * Tests error case: invalid TID format (missing colon).
	 */
	public void testGetInstanceInvalidTidFormat() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.getInstance(List.of("InvalidTid"), ToolContext.EMPTY);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain errors", result.contains("\"errors\""));
			assertTrue("Error should mention invalid format",
				result.contains("Invalid") || result.contains("format"));
		});
	}

	/**
	 * Tests error case: TID not found.
	 */
	public void testGetInstanceTidNotFound() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.getInstance(List.of("Event:99999"), ToolContext.EMPTY);

			assertNotNull("Result should not be null", result);
			assertTrue("Result should contain errors", result.contains("\"errors\":[\"Event:99999: "));
		});
	}

	/**
	 * Tests listing instances of different types.
	 */
	public void testListInstancesMultipleTypes() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();

			// List events
			String eventsResult = tool.listInstances(EVENTS_MODULE, EVENT_CLASS, null, null, null, ToolContext.EMPTY);
			assertNotNull("Events result should not be null", eventsResult);
			assertTrue("Should have events", eventsResult.contains("\"totalCount\":3"));

			// List rooms
			String roomsResult = tool.listInstances(EVENTS_MODULE, ROOM_CLASS, null, null, null, ToolContext.EMPTY);
			assertNotNull("Rooms result should not be null", roomsResult);
			assertTrue("Should have rooms", roomsResult.contains("\"totalCount\":2"));
		});
	}

	/**
	 * Tests that empty cursor is treated as offset 0.
	 */
	public void testListInstancesEmptyCursor() throws Exception {
		withTestObjects(objects -> {
			InstanceBrowserTool tool = new InstanceBrowserTool();
			String result = tool.listInstances(EVENTS_MODULE, EVENT_CLASS, null, null, "", ToolContext.EMPTY);

			assertNotNull("Result should not be null", result);
			assertTrue("Empty cursor should start at offset 0", result.contains("\"offset\":0"));
		});
	}

	/**
	 * Helper method to execute a test with test objects created via TLScript.
	 */
	private void withTestObjects(TestConsumer test) throws Exception {
		List<TLObject> createdObjects = new ArrayList<>();

		// First transaction: Create rooms and commit
		try (Transaction tx = kb().beginTransaction(ResKey.NONE)) {
			TLObject room1 = createObject(
				"new(`demo.events:Room`)" +
				"..set(`demo.events:Named#name`, 'Saal Rhein')" +
				"..set(`demo.events:Room#capacity`, 800)" +
				"..set(`demo.events:Room#hasProjector`, true)");
			createdObjects.add(room1);

			TLObject room2 = createObject(
				"new(`demo.events:Room`)" +
				"..set(`demo.events:Named#name`, 'Raum Dom')" +
				"..set(`demo.events:Room#capacity`, 120)" +
				"..set(`demo.events:Room#hasProjector`, false)");
			createdObjects.add(room2);

			tx.commit();
		}

		// Second transaction: Create events whose sessions reference the rooms
		try (Transaction tx = kb().beginTransaction(ResKey.NONE)) {
			// Event 1 with 2 sessions
			TLObject event1 = createObject(
				"{ " +
				"  room1 = all(`demo.events:Room`).filter(r -> $r.get(`demo.events:Named#name`) == 'Saal Rhein').firstElement(); " +
				"  room2 = all(`demo.events:Room`).filter(r -> $r.get(`demo.events:Named#name`) == 'Raum Dom').firstElement(); " +
				"  new(`demo.events:Event`)" +
				"    ..set(`demo.events:Named#name`, 'Herbstkonferenz')" +
				"    ..set(`demo.events:Event#status`, `demo.events:EventStatus#PLANNED`)" +
				"    ..set(`demo.events:Event#startDate`, today())" +
				"    ..set(`demo.events:Event#maxParticipants`, 100)" +
				"    ..set(`demo.events:Event#ticketPrice`, 500.0)" +
				"    ..set(`demo.events:Event#sessions`, list(" +
				"      new(`demo.events:Session`)" +
				"        ..set(`demo.events:Named#name`, 'Keynote')" +
				"        ..set(`demo.events:Session#room`, $room1)" +
				"        ..set(`demo.events:Session#durationMinutes`, 45), " +
				"      new(`demo.events:Session`)" +
				"        ..set(`demo.events:Named#name`, 'Workshop')" +
				"        ..set(`demo.events:Session#room`, $room2)" +
				"        ..set(`demo.events:Session#durationMinutes`, 90)));" +
				"}");
			createdObjects.add(event1);

			// Event 2 with 1 session
			TLObject event2 = createObject(
				"{ " +
				"  room2 = all(`demo.events:Room`).filter(r -> $r.get(`demo.events:Named#name`) == 'Raum Dom').firstElement(); " +
				"  new(`demo.events:Event`)" +
				"    ..set(`demo.events:Named#name`, 'Datentag')" +
				"    ..set(`demo.events:Event#status`, `demo.events:EventStatus#DRAFT`)" +
				"    ..set(`demo.events:Event#startDate`, today())" +
				"    ..set(`demo.events:Event#maxParticipants`, 80)" +
				"    ..set(`demo.events:Event#ticketPrice`, 600.0)" +
				"    ..set(`demo.events:Event#sessions`, list(" +
				"      new(`demo.events:Session`)" +
				"        ..set(`demo.events:Named#name`, 'Vortrag')" +
				"        ..set(`demo.events:Session#room`, $room2)" +
				"        ..set(`demo.events:Session#durationMinutes`, 30)));" +
				"}");
			createdObjects.add(event2);

			// Event 3 with 1 session
			TLObject event3 = createObject(
				"{ " +
				"  room1 = all(`demo.events:Room`).filter(r -> $r.get(`demo.events:Named#name`) == 'Saal Rhein').firstElement(); " +
				"  new(`demo.events:Event`)" +
				"    ..set(`demo.events:Named#name`, 'Sommerfest')" +
				"    ..set(`demo.events:Event#status`, `demo.events:EventStatus#COMPLETED`)" +
				"    ..set(`demo.events:Event#startDate`, today())" +
				"    ..set(`demo.events:Event#maxParticipants`, 60)" +
				"    ..set(`demo.events:Event#ticketPrice`, 450.0)" +
				"    ..set(`demo.events:Event#sessions`, list(" +
				"      new(`demo.events:Session`)" +
				"        ..set(`demo.events:Named#name`, 'Lightning Talks')" +
				"        ..set(`demo.events:Session#room`, $room1)" +
				"        ..set(`demo.events:Session#durationMinutes`, 60)));" +
				"}");
			createdObjects.add(event3);

			tx.commit();
		}

		try {
			TestObjects objects = new TestObjects(createdObjects);
			try (Transaction tx = kb().beginTransaction(ResKey.NONE)) {
				test.accept(objects);
				// Roll back any modifications done by the test callback.
				tx.rollback(ResKey.NONE, null);
			}
		} finally {
			// Clean up created objects
			try (Transaction tx = kb().beginTransaction(ResKey.NONE)) {
				for (TLObject obj : createdObjects) {
					if (obj.tValid()) {
						obj.tDelete();
					}
				}
				tx.commit();
			}
		}
	}

	/**
	 * Creates an object using TLScript.
	 */
	private TLObject createObject(String script) throws ParseException {
		SearchExpression expr = search(script);
		Object result = execute(expr);
		return (TLObject) result;
	}

	/**
	 * Parses and builds a TLScript search expression.
	 */
	private static SearchExpression search(String expr) throws ParseException {
		SearchExpressionParser parser = new SearchExpressionParser(new StringReader(expr));
		Expr result = parser.expr();
		return SearchBuilder.toSearchExpression(model(), result);
	}

	/**
	 * Executes a search expression.
	 */
	private static Object execute(SearchExpression search, Object... args) {
		QueryExecutor executor = compile(kb(), model(), search);
		return executor.executeWith(null, null, Args.some(args));
	}

	/**
	 * Gets the knowledge base.
	 */
	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * Gets the application model.
	 */
	private static TLModel model() {
		return ModelService.getApplicationModel();
	}

	/**
	 * Functional interface for test consumers that can throw exceptions.
	 */
	@FunctionalInterface
	private interface TestConsumer {
		void accept(TestObjects objects) throws Exception;
	}

	/**
	 * Container for test objects.
	 */
	private static class TestObjects {
		private final List<TLObject> objects;

		TestObjects(List<TLObject> objects) {
			this.objects = objects;
		}

		TLObject getEvent(int index) {
			// Events are at indices 2, 3, 4 (after 2 rooms)
			return objects.get(index + 2);
		}
	}

	/**
	 * Creates the test suite with required service dependencies.
	 */
	public static Test suite() {
		return suite(InstanceBrowserToolTest.class);
	}

	/**	*/
	protected static Test suite(Class<? extends Test> test, BasicRuntimeModule<?>... additionalModules) {
		BasicRuntimeModule<?>[] modules = getModules(additionalModules);

		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(test),
				modules));
	}

	private static BasicRuntimeModule<?>[] getModules(BasicRuntimeModule<?>... additionalModules) {
		ArrayList<BasicRuntimeModule<?>> modules = new ArrayList<>();

		modules.add(SearchBuilder.Module.INSTANCE);
		modules.add(ModelService.Module.INSTANCE);
		modules.add(LabelProviderService.Module.INSTANCE);
		// Needed to run the tests as the root account, see setUp().
		modules.add(PersonManager.Module.INSTANCE);

		modules.addAll(Arrays.asList(additionalModules));

		return modules.toArray(new BasicRuntimeModule<?>[modules.size()]);
	}
}
