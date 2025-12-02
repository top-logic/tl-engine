/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.changelog.model.ChangeSet;
import com.top_logic.layout.table.RowClassProvider;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;

/**
 * {@link RowClassProvider} for a table that displays the application's change log.
 */
@Label("CSS classes of the change log row")
public class ChangeLogRowClassProvider extends AbstractConfiguredInstance<ChangeLogRowClassProvider.Config<?>>
		implements RowClassProvider, TableConfigurationProvider {

	/**
	 * Configuration for {@link ChangeLogRowClassProvider}.
	 */
	public interface Config<I extends ChangeLogRowClassProvider> extends PolymorphicConfiguration<I> {

		/**
		 * CSS class for the rows that display a change set that was reverted.
		 */
		@StringDefault("change-reverted")
		@Nullable
		@Label("CSS class for reverted changes")
		String getRevertedCSSClass();
	}

	/**
	 * Creates a {@link ChangeLogRowClassProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ChangeLogRowClassProvider(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		table.setRowClassProvider(this);
	}

	@Override
	public String getTRClass(TableControl view, int rowOptions, int displayedRow, int row) {
		Object rowObj = view.getViewModel().getRowObject(row);
		if (rowObj instanceof ChangeSet cs) {
			ChangeSet revertedBy = cs.getRevertedBy();
			if (revertedBy != null) {
				return getConfig().getRevertedCSSClass();
			}

		}
		return null;
	}
}
