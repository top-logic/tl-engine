/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage.mappings;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.internal.AbstractConfiguredStorageMapping;

/**
 * {@link StorageMapping} that uses identical values at the database and application layer.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DirectMapping<T> extends AbstractConfiguredStorageMapping<DirectMapping.Config<?>, T> {

	private final Class<T> _applicationType;

	/**
	 * Configuration options for {@link DirectMapping}.
	 */
	public interface Config<T> extends PolymorphicConfiguration<StorageMapping<T>> {

		/** @see #getApplicationType() */
		String APPLICATION_TYPE = "application-type";

		/**
		 * The application value class.
		 */
		@Name(APPLICATION_TYPE)
		@Mandatory
		Class<T> getApplicationType();

	}

	/**
	 * Creates a {@link DirectMapping} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DirectMapping(InstantiationContext context, Config<T> config) {
		super(context, config);
		_applicationType = config.getApplicationType();
	}

	@Override
	public Class<T> getApplicationType() {
		return _applicationType;
	}

	@Override
	public T getBusinessObject(Object aStorageObject) {
		return _applicationType.cast(aStorageObject);
	}

	@Override
	public Object getStorageObject(Object aBusinessObject) {
		return _applicationType.cast(aBusinessObject);
	}

	@Override
	public boolean isCompatible(Object businessObject) {
		return businessObject == null || _applicationType.isInstance(businessObject);
	}

}
