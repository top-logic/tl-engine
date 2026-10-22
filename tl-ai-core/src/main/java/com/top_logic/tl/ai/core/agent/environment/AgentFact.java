/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import com.top_logic.basic.Logger;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * A fact an agent definition states, as TL-Script that is evaluated when a run of that agent starts.
 *
 * <p>
 * These are the facts that need no page: they are computed in the acting user's context and are
 * therefore the same whether the run comes from the chat, from {@code aiAgentChat} or from the
 * scheduler. Facts that need what a page is showing are declared on {@code <chat-conversation>}
 * instead and travel with the send call.
 * </p>
 *
 * <p>
 * The expression is compiled on first use rather than when the definition is read: definitions are
 * read while the services start, and the model the expression refers to is not up yet at that
 * point.
 * </p>
 *
 * @see EnvironmentFact
 */
public final class AgentFact {

	private final String _name;

	private final boolean _stable;

	private final Expr _value;

	private volatile QueryExecutor _compiled;

	/**
	 * Creates an {@link AgentFact}.
	 *
	 * @param name
	 *        What the fact is called in front of the model.
	 * @param stable
	 *        Whether the value holds for the whole conversation.
	 * @param value
	 *        TL-Script of no arguments computing the value.
	 */
	public AgentFact(String name, boolean stable, Expr value) {
		_name = name;
		_stable = stable;
		_value = value;
	}

	/** What the fact is called in front of the model. */
	public String name() {
		return _name;
	}

	/** Whether the value holds for the whole conversation. */
	public boolean stable() {
		return _stable;
	}

	/**
	 * Evaluates the expression, or {@code null} when it cannot be evaluated.
	 *
	 * <p>
	 * A broken fact is dropped with a log line rather than failing the run: the user asked a
	 * question, and losing one line of context is a better answer to a mistyped expression than no
	 * answer at all.
	 * </p>
	 */
	public EnvironmentFact evaluate() {
		try {
			QueryExecutor executor = _compiled;
			if (executor == null) {
				executor = QueryExecutor.compile(_value);
				_compiled = executor;
			}
			Object result = executor.execute();
			return new EnvironmentFact(_name, label(result), _stable);
		} catch (RuntimeException ex) {
			Logger.warn("Agent fact '" + _name + "' could not be evaluated and is left out of the"
				+ " environment block: " + ex.getMessage(), ex, AgentFact.class);
			return null;
		}
	}

	private static String label(Object value) {
		if (value == null) {
			return "";
		}
		if (value instanceof String text) {
			return text;
		}
		return MetaLabelProvider.INSTANCE.getLabel(value);
	}
}
