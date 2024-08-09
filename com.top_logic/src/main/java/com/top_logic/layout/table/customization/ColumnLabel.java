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
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfigurator;

/**
 * Configures the visibility of the column label in the header.
 */
@InApp
public class ColumnLabel extends AbstractConfiguredInstance<ColumnLabel.Config<?>>
		implements ColumnConfigurator {

	/**
	 * Configuration options for {@link ColumnLabel}.
	 */
	@TagName("column-label")
	public interface Config<I extends ColumnLabel> extends PolymorphicConfiguration<I> {
		/**
		 * The label to display in the column header.
		 */
		@Label("Column label")
		ResKey getValue();
	}

	/**
	 * Creates a {@link ColumnLabel} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ColumnLabel(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void adapt(ColumnConfiguration column) {
		column.setColumnLabelKey(getConfig().getValue());
	}

}
