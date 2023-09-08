/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mig.html;

import junit.framework.Test;
import junit.framework.TestCase;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.mig.html.DefaultSelectionModelFactory;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelFactory;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.mig.html.DefaultSelectionModelFactory.Config;

/**
 * {@link TestCase} for {@link DefaultSelectionModelFactory}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestDefaultSelectionModelFactory extends BasicTestCase {

	public void testMultiSelection() {
		SelectionModel selectionModel = newSelectionModel(true, null);
		Object o1 = new Object();
		Object o2 = new Object();
		selectionModel.setSelected(o1, true);
		selectionModel.setSelected(o2, true);
		assertTrue(selectionModel.isSelected(o1));
		assertTrue(selectionModel.isSelected(o2));
	}

	public void testFilter() {
		SelectionModel selectionModel = newSelectionModel(true, FilterFactory.createClassFilter(Integer.class));
		Object o1 = new Object();
		selectionModel.setSelected(o1, true);
		assertFalse("Selection does not match filter", selectionModel.isSelected(o1));
		assertFalse(selectionModel.isSelectable(new Object()));
		assertTrue(selectionModel.isSelectable(Integer.valueOf(15)));
	}

	public void testSingleSelection() {
		SelectionModel selectionModel = newSelectionModel(false, null);
		Object o1 = new Object();
		Object o2 = new Object();
		selectionModel.setSelected(o1, true);
		assertTrue(selectionModel.isSelected(o1));
		selectionModel.setSelected(o2, true);
		assertTrue(selectionModel.isSelected(o2));
		assertFalse("Only one element can be selected", selectionModel.isSelected(o1));
	}

	private DefaultSelectionModelFactory.Config newConfig(boolean multiple, Filter filter) {
		Config config = TypedConfiguration.newConfigItem(DefaultSelectionModelFactory.Config.class);
		config.setMultiple(multiple);
		config.setFilter(filter);
		return config;
	}

	private SelectionModelFactory newFactory(boolean multiple, Filter filter) {
		DefaultSelectionModelFactory.Config config = newConfig(multiple, filter);
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	private SelectionModel newSelectionModel(boolean multiple, Filter filter) {
		SelectionModelFactory factory = newFactory(multiple, filter);
		return factory.newSelectionModel(SelectionModelOwner.NO_OWNER);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestDefaultSelectionModelFactory}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestDefaultSelectionModelFactory.class);
	}
}
