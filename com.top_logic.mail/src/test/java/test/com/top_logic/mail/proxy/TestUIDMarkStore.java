/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mail.proxy;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.mail.proxy.UIDMark;
import com.top_logic.mail.proxy.UIDMarkStore;

/**
 * Tests storing a {@link UIDMark} in the {@link DBProperties} with a {@link UIDMarkStore}.
 */
@SuppressWarnings("javadoc")
public class TestUIDMarkStore extends BasicTestCase {

	private DBProperties _properties;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_properties = new DBProperties(KBUtils.getConnectionPool(PersistencyLayer.getKnowledgeBase()));
	}

	public void testRoundTrip() {
		UIDMarkStore store = UIDMarkStore.forName(_properties, "testRoundTrip");
		assertEquals(UIDMarkStore.KEY_PREFIX + "testRoundTrip", store.getKey());
		assertNull(store.load());

		try {
			UIDMark mark = new UIDMark(4711, 42);
			store.store(mark);
			assertEquals(mark, store.load());
			assertEquals(mark.format(), _properties.getProperty(DBProperties.GLOBAL_PROPERTY, store.getKey()));

			UIDMark advanced = new UIDMark(4711, 50);
			store.store(advanced);
			assertEquals(advanced, store.load());
		} finally {
			store.store(null);
		}
		assertNull(store.load());
	}

	public void testMarksAreSeparatedByName() {
		UIDMarkStore store1 = UIDMarkStore.forName(_properties, "daemon1");
		UIDMarkStore store2 = UIDMarkStore.forName(_properties, "daemon2");
		try {
			store1.store(new UIDMark(1, 10));
			store2.store(new UIDMark(2, 20));

			assertEquals(new UIDMark(1, 10), store1.load());
			assertEquals(new UIDMark(2, 20), store2.load());
		} finally {
			store1.store(null);
			store2.store(null);
		}
	}

	public void testMalformedValueIsIgnored() {
		UIDMarkStore store = UIDMarkStore.forName(_properties, "testMalformedValueIsIgnored");
		try {
			_properties.setProperty(DBProperties.GLOBAL_PROPERTY, store.getKey(), "garbage");

			assertNull(store.load());
		} finally {
			store.store(null);
		}
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(ServiceTestSetup.createSetup(TestUIDMarkStore.class,
			PersistencyLayer.Module.INSTANCE));
	}

}
