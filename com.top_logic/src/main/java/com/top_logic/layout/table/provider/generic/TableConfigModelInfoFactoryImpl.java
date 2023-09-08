/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider.generic;

import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.model.TLClass;

/**
 * {@link TableConfigModelInfoFactory} that creates {@link TableConfigModelInfoImpl}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TableConfigModelInfoFactoryImpl
		implements TableConfigModelInfoFactory, ConfiguredInstance<TableConfigModelInfoFactoryImpl.Config> {

	/** The {@link PolymorphicConfiguration} of the {@link TableConfigModelInfoFactoryImpl}. */
	public interface Config extends PolymorphicConfiguration<TableConfigModelInfoFactoryImpl> {

		// Nothing needed, yet.

	}

	private final Config _config;

	/**
	 * Called by the {@link TypedConfiguration} for creating a
	 * {@link TableConfigModelInfoFactoryImpl}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public TableConfigModelInfoFactoryImpl(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public TableConfigModelInfo create(Set<? extends TLClass> contentTypes) {
		return new TableConfigModelInfoImpl(contentTypes);
	}

}
