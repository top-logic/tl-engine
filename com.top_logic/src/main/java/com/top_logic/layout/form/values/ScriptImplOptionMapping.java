/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.script.ScriptImplementationProvider;
import com.top_logic.basic.config.script.ScriptImplementations;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.OptionMapping;

/**
 * {@link OptionMapping} for a property whose options are implementation classes, and that also
 * accepts a script as implementation.
 *
 * <p>
 * For a property that {@link ScriptImplementations#acceptsScript(PropertyDescriptor) accepts
 * scripts}, the options contain the {@link #getScriptOption() script option} besides the Java
 * implementation classes. Like a Java implementation, the script option is a class: the
 * {@link PolymorphicConfiguration#getImplementationClass() implementation class} of the script
 * configurations of the property. It is therefore labeled, ordered and identified like a Java
 * implementation class, and a script value maps back to it through its implementation class as in
 * {@link ImplOptionMapping}. Selecting the script option creates an empty script configuration for
 * the property, see {@link ScriptImplementationProvider#createScriptConfig(PropertyDescriptor)}.
 * Selecting a Java implementation class creates a configuration of the property's type, if that
 * is more specific than the configuration interface of the class, see
 * {@link ImplOptionMapping#newConfig(Class, Class)}.
 * </p>
 *
 * @see #forProperty(PropertyDescriptor)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ScriptImplOptionMapping implements OptionMapping {

	private final PropertyDescriptor _property;

	private final Class<?> _scriptOption;

	private ScriptImplOptionMapping(PropertyDescriptor property, Class<?> scriptOption) {
		_property = property;
		_scriptOption = scriptOption;
	}

	/**
	 * Creates the {@link ScriptImplOptionMapping} for the given property.
	 *
	 * @param property
	 *        A property whose options are implementation classes.
	 * @return The option mapping for the given property, or <code>null</code>, if the property
	 *         does not {@link ScriptImplementations#acceptsScript(PropertyDescriptor) accept
	 *         scripts}, or its interface cannot be implemented by a script.
	 */
	public static ScriptImplOptionMapping forProperty(PropertyDescriptor property) {
		PolymorphicConfiguration<?> script;
		try {
			script = ScriptImplementations.createScriptConfig(property);
		} catch (ConfigurationException ex) {
			// The interface cannot be implemented by a script, there is no script option.
			return null;
		}
		if (script == null) {
			return null;
		}
		return new ScriptImplOptionMapping(property, script.getImplementationClass());
	}

	/**
	 * The property whose options are mapped.
	 */
	public PropertyDescriptor getProperty() {
		return _property;
	}

	/**
	 * The option that stands for a script implementation of {@link #getProperty()}.
	 *
	 * <p>
	 * The option is the {@link PolymorphicConfiguration#getImplementationClass() implementation
	 * class} of the script configurations of the property.
	 * </p>
	 */
	public Class<?> getScriptOption() {
		return _scriptOption;
	}

	/**
	 * The label of the {@link #getScriptOption() script option}.
	 *
	 * <p>
	 * The script option is a class only technically. For the user, it is the choice of a script as
	 * implementation, it is therefore labelled with the label of its class (e.g. "TL-Script")
	 * without the class name that labels of Java implementation classes carry.
	 * </p>
	 */
	public ResKey getScriptOptionLabel() {
		return ResKey.forClass(_scriptOption);
	}

	/**
	 * Whether the given option is the {@link #getScriptOption() script option}.
	 */
	public boolean isScriptOption(Object option) {
		return option == _scriptOption;
	}

	@Override
	public Object toSelection(Object option) {
		if (isScriptOption(option)) {
			try {
				return ScriptImplementations.createScriptConfig(_property);
			} catch (ConfigurationException ex) {
				// Cannot happen, the script option is only offered, if a script can be created.
				throw new ConfigurationError("Cannot create script implementation for: " + _property, ex);
			}
		}
		if (option == null) {
			return null;
		}
		return ImplOptionMapping.newConfig((Class<?>) option, _property.getType());
	}

	@Override
	public Object asOption(Iterable<?> allOptions, Object selection) {
		return ImplOptionMapping.INSTANCE.asOption(allOptions, selection);
	}

}
