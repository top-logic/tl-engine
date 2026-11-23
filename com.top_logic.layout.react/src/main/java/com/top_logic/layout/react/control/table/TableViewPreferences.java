/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.util.TLContext;

/**
 * How tables look for the person using them: stripes and column lines.
 *
 * <p>
 * The two settings are chosen in the user menu, stored per person in the
 * {@link PersonalConfiguration} and apply to every table. They are written as start values onto the
 * root element of the page, where the client reads them from; every table sets its own
 * {@code data-tl-striped} and {@code data-tl-column-lines} from them. The density of a table is not
 * among them: a table follows the density of the application, which the user menu chooses for the
 * whole page.
 * </p>
 *
 * <p>
 * Without a stored value - or without a user context to store one in - the factory values apply:
 * no stripes, no column lines.
 * </p>
 *
 * @see #writeAttributes(TagWriter)
 */
public final class TableViewPreferences {

	/** {@link PersonalConfiguration} key storing whether table rows are striped. */
	public static final String KEY_STRIPED = "react.table.striped";

	/** {@link PersonalConfiguration} key storing whether tables draw lines between columns. */
	public static final String KEY_COLUMN_LINES = "react.table.columnLines";

	/** Attribute of the root element marking that the table view is carried on it. Always written. */
	public static final String ATTR_MARK = "data-tl-table-view";

	/** Attribute of the root element that is present while table rows are striped. */
	public static final String ATTR_STRIPED = "data-tl-table-striped";

	/** Attribute of the root element that is present while tables draw lines between columns. */
	public static final String ATTR_COLUMN_LINES = "data-tl-table-column-lines";

	private TableViewPreferences() {
		// Static access only.
	}

	/**
	 * Whether the current person wants table rows striped.
	 */
	public static boolean striped() {
		return flag(KEY_STRIPED);
	}

	/**
	 * Whether the current person wants lines between table columns.
	 */
	public static boolean columnLines() {
		return flag(KEY_COLUMN_LINES);
	}

	private static boolean flag(String key) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		return pc != null && Boolean.TRUE.equals(pc.getJSONValue(key));
	}

	/**
	 * Stores whether the current person wants table rows striped.
	 *
	 * <p>
	 * Without a {@link TLContext user context} nothing happens, as there is no person to store the
	 * setting for.
	 * </p>
	 */
	public static void setStriped(boolean striped) {
		store(KEY_STRIPED, striped);
	}

	/**
	 * Stores whether the current person wants lines between table columns.
	 *
	 * <p>
	 * Without a {@link TLContext user context} nothing happens, as there is no person to store the
	 * setting for.
	 * </p>
	 */
	public static void setColumnLines(boolean columnLines) {
		store(KEY_COLUMN_LINES, columnLines);
	}

	private static void store(String key, boolean value) {
		if (TLContext.getContext() == null) {
			// The configuration of a missing context swallows writes - with a warning each time.
			return;
		}
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return;
		}
		pc.setJSONValue(key, Boolean.valueOf(value));
		PersonalConfiguration.storePersonalConfiguration();
	}

	/**
	 * Writes the table view of the current person as start values into the open start tag of the
	 * root element.
	 *
	 * @param out
	 *        The writer of the root element's start tag.
	 *
	 * @see #writeAttributes(TagWriter, boolean, boolean)
	 */
	public static void writeAttributes(TagWriter out) {
		writeAttributes(out, striped(), columnLines());
	}

	/**
	 * Writes the given table view as start values into the open start tag of the root element.
	 *
	 * <p>
	 * {@link #ATTR_MARK} is always written; the flag attributes are present or absent.
	 * </p>
	 *
	 * @param out
	 *        The writer of the root element's start tag.
	 * @param striped
	 *        See {@link #striped()}.
	 * @param columnLines
	 *        See {@link #columnLines()}.
	 */
	public static void writeAttributes(TagWriter out, boolean striped, boolean columnLines) {
		out.writeAttribute(ATTR_MARK, "");
		if (striped) {
			out.writeAttribute(ATTR_STRIPED, "");
		}
		if (columnLines) {
			out.writeAttribute(ATTR_COLUMN_LINES, "");
		}
	}

}
