/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.util.List;
import java.util.Objects;

/**
 * Description of a configured variable (alias) and the origin of its value.
 *
 * <p>
 * The {@link #getSource() source} and the {@link #getValue() value} describe the state the running
 * configuration was resolved with at startup. The {@link #getStoredValue() stored value} is the
 * content of the file {@link AliasManager#STORED_VALUES_FILE} at the time this description was
 * created. A stored value written after startup takes effect only after a restart, see
 * {@link #isChangePending()}.
 * </p>
 *
 * @see AliasManager#getDefinition(String)
 * @see AliasManager#getDefinitions()
 */
public final class AliasDefinition {

	private final String _name;

	private final String _definition;

	private final List<String> _environmentNames;

	private final boolean _implicitEnvironmentName;

	private final String _defaultValue;

	private final AliasSource _source;

	private final String _sourceName;

	private final String _value;

	private final String _storedValue;

	private final String _effectiveValue;

	/**
	 * Creates a {@link AliasDefinition}.
	 *
	 * @param name
	 *        See {@link #getName()}.
	 * @param definition
	 *        See {@link #getDefinition()}.
	 * @param environmentNames
	 *        See {@link #getEnvironmentNames()}.
	 * @param implicitEnvironmentName
	 *        See {@link #isImplicitEnvironmentName()}.
	 * @param defaultValue
	 *        See {@link #getDefaultValue()}.
	 * @param source
	 *        See {@link #getSource()}.
	 * @param sourceName
	 *        See {@link #getSourceName()}.
	 * @param value
	 *        See {@link #getValue()}.
	 * @param storedValue
	 *        See {@link #getStoredValue()}.
	 * @param effectiveValue
	 *        See {@link #getEffectiveValue()}.
	 */
	public AliasDefinition(String name, String definition, List<String> environmentNames,
			boolean implicitEnvironmentName, String defaultValue, AliasSource source, String sourceName, String value,
			String storedValue, String effectiveValue) {
		_name = Objects.requireNonNull(name);
		_definition = Objects.requireNonNull(definition);
		_environmentNames = List.copyOf(environmentNames);
		_implicitEnvironmentName = implicitEnvironmentName;
		_defaultValue = Objects.requireNonNull(defaultValue);
		_source = Objects.requireNonNull(source);
		_sourceName = sourceName;
		_value = Objects.requireNonNull(value);
		_storedValue = storedValue;
		_effectiveValue = effectiveValue;
	}

	/**
	 * The name of the variable including the enclosing <code>%</code> characters, e.g.
	 * <code>%STORAGE_PATH%</code>.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * The definition of the variable as given in the configuration, including all
	 * <code>${env:...}</code> references.
	 */
	public String getDefinition() {
		return _definition;
	}

	/**
	 * The names of the JNDI values, system properties and environment variables that set the
	 * variable.
	 *
	 * <p>
	 * For a variable whose definition references environment names with <code>${env:...}</code>,
	 * these are the referenced names in the order of the definition. For any other variable, this
	 * is the single {@link #isImplicitEnvironmentName() implicit} name
	 * {@link AliasManager#implicitEnvironmentName(String)}.
	 * </p>
	 */
	public List<String> getEnvironmentNames() {
		return _environmentNames;
	}

	/**
	 * Whether the {@link #getEnvironmentNames() environment name} is derived from the name of the
	 * variable, because its definition does not reference an environment name.
	 *
	 * @see AliasManager#implicitEnvironmentName(String)
	 */
	public boolean isImplicitEnvironmentName() {
		return _implicitEnvironmentName;
	}

	/**
	 * The value the definition yields when none of the {@link #getEnvironmentNames() environment
	 * names} is set and no value is {@link #getStoredValue() stored}.
	 *
	 * <p>
	 * Each <code>${env:...}</code> reference of the {@link #getDefinition() definition} is
	 * replaced by its default (the empty string, if it has none). References to other variables
	 * are not expanded.
	 * </p>
	 */
	public String getDefaultValue() {
		return _defaultValue;
	}

	/**
	 * Where the value of the running configuration came from.
	 */
	public AliasSource getSource() {
		return _source;
	}

	/**
	 * The {@link #getEnvironmentNames() environment name} that delivered the value, if the
	 * {@link #getSource() source} is {@link AliasSource#isEnvironment() an environment source},
	 * <code>null</code> otherwise.
	 */
	public String getSourceName() {
		return _sourceName;
	}

	/**
	 * The value of the running configuration, before references to other variables are expanded.
	 *
	 * @see #getEffectiveValue()
	 */
	public String getValue() {
		return _value;
	}

	/**
	 * The value stored in the application for this variable, <code>null</code> if none is stored.
	 *
	 * <p>
	 * This is the current content of the file {@link AliasManager#STORED_VALUES_FILE}, which may
	 * differ from the value the running configuration was resolved with.
	 * </p>
	 *
	 * @see #isChangePending()
	 */
	public String getStoredValue() {
		return _storedValue;
	}

	/**
	 * The value of the running configuration with all references to other variables expanded.
	 */
	public String getEffectiveValue() {
		return _effectiveValue;
	}

	/**
	 * Whether the {@link #getStoredValue() stored value} was changed after startup in a way that
	 * changes the value of the variable after a restart.
	 *
	 * <p>
	 * A stored value has no effect while one of the {@link #getEnvironmentNames() environment
	 * names} sets the variable.
	 * </p>
	 */
	public boolean isChangePending() {
		if (_source.isEnvironment()) {
			return false;
		}
		String startupStoredValue = _source == AliasSource.STORED ? _value : null;
		return !Objects.equals(startupStoredValue, _storedValue);
	}

	@Override
	public String toString() {
		// Values are not printed, since they may be sensitive.
		return getClass().getSimpleName() + "(" + _name + ", source=" + _source + ")";
	}

}
