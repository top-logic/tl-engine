/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.control;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.layout.TestControl;

import com.top_logic.basic.Logger;
import com.top_logic.layout.SimpleAccessor;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.resources.SimpleResourceView;
import com.top_logic.layout.table.DefaultTableData;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.control.EditableTableControl;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.control.TableControl.SelectAction;
import com.top_logic.layout.table.model.SelectionTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;

/**
 * The class {@link TestEditableTableControl} tests miscellaneous of {@link EditableTableControl}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestEditableTableControl extends TestControl {

	private static final String COL_1 = "this";

	public void testMoveRowUp() {
		List options = Arrays.asList(new String[] { "zero", "one", "two" });
		TableControl theCtrl = createControl(options);

		writeControl(theCtrl);
		selectRow(theCtrl, 1);

		int col1 = col1Index(theCtrl);
		Object obj0 = theCtrl.getViewModel().getValueAt(0, col1);
		Object obj1 = theCtrl.getViewModel().getValueAt(1, col1);
		executeControlCommand(theCtrl, EditableTableControl.UP_COMMAND_NAME, Collections.<String, Object> emptyMap());
		assertEquals(obj1, theCtrl.getViewModel().getValueAt(0, col1));
		assertEquals(obj0, theCtrl.getViewModel().getValueAt(1, col1));
	}

	public void testMoveRowDown() {
		List options = Arrays.asList(new String[] { "zero", "one", "two" });
		TableControl theCtrl = createControl(options);

		writeControl(theCtrl);
		selectRow(theCtrl, 1);

		int col1 = col1Index(theCtrl);
		Object obj1 = theCtrl.getViewModel().getValueAt(1, col1);
		Object obj2 = theCtrl.getViewModel().getValueAt(2, col1);
		executeControlCommand(theCtrl, EditableTableControl.DOWN_COMMAND_NAME, Collections.<String, Object> emptyMap());
		assertEquals(obj2, theCtrl.getViewModel().getValueAt(1, col1));
		assertEquals(obj1, theCtrl.getViewModel().getValueAt(2, col1));
	}

	public void testMoveRowBottom() {
		List options = Arrays.asList(new String[] { "zero", "one", "two", "three" });
		TableControl theCtrl = createControl(options);

		writeControl(theCtrl);
		selectRow(theCtrl, 1);

		int col1 = col1Index(theCtrl);
		Object obj1 = theCtrl.getViewModel().getValueAt(1, col1);
		Object obj2 = theCtrl.getViewModel().getValueAt(2, col1);
		Object obj3 = theCtrl.getViewModel().getValueAt(3, col1);
		executeControlCommand(theCtrl, EditableTableControl.BOTTOM_COMMAND_NAME,
			Collections.<String, Object> emptyMap());
		assertEquals(obj2, theCtrl.getViewModel().getValueAt(1, col1));
		assertEquals(obj3, theCtrl.getViewModel().getValueAt(2, col1));
		assertEquals(obj1, theCtrl.getViewModel().getValueAt(3, col1));
	}

	public void testMoveRowTop() {
		List options = Arrays.asList(new String[] { "zero", "one", "two", "three" });
		TableControl theCtrl = createControl(options);

		writeControl(theCtrl);
		selectRow(theCtrl, 2);

		int col1 = col1Index(theCtrl);
		Object obj0 = theCtrl.getViewModel().getValueAt(0, col1);
		Object obj1 = theCtrl.getViewModel().getValueAt(1, col1);
		Object obj2 = theCtrl.getViewModel().getValueAt(2, col1);
		executeControlCommand(theCtrl, EditableTableControl.TOP_COMMAND_NAME, Collections.<String, Object> emptyMap());
		assertEquals(obj2, theCtrl.getViewModel().getValueAt(0, col1));
		assertEquals(obj0, theCtrl.getViewModel().getValueAt(1, col1));
		assertEquals(obj1, theCtrl.getViewModel().getValueAt(2, col1));
	}

	private void selectRow(TableControl ctrl, int row) {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put(SelectAction.ROW_PARAM, row);
		arguments.put(SelectAction.COLUMN_PARAM, col1Index(ctrl));
		executeControlCommand(ctrl, TableControl.TABLE_SELECT_COMMAND,
			arguments);
	}

	private int col1Index(TableControl theCtrl) {
		return theCtrl.getViewModel().getHeader().getColumn(COL_1).getIndex();
	}

	private TableControl createControl(List options) {
		SelectField theSelectField = FormFactory.newSelectField("select", options, true, options, false);
		theSelectField.setCustomOrder(true);

		String[] columnNames = { COL_1 };
		TableConfiguration config = TableConfigurationFactory.table();
		config.setResPrefix(SimpleResourceView.INSTANCE);
		config.getDefaultColumn().setAccessor(SimpleAccessor.INSTANCE);
		SelectionTableModel theModel = new SelectionTableModel(theSelectField, columnNames, config);

		DefaultTableRenderer theRenderer = DefaultTableRenderer.newInstance();

		TableData tableData =
			DefaultTableData.createAnonymousTableData(theModel);
		EditableTableControl theCtrl = new EditableTableControl(tableData, theRenderer,
			EditableTableControl.ROW_MOVE);
		theCtrl.setSelectable(true);
		return theCtrl;
	}

	/**
	 * Return the suite of Tests to execute.
	 */
	public static Test suite() {
		return TestControl.suite(TestEditableTableControl.class);
	}

	/**
	 * main function for direct execution.
	 */
	public static void main(String[] args) {

		Logger.configureStdout("DEBUG");
		TestRunner.run(suite());
	}

}
