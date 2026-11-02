/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.admin.monitor.SectionedTable;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.Resources;

/**
 * Read-only table of the {@link DatabaseState state of the database} of the application: the
 * database product and the range of revisions its history still holds.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName} element).
 * The state is read when the table is created and on every change of its
 * {@link SectionedTable.Config#getRefresh() refresh channel}.
 * </p>
 */
public class DatabaseStateTable extends SectionedTable {

	/**
	 * Configuration for {@link DatabaseStateTable}.
	 */
	public interface Config extends SectionedTable.Config {

		@Override
		@ClassDefault(DatabaseStateTable.class)
		Class<? extends UIElement> getImplementationClass();
	}

	/**
	 * Creates a new {@link DatabaseStateTable} from configuration.
	 */
	@CalledByReflection
	public DatabaseStateTable(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected List<Row> rows() {
		DatabaseState state = DatabaseState.of(PersistencyLayer.getKnowledgeBase());

		Resources resources = Resources.getInstance();
		String database = resources.getString(I18NConstants.DATABASE_SECTION_DATABASE);
		String history = resources.getString(I18NConstants.DATABASE_SECTION_HISTORY);

		List<Row> rows = new ArrayList<>();
		addRow(rows, database, I18NConstants.DATABASE_PRODUCT, state.product());
		addRow(rows, database, I18NConstants.DATABASE_PRODUCT_VERSION, state.productVersion());
		addRow(rows, database, I18NConstants.DATABASE_DRIVER, state.driver());
		addRow(rows, database, I18NConstants.DATABASE_SQL_DIALECT, state.sqlDialect());

		if (state.lastRevision() == DatabaseState.NO_HISTORY) {
			addRow(rows, history, I18NConstants.DATABASE_NO_HISTORY, "");
		} else {
			addRow(rows, history, I18NConstants.DATABASE_LAST_REVISION, Long.toString(state.lastRevision()));
			addRow(rows, history, I18NConstants.DATABASE_LAST_REVISION_DATE, label(state.lastRevisionDate()));
			addRow(rows, history, I18NConstants.DATABASE_FIRST_REVISION, Long.toString(state.firstRevision()));
			addRow(rows, history, I18NConstants.DATABASE_FIRST_REVISION_DATE, label(state.firstRevisionDate()));
		}
		return rows;
	}

	private static void addRow(List<Row> rows, String section, ResKey name, String value) {
		rows.add(new Row(section, Resources.getInstance().getString(name), value == null ? "" : value));
	}

	private static String label(Date date) {
		return date == null ? "" : HTMLFormatter.getInstance().formatDateTime(date);
	}
}
