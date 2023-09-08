/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.layout.SimpleAccessor;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.table.model.SelectionTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;

/**
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestSelectionTableModel extends BasicTestCase {
	
	private Vector<?> options;
	private SelectionTableModel tableModel;
	private SelectField selectField;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		options = createOptions();
		selectField =
			FormFactory.newSelectField("tested list", new ArrayList(options), true, new ArrayList(options), false);
		selectField.setCustomOrder(true);
		TableConfiguration config = TableConfigurationFactory.table();
		config.getDefaultColumn().setAccessor(SimpleAccessor.INSTANCE);
		tableModel = new SelectionTableModel(selectField, new String[] { "a", "b", "c" }, config);
	}

	public void testSelectionUpdate() {
		ObjectTableModelTester tester = new ObjectTableModelTester(tableModel, 3);
		tester.attach();
		assertEquals(options, tester.getObservedRows());
		
		options.remove(1);
		options.remove(1);
		options.remove(1);
		selectField.setAsSelection(new ArrayList(options));
		assertEquals(options, tester.getObservedRows());
		
		options.remove(1);
		selectField.setAsSelection(new ArrayList(options));
		tester.attach();
		assertEquals(options, tester.getObservedRows());
		
		options.remove(1);
		selectField.setAsSelection(new ArrayList(options));
		tester.attach();
		assertEquals(options, tester.getObservedRows());
		
		options.remove(1);
		selectField.setAsSelection(new ArrayList(options));
		tester.attach();
		assertEquals(options, tester.getObservedRows());

		tester.detach();
		options.remove(1);
		selectField.setAsSelection(new ArrayList(options));
		assertNotEquals(options, tester.getObservedRows());

		tester.attach();
		assertEquals(options, tester.getObservedRows());
	}

	public void testRemoveRowFromSelection() throws Exception {
		SelectionTableModel.beginModification(tableModel);
		List<?> subList = new ArrayList<Object>(options.subList(1, 4));
		selectField.setAsSelection(new ArrayList<>(subList));

		subList.remove(1);
		tableModel.removeRow(1);
		assertEquals(subList, selectField.getSelection());

		SelectionTableModel.endModification(tableModel);
	}

	public void testRemoveMultipleRowsFromSelection() throws Exception {
		SelectionTableModel.beginModification(tableModel);
		List<?> subList = new ArrayList<Object>(options.subList(1, 4));
		selectField.setAsSelection(new ArrayList<>(subList));

		subList.remove(1);
		subList.remove(1);
		tableModel.removeRows(1, 3);
		assertEquals(subList, selectField.getSelection());

		SelectionTableModel.endModification(tableModel);
	}

	public void testMoveRowInSelection() throws Exception {
		SelectionTableModel.beginModification(tableModel);
		List<Object> subList = new ArrayList<>(options.subList(1, 4));
		selectField.setAsSelection(new ArrayList<>(subList));

		Object movedRow = subList.remove(1);
		subList.add(2, movedRow);
		tableModel.moveRow(1, 2);
		assertEquals(subList, selectField.getSelection());

		SelectionTableModel.endModification(tableModel);
	}

	public void testInsertRowInSelection() throws Exception {
		SelectionTableModel.beginModification(tableModel);
		List<Object> subList = new ArrayList<>(options.subList(1, 4));
		selectField.setAsSelection(new ArrayList<>(subList));

		int insertPosition = 2;
		Object newRow = options.get(5);
		subList.add(insertPosition, newRow);
		tableModel.insertRowObject(insertPosition, newRow);
		assertEquals(subList, selectField.getSelection());

		SelectionTableModel.endModification(tableModel);
	}

	public void testInsertMultipleRowsInSelection() throws Exception {
		SelectionTableModel.beginModification(tableModel);
		List<Object> subList = new ArrayList<>(options.subList(1, 4));
		selectField.setAsSelection(new ArrayList<>(subList));

		int insertPosition = 2;
		List<? extends Object> newRows = options.subList(5, 7);
		subList.add(insertPosition, newRows.get(0));
		subList.add(insertPosition + 1, newRows.get(1));
		tableModel.insertRowObject(insertPosition, newRows);
		assertEquals(subList, selectField.getSelection());

		SelectionTableModel.endModification(tableModel);
	}

	private Vector<?> createOptions() {
		Vector<Object> options = new Vector<>(100);
		for (int i = 0 ; i < 100;i++){
			Vector<Object> v = new Vector<>();
			v.add("a" + Integer.toString(i));
			v.add("b" + Integer.toString(i));
			v.add("c" + Integer.toString(i));
			options.add(v);
		}
		return options;
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(TestSelectionTableModel.class, TableConfigurationFactory.Module.INSTANCE));
	}

}
