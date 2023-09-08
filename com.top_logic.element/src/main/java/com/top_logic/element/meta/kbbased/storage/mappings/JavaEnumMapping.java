/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage.mappings;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.EnumerationNameMapping;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.internal.AbstractConfiguredStorageMapping;

/**
 * {@link StorageMapping} for storing Java {@link Enum} values in model attributes.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JavaEnumMapping<E extends Enum<E>, C extends JavaEnumMapping.Config<?>>
		extends AbstractConfiguredStorageMapping<C, E> {

	/**
	 * Configuration options for {@link JavaEnumMapping}.
	 */
	@TagName("enum-storage")
	public interface Config<I extends JavaEnumMapping<?, ?>> extends PolymorphicConfiguration<I> {

		/**
		 * The Java {@link Enum} type to store.
		 */
		Class<? extends Enum<?>> getEnum();

	}

	private Class<? extends E> _enumType;

	/**
	 * Creates a {@link JavaEnumMapping} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public JavaEnumMapping(InstantiationContext context, C config) {
		super(context, config);

		@SuppressWarnings("unchecked")
		Class<? extends E> enumType = (Class<? extends E>) getConfig().getEnum();
		_enumType = enumType;
	}

	@Override
	public Class<? extends E> getApplicationType() {
		return _enumType;
	}

	@Override
	public E getBusinessObject(Object aStorageObject) {
		String name = (String) aStorageObject;
		if (StringServices.isEmpty(name)) {
			return null;
		}
		try {
			return ConfigUtil.getEnum(_enumType, name);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	@Override
	public Object getStorageObject(Object aBusinessObject) {
		if (aBusinessObject == null) {
			return null;
		}
		return EnumerationNameMapping.INSTANCE.map(((Enum<?>) aBusinessObject));
	}

	@Override
	public boolean isCompatible(Object businessObject) {
		return businessObject == null || getApplicationType().isInstance(businessObject);
	}

}
