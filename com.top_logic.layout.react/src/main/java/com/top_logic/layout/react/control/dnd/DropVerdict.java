/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.dnd;

import java.util.Objects;

import com.top_logic.basic.util.ResKey;

/**
 * Whether a {@link DropTarget} accepts a concrete {@link DropRequest drop}, where, and if not, why.
 *
 * <p>
 * A verdict is either {@link #accepted(DropLocation) accepted} at the location of the operation
 * that accepts the drop, or {@link #refused(ResKey) refused} with a reason the user is shown: while
 * the drag still hovers the target, and as the error of a drop made anyway. The location of an
 * accepting verdict is where the drop is applied, and what the control displaying the target draws
 * its {@link DropMarker marker} for.
 * </p>
 *
 * @see DropTarget#check(DropRequest)
 */
public final class DropVerdict {

	private final DropLocation _location;

	private final ResKey _reason;

	private DropVerdict(DropLocation location, ResKey reason) {
		_location = location;
		_reason = reason;
	}

	/**
	 * The verdict accepting a drop at the given location.
	 *
	 * @param location
	 *        The location the accepting operation applies the drop at, one the
	 *        {@link DropRequest#location(DropMode) request} offered.
	 */
	public static DropVerdict accepted(DropLocation location) {
		return new DropVerdict(Objects.requireNonNull(location, "An accepted drop needs a location."), null);
	}

	/**
	 * The verdict refusing a drop for the given reason.
	 *
	 * @param reason
	 *        Why the drop is impossible, phrased for the user.
	 */
	public static DropVerdict refused(ResKey reason) {
		return new DropVerdict(null, Objects.requireNonNull(reason, "A refusal needs a reason."));
	}

	/**
	 * Whether the drop is accepted.
	 */
	public boolean isAccepted() {
		return _reason == null;
	}

	/**
	 * Where the drop is applied, {@code null} when it is refused.
	 */
	public DropLocation location() {
		return _location;
	}

	/**
	 * Why the drop is refused, {@code null} when it is {@link #isAccepted() accepted}.
	 */
	public ResKey reason() {
		return _reason;
	}

	@Override
	public String toString() {
		return isAccepted() ? "accepted(" + _location + ")" : "refused(" + _reason + ")";
	}

}
