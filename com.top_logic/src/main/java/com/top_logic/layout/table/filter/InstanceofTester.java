/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;

/**
 * {@link CellExistenceTester} checking for instance of a given class.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class InstanceofTester implements CellExistenceTester {

	/**
	 * Configuration of an {@link InstanceofTester}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends PolymorphicConfiguration<InstanceofTester> {

		/** Configuration name for {@link #getType()}. */
		String TYPE = "type";

		/** Configuration name for {@link #isSupportsNull()}. */
		String SUPPORTS_NULL = "supports-null";

		/**
		 * The supported type.
		 */
		@Mandatory
		@Name(TYPE)
		Class<?> getType();

		/**
		 * Setter for {@link #getType()}.
		 */
		void setType(Class<?> type);

		/**
		 * Whether null is supported or not.
		 */
		@BooleanDefault(true)
		@Name(SUPPORTS_NULL)
		boolean isSupportsNull();

		/**
		 * Setter for {@link #isSupportsNull()}.
		 */
		void setSupportsNull(boolean b);

	}

	private final Class<?> _type;

	private final boolean _supportsNull;

	/**
	 * Creates a new {@link InstanceofTester} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link InstanceofTester}.
	 */
	public InstanceofTester(InstantiationContext context, Config config) {
		_type = config.getType();
		_supportsNull = config.isSupportsNull();
	}

	@Override
	public boolean isCellExistent(Object rowObject, String columnName) {
		if (rowObject == null) {
			return _supportsNull;
		}
		return _type.isInstance(rowObject);
	}

}

