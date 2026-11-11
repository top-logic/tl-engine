/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.ServletContext;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.category.RuntimeCategory;
import com.top_logic.basic.module.services.ServletContextService;
import com.top_logic.basic.vars.VariableExpander;

/**
 * Service resolving configuration properties (aliases).
 * 
 * <p>
 * Aliases can be of any form, but it is recommended to use <code>%ABC%</code>.
 * </p>
 * 
 * <p>
 * It is possible to define aliases recursively. The definitions <code>%A%=%B%a</code> and
 * <code>%B%=b</code> imply that
 * e.g <code>c%A%d</code> will be expanded to <code>cbad</code> regardless of the order of
 * definition. What happens if a cycle (e.g <code>%A%=%A%</code>) occurs is undefined. The aliases
 * are not necessarily expanded in the internal representation. Thus a call to
 * {@link com.top_logic.basic.AliasManager#getAlias(java.lang.String)} can deliver an alias that is
 * completely, partially or not at all expanded.
 * </p>
 * 
 * <p>
 * Aliases are defined through two mechanisms. You can set base aliases (these may not contain
 * references to other aliases) through {@link #setBaseAliases(java.util.Map)}. Additionally further
 * aliases are read from the configuration.
 * </p>
 *
 * <h2>Values of configured aliases</h2>
 *
 * <p>
 * The value of a configured alias is determined from the following origins, the first one that
 * delivers a value wins (see {@link AliasSource}):
 * </p>
 * <ol>
 * <li>The environment names of the alias, each looked up as JNDI value <code>tl/&lt;name&gt;</code>,
 * system property and environment variable (in this order). The environment names of an alias whose
 * definition contains <code>${env:NAME:default}</code> references are the referenced names in the
 * order of the definition; when one of them is set, each reference is replaced by its value or its
 * default. An alias without such a reference has the single implicit environment name
 * {@link #implicitEnvironmentName(String)}, whose value replaces the whole definition.</li>
 * <li>A value stored in the application, see {@link #storeValue(String, String)}. It replaces the
 * whole definition.</li>
 * <li>The definition with each <code>${env:...}</code> reference replaced by its default.</li>
 * </ol>
 *
 * <p>
 * The programmatic base aliases ({@link #createProgrammaticAliases()}) are no configured aliases:
 * They have no implicit environment name and cannot be stored.
 * </p>
 *
 * <p>
 * How each configured alias got its value is described by {@link #getDefinition(String)}.
 * </p>
 *
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
@Label("Alias management")
@ServiceCategory.Of(RuntimeCategory.class)
public abstract class AliasManager extends ManagedClass implements Reloadable {

	/**
	 * Pattern matching things to quote in replacements.
	 * 
	 * @see #quoteReplacement(String)
	 */
	private static final Pattern REPLACEMENT_QUOTE_PATTERN = Pattern.compile("\\$|\\\\");

	/** variable mappinng to {@link ServletContextService#getApplication()} */
    public static final String APP_ROOT = "%APP_ROOT%";

    /** context path of the web application */
    public static final String APP_CONTEXT = "%APP_CONTEXT%";

	/** Alias for the host of the application. Potential value: "http://apps.top-logic.com:8080" */
	public static final String HOST = "%HOST%";

	/** Well-known alias holding the local name of the host of the application.. */
	public static final String LOCAL_HOST_NAME = "%LOCAL_HOST_NAME%";

	/**
	 * Name of the file in the <code>autoconf</code> folder holding the values of configured
	 * aliases set in the application.
	 *
	 * <p>
	 * The file is a properties file in UTF-8 encoding. Its keys are alias names including the
	 * enclosing <code>%</code> characters.
	 * </p>
	 *
	 * @see #storeValue(String, String)
	 * @see #getStoredValuesFile()
	 */
	public static final String STORED_VALUES_FILE = "variables.properties";

	/**
	 * Prefix of the {@link #implicitEnvironmentName(String) implicit environment name} of an alias.
	 */
	public static final String IMPLICIT_ENVIRONMENT_PREFIX = "tl_";

	/** Suffix of the temporary file written before replacing {@link #STORED_VALUES_FILE}. */
	private static final String TEMP_FILE_SUFFIX = ".tmp";

	/** Characters not allowed in an {@link #implicitEnvironmentName(String) implicit name}. */
	private static final Pattern NON_ENVIRONMENT_NAME_CHAR = Pattern.compile("[^a-z0-9_]");

	/** Replacement of a {@link #NON_ENVIRONMENT_NAME_CHAR}. */
	private static final String ENVIRONMENT_NAME_REPLACEMENT = "_";

	/** Separator of alias names. */
	private static final String ALIAS_MARKER = "%";

	/** Pattern matching an environment variable. */
	private static final Pattern ENVIRONMENT_VARIABLE =
		Pattern.compile(
			"\\$\\{env:" +
				"(" + "(?:" + "[^\\\\}:]|\\\\." + ")*" + ")" +
				"(?:" +
				":(" + "(?:" + "[^\\\\}]|\\\\." + ")*" + ")" +
				")?" +
				"\\}");

	private Map<String, String> _baseAliases;

	private Map<String, String> _configuredAliases;

	/**
	 * Descriptions of the configured aliases, indexed by the alias name without the enclosing
	 * <code>%</code> characters.
	 *
	 * <p>
	 * The {@link AliasDefinition#getStoredValue() stored value} of a description in this map is the
	 * one at the time of resolution.
	 * </p>
	 */
	private Map<String, AliasDefinition> _definitions = Collections.emptyMap();

	/** The map containing the aliases. */
	private VariableExpander _expander = new VariableExpander();

    /**
	 * Creates an {@link AliasManager}.
	 */
	protected AliasManager() {
		super();
    }

	@Override
	protected void startUp() {
		super.startUp();

		_baseAliases = createProgrammaticAliases();
		fetchConfiguredAliases();
		initAliasMap();

		ReloadableManager.getInstance().addReloadable(this);
	}

	/**
	 * Creates programmatic aliases.
	 */
	protected Map<String, String> createProgrammaticAliases() {
		Map<String, String> aliases = new HashMap<>();
		if (ServletContextService.Module.INSTANCE.isActive()) {
			ServletContextService contextService = ServletContextService.getInstance();
			ServletContext context = contextService.getServletContext();
			String applicationPath = contextService.getApplication().getPath();

			aliases.put(APP_CONTEXT, context.getContextPath());
			aliases.put(APP_ROOT, applicationPath);
		} else {
			aliases.put(APP_CONTEXT, "");
			aliases.put(APP_ROOT, new File(ModuleLayoutConstants.WEBAPP_DIR).getAbsolutePath());
		}
		addLocalHostName(aliases);
		return aliases;
	}

	private void addLocalHostName(Map<String, String> aliases) {
		String hostName;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException ex) {
			Logger.error("Unable to find host name of this machine", ex, AliasManager.class);
			hostName = "localhost";
		}
		aliases.put(LOCAL_HOST_NAME, hostName);
	}

	@Override
	protected void shutDown() {
		ReloadableManager.getInstance().removeReloadable(this);
		super.shutDown();
	}
    
	@Override
	public String getDescription() {
        return "Aliases for the whole application";
    }

    @Override
	public String getName() {
        return "Alias Manager";
    }

    @Override
	public boolean reload() {
		fetchConfiguredAliases();
		initAliasMap();
        return true;
    }

    @Override
	public String toString () {
		return getClass().getName() + " [" + getDefinedAliases() + "]";
    }

    /**
	 * Do the replacement of all known aliases in the given string.
	 * <p>
	 * <code>null</code> values are returned untreated.
	 * </p>
	 *
	 * @param s
	 *        The string with the parts to be replaced.
	 * @return The alias free string.
	 */
	public String replace(String s) {
		return _expander.expand(s);
    }

	/**
	 * The {@link VariableExpander} in use.
	 */
	public VariableExpander getExpander() {
		return _expander;
	}

    /**
	 * Returns the mapped name for the given alias.
	 *
	 * @param name
	 *        The name of the requested alias.
	 * @return The real name as defined in the alias.
	 */
	public String getAlias(String name) {
		return _expander.getValue(stripSpecial(name));
    }

    /**
	 * Set the aliases that are used as "bootstrap" for all later replacement (including the
	 * configured aliases).
	 *
	 * @param baseAliases
	 *        the aliases; must not be null
	 */
	public final void setBaseAliases(Map<String, String> baseAliases) {
		_baseAliases = baseAliases;
		initAliasMap();
    }

	private void fetchConfiguredAliases() {
		_configuredAliases = resolveConfiguredAliases();
	}

	/**
	 * Retrieves the values of the configured aliases.
	 *
	 * <p>
	 * An implementation that reads alias definitions from the configuration resolves them with
	 * {@link #resolveDefinitions(Map)}.
	 * </p>
	 */
	protected abstract Map<String, String> resolveConfiguredAliases();

	/**
	 * Determines the values of the given configured alias definitions and records how each value
	 * was determined.
	 *
	 * <p>
	 * The recorded descriptions replace the ones of a previous resolution and are reported by
	 * {@link #getDefinition(String)}.
	 * </p>
	 *
	 * @param definitions
	 *        The alias definitions from the configuration, mapping alias names to their raw
	 *        definition.
	 * @return The values of the aliases, mapping the same names to the determined values.
	 *
	 * @see AliasManager Values of configured aliases.
	 */
	protected final Map<String, String> resolveDefinitions(Map<String, String> definitions) {
		Properties storedValues = readStoredValues();
		Map<String, AliasDefinition> descriptions = new TreeMap<>();
		Map<String, String> result = new HashMap<>();
		for (Entry<String, String> entry : definitions.entrySet()) {
			String name = entry.getKey();
			String definition = entry.getValue();
			String key = stripSpecial(name);
			boolean programmatic = isProgrammatic(key);

			List<String> environmentNames = new ArrayList<>();
			List<String> missing = new ArrayList<>();
			StringBuilder defaultValue = new StringBuilder();
			StringBuilder environmentValue = new StringBuilder();
			AliasSource source = null;
			String sourceName = null;

			Matcher matcher = ENVIRONMENT_VARIABLE.matcher(definition);
			int last = 0;
			boolean explicit = false;
			while (matcher.find()) {
				explicit = true;
				String variableName = matcher.group(1);
				String variableDefault = matcher.group(2);
				if (variableDefault != null) {
					variableDefault = variableDefault.replaceAll("\\\\(.)", "$1");
				}
				if (!environmentNames.contains(variableName)) {
					environmentNames.add(variableName);
				}

				String prefix = definition.substring(last, matcher.start());
				defaultValue.append(prefix);
				environmentValue.append(prefix);
				last = matcher.end();

				EnvironmentValue lookup = lookupEnvironment(variableName);
				if (lookup != null) {
					if (source == null) {
						source = lookup.source();
						sourceName = variableName;
					}
					environmentValue.append(lookup.value());
				} else if (variableDefault != null) {
					environmentValue.append(variableDefault);
				} else {
					missing.add(variableName);
				}
				defaultValue.append(variableDefault == null ? "" : variableDefault);
			}
			String suffix = definition.substring(last);
			defaultValue.append(suffix);
			environmentValue.append(suffix);

			if (programmatic) {
				// A configured value for a programmatic alias is resolved without being described.
				logMissing(missing, name);
				result.put(name, explicit ? environmentValue.toString() : definition);
				continue;
			}

			String value;
			if (!explicit) {
				String implicitName = implicitEnvironmentName(key);
				environmentNames.add(implicitName);
				EnvironmentValue lookup = lookupEnvironment(implicitName);
				if (lookup != null) {
					source = lookup.source();
					sourceName = implicitName;
				}
				value = lookup != null ? lookup.value() : null;
			} else {
				value = source != null ? environmentValue.toString() : null;
				if (source != null) {
					logMissing(missing, name);
				}
			}

			String aliasName = ALIAS_MARKER + key + ALIAS_MARKER;
			String storedValue = storedValues.getProperty(aliasName);
			if (value == null) {
				if (storedValue != null) {
					source = AliasSource.STORED;
					value = storedValue;
				} else {
					source = AliasSource.DEFINITION;
					value = defaultValue.toString();
					logMissing(missing, name);
				}
			}

			result.put(name, value);
			descriptions.put(key, new AliasDefinition(aliasName, definition, environmentNames, !explicit,
				defaultValue.toString(), source, sourceName, value, storedValue, null));
		}
		_definitions = descriptions;
		return result;
	}

	private boolean isProgrammatic(String key) {
		Map<String, String> baseAliases = _baseAliases;
		if (baseAliases == null) {
			return false;
		}
		for (String baseName : baseAliases.keySet()) {
			if (stripSpecial(baseName).equals(key)) {
				return true;
			}
		}
		return false;
	}

	private static void logMissing(List<String> missing, String aliasName) {
		for (String variableName : missing) {
			Logger.info("Neither system property nor environment variable '" + variableName
				+ "' set. No default value specified. Using empty alias for alias '" + aliasName + "'.",
				AliasManager.class);
		}
	}

	/**
	 * Looks up the given name as JNDI value, system property and environment variable (in this
	 * order).
	 *
	 * @return The first value found and its origin, <code>null</code> if the name is not set.
	 *
	 * @see Environment#getSystemPropertyOrEnvironmentVariable(String, String)
	 */
	private static EnvironmentValue lookupEnvironment(String name) {
		String jndiValue = Environment.lookupJNDIProperty(name);
		if (jndiValue != null) {
			return new EnvironmentValue(AliasSource.JNDI, jndiValue);
		}
		String propertyValue = Environment.lookupSystemProperty(name);
		if (propertyValue != null) {
			return new EnvironmentValue(AliasSource.SYSTEM_PROPERTY, propertyValue);
		}
		String environmentValue = Environment.lookupEnvironmentVariable(name);
		if (environmentValue != null) {
			return new EnvironmentValue(AliasSource.ENVIRONMENT, environmentValue);
		}
		return null;
	}

	/**
	 * A value found for an environment name.
	 */
	private record EnvironmentValue(AliasSource source, String value) {
		// Pure value.
	}

	/**
	 * The environment name that sets a configured alias whose definition references no environment
	 * name.
	 *
	 * <p>
	 * The name is {@value #IMPLICIT_ENVIRONMENT_PREFIX} followed by the alias name without the
	 * enclosing <code>%</code> characters, lower-cased, with each character other than
	 * <code>a-z</code>, <code>0-9</code> and <code>_</code> replaced by <code>_</code>: The
	 * alias <code>%STORAGE_PATH%</code> is set by <code>tl_storage_path</code>.
	 * </p>
	 *
	 * @param aliasName
	 *        The name of the alias with or without the enclosing <code>%</code> characters.
	 * @return The environment name.
	 */
	public static String implicitEnvironmentName(String aliasName) {
		String lowerName = stripSpecial(aliasName).toLowerCase(Locale.ROOT);
		return IMPLICIT_ENVIRONMENT_PREFIX
			+ NON_ENVIRONMENT_NAME_CHAR.matcher(lowerName).replaceAll(ENVIRONMENT_NAME_REPLACEMENT);
	}

	/**
	 * The description of the configured alias with the given name.
	 *
	 * <p>
	 * The {@link AliasDefinition#getStoredValue() stored value} is read freshly from
	 * {@link #getStoredValuesFile()}.
	 * </p>
	 *
	 * @param name
	 *        The name of the alias, with or without the enclosing <code>%</code> characters.
	 * @return The description, <code>null</code> if there is no configured alias with the given
	 *         name. A programmatic alias ({@link #createProgrammaticAliases()}) has no description.
	 */
	public AliasDefinition getDefinition(String name) {
		AliasDefinition definition = lookupDefinition(name);
		if (definition == null) {
			return null;
		}
		return current(definition, readStoredValues());
	}

	/**
	 * The descriptions of all configured aliases in the order of their names.
	 *
	 * @see #getDefinition(String)
	 */
	public List<AliasDefinition> getDefinitions() {
		Properties storedValues = readStoredValues();
		List<AliasDefinition> result = new ArrayList<>(_definitions.size());
		for (AliasDefinition definition : _definitions.values()) {
			result.add(current(definition, storedValues));
		}
		return result;
	}

	private AliasDefinition current(AliasDefinition definition, Properties storedValues) {
		String name = definition.getName();
		return new AliasDefinition(name, definition.getDefinition(), definition.getEnvironmentNames(),
			definition.isImplicitEnvironmentName(), definition.getDefaultValue(), definition.getSource(),
			definition.getSourceName(), definition.getValue(), storedValues.getProperty(name),
			_expander.getValue(stripSpecial(name)));
	}

	private AliasDefinition lookupDefinition(String name) {
		String key;
		try {
			key = stripSpecial(name);
		} catch (IllegalArgumentException ex) {
			return null;
		}
		return _definitions.get(key);
	}

	/**
	 * The file holding the values of configured aliases set in the application.
	 *
	 * <p>
	 * The file belongs to a single node of a cluster: A value stored on one node is not seen by
	 * the other nodes.
	 * </p>
	 *
	 * @return The file, existing or not, <code>null</code> if this {@link AliasManager} does not
	 *         support stored values.
	 *
	 * @see #STORED_VALUES_FILE
	 */
	public File getStoredValuesFile() {
		return null;
	}

	/**
	 * Whether values can be stored with {@link #storeValue(String, String)}.
	 *
	 * <p>
	 * This is the case, if the folder of the {@link #getStoredValuesFile() file} is a writable
	 * directory (or does not exist but can be created in an existing writable directory), and the
	 * file is writable if it exists.
	 * </p>
	 */
	public boolean canStoreValues() {
		File file = getStoredValuesFile();
		if (file == null) {
			return false;
		}
		file = file.getAbsoluteFile();
		File folder = file.getParentFile();
		if (folder.exists()) {
			if (!folder.isDirectory() || !Files.isWritable(folder.toPath())) {
				return false;
			}
			return !file.exists() || (file.isFile() && Files.isWritable(file.toPath()));
		}
		File parent = folder.getParentFile();
		return parent != null && parent.isDirectory() && Files.isWritable(parent.toPath());
	}

	/**
	 * Stores a value for the configured alias with the given name.
	 *
	 * <p>
	 * The stored value replaces the whole definition of the alias, if none of the alias'
	 * {@link AliasDefinition#getEnvironmentNames() environment names} is set. It takes effect
	 * after a restart of the application: The running configuration keeps the values it was
	 * resolved with, {@link AliasDefinition#isChangePending()} reports the pending change.
	 * </p>
	 *
	 * <p>
	 * The value is stored on this node only, see {@link #getStoredValuesFile()}.
	 * </p>
	 *
	 * @param name
	 *        The name of the alias, with or without the enclosing <code>%</code> characters.
	 * @param value
	 *        The value to store.
	 * @throws I18NRuntimeException
	 *         If the given name is not a configured alias, or the value cannot be stored.
	 */
	public synchronized void storeValue(String name, String value) {
		if (value == null) {
			throw new IllegalArgumentException("A stored value must not be null, use removeValue() instead.");
		}
		AliasDefinition definition = lookupDefinition(name);
		if (definition == null) {
			throw new I18NRuntimeException(I18NConstants.ERROR_NOT_A_CONFIGURED_VARIABLE__NAME.fill(name));
		}
		File file = checkStorable();
		try {
			Properties storedValues = loadStoredValues(file);
			storedValues.setProperty(definition.getName(), value);
			writeStoredValues(file, storedValues);
		} catch (IOException ex) {
			throw new I18NRuntimeException(I18NConstants.ERROR_STORING_VARIABLES_FAILED__FILE.fill(file.getPath()),
				ex);
		}
	}

	/**
	 * Removes the value stored for the alias with the given name.
	 *
	 * <p>
	 * As for {@link #storeValue(String, String)}, the change takes effect after a restart. The
	 * {@link #getStoredValuesFile() file} is deleted when its last value is removed.
	 * </p>
	 *
	 * @param name
	 *        The name of the alias, with or without the enclosing <code>%</code> characters. A
	 *        value stored for a name that is not (or no longer) a configured alias can be removed
	 *        as well.
	 * @throws I18NRuntimeException
	 *         If the stored values cannot be written.
	 */
	public synchronized void removeValue(String name) {
		String aliasName;
		try {
			aliasName = ALIAS_MARKER + stripSpecial(name) + ALIAS_MARKER;
		} catch (IllegalArgumentException ex) {
			throw new I18NRuntimeException(I18NConstants.ERROR_NOT_A_CONFIGURED_VARIABLE__NAME.fill(name), ex);
		}
		File file = checkStorable();
		try {
			Properties storedValues = loadStoredValues(file);
			if (storedValues.remove(aliasName) == null) {
				return;
			}
			if (storedValues.isEmpty()) {
				// No file is left behind without a stored value.
				Files.deleteIfExists(file.getAbsoluteFile().toPath());
			} else {
				writeStoredValues(file, storedValues);
			}
		} catch (IOException ex) {
			throw new I18NRuntimeException(I18NConstants.ERROR_STORING_VARIABLES_FAILED__FILE.fill(file.getPath()),
				ex);
		}
	}

	private File checkStorable() {
		File file = getStoredValuesFile();
		if (!canStoreValues()) {
			throw new I18NRuntimeException(I18NConstants.ERROR_VARIABLES_NOT_WRITABLE__FILE
				.fill(file == null ? "" : file.getAbsolutePath()));
		}
		return file;
	}

	/**
	 * Reads the {@link #getStoredValuesFile() stored values}, an unreadable file is reported and
	 * treated as empty.
	 */
	private Properties readStoredValues() {
		File file = getStoredValuesFile();
		if (file == null) {
			return new Properties();
		}
		try {
			return loadStoredValues(file);
		} catch (IOException ex) {
			Logger.error("Cannot read stored configuration variables from '" + file.getAbsolutePath() + "'.", ex,
				AliasManager.class);
			return new Properties();
		}
	}

	private static Properties loadStoredValues(File file) throws IOException {
		Properties result = new Properties();
		if (file.exists()) {
			try (InputStream in = Files.newInputStream(file.toPath());
					Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
				result.load(reader);
			}
		}
		return result;
	}

	/**
	 * Writes the given values to the given file by replacing it with a completely written
	 * temporary file.
	 */
	private static void writeStoredValues(File file, Properties values) throws IOException {
		Path target = file.getAbsoluteFile().toPath();
		Path folder = target.getParent();
		Files.createDirectories(folder);
		Path temp = Files.createTempFile(folder, STORED_VALUES_FILE, TEMP_FILE_SUFFIX);
		try {
			try (OutputStream out = Files.newOutputStream(temp);
					Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
				values.store(writer, null);
			}
			try {
				Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException ex) {
				Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
			}
		} finally {
			Files.deleteIfExists(temp);
		}
	}


	/**
	 * Quotes a replacement string for {@link Matcher#appendReplacement(StringBuffer, String)} to
	 * produce a literal replacement (without back references).
	 * 
	 * @param replacement
	 *        The literal replacement string.
	 * @return A string usable as second argument to
	 *         {@link Matcher#appendReplacement(StringBuffer, String)} creating a literal
	 *         replacement with the character of the given string.
	 */
	public static String quoteReplacement(String replacement) {
		return REPLACEMENT_QUOTE_PATTERN.matcher(replacement).replaceAll("\\\\$0");
	}

    /**
     * Returns the map containing the replacements.
     *
     * @return    The map containing the aliases.
     */
    public final Map<String, String> getDefinedAliases() {
		HashMap<String, String> result = new HashMap<>();
		for (Entry<String, String> entry : _expander.getVariables().entrySet()) {
			result.put("%" + entry.getKey() + "%", entry.getValue());
		}
		return result;
    }
    
	private void initAliasMap() {
		initAliasMap(_baseAliases, _configuredAliases);
	}

	private void initAliasMap(Map<String, String> baseAliases, Map<String, String> configuredAliases) {
		VariableExpander expander = new VariableExpander();
		if (baseAliases != null) {
			putVariables(expander, baseAliases);
		}
		if (configuredAliases != null) {
			putVariables(expander, configuredAliases);
		}
		expander.resolveRecursion();
		_expander = expander;
	}

	/**
	 * Adds all given variables to the given {@link VariableExpander}.
	 *
	 * @param expander
	 *        The expander to add the variables to.
	 * @param vars
	 *        Mapping of variable names to values. A leading <code>%</code> of a name is stripped.
	 */
	public static void putVariables(VariableExpander expander, Map<String, String> vars) {
		for (Entry<String, String> entry : vars.entrySet()) {
			putVariable(expander, entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Adds a single variable to the given {@link VariableExpander}.
	 *
	 * @param expander
	 *        The expander to add the variable to.
	 * @param key
	 *        The variable name. A leading <code>%</code> is stripped.
	 * @param value
	 *        The value of the variable.
	 */
	public static void putVariable(VariableExpander expander, String key, String value) {
		expander.addVariable(stripSpecial(key), value);
	}

	private static String stripSpecial(String key) {
		if (key.startsWith(ALIAS_MARKER)) {
			key = key.substring(1);
		}
		if (key.endsWith(ALIAS_MARKER)) {
			key = key.substring(0, key.length() - 1);
		}
		if (key.contains(ALIAS_MARKER)) {
			throw new IllegalArgumentException("Variable name must not contain '%'.");
		}
		return key;
	}

    /**
     * Singleton pattern: get the instance, lazy initialization
     *
     * @return    The only instance.
     */
    public static AliasManager getInstance () {
    	return Module.INSTANCE.getImplementationInstance();
    }
    
    /**
     * Singleton reference for {@link AliasManager} service.
     */
    public static final class Module extends BasicRuntimeModule<AliasManager> {

    	/**
		 * Singleton {@link AliasManager.Module} instance.
		 */
		public static final Module INSTANCE = new Module();
		
		private Module() {
			// Singleton constructor.
		}

		@Override
		public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
			return Arrays.asList(XMLProperties.Module.class);
		}

		/**
		 * Programmatically starts up {@link AliasManager} with a given constant set of alias
		 * definitions.
		 */
		public void startup(Map<String, String> aliasDefinitions) throws ModuleException {
			startUp(new ConstantAliasManager(aliasDefinitions));

			ModuleUtil.INSTANCE.markStarted(this);
		}

		@Override
		public Class<AliasManager> getImplementation() {
			return AliasManager.class;
		}

		@Override
		protected AliasManager newImplementationInstance() throws ModuleException {
			return new DefaultAliasManager();
		}
	}

}
