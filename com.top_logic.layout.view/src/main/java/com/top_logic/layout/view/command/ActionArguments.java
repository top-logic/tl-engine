/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The named values an action chain runs with.
 *
 * <p>
 * The trigger of a chain - a command, a drop, an upload - supplies the arguments, and they flow
 * unchanged through the chain. An action with a result runs its follow-up actions with the arguments
 * {@link #with(String, Object) extended} by that result. One of the names is the
 * {@link #defaultName() default}: its value is what an action processing a single value works on
 * unless it selects another argument. The default is the innermost result, or the main argument of
 * the trigger outside of follow-up actions.
 * </p>
 *
 * <p>
 * Instances are immutable; every extension creates new arguments.
 * </p>
 *
 * @see ActionScope
 */
public final class ActionArguments {

	/**
	 * Arguments without any value, for a trigger that supplies nothing.
	 */
	public static final ActionArguments NONE = new ActionArguments(Collections.emptyMap(), null);

	private final Map<String, Object> _values;

	private final String _defaultName;

	private ActionArguments(Map<String, Object> values, String defaultName) {
		_values = values;
		_defaultName = defaultName;
	}

	/**
	 * Arguments consisting of a single named value, which is the default.
	 *
	 * @param name
	 *        The name of the value.
	 * @param value
	 *        The value, may be {@code null}.
	 */
	public static ActionArguments of(String name, Object value) {
		return NONE.with(name, value);
	}

	/**
	 * Arguments with the given names and values.
	 *
	 * <p>
	 * A name occurring more than once is shadowed by its last occurrence, see
	 * {@link #with(String, Object)}.
	 * </p>
	 *
	 * @param names
	 *        The names of the arguments.
	 * @param values
	 *        The values of the arguments, in the order of the names.
	 * @param defaultName
	 *        The name of the default argument, one of the given names; {@code null} only for no
	 *        names at all.
	 */
	public static ActionArguments of(List<String> names, List<?> values, String defaultName) {
		if (names.size() != values.size()) {
			throw new IllegalArgumentException("Names " + names + " do not match values " + values + ".");
		}
		if (names.isEmpty()) {
			return NONE;
		}
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		for (int n = 0, cnt = names.size(); n < cnt; n++) {
			map.remove(names.get(n));
			map.put(names.get(n), values.get(n));
		}
		if (!map.containsKey(defaultName)) {
			throw new IllegalArgumentException("Default '" + defaultName + "' is not among " + names + ".");
		}
		return new ActionArguments(Collections.unmodifiableMap(map), defaultName);
	}

	/**
	 * These arguments extended by the given named value, which becomes the default.
	 *
	 * <p>
	 * A value of the same name in these arguments is shadowed: it is no longer accessible in the
	 * result, and the name moves to the end of the {@link #names() order}.
	 * </p>
	 *
	 * @param name
	 *        The name of the additional value.
	 * @param value
	 *        The value, may be {@code null}.
	 */
	public ActionArguments with(String name, Object value) {
		LinkedHashMap<String, Object> values = new LinkedHashMap<>(_values);
		values.remove(name);
		values.put(name, value);
		return new ActionArguments(Collections.unmodifiableMap(values), name);
	}

	/**
	 * These arguments with the given argument as default.
	 *
	 * @param name
	 *        The name of an argument of these arguments.
	 * @throws IllegalArgumentException
	 *         If there is no argument of the given name.
	 */
	public ActionArguments withDefault(String name) {
		checkName(name);
		if (name.equals(_defaultName)) {
			return this;
		}
		return new ActionArguments(_values, name);
	}

	/**
	 * Whether there is an argument of the given name.
	 */
	public boolean has(String name) {
		return _values.containsKey(name);
	}

	/**
	 * The value of the argument with the given name.
	 *
	 * @throws IllegalArgumentException
	 *         If there is no argument of the given name.
	 */
	public Object get(String name) {
		checkName(name);
		return _values.get(name);
	}

	/**
	 * The value of the {@link #defaultName() default argument}, {@code null} when there is none.
	 */
	public Object value() {
		return _defaultName == null ? null : _values.get(_defaultName);
	}

	/**
	 * The name of the argument an action processing a single value works on, {@code null} for
	 * arguments without any value.
	 */
	public String defaultName() {
		return _defaultName;
	}

	/**
	 * The names of all arguments, in the order they were added.
	 */
	public List<String> names() {
		return List.copyOf(_values.keySet());
	}

	/**
	 * The values of the arguments with the given names, in the order of the names.
	 *
	 * @param names
	 *        Names of arguments of these arguments, e.g. the {@link ActionScope#names() names of the
	 *        scope} an action was instantiated in.
	 * @throws IllegalArgumentException
	 *         If one of the names has no argument.
	 */
	public List<Object> values(List<String> names) {
		List<Object> result = new ArrayList<>(names.size());
		for (String name : names) {
			result.add(get(name));
		}
		return result;
	}

	private void checkName(String name) {
		if (!_values.containsKey(name)) {
			throw new IllegalArgumentException("No action argument '" + name + "', available: " + _values.keySet());
		}
	}

	@Override
	public String toString() {
		return _values + " (default: " + _defaultName + ")";
	}
}
