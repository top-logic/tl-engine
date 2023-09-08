/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.layout.AbstractLayoutTest;

import com.top_logic.basic.col.IdentityPermutation;
import com.top_logic.basic.listener.EventType;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.PageCountListener;
import com.top_logic.layout.table.model.PageListener;
import com.top_logic.layout.table.model.PageSizeListener;
import com.top_logic.layout.table.model.PageSizeOptionsListener;
import com.top_logic.layout.table.model.PagingModel;
import com.top_logic.layout.table.model.TableConfiguration;

/**
 * Test case for {@link PagingModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestPagingModel extends AbstractLayoutTest {

	public class EventTester implements PageCountListener, PageListener, PageSizeOptionsListener, PageSizeListener {

		public Set<EventType<?, ?, ?>> seenEvents = new HashSet<>();

		@Override
		public void handlePageSizeChanged(PagingModel sender, Integer oldValue, Integer newValue) {
			seenEvents.add(PagingModel.PAGE_SIZE_EVENT);
		}

		@Override
		public void handlePageSizeOptionsChanged(PagingModel sender, int[] oldValue, int[] newValue) {
			seenEvents.add(PagingModel.PAGE_SIZE_OPTIONS_EVENT);
		}

		@Override
		public void handlePageChanged(PagingModel sender, Integer oldValue, Integer newValue) {
			seenEvents.add(PagingModel.PAGE_EVENT);
		}

		@Override
		public void handlePageCountChanged(PagingModel sender, Integer oldValue, Integer newValue) {
			seenEvents.add(PagingModel.PAGE_COUNT_EVENT);
		}
	}

	private static final List<String> NO_COLUMNS = Collections.<String> emptyList();

	private EditableRowTableModel _tableModel;
	private PagingModel _pagingModel;
	private EventTester _eventTester;
	private ConfigKey _configKey;
	private PersonalConfiguration _personalConfiguration;
	private int[] _pageSizeOptions;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_personalConfiguration = PersonalConfiguration.getPersonalConfiguration();
		_tableModel = new ObjectTableModel(NO_COLUMNS, TableConfiguration.table(), new ArrayList<>());
		_configKey = ConfigKey.named(TestPagingModel.class.getSimpleName());
		_pagingModel = new PagingModel(_configKey, _tableModel);

		_pageSizeOptions = new int[] { 5, 10, 15, PagingModel.SHOW_ALL };
		_pagingModel.setPageSizeOptions(_pageSizeOptions);
		_eventTester = new EventTester();
		_pagingModel.addListener(PagingModel.PAGE_COUNT_EVENT, _eventTester);
		_pagingModel.addListener(PagingModel.PAGE_EVENT, _eventTester);
		_pagingModel.addListener(PagingModel.PAGE_SIZE_EVENT, _eventTester);
		_pagingModel.addListener(PagingModel.PAGE_SIZE_OPTIONS_EVENT, _eventTester);

		assertEquals(5, _pagingModel.getPageSizeSpec());
		assertTrue(_eventTester.seenEvents.isEmpty());
	}

	@Override
	protected void tearDown() throws Exception {
		_tableModel = null;
		_pagingModel = null;
		_eventTester = null;
		_personalConfiguration.setValue(_configKey.get() + PagingModel.PAGE_SIZE_PROPERTY_SUFFIX, null);

		super.tearDown();
	}

	public void testRowChange() {
		_tableModel.addAllRowObjects(new IdentityPermutation(19));

		assertEquals(4, _pagingModel.getPageCount());
		assertEvent(PagingModel.PAGE_COUNT_EVENT);
		clearEvents();

		_pagingModel.setPageSizeSpec(10);
		assertEquals(2, _pagingModel.getPageCount());
		assertEvent(PagingModel.PAGE_SIZE_EVENT);
		assertEvent(PagingModel.PAGE_COUNT_EVENT);
		clearEvents();

		_pagingModel.setPage(1);
		assertEvent(PagingModel.PAGE_EVENT);
		clearEvents();

		for (int n = 18; n >= 10; n--) {
			_tableModel.removeRow(n);
		}
		assertEquals(1, _pagingModel.getPageCount());
		assertEquals(0, _pagingModel.getPage());
		assertEvent(PagingModel.PAGE_EVENT);
		assertEvent(PagingModel.PAGE_COUNT_EVENT);
	}

	public void testOptionsChange() {
		_pagingModel.setPageSizeOptions(new int[] { 6, 10, 15 });
		assertEquals(6, _pagingModel.getPageSizeSpec());
		assertEvent(PagingModel.PAGE_SIZE_EVENT);
		assertEvent(PagingModel.PAGE_SIZE_OPTIONS_EVENT);
	}

	public void testPageSizes() {
		assertEquals(5, _pagingModel.getPageSizeSpec());
		assertEquals(5, _pagingModel.getEffectivePageSize());
		assertEquals(0, _pagingModel.getCurrentPageSize());

		_tableModel.addAllRowObjects(new IdentityPermutation(2));
		assertEquals(5, _pagingModel.getPageSizeSpec());
		assertEquals(5, _pagingModel.getEffectivePageSize());
		assertEquals(2, _pagingModel.getCurrentPageSize());

		_tableModel.addAllRowObjects(new IdentityPermutation(7).subList(2, 7));
		assertEquals(5, _pagingModel.getPageSizeSpec());
		assertEquals(5, _pagingModel.getEffectivePageSize());
		assertEquals(5, _pagingModel.getCurrentPageSize());

		_pagingModel.setPage(1);
		assertEquals(5, _pagingModel.getPageSizeSpec());
		assertEquals(5, _pagingModel.getEffectivePageSize());
		assertEquals(2, _pagingModel.getCurrentPageSize());
	}

	public void testShowAll() {
		_pagingModel.setPageSizeSpec(PagingModel.SHOW_ALL);
		assertEquals(PagingModel.SHOW_ALL, _pagingModel.getPageSizeSpec());
		assertEquals(0, _pagingModel.getEffectivePageSize());
		assertEquals(0, _pagingModel.getCurrentPageSize());

		_tableModel.addAllRowObjects(new IdentityPermutation(2));
		assertEquals(PagingModel.SHOW_ALL, _pagingModel.getPageSizeSpec());
		assertEquals(2, _pagingModel.getEffectivePageSize());
		assertEquals(2, _pagingModel.getCurrentPageSize());

		_tableModel.addAllRowObjects(new IdentityPermutation(7).subList(2, 7));
		assertEquals(PagingModel.SHOW_ALL, _pagingModel.getPageSizeSpec());
		assertEquals(7, _pagingModel.getEffectivePageSize());
		assertEquals(7, _pagingModel.getCurrentPageSize());
	}

	public void testStorePageSizeInPersonalConfiguration() throws Exception {
		int pageSize = 15;
		_pagingModel.changePageSizeSpec(pageSize);
		assertEquals(pageSize,
			_personalConfiguration.getValue(_configKey.get() + PagingModel.PAGE_SIZE_PROPERTY_SUFFIX));
	}

	public void testLoadPageSizeFromPersonalConfiguration() throws Exception {
		int pageSize = 10;
		_personalConfiguration.setValue(_configKey.get() + PagingModel.PAGE_SIZE_PROPERTY_SUFFIX, pageSize);
		_pagingModel.loadPageSize();
		assertEquals(pageSize, _pagingModel.getPageSizeSpec());
	}

	public void testUseDefaultPageSizeForInvalidPageSize() throws Exception {
		int invalidPageSize = 42;
		_pagingModel.setPageSizeSpec(invalidPageSize);
		assertEquals(getDefaultPageSize(), _pagingModel.getPageSizeSpec());
	}

	private int getDefaultPageSize() {
		return _pageSizeOptions[0];
	}

	public void testUseDefaultPageSizeForInvalidPersonalConfigurationPageSize() throws Exception {
		int invalidPageSize = 42;
		_personalConfiguration.setValue(_configKey.get() + PagingModel.PAGE_SIZE_PROPERTY_SUFFIX, invalidPageSize);
		_pagingModel.loadPageSize();
		assertEquals(getDefaultPageSize(), _pagingModel.getPageSizeSpec());
	}

	private void clearEvents() {
		_eventTester.seenEvents.clear();
	}

	private void assertEvent(EventType<?, ?, ?> event) {
		assertTrue(_eventTester.seenEvents.contains(event));
	}

	public static Test suite() {
		return suite(TestPagingModel.class);
	}

}
