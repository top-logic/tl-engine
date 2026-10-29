/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.anthropic;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.tl.ai.core.provider.FixedCredentials;
import com.top_logic.tl.ai.core.provider.AbstractProviderStreamTest;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;

/**
 * Contract tests for {@link AnthropicMessagesProvider} stream parsing.
 */
public class TestAnthropicStreamContract extends AbstractProviderStreamTest {

	static class TestableProvider extends AnthropicMessagesProvider {
		TestableProvider() {
			super(FixedCredentials.NONE);
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
			+ "event: message_start\n"
			+ "data: {\"type\":\"message_start\",\"message\":{\"id\":\"msg_01\",\"type\":\"message\","
			+ "\"role\":\"assistant\",\"model\":\"claude-haiku-4-5-20251001\","
			+ "\"usage\":{\"input_tokens\":10,\"output_tokens\":0}}}\n\n"

			+ "event: content_block_start\n"
			+ "data: {\"type\":\"content_block_start\",\"index\":0,"
			+ "\"content_block\":{\"type\":\"text\",\"text\":\"\"}}\n\n"

			+ "event: content_block_delta\n"
			+ "data: {\"type\":\"content_block_delta\",\"index\":0,"
			+ "\"delta\":{\"type\":\"text_delta\",\"text\":\"Hello\"}}\n\n"

			+ "event: content_block_delta\n"
			+ "data: {\"type\":\"content_block_delta\",\"index\":0,"
			+ "\"delta\":{\"type\":\"text_delta\",\"text\":\" world\"}}\n\n"

			+ "event: content_block_stop\n"
			+ "data: {\"type\":\"content_block_stop\",\"index\":0}\n\n"

			+ "event: message_delta\n"
			+ "data: {\"type\":\"message_delta\","
			+ "\"delta\":{\"stop_reason\":\"end_turn\"},\"usage\":{\"output_tokens\":5}}\n\n"

			+ "event: message_stop\n"
			+ "data: {\"type\":\"message_stop\"}\n\n";
	}

	@Override
	protected String cannedToolCallSSE() {
		return ""
			+ "event: message_start\n"
			+ "data: {\"type\":\"message_start\",\"message\":{\"id\":\"msg_02\",\"type\":\"message\","
			+ "\"role\":\"assistant\",\"model\":\"claude-haiku-4-5-20251001\","
			+ "\"usage\":{\"input_tokens\":15,\"output_tokens\":0}}}\n\n"

			+ "event: content_block_start\n"
			+ "data: {\"type\":\"content_block_start\",\"index\":0,"
			+ "\"content_block\":{\"type\":\"tool_use\",\"id\":\"toolu_01\",\"name\":\"get_weather\"}}\n\n"

			+ "event: content_block_delta\n"
			+ "data: {\"type\":\"content_block_delta\",\"index\":0,"
			+ "\"delta\":{\"type\":\"input_json_delta\",\"partial_json\":\"{\\\"city\\\":\"}}\n\n"

			+ "event: content_block_delta\n"
			+ "data: {\"type\":\"content_block_delta\",\"index\":0,"
			+ "\"delta\":{\"type\":\"input_json_delta\",\"partial_json\":\"\\\"Berlin\\\"}\"}}\n\n"

			+ "event: content_block_stop\n"
			+ "data: {\"type\":\"content_block_stop\",\"index\":0}\n\n"

			+ "event: message_delta\n"
			+ "data: {\"type\":\"message_delta\","
			+ "\"delta\":{\"stop_reason\":\"tool_use\"},\"usage\":{\"output_tokens\":8}}\n\n"

			+ "event: message_stop\n"
			+ "data: {\"type\":\"message_stop\"}\n\n";
	}

	@Override
	protected String cannedThinkingSSE() {
		return ""
			+ "event: message_start\n"
			+ "data: {\"type\":\"message_start\",\"message\":{\"id\":\"msg_03\",\"type\":\"message\","
			+ "\"role\":\"assistant\",\"model\":\"claude-sonnet-4-20250514\","
			+ "\"usage\":{\"input_tokens\":12,\"output_tokens\":0}}}\n\n"

			+ "event: content_block_start\n"
			+ "data: {\"type\":\"content_block_start\",\"index\":0,"
			+ "\"content_block\":{\"type\":\"thinking\",\"text\":\"\"}}\n\n"

			+ "event: content_block_delta\n"
			+ "data: {\"type\":\"content_block_delta\",\"index\":0,"
			+ "\"delta\":{\"type\":\"thinking_delta\",\"thinking\":\"Let me think about this.\"}}\n\n"

			+ "event: content_block_stop\n"
			+ "data: {\"type\":\"content_block_stop\",\"index\":0}\n\n"

			+ "event: content_block_start\n"
			+ "data: {\"type\":\"content_block_start\",\"index\":1,"
			+ "\"content_block\":{\"type\":\"text\",\"text\":\"\"}}\n\n"

			+ "event: content_block_delta\n"
			+ "data: {\"type\":\"content_block_delta\",\"index\":1,"
			+ "\"delta\":{\"type\":\"text_delta\",\"text\":\"The answer is 42.\"}}\n\n"

			+ "event: content_block_stop\n"
			+ "data: {\"type\":\"content_block_stop\",\"index\":1}\n\n"

			+ "event: message_delta\n"
			+ "data: {\"type\":\"message_delta\","
			+ "\"delta\":{\"stop_reason\":\"end_turn\"},\"usage\":{\"output_tokens\":15}}\n\n"

			+ "event: message_stop\n"
			+ "data: {\"type\":\"message_stop\"}\n\n";
	}
}
