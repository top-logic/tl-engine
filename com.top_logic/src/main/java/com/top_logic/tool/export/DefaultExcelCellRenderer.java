/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.currency.Amount;
import com.top_logic.layout.Control;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.security.ProtectedValueExportMapping;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.Column;
import com.top_logic.mig.html.HTMLFormatterFormat;
import com.top_logic.util.FormFieldHelper;

/**
 * Default {@link ExcelCellRenderer}.
 */
public final class DefaultExcelCellRenderer extends AbstractExcelCellRenderer {

	/**
	 * Singleton {@link DefaultExcelCellRenderer} instance.
	 */
	public static final DefaultExcelCellRenderer INSTANCE = new DefaultExcelCellRenderer();

	private DefaultExcelCellRenderer() {
		// Singleton constructor.
	}

	@Override
	protected ExcelValue renderValue(RenderContext context, Object cellValue, int excelRow, int excelColumn) {
		Object theCellValue = getExportValue(context, cellValue);
		return new ExcelValue(excelRow, excelColumn, theCellValue);
	}

	@Override
	public Object newCustomContext(TableModel model, Column modelColumn) {
		return HTMLFormatterFormat.INSTANCE;
	}

	/**
	 * Creates a default Excel representation for the given value.
	 */
	public static Object getExportValue(RenderContext context, Object value) {
		value = ProtectedValueExportMapping.INSTANCE.map(value);

		if (value instanceof FormField && ((FormField) value).hasValue()) {
			value = ((FormField) value).getValue();
		}
		if (value instanceof Collection collection) {
			ResourceProvider resourceProvider = context.modelColumn().getConfig().getResourceProvider();

			StringBuilder buffer = new StringBuilder();
			for (Iterator<?> it = collection.iterator(); it.hasNext();) {
				buffer.append(resourceProvider.getLabel(it.next()));
				if (it.hasNext()) {
					buffer.append(", ");
				}
			}
			return buffer.toString();
		} else if (value instanceof Amount) {
			value = Double.valueOf(((Amount) value).getValue());
		} else if (value instanceof Control) {
			value = FormFieldHelper.getProperValue(value);
		} else if ((value != null)
			&& !(value instanceof Date)
			&& !(value instanceof Number)
			&& !(value instanceof String)
			&& !(value instanceof WebFolder)) {
			value = MetaLabelProvider.INSTANCE.getLabel(value);
		}

		if (value == null) {
			value = "";
		}

		return (value);
	}

}