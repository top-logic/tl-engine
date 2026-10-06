/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

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
	 * renderer {@link LabelProviderService#getExcelCellRendererForType(com.top_logic.model.TLType)
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
		return LabelProviderService.getInstance().getExcelCellRendererForType(type.type());
	}

}
