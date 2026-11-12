/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import java.time.Instant;

import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.util.Resources;

/**
 * What an exception that ended an agent run is reported as: the {@link AgentEvent.ProviderError}
 * every listener of the run sees, with the kind saying whose failure it was and the message the user
 * is told.
 *
 * @see RunRecording#fail(Throwable)
 */
public final class RunFailure {

	/**
	 * The {@link AgentEvent.ProviderError#kind()} of a failure of this application rather than of
	 * the model's provider.
	 *
	 * <p>
	 * Every other value of that field is a {@link ProviderException.Kind}, and a client is entitled
	 * to read it as one: {@code RATE_LIMITED} means wait, {@code CONTEXT_WINDOW_EXCEEDED} means start
	 * a new chat. This value says the opposite - the model did nothing wrong and repeating the
	 * message will not help - and exists so that the two are distinguishable at all, which
	 * {@code UNKNOWN} for everything made impossible (issue #410). The chat UI matches on the
	 * literal, so it is part of the wire contract of the chat's {@code provider_error} frame.
	 * </p>
	 */
	public static final String APPLICATION_ERROR = "APPLICATION_ERROR";

	private RunFailure() {
		// Static utility.
	}

	/**
	 * The {@link ProviderException} behind a failed run, or <code>null</code> if the run did not
	 * fail in the provider.
	 *
	 * <p>
	 * Looked for one level down as well, because the loop's own wrapping puts it there; deeper than
	 * that a provider failure is no longer what the run died of.
	 * </p>
	 */
	public static ProviderException providerFailure(Throwable ex) {
		if (ex instanceof ProviderException pe) {
			return pe;
		}
		return ex.getCause() instanceof ProviderException pe ? pe : null;
	}

	/**
	 * Whose failure ended the run: the {@link ProviderException.Kind} of a provider failure,
	 * otherwise {@link #APPLICATION_ERROR}.
	 */
	public static String kind(Throwable ex) {
		ProviderException provider = providerFailure(ex);
		return provider != null ? provider.kind().name() : APPLICATION_ERROR;
	}

	/**
	 * The message for the user a failed run carries, or <code>null</code> if it has none.
	 *
	 * <p>
	 * The {@link ProviderException#errorKey()} of a provider failure, otherwise the
	 * {@link I18NFailure#getErrorKey()} of a failure that has a message for the user - such as the
	 * {@link com.top_logic.util.error.TopLogicException} naming the secrets a run needs when no
	 * provider is available to choose its model - looked for one level down as well, like
	 * {@link #providerFailure(Throwable)}.
	 * </p>
	 */
	public static ResKey messageKey(Throwable ex) {
		ProviderException provider = providerFailure(ex);
		if (provider != null) {
			return provider.errorKey();
		}
		I18NFailure failure = ex instanceof I18NFailure own ? own
			: ex.getCause() instanceof I18NFailure cause ? cause : null;
		return failure != null ? failure.getErrorKey() : null;
	}

	/**
	 * What the user is told a failed run died of, in the language of the current thread.
	 *
	 * <p>
	 * The text of {@link #messageKey(Throwable)}; where there is none, or its text is empty, the
	 * provider's own words for a provider failure, otherwise the exception's message.
	 * </p>
	 *
	 * <p>
	 * A run whose model has a provider without its credentials does not fail with an exception: it
	 * ends with a provider error of the kind {@link ProviderException.Kind#NO_CREDENTIALS}, whose
	 * message names the secret to provide.
	 * </p>
	 *
	 * @return The message; <code>null</code> if the exception has none.
	 */
	public static String message(Throwable ex) {
		ProviderException provider = providerFailure(ex);
		String words = provider != null ? provider.getMessage() : ex.getMessage();
		return userMessage(words, messageKey(ex));
	}

	/**
	 * The text of the given key in the language of the current thread, or the given words where
	 * there is no key or its text is empty.
	 */
	public static String userMessage(String words, ResKey key) {
		if (key != null) {
			String text = Resources.getInstance().getString(key);
			if (text != null && !text.isBlank()) {
				return text;
			}
		}
		return words;
	}

	/**
	 * The failure as the {@link AgentEvent.ProviderError} of the run it ended.
	 *
	 * @param runId
	 *        The run's id.
	 * @param turnNumber
	 *        The turn the run was in.
	 * @param ex
	 *        What ended the run.
	 * @return The event, with {@link #kind(Throwable)}, {@link #message(Throwable)} - the class
	 *         name where the exception has no message - and {@link #messageKey(Throwable)}.
	 */
	public static AgentEvent.ProviderError event(String runId, int turnNumber, Throwable ex) {
		String message = message(ex);
		return new AgentEvent.ProviderError(runId, turnNumber, Instant.now(), kind(ex),
			message != null ? message : ex.getClass().getSimpleName(), messageKey(ex));
	}
}
