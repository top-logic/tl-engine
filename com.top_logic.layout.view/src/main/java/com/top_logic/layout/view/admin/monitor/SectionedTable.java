/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.monitor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Base for read-only monitoring tables that present data as (section, name, value) rows in a
 * sortable, text-filterable green-field table.
 *
 * @implNote Subclasses provide the rows via {@link #rows()}. This base builds the three columns,
 *           assigns a stable per-row key (so value-equal rows stay individually selectable rather
 *           than collapsing under the record's value equality) and wraps the result in a
 *           {@link TableViewControl}.
 */
public abstract class SectionedTable implements UIElement {

	/** Column id for the originating section. */
	private static final String SECTION_COLUMN = "section";

	/** Column id for the entry name. */
	private static final String NAME_COLUMN = "name";

	/** Column id for the entry value. */
	private static final String VALUE_COLUMN = "value";

	/**
	 * Configuration for {@link SectionedTable}.
	 *
	 * <p>
	 * Concrete subclasses bind their implementation class via {@code @ClassDefault}.
	 * </p>
	 */
	public interface Config extends UIElement.Config {
		// No shared configuration properties.
	}

	/**
	 * Creates a new {@link SectionedTable} from configuration.
	 */
	protected SectionedTable(InstantiationContext context, Config config) {
		// No shared configuration.
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<Row> rows = rows();

		List<Column<Row, ?>> columns = new ArrayList<>();
		columns.add(textColumn(SECTION_COLUMN, I18NConstants.COLUMN_SECTION, Row::section, 200));
		columns.add(textColumn(NAME_COLUMN, I18NConstants.COLUMN_NAME, Row::name, 320));
		columns.add(textColumn(VALUE_COLUMN, I18NConstants.COLUMN_VALUE, Row::value, 480));

		// Stable per-row key by source position. Row is a value record, so the default identity key
		// (Function.identity()) would treat value-equal rows as one selection key and select them
		// together; the source position keeps every row individually selectable.
		IdentityHashMap<Row, Integer> keyByRow = new IdentityHashMap<>();
		for (int i = 0; i < rows.size(); i++) {
			keyByRow.put(rows.get(i), Integer.valueOf(i));
		}

		ListRowSource<Row> source = new ListRowSource<>(rows, columns, keyByRow::get);
		DefaultTableView<Row> view = DefaultTableView.create(columns, source);
		return new TableViewControl<>(context, view, false);
	}

	/**
	 * The rows to display, in order.
	 */
	protected abstract List<Row> rows();

	/**
	 * A sortable, text-filterable column reading one {@link String} property of a {@link Row}.
	 */
	private static Column<Row, String> textColumn(String id, ResKey label,
			Function<? super Row, String> value, int width) {
		return DefaultColumn.<Row, String> builder(id, value)
			.label(label)
			.renderer(CellContent::text)
			.sort(() -> Comparator.<String> naturalOrder())
			.filter(new TextColumnFilter<>(text -> text))
			.width(width)
			.build();
	}

	/**
	 * One table row: an originating section, an entry name and its value.
	 *
	 * @param section
	 *        The originating section.
	 * @param name
	 *        The entry name.
	 * @param value
	 *        The entry value.
	 */
	public record Row(String section, String name, String value) {
		// Accessors section()/name()/value() are generated.
	}
}
