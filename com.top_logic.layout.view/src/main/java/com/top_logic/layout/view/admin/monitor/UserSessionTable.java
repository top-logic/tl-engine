/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.monitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.monitor.UserMonitor;
import com.top_logic.knowledge.monitor.UserSession;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.admin.system.LogFunctions;
import com.top_logic.table.Column;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortSpec;

/**
 * Read-only table of the recorded user sessions begun within a time range, one row per
 * {@link UserSession} with the user, login and logout time, client and server, and the mark the
 * log entries of the session carry. A session that is still active has no logout time.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName}
 * element). The rows are the newest sessions first until the user sorts the table.
 * </p>
 *
 * @see LogFunctions#sessionMark(UserSession)
 */
public class UserSessionTable extends TimeRangeTable<UserSession> {

	/** Name of the column of the login time. */
	public static final String LOGIN = "login";

	/**
	 * Configuration for {@link UserSessionTable}.
	 */
	public interface Config extends TimeRangeTable.Config {

		@Override
		@ClassDefault(UserSessionTable.class)
		Class<? extends UIElement> getImplementationClass();
	}

	/**
	 * Creates a new {@link UserSessionTable} from configuration.
	 */
	@CalledByReflection
	public UserSessionTable(InstantiationContext context, Config config) {
		super(config);
	}

	@Override
	protected List<Column<UserSession, ?>> columns() {
		List<Column<UserSession, ?>> columns = new ArrayList<>();
		columns.add(MonitorTables.textColumn("user", I18NConstants.SESSION_COLUMN_USER, UserSession::getUsername, 200));
		columns.add(MonitorTables.dateColumn(LOGIN, I18NConstants.SESSION_COLUMN_LOGIN, UserSession::getLogin, 170));
		columns.add(MonitorTables.dateColumn("logout", I18NConstants.SESSION_COLUMN_LOGOUT, UserSession::getLogout, 170));
		columns.add(MonitorTables.textColumn("client", I18NConstants.SESSION_COLUMN_CLIENT, UserSession::getMachine, 160));
		columns.add(MonitorTables.textColumn("server", I18NConstants.SESSION_COLUMN_SERVER, UserSession::getServer, 160));
		columns.add(MonitorTables.textColumn("session", I18NConstants.SESSION_COLUMN_LOG_MARK,
			LogFunctions::sessionMark, 420));
		return columns;
	}

	@Override
	protected SortSpec defaultSort() {
		return new SortSpec(List.of(new SortColumn(LOGIN, false)));
	}

	@Override
	protected List<UserSession> records(Date start, Date end) {
		// UserSession wrappers are distinct (identity equality), so the default identity row key
		// is unique; no explicit key function is needed.
		return UserMonitor.userSessions(start, end);
	}
}
