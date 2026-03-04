/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.MapAccessor;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.table.ReactCellControlProvider;
import com.top_logic.layout.react.control.table.ReactTableControl;
import com.top_logic.layout.react.control.table.ReactTextCellControl;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.tree.model.DefaultTreeTableModel;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableBuilder;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Demo {@link LayoutComponent} that showcases the React table control.
 *
 * <p>
 * Creates a {@link ReactTableControl} with 1000 synthetic rows and 5 columns to demonstrate virtual
 * scrolling, sorting, and row selection.
 * </p>
 */
public class DemoReactTableComponent extends LayoutComponent {

	/**
	 * Configuration for {@link DemoReactTableComponent}.
	 */
	public interface Config extends LayoutComponent.Config {
		// No additional configuration needed.
	}

	private ReactTableControl _tableControl;

	private ReactTableControl _treeTableControl;

	/**
	 * Creates a new {@link DemoReactTableComponent}.
	 */
	public DemoReactTableComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public void writeBody(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response,
			TagWriter out) throws IOException, ServletException {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);

		if (_tableControl == null) {
			_tableControl = createDemoTable();
		}

		out.beginTag(HTMLConstants.H2);
		out.writeText("React Table Demo");
		out.endTag(HTMLConstants.H2);

		out.beginTag(HTMLConstants.PARAGRAPH);
		out.writeText("A table with 1000 rows and virtual scrolling. "
			+ "Click column headers to sort. Click rows to select.");
		out.endTag(HTMLConstants.PARAGRAPH);

		out.beginBeginTag(HTMLConstants.DIV);
		out.writeAttribute(HTMLConstants.STYLE_ATTR, "height: 500px; border: 1px solid #ccc;");
		out.endBeginTag();
		_tableControl.write(displayContext, out);
		out.endTag(HTMLConstants.DIV);

		if (_treeTableControl == null) {
			_treeTableControl = createDemoTreeTable();
		}

		out.beginTag(HTMLConstants.H2);
		out.writeText("React Tree Table Demo");
		out.endTag(HTMLConstants.H2);

		out.beginTag(HTMLConstants.PARAGRAPH);
		out.writeText("A tree table with expandable nodes. Click chevrons to expand/collapse.");
		out.endTag(HTMLConstants.PARAGRAPH);

		out.beginBeginTag(HTMLConstants.DIV);
		out.writeAttribute(HTMLConstants.STYLE_ATTR, "height: 500px; border: 1px solid #ccc;");
		out.endBeginTag();
		_treeTableControl.write(displayContext, out);
		out.endTag(HTMLConstants.DIV);
	}

	private ReactTableControl createDemoTable() {
		List<String> columnNames = Arrays.asList("id", "name", "department", "email", "status");

		TableConfiguration tableConfig = TableConfiguration.table();
		tableConfig.getDefaultColumn().setAccessor(MapAccessor.INSTANCE);
		tableConfig.getDefaultColumn().setComparator(ComparableComparator.INSTANCE);

		declareColumn(tableConfig, "id", "ID", "80px");
		declareColumn(tableConfig, "name", "Name", "200px");
		declareColumn(tableConfig, "department", "Department", "150px");
		declareColumn(tableConfig, "email", "Email", "250px");
		declareColumn(tableConfig, "status", "Status", "120px");

		String[] departments = { "Engineering", "Marketing", "Sales", "HR", "Finance", "Support" };
		String[] statuses = { "Active", "Inactive", "On Leave" };

		List<Map<String, Object>> rows = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("id", String.valueOf(i + 1));
			row.put("name", "Employee " + (i + 1));
			row.put("department", departments[i % departments.length]);
			row.put("email", "employee" + (i + 1) + "@example.com");
			row.put("status", statuses[i % statuses.length]);
			rows.add(row);
		}

		ObjectTableModel model = new ObjectTableModel(columnNames, tableConfig, rows);

		LabelProvider labels = MetaLabelProvider.INSTANCE;
		ReactCellControlProvider cellProvider = (rowObject, columnName, cellValue) -> {
			return new ReactTextCellControl(labels.getLabel(cellValue));
		};

		ReactTableControl table = new ReactTableControl(model, cellProvider);
		table.setSelectionMode("multi");
		table.setFrozenColumnCount(2);
		return table;
	}

	private ReactTableControl createDemoTreeTable() {
		List<String> columnNames = Arrays.asList("name", "type", "size");

		TableConfiguration tableConfig = TableConfiguration.table();
		tableConfig.getDefaultColumn().setAccessor(MapAccessor.INSTANCE);
		tableConfig.getDefaultColumn().setComparator(ComparableComparator.INSTANCE);

		declareColumn(tableConfig, "name", "Name", "300px");
		declareColumn(tableConfig, "type", "Type", "150px");
		declareColumn(tableConfig, "size", "Size", "100px");

		Map<String, Object> rootData = new LinkedHashMap<>();
		rootData.put("name", "Root");
		rootData.put("type", "Root");
		rootData.put("size", "");

		DefaultTreeTableModel model = new DefaultTreeTableModel(
			new DefaultTreeTableBuilder(), rootData, columnNames, tableConfig);

		DefaultTreeTableNode root = model.getRoot();
		String[] folders = { "Documents", "Pictures", "Source Code", "Music" };

		for (int f = 0; f < folders.length; f++) {
			Map<String, Object> folderData = new LinkedHashMap<>();
			folderData.put("name", folders[f]);
			folderData.put("type", "Folder");
			folderData.put("size", "");
			DefaultTreeTableNode folder = root.createChild(folderData);

			for (int i = 0; i < 5; i++) {
				Map<String, Object> fileData = new LinkedHashMap<>();
				fileData.put("name", "File " + (i + 1) + " in " + folders[f]);
				fileData.put("type", "File");
				fileData.put("size", String.valueOf((i + 1) * 128) + " KB");
				DefaultTreeTableNode file = folder.createChild(fileData);

				if (i == 0) {
					for (int j = 0; j < 3; j++) {
						Map<String, Object> subData = new LinkedHashMap<>();
						subData.put("name", "Version " + (j + 1));
						subData.put("type", "Version");
						subData.put("size", String.valueOf((j + 1) * 64) + " KB");
						file.createChild(subData);
					}
				}
			}
		}

		LabelProvider labels = MetaLabelProvider.INSTANCE;
		ReactCellControlProvider cellProvider = (rowObject, columnName, cellValue) -> {
			return new ReactTextCellControl(labels.getLabel(cellValue));
		};

		ReactTableControl table = new ReactTableControl(model, cellProvider);
		table.setSelectionMode("multi");
		return table;
	}

	private static void declareColumn(TableConfiguration tableConfig, String name, String label, String width) {
		ColumnConfiguration col = tableConfig.declareColumn(name);
		col.setColumnLabel(label);
		col.setDefaultColumnWidth(width);
	}
}
