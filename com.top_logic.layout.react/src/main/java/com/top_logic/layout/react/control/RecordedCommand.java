/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import java.util.Map;

/**
 * How a control wants a just-dispatched command represented in a recording.
 *
 * <p>
 * Returned by {@link ReactControl#recordCommand(String, Map)}. By default the {@link ReactCommand}
 * item mirrors the dispatched command verbatim; a control whose live arguments are session-bound
 * (e.g. allocated option ids) returns a replay-stable form — possibly a different command and
 * business-key arguments — so a recorded step resolves in a later session.
 * </p>
 *
 * @param command
 *        The typed command item to record (never {@code null}; its
 *        {@link ReactCommand#getAddress() address} is filled in by the recording servlet).
 * @param coalescing
 *        Whether a run of consecutive recordings of this same command on the same target should
 *        collapse to a single step holding the latest arguments (e.g. per-keystroke field input is
 *        recorded as one value, like the legacy {@code FormInput}). The recorder merges purely by the
 *        step's {@link ReactCommand#getAddress() address} and {@link ReactCommand#getName() name}, so
 *        no control-type knowledge leaks into the recording loop.
 */
public record RecordedCommand(ReactCommand command, boolean coalescing) {

	/**
	 * A non-coalescing recorded command — each occurrence is a distinct step.
	 *
	 * @param command
	 *        The typed command item to record.
	 */
	public RecordedCommand(ReactCommand command) {
		this(command, false);
	}
}
