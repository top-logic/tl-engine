/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;

/**
 * {@link MapBasedMapping} with configured base map and default.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfiguredStringMapping extends MapBasedMapping<String, String> {

	/**
	 * Configuration for a {@link ConfiguredStringMapping}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<Mapping<String, String>> {

		/** Configuration name of the {@link #getEntrySet() entry set}. */
		String ENTRY_SET_NAME = "entry-set";

		/** Configuration name of the {@link #getDefaultValue()}. */
		String DEFAULT_VALUE_NAME = "default-value";

		/** Configuration name of the {@link #hasNoDefaultValue()}. */
		String NO_DEFAULT_VALUE_NAME = "no-default-value";

		/**
		 * The base map.
		 */
		@Name(ENTRY_SET_NAME)
		@MapBinding(tag = "entry", key = "key", attribute = "value")
		Map<String, String> getEntrySet();

		/**
		 * Setter for {@link #getEntrySet()}.
		 */
		void setEntrySet(Map<String, String> entrySet);

		/**
		 * The default value when the key is not contained in {@link #getEntrySet()}.
		 * 
		 * <p>
		 * Value is ignored when {@link #hasNoDefaultValue()} is set.
		 * </p>
		 * 
		 * @see #hasNoDefaultValue()
		 */
		@Name(DEFAULT_VALUE_NAME)
		@Nullable
		String getDefaultValue();

		/**
		 * Setter for {@link #getDefaultValue()}.
		 */
		void setDefaultValue(String defaultValue);

		/**
		 * Use input as output when the key is not contained in {@link #getEntrySet()}
		 * 
		 * @see #getDefaultValue()
		 */
		@BooleanDefault(false)
		@Name(NO_DEFAULT_VALUE_NAME)
		boolean hasNoDefaultValue();

		/**
		 * Setter for {@link #hasNoDefaultValue()}.
		 */
		void setNoDefaultValue(boolean b);

		@Override
		@ClassDefault(ConfiguredStringMapping.class)
		Class<? extends Mapping<String, String>> getImplementationClass();

	}

	/** @see #getDefault(String) */
	private final Config _config;

	/**
	 * Creates a {@link ConfiguredStringMapping} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConfiguredStringMapping(InstantiationContext context, Config config) {
		super(config.getEntrySet());
		_config = config;
	}

	@Override
	protected String getDefault(String input) {
		if (_config.hasNoDefaultValue()) {
			return input;
		}
		return _config.getDefaultValue();
	}

}
