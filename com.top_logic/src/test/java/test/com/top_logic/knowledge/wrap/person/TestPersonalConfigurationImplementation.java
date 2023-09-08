/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap.person;

import java.util.Arrays;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;

/**
 * Test for {@link PersonalConfiguration} implementations.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings({ "javadoc" })
public abstract class TestPersonalConfigurationImplementation extends BasicTestCase {

	protected PersonalConfiguration _config;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_config = newEmptyPersonConfiguration();
	}

	@Override
	protected void tearDown() throws Exception {
		_config = null;
		super.tearDown();
	}

	/**
	 * A new empty {@link PersonalConfiguration}.
	 */
	protected abstract PersonalConfiguration newEmptyPersonConfiguration();

	public void testGetSetBooleanValue() {
		String key = "key";
		assertNull("No value stored for " + key, _config.getBoolean(key));

		_config.setBoolean(key, Boolean.TRUE);
		assertSame(Boolean.TRUE, _config.getBoolean(key));
		_config.setBoolean(key, Boolean.FALSE);
		assertSame(Boolean.FALSE, _config.getBoolean(key));
		_config.setBoolean(key, null);
		assertSame(null, _config.getBoolean(key));

		_config.setBooleanValue(key, true);
		assertSame(true, _config.getBooleanValue(key));
		_config.setBooleanValue(key, false);
		assertSame(false, _config.getBoolean(key));

		_config.setBoolean(key, Boolean.TRUE);
		assertSame(true, _config.getBooleanValue(key));
		_config.setBoolean(key, Boolean.FALSE);
		assertSame(false, _config.getBooleanValue(key));
		_config.setBoolean(key, null);
		assertSame(false, _config.getBooleanValue(key));

		_config.setBooleanValue(key, true);
		assertSame(Boolean.TRUE, _config.getBoolean(key));
		_config.setBooleanValue(key, false);
		assertSame(Boolean.FALSE, _config.getBoolean(key));
	}

	public void testGetSetJSON() {
		String key = "key";
		assertNull("No value stored for " + key, _config.getJSONValue(key));

		Object json = Arrays.asList("a", 5, true);
		_config.setJSONValue(key, json);
		assertEquals(json, _config.getJSONValue(key));

		json = new MapBuilder<>().put("a", "bar").put("b", null).put("c", Boolean.FALSE).put("d", 15).toMap();
		_config.setJSONValue(key, json);
		assertEquals(json, _config.getJSONValue(key));

		json = null;
		_config.setJSONValue(key, json);
		assertEquals(json, _config.getJSONValue(key));
		assertNull("Value was removed by setting null. Unexpected: " + _config.getValue(key), _config.getValue(key));
	}

	public void testIllegalValueFound() {
		String key = "key";
		assertNull("No value stored for " + key, _config.getValue(key));
		_config.setValue(key, "stringValue");
		try {
			assertFalse("Not a boolean but a string value contained for given key.", _config.getBooleanValue(key));
		} catch (ClassCastException ex) {
			// expected
		}
	}

}

