/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.openai;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.tl.ai.core.provider.AbstractProviderStreamTest;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;

/**
 * Contract tests for {@link OpenAIResponsesProvider} stream parsing.
 */
public class TestOpenAIStreamContract extends AbstractProviderStreamTest {

	static class TestableProvider extends OpenAIResponsesProvider {
		TestableProvider() {
			super("test-key");
		}

		List<StreamEvent> parse(InputStream sse) throws IOException, ProviderException {
			List<StreamEvent> events = new ArrayList<>();
			processStream(sse, events::add);
			return events;
		}
	}

	private final TestableProvider _provider = new TestableProvider();

	@Override
	protected List<StreamEvent> parseStream(InputStream sse) throws Exception {
		return _provider.parse(sse);
	}

	@Override
	protected String cannedTextSSE() {
		return ""
			+ "event: message\n"
			+ "data: {\"type\":\"response.created\",\"response\":{\"id\":\"resp_01\",\"model\":\"gpt-4.1-nano\"}}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.output_item.added\",\"output_index\":0,"
			+ "\"item\":{\"type\":\"message\",\"id\":\"item_01\"}}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.output_text.delta\",\"output_index\":0,"
			+ "\"content_index\":0,\"delta\":\"Hello\"}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.output_text.delta\",\"output_index\":0,"
			+ "\"content_index\":0,\"delta\":\" world\"}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.output_item.done\",\"output_index\":0,"
			+ "\"item\":{\"type\":\"message\",\"id\":\"item_01\"}}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.completed\",\"response\":{\"id\":\"resp_01\","
			+ "\"model\":\"gpt-4.1-nano\",\"status\":\"completed\","
			+ "\"usage\":{\"input_tokens\":10,\"output_tokens\":5}}}\n\n";
	}

	@Override
	protected String cannedToolCallSSE() {
		return ""
			+ "event: message\n"
			+ "data: {\"type\":\"response.created\",\"response\":{\"id\":\"resp_02\",\"model\":\"gpt-4.1-nano\"}}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.output_item.added\",\"output_index\":0,"
			+ "\"item\":{\"type\":\"function_call\",\"id\":\"fc_01\",\"call_id\":\"call_01\","
			+ "\"name\":\"get_weather\"}}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.function_call_arguments.delta\",\"output_index\":0,"
			+ "\"delta\":\"{\\\"city\\\":\"}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.function_call_arguments.delta\",\"output_index\":0,"
			+ "\"delta\":\"\\\"Berlin\\\"}\"}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.output_item.done\",\"output_index\":0,"
			+ "\"item\":{\"type\":\"function_call\",\"id\":\"fc_01\",\"call_id\":\"call_01\","
			+ "\"name\":\"get_weather\",\"arguments\":\"{\\\"city\\\":\\\"Berlin\\\"}\"}}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.completed\",\"response\":{\"id\":\"resp_02\","
			+ "\"model\":\"gpt-4.1-nano\",\"status\":\"completed\","
			+ "\"usage\":{\"input_tokens\":15,\"output_tokens\":8}}}\n\n";
	}

	@Override
	protected String cannedThinkingSSE() {
		return ""
			+ "event: message\n"
			+ "data: {\"type\":\"response.created\",\"response\":{\"id\":\"resp_03\",\"model\":\"o4-mini\"}}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.output_item.added\",\"output_index\":0,"
			+ "\"item\":{\"type\":\"reasoning\",\"id\":\"item_r1\"}}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.reasoning_summary_text.delta\",\"output_index\":0,"
			+ "\"content_index\":0,\"delta\":\"Thinking deeply.\"}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.output_item.done\",\"output_index\":0,"
			+ "\"item\":{\"type\":\"reasoning\",\"id\":\"item_r1\"}}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.output_item.added\",\"output_index\":1,"
			+ "\"item\":{\"type\":\"message\",\"id\":\"item_m1\"}}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.output_text.delta\",\"output_index\":1,"
			+ "\"content_index\":0,\"delta\":\"The answer is 42.\"}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.output_item.done\",\"output_index\":1,"
			+ "\"item\":{\"type\":\"message\",\"id\":\"item_m1\"}}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.completed\",\"response\":{\"id\":\"resp_03\","
			+ "\"model\":\"o4-mini\",\"status\":\"completed\","
			+ "\"usage\":{\"input_tokens\":12,\"output_tokens\":15,"
			+ "\"output_tokens_details\":{\"reasoning_tokens\":10}}}}\n\n";
	}
}
