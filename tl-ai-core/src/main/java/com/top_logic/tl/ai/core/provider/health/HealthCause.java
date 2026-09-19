/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.health;

/**
 * Why a model is marked unhealthy, and how strong that statement is.
 *
 * <p>
 * The order of the constants is the strength order: a mark replaces a live one only when its cause
 * is at least as strong. That is what keeps a transient blip from clearing a quota lockout that is
 * still in force - see {@link ModelAvailability#reportFailure}.
 * </p>
 */
public enum HealthCause {

	/**
	 * One call failed for a reason nothing recognised.
	 *
	 * <p>
	 * The weakest statement: the model stays usable once per turn, so a single bad connection
	 * neither routes around a healthy model nor hides a fault that repeats.
	 * </p>
	 */
	TRANSIENT,

	/**
	 * The model is there but has no room right now - rate limited or overloaded.
	 *
	 * <p>
	 * Heals by itself within seconds, so the mark carries a short TTL rather than waiting for an
	 * administrator.
	 * </p>
	 */
	CAPACITY,

	/**
	 * The account cannot use this model at all - billing exhausted, or the key rejected.
	 *
	 * <p>
	 * Nothing this application does makes that go away, so the mark is sticky: it holds until the
	 * configured long TTL runs out or an administrator resets it.
	 * </p>
	 */
	QUOTA;

	/**
	 * Whether a mark with this cause may replace a live mark with the given cause.
	 *
	 * <p>
	 * Equal causes replace, which refreshes the TTL of a fault that is still happening.
	 * </p>
	 */
	public boolean overrides(HealthCause live) {
		return live == null || ordinal() >= live.ordinal();
	}
}
