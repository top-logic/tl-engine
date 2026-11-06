/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.monitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.monitor.FailedLogin;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.view.UIElement;
import com.top_logic.table.Column;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortSpec;

/**
 * Read-only table of the failed login attempts within a time range, one row per
 * {@link FailedLogin} with the time, attempted user, client IP, server and failure reason.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName}
 * element). The rows are the newest attempts first until the user sorts the table.
 * </p>
 */
public class FailedLoginTable extends TimeRangeTable<FailedLogin> {

	/** Name of the column of the time of the attempt. */
	public static final String TIME = "time";

	/**
	 * Configuration for {@link FailedLoginTable}.
	 */
	public interface Config extends TimeRangeTable.Config {

		@Override
		@ClassDefault(FailedLoginTable.class)
		Class<? extends UIElement> getImplementationClass();
	}

	/**
	 * Creates a new {@link FailedLoginTable} from configuration.
	 */
	@CalledByReflection
	public FailedLoginTable(InstantiationContext context, Config config) {
		super(config);
	}

	@Override
	protected List<Column<FailedLogin, ?>> columns() {
		List<Column<FailedLogin, ?>> columns = new ArrayList<>();
		columns.add(MonitorTables.dateColumn(TIME, I18NConstants.FAILED_LOGIN_COLUMN_TIME,
			login -> (Date) login.tValueByName(FailedLogin.DATE), 170));
		columns.add(column("user", I18NConstants.FAILED_LOGIN_COLUMN_USER, FailedLogin.USER_NAME, 200));
		columns.add(column("ip", I18NConstants.FAILED_LOGIN_COLUMN_IP, FailedLogin.CLIENT_IP, 160));
		columns.add(column("server", I18NConstants.FAILED_LOGIN_COLUMN_SERVER, FailedLogin.SERVER, 160));
		columns.add(column("reason", I18NConstants.FAILED_LOGIN_COLUMN_REASON, FailedLogin.REASON, 200));
		return columns;
	}

	@Override
	protected SortSpec defaultSort() {
		return new SortSpec(List.of(new SortColumn(TIME, false)));
	}

	@Override
	protected List<FailedLogin> records(Date start, Date end) {
		// FailedLogin wrappers are distinct (identity equality), so the default identity row key
		// is unique; no explicit key function is needed.
		return FailedLogin.getFailedLogins(start, end);
	}

	/**
	 * A text column of one attribute of a {@link FailedLogin}, rendered via
	 * {@link MetaLabelProvider}.
	 */
	private static Column<FailedLogin, String> column(String id, ResKey label, String attribute, int width) {
		Function<FailedLogin, String> value = login -> {
			Object raw = login.tValueByName(attribute);
			return raw == null ? "" : MetaLabelProvider.INSTANCE.getLabel(raw);
		};
		return MonitorTables.textColumn(id, label, value, width);
	}
}
