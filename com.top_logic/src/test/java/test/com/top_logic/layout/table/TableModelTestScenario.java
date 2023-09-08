/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table;

import java.util.Arrays;
import java.util.List;

import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;

/**
 * Factory for {@link EditableRowTableModel}s under test.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TableModelTestScenario {

	public static final String C4 = "C4";

	public static final String C3 = "C3";

	public static final String C2 = "C2";

	public static final String C1 = "C1";

	public static final String C0 = "C0";

	public static final String C0_A = "C0_A";

	public static final String C0_B = "C0_B";

	public static final String C1_A = "C1_A";

	public static final String C1_B = "C1_B";

	public static final String C1_C = "C1_C";

	public static final String C1_D = "C1_D";

	public static final String C2_A = "C2_A";

	public static final String C2_B = "C2_B";

	public static final String C2_C = "C2_C";

	public static final String C2_D = "C2_D";

	public static final String C2_E = "C2_E";

	public static final String C2_F = "C2_F";

	public static final String C2_G = "C2_G";

	public static final String CX_A = "CX_A";

	public static final String CX_B = "CX_B";

	public static final String CX_C = "CX_C";

	public static final String C3_A = "C3_A";

	public static final String C3_B = "C3_B";

	public static final String C3_C = "C3_C";

	public static final String C4_A = "C4_A";

	public static final String C4_B = "C4_B";

	public static final String C4_C = "C4_C";

	private List _rows;

	private final boolean _priorityTable;

	public TableModelTestScenario() {
		this(false);
	}

	public TableModelTestScenario(boolean priorityTable) {
		_priorityTable = priorityTable;
		_rows = createRows();
	}

	public List getRows() {
		return _rows;
	}

	public EditableRowTableModel createTableModel() {
		return createTableModel(createColumnDescriptionManager());
	}

	public EditableRowTableModel createTableModel(TableConfiguration tableConfig) {
		String[] columnNames = getColumnNames();

		return createTableModel(tableConfig, columnNames);
	}

	public EditableRowTableModel createTableModel(TableConfiguration tableConfig, String[] columnNames) {
		return new ObjectTableModel(columnNames, tableConfig, _rows, _priorityTable);
	}

	public String[] getColumnNames() {
		return new String[] { C0, C1, C2, C3 };
	}

	public List<Object[]> createRows() {
		return Arrays.asList(new Object[][] {
			row(C0_A, C1_A, C2_A, C3_A),
			row(C0_B, C1_A, C2_D, C3_A),
			row(C0_A, C1_B, C2_B, C3_C),
			row(C0_B, C1_B, C2_E, null),
			row(C0_A, C1_C, C2_C, null),
			row(C0_B, C1_C, C2_F, C3_A),
			row(C0_B, C1_D, C2_G, C3_B)
		});
	}

	public Object[] row(String c0, String c1, String c2, String c3) {
		return new String[] { c0, c1, c2, c3 };
	}

	protected TableConfiguration createColumnDescriptionManager() {
		return createColumnDescriptionManager(getColumnNames());
	}

	protected TableConfiguration createColumnDescriptionManager(String[] columnNames) {
		TableConfiguration config = TableConfiguration.table();
		deactivateDefaultGlobalFilterProvider(config);
		createColumnConfigsAndInitAccessors(config, columnNames);
		return config;
	}

	private void deactivateDefaultGlobalFilterProvider(TableConfiguration config) {
		config.setDefaultFilterProvider(null);
	}

	public void createColumnConfigsAndInitAccessors(TableConfiguration config, String[] columnNames) {
		for (int n = 0, cnt = columnNames.length; n < cnt; n++) {
			config.declareColumn(columnNames[n]).setAccessor(new ArrayReadAccessor(n));
		}
	}

	private static final class ArrayReadAccessor extends ReadOnlyAccessor<Object[]> {
		private final int _columnIndex;

		ArrayReadAccessor(int columnIndex) {
			_columnIndex = columnIndex;
		}

		@Override
		public Object getValue(Object[] object, String property) {
			return object[_columnIndex];
		}
	}

}
