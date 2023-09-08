/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.config.copy.ConfigCopier;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.vars.IVariableExpander;
import com.top_logic.basic.vars.VariableExpander;

/**
 * Super class for reader de-serializing {@link ConfigurationItem}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractConfigurationReader {

	/** Context to instantiate items. */
	protected InstantiationContext _context;

	/** Source definitions to read serialised config from. */
	protected List<? extends Content> _sources;

	/** Base configuration to adapt. */
	protected ConfigurationItem _baseConfig;

	private boolean _read = false;

	/** {@link VariableExpander} for expanding variable in content sources. */
	protected IVariableExpander _expander = null;

	/**
	 * Creates a new {@link AbstractConfigurationReader}.
	 */
	public AbstractConfigurationReader(InstantiationContext context) {
		_context = context;
	}

	/**
	 * The given {@link Content}s are the sources from which the configuration should be parsed.
	 * <p>
	 * Exactly one of the Methods {@link #setSources(List)}, {@link #setSources(List)},
	 * {@link #setSources(Content...)}, {@link #setSource(Content)} and {@link #setSource(Content)}
	 * has to be called, before {@link #read()} or {@link #readConfigBuilder()} can be called. <br/>
	 * If a base config has been set with {@link #setBaseConfig(ConfigurationItem)}, it is used (as
	 * base config) to parse the first source of the given list. The second source uses the first
	 * source as base config. And the third source uses the second source and so on.
	 * </p>
	 * 
	 * @param sources
	 *        Is not allowed to be <code>null</code>. Is allowed to be empty. Is not allowed to
	 *        contain <code>null</code>.
	 * @return This {@link AbstractConfigurationReader}, for convenience for method chaining.
	 */
	public AbstractConfigurationReader setSources(List<? extends Content> sources) {
		checkSourcesNotSet();
		checkSourcesNotNull(sources);
		_sources = new ArrayList<Content>(sources);
		return this;
	}

	private void checkSourcesNotNull(List<?> sources) {
		if (sources == null) {
			throw new NullPointerException("Config sources are not allowed to be null.");
		}
		if (sources.contains(null)) {
			throw new NullPointerException("Config sources are not allowed to contain null.");
		}
	}

	/**
	 * The given {@link CharacterContent}s are the sources from which the configuration should be
	 * parsed.
	 * <p>
	 * Exactly one of the Methods {@link #setSources(List)}, {@link #setSources(List)},
	 * {@link #setSources(Content...)}, {@link #setSource(Content)} and {@link #setSource(Content)}
	 * has to be called, before {@link #read()} or {@link #readConfigBuilder()} can be called. <br/>
	 * If a base config has been set with {@link #setBaseConfig(ConfigurationItem)}, it is used (as
	 * base config) to parse the first source of the given list. The second source uses the first
	 * source as base config. And the third source uses the second source and so on.
	 * </p>
	 * 
	 * @param sources
	 *        Is not allowed to be <code>null</code>. Is allowed to be empty. Is not allowed to
	 *        contain <code>null</code>.
	 * @return This {@link AbstractConfigurationReader}, for convenience for method chaining.
	 */
	public AbstractConfigurationReader setSources(Content... sources) {
		checkSourcesNotSet();
		checkSourcesNotNull(sources);
		_sources = new ArrayList<>(Arrays.asList(sources));
		return this;
	}

	private void checkSourcesNotNull(Object[] sources) {
		if (sources == null) {
			throw new NullPointerException("Config sources are not allowed to be null.");
		}
		if (ArrayUtil.contains(sources, null)) {
			throw new NullPointerException("Config sources are not allowed to contain null.");
		}
	}

	/**
	 * The given {@link Content} is the source from which the configuration should be parsed.
	 * <p>
	 * Exactly one of the Methods {@link #setSources(List)}, {@link #setSources(List)},
	 * {@link #setSources(Content...)}, {@link #setSource(Content)} and {@link #setSource(Content)}
	 * has to be called, before {@link #read()} or {@link #readConfigBuilder()} can be called. <br/>
	 * If a base config has been set with {@link #setBaseConfig(ConfigurationItem)}, it is used (as
	 * base config) to parse the given source.
	 * </p>
	 * 
	 * @param source
	 *        Is not allowed to be <code>null</code>.
	 * @return This {@link AbstractConfigurationReader}, for convenience for method chaining.
	 */
	public AbstractConfigurationReader setSource(Content source) {
		checkSourcesNotSet();
		checkSourceNotNull(source);
		_sources = Collections.singletonList(source);
		return this;
	}

	private void checkSourceNotNull(Object source) {
		if (source == null) {
			throw new NullPointerException("Config source is not allowed to be null.");
		}
	}

	private void checkSourcesNotSet() {
		if (_sources != null) {
			throw new IllegalStateException("Config sources are already set.");
		}
	}

	/**
	 * Set the given {@link ConfigurationItem} as base / fallback config.
	 * <p>
	 * Is not allowed to be called multiple times.
	 * </p>
	 * 
	 * @param baseConfig
	 *        Is allowed to be <code>null</code>. Is allowed to be a {@link ConfigBuilder}.
	 * @return This {@link AbstractConfigurationReader}, for convenience for method chaining.
	 */
	public AbstractConfigurationReader setBaseConfig(ConfigurationItem baseConfig) {
		checkBaseConfigNotSet();
		_baseConfig = (baseConfig == null) ? null : ConfigCopier.copyBuilder(baseConfig, _context);
		return this;
	}

	private void checkBaseConfigNotSet() {
		if (_baseConfig != null) {
			throw new IllegalStateException("Base configuration is already set.");
		}
	}

	/**
	 * Resolve variables during reading the configuration.
	 * 
	 * @param expander
	 *        The {@link IVariableExpander} to use.
	 */
	public AbstractConfigurationReader setVariableExpander(IVariableExpander expander) {
		_expander = expander;
		return this;
	}

	/**
	 * Reads the given sources and returns the {@link ConfigurationItem}.
	 * <p>
	 * Exactly one of the Methods {@link #setSources(List)}, {@link #setSources(List)},
	 * {@link #setSources(Content...)}, {@link #setSource(Content)} and {@link #setSource(Content)}
	 * has to be called, before {@link #read()} or {@link #readConfigBuilder()} can be called. <br/>
	 * Only one of the methods {@link #read()} and {@link #readConfigBuilder()} is allowed to be
	 * called, and only once. <br/>
	 * </p>
	 * 
	 * @return Never <code>null</code>.
	 */
	public ConfigurationItem read() throws ConfigurationException {
		checkNotRead();
		_read = true;
		return read(true);
	}

	/**
	 * Reads the given sources and returns the {@link ConfigBuilder}, not the
	 * {@link ConfigurationItem}.
	 * <p>
	 * Exactly one of the Methods {@link #setSources(List)}, {@link #setSources(List)},
	 * {@link #setSources(Content...)}, {@link #setSource(Content)} and {@link #setSource(Content)}
	 * has to be called, before {@link #read()} or {@link #readConfigBuilder()} can be called. <br/>
	 * Only one of the methods {@link #read()} and {@link #readConfigBuilder()} is allowed to be
	 * called, and only once. <br/>
	 * This method is required if the stacking of the configuration sources should not be done by
	 * the {@link AbstractConfigurationReader}, but by the caller.
	 * </p>
	 * 
	 * @return Never <code>null</code>.
	 */
	public ConfigBuilder readConfigBuilder() throws ConfigurationException {
		checkNotRead();
		_read = true;
		return (ConfigBuilder) read(false);
	}

	private ConfigurationItem read(boolean buildConfigItem) throws ConfigurationException {
		if (_sources == null) {
			throw new IllegalStateException("Config sources have not been set.");
		}
		ConfigurationItem result;
		if (_sources.isEmpty()) {
			result = _baseConfig;
		} else {
			result = readInternal();
		}
		checkSuccess();
		if (result != null && buildConfigItem) {
			result = ((ConfigBuilder) result).createConfig(_context);
			checkSuccess();
		}
		return result;
	}

	private void checkSuccess() throws ConfigurationException {
		_context.checkErrors();
	}

	private ConfigurationItem readInternal() throws ConfigurationException {
		ConfigurationItem currentResult = _baseConfig;
		for (Content source : _sources) {
			currentResult = readInternal(source, currentResult);
			if (currentResult == null) {
				// Something went wrong.
				// This will cause an error to be logged to the protocol,
				// which is checked outside of this method.
				return null;
			}
		}
		return currentResult;
	}

	/**
	 * Reads the {@link ConfigurationItem} serialized in the given source.
	 * 
	 * @param source
	 *        The source to read serialized item from.
	 * @param baseItem
	 *        The base configuration to adapt. May be <code>null</code>.
	 */
	protected abstract ConfigurationItem readInternal(Content source, ConfigurationItem baseItem)
			throws ConfigurationException;

	private void checkNotRead() {
		if (_read) {
			throw new IllegalStateException("One of the \"read\" methods has already been called.");
		}
	}

	/**
	 * Copies the values (not deep) from the source configuration to the target configuration.
	 */
	protected static void moveValues(ConfigurationItem sourceConfig, ConfigBuilder targetConfig) {
		ConfigurationDescriptor oldType = sourceConfig.descriptor();
		for (PropertyDescriptor newProperty : targetConfig.descriptor().getProperties()) {
			if (targetConfig.valueSet(newProperty)) {
				// Note: The class attribute may have been already set in the new builder.
				continue;
			}
			if (!oldType.hasProperty(newProperty.getPropertyName())) {
				continue;
			}
			PropertyDescriptor oldProperty = oldType.getProperty(newProperty.getPropertyName());
			if (!newProperty.identifier().equals(oldProperty.identifier())) {
				continue;
			}
			if (!sourceConfig.valueSet(oldProperty)) {
				continue;
			}

			// Move value from the source to the target configuration.
			Object value = sourceConfig.value(oldProperty);
			if (oldProperty.kind() != PropertyKind.PLAIN) {
				// A item-valued property must only be child of a single parent. Therefore, it
				// must be removed from its old parent before it can be added to a new parent.
				// Plain properties are not reset to allow applying updates to invalid
				// configurations, where one item is child of multiple parents. Such a situation
				// occurs when applying component overlays. To fix the configuration structure
				// after applying the update, at least the name property in the base
				// configuration is required to identify the configuration item that must be
				// exchanged manually.
				sourceConfig.reset(oldProperty);
			}
			targetConfig.update(newProperty, value);
		}
	}

}
