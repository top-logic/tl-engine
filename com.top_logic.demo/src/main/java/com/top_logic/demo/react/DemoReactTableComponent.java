/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.TreeStructure;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.table.impl.TreeRowSource;

/**
 * Demo {@link LayoutComponent} that showcases the green-field React table control
 * ({@link TableViewControl}).
 *
 * <p>
 * Builds a flat table with 1000 synthetic rows (virtual scrolling, sorting) and a tree table with
 * expandable nodes, both on the green-field {@link com.top_logic.table.TableView} stack.
 * </p>
 */
public class DemoReactTableComponent extends LayoutComponent {

	/**
	 * Configuration for {@link DemoReactTableComponent}.
	 */
	public interface Config extends LayoutComponent.Config {
		// No additional configuration needed.
	}

	private ReactContext _context;

	private TableViewControl<Map<String, Object>> _tableControl;

	private TableViewControl<Map<String, Object>> _treeTableControl;

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
			_context = ReactContext.fromDisplayContext(displayContext);
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

	private TableViewControl<Map<String, Object>> createDemoTable() {
		List<Column<Map<String, Object>, ?>> columns = new ArrayList<>();
		columns.add(dataColumn("id", "ID", 80));
		columns.add(dataColumn("name", "Name", 200));
		columns.add(dataColumn("department", "Department", 150));
		columns.add(dataColumn("email", "Email", 250));
		columns.add(dataColumn("status", "Status", 120));

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

		ListRowSource<Map<String, Object>> source = new ListRowSource<>(rows, columns);
		DefaultTableView<Map<String, Object>> view = DefaultTableView.create(columns, source);
		return new TableViewControl<>(_context, view, false);
	}

	private TableViewControl<Map<String, Object>> createDemoTreeTable() {
		List<Column<Map<String, Object>, ?>> columns = new ArrayList<>();
		columns.add(dataColumn("name", "Name", 300));
		columns.add(dataColumn("type", "Type", 150));
		columns.add(dataColumn("size", "Size", 100));

		List<Node> roots = new ArrayList<>();
		String[] folders = { "Documents", "Pictures", "Source Code", "Music" };
		for (String folderName : folders) {
			Node folder = new Node(data(folderName, "Folder", ""));
			roots.add(folder);
			for (int i = 0; i < 5; i++) {
				Node file = new Node(data("File " + (i + 1) + " in " + folderName, "File", ((i + 1) * 128) + " KB"));
				folder._children.add(file);
				if (i == 0) {
					for (int j = 0; j < 3; j++) {
						file._children.add(new Node(data("Version " + (j + 1), "Version", ((j + 1) * 64) + " KB")));
					}
				}
			}
		}

		TreeStructure<Node, Map<String, Object>> structure = new TreeStructure<>() {
			@Override
			public List<Node> roots() {
				return roots;
			}

			@Override
			public List<Node> children(Node node) {
				return node._children;
			}

			@Override
			public boolean isLeaf(Node node) {
				return node._children.isEmpty();
			}

			@Override
			public Map<String, Object> businessObject(Node node) {
				return node._data;
			}
		};

		TreeRowSource<Node, Map<String, Object>> source = new TreeRowSource<>(structure, columns);
		DefaultTableView<Map<String, Object>> view = DefaultTableView.create(columns, source);
		return new TableViewControl<>(_context, view, true);
	}

	private static Map<String, Object> data(String name, String type, String size) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("name", name);
		row.put("type", type);
		row.put("size", size);
		return row;
	}

	/**
	 * A text column reading one entry of the row map, sorted by its display label.
	 */
	private static Column<Map<String, Object>, Object> dataColumn(String name, String label, int width) {
		return DefaultColumn.<Map<String, Object>, Object> builder(name, row -> row.get(name))
			.label(ResKey.text(label))
			.renderer(value -> CellContent.text(MetaLabelProvider.INSTANCE.getLabel(value)))
			.sort(() -> Comparator.comparing(MetaLabelProvider.INSTANCE::getLabel))
			.width(width)
			.build();
	}

	/**
	 * A tree node carrying a row data map and its children.
	 */
	private static final class Node {

		final Map<String, Object> _data;

		final List<Node> _children = new ArrayList<>();

		Node(Map<String, Object> data) {
			_data = data;
		}
	}
}
