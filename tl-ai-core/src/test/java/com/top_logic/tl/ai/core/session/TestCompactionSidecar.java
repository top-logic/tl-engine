/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.cost.Money;
import com.top_logic.tl.ai.core.memory.ByteBasedTokenEstimator;
import com.top_logic.tl.ai.core.memory.CompactionResult;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.CompactionMarker;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.SystemMessage;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.session.persist.ConversationLogSerializer;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Tests that a compacted session survives storage: {@link CompactionSidecar},
 * {@link CompactionFingerprint} and the replay that consumes them.
 */
public class TestCompactionSidecar extends TestCase {

	private static final Instant T0 = Instant.parse("2026-04-04T12:00:00Z");

	private static final String RUN = "run-1";

	/** A tool result big enough that its presence is unmistakable in a token count. */
	private static final String FAT_RESULT = "x".repeat(4000);

	private ConversationLog _log;

	private ConversationLogListener _listener;

	@Override
	protected void setUp() {
		_log = new ConversationLog();
		_listener = new ConversationLogListener(_log);
	}

	/**
	 * A session that compacted, was persisted and reloaded comes back at its compacted size.
	 */
	public void testReloadedSessionComesBackCompacted() {
		List<Message> replacement = recordTurnEndingInCompaction();
		finishRun();

		ConversationLog restored = roundTrip(_log);
		List<Message> reloaded = replay(restored);

		assertEquals("The reloaded session must hold what the compaction produced, "
			+ "not the history it replaced.",
			texts(replacement), texts(reloaded));

		List<Message> full = replay(LegacyLogs.withoutSidecars(roundTrip(_log)));
		int compactedTokens = new ByteBasedTokenEstimator().estimateTokens(reloaded);
		int fullTokens = new ByteBasedTokenEstimator().estimateTokens(full);
		assertTrue("Reloading must not restore the token cost the compaction freed: "
			+ compactedTokens + " vs. " + fullTokens + " without the sidecar.",
			compactedTokens * 4 < fullTokens);
	}

	/**
	 * A summary the framework wrote comes back from storage as the framework's, not as something the
	 * user typed.
	 */
	public void testTheSummaryStaysTheFrameworks() {
		recordTurnEndingInCompaction();
		finishRun();

		List<Message> reloaded = replay(roundTrip(_log));

		UserMessage summary = (UserMessage) reloaded.get(1);
		assertEquals(UserMessage.Origin.FRAMEWORK, summary.origin());
	}

	/**
	 * The fingerprint survives a full serialize/deserialize round trip unchanged.
	 *
	 * <p>
	 * This is the trap the same design fell into in cline (issue 12747): their fingerprint
	 * covered message ids and timestamps, which the codec regenerates, so every reload
	 * rejected the sidecar as stale and re-compacted the session on its very next turn. Here
	 * the ids and timestamps of a replayed message are minted fresh by
	 * {@link ConversationLogReplay} on every single reload, so a fingerprint that covered
	 * them would not even survive two replays of the <em>same</em> log.
	 * </p>
	 */
	public void testFingerprintSurvivesStorageRoundTrip() {
		recordTurnEndingInCompaction();
		finishRun();
		CompactionSidecar written = sidecarOf(_log);

		ConversationLog restored = roundTrip(_log);
		CompactionSidecar read = sidecarOf(restored);

		assertEquals("The stored fingerprint must come back unchanged.",
			written.fingerprint(), read.fingerprint());
		assertEquals(CompactionFingerprint.VERSION, read.fingerprintVersion());

		List<Message> history = ConversationLogReplay.replayedHistoryBefore(restored,
			compactionPartOf(restored).id());
		assertTrue("A sidecar read back from storage must still match the history it covers.",
			read.matches(history));

		assertEquals("Two replays of the same log must fingerprint identically - the ids and "
			+ "timestamps replay mints per message must not reach the fingerprint.",
			CompactionFingerprint.of(history),
			CompactionFingerprint.of(ConversationLogReplay.replayedHistoryBefore(restored,
				compactionPartOf(restored).id())));
	}

	/**
	 * A session whose canonical history has changed since the sidecar was written falls back
	 * to a full replay, rather than serving a summary of a history that no longer exists.
	 */
	public void testChangedHistoryFallsBackToFullReplay() {
		recordTurnEndingInCompaction();
		finishRun();
		ConversationLog restored = roundTrip(_log);

		// The rollback of a turn rewrites the tool calls it recorded, which is exactly the
		// case where the sidecar's summary describes writes that never happened.
		Turn turn = restored.turns().get(0);
		Part.ToolCallPart call = (Part.ToolCallPart) turn.assistantParts().stream()
			.filter(Part.ToolCallPart.class::isInstance).findFirst().orElseThrow();
		turn.updatePart(call.id(), call.rolledBack("Database operation failed."));

		assertFullReplay("A stale sidecar must be ignored, not applied to a history it no "
			+ "longer describes.", restored);
		assertTrue("The fallback must be the full canonical history, retraction included.",
			texts(replay(restored)).stream().anyMatch(t -> t.contains("rolled back")));
	}

	/** A sidecar written under an older hash-seed version is rejected, not migrated. */
	public void testOlderFingerprintVersionIsRejected() {
		recordTurnEndingInCompaction();
		finishRun();
		ConversationLog restored = roundTrip(_log);
		Part.CompactionPart part = compactionPartOf(restored);
		CompactionSidecar current = part.sidecar();

		// Same fingerprint, older seed version: the projection behind it is not this
		// reader's, so its value carries no information and must not be trusted.
		Turn turn = restored.turns().get(0);
		turn.updatePart(part.id(), part.completed(part.tokensAfter(),
			new CompactionSidecar(CompactionFingerprint.VERSION - 1, current.fingerprint(),
				current.replacementMessages())));

		assertFullReplay("A sidecar from an older fingerprint version must fail projection "
			+ "once and be replaced, never migrated.", restored);
	}

	/** A compaction recorded before the sidecar existed replays the full history. */
	public void testCompactionWithoutSidecarFallsBackToFullReplay() {
		recordTurnEndingInCompaction();
		finishRun();
		ConversationLog restored = LegacyLogs.withoutSidecars(roundTrip(_log));

		assertEquals("Without a sidecar there is nothing to shortcut with: the fat tool "
			+ "result the compaction dropped is back in the history.",
			List.of("user:Summarize the report.",
				"assistant:Reading the report.[call readReport {\"id\":7}]",
				"tool_result:readReport:" + FAT_RESULT),
			texts(replay(restored)));
	}

	/**
	 * The next compaction chains onto the previous one's sidecar rather than fighting it.
	 *
	 * <p>
	 * The second compaction runs on a conversation that is already the first one's output, so
	 * its fingerprint covers the first sidecar's messages plus what came after. Both have to
	 * survive together, or a twice-compacted session would fall back to full replay.
	 * </p>
	 */
	public void testSecondCompactionChainsOntoTheFirst() {
		recordTurnEndingInCompaction();
		_listener.onEvent(new AgentEvent.TurnStarted(RUN, 2, T0));
		_listener.onEvent(new AgentEvent.TextDelta(RUN, 2, T0, "Still working."));
		List<Message> secondReplacement = List.of(
			new CompactionMarker("m2", T0, 3, 900, "proactive"),
			new UserMessage("s2", T0, List.of(new TextContent("Second summary."))));
		compact(2, 900, 120, secondReplacement);
		finishRun();

		assertEquals(texts(secondReplacement), texts(replay(roundTrip(_log))));
	}

	/** An empty history fingerprints without blowing up, and differs from a non-empty one. */
	public void testEmptyHistoryFingerprint() {
		String empty = CompactionFingerprint.of(List.of());
		assertNotNull(empty);
		assertFalse(empty.equals(CompactionFingerprint.of(
			List.of(new UserMessage("u1", T0, List.of(new TextContent("Hi")))))));
	}

	/**
	 * Records one turn that reads a fat tool result and then compacts, driving the listener
	 * with exactly the events the agent loop emits in that order.
	 *
	 * @return The messages the compaction produced.
	 */
	private List<Message> recordTurnEndingInCompaction() {
		Turn turn = _log.startTurn();
		turn.setUserParts(List.of(new Part.TextPart("u1", T0, "Summarize the report.")));

		_listener.onEvent(new AgentEvent.RunStarted(RUN, 0, T0, "test-model"));
		_listener.onEvent(new AgentEvent.TurnStarted(RUN, 1, T0));
		_listener.onEvent(new AgentEvent.TextDelta(RUN, 1, T0, "Reading the report."));
		_listener.onEvent(new AgentEvent.ToolCallStarted(RUN, 1, T0, "tc1", "readReport",
			"{\"id\":7}", ToolMode.READ));
		_listener.onEvent(new AgentEvent.ToolCallCompleted(RUN, 1, T0, "tc1", "readReport",
			FAT_RESULT, Duration.ofMillis(12)));

		List<Message> replacement = List.of(
			new CompactionMarker("m1", T0, 4, 1200, "proactive"),
			new UserMessage("s1", T0, List.of(new TextContent("The report was read.")),
				UserMessage.Origin.FRAMEWORK),
			new AssistantMessage("a1", T0, List.of(new TextContent("Ready.")),
				TokenUsage.EMPTY, "stop"));
		compact(1, 1200, 150, replacement);

		_listener.onEvent(new AgentEvent.TurnCompleted(RUN, 1, T0,
			new TokenUsage(1200, 40, 0, 0, 0), List.of()));
		return replacement;
	}

	private void finishRun() {
		_listener.onEvent(new AgentEvent.RunCompleted(RUN, 1, T0, StopReason.COMPLETE,
			new TokenUsage(1200, 40, 0, 0, 0), Money.zero(Money.USD)));
	}

	private static List<Message> replay(ConversationLog log) {
		Conversation conversation = new Conversation();
		ConversationLogReplay.rebuildMemory(log, conversation);
		return conversation.messages();
	}

	/** Asserts that the log replays to exactly what it replays to with no sidecar at all. */
	private static void assertFullReplay(String message, ConversationLog log) {
		assertEquals(message, texts(replay(LegacyLogs.withoutSidecars(roundTrip(log)))),
			texts(replay(log)));
	}

	private void compact(int turnNumber, int tokensBefore, int tokensAfter,
			List<Message> replacement) {
		_listener.onEvent(new AgentEvent.CompactionStarted(RUN, turnNumber, T0, tokensBefore,
			"proactive"));
		_listener.onEvent(new AgentEvent.CompactionCompleted(RUN, turnNumber, T0,
			new CompactionResult(replacement, 4, tokensBefore, tokensAfter, "proactive")));
	}

	private static ConversationLog roundTrip(ConversationLog log) {
		return ConversationLogSerializer.fromJson(ConversationLogSerializer.toJson(log));
	}


	private static Part.CompactionPart compactionPartOf(ConversationLog log) {
		for (Turn turn : log.turns()) {
			for (Part part : turn.assistantParts()) {
				if (part instanceof Part.CompactionPart cp) {
					return cp;
				}
			}
		}
		throw new AssertionError("No compaction recorded.");
	}

	private static CompactionSidecar sidecarOf(ConversationLog log) {
		CompactionSidecar sidecar = compactionPartOf(log).sidecar();
		assertNotNull("The compaction must have recorded a sidecar.", sidecar);
		return sidecar;
	}

	/**
	 * The role and text of each message - what survives a replay, unlike the ids and
	 * timestamps replay regenerates.
	 */
	private static List<String> texts(List<Message> messages) {
		List<String> texts = new ArrayList<>();
		for (Message message : messages) {
			texts.add(textOf(message));
		}
		return texts;
	}

	private static String textOf(Message message) {
		if (message instanceof UserMessage user) {
			return "user:" + contentText(user.content());
		}
		if (message instanceof AssistantMessage assistant) {
			return "assistant:" + contentText(assistant.content());
		}
		if (message instanceof ToolResultMessage result) {
			String text = result.state() instanceof ToolState.Completed done ? done.result()
				: result.state() instanceof ToolState.Failed failed ? failed.error() : "";
			return "tool_result:" + result.toolName() + ":" + text;
		}
		if (message instanceof SystemMessage system) {
			return "system:" + system.content();
		}
		if (message instanceof CompactionMarker marker) {
			return "marker:" + marker.messagesSummarized() + ":" + marker.trigger();
		}
		throw new AssertionError("Unhandled message: " + message);
	}

	private static String contentText(List<ContentPart> content) {
		StringBuilder text = new StringBuilder();
		for (ContentPart part : content) {
			if (part instanceof TextContent plain) {
				text.append(plain.text());
			} else if (part instanceof ThinkingContent thinking) {
				text.append("[thinking]").append(thinking.text());
			} else if (part instanceof ToolCall call) {
				text.append("[call ").append(call.name()).append(' ')
					.append(call.arguments()).append(']');
			}
		}
		return text.toString();
	}
}
