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
 * How tables look for the person using them: density, stripes and column lines.
 *
 * <p>
 * The three settings are stored per person in the {@link PersonalConfiguration} and apply to every
 * table. They are written as start values onto the root element of the page, where the client reads
 * them from; every table sets its own {@code data-tl-density}, {@code data-tl-striped} and
 * {@code data-tl-column-lines} from them.
 * </p>
 *
 * <p>
 * Without a stored value - or without a user context to store one in - the factory values apply:
 * no density of its own (the density of the application counts), no stripes, no column lines.
 * </p>
 *
 * @see #writeAttributes(TagWriter)
 */
public final class TableViewPreferences {

	/**
	 * {@link PersonalConfiguration} key storing the table density: {@code ""}, {@code normal} or
	 * {@code compact}.
	 */
	public static final String KEY_DENSITY = "react.table.density";

	/** {@link PersonalConfiguration} key storing whether table rows are striped. */
	public static final String KEY_STRIPED = "react.table.striped";

	/** {@link PersonalConfiguration} key storing whether tables draw lines between columns. */
	public static final String KEY_COLUMN_LINES = "react.table.columnLines";

	/** Attribute of the root element marking that the table view is carried on it. Always written. */
	public static final String ATTR_MARK = "data-tl-table-view";

	/**
	 * Attribute of the root element naming the table density; written only for {@code normal} and
	 * {@code compact}.
	 */
	public static final String ATTR_DENSITY = "data-tl-table-density";

	/** Attribute of the root element that is present while table rows are striped. */
	public static final String ATTR_STRIPED = "data-tl-table-striped";

	/** Attribute of the root element that is present while tables draw lines between columns. */
	public static final String ATTR_COLUMN_LINES = "data-tl-table-column-lines";

	private static final String DENSITY_NORMAL = "normal";

	private static final String DENSITY_COMPACT = "compact";

	private TableViewPreferences() {
		// Static access only.
	}

	/**
	 * The table density the current person has chosen.
	 *
	 * @return {@code normal} or {@code compact}, or the empty string for no choice of its own, which
	 *         is also the result without a personal configuration.
	 */
	public static String density() {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		return densityOf(pc == null ? null : pc.getJSONValue(KEY_DENSITY));
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
	 * The density a stored value stands for.
	 *
	 * @param stored
	 *        The value read from the personal configuration, possibly {@code null}.
	 * @return {@code normal} or {@code compact} if that is what is stored, otherwise the empty
	 *         string.
	 */
	public static String densityOf(Object stored) {
		if (DENSITY_NORMAL.equals(stored) || DENSITY_COMPACT.equals(stored)) {
			return (String) stored;
		}
		return "";
	}

	/**
	 * Stores the table view of the current person.
	 *
	 * <p>
	 * Without a {@link TLContext user context} nothing happens, as there is no person to store the
	 * view for.
	 * </p>
	 *
	 * @param density
	 *        {@code normal}, {@code compact}, or the empty string for no density of its own; any
	 *        other value counts as the empty string.
	 * @param striped
	 *        Whether table rows are striped.
	 * @param columnLines
	 *        Whether tables draw lines between columns.
	 */
	public static void store(String density, boolean striped, boolean columnLines) {
		if (TLContext.getContext() == null) {
			// The configuration of a missing context swallows writes - with a warning each time.
			return;
		}
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return;
		}
		pc.setJSONValue(KEY_DENSITY, densityOf(density));
		pc.setJSONValue(KEY_STRIPED, Boolean.valueOf(striped));
		pc.setJSONValue(KEY_COLUMN_LINES, Boolean.valueOf(columnLines));
		PersonalConfiguration.storePersonalConfiguration();
	}

	/**
	 * Writes the table view of the current person as start values into the open start tag of the
	 * root element.
	 *
	 * @param out
	 *        The writer of the root element's start tag.
	 *
	 * @see #writeAttributes(TagWriter, String, boolean, boolean)
	 */
	public static void writeAttributes(TagWriter out) {
		writeAttributes(out, density(), striped(), columnLines());
	}

	/**
	 * Writes the given table view as start values into the open start tag of the root element.
	 *
	 * <p>
	 * {@link #ATTR_MARK} is always written. {@link #ATTR_DENSITY} follows only for a density of
	 * {@code normal} or {@code compact}; the flag attributes are present or absent.
	 * </p>
	 *
	 * @param out
	 *        The writer of the root element's start tag.
	 * @param density
	 *        See {@link #density()}.
	 * @param striped
	 *        See {@link #striped()}.
	 * @param columnLines
	 *        See {@link #columnLines()}.
	 */
	public static void writeAttributes(TagWriter out, String density, boolean striped, boolean columnLines) {
		out.writeAttribute(ATTR_MARK, "");
		String known = densityOf(density);
		if (!known.isEmpty()) {
			out.writeAttribute(ATTR_DENSITY, known);
		}
		if (striped) {
			out.writeAttribute(ATTR_STRIPED, "");
		}
		if (columnLines) {
			out.writeAttribute(ATTR_COLUMN_LINES, "");
		}
	}

}
