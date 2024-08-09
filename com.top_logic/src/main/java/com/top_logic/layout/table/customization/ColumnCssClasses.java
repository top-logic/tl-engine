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
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.table.CellClassProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfigurator;
import com.top_logic.layout.table.model.WithColumnCssClasses;

/**
 * Configures CSS classes to add to a column's cells.
 * 
 * @see ColumnCustomizations
 */
@InApp
public class ColumnCssClasses extends AbstractConfiguredInstance<ColumnCssClasses.Config<?>>
		implements ColumnConfigurator {

	/**
	 * Configuration options for {@link ColumnCssClasses}.
	 */
	@DisplayOrder({
		Config.CSS_HEADER_CLASS,
		Config.CSS_CLASS,
		Config.CSS_CLASS_GROUP_FIRST,
		Config.CSS_CLASS_GROUP_LAST,
		Config.CSS_CLASS_PROVIDER,
	})
	@TagName("css-classes")
	public interface Config<I extends ColumnCssClasses> extends PolymorphicConfiguration<I>, WithColumnCssClasses {
		// Pure sum interface.
	}

	private final CellClassProvider _provider;

	/**
	 * Creates a {@link ColumnCssClasses} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ColumnCssClasses(InstantiationContext context, Config<?> config) {
		super(context, config);

		_provider = context.getInstance(config.getCssClassProvider());
	}

	@Override
	public void adapt(ColumnConfiguration column) {
		column.addCssClass(getConfig().getCssClass());
		column.addCssHeaderClass(getConfig().getCssHeaderClass());
		if (_provider != null) {
			column.setCssClassProvider(_provider);
		}
	}

}
