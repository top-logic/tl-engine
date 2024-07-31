/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.customization;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.table.CellClassProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfigurator;

/**
 * Configures a function computing dynamic CSS classes to add to a column's cells based on it's
 * content.
 */
@InApp
public class DynamicCssClasses extends AbstractConfiguredInstance<DynamicCssClasses.Config<?>>
		implements ColumnConfigurator {

	/**
	 * Configuration options for {@link DynamicCssClasses}.
	 */
	@TagName("dynamic-css-classes")
	public interface Config<I extends DynamicCssClasses> extends PolymorphicConfiguration<I> {
		/**
		 * Algorithm to compute dynamic CSS classs to add to certain cells of a column.
		 */
		@Mandatory
		@Options(fun = AllInAppImplementations.class)
		@DefaultContainer
		PolymorphicConfiguration<? extends CellClassProvider> getCssClassProvider();
	}

	private final CellClassProvider _provider;

	/**
	 * Creates a {@link DynamicCssClasses} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DynamicCssClasses(InstantiationContext context, Config<?> config) {
		super(context, config);
		_provider = context.getInstance(config.getCssClassProvider());
	}

	@Override
	public void adapt(ColumnConfiguration column) {
		column.setCssClassProvider(_provider);
	}

}
