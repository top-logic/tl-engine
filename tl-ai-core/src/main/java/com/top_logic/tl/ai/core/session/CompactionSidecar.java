/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

import java.util.List;
import java.util.Objects;

import com.top_logic.tl.ai.core.message.Message;

/**
 * What a compaction put in place of the history it replaced, recorded on the
 * {@link Part.CompactionPart} so that a reloaded session comes back compacted.
 *
 * <p>
 * Without it, {@link ConversationLogReplay} rebuilds a session from the full canonical log,
 * which is the size the compaction was run to escape - a session evicted from the chat's
 * session cache and reopened would arrive back at its original size, having thrown away
 * every compaction it ever did.
 * </p>
 *
 * <p>
 * <b>This is a cache, not a source of truth.</b> The canonical log keeps every part, and the
 * sidecar is used only while {@link #fingerprint()} still matches the history it was written
 * for - see {@link CompactionFingerprint}. When it does not, or when the recorded
 * {@link #fingerprintVersion()} is not
 * {@link CompactionFingerprint#VERSION the reader's own}, replay falls back to rebuilding
 * the full history and the session is merely large, never wrong.
 * </p>
 *
 * @param fingerprintVersion
 *        The {@link CompactionFingerprint#VERSION} that {@link #fingerprint()} was computed
 *        with.
 * @param fingerprint
 *        {@link CompactionFingerprint} of the message history this compaction replaced.
 * @param replacementMessages
 *        The messages the compaction produced, i.e. what the conversation held immediately
 *        afterwards.
 */
public record CompactionSidecar(int fingerprintVersion, String fingerprint,
		List<Message> replacementMessages) {

	/** */
	public CompactionSidecar {
		Objects.requireNonNull(fingerprint, "Fingerprint must not be null.");
		Objects.requireNonNull(replacementMessages, "Replacement messages must not be null.");
		replacementMessages = List.copyOf(replacementMessages);
	}

	/**
	 * Creates a sidecar fingerprinted with the current {@link CompactionFingerprint#VERSION}.
	 *
	 * @param replacedHistory
	 *        The messages the compaction replaced, i.e. what the conversation held immediately
	 *        before it ran.
	 * @param replacementMessages
	 *        What it put in their place.
	 */
	public static CompactionSidecar of(List<Message> replacedHistory,
			List<Message> replacementMessages) {
		return new CompactionSidecar(CompactionFingerprint.VERSION,
			CompactionFingerprint.of(replacedHistory), replacementMessages);
	}

	/**
	 * Whether this sidecar may be used to shortcut the replay of the given history.
	 *
	 * @param replayedHistory
	 *        The message history rebuilt from the canonical log up to the compaction that
	 *        carries this sidecar.
	 */
	public boolean matches(List<Message> replayedHistory) {
		return fingerprintVersion == CompactionFingerprint.VERSION
			&& fingerprint.equals(CompactionFingerprint.of(replayedHistory));
	}
}
