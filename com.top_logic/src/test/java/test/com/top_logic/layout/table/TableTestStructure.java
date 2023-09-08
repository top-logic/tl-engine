/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Permutation;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.basic.ConstantControl;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.resources.SimpleResourceView;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.DefaultTableData;
import com.top_logic.layout.table.DefaultTableData.NoTableDataOwner;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.mig.html.HTMLConstants;

/**
 * @author     <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TableTestStructure {

	private final TableConfiguration _tableConfig;

	public final ArrayList<Integer> rowObjects;

	public final ObjectTableModel applicationModel;

	public final TableControl tableControl;

	public TestingConfigKey configKey;

	private TableData tableData;

	public static final String COL_A = "A";

	public static final String COL_B = "B";

	public static final String COL_C = "C";

	public static final String COL_D = "D";

	public static final String COL_E = "E";

	public static final String COL_F = "F";

	public static final int COL_A_INDEX = 0;

	public static final int COL_B_INDEX = 1;

	public static final int COL_C_INDEX = 2;

	public static final int COL_D_INDEX = 3;

	public static final int COL_E_INDEX = 4;

	public static final int COL_F_INDEX = 5;

	/**
	 * Default number of application model rows.
	 */
	public static final int ROW_COUNT = 1000;

	/**
	 * Default test page size.
	 */
	public static final int PAGE_SIZE = TableViewModel.INITIAL_VIEW_PORT_ROW_COUNT;

	/**
	 * Default test columns.
	 */
	public static final String[] TEST_COLUMNS = { COL_A, COL_B, COL_C, COL_D, COL_E, COL_F };

	/**
	 * Accessor for {@link #applicationModel}.
	 */
	public static final Accessor<Object> TEST_VALUES = new ReadOnlyAccessor<>() {
		@Override
		public Object getValue(Object object, String property) {
			return property + object.toString();
		}
	};

	/**
	 * Dummy control for testing tables with active contents.
	 * 
	 * <p>
	 * Writes the {@link Object#toString()} representation of its {@link #getModel()} in a
	 * {@link HTMLConstants#SPAN} tag.
	 * </p>
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class TestCellControl extends ConstantControl<Object> {

		public TestCellControl(Object model) {
			super(model);
		}

		@Override
		protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
			out.beginBeginTag(HTMLConstants.SPAN);
			writeControlAttributes(context, out);
			out.endBeginTag();
			out.writeText(this.getModel().toString());
			out.endTag(HTMLConstants.SPAN);
		}
	}

	public static final int PAGE_COUNT = (TableTestStructure.ROW_COUNT + (TableTestStructure.PAGE_SIZE - 1))
		/ TableTestStructure.PAGE_SIZE;

	public static final int LAST_PAGE = PAGE_COUNT > 0 ? PAGE_COUNT - 1 : 0;

	/**
	 * The application model row that is the first one on the {@link #LAST_PAGE}.
	 */
	public static final int LAST_PAGE_FIRST_ROW = LAST_PAGE * TableTestStructure.PAGE_SIZE;

	/**
	 * Number of rows on the {@link #LAST_PAGE}.
	 */
	public static final int LAST_PAGE_ROWS = TableTestStructure.ROW_COUNT - LAST_PAGE_FIRST_ROW;

	/**
	 * {@link ControlProvider} creating {@link TestCellControl}s.
	 */
	public static final ControlProvider TEST_CP = new ControlProvider() {
		@Override
		public Control createControl(Object model, String style) {
			return new TestCellControl(model);
		}
	};

	/**
	 * Workaround for {@link TableFilter} requiring {@link XMLProperties} in its static initializer.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class DeferredLoading {

		/**
		 * Filters rows 100 to 111 inclusive.
		 */
		public static final Filter<Object> FILTER_1 = new Filter<>() {
			@Override
			public boolean accept(Object anObject) {
				int value = Integer.parseInt(((String) anObject).substring(1));
				return value >= 100 && value <= 111;
			}

		};

		/**
		 * Filters odd rows.
		 */
		public static final Filter<Object> FILTER_2 = new Filter<>() {

			@Override
			public boolean accept(Object anObject) {
				int value = Integer.parseInt(((String) anObject).substring(1));
				return value % 2 == 1;
			}

		};

	}

	public interface TableControlProvider {
		default ResourceView getTableResources() {
			return SimpleResourceView.INSTANCE;
		}
		TableControl createTableControl(TableData model);
	}

	private static AtomicInteger counter = new AtomicInteger();

	public TableTestStructure(TableControlProvider provider) {
		this._tableConfig = createTableConfiguration(provider.getTableResources());
		this.rowObjects = createRowObjects();
		this.applicationModel = createApplicationModel(_tableConfig, rowObjects);
		this.configKey = new TestingConfigKey(getClass().getName() + "_" + counter.getAndIncrement());
		this.tableData = createTableViewModel(applicationModel, configKey);
		this.tableControl = provider.createTableControl(tableData);
	}

	private static TableConfiguration createTableConfiguration(ResourceView resourceView) {
		TableConfiguration tableConfig = TableConfiguration.table();
		tableConfig.setResPrefix(resourceView);
		tableConfig.getDefaultColumn().setAccessor(TableTestStructure.TEST_VALUES);

		ColumnConfiguration cColumn = tableConfig.declareColumn(COL_C);
		cColumn.setControlProvider(TEST_CP);
		cColumn.setShowHeader(false);

		return tableConfig;
	}

	private ArrayList<Integer> createRowObjects() {
		return Permutation.identity(ROW_COUNT);
	}

	private ObjectTableModel createApplicationModel(TableConfiguration tableConfig, List<?> rowObjects) {
		return new ObjectTableModel(TableTestStructure.TEST_COLUMNS, tableConfig, rowObjects);
	}

	private static TableData createTableViewModel(TableModel applicationModel, ConfigKey key) {
		TableData tableData =
			DefaultTableData.createTableData(NoTableDataOwner.INSTANCE, applicationModel, key);
		TableViewModel tableModel = tableData.getViewModel();
		tableModel.getPagingModel().setPageSizeSpec(PAGE_SIZE);
		return tableData;
	}

	public TableViewModel getViewModel() {
		return getTableData().getViewModel();
	}

	public TableData getTableData() {
		return tableData;
	}

}
