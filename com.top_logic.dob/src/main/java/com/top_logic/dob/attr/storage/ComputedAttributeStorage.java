/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr.storage;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;

/**
 * {@link AbstractComputedAttributeStorage} that has no storage slot for the value, but directly
 * dispatched to a {@link CacheValueFactory}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ComputedAttributeStorage extends AbstractComputedAttributeStorage
		implements ConfiguredInstance<ComputedAttributeStorage.Config> {

	/**
	 * Configuration for an {@link AbstractComputedAttributeStorage}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends PolymorphicConfiguration<ComputedAttributeStorage> {

		/** Name of the configuration of {@link #getValueFactory()}. */
		String VALUE_FACTORY_NAME = "value-factory";

		/**
		 * The factory that actually creates the value for the {@link AttributeStorage}.
		 */
		@Mandatory
		@InstanceFormat
		@Name(VALUE_FACTORY_NAME)
		CacheValueFactory getValueFactory();

		/**
		 * Sets value of {@link #getValueFactory()}.
		 */
		void setValueFactory(CacheValueFactory fetch);

	}

	private final CacheValueFactory _valueFactory;

	private Config _config;

	/**
	 * Creates a new {@link ComputedAttributeStorage} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link ComputedAttributeStorage}.
	 */
	public ComputedAttributeStorage(InstantiationContext context, Config config) {
		this(config.getValueFactory());
		_config = config;
	}

	/**
	 * Creates a new {@link ComputedAttributeStorage}.
	 * 
	 * @param factory
	 *        The {@link CacheValueFactory} to dispatch getting cache value to.
	 */
	public ComputedAttributeStorage(CacheValueFactory factory) {
		_valueFactory = factory;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public int getCacheSize() {
		return 0;
	}

	@Override
	public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
		return getValueFactory().getCacheValue(attribute, item, storage);
	}

	/**
	 * The {@link CacheValueFactory} that is used to fetch the cache value from.
	 */
	public CacheValueFactory getValueFactory() {
		return _valueFactory;
	}

}

