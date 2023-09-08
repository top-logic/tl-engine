/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.implementation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableRenderer;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.Column;
import com.top_logic.layout.table.model.ColumnConfig;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.model.NoDefaultColumnAdaption;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.export.pdf.PDFRenderer;
import com.top_logic.util.Resources;

/**
 * {@link HTMLFragment} to render a simple HTML table for PDF export.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimplePDFTableFragment implements HTMLFragment {

	private TableConfigurationProvider _table;

	private List<?> _rows;

	/**
	 * Creates a {@link SimplePDFTableFragment}.
	 * 
	 * @param table
	 *        Definition of the export table.
	 * @param rows
	 *        Rows of the table.
	 */
	public SimplePDFTableFragment(TableConfigurationProvider table, List<?> rows) {
		_table = table;
		_rows = rows;
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		SelectField field =
			FormFactory.newSelectField("temporaryField", _rows, FormFactory.MULTIPLE, FormFactory.IMMUTABLE);
		ConfigKey.setPreventPersonalization(field);
		field.initSelection(_rows);
		field.setTableConfigurationProvider(_table);
		TableData tableData = field.getTableData();

		TableRenderer.ColumnsInfo columnsInfo =
			new DefaultTableRenderer.DefaultColumnsInfo(tableData.getViewModel().getHeader());

		out.beginTag(HTMLConstants.TABLE);

		out.beginTag(HTMLConstants.THEAD);
		writeTableHeader(out, tableData, columnsInfo);
		out.endTag(HTMLConstants.THEAD);

		out.beginTag(HTMLConstants.TBODY);
		writeTableBody(context, out, tableData, columnsInfo);
		out.endTag(HTMLConstants.TBODY);

		out.endTag(HTMLConstants.TABLE);
	}

	private void writeTableBody(DisplayContext context, TagWriter out, TableData tableData,
			TableRenderer.ColumnsInfo columnsInfo) throws IOException {
		for (Object row : tableData.getViewModel().getDisplayedRows()) {
			out.beginTag(HTMLConstants.TR);
			for (Column col : columnsInfo.getColumns()) {
				out.beginTag(HTMLConstants.TD);

				Object cellValue = tableData.getViewModel().getValueAt(row, col.getName());

				PDFRenderer pdfRenderer = col.getConfig().getPDFRenderer();
				if (pdfRenderer == null) {
					pdfRenderer = LabelProviderService.getInstance().getPDFRenderer(cellValue);
				}
				pdfRenderer.write(context, out, row, cellValue);

				out.endTag(HTMLConstants.TD);
			}

			out.endTag(HTMLConstants.TR);
		}
	}

	private void writeTableHeader(TagWriter out, TableData tableData, TableRenderer.ColumnsInfo columnsInfo) {
		writeTitle(out, tableData, columnsInfo);
		writeGroupRows(out, tableData, columnsInfo);
	}

	private void writeTitle(TagWriter out, TableData tableData, TableRenderer.ColumnsInfo columnsInfo) {
		out.beginTag(HTMLConstants.TR);

		out.beginBeginTag(HTMLConstants.TH);
		out.writeAttribute(HTMLConstants.CLASS_ATTR, "title");
		out.writeAttribute(HTMLConstants.COLSPAN_ATTR, columnsInfo.getColumnCount());
		out.endBeginTag();
		String title = getTableTitle(tableData);
		if (!title.isEmpty()) {
			out.writeText(title);
		} else {
			// Ensure that header does not collapse without entry.
			out.writeText(HTMLConstants.NBSP);
		}
		out.endTag(HTMLConstants.TH);

		out.endTag(HTMLConstants.TR);
	}

	private void writeGroupRows(TagWriter out, TableData data, TableRenderer.ColumnsInfo columnsInfo) {
		int groupHeaderLine = 0;
		for (List<Column> line : columnsInfo.getGroupRows()) {
			writeGroupRow(out, data, 0, columnsInfo.getColumnCount(), groupHeaderLine, line);

			groupHeaderLine++;
		}

		out.beginTag(HTMLConstants.TR);
		for (Column column : columnsInfo.getColumns()) {
			out.beginTag(HTMLConstants.TH);
			writeHeaderContent(out, data, column);
			out.endTag(HTMLConstants.TH);

		}
		out.endTag(HTMLConstants.TR);

	}

	private void writeHeaderContent(TagWriter out, TableData data, Column column) {
		ColumnConfiguration theColDesc = column.getConfig();
		if (!theColDesc.isShowHeader()) {
			return;
		}
		String columnLabel = column.getLabel(data);
		if (StringServices.isEmpty(columnLabel)) {
			columnLabel = HTMLConstants.NBSP;
		}
		out.writeText(columnLabel);
	}

	private void writeGroupRow(TagWriter out, TableData data, int startColumn, int stopColumn, int groupHeaderLine,
			List<Column> line) {
		out.beginTag(HTMLConstants.TR);

		int groupIndex = 0;
		for (Column group : line) {
			int groupStart = groupIndex;
			int groupStop = groupIndex = groupStart + group.getSpan();
			int groupSpan = Math.min(stopColumn, groupStop) - Math.max(startColumn, groupStart);
			if (groupSpan < 1) {
				continue;
			}

			boolean hasLabel = data.getViewModel().getHeader().hasLabel(group, groupHeaderLine);

			out.beginBeginTag(HTMLConstants.TH);
			if (groupSpan > 1) {
				out.writeAttribute(HTMLConstants.COLSPAN_ATTR, groupSpan);
			}
			out.endBeginTag();
			String label;
			if (hasLabel) {
				label = group.getLabel(data);
			} else {
				label = HTMLConstants.NBSP;
			}
			out.writeText(label);
			out.endTag(HTMLConstants.TH);

		}
		out.endTag(HTMLConstants.TR);
	}

	private String getTableTitle(TableData model) {
		TableConfiguration tableConfig = model.getTableModel().getTableConfiguration();
		ResKey titleKey = tableConfig.getTitleKey();
		if (titleKey != null) {
			return Resources.getInstance().getString(titleKey);
		}
		return tableConfig.getResPrefix().getStringResource(TableControl.RES_TITLE, "");
	}

	/**
	 * Creates a {@link TableConfigurationProvider} removing all columns that are not exported.
	 */
	public static TableConfigurationProvider removeNonExportColumns() {
		return new NoDefaultColumnAdaption() {
	
			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				Set<String> excludedByExport = new HashSet<>();
				for (ColumnConfiguration declaredColumn : table.getDeclaredColumns()) {
					if (declaredColumn.isClassifiedBy(ColumnConfig.CLASSIFIER_NO_EXPORT)) {
						declaredColumn.setVisibility(DisplayMode.excluded);
						excludedByExport.add(declaredColumn.getName());
					}
				}
				if (!excludedByExport.isEmpty()) {
					ArrayList<String> newDefColumns = new ArrayList<>(table.getDefaultColumns());
					newDefColumns.removeAll(excludedByExport);
					table.setDefaultColumns(newDefColumns);
				}
			}
		};
	}

}
