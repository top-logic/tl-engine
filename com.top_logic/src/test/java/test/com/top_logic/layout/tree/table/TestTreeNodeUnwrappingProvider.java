/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.table;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.layout.IdentityAccessor;
import com.top_logic.layout.table.filter.AllCellsExist;
import com.top_logic.layout.table.filter.WrapperValueExistenceTester;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.tree.TreeNodeUnwrappingProvider;
import com.top_logic.layout.table.tree.TreeTableAccessor;
import com.top_logic.layout.table.tree.TreeTableCellTester;

/**
 * {@link BasicTestCase} of {@link TreeNodeUnwrappingProvider}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
@SuppressWarnings("javadoc")
public class TestTreeNodeUnwrappingProvider extends BasicTestCase {

	public void testWrapElementaryColumns() {
		TableConfiguration tableConfiguration = TableConfigurationFactory.emptyTable();
		ColumnConfiguration groupColumn = tableConfiguration.declareColumn("group");
		ColumnConfiguration firstElementaryColumn = groupColumn.declareColumn("column1");
		setAccessor(firstElementaryColumn);
		setCellExistenceTester(firstElementaryColumn);
		ColumnConfiguration secondElementaryColumn = groupColumn.declareColumn("column2");
		setAccessor(secondElementaryColumn);
		setCellExistenceTester(secondElementaryColumn);

		TreeNodeUnwrappingProvider.INSTANCE.adaptConfigurationTo(tableConfiguration);
		assertTreeNodeAccessor(firstElementaryColumn, secondElementaryColumn);
		assertTreeNodeCellTester(firstElementaryColumn, secondElementaryColumn);
	}

	public void testNoUnnecessaryWrapping() {
		TableConfiguration table = TableConfigurationFactory.emptyTable();
		ColumnConfiguration firstElementaryColumn = table.declareColumn("column1");
		assertEquals("Check default value", null, firstElementaryColumn.getAccessor());
		assertEquals("Check default value", AllCellsExist.INSTANCE, firstElementaryColumn.getCellExistenceTester());

		TreeNodeUnwrappingProvider.INSTANCE.adaptConfigurationTo(table);
		assertEquals("Null must not be wrapped.", null, firstElementaryColumn.getAccessor());
		assertEquals("AllCellsExist must not be wrapped.", AllCellsExist.INSTANCE,
			firstElementaryColumn.getCellExistenceTester());

	}

	private void setAccessor(ColumnConfiguration columnConfiguration) {
		columnConfiguration.setAccessor(IdentityAccessor.INSTANCE);
	}

	private void setCellExistenceTester(ColumnConfiguration columnConfiguration) {
		columnConfiguration.setCellExistenceTester(WrapperValueExistenceTester.INSTANCE);
	}

	private void assertTreeNodeAccessor(ColumnConfiguration... columnConfigurations) {
		for (ColumnConfiguration columnConfiguration : columnConfigurations) {
			assertInstanceof(columnConfiguration.getAccessor(), TreeTableAccessor.class);
		}
	}

	private void assertTreeNodeCellTester(ColumnConfiguration... columnConfigurations) {
		for (ColumnConfiguration columnConfiguration : columnConfigurations) {
			assertInstanceof(columnConfiguration.getCellExistenceTester(), TreeTableCellTester.class);
		}
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(TestTreeNodeUnwrappingProvider.class, TableConfigurationFactory.Module.INSTANCE));
	}

}
