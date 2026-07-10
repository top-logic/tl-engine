/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.List;

import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.message.ToolCall;

import junit.framework.TestCase;

/**
 * Tests for {@link ContentBlockAccumulator}.
 */
public class TestContentBlockAccumulator extends TestCase {

	/** A text block with appended text produces a {@link TextContent}. */
	public void testTextBlock() {
		ContentBlockAccumulator acc = new ContentBlockAccumulator();
		acc.addBlock(0, "text", null, null);
		acc.appendText(0, "Hello, ");
		acc.appendText(0, "world!");

		List<ContentPart> parts = acc.buildContentParts();
		assertEquals(1, parts.size());
		assertInstanceOf(TextContent.class, parts.get(0));
		assertEquals("Hello, world!", ((TextContent) parts.get(0)).text());
	}

	/** A thinking block with appended text produces a {@link ThinkingContent}. */
	public void testThinkingBlock() {
		ContentBlockAccumulator acc = new ContentBlockAccumulator();
		acc.addBlock(0, "thinking", null, null);
		acc.appendText(0, "Let me reason about this.");

		List<ContentPart> parts = acc.buildContentParts();
		assertEquals(1, parts.size());
		assertInstanceOf(ThinkingContent.class, parts.get(0));
		assertEquals("Let me reason about this.", ((ThinkingContent) parts.get(0)).text());
	}

	/** A tool_use block with accumulated argument fragments produces a {@link ToolCall}. */
	public void testToolCallBlock() {
		ContentBlockAccumulator acc = new ContentBlockAccumulator();
		acc.addBlock(0, "tool_use", "call-123", "my_tool");
		acc.appendArguments(0, "{\"key\"");
		acc.appendArguments(0, ": \"value\"}");

		List<ContentPart> parts = acc.buildContentParts();
		assertEquals(1, parts.size());
		assertInstanceOf(ToolCall.class, parts.get(0));
		ToolCall call = (ToolCall) parts.get(0);
		assertEquals("call-123", call.id());
		assertEquals("my_tool", call.name());
		assertEquals("{\"key\": \"value\"}", call.arguments());
	}

	/** A function_call block (OpenAI Responses naming) also produces a {@link ToolCall}. */
	public void testFunctionCallBlock() {
		ContentBlockAccumulator acc = new ContentBlockAccumulator();
		acc.addBlock(0, "function_call", "fc-456", "search");
		acc.appendArguments(0, "{\"query\": \"foo\"}");

		List<ContentPart> parts = acc.buildContentParts();
		assertEquals(1, parts.size());
		assertInstanceOf(ToolCall.class, parts.get(0));
		ToolCall call = (ToolCall) parts.get(0);
		assertEquals("fc-456", call.id());
		assertEquals("search", call.name());
		assertEquals("{\"query\": \"foo\"}", call.arguments());
	}

	/** setArguments replaces any previously appended argument fragments. */
	public void testSetArguments() {
		ContentBlockAccumulator acc = new ContentBlockAccumulator();
		acc.addBlock(0, "tool_use", "call-789", "run");
		acc.appendArguments(0, "{\"partial\"");
		acc.setArguments(0, "{\"final\": true}");

		List<ContentPart> parts = acc.buildContentParts();
		assertEquals(1, parts.size());
		ToolCall call = (ToolCall) parts.get(0);
		assertEquals("{\"final\": true}", call.arguments());
	}

	/** Multiple blocks are preserved in insertion order. */
	public void testMultipleBlocks() {
		ContentBlockAccumulator acc = new ContentBlockAccumulator();
		acc.addBlock(0, "text", null, null);
		acc.appendText(0, "Before the call.");
		acc.addBlock(1, "tool_use", "call-001", "fetch");
		acc.appendArguments(1, "{\"url\": \"https://example.com\"}");

		List<ContentPart> parts = acc.buildContentParts();
		assertEquals(2, parts.size());
		assertInstanceOf(TextContent.class, parts.get(0));
		assertInstanceOf(ToolCall.class, parts.get(1));
		assertEquals("Before the call.", ((TextContent) parts.get(0)).text());
		assertEquals("fetch", ((ToolCall) parts.get(1)).name());
	}

	/** getBlockType returns the type for known indices and null for unknown indices. */
	public void testGetBlockType() {
		ContentBlockAccumulator acc = new ContentBlockAccumulator();
		acc.addBlock(0, "text", null, null);
		acc.addBlock(1, "tool_use", "id", "tool");
		acc.addBlock(5, "thinking", null, null);

		assertEquals("text", acc.getBlockType(0));
		assertEquals("tool_use", acc.getBlockType(1));
		assertEquals("thinking", acc.getBlockType(5));
		assertNull(acc.getBlockType(2));
		assertNull(acc.getBlockType(99));
	}

	/** A text block with no appended text is omitted from buildContentParts. */
	public void testEmptyTextBlockOmitted() {
		ContentBlockAccumulator acc = new ContentBlockAccumulator();
		acc.addBlock(0, "text", null, null);

		List<ContentPart> parts = acc.buildContentParts();
		assertTrue(parts.isEmpty());
	}

	// --- helpers ---

	private static <T> void assertInstanceOf(Class<T> expectedType, Object actual) {
		assertTrue(
			"Expected instance of " + expectedType.getSimpleName()
				+ " but was " + (actual == null ? "null" : actual.getClass().getSimpleName()),
			expectedType.isInstance(actual));
	}
}
