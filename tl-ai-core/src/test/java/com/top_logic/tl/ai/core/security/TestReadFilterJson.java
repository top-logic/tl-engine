/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.citation.ReadingList;
import com.top_logic.tl.ai.core.citation.ReadingListBinding;
import com.top_logic.tl.ai.core.model.TlAiCoreFactory;
import com.top_logic.tl.ai.core.tools.script.TLScriptExecutor;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * Tests the JSON output of {@link ReadFilter} against real model objects: the identity of objects
 * and classifiers, dates, whole numbers, the omission of unreadable objects, the cut list and the
 * reading list entries of everything written.
 */
public class TestReadFilterJson extends BasicTestCase {

	/** The structured result of a script tool, as in the ticket. */
	private static final String DICTIONARY = "x -> {\"object\": $x, \"date\": today(), "
		+ "\"kind\": `tl.ai.execution:MemoryKind#FACT`, \"count\": list(1, 2).size()}";

	/** A user zone that differs from the system zone in November. */
	private static final String USER_ZONE = "America/New_York";

	private static final String AGENT_TYPE = "tl.ai.core:Agent";

	private ReadingList _readingList;

	@Override
	protected void runTest() throws Throwable {
		TLContextManager.<Void, Throwable, RuntimeException> inPersonContext(PersonManager.getManager().getRoot(),
			() -> {
				_readingList = new ReadingList();
				try (ReadingListBinding.Binding bound = ReadingListBinding.bind(_readingList)) {
					super.runTest();
				}
				return null;
			});
	}

	/**
	 * The dictionary of the ticket becomes a JSON object: the object and the classifier as identity,
	 * the date as a day, the count as a whole number; both objects are on the reading list.
	 */
	public void testDictionaryOfTheTicket() {
		TLObject agent = agent("json-dictionary");
		Map<?, ?> raw = (Map<?, ?>) evaluate(DICTIONARY, agent);
		TLObject kind = (TLObject) raw.get("kind");

		Map<String, Object> expected = new LinkedHashMap<>();
		expected.put("object", identity(agent, AGENT_TYPE));
		expected.put("date", LocalDate.now(ThreadContext.getTimeZone().toZoneId()).toString());
		expected.put("kind", identity(kind, TLModelUtil.qualifiedName(kind.tType())));
		expected.put("count", 2L);

		ReadFilter filter = ReadFilter.forSecurity(ModelSecurity.UNCHECKED);
		assertEquals(expected, filter.disclose(raw));

		String marked = filter.markedJson("aTool", raw);
		assertTrue(marked, marked.contains(ReadFilter.json(expected)));
		assertTrue(marked, marked.contains("\"count\":2,") || marked.contains("\"count\":2}"));
		assertTrue(marked, marked.contains("<untrusted_model_data source=\"aTool\">"));

		assertRead(agent);
		assertRead(kind);
	}

	/** A day stays a day for a user whose time zone is not the system time zone. */
	public void testTodayInAnotherUserZone() {
		ZoneId userZone = ZoneId.of(USER_ZONE);
		assertFalse("The test needs a user zone different from the system zone.",
			TimeZones.systemTimeZone().toZoneId().getRules().equals(userZone.getRules()));

		withUserZone(userZone, () -> {
			Object today = ReadFilter.forSecurity(ModelSecurity.UNCHECKED).disclose(evaluate("today()"));

			assertEquals(LocalDate.now(userZone).toString(), today);
		});
	}

	/** A point in time that is not a day is written in the user's zone with time and offset. */
	public void testDateWithTimeHasTimeAndOffset() {
		withUserZone(ZoneId.of(USER_ZONE), () -> {
			Date afternoon = Date.from(ZonedDateTime.of(2026, 11, 14, 13, 5, 0, 0, ZoneId.of(USER_ZONE)).toInstant());

			assertEquals("2026-11-14T13:05:00-05:00",
				ReadFilter.forSecurity(ModelSecurity.UNCHECKED).disclose(afternoon));
		});
	}

	/** In the default setup, the user zone and the system zone are the same, and a time keeps its time. */
	public void testDateWithTimeInDefaultZone() {
		ZoneId zone = ThreadContext.getTimeZone().toZoneId();
		Date afternoon = Date.from(ZonedDateTime.of(2026, 11, 14, 13, 5, 0, 0, zone).toInstant());

		String written = (String) ReadFilter.forSecurity(ModelSecurity.UNCHECKED).disclose(afternoon);

		assertTrue(written, written.matches("2026-11-14T13:05:00([+-]\\d\\d:\\d\\d|Z)"));
	}

	/** Whole numbers lose the fraction TL-Script's doubles give them; other numbers keep it. */
	public void testWholeNumbers() {
		ReadFilter filter = ReadFilter.forSecurity(ModelSecurity.UNCHECKED);

		assertEquals(2L, filter.disclose(2.0));
		assertEquals(3L, filter.disclose(3.0f));
		assertEquals(2.5, filter.disclose(2.5));
		assertEquals(7, filter.disclose(7));
		assertEquals("[1,2.5]", ReadFilter.json(filter.disclose(List.of(1.0, 2.5))));
	}

	/**
	 * An object the principal may not read is left out of a dictionary - as a value and as an element
	 * of a nested list - and is not on the reading list.
	 */
	public void testUnreadableObjectInDictionaryIsLeftOut() {
		TLObject visible = agent("json-visible");
		TLObject hidden = agent("json-hidden");

		Map<String, Object> raw = new LinkedHashMap<>();
		raw.put("visible", visible);
		raw.put("hidden", hidden);
		raw.put("both", Arrays.asList(visible, hidden));

		Map<String, Object> expected = new LinkedHashMap<>();
		expected.put("visible", identity(visible, AGENT_TYPE));
		expected.put("both", List.of(identity(visible, AGENT_TYPE)));

		String marked = hiding(hidden).markedJson("aTool", raw);

		assertTrue(marked, marked.contains(ReadFilter.json(expected)));
		assertFalse(marked, marked.contains(ReadFilter.idOf(hidden)));
		assertRead(visible);
		assertNull("An object not shown is not on the reading list.", _readingList.find(ReadFilter.idOf(hidden)));
	}

	/**
	 * A list longer than the limit is cut to its first readable entries, with the number of all
	 * readable entries; only the shown entries are on the reading list.
	 */
	public void testCutList() {
		TLObject first = agent("json-cut-1");
		TLObject hidden = agent("json-cut-hidden");
		TLObject second = agent("json-cut-2");
		TLObject third = agent("json-cut-3");
		ReadFilter filter = hiding(hidden);

		Map<String, Object> expected = new LinkedHashMap<>();
		expected.put(ReadFilter.ITEMS, List.of(identity(first, AGENT_TYPE), identity(second, AGENT_TYPE)));
		expected.put(ReadFilter.TOTAL, 3);

		List<TLObject> all = List.of(first, hidden, second, third);
		assertEquals(expected, filter.disclose(all, 2));
		assertTrue(filter.markedJson("aTool", all, 2).contains(ReadFilter.json(expected)));

		assertRead(first);
		assertRead(second);
		assertNull("An entry cut off is not on the reading list.", _readingList.find(ReadFilter.idOf(third)));

		assertEquals("A list within the limit is a plain array.",
			List.of(identity(first, AGENT_TYPE), identity(second, AGENT_TYPE), identity(third, AGENT_TYPE)),
			filter.disclose(all, 3));
	}

	/** The identity embedded in a text is the identity of the JSON results, and is a read. */
	public void testIdentityJson() {
		TLObject agent = agent("json-identity");

		assertEquals(ReadFilter.json(identity(agent, AGENT_TYPE)), ReadFilter.identityJson(agent));
		assertRead(agent);
		assertEquals("null", ReadFilter.identityJson(null));
	}

	private void assertRead(TLObject obj) {
		assertNotNull("Not on the reading list: " + obj, _readingList.find(ReadFilter.idOf(obj)));
	}

	private static Map<String, Object> identity(TLObject obj, String type) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(ReadFilter.TID, ReadFilter.idOf(obj));
		result.put(ReadFilter.TYPE, type);
		result.put(ReadFilter.DISPLAY, MetaLabelProvider.INSTANCE.getLabel(obj));
		return result;
	}

	private static Object evaluate(String script, Object... args) {
		return TLScriptExecutor.evaluate(TLScriptExecutor.compileReadOnly(script), args);
	}

	private static void withUserZone(ZoneId zone, Runnable test) {
		ThreadContext session = ThreadContext.getThreadContext();
		TimeZone before = session.getCurrentTimeZone();
		session.setCurrentTimeZone(TimeZone.getTimeZone(zone));
		try {
			test.run();
		} finally {
			session.setCurrentTimeZone(before);
		}
	}

	/** A filter reading everything but the given objects. */
	private static ReadFilter hiding(TLObject... hidden) {
		Set<TLObject> denied = Set.of(hidden);
		ModelSecurity security = (ModelSecurity) Proxy.newProxyInstance(ModelSecurity.class.getClassLoader(),
			new Class<?>[] { ModelSecurity.class },
			(proxy, method, args) -> {
				if (method.getName().equals("canRead") && args.length == 1) {
					return !denied.contains(args[0]);
				}
				if (method.getName().equals("filterReadable")) {
					List<Object> result = new ArrayList<>();
					for (Object obj : (Iterable<?>) args[0]) {
						if (!denied.contains(obj)) {
							result.add(obj);
						}
					}
					return result;
				}
				try {
					return method.invoke(ModelSecurity.UNCHECKED, args);
				} catch (InvocationTargetException ex) {
					throw ex.getCause();
				}
			});
		return ReadFilter.forSecurity(security);
	}

	private static TLObject agent(String name) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			TLObject agent = TlAiCoreFactory.getInstance().createAgent();
			agent.tUpdate(TlAiCoreFactory.getNameAgentAttr(), name + "-" + System.nanoTime());
			tx.commit();
			return agent;
		}
	}

	/** Creates the test suite. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestReadFilterJson.class), ModelService.Module.INSTANCE));
	}
}
