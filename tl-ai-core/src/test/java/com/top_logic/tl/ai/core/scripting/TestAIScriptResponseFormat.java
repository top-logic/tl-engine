/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.scripting;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.ResponseFormat;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.StreamHandler;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.util.error.TopLogicException;

/**
 * What the {@code responseFormat} argument of {@code aiChat} becomes.
 *
 * <p>
 * The contract of Gitea #388 on the script side: the three documented shapes are the three shapes
 * that work, anything else is refused where it is written, and what the caller asked for is on the
 * request the loop sends - it used to be put into a generic options map that no provider reads.
 * </p>
 */
public class TestAIScriptResponseFormat extends TestCase {

	private static final Instant T0 = Instant.parse("2026-09-19T12:00:00Z");

	/** The schema shape a script writes: a map, as {@code parseJson} or a literal produces it. */
	private static Map<String, Object> schema() {
		Map<String, Object> properties = new LinkedHashMap<>();
		properties.put("amount", Map.of("type", "number"));

		Map<String, Object> schema = new LinkedHashMap<>();
		schema.put("type", "object");
		schema.put("properties", properties);
		return schema;
	}

	/** Records the request and answers, so the test can look at what was sent. */
	private static final class Recorder implements LLMProvider {

		ProviderRequest request;

		@Override
		public void stream(ProviderRequest request, StreamHandler handler) {
			this.request = request;
			handler.onEvent(new StreamEvent.MessageComplete("msg", "test-model", "end_turn",
				TokenUsage.EMPTY, List.of(new TextContent("{}"))));
		}
	}

	/** Nothing asked for is nothing sent. */
	public void testNoFormat() {
		assertNull(AIScriptFunctions.toResponseFormat(null));
	}

	/** The two documented strings. */
	public void testTextAndJson() {
		assertSame(ResponseFormat.TEXT, AIScriptFunctions.toResponseFormat("text"));
		assertSame(ResponseFormat.JSON, AIScriptFunctions.toResponseFormat("json"));
		assertSame("The case a user writes it in is not the point.", ResponseFormat.JSON,
			AIScriptFunctions.toResponseFormat("JSON"));
	}

	/** A schema map becomes a schema-bearing format, with the documented defaults. */
	public void testSchemaMap() {
		Map<String, Object> argument = new LinkedHashMap<>();
		argument.put("name", "invoice");
		argument.put("schema", schema());

		ResponseFormat format = AIScriptFunctions.toResponseFormat(argument);

		assertEquals(ResponseFormat.Kind.JSON_SCHEMA, format.kind());
		assertEquals("invoice", format.schemaName());
		assertEquals(schema(), format.schema());
		assertFalse("Strict is opt-in: it additionally constrains the schema itself.", format.strict());
	}

	/** The name is optional, the strict flag is honoured when it is there. */
	public void testSchemaMapDefaults() {
		Map<String, Object> argument = new LinkedHashMap<>();
		argument.put("schema", schema());
		argument.put("strict", Boolean.TRUE);

		ResponseFormat format = AIScriptFunctions.toResponseFormat(argument);

		assertEquals(ResponseFormat.DEFAULT_SCHEMA_NAME, format.schemaName());
		assertTrue(format.strict());
	}

	/** An unknown string names itself in the failure rather than being ignored. */
	public void testUnknownStringIsRefused() {
		try {
			AIScriptFunctions.toResponseFormat("yaml");
			fail("A format nothing implements must not be accepted.");
		} catch (TopLogicException expected) {
			assertTrue(List.of(expected.getErrorKey().arguments()).contains("yaml"));
		}
	}

	/** A map without a schema is a schema request that says nothing - refused. */
	public void testSchemaMapWithoutSchemaIsRefused() {
		try {
			AIScriptFunctions.toResponseFormat(Map.of("name", "invoice"));
			fail("A schema map without a schema must not be accepted.");
		} catch (TopLogicException expected) {
			// Refused where it is written.
		}
	}

	/**
	 * The end of the path the issue is about: what the script asked for is on the request the
	 * provider is given, not in a generic option map nobody reads.
	 */
	public void testTheFormatReachesTheRequest() {
		Map<String, Object> argument = new LinkedHashMap<>();
		argument.put("name", "invoice");
		argument.put("schema", schema());

		Recorder provider = new Recorder();
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Read the invoice."))));

		ToolCatalog tools = new ToolCatalog();
		tools.freeze();
		AgentParams params = new AgentParams("test-model", "", 1, 1024, Map.of(),
			AIScriptFunctions.toResponseFormat(argument));
		new AgentLoop(params, provider, tools).run(conversation, event -> {
			/* No streaming surface. */
		});

		ResponseFormat sent = provider.request.responseFormat();
		assertNotNull("The response format must reach the provider request.", sent);
		assertEquals(ResponseFormat.Kind.JSON_SCHEMA, sent.kind());
		assertEquals("invoice", sent.schemaName());
		assertEquals(schema(), sent.schema());
	}

	/** A run that asked for nothing still sends nothing. */
	public void testNoFormatReachesTheRequestAsNull() {
		Recorder provider = new Recorder();
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Hello"))));

		ToolCatalog tools = new ToolCatalog();
		tools.freeze();
		new AgentLoop(new AgentParams("test-model", "", 1, 1024, Map.of()), provider, tools)
			.run(conversation, event -> {
				/* No streaming surface. */
			});

		assertNull(provider.request.responseFormat());
	}
}
