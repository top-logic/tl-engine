/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.message.ToolCall;

/**
 * Accumulates streaming content blocks from an LLM and assembles them into {@link ContentPart}
 * instances.
 *
 * <p>Blocks are registered by index via {@link #addBlock} and then populated incrementally
 * through {@link #appendText}, {@link #appendArguments}, and {@link #setArguments}. Once the
 * stream is complete, {@link #buildContentParts} converts all accumulated blocks to the
 * appropriate {@link ContentPart} subtypes.</p>
 *
 * <p>Supports the following block types across providers:</p>
 * <ul>
 *   <li>{@code text} / {@code message} — {@link TextContent} (omitted if empty)</li>
 *   <li>{@code thinking} / {@code reasoning} — {@link ThinkingContent} (omitted if empty)</li>
 *   <li>{@code tool_use} / {@code function_call} — {@link ToolCall}</li>
 * </ul>
 *
 * <p>Not thread-safe — intended for single-stream use within a provider's
 * {@code processStream} method.</p>
 */
public class ContentBlockAccumulator {

	private final LinkedHashMap<Integer, Block> _blocks = new LinkedHashMap<>();

	/**
	 * Registers a new block at the given index.
	 *
	 * @param index
	 *        The block index (from the streaming protocol).
	 * @param type
	 *        The block type (e.g. {@code "text"}, {@code "tool_use"}, {@code "thinking"}).
	 * @param id
	 *        The tool call id; may be {@code null} for non-tool blocks.
	 * @param name
	 *        The tool name; may be {@code null} for non-tool blocks.
	 */
	public void addBlock(int index, String type, String id, String name) {
		Block block = new Block(type);
		block._id = id;
		block._name = name;
		_blocks.put(index, block);
	}

	/**
	 * Appends a text fragment to the block at the given index.
	 *
	 * <p>Used for {@code text}, {@code thinking}, {@code message}, and {@code reasoning} blocks.</p>
	 *
	 * @param index
	 *        The block index.
	 * @param text
	 *        The text fragment to append; ignored if blank.
	 */
	public void appendText(int index, String text) {
		Block block = _blocks.get(index);
		if (block != null && text != null) {
			block._text.append(text);
		}
	}

	/**
	 * Appends a JSON argument fragment to the block at the given index.
	 *
	 * <p>Used for tool blocks receiving partial JSON deltas.</p>
	 *
	 * @param index
	 *        The block index.
	 * @param json
	 *        The JSON fragment to append; ignored if null.
	 */
	public void appendArguments(int index, String json) {
		Block block = _blocks.get(index);
		if (block != null && json != null) {
			block._arguments.append(json);
		}
	}

	/**
	 * Replaces the accumulated arguments of the block at the given index with a final value.
	 *
	 * <p>Some providers emit a {@code done} event with the complete argument string; this method
	 * discards any previously appended fragments and stores the authoritative value instead.</p>
	 *
	 * @param index
	 *        The block index.
	 * @param json
	 *        The final JSON arguments string.
	 */
	public void setArguments(int index, String json) {
		Block block = _blocks.get(index);
		if (block != null && json != null) {
			block._arguments.setLength(0);
			block._arguments.append(json);
		}
	}

	/**
	 * Returns the type string of the block at the given index, or {@code null} if no block
	 * is registered at that index.
	 *
	 * @param index
	 *        The block index.
	 * @return the block type, or {@code null}.
	 */
	public String getBlockType(int index) {
		Block block = _blocks.get(index);
		return block != null ? block._type : null;
	}

	/**
	 * Sets the continuity token on the block at the given index.
	 *
	 * <p>Used for Anthropic's {@code signature} and OpenAI's {@code encrypted_content},
	 * which must be passed back in subsequent turns for multi-turn thinking continuity.</p>
	 *
	 * @param index
	 *        The block index.
	 * @param token
	 *        The opaque continuity token; ignored if null.
	 */
	public void setContinuityToken(int index, String token) {
		Block block = _blocks.get(index);
		if (block != null && token != null) {
			block._continuityToken = token;
		}
	}

	/**
	 * Assembles all accumulated blocks into a list of {@link ContentPart} instances.
	 *
	 * <p>Insertion order is preserved. Empty text and thinking blocks are omitted.</p>
	 *
	 * @return an ordered list of content parts; never {@code null}.
	 */
	public List<ContentPart> buildContentParts() {
		List<ContentPart> parts = new ArrayList<>(_blocks.size());
		for (Block block : _blocks.values()) {
			switch (block._type) {
				case "text", "message" -> {
					String text = block._text.toString();
					if (!text.isEmpty()) {
						parts.add(new TextContent(text));
					}
				}
				case "thinking", "reasoning" -> {
					String text = block._text.toString();
					if (!text.isEmpty() || block._continuityToken != null) {
						parts.add(new ThinkingContent(
							text.isEmpty() ? "" : text,
							block._continuityToken));
					}
				}
				case "tool_use", "function_call" -> {
					parts.add(new ToolCall(block._id, block._name, block._arguments.toString()));
				}
				default -> {
					// Unknown block type — skip.
				}
			}
		}
		return parts;
	}

	// --- Internal state ---

	private static final class Block {
		final String _type;

		final StringBuilder _text = new StringBuilder();

		String _id;

		String _name;

		final StringBuilder _arguments = new StringBuilder();

		String _continuityToken;

		Block(String type) {
			_type = type;
		}
	}
}
