/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.health;

import java.time.Instant;

/**
 * What {@link ModelAvailability} currently holds against one model.
 *
 * @param model
 *        The model name the mark is about.
 * @param terminal
 *        <code>true</code> when the model is not to be called at all while the mark lives;
 *        <code>false</code> for a mark that still allows one attempt per turn.
 * @param cause
 *        Why the model was marked.
 * @param kind
 *        The name of the {@link com.top_logic.tl.ai.core.provider.ProviderException.Kind} that
 *        produced the mark, for an operator reading the run.
 * @param detail
 *        The provider's own words, or <code>null</code>.
 * @param markedAt
 *        When the mark was last written or refreshed.
 * @param expiresAt
 *        When the mark stops holding, or <code>null</code> for a mark that holds until an
 *        administrator resets it.
 * @param consumedTurn
 *        The turn token that already used up this mark's one attempt, or <code>null</code>.
 *        Meaningless for a terminal mark.
 */
public record ModelHealthMark(
		String model,
		boolean terminal,
		HealthCause cause,
		String kind,
		String detail,
		Instant markedAt,
		Instant expiresAt,
		String consumedTurn) {

	/** Whether this mark still holds at the given moment. */
	public boolean live(Instant now) {
		return expiresAt == null || now.isBefore(expiresAt);
	}

	/** A one-line reason for a transcript or a log line. */
	public String reason() {
		StringBuilder result = new StringBuilder();
		result.append(cause.name().toLowerCase()).append(" (").append(kind).append(")");
		if (detail != null && !detail.isBlank()) {
			result.append(": ").append(detail);
		}
		return result.toString();
	}

	/** This mark with its one per-turn attempt used up by the given turn. */
	ModelHealthMark consumedBy(String turnToken) {
		return new ModelHealthMark(model, terminal, cause, kind, detail, markedAt, expiresAt, turnToken);
	}
}
