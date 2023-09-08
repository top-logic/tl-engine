/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;

/**
 * This class helps using Oracle driver 12 and higher with database version 12 and higher.
 * 
 * @author <a href=mailto:msi@top-logic.com>Marc Siebenhaar</a>
 */
public class Oracle12Helper extends OracleHelper {
    
	/**
	 * Configuration options for {@link Oracle12Helper}.
	 */
	public interface Config extends OracleHelper.Config {
		// No additional properties, just to be able to configure different application-wide
		// defaults.

		@Override
		@ClassDefault(Oracle12Helper.class)
		Class<? extends DBHelper> getImplementationClass();
	}

	/**
	 * Creates a {@link Oracle12Helper} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public Oracle12Helper(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public boolean supportsBatchInfo() {
		return true;
	}

	@Override
	public void setDate(PreparedStatement pstm, int col, Date date) throws SQLException {
		Calendar systemCalendar = getSystemCalendar();
		systemCalendar.setTime(date);
		systemCalendar.set(Calendar.HOUR_OF_DAY, 0);
		systemCalendar.set(Calendar.MINUTE, 0);
		systemCalendar.set(Calendar.SECOND, 0);
		systemCalendar.set(Calendar.MILLISECOND, 0);
		pstm.setDate(col, new java.sql.Date(systemCalendar.getTimeInMillis()), systemCalendar);
	}
}
