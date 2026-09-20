/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicRequestBuilder;
import com.top_logic.tl.ai.core.provider.bedrock.BedrockConverseRequestBuilder;
import com.top_logic.tl.ai.core.provider.chatcompletions.ChatCompletionsRequestBuilder;
import com.top_logic.tl.ai.core.provider.openai.OpenAIResponsesRequestBuilder;

import de.haumacher.msgbuf.json.JsonUtil;

/**
 * What a {@link ResponseFormat} looks like on the wire, per provider.
 *
 * <p>
 * The contract of Gitea #388: a format the caller asked for reaches the request of every provider
 * that has one, in that provider's own spelling - and a provider that has none says so instead of
 * sending the request without it.
 * </p>
 */
public class TestResponseFormatInRequests extends TestCase {

	private static final String SCHEMA_NAME = "invoice";

	/** The schema the invoice demo asks for, reduced to what the shape test needs. */
	private static final Map<String, Object> SCHEMA = schema();

	private static Map<String, Object> schema() {
		Map<String, Object> amount = new LinkedHashMap<>();
		amount.put("type", "number");

		Map<String, Object> properties = new LinkedHashMap<>();
		properties.put("amount", amount);

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("type", "object");
		result.put("additionalProperties", Boolean.FALSE);
		result.put("properties", properties);
		result.put("required", List.of("amount"));
		return result;
	}

	private static ProviderRequest request(ResponseFormat format) {
		UserMessage user = new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("Read the invoice.")));
		return new ProviderRequest("some-model", "You extract invoices.", List.of(user), List.of(),
			1024, Map.of(), format, null);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> asMap(Object value) {
		assertTrue("Expected an object, got: " + value, value instanceof Map);
		return (Map<String, Object>) value;
	}

	private static Map<String, Object> parse(String json) {
		return asMap(JsonUtil.parseJsonValue(json));
	}

	/** Anthropic states a schema as {@code output_config.format}, its structured-output field. */
	public void testAnthropicSendsTheSchema() throws Exception {
		String json = AnthropicRequestBuilder
			.buildRequestJson(request(ResponseFormat.jsonSchema(SCHEMA_NAME, SCHEMA, true)));

		Map<String, Object> format = asMap(asMap(parse(json).get("output_config")).get("format"));
		assertEquals("json_schema", format.get("type"));
		assertEquals(SCHEMA, format.get("schema"));
	}

	/**
	 * The schema does not displace the thinking effort: both live in the one
	 * {@code output_config}.
	 */
	public void testAnthropicKeepsTheThinkingEffortBesideTheSchema() throws Exception {
		UserMessage user = new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("Read the invoice.")));
		ProviderRequest request = new ProviderRequest("claude-opus-5", null, List.of(user), List.of(),
			1024, Map.of("thinking_effort", "high"),
			ResponseFormat.jsonSchema(SCHEMA_NAME, SCHEMA, false), null);

		Map<String, Object> outputConfig = asMap(parse(AnthropicRequestBuilder.buildRequestJson(request))
			.get("output_config"));
		assertEquals("high", outputConfig.get("effort"));
		assertEquals("json_schema", asMap(outputConfig.get("format")).get("type"));
	}

	/**
	 * Anthropic has no untyped JSON mode, so asking for one is refused rather than sent - the
	 * request that was sent before carried a {@code response_format} field the API does not know.
	 */
	public void testAnthropicRefusesUntypedJson() throws Exception {
		try {
			AnthropicRequestBuilder.buildRequestJson(request(ResponseFormat.JSON));
			fail("A format Anthropic cannot express must not be dropped silently.");
		} catch (ProviderException expected) {
			assertEquals(ProviderException.Kind.INVALID_REQUEST, expected.kind());
		}
	}

	/** A request that asks for nothing says nothing. */
	public void testAnthropicSendsNothingForText() throws Exception {
		assertNull(parse(AnthropicRequestBuilder.buildRequestJson(request(ResponseFormat.TEXT)))
			.get("output_config"));
		assertNull(parse(AnthropicRequestBuilder.buildRequestJson(request(null))).get("output_config"));
	}

	/** Chat Completions nests the schema under a name, beside the strict flag. */
	public void testChatCompletionsSendsTheSchema() throws Exception {
		String json = ChatCompletionsRequestBuilder
			.buildRequestJson(request(ResponseFormat.jsonSchema(SCHEMA_NAME, SCHEMA, true)));

		Map<String, Object> responseFormat = asMap(parse(json).get("response_format"));
		assertEquals("json_schema", responseFormat.get("type"));

		Map<String, Object> jsonSchema = asMap(responseFormat.get("json_schema"));
		assertEquals(SCHEMA_NAME, jsonSchema.get("name"));
		assertEquals(Boolean.TRUE, jsonSchema.get("strict"));
		assertEquals(SCHEMA, jsonSchema.get("schema"));
	}

	/** The strict flag is the caller's, not a default: a schema without one is sent as lenient. */
	public void testChatCompletionsSendsStrictAsAsked() throws Exception {
		String json = ChatCompletionsRequestBuilder
			.buildRequestJson(request(ResponseFormat.jsonSchema(SCHEMA_NAME, SCHEMA, false)));

		assertEquals(Boolean.FALSE,
			asMap(asMap(parse(json).get("response_format")).get("json_schema")).get("strict"));
	}

	/** Untyped JSON stays what it was: the API's own {@code json_object} mode. */
	public void testChatCompletionsKeepsUntypedJson() throws Exception {
		String json = ChatCompletionsRequestBuilder.buildRequestJson(request(ResponseFormat.JSON));

		assertEquals("json_object", asMap(parse(json).get("response_format")).get("type"));
	}

	/** The Responses API flattens name, schema and strict into the format object itself. */
	public void testOpenAIResponsesSendsTheSchema() throws Exception {
		String json = OpenAIResponsesRequestBuilder
			.buildRequestJson(request(ResponseFormat.jsonSchema(SCHEMA_NAME, SCHEMA, true)));

		Map<String, Object> format = asMap(asMap(parse(json).get("text")).get("format"));
		assertEquals("json_schema", format.get("type"));
		assertEquals(SCHEMA_NAME, format.get("name"));
		assertEquals(Boolean.TRUE, format.get("strict"));
		assertEquals(SCHEMA, format.get("schema"));
	}

	/** Untyped JSON stays the Responses API's own {@code json_object} format. */
	public void testOpenAIResponsesKeepsUntypedJson() throws Exception {
		String json = OpenAIResponsesRequestBuilder.buildRequestJson(request(ResponseFormat.JSON));

		assertEquals("json_object", asMap(asMap(parse(json).get("text")).get("format")).get("type"));
	}

	/** The Converse API has no response format at all, so it refuses rather than ignores. */
	public void testBedrockRefusesEveryJsonFormat() throws Exception {
		try {
			BedrockConverseRequestBuilder.buildRequestJson(request(ResponseFormat.JSON));
			fail("A format the Converse API cannot express must not be dropped silently.");
		} catch (ProviderException expected) {
			assertEquals(ProviderException.Kind.INVALID_REQUEST, expected.kind());
		}
		try {
			BedrockConverseRequestBuilder
				.buildRequestJson(request(ResponseFormat.jsonSchema(SCHEMA_NAME, SCHEMA, false)));
			fail("A schema the Converse API cannot express must not be dropped silently.");
		} catch (ProviderException expected) {
			assertEquals(ProviderException.Kind.INVALID_REQUEST, expected.kind());
		}

		// A request that asks for nothing is unaffected.
		assertNotNull(BedrockConverseRequestBuilder.buildRequestJson(request(null)));
	}

	/** An unnamed schema still gets a name: the providers that need one always get one. */
	public void testSchemaWithoutANameGetsTheDefault() {
		assertEquals(ResponseFormat.DEFAULT_SCHEMA_NAME,
			ResponseFormat.jsonSchema(null, SCHEMA, false).schemaName());
		assertEquals(ResponseFormat.DEFAULT_SCHEMA_NAME,
			ResponseFormat.jsonSchema("  ", SCHEMA, false).schemaName());
	}

	/** The format holds its own copy: a schema changed afterwards is not the one that was sent. */
	public void testSchemaIsCopied() {
		Map<String, Object> mutable = new LinkedHashMap<>(SCHEMA);
		ResponseFormat format = ResponseFormat.jsonSchema(SCHEMA_NAME, mutable, false);

		mutable.put("type", "array");

		assertEquals("object", format.schema().get("type"));
	}
}
