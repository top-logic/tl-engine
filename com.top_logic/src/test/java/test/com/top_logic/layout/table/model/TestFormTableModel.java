/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.model;

import java.util.Arrays;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.table.model.ColumnConfig;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.layout.table.model.FieldProvider;
import com.top_logic.layout.table.model.FormTableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfig;
import com.top_logic.layout.table.model.TableConfigurationFactory;

/**
 * Test case for {@link FormTableModel}.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
@SuppressWarnings("javadoc")
public class TestFormTableModel extends BasicTestCase {
    
	public static final class TestingFieldProvider implements FieldProvider {
		@Override
		public FormMember createField(Object aModel, Accessor aAnAccessor,
				String aProperty) {
			return FormFactory.newStringField("a", null, false);
		}

		@Override
		public String getFieldName(Object model, Accessor anAccessor, String property) {
			return "a";
		}
	}

	public void testRemoveRowObject() throws ConfigurationException {

		TableConfig tableConfiguration = TableConfigurationFactory.tableConfig();
		ColumnConfig defaultColumn = tableConfiguration.getDefaultColumn();
		defaultColumn.setFieldProvider(TestTableConfigurationFactory.polyConfig(TestingFieldProvider.class));
		FormContainer formContext = new FormContext("form", ResPrefix.forTest("resPrefix"));
		List<String> rows = Arrays.asList(new String[] { "o1", "o2", "o3" });
		String[] columnNames = new String[] { "a", "b", "c" };
		EditableRowTableModel ertm = new ObjectTableModel(
			columnNames, TestTableConfigurationFactory.build(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, tableConfiguration), rows);
		FormTableModel formTableModel = new FormTableModel(ertm, formContext);
        
		int col1 = formTableModel.getHeader().getColumn("a").getIndex();
		formTableModel.getValueAt(0, col1);
		formTableModel.getValueAt(1, col1);
		formTableModel.getValueAt(2, col1);

        FormContainer theContainer = formTableModel.getGroupContainer();
        assertEquals("Number of row groups incorrect initially", 3, theContainer.size());
		formTableModel.removeRow(0);
        assertEquals("Number of row groups incorrect after remove", 2, theContainer.size());
        
    }
    
    /**
     * Return the suite of tests to perform. 
     */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(TestFormTableModel.class, TableConfigurationFactory.Module.INSTANCE));
	}

}
