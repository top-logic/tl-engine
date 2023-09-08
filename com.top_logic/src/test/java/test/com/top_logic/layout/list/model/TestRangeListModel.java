/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.list.model;

import javax.swing.DefaultListModel;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.list.model.RangeListModel;

/**
 * Test case for {@link RangeListModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestRangeListModel extends AbstractListModelTest {

	private final class IntRangeFilter implements Filter {
		private final int start;
		private final int stop;

		/*package protected*/ IntRangeFilter(int start, int stop) {
			this.start = start;
			this.stop = stop;
		}

		@Override
		public boolean accept(Object anObject) {
			int value = ((Integer) anObject).intValue();
			return value >= start && value < stop;
		}
	}

	public void testRangeChange() {
		
		DefaultListModel baseModel = newBaseModel();
		
		RangeListModel testedModel = new RangeListModel(baseModel);
		ListModelTester tester = new ListModelTester(testedModel);

		tester.attach();
		
		tester.check();
		
		updateRange(testedModel, tester, 10, 90);
		updateRange(testedModel, tester, 20, 100);
		updateRange(testedModel, tester, 0, 10);
		updateRange(testedModel, tester, 90, 100);
		updateRange(testedModel, tester, 5, 95);

		tester = new ListModelTester(testedModel);
		tester.attach();
		
		tester.check();
		
		updateRange(testedModel, tester, 10, 90);
	}

	private void updateRange(RangeListModel testedModel, ListModelTester tester, int start, int stop) {
		testedModel.setRange(start, stop - start);
		checkContents(new IntRangeFilter(start, stop), testedModel);
		tester.check();
	}
	
}
