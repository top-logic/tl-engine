/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.AliasDefinition;
import com.top_logic.basic.AliasManager;
import com.top_logic.basic.AliasSource;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.search.expr.config.operations.AdminOnly;
import com.top_logic.model.search.expr.config.operations.ConfigFunctions;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.monitor.SystemEnvironmentBuilder;

/**
 * TL-Script functions about the configuration variables (aliases) of the application: what a view
 * displaying the variables of an installation shows of them, and setting a value for a variable in
 * the application.
 *
 * <p>
 * {@link #definitions()} reads the descriptions of all configured variables, the other functions
 * describe one of these {@link AliasDefinition}s, e.g. the row of a table. Every function
 * describing a variable accepts no variable at all and answers with nothing.
 * </p>
 *
 * <p>
 * To read the value of a configuration variable (alias) by its name, use the TL-Script function
 * <code>configVariable(name)</code> ({@link ConfigFunctions#variable(String)}).
 * </p>
 *
 * <p>
 * The value of a variable whose name is {@link SystemEnvironmentBuilder#protectedNames()
 * protected} is never revealed: Each function returning a value answers with the text
 * {@link com.top_logic.layout.form.I18NConstants#BLOCKED_VALUE_TEXT} instead, and such a variable
 * cannot be set or removed here.
 * </p>
 *
 * <p>
 * {@link #store(AliasDefinition, String)} and {@link #remove(AliasDefinition)} write and remove the
 * value stored in the application for a variable, see {@link AliasManager#storeValue(String,
 * String)}. A change takes effect after a restart of the application and is stored on this node
 * only. Values are never written to the log.
 * </p>
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #definitions()} is called <code>configVariableDefinitions</code>. The functions
 *           read the configuration of the installation, which only an administrator may inspect
 *           from a script entered interactively.
 */
@ScriptPrefix(ConfigVariableFunctions.PREFIX)
@AdminOnly
public class ConfigVariableFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "configVariable";

	/** Separator between the environment names of a variable in {@link #environmentNames(AliasDefinition)}. */
	private static final String NAME_SEPARATOR = ", ";

	/**
	 * The descriptions of all configured variables, in the order of their names.
	 *
	 * @return A snapshot: the stored value of each description is the one found when it was read.
	 *
	 * @see AliasManager#getDefinitions()
	 */
	@Label("Configuration variables")
	public static List<AliasDefinition> definitions() {
		return AliasManager.getInstance().getDefinitions();
	}

	/**
	 * The name of the given variable including the enclosing <code>%</code> characters.
	 *
	 * @param variable
	 *        The description of the variable.
	 * @return The name, <code>null</code> without a variable.
	 */
	@Label("Name of a configuration variable")
	@SideEffectFree
	public static String name(AliasDefinition variable) {
		return variable == null ? null : variable.getName();
	}

	/**
	 * The names of the JNDI values, system properties and environment variables setting the given
	 * variable, an implicit name marked as such.
	 *
	 * @param variable
	 *        The description of the variable.
	 * @return The names separated by commas, <code>null</code> without a variable.
	 *
	 * @see AliasDefinition#getEnvironmentNames()
	 */
	@Label("Environment names of a configuration variable")
	@SideEffectFree
	public static String environmentNames(AliasDefinition variable) {
		if (variable == null) {
			return null;
		}
		List<String> names = variable.getEnvironmentNames();
		if (variable.isImplicitEnvironmentName()) {
			Resources resources = Resources.getInstance();
			return names.stream()
				.map(name -> resources.getString(I18NConstants.CONFIG_VARIABLE_IMPLICIT_NAME__NAME.fill(name)))
				.collect(Collectors.joining(NAME_SEPARATOR));
		}
		return String.join(NAME_SEPARATOR, names);
	}

	/**
	 * Where the value of the running configuration came from, with the environment name that
	 * delivered it.
	 *
	 * @param variable
	 *        The description of the variable.
	 * @return The label of the {@link AliasSource}, followed by the environment name for a value
	 *         from the environment, <code>null</code> without a variable.
	 *
	 * @see AliasDefinition#getSource()
	 * @see AliasDefinition#getSourceName()
	 */
	@Label("Source of a configuration variable")
	@SideEffectFree
	public static String source(AliasDefinition variable) {
		if (variable == null) {
			return null;
		}
		ResKey source = ResKey.forEnum(variable.getSource());
		String sourceName = variable.getSourceName();
		ResKey text =
			sourceName == null ? source : I18NConstants.CONFIG_VARIABLE_SOURCE_NAME__SOURCE_NAME.fill(source, sourceName);
		return Resources.getInstance().getString(text);
	}

	/**
	 * The value of the given variable in the running configuration, with all references to other
	 * variables expanded.
	 *
	 * @param variable
	 *        The description of the variable.
	 * @return The value, or the replacement text for a protected variable, <code>null</code>
	 *         without a variable.
	 *
	 * @see AliasDefinition#getEffectiveValue()
	 */
	@Label("Value of a configuration variable")
	@SideEffectFree
	public static String value(AliasDefinition variable) {
		return variable == null ? null : mask(variable, variable.getEffectiveValue());
	}

	/**
	 * The value the definition of the given variable yields, when it is not set otherwise.
	 *
	 * @param variable
	 *        The description of the variable.
	 * @return The default value with references to other variables not expanded, or the
	 *         replacement text for a protected variable, <code>null</code> without a variable.
	 *
	 * @see AliasDefinition#getDefaultValue()
	 */
	@Label("Default value of a configuration variable")
	@SideEffectFree
	public static String defaultValue(AliasDefinition variable) {
		return variable == null ? null : mask(variable, variable.getDefaultValue());
	}

	/**
	 * The value stored in the application for the given variable.
	 *
	 * @param variable
	 *        The description of the variable.
	 * @return The stored value, or the replacement text for a protected variable,
	 *         <code>null</code> if none is stored and without a variable.
	 *
	 * @see AliasDefinition#getStoredValue()
	 */
	@Label("Stored value of a configuration variable")
	@SideEffectFree
	public static String storedValue(AliasDefinition variable) {
		return variable == null ? null : mask(variable, variable.getStoredValue());
	}

	/**
	 * The value to start editing the given variable with: the stored value, otherwise the value of
	 * the running configuration before references to other variables are expanded.
	 *
	 * @param variable
	 *        The description of the variable.
	 * @return The value, <code>null</code> for a protected variable and without a variable.
	 */
	@Label("Value to edit of a configuration variable")
	@SideEffectFree
	public static String editValue(AliasDefinition variable) {
		if (variable == null || isProtected(variable)) {
			return null;
		}
		String stored = variable.getStoredValue();
		return stored != null ? stored : variable.getValue();
	}

	/**
	 * Whether the value of the given variable changes after a restart, because its stored value
	 * was changed.
	 *
	 * @param variable
	 *        The description of the variable.
	 * @return Whether a change is pending, <code>false</code> without a variable.
	 *
	 * @see AliasDefinition#isChangePending()
	 */
	@Label("Whether a configuration variable changes on restart")
	@SideEffectFree
	public static boolean changePending(AliasDefinition variable) {
		return variable != null && variable.isChangePending();
	}

	/**
	 * Why no value can be stored for the given variable.
	 *
	 * @param variable
	 *        The description of the variable.
	 * @return The reason, <code>null</code> if a value can be stored.
	 *
	 * @see #store(AliasDefinition, String)
	 */
	@Label("Why a configuration variable cannot be set")
	public static ResKey storeProblem(AliasDefinition variable) {
		if (variable == null) {
			return I18NConstants.ERROR_NO_CONFIG_VARIABLE;
		}
		if (isProtected(variable)) {
			return I18NConstants.ERROR_CONFIG_VARIABLE_PROTECTED__NAME.fill(variable.getName());
		}
		AliasSource source = variable.getSource();
		if (source.isEnvironment()) {
			return I18NConstants.ERROR_CONFIG_VARIABLE_SET_BY_ENVIRONMENT__SOURCE_NAME.fill(ResKey.forEnum(source),
				variable.getSourceName());
		}
		return fileProblem();
	}

	/**
	 * Why the value stored for the given variable cannot be removed.
	 *
	 * <p>
	 * A stored value can be removed while the variable is set by its environment, in which case
	 * the stored value has no effect anyway.
	 * </p>
	 *
	 * @param variable
	 *        The description of the variable.
	 * @return The reason, <code>null</code> if the stored value can be removed.
	 *
	 * @see #remove(AliasDefinition)
	 */
	@Label("Why the stored value of a configuration variable cannot be removed")
	public static ResKey removeProblem(AliasDefinition variable) {
		if (variable == null) {
			return I18NConstants.ERROR_NO_CONFIG_VARIABLE;
		}
		if (isProtected(variable)) {
			return I18NConstants.ERROR_CONFIG_VARIABLE_PROTECTED__NAME.fill(variable.getName());
		}
		if (variable.getStoredValue() == null) {
			return I18NConstants.ERROR_CONFIG_VARIABLE_NOT_STORED__NAME.fill(variable.getName());
		}
		return fileProblem();
	}

	/**
	 * Whether a value can be stored for the given variable.
	 *
	 * @param variable
	 *        The description of the variable.
	 * @return Whether there is no reason against storing a value.
	 *
	 * @see #storeProblem(AliasDefinition)
	 */
	@Label("Whether a configuration variable can be set")
	public static boolean storable(AliasDefinition variable) {
		return storeProblem(variable) == null;
	}

	/**
	 * Whether the value stored for the given variable can be removed.
	 *
	 * @param variable
	 *        The description of the variable.
	 * @return Whether there is no reason against removing the stored value.
	 *
	 * @see #removeProblem(AliasDefinition)
	 */
	@Label("Whether the stored value of a configuration variable can be removed")
	public static boolean removable(AliasDefinition variable) {
		return removeProblem(variable) == null;
	}

	/**
	 * Stores a value for the given variable.
	 *
	 * <p>
	 * The value takes effect after a restart of the application, if none of the environment names
	 * of the variable is set. It is stored on this node only. The description itself still shows
	 * the stored value found when it was read, reading the variables again shows the new one.
	 * </p>
	 *
	 * @param variable
	 *        The description of the variable.
	 * @param value
	 *        The value to store. No value stores the empty value; to fall back to the definition,
	 *        {@link #remove(AliasDefinition) remove} the stored value instead.
	 *
	 * @throws TopLogicException
	 *         If {@link #storeProblem(AliasDefinition)} finds a reason against storing a value. No
	 *         message contains the value.
	 *
	 * @see AliasManager#storeValue(String, String)
	 */
	@Label("Set a configuration variable")
	public static void store(AliasDefinition variable, String value) {
		check(storeProblem(variable));
		AliasManager.getInstance().storeValue(variable.getName(), value == null ? "" : value);
	}

	/**
	 * Removes the value stored for the given variable, so that after a restart of the application
	 * the variable takes the value of its environment or its definition again.
	 *
	 * @param variable
	 *        The description of the variable.
	 *
	 * @throws TopLogicException
	 *         If {@link #removeProblem(AliasDefinition)} finds a reason against removing the
	 *         value.
	 *
	 * @see AliasManager#removeValue(String)
	 */
	@Label("Remove the stored value of a configuration variable")
	public static void remove(AliasDefinition variable) {
		check(removeProblem(variable));
		AliasManager.getInstance().removeValue(variable.getName());
	}

	/**
	 * The file holding the values stored in the application on this node.
	 *
	 * @return The absolute path of the file, existing or not, <code>null</code> if the application
	 *         does not support stored values.
	 *
	 * @see AliasManager#getStoredValuesFile()
	 */
	@Label("File of the stored configuration variables")
	public static String storedValuesFile() {
		File file = AliasManager.getInstance().getStoredValuesFile();
		return file == null ? null : file.getAbsolutePath();
	}

	private static ResKey fileProblem() {
		AliasManager aliasManager = AliasManager.getInstance();
		File file = aliasManager.getStoredValuesFile();
		if (file == null) {
			return I18NConstants.ERROR_CONFIG_VARIABLES_NOT_SUPPORTED;
		}
		if (!aliasManager.canStoreValues()) {
			return I18NConstants.ERROR_CONFIG_VARIABLES_NOT_WRITABLE__FILE.fill(file.getAbsolutePath());
		}
		return null;
	}

	private static void check(ResKey problem) {
		if (problem != null) {
			throw new TopLogicException(problem);
		}
	}

	private static boolean isProtected(AliasDefinition variable) {
		return SystemEnvironmentBuilder.protectedNames().test(variable.getName());
	}

	private static String mask(AliasDefinition variable, String value) {
		if (value == null || !isProtected(variable)) {
			return value;
		}
		return Resources.getInstance().getString(com.top_logic.layout.form.I18NConstants.BLOCKED_VALUE_TEXT);
	}

}
