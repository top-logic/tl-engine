/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.InstantiationContextAdaptor;

/**
 * The names of the {@link ActionArguments} an action runs with, known when the action is
 * instantiated.
 *
 * <p>
 * The trigger of a chain declares the names it supplies, and an action with follow-up actions
 * {@link #with(String) adds} the name of its result for them. The scope is handed down while the
 * chain is instantiated: {@link ViewActions#instantiate(InstantiationContext, List, ActionScope)}
 * {@link #bind(InstantiationContext) binds} it to the context the actions are created in, and an
 * action looks up the scope of its position with {@link #of(InstantiationContext)} in its
 * constructor.
 * </p>
 *
 * <p>
 * The names and the default name of a scope are those of the arguments the actions in the scope run
 * with. A scope also knows whether its actions run inside a transaction, see
 * {@link #isTransactional()}.
 * </p>
 */
public final class ActionScope {

	/**
	 * The scope of actions run without any argument.
	 */
	public static final ActionScope NONE = new ActionScope(Collections.emptyList(), null, false);

	private final List<String> _names;

	private final String _defaultName;

	private final boolean _transactional;

	private ActionScope(List<String> names, String defaultName, boolean transactional) {
		_names = names;
		_defaultName = defaultName;
		_transactional = transactional;
	}

	/**
	 * The scope of a trigger supplying a single argument with the given name.
	 */
	public static ActionScope single(String name) {
		return NONE.with(name);
	}

	/**
	 * The scope of arguments with the given names.
	 *
	 * <p>
	 * A name occurring more than once is shadowed by its last occurrence, see
	 * {@link #with(String)}.
	 * </p>
	 *
	 * @param names
	 *        The names in the order of the arguments.
	 * @param defaultName
	 *        The name of the default argument, one of the given names; {@code null} only for no
	 *        names at all.
	 */
	public static ActionScope of(List<String> names, String defaultName) {
		List<String> unique = new ArrayList<>(names.size());
		for (String name : names) {
			unique.remove(name);
			unique.add(name);
		}
		if (defaultName != null && !unique.contains(defaultName)) {
			throw new IllegalArgumentException("Default '" + defaultName + "' is not among " + names + ".");
		}
		if (unique.isEmpty()) {
			return NONE;
		}
		return new ActionScope(Collections.unmodifiableList(unique), defaultName, false);
	}

	/**
	 * The scope of actions instantiated in the given context.
	 *
	 * @return The scope {@link #bind(InstantiationContext) bound} to the context, {@link #NONE} when
	 *         no scope is bound.
	 */
	public static ActionScope of(InstantiationContext context) {
		if (context instanceof ScopedContext scoped) {
			return scoped.scope();
		}
		return NONE;
	}

	/**
	 * A context instantiating actions in this scope.
	 *
	 * @param context
	 *        The context to create the actions in, and where problems are reported.
	 */
	public InstantiationContext bind(InstantiationContext context) {
		if (of(context) == this) {
			return context;
		}
		return new ScopedContext(context, this);
	}

	/**
	 * This scope extended by the given name, which becomes the default.
	 *
	 * <p>
	 * An equal name of this scope is shadowed, see {@link ActionArguments#with(String, Object)}.
	 * </p>
	 */
	public ActionScope with(String name) {
		List<String> names = new ArrayList<>(_names);
		names.remove(name);
		names.add(name);
		return new ActionScope(Collections.unmodifiableList(names), name, _transactional);
	}

	/**
	 * This scope for actions that run inside a transaction, e.g. the body of a
	 * {@link WithTransactionAction}.
	 *
	 * <p>
	 * The names stay the same, and every scope {@link #with(String) derived} from the result is
	 * transactional as well.
	 * </p>
	 *
	 * @see #isTransactional()
	 */
	public ActionScope transactional() {
		if (_transactional) {
			return this;
		}
		return new ActionScope(_names, _defaultName, true);
	}

	/**
	 * Whether the actions in this scope run inside an open transaction.
	 *
	 * <p>
	 * Such an action must not {@link ViewAction#changesViewState() change the state of the view}: a
	 * failing commit rolls back the model changes, but not what the view shows.
	 * </p>
	 */
	public boolean isTransactional() {
		return _transactional;
	}

	/**
	 * Whether there is an argument of the given name in this scope.
	 */
	public boolean contains(String name) {
		return _names.contains(name);
	}

	/**
	 * The names of the arguments in this scope, in the order of {@link ActionArguments#names()}.
	 */
	public List<String> names() {
		return _names;
	}

	/**
	 * The name of the default argument in this scope, {@code null} for {@link #NONE}.
	 */
	public String defaultName() {
		return _defaultName;
	}

	/**
	 * Arguments with the names of this scope, the default holding the given value and every other
	 * argument {@code null}.
	 *
	 * <p>
	 * These are the arguments of an operation that supplies a single value, e.g. the row of a table
	 * an executability rule decides over.
	 * </p>
	 *
	 * @param value
	 *        The value of the {@link #defaultName() default argument}; ignored for {@link #NONE}.
	 */
	public ActionArguments arguments(Object value) {
		if (_defaultName == null) {
			return ActionArguments.NONE;
		}
		List<Object> values = new ArrayList<>(_names.size());
		for (String name : _names) {
			values.add(name.equals(_defaultName) ? value : null);
		}
		return ActionArguments.of(_names, values, _defaultName);
	}

	@Override
	public String toString() {
		return _names + " (default: " + _defaultName + (_transactional ? ", transactional" : "") + ")";
	}

	/**
	 * {@link InstantiationContext} carrying the {@link ActionScope} of the actions it creates.
	 */
	private static final class ScopedContext extends InstantiationContextAdaptor {

		private final ActionScope _scope;

		ScopedContext(InstantiationContext context, ActionScope scope) {
			super(context, context);
			_scope = scope;
		}

		ActionScope scope() {
			return _scope;
		}
	}
}
