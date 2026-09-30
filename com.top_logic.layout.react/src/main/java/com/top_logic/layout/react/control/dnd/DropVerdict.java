/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

import java.util.Objects;

import com.top_logic.basic.util.ResKey;

/**
 * Whether a {@link DropTarget} accepts a concrete {@link DropEvent drop}, and if not, why.
 *
 * <p>
 * A verdict is either {@link #ACCEPTED} or {@link #refused(ResKey) refused} with a reason the user
 * is shown: while the drag still hovers the target, and as the error of a drop made anyway.
 * </p>
 *
 * @see DropTarget#check(DropEvent)
 */
public final class DropVerdict {

	/** The verdict accepting a drop. */
	public static final DropVerdict ACCEPTED = new DropVerdict(null);

	private final ResKey _reason;

	private DropVerdict(ResKey reason) {
		_reason = reason;
	}

	/**
	 * The verdict refusing a drop for the given reason.
	 *
	 * @param reason
	 *        Why the drop is impossible, phrased for the user.
	 */
	public static DropVerdict refused(ResKey reason) {
		return new DropVerdict(Objects.requireNonNull(reason, "A refusal needs a reason."));
	}

	/**
	 * Whether the drop is accepted.
	 */
	public boolean isAccepted() {
		return _reason == null;
	}

	/**
	 * Why the drop is refused, {@code null} when it is {@link #isAccepted() accepted}.
	 */
	public ResKey reason() {
		return _reason;
	}

	@Override
	public String toString() {
		return isAccepted() ? "accepted" : "refused(" + _reason + ")";
	}

}
