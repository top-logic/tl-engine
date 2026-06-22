/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.view.UIElement;
import com.top_logic.table.ColumnFilter;

/**
 * Backward-compatible {@code <table>} tag for a model-defined React table.
 *
 * <p>
 * {@code <table>} is an alias for {@link TableViewElement the green-field <table-view>} element: both
 * tags instantiate {@link TableViewElement}, so a {@code <table>} renders through the green-field
 * {@link com.top_logic.table.TableView} stack and is sortable and (per-column) filterable like any
 * {@code <table-view>}. The tag is retained so existing views need not be rewritten; new views
 * should prefer {@code <table-view>}.
 * </p>
 *
 * <p>
 * This class is not itself a {@link UIElement} - it only hosts the {@code <table>} {@link Config} and
 * the shared {@link ColumnsConfig}/{@link ColumnConfig} column configuration reused by
 * {@link TableViewElement}.
 * </p>
 */
public final class TableElement {

	private TableElement() {
		// Configuration holder, not instantiable.
	}

	/**
	 * Configuration for the {@code <table>} tag, an alias of {@link TableViewElement.Config}.
	 */
	@TagName("table")
	public interface Config extends TableViewElement.Config {

		@Override
		@ClassDefault(TableViewElement.class)
		Class<? extends UIElement> getImplementationClass();

	}

	/**
	 * Container for the list of {@link ColumnConfig}s of a table element.
	 */
	public interface ColumnsConfig extends ConfigurationItem {

		/**
		 * The columns to display, in display order.
		 */
		@DefaultContainer
		List<ColumnConfig> getColumns();
	}

	/**
	 * Configuration for a single displayed column of a table element.
	 */
	@TagName("column")
	public interface ColumnConfig extends ConfigurationItem {

		/** Configuration name for {@link #getAttribute()}. */
		String ATTRIBUTE = "attribute";

		/** Configuration name for {@link #getFilter()}. */
		String FILTER = "filter";

		/**
		 * The name of the attribute (column) to display.
		 */
		@Name(ATTRIBUTE)
		@Mandatory
		String getAttribute();

		/**
		 * An optional application-defined filter for this column, overriding the type-derived
		 * default. The filter matches against the cell's display text.
		 */
		@Name(FILTER)
		PolymorphicConfiguration<? extends ColumnFilter<?>> getFilter();
	}

}
