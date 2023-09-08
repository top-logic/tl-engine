/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.list.model;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.top_logic.layout.list.model.ListUpdateAccumulator;

/**
 * Test case for {@link ListUpdateAccumulator}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestListUpdateAccumulator extends TestCase {
	
	public void testSimpleUpdates() {
		DefaultListModel model = new DefaultListModel();
		ListSelectionModel selection = new DefaultListSelectionModel();
		
		ListUpdateAccumulator accumulator = new ListUpdateAccumulator(model, selection);
		
		accumulator.attach();
		
		// Initial insert.
		for (int n = 0; n < 50; n++) {
			model.addElement(Integer.valueOf(n));
		}

		// Model: 0, 1, ... 49
		
		// Is reported as single add.
		check(accumulator, new ListDataEventChecker(new AddCheck(0, 49)));

		
		// Consecutive deletions without resetting the accumulator report a
		// single smaller add.
		for (int n = 19; n >= 10; n--) {
			model.remove(n);
		}

		// Model: 0, 1, ..., 9, [10-19 missing] 20, 21, ..., 49
		
		check(accumulator, new ListDataEventChecker(new AddCheck(0, 39)));
		
		accumulator.detach();
		
		accumulator.attach();

		Assert.assertFalse("No changes after attach.", accumulator.hasUpdates());
		
		// Directly after an attach, the accumulator reports an empty change set.
		check(accumulator, new ListDataEventChecker());
		

		// Consecutive deletions without resetting the accumulator report a
		// single smaller add.
		for (int n = 5; n < 10; n++) {
			model.add(n, Integer.valueOf(-n));
		}

		// Model: 0, 1, ..., 4, -5, -6, -7, -8, -9, 5, 6, ..., 9, [10-19 missing] 20, 21, ..., 49

		check(accumulator, new ListDataEventChecker(new AddCheck(5, 9)));

		model.removeRange(5, 9);
		
		// Removing all the added entries generates no event, because the
		// accumulator can determine that the list is in its initial state.
		check(accumulator, new ListDataEventChecker());
	}

	public void testComplexUpdates() {
		DefaultListModel model = new DefaultListModel();
		ListSelectionModel selection = new DefaultListSelectionModel();
		
		ListUpdateAccumulator accumulator = new ListUpdateAccumulator(model, selection);
		
		for (int n = 0; n < 10; n++) {
			model.addElement(Integer.valueOf(n));
		}
		
		selection.setSelectionInterval(3, 3);
		
		accumulator.attach();
		
		// Removing some elements.
		model.removeRange(4, 7);
		
		// Inserting at the index, where the removal started.
		model.add(4, Integer.valueOf(-1));
		model.add(5, Integer.valueOf(-1));
		model.add(6, Integer.valueOf(-1));
		
		// Changing a yet untouched index.
		model.set(3, Integer.valueOf(-1));
		
		// Changing an added index.
		model.set(5, Integer.valueOf(-1));
		
		// Adding has priority over removing. Therefore, the remove event is
		// reported last. The indexes of the remove event are adjusted to the 
		// state of the list just before the remove event.
		check(accumulator,
			new ListDataEventChecker(new ChangeCheck(3, 3), new AddCheck(4,6), new RemoveCheck(7, 10)));

		accumulator.reset();
		
		selection.setSelectionInterval(5, 5);

		// Selection has changed at index 3 and 5. The default list selection
		// model fires a single change event for the whole index range [3, 5].
		check(accumulator, new ListDataEventChecker(new ChangeCheck(3, 5)));
	}
	
	public void testRemoveTail() {
		DefaultListModel model = new DefaultListModel();
		
		ListUpdateAccumulator accumulator = new ListUpdateAccumulator(model, null);
		
		for (int n = 0; n < 10; n++) {
			model.addElement(Integer.valueOf(n));
		}
		
		accumulator.attach();
		
		model.remove(9);

		check(accumulator, new ListDataEventChecker(new RemoveCheck(9, 9)));
	}

	private void check(ListUpdateAccumulator accumulator, ListDataEventChecker checker) {
		accumulator.forwardConsolidatedEvents(checker);
		Assert.assertFalse("All expected events have been delivered.", checker.expectsMoreEvents());
	}
}
