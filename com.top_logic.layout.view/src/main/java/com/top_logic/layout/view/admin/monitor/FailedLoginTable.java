/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.monitor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.monitor.FailedLogin;
import com.top_logic.layout.provider.MetaLabelProvider;
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
 * Read-only table of the failed login attempts of the last {@value #WINDOW_DAYS} days, one row per
 * {@link FailedLogin} with the time, attempted user, client IP, server and failure reason.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName} element).
 * </p>
 */
public class FailedLoginTable implements UIElement {

	/** Number of past days of failed logins to show. */
	private static final int WINDOW_DAYS = 30;

	private static final long DAY_MILLIS = 24L * 60 * 60 * 1000;

	/**
	 * Configuration for {@link FailedLoginTable}.
	 */
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(FailedLoginTable.class)
		Class<? extends UIElement> getImplementationClass();
	}

	/**
	 * Creates a new {@link FailedLoginTable} from configuration.
	 */
	@CalledByReflection
	public FailedLoginTable(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		Date to = new Date();
		Date from = new Date(to.getTime() - WINDOW_DAYS * DAY_MILLIS);
		List<FailedLogin> rows = new ArrayList<>(FailedLogin.getFailedLogins(from, to));

		List<Column<FailedLogin, ?>> columns = new ArrayList<>();
		columns.add(column("time", I18NConstants.FAILED_LOGIN_COLUMN_TIME, FailedLogin.DATE, 200));
		columns.add(column("user", I18NConstants.FAILED_LOGIN_COLUMN_USER, FailedLogin.USER_NAME, 200));
		columns.add(column("ip", I18NConstants.FAILED_LOGIN_COLUMN_IP, FailedLogin.CLIENT_IP, 160));
		columns.add(column("server", I18NConstants.FAILED_LOGIN_COLUMN_SERVER, FailedLogin.SERVER, 160));
		columns.add(column("reason", I18NConstants.FAILED_LOGIN_COLUMN_REASON, FailedLogin.REASON, 200));

		// FailedLogin wrappers are distinct (identity equality), so the default identity row key is
		// unique; no explicit key function is needed.
		ListRowSource<FailedLogin> source = new ListRowSource<>(rows, columns);
		DefaultTableView<FailedLogin> view = DefaultTableView.create(columns, source);
		return new TableViewControl<>(context, view, false);
	}

	/**
	 * A sortable, text-filterable column reading one attribute of a {@link FailedLogin}, rendered via
	 * {@link MetaLabelProvider} (so the date attribute is formatted for the current locale).
	 */
	private static Column<FailedLogin, String> column(String id, ResKey label, String attribute, int width) {
		Function<FailedLogin, String> value = login -> {
			Object raw = login.tValueByName(attribute);
			return raw == null ? "" : MetaLabelProvider.INSTANCE.getLabel(raw);
		};
		return DefaultColumn.<FailedLogin, String> builder(id, value)
			.label(label)
			.renderer(CellContent::text)
			.sort(() -> Comparator.<String> naturalOrder())
			.filter(new TextColumnFilter<>(text -> text))
			.width(width)
			.build();
	}
}
