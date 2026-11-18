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

import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLClass;

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
 * An argument filled from a channel may carry the {@link ViewChannel#getValueType() declared type}
 * of the channel's values, see {@link #withValueType(TLClass)}. A rule deciding over an argument
 * whose value is missing can then still decide by the type, see {@link #valueType()}.
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
	public static final ActionArguments NONE = new ActionArguments(Collections.emptyMap(), null, Collections.emptyMap());

	private final Map<String, Object> _values;

	private final String _defaultName;

	/**
	 * The declared types of the values of the arguments, by argument name; an argument without
	 * declared type has no entry.
	 */
	private final Map<String, TLClass> _valueTypes;

	private ActionArguments(Map<String, Object> values, String defaultName, Map<String, TLClass> valueTypes) {
		_values = values;
		_defaultName = defaultName;
		_valueTypes = valueTypes;
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
		return new ActionArguments(Collections.unmodifiableMap(map), defaultName, Collections.emptyMap());
	}

	/**
	 * These arguments extended by the given named value, which becomes the default.
	 *
	 * <p>
	 * A value of the same name in these arguments is shadowed: it is no longer accessible in the
	 * result, and the name moves to the end of the {@link #names() order}. The
	 * {@link #valueType() declared types} of the other arguments are kept, the shadowed value's type
	 * is dropped.
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
		Map<String, TLClass> valueTypes = _valueTypes;
		if (valueTypes.containsKey(name)) {
			valueTypes = new LinkedHashMap<>(valueTypes);
			valueTypes.remove(name);
			valueTypes = Collections.unmodifiableMap(valueTypes);
		}
		return new ActionArguments(Collections.unmodifiableMap(values), name, valueTypes);
	}

	/**
	 * These arguments with the given type declared for the values of the
	 * {@link #defaultName() default argument}.
	 *
	 * <p>
	 * The type is a declaration only, e.g. the {@link ViewChannel#getValueType() value type} of the
	 * channel the argument is read from; the value is not checked against it.
	 * </p>
	 *
	 * @param type
	 *        The declared type, {@code null} for none.
	 * @throws IllegalStateException
	 *         If these arguments have no default argument.
	 */
	public ActionArguments withValueType(TLClass type) {
		if (_defaultName == null) {
			throw new IllegalStateException("Arguments without default argument declare no value type.");
		}
		if (type == _valueTypes.get(_defaultName)) {
			return this;
		}
		LinkedHashMap<String, TLClass> valueTypes = new LinkedHashMap<>(_valueTypes);
		if (type == null) {
			valueTypes.remove(_defaultName);
		} else {
			valueTypes.put(_defaultName, type);
		}
		return new ActionArguments(_values, _defaultName, Collections.unmodifiableMap(valueTypes));
	}

	/**
	 * These arguments with the given argument as default.
	 *
	 * <p>
	 * The {@link #valueType() declared types} of the arguments are kept.
	 * </p>
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
		return new ActionArguments(_values, name, _valueTypes);
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
	 * The declared type of the values of the {@link #defaultName() default argument},
	 * {@code null} if unknown.
	 *
	 * <p>
	 * The type is known also while the value is missing, so that a rule can decide by the type
	 * what it decides by the object otherwise.
	 * </p>
	 *
	 * @see #withValueType(TLClass)
	 */
	public TLClass valueType() {
		return _defaultName == null ? null : _valueTypes.get(_defaultName);
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
