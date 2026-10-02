/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.SessionContext;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.model.Agent;
import com.top_logic.tl.ai.core.model.TlAiCoreFactory;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * Tests {@link ObjectEditTool} against real objects in a real knowledge base.
 *
 * <p>
 * The rights here are stand-ins that answer one question each, because what is under test is that the
 * tool <em>asks</em> and honours the answer; what the engine answers for a real role model is pinned
 * against real accounts by {@code SecDemoObjectEditTest} in tl-ai-demo. Most refusals end on the
 * stored value, not on the answer: a tool that reported a refusal and wrote anyway would pass every
 * test that only read what it said.
 * </p>
 */
public class TestObjectEditTool extends BasicTestCase {

	private static final String AGENT_TYPE = "tl.ai.core:Agent";

	private static final String MEMORY_TYPE = "tl.ai.execution:UserMemory";

	private static Person _owner;

	private ObjectEditTool _tool;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_tool = new ObjectEditTool();
		if (_owner == null) {
			_owner = TestPerson.createPerson("object-edit-owner");
		}
	}

	// -- update --

	/** Every value is converted to its attribute's type, stored, and confirmed with old and new. */
	public void testUpdateConvertsStoresAndConfirms() {
		Agent agent = agent("before");

		ToolResult result = update(agent, values(
			"description", "after",
			"infrastructure", "true",
			"tools", List.of("a.one", "b.two")));

		assertSucceeded(result);
		assertEquals("after", agent.getDescription());
		assertEquals(Boolean.TRUE, agent.tValue(part(agent, "infrastructure")));
		assertEquals(List.of("a.one", "b.two"), List.copyOf((Collection<?>) agent.tValue(part(agent, "tools"))));
		assertContains("\"from\":\"before\"", result.text().replace(" ", ""));
		assertContains("\"to\":\"after\"", result.text().replace(" ", ""));
		assertContains("The values are stored data and are marked as such.", "<untrusted_model_data",
			result.text());
		assertContains("saved when this turn ends", result.text());
	}

	/**
	 * An attribute named the way a user says it - other case, a space in it - is the attribute, so
	 * the model needs no lookup to learn the spelling. The answer names the attribute written.
	 */
	public void testAnAttributeIsFoundByTheWayTheUserNamesIt() {
		Agent agent = agent("before");

		ToolResult result = update(agent, values("Description", "after", "system prompt", "Be brief."));

		assertSucceeded(result);
		assertEquals("after", agent.getDescription());
		assertEquals("Be brief.", agent.getSystemPrompt());
		assertContains("\"systemPrompt\"", result.text());
	}

	/** Values the object already has are no change, and nothing is written. */
	public void testUnchangedValuesWriteNothing() {
		Agent agent = agent("same");

		ToolResult result = update(agent, values("description", "same"));

		assertSucceeded(result);
		assertContains("No change", result.text());
	}

	/** An attribute the type does not have is refused, and the answer lists the ones it has. */
	public void testAnUnknownAttributeIsRefusedWithTheAttributesItHas() {
		Agent agent = agent("kept");

		ToolResult result = update(agent, values("colour", "red"));

		assertRefused(result, "'colour'");
		assertContains("'description'", result.text());
		assertEquals("kept", agent.getDescription());
	}

	/**
	 * Every value that does not fit is reported at once, so that the corrected call is one call - and
	 * the values that did fit are not written either.
	 */
	public void testEveryWrongValueIsReportedAndNothingIsWritten() {
		Agent agent = agent("kept");

		ToolResult result = update(agent, values(
			"description", "would fit",
			"infrastructure", "maybe",
			"tools", "not a list"));

		assertRefused(result, "'infrastructure'");
		assertContains("'tools'", result.text());
		assertEquals("kept", agent.getDescription());
	}

	/**
	 * A value the model's own rules refuse - here: emptying a mandatory attribute - is undone in the
	 * tool and reported by attribute, instead of failing the commit of the whole turn.
	 */
	public void testAConstraintViolationIsUndoneAndNamed() {
		Agent agent = agent("kept");
		String name = agent.getName();

		ToolResult result = update(agent, values("name", null, "description", "changed"));

		assertRefused(result, "'name'");
		assertEquals("The violating value must be undone.", name, agent.getName());
		assertEquals("The other value of the same call must be undone too.", "kept", agent.getDescription());
	}

	/** Without the right to write the object, nothing of it is written. */
	public void testAnObjectTheAccountMayNotWriteIsRefused() {
		Agent agent = agent("kept");

		ToolResult result = update(context(new DenyWrite(agent, null)), agent, values("description", "x"));

		assertRefused(result, "may not change");
		assertEquals("kept", agent.getDescription());
	}

	/**
	 * Model access is attribute-granular: holding the object is not holding each of its attributes,
	 * and the refusal names the attribute.
	 */
	public void testAnAttributeTheAccountMayNotWriteIsRefusedByName() {
		Agent agent = agent("kept");

		ToolResult result = update(context(new DenyWrite(agent, part(agent, "description"))), agent,
			values("description", "x", "summary", "y"));

		assertRefused(result, "'description'");
		assertFalse("A permitted attribute is not named as refused: " + result.text(),
			result.text().contains("'summary'"));
		assertEquals("Nothing of the call is written.", "kept", agent.getDescription());
		assertNull(agent.tValue(part(agent, "summary")));
	}

	/** An object the account may not read is refused like one that does not exist. */
	public void testAnObjectTheAccountMayNotReadIsNotFound() {
		Agent agent = agent("kept");

		ToolResult result = update(context(new DenyRead(agent)), agent, values("description", "x"));

		assertRefused(result, "not found");
		assertFalse("The refusal must not disclose the label: " + result.text(),
			result.text().contains(agent.getName()));
		assertEquals("kept", agent.getDescription());
	}

	/** An attribute the account may write but not read is written, and its value is not shown back. */
	public void testTheAnswerShowsOnlyWhatTheAccountMayRead() {
		Agent agent = agent("before");

		ToolResult result = update(context(new DenyReadAttribute(agent, part(agent, "description"))), agent,
			values("description", "secret-after"));

		assertSucceeded(result);
		assertEquals("secret-after", agent.getDescription());
		assertFalse("A value the account may not read must not be shown: " + result.text(),
			result.text().contains("secret-after") || result.text().contains("before"));
	}

	/**
	 * In a system context the engine grants every right whoever the run is for, and records the
	 * system as the author: no write is made there.
	 */
	public void testASystemContextIsRefused() {
		Agent agent = agent("kept");
		TLContext context = TLContext.getContext();
		Person before = context.getPerson();
		String beforeId = context.getContextId();
		context.setPerson(null);
		context.setContextId(SessionContext.SYSTEM_ID_PREFIX + "test");
		ToolResult result;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			result = _tool.updateObject(idOf(agent), values("description", "x"), ToolContext.EMPTY);
			tx.commit();
		} finally {
			restore(context, before, beforeId);
		}

		assertRefused(result, "system context");
		assertEquals("kept", agent.getDescription());
	}

	// -- create --

	/** A new object gets every value of the call, typed, and its identifier is in the answer. */
	public void testCreateSetsTypedValues() {
		ToolResult result = create(MEMORY_TYPE, values(
			"owner", idOf(_owner),
			"kind", "FACT",
			"text", "Prefers short answers.",
			"createdAt", "2027-05-14T09:30:00+02:00"));

		assertSucceeded(result);
		TLObject created = onlyMemoryWith("Prefers short answers.");
		assertEquals(_owner, created.tValue(part(created, "owner")));
		assertEquals("FACT", ((com.top_logic.model.TLClassifier) created.tValue(part(created, "kind"))).getName());
		assertEquals(Date.from(OffsetDateTime.parse("2027-05-14T09:30:00+02:00").toInstant()),
			created.tValue(part(created, "createdAt")));
		assertContains(idOf(created), result.text());
		delete(created);
	}

	/** A missing mandatory value creates nothing, and the answer names what the type needs. */
	public void testCreateWithoutAMandatoryValueCreatesNothing() {
		int before = memories().size();

		ToolResult result = create(MEMORY_TYPE, values("kind", "FACT", "text", "Incomplete."));

		assertRefused(result, "'owner'");
		assertContains("Mandatory attributes", result.text());
		assertEquals("No object may be left behind.", before, memories().size());
	}

	/** An enumeration value that does not exist is refused with the ones that do. */
	public void testAnUnknownEnumerationValueListsTheValidOnes() {
		ToolResult result = create(MEMORY_TYPE, values("owner", idOf(_owner), "kind", "OPINION",
			"text", "x", "createdAt", "2027-05-14T09:30"));

		assertRefused(result, "'kind'");
		assertContains("PREFERENCE", result.text());
		assertContains("FACT", result.text());
	}

	/** A reference to an object the account may not read is refused like one to nothing. */
	public void testAReferenceToAnUnreadableObjectIsRefused() {
		int before = memories().size();

		ToolResult result = create(context(new DenyRead(_owner)), MEMORY_TYPE, values("owner", idOf(_owner),
			"kind", "FACT", "text", "x", "createdAt", "2027-05-14T09:30"));

		assertRefused(result, "not found");
		assertEquals(before, memories().size());
	}

	/** A name where an id belongs is refused with the type the reference takes, to look it up in. */
	public void testANameForAReferenceNamesTheTypeToLookIn() {
		ToolResult result = create(MEMORY_TYPE, values("owner", "Smith", "kind", "FACT", "text", "x",
			"createdAt", "2027-05-14T09:30"));

		assertRefused(result, "'owner' refers to an object of type 'tl.accounts:Person'");
	}

	/** Creating is its own right, asked before anything exists. */
	public void testCreateWithoutTheRightCreatesNothing() {
		int before = memories().size();

		ToolResult result = create(context(new DenyCreate()), MEMORY_TYPE, values("owner", idOf(_owner),
			"kind", "FACT", "text", "x", "createdAt", "2027-05-14T09:30"));

		assertRefused(result, "may not create");
		assertEquals(before, memories().size());
	}

	/** A type that does not exist, and one that is not a class, are refused with what to pass. */
	public void testCreateOfSomethingThatIsNotAConcreteClass() {
		assertRefused(create("tl.ai.core:NoSuchType", Map.of()), "There is no type");
		assertRefused(create("tl.ai.execution:MemoryKind", Map.of()), "not a class");
	}

	// -- delete --

	/** The object is gone after the turn commits. */
	public void testDelete() {
		Agent agent = agent("doomed");

		ToolResult result = call(() -> _tool.deleteObject(idOf(agent), ToolContext.EMPTY));

		assertSucceeded(result);
		assertContains("Deleted 1 object", result.text());
		assertFalse(agent.tValid());
	}

	/** Deleting is its own right; without it the object stays. */
	public void testDeleteWithoutTheRightKeepsTheObject() {
		Agent agent = agent("kept");

		ToolResult result = call(() -> _tool.deleteObject(idOf(agent), context(new DenyDelete(agent))));

		assertRefused(result, "may not delete");
		assertTrue(agent.tValid());
		delete(agent);
	}

	// -- conversion --

	/** A whole number is accepted as a number and as its text, a fraction is not. */
	public void testWholeNumbers() {
		TLStructuredTypePart tokens = typePart("tl.ai.execution:AgentExecution#inputTokens");

		assertEquals(Long.valueOf(450), AttributeValues.convert(tokens, Integer.valueOf(450), ModelSecurity.UNCHECKED));
		assertEquals(Long.valueOf(450), AttributeValues.convert(tokens, "450", ModelSecurity.UNCHECKED));
		assertEquals(Long.valueOf(450), AttributeValues.convert(tokens, Double.valueOf(450.0), ModelSecurity.UNCHECKED));
		assertConversionRefused(tokens, Double.valueOf(4.5), "whole number");
		assertConversionRefused(tokens, "450 EUR", "without unit");
	}

	/** A point in time without an offset is read in the user's time zone. */
	public void testADateTimeWithoutOffsetIsTheUsersTime() {
		TLStructuredTypePart created = typePart(MEMORY_TYPE + "#createdAt");
		ZoneId zone = com.top_logic.basic.thread.ThreadContext.getTimeZone().toZoneId();

		Object value = AttributeValues.convert(created, "2027-05-14T09:30", ModelSecurity.UNCHECKED);

		assertEquals(Date.from(LocalDate.of(2027, 5, 14).atTime(9, 30).atZone(zone).toInstant()), value);
		assertConversionRefused(created, "next Tuesday", "ISO");
	}

	/** One value for an attribute holding several is refused, a list of one for a single one is not. */
	public void testMultiplicity() {
		TLStructuredTypePart tools = typePart(AGENT_TYPE + "#tools");
		TLStructuredTypePart description = typePart(AGENT_TYPE + "#description");

		assertConversionRefused(tools, "single", "pass a list");
		assertEquals("x", AttributeValues.convert(description, List.of("x"), ModelSecurity.UNCHECKED));
		assertConversionRefused(description, List.of("x", "y"), "one value");
	}

	// -- what the model is offered --

	/**
	 * The three tools under the names the shipped configuration offers to Albert: the names are a wire
	 * contract with {@code tl-ai-core.conf.config.xml}.
	 */
	public void testTheToolsAndTheirContract() {
		for (String name : List.of("updateObject", "createObject", "deleteObject")) {
			Tool tool = tool(name);
			assertEquals(name + " writes shared data and asks for approval.", ToolMode.DATA_WRITE, tool.mode());
			assertEquals(name + " is granted with the data.write capability.", ToolGroups.DATA_WRITE, tool.group());
		}
		assertEquals(3, AnnotatedTools.from(_tool).size());
		String create = tool("createObject").schema().inputSchemaJson();
		for (String parameter : List.of("type", "values", "container", "containerReference")) {
			assertContains(parameter, create);
		}
		String update = tool("updateObject").schema().inputSchemaJson();
		assertContains("\"values\":{\"type\":\"object\"", update.replace(" ", ""));
	}

	/** The values reach the tool as an object, also when the model sends them as a JSON text. */
	public void testValuesArriveThroughTheToolDispatch() {
		Agent agent = agent("before");
		Map<String, Object> args = new HashMap<>();
		args.put("id", idOf(agent));
		args.put("values", "{\"description\": \"after\"}");

		ToolResult result = call(() -> tool("updateObject").execute(args, ToolContext.EMPTY));

		assertSucceeded(result);
		assertEquals("after", agent.getDescription());
	}

	// -- helpers --

	private Tool tool(String name) {
		for (Tool candidate : AnnotatedTools.from(_tool)) {
			if (name.equals(candidate.name())) {
				return candidate;
			}
		}
		fail("No tool named '" + name + "'.");
		return null;
	}

	private ToolResult update(TLObject target, Map<String, Object> values) {
		return update(ToolContext.EMPTY, target, values);
	}

	private ToolResult update(ToolContext context, TLObject target, Map<String, Object> values) {
		return call(() -> _tool.updateObject(idOf(target), values, context));
	}

	private ToolResult create(String type, Map<String, Object> values) {
		return create(ToolContext.EMPTY, type, values);
	}

	private ToolResult create(ToolContext context, String type, Map<String, Object> values) {
		return call(() -> _tool.createObject(type, values, null, null, context));
	}

	/**
	 * Runs one call as an account, inside a transaction that is committed afterwards - which is what
	 * the agent loop does around a {@code DATA_WRITE} tool call.
	 */
	private static ToolResult call(Supplier<ToolResult> call) {
		TLContext context = TLContext.getContext();
		Person before = context.getPerson();
		String beforeId = context.getContextId();
		context.setPerson(_owner);
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			ToolResult result = call.get();
			tx.commit();
			return result;
		} finally {
			restore(context, before, beforeId);
		}
	}

	/** Puts the test's own context back, including the context id a person-less one is known by. */
	private static void restore(TLContext context, Person person, String contextId) {
		context.setPerson(person);
		if (person == null) {
			context.setContextId(contextId);
		}
	}

	private static Map<String, Object> values(Object... keysAndValues) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (int i = 0; i < keysAndValues.length; i += 2) {
			result.put((String) keysAndValues[i], keysAndValues[i + 1]);
		}
		return result;
	}

	private static Agent agent(String description) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			Agent agent = TlAiCoreFactory.getInstance().createAgent();
			agent.tUpdate(TlAiCoreFactory.getNameAgentAttr(), "object-edit-" + System.nanoTime());
			agent.setDescription(description);
			tx.commit();
			return agent;
		}
	}

	private static void delete(TLObject object) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			object.tDelete();
			tx.commit();
		}
	}

	private static List<TLObject> memories() {
		return MetaElementUtil.getAllInstancesOf((TLClass) TLModelUtil.findType(MEMORY_TYPE), TLObject.class);
	}

	private static TLObject onlyMemoryWith(String text) {
		List<TLObject> found = memories().stream()
			.filter(memory -> text.equals(memory.tValue(part(memory, "text"))))
			.toList();
		assertEquals(1, found.size());
		return found.get(0);
	}

	private static TLStructuredTypePart part(TLObject object, String name) {
		return object.tType().getPart(name);
	}

	private static TLStructuredTypePart typePart(String qualifiedName) {
		return (TLStructuredTypePart) TLModelUtil.resolveQualifiedName(qualifiedName);
	}

	private static String idOf(TLObject object) {
		return ReadFilter.idOf(object);
	}

	private static void assertConversionRefused(TLStructuredTypePart part, Object value, String expected) {
		try {
			AttributeValues.convert(part, value, ModelSecurity.UNCHECKED);
			fail("Expected " + value + " to be refused for " + part.getName() + ".");
		} catch (IllegalArgumentException ex) {
			assertContains(expected, ex.getMessage());
			assertContains("'" + part.getName() + "'", ex.getMessage());
		}
	}

	private static void assertSucceeded(ToolResult result) {
		assertFalse("Expected success, got: " + result.text(), result.failed());
	}

	private static void assertRefused(ToolResult result, String expected) {
		assertTrue("Expected a refusal, got: " + result.text(), result.failed());
		assertContains(expected, result.text());
	}

	private static ToolContext context(ModelSecurity security) {
		return new ToolContext(new SecurityContext() {

			@Override
			public boolean hasToolPermission(Tool tool) {
				return true;
			}

			@Override
			public ModelSecurity modelSecurity() {
				return security;
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.DEVELOPMENT;
			}
		}, ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP);
	}

	/** Everything except writing the named object, or - with an attribute - that attribute of it. */
	private record DenyWrite(TLObject object, TLStructuredTypePart attribute) implements GrantAll {

		@Override
		public boolean canWrite(TLObject obj) {
			return attribute != null || !obj.equals(object);
		}

		@Override
		public boolean canWrite(TLObject obj, TLStructuredTypePart part) {
			return !obj.equals(object) || (attribute != null && !part.equals(attribute));
		}
	}

	/** Everything except reading the named object at all. */
	private record DenyRead(TLObject object) implements GrantAll {

		@Override
		public boolean canRead(TLObject obj) {
			return !obj.equals(object);
		}
	}

	/** Everything except reading the named attribute of the named object. */
	private record DenyReadAttribute(TLObject object, TLStructuredTypePart attribute) implements GrantAll {

		@Override
		public boolean canRead(TLObject obj, TLStructuredTypePart part) {
			return !obj.equals(object) || !part.equals(attribute);
		}
	}

	/** Everything except creating. */
	private record DenyCreate() implements GrantAll {

		@Override
		public boolean canCreate(TLClass type, TLObject context) {
			return false;
		}
	}

	/** Everything except deleting the named object. */
	private record DenyDelete(TLObject object) implements GrantAll {

		@Override
		public boolean canDelete(TLObject obj) {
			return !obj.equals(object);
		}
	}

	/** Grants everything, so that a stand-in overrides only the one question it is about. */
	private interface GrantAll extends ModelSecurity {

		@Override
		default boolean canRead(TLObject obj) {
			return true;
		}

		@Override
		default boolean canRead(TLObject obj, TLStructuredTypePart attribute) {
			return true;
		}

		@Override
		default boolean canWrite(TLObject obj) {
			return true;
		}

		@Override
		default boolean canWrite(TLObject obj, TLStructuredTypePart attribute) {
			return true;
		}

		@Override
		default boolean canDelete(TLObject obj) {
			return true;
		}

		@Override
		default boolean canCreate(TLClass type, TLObject context) {
			return true;
		}

		@Override
		default boolean canCreate(TLObject parent, TLStructuredTypePart compositionAttribute) {
			return true;
		}

		@Override
		default boolean canPerform(TLObject obj, com.top_logic.tool.boundsec.BoundCommandGroup operation) {
			return true;
		}

		@Override
		default <T extends TLObject> List<T> filterReadable(Collection<T> objects) {
			return List.copyOf(objects);
		}

		@Override
		default Set<TLClass> accessibleTypes(com.top_logic.tool.boundsec.BoundCommandGroup operation) {
			return Set.of();
		}
	}

	/** The knowledge base, the model and real accounts: these tests work on real model objects. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestObjectEditTool.class), ModelService.Module.INSTANCE));
	}
}
