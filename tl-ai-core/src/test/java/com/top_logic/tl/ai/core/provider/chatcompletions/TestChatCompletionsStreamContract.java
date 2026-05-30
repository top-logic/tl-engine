/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.chatcompletions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.tl.ai.core.provider.AbstractProviderStreamTest;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;

/**
 * Contract tests for {@link ChatCompletionsProvider} stream parsing.
 */
public class TestChatCompletionsStreamContract extends AbstractProviderStreamTest {

	static class TestableProvider extends ChatCompletionsProvider {
		/** */
		TestableProvider() {
			super("test-key", "https://example.test");
		}

		/** */
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
			+ "data: {\"id\":\"chatcmpl-01\",\"model\":\"mistral-small-latest\","
			+ "\"choices\":[{\"index\":0,\"delta\":{\"content\":\"Hello\"},\"finish_reason\":null}]}\n\n"

			+ "data: {\"id\":\"chatcmpl-01\",\"model\":\"mistral-small-latest\","
			+ "\"choices\":[{\"index\":0,\"delta\":{\"content\":\" world\"},\"finish_reason\":null}]}\n\n"

			+ "data: {\"id\":\"chatcmpl-01\",\"model\":\"mistral-small-latest\","
			+ "\"choices\":[{\"index\":0,\"delta\":{},\"finish_reason\":\"stop\"}],"
			+ "\"usage\":{\"prompt_tokens\":10,\"completion_tokens\":5}}\n\n"

			+ "data: [DONE]\n\n";
	}

	@Override
	protected String cannedToolCallSSE() {
		return ""
			+ "data: {\"id\":\"chatcmpl-02\",\"model\":\"mistral-small-latest\","
			+ "\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"id\":\"call_01\","
			+ "\"function\":{\"name\":\"get_weather\","
			+ "\"arguments\":\"{\\\"city\\\":\\\"Berlin\\\"}\"}}]},\"finish_reason\":null}]}\n\n"

			+ "data: {\"id\":\"chatcmpl-02\",\"model\":\"mistral-small-latest\","
			+ "\"choices\":[{\"index\":0,\"delta\":{},\"finish_reason\":\"tool_calls\"}],"
			+ "\"usage\":{\"prompt_tokens\":15,\"completion_tokens\":8}}\n\n"

			+ "data: [DONE]\n\n";
	}
}
