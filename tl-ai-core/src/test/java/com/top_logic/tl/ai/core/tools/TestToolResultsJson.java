/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.model.Agent;
import com.top_logic.tl.ai.core.model.TlAiCoreFactory;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.tools.model.InstanceBrowserTool;
import com.top_logic.tl.ai.core.tools.model.ObjectEditTool;
import com.top_logic.tl.ai.core.tools.parameters.DeclaredParameters;
import com.top_logic.tl.ai.core.tools.present.PresentAs;
import com.top_logic.tl.ai.core.tools.present.PresentTool;
import com.top_logic.tl.ai.core.tools.present.Presentation;
import com.top_logic.tl.ai.core.tools.registry.ScriptToolExecutor;
import com.top_logic.tl.ai.core.tools.script.QueryTool;
import com.top_logic.tl.ai.core.tools.script.TLScriptExecutor;
import com.top_logic.ai.tool.api.ToolMode;
import com.top_logic.ai.tool.api.ToolResult;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonUtil;

/**
 * Tests that the results of the tools that report model objects are JSON, that every object in
 * them is its identity with a {@link ReadFilter#TID}, and that such a {@link ReadFilter#TID} is what
 * the next tool call names the object by.
 */
public class TestToolResultsJson extends BasicTestCase {

	private static final String ENVELOPE_START = "<untrusted_model_data source=\"";

	private static final String ENVELOPE_END = "</untrusted_model_data>";

	@Override
	protected void runTest() throws Throwable {
		TLContextManager.<Void, Throwable, RuntimeException> inPersonContext(PersonManager.getManager().getRoot(),
			() -> {
				super.runTest();
				return null;
			});
	}

	/** A query's objects are identities, and the identifier of one is resolved by getInstance. */
	public void testQueryIdentityIsResolvedByGetInstance() {
		Agent first = agent("query-1");
		Agent second = agent("query-2");

		String answer = new QueryTool().query("list(" + resolve(first) + ", " + resolve(second) + ")", context());

		Object result = payload(answer);
		assertEquals(List.of(ReadFilter.idOf(first), ReadFilter.idOf(second)), tids(result));

		String tid = (String) ((Map<?, ?>) ((List<?>) result).get(0)).get(ReadFilter.TID);
		String read = new InstanceBrowserTool().getInstance(List.of(tid), context());
		Map<?, ?> instances = (Map<?, ?>) payload(read);
		Map<?, ?> instance = (Map<?, ?>) ((List<?>) instances.get("instances")).get(0);
		assertEquals(tid, instance.get(ReadFilter.TID));
	}

	/** A dictionary a script tool returns is a JSON object, and the object in it its identity. */
	public void testScriptToolResultIsJson() {
		Agent agent = agent("script");
		ScriptToolExecutor tool = new ScriptToolExecutor("structured", "A structured result.",
			DeclaredParameters.NONE,
			TLScriptExecutor.compileReadOnly("{\"object\": " + resolve(agent) + ", \"count\": list(1, 2).size()}"),
			ToolMode.READ, true, null, 0, null, null);

		ToolResult result = tool.execute(Map.of(), context());

		Map<?, ?> data = (Map<?, ?>) payload(result.text());
		assertEquals(List.of(ReadFilter.idOf(agent)), tids(data));
		assertEquals(2L, ((Number) data.get("count")).longValue());
		assertFalse(result.text(), result.text().contains("2.0"));
	}

	/** The answer of present lists the objects shown as their identities. */
	public void testPresentAnswerIsJson() {
		Agent first = agent("present-1");
		Agent second = agent("present-2");
		PresentTool present = new PresentTool() {
			@Override
			protected String deliver(Presentation presentation, String channel, ToolContext context) {
				return null;
			}
		};

		String answer = present.present(null, List.of(ReadFilter.idOf(first), ReadFilter.idOf(second)),
			PresentAs.TABLE, null, null, null, context());

		assertEquals(List.of(ReadFilter.idOf(first), ReadFilter.idOf(second)), tids(payload(answer)));
	}

	/** A listed instance is an identity, and its attributes are JSON values, not quoted JSON. */
	public void testListedInstancesAreIdentities() {
		Agent agent = agent("listed");

		String answer =
			new InstanceBrowserTool().listInstances("tl.ai.core", "Agent", List.of("name"), 200, null, context());

		Map<?, ?> data = (Map<?, ?>) payload(answer);
		Map<?, ?> entry = ((List<?>) data.get("instances")).stream()
			.map(Map.class::cast)
			.filter(e -> ReadFilter.idOf(agent).equals(e.get(ReadFilter.TID)))
			.findFirst()
			.orElseThrow(() -> new AssertionError("Not listed: " + answer));
		assertEquals("tl.ai.core:Agent", entry.get(ReadFilter.TYPE));
		assertEquals(MetaLabelProvider.INSTANCE.getLabel(agent), entry.get(ReadFilter.DISPLAY));
		assertEquals(agent.getName(), ((Map<?, ?>) entry.get("attributes")).get("name"));
	}

	/** The confirmation of a change names the object as its identity. */
	public void testObjectEditAnswerIsJson() {
		Agent agent = agent("edited");

		ToolResult result = inTransaction(() -> new ObjectEditTool().updateObject(ReadFilter.idOf(agent),
			Map.of("description", "after"), context()));

		Map<?, ?> data = (Map<?, ?>) payload(result.text());
		assertEquals(List.of(ReadFilter.idOf(agent)), tids(data.get("object")));
	}

	/** The JSON inside the one envelope of a tool answer. */
	private static Object payload(String answer) {
		int start = answer.indexOf(ENVELOPE_START);
		assertTrue("Not marked: " + answer, start >= 0);
		int body = answer.indexOf('\n', start) + 1;
		int end = answer.indexOf(ENVELOPE_END, body);
		String json = answer.substring(body, end).trim();
		try {
			return JsonUtil.nextJsonValue(new JsonReader(new StringR(json)));
		} catch (IOException | RuntimeException ex) {
			throw new AssertionError("Not JSON: " + json, ex);
		}
	}

	/**
	 * The {@link ReadFilter#TID}s of all identities in the given JSON data, in order; fails for an
	 * object that names a label or type without one.
	 */
	private static List<String> tids(Object data) {
		List<String> result = new ArrayList<>();
		collect(data, result);
		return result;
	}

	private static void collect(Object data, List<String> result) {
		if (data instanceof Map<?, ?> map) {
			if (map.containsKey(ReadFilter.DISPLAY) || map.containsKey(ReadFilter.TYPE)) {
				Object tid = map.get(ReadFilter.TID);
				assertTrue("An object without identifier: " + map, tid instanceof String);
				result.add((String) tid);
			}
			for (Object value : map.values()) {
				collect(value, result);
			}
		} else if (data instanceof List<?> list) {
			for (Object value : list) {
				collect(value, result);
			}
		}
	}

	private static String resolve(Agent agent) {
		return "\"" + ReadFilter.idOf(agent) + "\".objectResolveKey()";
	}

	private static <T> T inTransaction(Supplier<T> call) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			T result = call.get();
			tx.commit();
			return result;
		}
	}

	private static Agent agent(String name) {
		return inTransaction(() -> {
			Agent agent = TlAiCoreFactory.getInstance().createAgent();
			agent.tUpdate(TlAiCoreFactory.getNameAgentAttr(), "json-" + name + "-" + System.nanoTime());
			return agent;
		});
	}

	private static ToolContext context() {
		return new ToolContext(SecurityContext.UNRESTRICTED, ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP);
	}

	/** The knowledge base, the model and real accounts: these tests work on real model objects. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestToolResultsJson.class), ModelService.Module.INSTANCE));
	}
}
