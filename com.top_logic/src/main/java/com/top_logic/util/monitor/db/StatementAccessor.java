/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor.db;

import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.sql.LoggingDataSourceProxy.Statistics;
import com.top_logic.layout.ReadOnlyAccessor;

/**
 * Accessors of various aspects of a {@link Statistics} value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StatementAccessor extends ReadOnlyAccessor<Entry<String, Statistics>> {

	@Override
	public Object getValue(Entry<String, Statistics> object, String property) {
		return object.getKey();
	}

	/**
	 * Extracts the primary accessed table from the SQL.
	 */
	public static class Table extends ReadOnlyAccessor<Entry<String, Statistics>> {
		Pattern _pattern =
			Pattern.compile(
				"(?:(?:\\bfrom\\b)|(?:\\binto\\b)|(?:\\bupdate\\b))\\s+(?:'([^']+)'|\"([^\"]+)\"|([^\\s]+))",
				Pattern.CASE_INSENSITIVE);

		@Override
		public Object getValue(Entry<String, Statistics> object, String property) {
			String sql = object.getKey();
			Matcher matcher = _pattern.matcher(sql);
			if (matcher.find()) {
				String squoted = matcher.group(1);
				if (squoted != null) {
					return squoted;
				}
				String dquoted = matcher.group(2);
				if (dquoted != null) {
					return dquoted;
				}
				String unquoted = matcher.group(3);
				if (unquoted != null) {
					return unquoted;
				}
			}
			return null;
		}
	}

	/**
	 * @see Statistics#getCnt()
	 */
	public static class Calls extends ReadOnlyAccessor<Entry<String, Statistics>> {
		@Override
		public Object getValue(Entry<String, Statistics> object, String property) {
			return object.getValue().getCnt();
		}
	}

	/**
	 * @see Statistics#getStacktrace()
	 */
	public static class Stacktrace extends ReadOnlyAccessor<Entry<String, Statistics>> {
		@Override
		public Object getValue(Entry<String, Statistics> object, String property) {
			return object.getValue().getStacktrace();
		}
	}

	/**
	 * @see Statistics#getElapsedNanoSum()
	 */
	public static class Time extends ReadOnlyAccessor<Entry<String, Statistics>> {
		@Override
		public Object getValue(Entry<String, Statistics> object, String property) {
			return object.getValue().getElapsedNanoSum();
		}
	}

	/**
	 * @see Statistics#getElapsedNanoAvg()
	 */
	public static class TimeAvg extends ReadOnlyAccessor<Entry<String, Statistics>> {
		@Override
		public Object getValue(Entry<String, Statistics> object, String property) {
			return object.getValue().getElapsedNanoAvg();
		}
	}

	/**
	 * @see Statistics#getElapsedNanoMax()
	 */
	public static class TimeMax extends ReadOnlyAccessor<Entry<String, Statistics>> {
		@Override
		public Object getValue(Entry<String, Statistics> object, String property) {
			return object.getValue().getElapsedNanoMax();
		}
	}

	/**
	 * @see Statistics#getRowsSum()
	 */
	public static class Rows extends ReadOnlyAccessor<Entry<String, Statistics>> {
		@Override
		public Object getValue(Entry<String, Statistics> object, String property) {
			return object.getValue().getRowsSum();
		}
	}

	/**
	 * @see Statistics#getRowsAvg()
	 */
	public static class RowsAvg extends ReadOnlyAccessor<Entry<String, Statistics>> {
		@Override
		public Object getValue(Entry<String, Statistics> object, String property) {
			return object.getValue().getRowsAvg();
		}
	}

	/**
	 * @see Statistics#getRowsMax()
	 */
	public static class RowsMax extends ReadOnlyAccessor<Entry<String, Statistics>> {
		@Override
		public Object getValue(Entry<String, Statistics> object, String property) {
			return object.getValue().getRowsMax();
		}
	}

}
