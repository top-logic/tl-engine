/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;

/**
 * {@link AttributeValueLocator} that performs an indexed access to a {@link List}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class IndexAccess extends Operation implements ConfiguredInstance<IndexAccess.Config> {

	/**
	 * Configuration options of {@link IndexAccess}.
	 */
	public interface Config extends PolymorphicConfiguration<IndexAccess> {

		/**
		 * The constant index in the surrounding list to access.
		 */
		@Mandatory
		int getIndex();

		/**
		 * @see #getIndex()
		 */
		void setIndex(int index);

	}

	private final int _index;

	private final Config _config;

	/**
	 * Creates a {@link IndexAccess} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public IndexAccess(InstantiationContext context, Config config) {
		_config = config;
		_index = config.getIndex();
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public Object locateAttributeValue(Object obj) {
		if (!(obj instanceof List<?>)) {
			return null;
		}

		List<?> list = (List<?>) obj;
		if (_index >= list.size()) {
			return null;
		}

		return list.get(_index);
	}

	/**
	 * Creates a {@link IndexAccess} configuration.
	 * 
	 * @param index
	 *        See {@link Config#getIndex()}.
	 */
	public static PolymorphicConfiguration<? extends AttributeValueLocator> newInstance(int index) {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		config.setIndex(index);
		return config;
	}
}