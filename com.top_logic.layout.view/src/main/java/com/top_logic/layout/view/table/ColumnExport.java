/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.text.Format;
import java.text.SimpleDateFormat;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.ui.TLColumnInfo;
import com.top_logic.tool.export.ExcelCellRenderer;
import com.top_logic.tool.export.FormattedValueExcelRenderer;

/**
 * How a column takes part in an export of its table, as its declaration says.
 *
 * @param exported
 *        Whether the column is exported at all.
 * @param renderer
 *        The renderer the declaration configures, {@code null} for the one derived from the type
 *        of the column's values.
 *
 * @see ColumnExportConfig
 */
public record ColumnExport(boolean exported, ExcelCellRenderer renderer) {

	/** A column exported by the renderer of its type. */
	public static final ColumnExport DEFAULT = new ColumnExport(true, null);

	/**
	 * Excel's built-in format of a date with time, which Excel displays in the date and time order
	 * of the reader's region.
	 */
	public static final String EXCEL_DATE_TIME_FORMAT = "m/d/yy h:mm";

	/** Excel's built-in format of a time of day with hours and minutes. */
	public static final String EXCEL_TIME_FORMAT = "h:mm";

	/** Excel's built-in format of a time of day with hours, minutes and seconds. */
	public static final String EXCEL_TIME_SECONDS_FORMAT = "h:mm:ss";

	/** The letters of a {@link SimpleDateFormat} pattern standing for a part of the date. */
	private static final String DATE_LETTERS = "GyYMLwWDdFEu";

	/** The letters of a {@link SimpleDateFormat} pattern standing for a part of the time of day. */
	private static final String TIME_LETTERS = "aHkKhmsS";

	/** The letter of a {@link SimpleDateFormat} pattern standing for the seconds. */
	private static final char SECONDS_LETTER = 's';

	/** The quote of a literal in a {@link SimpleDateFormat} pattern. */
	private static final char QUOTE = '\'';

	/**
	 * The export settings of the given declaration.
	 */
	public static ColumnExport of(InstantiationContext context, ColumnExportConfig config) {
		ExcelCellRenderer renderer = context.getInstance(config.getExportRenderer());
		if (config.getExport() && renderer == null) {
			return DEFAULT;
		}
		return new ColumnExport(config.getExport(), renderer);
	}

	/**
	 * The renderer writing the values of a column of the given type into an Excel cell.
	 *
	 * <p>
	 * The {@link #renderer() configured} one, otherwise the one the type is configured with - in
	 * the order the generic columns of a classic table look for it: a {@link TLColumnInfo}
	 * annotation of the attribute or type, a format pattern annotated to a primitive value, the
	 * Excel format a date format showing the time of day needs (see
	 * {@link #excelDateFormat(Format)}), the renderer
	 * {@link LabelProviderService#getExcelCellRendererForType(com.top_logic.model.TLType)
	 * registered for the type}.
	 * </p>
	 *
	 * @return The renderer to use, {@code null} for the default renderer.
	 */
	public ExcelCellRenderer rendererFor(ColumnType type) {
		if (renderer != null) {
			return renderer;
		}
		if (type == null || !type.resolved()) {
			return null;
		}
		TLColumnInfo columnInfo = type.annotations().getAnnotation(TLColumnInfo.class);
		if (columnInfo != null) {
			PolymorphicConfiguration<? extends ExcelCellRenderer> annotated = columnInfo.getExcelRenderer();
			if (annotated != null) {
				InstantiationContext context = ApplicationConfig.getInstance().getServiceStartupContext();
				ExcelCellRenderer result = context.getInstance(annotated);
				if (result != null) {
					return result;
				}
			}
		}
		if (type.type() instanceof TLPrimitive && !type.multiple()) {
			try {
				String pattern = DisplayAnnotations.getFormatPattern(type.annotations());
				if (pattern != null) {
					return new FormattedValueExcelRenderer(pattern);
				}
			} catch (ConfigurationException ex) {
				Logger.error("Invalid format annotation of " + type.type() + ".", ex, ColumnExport.class);
			}
		}
		if (type.type() instanceof TLPrimitive primitive && primitive.getKind() == TLPrimitive.Kind.DATE
			&& !type.multiple()) {
			try {
				String dateFormat = excelDateFormat(DisplayAnnotations.getConfiguredFormat(type.annotations()));
				if (dateFormat != null) {
					return new FormattedValueExcelRenderer(dateFormat);
				}
			} catch (ConfigurationException ex) {
				Logger.error("Invalid format annotation of " + type.type() + ".", ex, ColumnExport.class);
			}
		}
		return LabelProviderService.getInstance().getExcelCellRendererForType(type.type());
	}

	/**
	 * The Excel format of a cell holding a date that is displayed with the given format, where the
	 * default date format of an Excel cell would lose a part of it.
	 *
	 * <p>
	 * A date is written to an Excel cell as a date without time of day, unless the cell states a
	 * format of its own. A format showing the time of day is given an Excel format showing it as
	 * well: {@link #EXCEL_DATE_TIME_FORMAT} where the format shows the date, too,
	 * {@link #EXCEL_TIME_FORMAT} or {@link #EXCEL_TIME_SECONDS_FORMAT} where it shows the time of day
	 * only. These are built-in formats of Excel, which it displays in the order of the reader's
	 * region, as it does the default date format.
	 * </p>
	 *
	 * @param format
	 *        The format the values are displayed with, {@code null} for none.
	 * @return The Excel format for the cells, {@code null} for the default date format.
	 */
	public static String excelDateFormat(Format format) {
		if (!(format instanceof SimpleDateFormat dateFormat)) {
			return null;
		}
		String letters = withoutLiterals(dateFormat.toPattern());
		if (!containsAny(letters, TIME_LETTERS)) {
			return null;
		}
		if (containsAny(letters, DATE_LETTERS)) {
			return EXCEL_DATE_TIME_FORMAT;
		}
		return letters.indexOf(SECONDS_LETTER) >= 0 ? EXCEL_TIME_SECONDS_FORMAT : EXCEL_TIME_FORMAT;
	}

	/**
	 * The given {@link SimpleDateFormat} pattern without its quoted literals.
	 */
	private static String withoutLiterals(String pattern) {
		StringBuilder result = new StringBuilder();
		boolean quoted = false;
		for (int n = 0; n < pattern.length(); n++) {
			char ch = pattern.charAt(n);
			if (ch == QUOTE) {
				quoted = !quoted;
			} else if (!quoted) {
				result.append(ch);
			}
		}
		return result.toString();
	}

	private static boolean containsAny(String text, String characters) {
		for (int n = 0; n < characters.length(); n++) {
			if (text.indexOf(characters.charAt(n)) >= 0) {
				return true;
			}
		}
		return false;
	}

}
