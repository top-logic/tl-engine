/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.list.model;

import javax.swing.DefaultListModel;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.col.filter.NotEqualsFilter;
import com.top_logic.layout.list.model.FilteredListModel;

/**
 * Test case for {@link FilteredListModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestFilteredListModel extends AbstractListModelTest {

	private static final Filter<? super Integer> ODD_AND_NOT_11 =
		FilterFactory.and(ODD, new NotEqualsFilter(Integer.valueOf(11)));

	public void testFilterChange() {
		
		DefaultListModel baseModel = newBaseModel();
		
		FilteredListModel testedModel = new FilteredListModel(baseModel);
		ListModelTester tester = new ListModelTester(testedModel);
		tester.attach();
		
		updateFilter(testedModel, tester, LESS_THAN_80);
		updateFilter(testedModel, tester, LESS_THAN_40);
		updateFilter(testedModel, tester, GREATER_THAN_60);
		updateFilter(testedModel, tester, ODD);
		updateFilter(testedModel, tester, EVEN);
		
		tester  = new ListModelTester(testedModel,50);
		updateFilter(testedModel, tester, LESS_THAN_80);
	}
	
	public void testBaseChange() {
		DefaultListModel baseModel = newBaseModel();
		
		FilteredListModel testedModel = new FilteredListModel(baseModel);
		ListModelTester tester = new ListModelTester(testedModel);
		tester.attach();
		
		updateFilter(testedModel, tester, ODD);
		
		baseModel.removeRange(10, 10);
		check(testedModel, tester, ODD);

		baseModel.removeRange(10, 10);
		check(testedModel, tester, ODD_AND_NOT_11);
		
		baseModel.removeRange(10, 10);
		check(testedModel, tester, ODD_AND_NOT_11);
	}

	private void updateFilter(FilteredListModel testedModel, ListModelTester tester, Filter predicate) {
		testedModel.setFilter(predicate);
		check(testedModel, tester, predicate);
	}

	private void check(FilteredListModel testedModel, ListModelTester tester, Filter predicate) {
		if (testedModel.isAttached()) {
			checkContents(predicate, testedModel);
		}
		tester.check();
	}

}
