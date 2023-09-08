/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DefaultTestFactory;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.list.TestFastList;

import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.layout.table.filter.AbstractTableFLElementFilter;
import com.top_logic.model.TLClassifier;
import com.top_logic.util.model.ModelService;

/**
 * {@link BasicTestCase} of {@link AbstractTableFLElementFilter}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
@SuppressWarnings("javadoc")
public class TestAbstractTableFLElementFilter extends BasicTestCase {

	static class TestFilter extends AbstractTableFLElementFilter {

		static final TestFilter INSTANCE = new TestFilter();

		@Override
		public boolean accept(Object anObject) {
			return true;
		}

		@Override
		public TLClassifier extractFastListElement(Object anObject) {
			return super.extractFastListElement(anObject);
		}

		@Override
		public Collection<? extends TLClassifier> extractFastListElements(Object anObject) {
			return super.extractFastListElements(anObject);
		}
	}

	private FastList systemFastList;

	private TestFilter testFilter;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		testFilter = TestFilter.INSTANCE;
		systemFastList = FastList.getFastList(TestFastList.SYSTEM_LIST_NAME);
	}

	public void testExtractFirstElementFromList() throws Exception {
		List<TLClassifier> singletonList = Collections.singletonList(systemFastList.elements().get(0));
		TLClassifier element = testFilter.extractFastListElement(singletonList);
		assertEquals("Could not retrieve element from singleton list!", singletonList.get(0), element);
	}

	public void testExtractFirstElementFromSet() throws Exception {
		Set<TLClassifier> singletonSet = Collections.singleton(systemFastList.elements().get(0));
		TLClassifier element = testFilter.extractFastListElement(singletonSet);
		assertEquals("Could not retrieve element from singleton set!", singletonSet.iterator().next(), element);
	}

	public void testGetElementList() throws Exception {
		List<TLClassifier> expectedList = new ArrayList<>(systemFastList.elements());
		Collection<? extends TLClassifier> actualList = testFilter.extractFastListElements(expectedList);
		assertEquals("Could not retrieve element list!", expectedList, actualList);
	}

	public void testGetElementSet() throws Exception {
		Set<TLClassifier> expectedSet = new HashSet<>(systemFastList.elements());
		Collection<? extends TLClassifier> actualSet = testFilter.extractFastListElements(expectedSet);
		assertEquals("Could not retrieve element set!", expectedSet, actualSet);
	}

	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(TestAbstractTableFLElementFilter.class, new TestFactory() {
			@Override
			public Test createSuite(Class<? extends Test> testCase, String suiteName) {
				Test innerTest = DefaultTestFactory.INSTANCE.createSuite(testCase, suiteName);
				return ServiceTestSetup.createSetup(innerTest, ModelService.Module.INSTANCE);
			}
		});
	}

}
