/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
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
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
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

	private Map<String, String> _baseAliases;

	private Map<String, String> _configuredAliases;

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

		fetchConfiguredAliases();
		Map<String, String> baseAliases;
		if (ServletContextService.Module.INSTANCE.isActive()) {
			ServletContextService contextService = ServletContextService.getInstance();
			ServletContext context = contextService.getServletContext();
			String applicationPath = contextService.getApplication().getPath();

			baseAliases = new HashMap<>();
			baseAliases.put(APP_CONTEXT, context.getContextPath());
			baseAliases.put(APP_ROOT, applicationPath);
		} else {
			baseAliases = new HashMap<>();
			baseAliases.put(APP_CONTEXT, "");
			baseAliases.put(APP_ROOT, new File(ModuleLayoutConstants.WEBAPP_DIR).getAbsolutePath());
		}
		setBaseAliases(baseAliases);

		ReloadableManager.getInstance().addReloadable(this);
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
	 * Retrieves the alias definitions.
	 */
	protected abstract Map<String, String> resolveConfiguredAliases();


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
		_expander = new VariableExpander();
		putVariables(_expander, baseAliases);
		putVariables(_expander, configuredAliases);
		_expander.resolveRecursion();
	}

	public static void putVariables(VariableExpander expander, Map<String, String> vars) {
		for (Entry<String, String> entry : vars.entrySet()) {
			putVariable(expander, entry.getKey(), entry.getValue());
		}
	}

	public static void putVariable(VariableExpander expander, String key, String value) {
		expander.addVariable(stripSpecial(key), value);
	}

	private static String stripSpecial(String key) {
		if (key.startsWith("%")) {
			key = key.substring(1);
		}
		if (key.endsWith("%")) {
			key = key.substring(0, key.length() - 1);
		}
		if (key.contains("%")) {
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
