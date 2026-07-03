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

import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.provider.AbstractProviderStreamTest;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.StreamEventContractAssertions;

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

	/**
	 * Tool call where {@code function.arguments} is split across several deltas, as Azure OpenAI
	 * and the canonical OpenAI Chat Completions API stream it: only the first delta carries
	 * {@code id} and {@code name}; subsequent deltas carry argument fragments keyed by
	 * {@code index} with no id/name. The fragments must be concatenated by index.
	 */
	public void testSplitToolCallArgumentsAccumulate() throws Exception {
		String sse = ""
			+ "data: {\"id\":\"chatcmpl-03\",\"model\":\"gpt-5\","
			+ "\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"index\":0,\"id\":\"call_99\","
			+ "\"type\":\"function\",\"function\":{\"name\":\"loadSkills\",\"arguments\":\"\"}}]},"
			+ "\"finish_reason\":null}]}\n\n"

			+ "data: {\"id\":\"chatcmpl-03\",\"model\":\"gpt-5\","
			+ "\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"index\":0,"
			+ "\"function\":{\"arguments\":\"{\\\"id\\\":\"}}]},\"finish_reason\":null}]}\n\n"

			+ "data: {\"id\":\"chatcmpl-03\",\"model\":\"gpt-5\","
			+ "\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"index\":0,"
			+ "\"function\":{\"arguments\":\"\\\"weather\\\"}\"}}]},\"finish_reason\":null}]}\n\n"

			+ "data: {\"id\":\"chatcmpl-03\",\"model\":\"gpt-5\","
			+ "\"choices\":[{\"index\":0,\"delta\":{},\"finish_reason\":\"tool_calls\"}],"
			+ "\"usage\":{\"prompt_tokens\":15,\"completion_tokens\":8}}\n\n"

			+ "data: [DONE]\n\n";

		List<StreamEvent> events = _provider.parse(toStream(sse));
		StreamEventContractAssertions.assertValidStream(events);

		ToolCall call = findToolCall(events);
		assertEquals("loadSkills", call.name());
		assertEquals("call_99", call.id());
		assertEquals("{\"id\":\"weather\"}", call.arguments());
	}

	/**
	 * Two tool calls streamed with split arguments, exercising index-keyed accumulation across
	 * distinct tool calls.
	 */
	public void testMultipleSplitToolCalls() throws Exception {
		String sse = ""
			+ "data: {\"id\":\"chatcmpl-04\",\"model\":\"gpt-5\","
			+ "\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"index\":0,\"id\":\"call_a\","
			+ "\"type\":\"function\",\"function\":{\"name\":\"first\",\"arguments\":\"\"}}]},"
			+ "\"finish_reason\":null}]}\n\n"

			+ "data: {\"id\":\"chatcmpl-04\",\"model\":\"gpt-5\","
			+ "\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"index\":1,\"id\":\"call_b\","
			+ "\"type\":\"function\",\"function\":{\"name\":\"second\",\"arguments\":\"\"}}]},"
			+ "\"finish_reason\":null}]}\n\n"

			+ "data: {\"id\":\"chatcmpl-04\",\"model\":\"gpt-5\","
			+ "\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"index\":0,"
			+ "\"function\":{\"arguments\":\"{\\\"x\\\":1}\"}}]},\"finish_reason\":null}]}\n\n"

			+ "data: {\"id\":\"chatcmpl-04\",\"model\":\"gpt-5\","
			+ "\"choices\":[{\"index\":0,\"delta\":{\"tool_calls\":[{\"index\":1,"
			+ "\"function\":{\"arguments\":\"{\\\"y\\\":2}\"}}]},\"finish_reason\":null}]}\n\n"

			+ "data: {\"id\":\"chatcmpl-04\",\"model\":\"gpt-5\","
			+ "\"choices\":[{\"index\":0,\"delta\":{},\"finish_reason\":\"tool_calls\"}],"
			+ "\"usage\":{\"prompt_tokens\":20,\"completion_tokens\":12}}\n\n"

			+ "data: [DONE]\n\n";

		List<StreamEvent> events = _provider.parse(toStream(sse));
		StreamEventContractAssertions.assertValidStream(events);

		List<ToolCall> calls = events.stream()
			.filter(StreamEvent.MessageComplete.class::isInstance)
			.map(StreamEvent.MessageComplete.class::cast)
			.findFirst().orElseThrow()
			.content().stream()
			.filter(ToolCall.class::isInstance)
			.map(ToolCall.class::cast)
			.toList();

		assertEquals(2, calls.size());
		assertEquals("first", calls.get(0).name());
		assertEquals("{\"x\":1}", calls.get(0).arguments());
		assertEquals("second", calls.get(1).name());
		assertEquals("{\"y\":2}", calls.get(1).arguments());
	}

	/**
	 * With {@code stream_options.include_usage}, OpenAI/Azure send {@code "usage": null} on every
	 * content chunk and emit the populated usage in a trailing chunk whose {@code choices} array is
	 * empty. The parser must tolerate the null intermediates and capture the final usage.
	 */
	public void testIncludeUsageFinalChunk() throws Exception {
		String sse = ""
			+ "data: {\"id\":\"chatcmpl-05\",\"model\":\"gpt-5\","
			+ "\"choices\":[{\"index\":0,\"delta\":{\"content\":\"Hi\"},\"finish_reason\":null}],"
			+ "\"usage\":null}\n\n"

			+ "data: {\"id\":\"chatcmpl-05\",\"model\":\"gpt-5\","
			+ "\"choices\":[{\"index\":0,\"delta\":{},\"finish_reason\":\"stop\"}],\"usage\":null}\n\n"

			+ "data: {\"id\":\"chatcmpl-05\",\"model\":\"gpt-5\",\"choices\":[],"
			+ "\"usage\":{\"prompt_tokens\":11,\"completion_tokens\":3,\"total_tokens\":14}}\n\n"

			+ "data: [DONE]\n\n";

		List<StreamEvent> events = _provider.parse(toStream(sse));
		StreamEventContractAssertions.assertValidStream(events);

		StreamEvent.MessageComplete complete = events.stream()
			.filter(StreamEvent.MessageComplete.class::isInstance)
			.map(StreamEvent.MessageComplete.class::cast)
			.findFirst().orElseThrow();
		assertEquals(11, complete.usage().inputTokens());
		assertEquals(3, complete.usage().outputTokens());
	}

	/**
	 * OpenAI/Azure automatic prompt caching reports cache hits in
	 * {@code usage.prompt_tokens_details.cached_tokens}; this must round-trip into the
	 * {@link com.top_logic.tl.ai.core.message.TokenUsage} cache-read field.
	 */
	public void testCachedTokensParsed() throws Exception {
		String sse = ""
			+ "data: {\"id\":\"chatcmpl-06\",\"model\":\"gpt-5\","
			+ "\"choices\":[{\"index\":0,\"delta\":{\"content\":\"Hi\"},\"finish_reason\":null}]}\n\n"

			+ "data: {\"id\":\"chatcmpl-06\",\"model\":\"gpt-5\","
			+ "\"choices\":[{\"index\":0,\"delta\":{},\"finish_reason\":\"stop\"}],"
			+ "\"usage\":{\"prompt_tokens\":4096,\"completion_tokens\":12,"
			+ "\"prompt_tokens_details\":{\"cached_tokens\":3072}}}\n\n"

			+ "data: [DONE]\n\n";

		List<StreamEvent> events = _provider.parse(toStream(sse));
		StreamEventContractAssertions.assertValidStream(events);

		StreamEvent.MessageComplete complete = events.stream()
			.filter(StreamEvent.MessageComplete.class::isInstance)
			.map(StreamEvent.MessageComplete.class::cast)
			.findFirst().orElseThrow();
		// prompt_tokens (4096) includes the cached share; inputTokens is the non-cached
		// remainder (1024), cacheReadTokens carries the cached share, and promptTokens()
		// reconstructs the full prompt size (4096) for compaction/context-window checks.
		assertEquals(1024, complete.usage().inputTokens());
		assertEquals(3072, complete.usage().cacheReadTokens());
		assertEquals(4096, complete.usage().promptTokens());
	}

	private static ToolCall findToolCall(List<StreamEvent> events) {
		return events.stream()
			.filter(StreamEvent.MessageComplete.class::isInstance)
			.map(StreamEvent.MessageComplete.class::cast)
			.findFirst().orElseThrow()
			.content().stream()
			.filter(ToolCall.class::isInstance)
			.map(ToolCall.class::cast)
			.findFirst().orElseThrow();
	}
}
