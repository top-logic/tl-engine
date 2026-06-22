/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.Selection;
import com.top_logic.table.SelectionMode;
import com.top_logic.table.Option;
import com.top_logic.table.TreeStructure;
import com.top_logic.table.filter.BooleanColumnFilter;
import com.top_logic.table.filter.ComparableColumnFilter;
import com.top_logic.table.filter.OptionsColumnFilter;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.table.impl.TreeRowSource;

/**
 * Demo {@link LayoutComponent} showcasing the green-field table model
 * ({@link com.top_logic.table.TableView}) rendered through {@link TableViewControl}.
 *
 * <p>
 * Demonstrates a flat table (sort, multi-select, frozen columns), a grouped table with
 * subtotals, and a tree table - all driven by the same windowed {@code RowSource} API with
 * no dependency on the legacy {@code TableModel}.
 * </p>
 */
public class DemoTableViewComponent extends LayoutComponent {

	/**
	 * A demo employee row.
	 *
	 * @param id
	 *        The employee identifier.
	 * @param name
	 *        The employee name.
	 * @param department
	 *        The department the employee belongs to.
	 * @param status
	 *        The employment status.
	 * @param salary
	 *        The employee salary.
	 * @param active
	 *        Whether the employee is currently active.
	 */
	public record Emp(int id, String name, String department, String status, int salary, boolean active) {
		// Demo fixture.
	}

	private static final String[] DEPARTMENTS =
		{ "Engineering", "Marketing", "Sales", "HR", "Finance", "Support" };

	private static final String[] STATUSES = { "Active", "Inactive", "On Leave" };

	/**
	 * A demo file-system row.
	 *
	 * @param name
	 *        The file or folder name.
	 * @param type
	 *        The entry type (e.g. file or folder).
	 * @param size
	 *        The human-readable size.
	 */
	public record FileRow(String name, String type, String size) {
		// Demo fixture.
	}

	/** A demo file-system tree node. */
	private static final class FsNode {
		final FileRow _row;

		final List<FsNode> _children = new ArrayList<>();

		FsNode(FileRow row) {
			_row = row;
		}

		FsNode child(FileRow row) {
			FsNode node = new FsNode(row);
			_children.add(node);
			return node;
		}
	}

	/**
	 * Configuration for {@link DemoTableViewComponent}.
	 */
	public interface Config extends LayoutComponent.Config {
		// No additional configuration needed.
	}

	private ReactContext _context;

	private TableViewControl<Emp> _flat;

	private TableViewControl<Emp> _grouped;

	private TableViewControl<FileRow> _tree;

	/**
	 * Creates a new {@link DemoTableViewComponent}.
	 */
	public DemoTableViewComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public void writeBody(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response,
			TagWriter out) throws IOException, ServletException {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
		if (_flat == null) {
			_context = ReactContext.fromDisplayContext(displayContext);
			_flat = createFlatTable();
			_grouped = createGroupedTable();
			_tree = createTreeTable();
		}

		section(out, displayContext, "Green-field Table (flat)",
			"1000 rows, virtual scrolling. Click headers to sort, rows to select. First two columns frozen.", _flat);
		section(out, displayContext, "Grouped by department (subtotals)",
			"Group headers double as subtotal rows; the salary column shows the group sum. Click to collapse.",
			_grouped);
		section(out, displayContext, "Tree table",
			"A file-system tree over the same RowSource API. Click chevrons to expand.", _tree);
	}

	private void section(TagWriter out, DisplayContext displayContext, String title, String description,
			TableViewControl<?> control) throws IOException {
		out.beginTag(HTMLConstants.H2);
		out.writeText(title);
		out.endTag(HTMLConstants.H2);
		out.beginTag(HTMLConstants.PARAGRAPH);
		out.writeText(description);
		out.endTag(HTMLConstants.PARAGRAPH);
		out.beginBeginTag(HTMLConstants.DIV);
		out.writeAttribute(HTMLConstants.STYLE_ATTR, "height: 400px; border: 1px solid #ccc;");
		out.endBeginTag();
		control.write(displayContext, out);
		out.endTag(HTMLConstants.DIV);
	}

	private List<Column<Emp, ?>> employeeColumns() {
		Column<Emp, Integer> id = DefaultColumn.<Emp, Integer> builder("id", Emp::id)
			.label(ResKey.text("ID")).width(70).sort(() -> Comparator.naturalOrder())
			.filter(ComparableColumnFilter.integers()).build();
		Column<Emp, String> name = DefaultColumn.<Emp, String> builder("name", Emp::name)
			.label(ResKey.text("Name")).width(180).sort(() -> Comparator.naturalOrder())
			.filter(TextColumnFilter.forStrings()).build();
		Column<Emp, String> department = DefaultColumn.<Emp, String> builder("department", Emp::department)
			.label(ResKey.text("Department")).width(150).sort(() -> Comparator.naturalOrder())
			.filter(new OptionsColumnFilter<>(options(DEPARTMENTS))).build();
		Column<Emp, String> status = DefaultColumn.<Emp, String> builder("status", Emp::status)
			.label(ResKey.text("Status")).width(120).sort(() -> Comparator.naturalOrder())
			.filter(new OptionsColumnFilter<>(options(STATUSES))).build();
		Column<Emp, Integer> salary = DefaultColumn.<Emp, Integer> builder("salary", Emp::salary)
			.label(ResKey.text("Salary")).width(120).sort(() -> Comparator.naturalOrder())
			.filter(ComparableColumnFilter.integers())
			.aggregate(group -> CellContent.text("Sum: " + group.members().stream().mapToInt(Emp::salary).sum()))
			.build();
		Column<Emp, Boolean> active = DefaultColumn.<Emp, Boolean> builder("active", Emp::active)
			.label(ResKey.text("Active")).width(90).sort(() -> Comparator.naturalOrder())
			.filter(BooleanColumnFilter.INSTANCE).build();
		return List.of(id, name, department, status, salary, active);
	}

	private static List<Option> options(String[] values) {
		List<Option> options = new ArrayList<>(values.length);
		for (String value : values) {
			options.add(new Option(value, ResKey.text(value)));
		}
		return options;
	}

	private List<Emp> employees() {
		List<Emp> rows = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			rows.add(new Emp(
				i + 1,
				"Employee " + (i + 1),
				DEPARTMENTS[i % DEPARTMENTS.length],
				STATUSES[i % STATUSES.length],
				40000 + (i % 50) * 1000,
				i % 2 == 0));
		}
		return rows;
	}

	private TableViewControl<Emp> createFlatTable() {
		List<Column<Emp, ?>> columns = employeeColumns();
		ListRowSource<Emp> source = new ListRowSource<>(employees(), columns, Emp::id);
		DefaultTableView<Emp> view = DefaultTableView.create(columns, source);
		view.state().setSelection(Selection.none(SelectionMode.MULTI));
		view.setFrozenColumnCount(2);
		return new TableViewControl<>(_context, view, false);
	}

	private TableViewControl<Emp> createGroupedTable() {
		List<Column<Emp, ?>> columns = employeeColumns();
		ListRowSource<Emp> source = new ListRowSource<>(employees(), columns, Emp::id);
		source.withGrouping(new GroupSpec(List.of("department")));
		DefaultTableView<Emp> view = new DefaultTableView<>(columns, source, groupedState());
		return new TableViewControl<>(_context, view, true);
	}

	private com.top_logic.table.TableViewState groupedState() {
		com.top_logic.table.TableViewState state = new com.top_logic.table.TableViewState();
		state.setColumnOrder(List.of("name", "status", "salary"));
		state.setGrouping(new GroupSpec(List.of("department")));
		return state;
	}

	private TableViewControl<FileRow> createTreeTable() {
		List<Column<FileRow, ?>> columns = List.of(
			DefaultColumn.<FileRow, String> builder("name", FileRow::name).label(ResKey.text("Name")).width(280)
				.sort(() -> Comparator.naturalOrder()).build(),
			DefaultColumn.<FileRow, String> builder("type", FileRow::type).label(ResKey.text("Type")).width(120)
				.build(),
			DefaultColumn.<FileRow, String> builder("size", FileRow::size).label(ResKey.text("Size")).width(100)
				.build());

		TreeRowSource<FsNode, FileRow> source = new TreeRowSource<>(fileSystem(), columns);
		DefaultTableView<FileRow> view = new DefaultTableView<>(columns, source, treeState());
		return new TableViewControl<>(_context, view, true);
	}

	private com.top_logic.table.TableViewState treeState() {
		com.top_logic.table.TableViewState state = new com.top_logic.table.TableViewState();
		state.setColumnOrder(List.of("name", "type", "size"));
		return state;
	}

	private TreeStructure<FsNode, FileRow> fileSystem() {
		List<FsNode> roots = new ArrayList<>();
		String[] folders = { "Documents", "Pictures", "Source Code", "Music" };
		for (String folderName : folders) {
			FsNode folder = new FsNode(new FileRow(folderName, "Folder", ""));
			for (int i = 0; i < 5; i++) {
				FsNode file = folder.child(new FileRow("File " + (i + 1) + " in " + folderName, "File",
					((i + 1) * 128) + " KB"));
				if (i == 0) {
					for (int j = 0; j < 3; j++) {
						file.child(new FileRow("Version " + (j + 1), "Version", ((j + 1) * 64) + " KB"));
					}
				}
			}
			roots.add(folder);
		}
		return new TreeStructure<>() {
			@Override
			public List<FsNode> roots() {
				return roots;
			}

			@Override
			public List<FsNode> children(FsNode node) {
				return node._children;
			}

			@Override
			public boolean isLeaf(FsNode node) {
				return node._children.isEmpty();
			}

			@Override
			public FileRow businessObject(FsNode node) {
				return node._row;
			}
		};
	}

}
