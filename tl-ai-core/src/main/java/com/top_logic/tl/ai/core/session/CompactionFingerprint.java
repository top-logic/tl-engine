/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.CompactionMarker;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.PdfContent;
import com.top_logic.tl.ai.core.message.SystemMessage;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;

/**
 * Fingerprint of a message history, used to decide whether a {@link CompactionSidecar} still
 * describes the history it was written for.
 *
 * <h2>What is hashed, and why exactly that</h2>
 *
 * <p>
 * The fingerprint covers <b>role and content</b> - what the model was shown - and nothing
 * that is regenerated on the way through storage. Specifically it excludes:
 * </p>
 * <ul>
 * <li><b>Message ids and timestamps.</b> {@link ConversationLogReplay} mints a fresh
 * {@link java.util.UUID} and an {@link java.time.Instant#now()} for every message it
 * rebuilds, so hashing either would make the fingerprint differ from itself on the next
 * reload. This is the trap that broke the first version of the same design in cline
 * (issue 12747): every save was rejected as stale, and the session re-compacted on every
 * single turn.</li>
 * <li><b>Token usage and finish reason.</b> Replay attributes a turn's cumulative usage to
 * every assistant message it emits and labels them all {@code "stop"}; neither says
 * anything about the content.</li>
 * <li><b>Thinking continuity tokens.</b> They are opaque provider bookkeeping, and they are
 * written into the log <em>after</em> the fact:
 * {@code ConversationLogListener#backfillContinuityTokens} patches them in when the turn
 * completes, which is after a proactive compaction in that same turn has already recorded
 * its sidecar. Hashing them would invalidate every mid-turn compaction one event later.</li>
 * </ul>
 *
 * <p>
 * Everything else a replayed message carries <em>is</em> hashed, including tool call ids -
 * those are the part ids from the canonical log, they survive storage unchanged, and
 * call-to-result pairing depends on them.
 * </p>
 *
 * <h2>Versioning</h2>
 *
 * <p>
 * {@link #VERSION} is both stored alongside the fingerprint and mixed into the hash. Change
 * it whenever the projection below changes: sidecars written by an older version then fail
 * to match once, are dropped, and are replaced by the next compaction. There is nothing to
 * migrate - a sidecar is a cache over a canonical log that is still complete.
 * </p>
 */
public final class CompactionFingerprint {

	/**
	 * Version of the projection and hash seed below.
	 *
	 * @see CompactionSidecar#fingerprintVersion()
	 */
	public static final int VERSION = 1;

	private static final String SEED = "tl-ai/compaction-prefix/v" + VERSION;

	private final MessageDigest _digest;

	private CompactionFingerprint() {
		try {
			_digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is required by every Java platform.", ex);
		}
	}

	/**
	 * The fingerprint of the given message history, as a lower-case hex digest.
	 */
	public static String of(List<Message> messages) {
		CompactionFingerprint fingerprint = new CompactionFingerprint();
		fingerprint.field(SEED);
		for (Message message : messages) {
			fingerprint.message(message);
		}
		return HexFormat.of().formatHex(fingerprint._digest.digest());
	}

	private void message(Message message) {
		if (message instanceof UserMessage user) {
			field("user");
			content(user.content());
		} else if (message instanceof AssistantMessage assistant) {
			field("assistant");
			content(assistant.content());
		} else if (message instanceof ToolResultMessage result) {
			field("tool_result");
			field(result.toolCallId());
			field(result.toolName());
			state(result.state());
		} else if (message instanceof SystemMessage system) {
			field("system");
			field(system.content());
		} else if (message instanceof CompactionMarker marker) {
			field("compaction_marker");
			field(Integer.toString(marker.messagesSummarized()));
			field(Integer.toString(marker.tokensBefore()));
			field(marker.trigger());
		}
	}

	private void content(List<ContentPart> parts) {
		field(Integer.toString(parts.size()));
		for (ContentPart part : parts) {
			if (part instanceof TextContent text) {
				field("text");
				field(text.text());
			} else if (part instanceof ThinkingContent thinking) {
				// The continuity token is deliberately not hashed, see the class comment.
				field("thinking");
				field(thinking.text());
			} else if (part instanceof ToolCall call) {
				field("tool_call");
				field(call.id());
				field(call.name());
				field(call.arguments());
			} else if (part instanceof ImageContent image) {
				field("image");
				field(image.mediaType());
				field(image.url());
			} else if (part instanceof PdfContent pdf) {
				field("pdf");
				field(pdf.mediaType());
				field(pdf.data());
			}
		}
	}

	private void state(ToolState state) {
		if (state instanceof ToolState.Completed completed) {
			field("completed");
			field(completed.result());
			field(Boolean.toString(completed.compacted()));
			field(completed.summary());
			field(Integer.toString(completed.truncationHint()));
			content(completed.content());
		} else if (state instanceof ToolState.Failed failed) {
			field("failed");
			field(failed.error());
			field(Boolean.toString(failed.synthetic()));
		} else if (state instanceof ToolState.Pending pending) {
			field("pending");
			field(pending.arguments());
		} else if (state instanceof ToolState.Running running) {
			field("running");
			field(running.arguments());
		}
	}

	/**
	 * Feeds one value into the digest, length-prefixed so that no regrouping of adjacent
	 * values can produce the same byte stream.
	 *
	 * @param value
	 *        The value, or <code>null</code> for an absent one - which hashes differently
	 *        from the empty string.
	 */
	private void field(String value) {
		if (value == null) {
			_digest.update(new byte[] { (byte) 0xFF });
			return;
		}
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		_digest.update(new byte[] {
			(byte) (bytes.length >>> 24), (byte) (bytes.length >>> 16),
			(byte) (bytes.length >>> 8), (byte) bytes.length });
		_digest.update(bytes);
	}
}
