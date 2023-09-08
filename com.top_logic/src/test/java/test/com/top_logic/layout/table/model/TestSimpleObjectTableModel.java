/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.model;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfigurationFactory;

/**
 * Tests {@link ObjectTableModel} without priority order.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestSimpleObjectTableModel extends TestObjectTableModel {

	public void testAddRowWithActiveFilter() {
		List<Integer> tableRows = list(0, 1, 2, 3);
		ObjectTableModel tableModel = createTableModelSimple(tableRows);

		setFilterZeroOrTwo(tableModel);

		tableModel.addRowObject(4);
		assertDisplayedRows(list(0, 2, 4), tableModel);

		setTrueFilter(tableModel);
		assertDisplayedRows(list(0, 1, 2, 3, 4), tableModel);
	}

	public void testAddMultipleRowsWithActiveFilter() {
		List<Integer> tableRows = list(0, 1, 2, 3);
		ObjectTableModel tableModel = createTableModelSimple(tableRows);

		setFilterZeroOrTwo(tableModel);

		tableModel.addAllRowObjects(list(4, 5, 6));
		assertDisplayedRows(list(0, 2, 4, 5, 6), tableModel);

		setTrueFilter(tableModel);
		assertDisplayedRows(list(0, 1, 2, 3, 4, 5, 6), tableModel);
	}

	@Override
	protected boolean createPriorityTable() {
		return false;
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(TestSimpleObjectTableModel.class, TableConfigurationFactory.Module.INSTANCE));
	}

}
