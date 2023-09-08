/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.tag;

import java.util.Arrays;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.AbstractLayoutTest;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.form.PlainKeyResources;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.tag.SelectionTableTag;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.NoDefaultColumnAdaption;
import com.top_logic.layout.table.model.SetTableResPrefix;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;

/**
 * {@link AbstractLayoutTest} for {@link SelectionTableTag}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
@SuppressWarnings("javadoc")
public class TestSelectionTableTag extends AbstractLayoutTest {

	private SelectField _selectField;
	private SelectionTableTag _selectionTableTag;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		FormContext formContext = new FormContext("formContext", ResPrefix.GLOBAL);
		_selectField = FormFactory.newSelectField("testField", Arrays.asList(new String[] { "1" }));
		formContext.addMember(_selectField);
		_selectionTableTag = new SelectionTableTag();
	}

	public void testUseSelectFieldResourcePrefixExplicitColumnsOnly() {
		_selectionTableTag.setColumnNames("column1");

		PlainKeyResources selectFieldResourceView = PlainKeyResources.INSTANCE;
		_selectField.setResources(selectFieldResourceView);
		assertResourcePrefix(selectFieldResourceView);
	}

	public void testUseSelectFieldResourcePrefixTableConfigColumnsOnly() {
		PlainKeyResources selectFieldResourceView = PlainKeyResources.INSTANCE;
		_selectField.setResources(selectFieldResourceView);
		_selectField.setTableConfigurationProvider(TestSingleColumnProvider.INSTANCE);
		assertResourcePrefix(selectFieldResourceView);
	}

	public void testUseSelectFieldResourcePrefixCombinedColumns() {
		PlainKeyResources selectFieldResourceView = PlainKeyResources.INSTANCE;
		_selectField.setResources(selectFieldResourceView);
		_selectField.setTableConfigurationProvider(TestSingleColumnProvider.INSTANCE);
		_selectionTableTag.setColumnNames("column2");
		assertResourcePrefix(selectFieldResourceView);
	}
	
	public void testUseTableConfigResourcePrefix() {
		_selectField.setResources(PlainKeyResources.INSTANCE);
		_selectField.setTableConfigurationProvider(new SetTableResPrefix(ResPrefix.GLOBAL));
		_selectionTableTag.setColumnNames("column1");
		assertResourcePrefix(ResPrefix.GLOBAL);
	}

	public void testJspColumnsOnly() {
		_selectionTableTag.setColumnNames("column1 column2");
		assertColumns("column1", "column2");
	}

	public void testTableConfigDefaultColumnsOnly() {
		_selectField.setTableConfigurationProvider(
			TableConfigurationFactory.setDefaultColumns(Arrays.asList("column1", "column2")));
		assertColumns("column1", "column2");
	}

	public void testTableConfigDeclaredColumnsOnly() {
		_selectField.setTableConfigurationProvider(new NoDefaultColumnAdaption() {

			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				table.declareColumn("column1");
				table.declareColumn("column2");
			}
		});
		assertColumns("column1", "column2");
	}

	public void testJSPColumnsOverTableConfigDefaultColumns() {
		_selectField.setTableConfigurationProvider(
			TableConfigurationFactory.setDefaultColumns(Arrays.asList("column1", "column2")));
		_selectionTableTag.setColumnNames("column3 column4");
		assertColumns("column3", "column4");
	}

	private void assertColumns(String... expectedColumnNames) {
		TableControl tableControl = (TableControl) _selectionTableTag.createControl(_selectField);
		tableControl.setSelectable(false);
		List<String> actualColumns = tableControl.getViewModel().getColumnNames();
		assertEquals(expectedColumnNames.length, actualColumns.size());
		for (int i = 0; i < expectedColumnNames.length; i++) {
			assertEquals(expectedColumnNames[i], actualColumns.get(i));
		}
	}

	private void assertResourcePrefix(ResourceView resourceView) {
		TableControl tableControl = (TableControl) _selectionTableTag.createControl(_selectField);
		assertEquals(resourceView, getTableResourcePrefix(tableControl));
	}

	private ResourceView getTableResourcePrefix(TableControl tableControl) {
		return tableControl.getTableData().getTableModel().getTableConfiguration().getResPrefix();
	}

	public static Test suite() {
		return suite(TestSelectionTableTag.class);
	}

	private static final class TestSingleColumnProvider extends NoDefaultColumnAdaption {

		static final TestSingleColumnProvider INSTANCE = new TestSingleColumnProvider();

		@Override
		public void adaptConfigurationTo(TableConfiguration table) {
			table.declareColumn("column1");
		}
	}

}
