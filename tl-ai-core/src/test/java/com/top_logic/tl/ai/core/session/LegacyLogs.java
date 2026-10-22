/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

/**
 * Turns a conversation log into the shape logs had before this version wrote them.
 */
public final class LegacyLogs {

	private LegacyLogs() {
		// Static utility
	}

	/**
	 * Removes every {@link CompactionSidecar} from the given log, in place.
	 *
	 * <p>
	 * This is what every persisted log looked like before issue #341, so it is both the
	 * backward-compatibility case and the control that shows a test would fail without the
	 * fix.
	 * </p>
	 *
	 * @return The same log, for chaining.
	 */
	public static ConversationLog withoutSidecars(ConversationLog log) {
		for (Turn turn : log.turns()) {
			for (Part part : turn.assistantParts()) {
				if (part instanceof Part.CompactionPart cp) {
					turn.updatePart(cp.id(), new Part.CompactionPart(cp.id(), cp.timestamp(),
						cp.tokensBefore(), cp.tokensAfter(), cp.trigger(), null));
				}
			}
		}
		return log;
	}
}
