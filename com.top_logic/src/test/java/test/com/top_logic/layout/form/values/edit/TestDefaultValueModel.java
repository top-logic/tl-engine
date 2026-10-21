/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.values.edit;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.values.edit.DefaultValueModel;
import com.top_logic.util.error.TopLogicException;

/**
 * Test case for {@link DefaultValueModel#addValue(Object)} on a keyed list.
 */
@SuppressWarnings("javadoc")
public class TestDefaultValueModel extends TestCase {

	public interface Entry extends ConfigurationItem {

		String ID = "id";

		@Name(ID)
		String getId();

		void setId(String value);
	}

	public interface Container extends ConfigurationItem {

		String ENTRIES = "entries";

		@Name(ENTRIES)
		@Key(Entry.ID)
		List<Entry> getEntries();
	}

	private Container _container;

	private DefaultValueModel _model;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_container = TypedConfiguration.newConfigItem(Container.class);
		_model = new DefaultValueModel(_container, _container.descriptor().getProperty(Container.ENTRIES));
	}

	private static Entry entry(String id) {
		Entry result = TypedConfiguration.newConfigItem(Entry.class);
		if (id != null) {
			result.setId(id);
		}
		return result;
	}

	/**
	 * Entries without key are anonymous and never violate the uniqueness of keys.
	 */
	public void testAddAnonymousEntries() {
		Entry first = entry(null);
		Entry second = entry(null);
		Entry empty = entry("");
		Entry keyed = entry("keyed");
		Entry afterKeyed = entry(null);

		assertTrue(_model.addValue(first));
		assertTrue(_model.addValue(second));
		assertTrue(_model.addValue(empty));
		assertTrue(_model.addValue(keyed));
		assertTrue(_model.addValue(afterKeyed));

		assertEquals(List.of(first, second, empty, keyed, afterKeyed), _container.getEntries());
	}

	/**
	 * A key that is already used is a violation, also among anonymous entries.
	 */
	public void testAddDuplicateKey() {
		assertTrue(_model.addValue(entry(null)));
		assertTrue(_model.addValue(entry("keyed")));
		assertTrue(_model.addValue(entry(null)));

		try {
			_model.addValue(entry("keyed"));
			fail("A duplicate key must be refused.");
		} catch (TopLogicException ex) {
			// Expected.
		}
		assertEquals(3, _container.getEntries().size());
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(TestDefaultValueModel.class);
	}

}
