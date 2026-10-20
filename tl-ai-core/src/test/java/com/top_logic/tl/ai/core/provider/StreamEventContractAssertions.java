/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.message.ToolCall;

import org.junit.Assert;

/**
 * Reusable assertions that verify a {@link List} of {@link StreamEvent}s satisfies the provider
 * contract.
 *
 * <p>Use {@link #assertValidStream(List)} as the single entry point, or call individual methods
 * for selective checks in provider-specific tests.</p>
 */
public class StreamEventContractAssertions {

	/**
	 * Runs all contract checks on the given event list.
	 */
	public static void assertValidStream(List<StreamEvent> events) {
		assertStructure(events);
		assertLifecycleTriples(events);
		assertContentIndices(events);
		assertMessageComplete(events);
		assertStreamedContentMatchesComplete(events);
	}

	/**
	 * Verifies structural invariants: StreamStart is first, MessageComplete is last and unique.
	 */
	public static void assertStructure(List<StreamEvent> events) {
		Assert.assertFalse("Event list must not be empty.", events.isEmpty());
		Assert.assertSame("First event must be StreamStart.",
			StreamEvent.StreamStart.class, events.get(0).getClass());
		Assert.assertSame("Last event must be MessageComplete.",
			StreamEvent.MessageComplete.class, events.get(events.size() - 1).getClass());

		int completeCount = (int) events.stream()
			.filter(StreamEvent.MessageComplete.class::isInstance)
			.count();
		Assert.assertEquals("Exactly one MessageComplete expected.", 1, completeCount);

		int errorCount = (int) events.stream()
			.filter(StreamEvent.Error.class::isInstance)
			.count();
		Assert.assertEquals("No Error events expected in a successful stream.", 0, errorCount);
	}

	/**
	 * Verifies lifecycle triples: every Start has a matching End, Deltas only appear between
	 * their matching Start/End, and each block's events are contiguous (no interleaving).
	 */
	public static void assertLifecycleTriples(List<StreamEvent> events) {
		Map<Integer, String> openBlocks = new HashMap<>();
		Map<Integer, Boolean> blockClosed = new HashMap<>();

		for (StreamEvent event : events) {
			if (event instanceof StreamEvent.TextStart e) {
				assertBlockNotOpen(openBlocks, e.contentIndex(), "TextStart");
				assertBlockNotReopened(blockClosed, e.contentIndex());
				openBlocks.put(e.contentIndex(), "text");
			} else if (event instanceof StreamEvent.TextDelta e) {
				assertBlockOpen(openBlocks, e.contentIndex(), "text", "TextDelta");
				Assert.assertNotNull("TextDelta.text must not be null.", e.text());
				Assert.assertFalse("TextDelta.text must not be empty.", e.text().isEmpty());
			} else if (event instanceof StreamEvent.TextEnd e) {
				assertBlockOpen(openBlocks, e.contentIndex(), "text", "TextEnd");
				openBlocks.remove(e.contentIndex());
				blockClosed.put(e.contentIndex(), true);
			} else if (event instanceof StreamEvent.ThinkingStart e) {
				assertBlockNotOpen(openBlocks, e.contentIndex(), "ThinkingStart");
				assertBlockNotReopened(blockClosed, e.contentIndex());
				openBlocks.put(e.contentIndex(), "thinking");
			} else if (event instanceof StreamEvent.ThinkingDelta e) {
				assertBlockOpen(openBlocks, e.contentIndex(), "thinking", "ThinkingDelta");
				Assert.assertNotNull("ThinkingDelta.text must not be null.", e.text());
				Assert.assertFalse("ThinkingDelta.text must not be empty.", e.text().isEmpty());
			} else if (event instanceof StreamEvent.ThinkingEnd e) {
				assertBlockOpen(openBlocks, e.contentIndex(), "thinking", "ThinkingEnd");
				openBlocks.remove(e.contentIndex());
				blockClosed.put(e.contentIndex(), true);
			} else if (event instanceof StreamEvent.ToolCallStart e) {
				assertBlockNotOpen(openBlocks, e.contentIndex(), "ToolCallStart");
				assertBlockNotReopened(blockClosed, e.contentIndex());
				openBlocks.put(e.contentIndex(), "tool_call");
				Assert.assertNotNull("ToolCallStart.id must not be null.", e.id());
				Assert.assertNotNull("ToolCallStart.name must not be null.", e.name());
			} else if (event instanceof StreamEvent.ToolCallDelta e) {
				assertBlockOpen(openBlocks, e.contentIndex(), "tool_call", "ToolCallDelta");
				Assert.assertNotNull("ToolCallDelta.partialJson must not be null.", e.partialJson());
			} else if (event instanceof StreamEvent.ToolCallEnd e) {
				assertBlockOpen(openBlocks, e.contentIndex(), "tool_call", "ToolCallEnd");
				openBlocks.remove(e.contentIndex());
				blockClosed.put(e.contentIndex(), true);
			}
			/* StreamStart, MessageComplete, Error -- handled elsewhere. */
		}

		Assert.assertTrue("All blocks must be closed. Unclosed: " + openBlocks, openBlocks.isEmpty());
	}

	/**
	 * Verifies that content indices are sequential starting from 0 with no gaps.
	 */
	public static void assertContentIndices(List<StreamEvent> events) {
		List<Integer> startIndices = new ArrayList<>();
		for (StreamEvent event : events) {
			if (event instanceof StreamEvent.TextStart e) {
				startIndices.add(e.contentIndex());
			} else if (event instanceof StreamEvent.ThinkingStart e) {
				startIndices.add(e.contentIndex());
			} else if (event instanceof StreamEvent.ToolCallStart e) {
				startIndices.add(e.contentIndex());
			}
		}

		for (int i = 0; i < startIndices.size(); i++) {
			Assert.assertEquals("Content indices must be sequential. Expected " + i
				+ " but found " + startIndices.get(i) + " at position " + i + ".",
				i, (int) startIndices.get(i));
		}
	}

	/**
	 * Verifies MessageComplete field invariants.
	 */
	public static void assertMessageComplete(List<StreamEvent> events) {
		StreamEvent.MessageComplete complete = extractComplete(events);
		Assert.assertNotNull("MessageComplete.id must not be null.", complete.id());
		Assert.assertNotNull("MessageComplete.model must not be null.", complete.model());
		Assert.assertNotNull("MessageComplete.finishReason must not be null.", complete.finishReason());
		Assert.assertNotNull("MessageComplete.usage must not be null.", complete.usage());
		Assert.assertTrue("inputTokens must be non-negative.", complete.usage().inputTokens() >= 0);
		Assert.assertTrue("outputTokens must be non-negative.", complete.usage().outputTokens() >= 0);
		Assert.assertFalse("MessageComplete.content must not be empty.", complete.content().isEmpty());
	}

	/**
	 * Verifies that streamed deltas match the content parts in MessageComplete.
	 */
	public static void assertStreamedContentMatchesComplete(List<StreamEvent> events) {
		StreamEvent.MessageComplete complete = extractComplete(events);

		Map<Integer, StringBuilder> textByIndex = new HashMap<>();
		Map<Integer, StringBuilder> thinkingByIndex = new HashMap<>();
		Map<Integer, String> toolNameByIndex = new HashMap<>();
		Map<Integer, StringBuilder> toolArgsByIndex = new HashMap<>();

		for (StreamEvent event : events) {
			if (event instanceof StreamEvent.TextDelta e) {
				textByIndex.computeIfAbsent(e.contentIndex(), k -> new StringBuilder()).append(e.text());
			} else if (event instanceof StreamEvent.ThinkingDelta e) {
				thinkingByIndex.computeIfAbsent(e.contentIndex(), k -> new StringBuilder()).append(e.text());
			} else if (event instanceof StreamEvent.ToolCallStart e) {
				toolNameByIndex.put(e.contentIndex(), e.name());
			} else if (event instanceof StreamEvent.ToolCallDelta e) {
				toolArgsByIndex.computeIfAbsent(e.contentIndex(), k -> new StringBuilder()).append(e.partialJson());
			}
		}

		for (ContentPart part : complete.content()) {
			if (part instanceof TextContent tc) {
				boolean found = false;
				for (Map.Entry<Integer, StringBuilder> entry : textByIndex.entrySet()) {
					if (entry.getValue().toString().equals(tc.text())) {
						found = true;
						break;
					}
				}
				Assert.assertTrue(
					"TextContent text must match accumulated TextDelta text. Got: \"" + tc.text() + "\"",
					found);
			} else if (part instanceof ThinkingContent tc) {
				boolean found = false;
				for (Map.Entry<Integer, StringBuilder> entry : thinkingByIndex.entrySet()) {
					if (entry.getValue().toString().equals(tc.text())) {
						found = true;
						break;
					}
				}
				Assert.assertTrue(
					"ThinkingContent text must match accumulated ThinkingDelta text. Got: \"" + tc.text() + "\"",
					found);
			} else if (part instanceof ToolCall tc) {
				Assert.assertTrue("ToolCall name must appear in a ToolCallStart event.",
					toolNameByIndex.containsValue(tc.name()));
			}
			/* ImageContent, PdfContent -- not tested here. */
		}
	}

	private static StreamEvent.MessageComplete extractComplete(List<StreamEvent> events) {
		StreamEvent.MessageComplete result = null;
		for (StreamEvent event : events) {
			if (event instanceof StreamEvent.MessageComplete mc) {
				result = mc;
				break;
			}
		}
		Assert.assertNotNull("No MessageComplete found.", result);
		return result;
	}

	private static void assertBlockNotOpen(Map<Integer, String> openBlocks, int index, String eventName) {
		Assert.assertFalse(eventName + " at index " + index + " but a block is already open there.",
			openBlocks.containsKey(index));
	}

	private static void assertBlockNotReopened(Map<Integer, Boolean> closedBlocks, int index) {
		Assert.assertFalse("Block at index " + index + " was already closed -- cannot reopen.",
			closedBlocks.containsKey(index));
	}

	private static void assertBlockOpen(Map<Integer, String> openBlocks, int index,
			String expectedType, String eventName) {
		String actualType = openBlocks.get(index);
		Assert.assertNotNull(eventName + " at index " + index + " but no block is open there.", actualType);
		Assert.assertEquals(eventName + " at index " + index + " -- wrong block type.",
			expectedType, actualType);
	}
}
