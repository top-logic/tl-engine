/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import java.time.Instant;
import java.util.Objects;

/**
 * Lifecycle state of a tool execution.
 *
 * <p>Tools transition through: {@link Pending} &#x2192; {@link Running} &#x2192;
 * {@link Completed} or {@link Failed}. The agent loop uses these states for
 * error recovery (detecting interrupted tools) and compaction (clearing old results).</p>
 *
 * @see ToolResultMessage
 */
public sealed interface ToolState
		permits ToolState.Pending, ToolState.Running, ToolState.Completed, ToolState.Failed {

	/**
	 * Tool call requested by LLM but not yet started.
	 *
	 * @param arguments raw JSON arguments from the LLM
	 */
	record Pending(String arguments) implements ToolState {
		public Pending {
			Objects.requireNonNull(arguments, "Arguments must not be null.");
		}
	}

	/**
	 * Tool execution in progress.
	 *
	 * @param arguments raw JSON arguments from the LLM
	 * @param startTime when execution started
	 */
	record Running(String arguments, Instant startTime) implements ToolState {
		public Running {
			Objects.requireNonNull(arguments, "Arguments must not be null.");
			Objects.requireNonNull(startTime, "Start time must not be null.");
		}
	}

	/**
	 * Tool execution completed successfully.
	 *
	 * @param result         the tool's output (string)
	 * @param startTime      when execution started
	 * @param endTime        when execution completed
	 * @param compacted      true if the result was cleared during context compaction
	 * @param summary        optional compact summary for compaction, or {@code null}
	 * @param truncationHint per-tool max chars before truncation, or {@code -1} for global default
	 */
	record Completed(String result, Instant startTime, Instant endTime,
			boolean compacted, String summary, int truncationHint) implements ToolState {

		/**
		 * Convenience constructor for call sites that do not supply summary or truncation hint.
		 */
		public Completed(String result, Instant startTime, Instant endTime, boolean compacted) {
			this(result, startTime, endTime, compacted, null, -1);
		}

		/** */
		public Completed {
			Objects.requireNonNull(result, "Result must not be null.");
			Objects.requireNonNull(startTime, "Start time must not be null.");
			Objects.requireNonNull(endTime, "End time must not be null.");
		}
	}

	/**
	 * Tool execution failed.
	 *
	 * @param error     error message
	 * @param startTime when execution started
	 * @param endTime   when the failure was recorded
	 * @param synthetic {@code true} if the tool's body never ran (e.g. unresolvable arguments,
	 *                  unknown tool) — such a failure cannot have produced side effects, so it
	 *                  must not by itself trigger an end-of-turn transaction rollback
	 */
	record Failed(String error, Instant startTime, Instant endTime, boolean synthetic) implements ToolState {
		public Failed {
			Objects.requireNonNull(error, "Error must not be null.");
			Objects.requireNonNull(startTime, "Start time must not be null.");
			Objects.requireNonNull(endTime, "End time must not be null.");
		}

		/**
		 * Convenience constructor for a genuine execution failure — the tool ran (or may have
		 * run) and could have produced side effects.
		 */
		public Failed(String error, Instant startTime, Instant endTime) {
			this(error, startTime, endTime, false);
		}
	}
}
