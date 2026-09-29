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
import com.top_logic.knowledge.monitor.UserMonitor;
import com.top_logic.knowledge.monitor.UserSession;
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
 * Read-only table of the recorded user sessions of the last {@value #WINDOW_DAYS} days, one row per
 * {@link UserSession} with the user, login and logout time, client and server. A session that is
 * still active has no logout time.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName} element).
 * </p>
 */
public class UserSessionTable implements UIElement {

	/** Number of past days of sessions to show. */
	private static final int WINDOW_DAYS = 30;

	private static final long DAY_MILLIS = 24L * 60 * 60 * 1000;

	/**
	 * Configuration for {@link UserSessionTable}.
	 */
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(UserSessionTable.class)
		Class<? extends UIElement> getImplementationClass();
	}

	/**
	 * Creates a new {@link UserSessionTable} from configuration.
	 */
	@CalledByReflection
	public UserSessionTable(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		Date to = new Date();
		Date from = new Date(to.getTime() - WINDOW_DAYS * DAY_MILLIS);
		List<UserSession> rows = new ArrayList<>(UserMonitor.userSessions(from, to));

		List<Column<UserSession, ?>> columns = new ArrayList<>();
		columns.add(textColumn("user", I18NConstants.SESSION_COLUMN_USER, UserSession::getUsername, 200));
		columns.add(textColumn("login", I18NConstants.SESSION_COLUMN_LOGIN, s -> label(s.getLogin()), 200));
		columns.add(textColumn("logout", I18NConstants.SESSION_COLUMN_LOGOUT, s -> label(s.getLogout()), 200));
		columns.add(textColumn("client", I18NConstants.SESSION_COLUMN_CLIENT, UserSession::getMachine, 160));
		columns.add(textColumn("server", I18NConstants.SESSION_COLUMN_SERVER, UserSession::getServer, 160));

		// UserSession wrappers are distinct (identity equality), so the default identity row key is
		// unique; no explicit key function is needed.
		ListRowSource<UserSession> source = new ListRowSource<>(rows, columns);
		DefaultTableView<UserSession> view = DefaultTableView.create(columns, source);
		return new TableViewControl<>(context, view, false);
	}

	/**
	 * Renders a (possibly {@code null}) value via {@link MetaLabelProvider}; {@code null} (e.g. the
	 * logout time of an active session) yields the empty string.
	 */
	private static String label(Object value) {
		return value == null ? "" : MetaLabelProvider.INSTANCE.getLabel(value);
	}

	/**
	 * A sortable, text-filterable column reading one {@link String} property of a {@link UserSession}.
	 */
	private static Column<UserSession, String> textColumn(String id, ResKey label,
			Function<? super UserSession, String> value, int width) {
		return DefaultColumn.<UserSession, String> builder(id, value)
			.label(label)
			.renderer(CellContent::text)
			.sort(() -> Comparator.<String> naturalOrder())
			.filter(new TextColumnFilter<>(text -> text))
			.width(width)
			.build();
	}
}
