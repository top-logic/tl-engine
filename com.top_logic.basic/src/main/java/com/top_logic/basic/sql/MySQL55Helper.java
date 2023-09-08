/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.io.IOException;
import java.sql.Types;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;

/**
 * MySQL helper for MySQL database in version 5.5.
 * 
 * <p>
 * A special MySQL for 5.5 is needed, because MySQL 5.5 does not allow milliseconds in TIMESTAMP
 * columns, i.e. TIMESTAMP has granularity seconds.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MySQL55Helper extends MySQLHelper {

	/**
	 * {@link MySQLHelper.MySQLWithBackslashEscape} variant for MySQL 5.5. server.
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class MySQL55WithBackslashEscape extends MySQL55Helper {

		/**
		 * Creates a {@link MySQL55WithBackslashEscape} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public MySQL55WithBackslashEscape(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		protected void internalEscape(Appendable out, String str) throws IOException {
			MySQLWithBackslashEscape.backslashEscape(out, str);
		}

	}

	/**
	 * Creates a {@link MySQL55Helper} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MySQL55Helper(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected String internalGetDBType(int sqlType, boolean binary) {
		if (sqlType == Types.TIMESTAMP) {
			// fraction in datetime is not allowed in MySQL 5.5.
			return "DATETIME";
		}
		return super.internalGetDBType(sqlType, binary);
	}

}

