/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.control.table.ColumnDef;
import com.top_logic.layout.react.control.table.ReactCellControlProvider;
import com.top_logic.layout.react.control.table.ReactTableControl;
import com.top_logic.layout.react.control.table.ReactTextCellControl;
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
	}

	private ReactTableControl createDemoTable() {
		List<ColumnDef> columns = new ArrayList<>();
		columns.add(new ColumnDef("id", "ID").setWidth(80));
		columns.add(new ColumnDef("name", "Name").setWidth(200));
		columns.add(new ColumnDef("department", "Department").setWidth(150));
		columns.add(new ColumnDef("email", "Email").setWidth(250));
		columns.add(new ColumnDef("status", "Status").setWidth(120));

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

		ReactCellControlProvider cellProvider = (rowObject, columnName, cellValue) -> {
			return new ReactTextCellControl(cellValue);
		};

		ReactTableControl table = new ReactTableControl(rows, columns, cellProvider);
		table.setSelectionMode("multi");
		table.setFrozenColumnCount(2);
		return table;
	}
}
