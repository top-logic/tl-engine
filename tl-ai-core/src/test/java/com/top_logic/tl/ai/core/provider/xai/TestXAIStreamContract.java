/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.xai;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.tl.ai.core.provider.AbstractProviderStreamTest;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;

/**
 * Contract tests for {@link XAIProvider} stream parsing.
 *
 * <p>{@link XAIProvider} extends {@link com.top_logic.tl.ai.core.provider.openai.OpenAIProvider}
 * with an identical wire format. This test verifies the subclass inherits correct parsing.</p>
 */
public class TestXAIStreamContract extends AbstractProviderStreamTest {

	static class TestableProvider extends XAIProvider {
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
			+ "data: {\"type\":\"response.created\",\"response\":{\"id\":\"resp_x1\",\"model\":\"grok-3-mini\"}}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.output_item.added\",\"output_index\":0,"
			+ "\"item\":{\"type\":\"message\",\"id\":\"item_x1\"}}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.output_text.delta\",\"output_index\":0,"
			+ "\"content_index\":0,\"delta\":\"Hello\"}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.output_text.delta\",\"output_index\":0,"
			+ "\"content_index\":0,\"delta\":\" world\"}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.output_item.done\",\"output_index\":0,"
			+ "\"item\":{\"type\":\"message\",\"id\":\"item_x1\"}}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.completed\",\"response\":{\"id\":\"resp_x1\","
			+ "\"model\":\"grok-3-mini\",\"status\":\"completed\","
			+ "\"usage\":{\"input_tokens\":10,\"output_tokens\":5}}}\n\n";
	}

	@Override
	protected String cannedToolCallSSE() {
		return ""
			+ "event: message\n"
			+ "data: {\"type\":\"response.created\",\"response\":{\"id\":\"resp_x2\",\"model\":\"grok-3-mini\"}}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.output_item.added\",\"output_index\":0,"
			+ "\"item\":{\"type\":\"function_call\",\"id\":\"fc_x1\",\"call_id\":\"call_x1\","
			+ "\"name\":\"get_weather\"}}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.function_call_arguments.delta\",\"output_index\":0,"
			+ "\"delta\":\"{\\\"city\\\":\"}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.function_call_arguments.delta\",\"output_index\":0,"
			+ "\"delta\":\"\\\"Berlin\\\"}\"}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.output_item.done\",\"output_index\":0,"
			+ "\"item\":{\"type\":\"function_call\",\"id\":\"fc_x1\",\"call_id\":\"call_x1\","
			+ "\"name\":\"get_weather\",\"arguments\":\"{\\\"city\\\":\\\"Berlin\\\"}\"}}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.completed\",\"response\":{\"id\":\"resp_x2\","
			+ "\"model\":\"grok-3-mini\",\"status\":\"completed\","
			+ "\"usage\":{\"input_tokens\":15,\"output_tokens\":8}}}\n\n";
	}

	@Override
	protected String cannedThinkingSSE() {
		return ""
			+ "event: message\n"
			+ "data: {\"type\":\"response.created\",\"response\":{\"id\":\"resp_x3\",\"model\":\"grok-3-mini\"}}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.output_item.added\",\"output_index\":0,"
			+ "\"item\":{\"type\":\"reasoning\",\"id\":\"item_xr1\"}}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.reasoning_summary_text.delta\",\"output_index\":0,"
			+ "\"content_index\":0,\"delta\":\"Thinking deeply.\"}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.output_item.done\",\"output_index\":0,"
			+ "\"item\":{\"type\":\"reasoning\",\"id\":\"item_xr1\"}}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.output_item.added\",\"output_index\":1,"
			+ "\"item\":{\"type\":\"message\",\"id\":\"item_xm1\"}}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.output_text.delta\",\"output_index\":1,"
			+ "\"content_index\":0,\"delta\":\"The answer is 42.\"}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.output_item.done\",\"output_index\":1,"
			+ "\"item\":{\"type\":\"message\",\"id\":\"item_xm1\"}}\n\n"

			+ "event: message\n"
			+ "data: {\"type\":\"response.completed\",\"response\":{\"id\":\"resp_x3\","
			+ "\"model\":\"grok-3-mini\",\"status\":\"completed\","
			+ "\"usage\":{\"input_tokens\":12,\"output_tokens\":15,"
			+ "\"output_tokens_details\":{\"reasoning_tokens\":10}}}}\n\n";
	}
}
