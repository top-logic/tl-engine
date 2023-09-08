/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.list.model;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.event.ListDataEvent;

import junit.framework.TestCase;

import com.top_logic.layout.list.model.ListSelectionManager;

/**
 * {@link TestCase} for {@link ListSelectionManager}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
@SuppressWarnings("javadoc")
public class TestListSelectionManager extends TestCase {

	private DefaultListModel listModel;
	private DefaultListSelectionModel selectionModel;
	private ListSelectionManager manager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		listModel = setupListModel();
		selectionModel = setupSelectionModel();
		manager = new ListSelectionManager(selectionModel);
	}


	private DefaultListModel setupListModel() {
		DefaultListModel listModel = new DefaultListModel();
		listModel.addElement("1");
		return listModel;
	}

	private DefaultListSelectionModel setupSelectionModel() {
		DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
		selectionModel.setSelectionInterval(0, 0);
		return selectionModel;
	}

	private ListDataEvent createListDataEvent(int selectionAddIndex) {
		return new ListDataEvent(listModel, ListDataEvent.INTERVAL_ADDED, selectionAddIndex, selectionAddIndex);
	}

	private void assertAnchorAndLeadIndex(int listIndex) {
		assertEquals("Anchor index does not match", listIndex, selectionModel.getAnchorSelectionIndex());
		assertEquals("Lead index does not match", listIndex, selectionModel.getLeadSelectionIndex());
	}

	public void testAddBeforeSelection() {
		int firstListIndex = 0;
		int secondListIndex = 1;
		listModel.add(firstListIndex, "0");
		manager.intervalAdded(createListDataEvent(firstListIndex));
		assertAnchorAndLeadIndex(secondListIndex);
	}

	public void testAddAfterSelection() {
		int firstListIndex = 0;
		int secondListIndex = 1;
		listModel.addElement("2");
		manager.intervalAdded(createListDataEvent(secondListIndex));
		assertAnchorAndLeadIndex(firstListIndex);
	}

	public void testAddToLastPositionAfterLastPositionRemoval() {
		int firstListIndex = 0;
		listModel.removeAllElements();
		selectionModel.removeIndexInterval(firstListIndex, firstListIndex);
		listModel.addElement("1");
		manager.intervalAdded(createListDataEvent(firstListIndex));
		assertAnchorAndLeadIndex(firstListIndex);
	}
}
