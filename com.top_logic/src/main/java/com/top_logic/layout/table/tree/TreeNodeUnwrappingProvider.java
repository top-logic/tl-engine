/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree;

import java.util.Set;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.filter.AllCellsExist;
import com.top_logic.layout.table.filter.CellExistenceTester;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * {@link TableConfigurationProvider} that wraps {@link TreeTableAccessor} and
 * {@link TreeTableCellTester} around column accessors and column cell existence tester of table.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public final class TreeNodeUnwrappingProvider extends AbstractConfiguredInstance<TreeNodeUnwrappingProvider.Config>
		implements TableConfigurationProvider {

	/** Static instance of {@link TreeNodeUnwrappingProvider} */
	public static final TreeNodeUnwrappingProvider INSTANCE =
		TypedConfigUtil.createInstance(TypedConfiguration.newConfigItem(TreeNodeUnwrappingProvider.Config.class));

	/**
	 * Typed configuration interface definition for {@link TreeNodeUnwrappingProvider}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<TreeNodeUnwrappingProvider> {

		/** Configuration name for the option {@link #getExcludeColumns()}. */
		String EXCLUDE_COLUMNS = "exclude-columns";

		/**
		 * Set of column names for which the {@link TreeNodeUnwrappingProvider} must <b>not</b> be
		 * applied. The {@link TableControl#SELECT_COLUMN_NAME} is also untouched.
		 * 
		 * <p>
		 * The columns in the configured set remain untouched, i.e. the configured accessor gets the
		 * {@link TLTreeNode} as object.
		 * </p>
		 */
		@Format(CommaSeparatedStringSet.class)
		@Name(EXCLUDE_COLUMNS)
		Set<String> getExcludeColumns();

		/**
		 * Setter for {@link #getExcludeColumns()}.
		 */
		void setExcludeColumns(Set<String> excludeColumns);

	}

	/**
	 * Create a {@link TreeNodeUnwrappingProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public TreeNodeUnwrappingProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
		/* Need to adapt the default column, because for the configured "defaultColumns" there are
		 * no declared columns to adapt in "adaptConfigurationTo". */
		adaptColumn(defaultColumn);
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		for (ColumnConfiguration columnConfiguration : table.getElementaryColumns()) {
			String columnName = columnConfiguration.getName();
			if (TableControl.SELECT_COLUMN_NAME.equals(columnName)
				|| getConfig().getExcludeColumns().contains(columnName)) {
				// Ignore select column and excluded columns.
				continue;
			}
			adaptColumn(columnConfiguration);
		}
	}

	private void adaptColumn(ColumnConfiguration columnConfiguration) {
		addNodeUnwrappingAccessor(columnConfiguration);
		addUnwrappingCellExistenceTester(columnConfiguration);
	}

	private void addNodeUnwrappingAccessor(ColumnConfiguration columnConfiguration) {
		Accessor<?> accessor = columnConfiguration.getAccessor();
		if (accessor == null) {
			return;
		}
		if (accessor instanceof TreeTableAccessor) {
			// Do not wrap twice
			return;
		}
		columnConfiguration.setAccessor(new TreeTableAccessor(accessor));
	}

	private void addUnwrappingCellExistenceTester(ColumnConfiguration columnConfiguration) {
		CellExistenceTester tester = columnConfiguration.getCellExistenceTester();
		if (tester == null || tester instanceof AllCellsExist) {
			return;
		}
		if (tester instanceof TreeTableCellTester) {
			// Do not wrap twice
			return;
		}
		columnConfiguration.setCellExistenceTester(new TreeTableCellTester(tester));
	}

}