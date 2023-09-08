/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static com.top_logic.basic.config.TypedConfiguration.*;
import static java.util.Collections.*;

import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;

/**
 * {@link TestCase} for {@link ConfigurationItem#reset(PropertyDescriptor)}
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestReset extends AbstractTypedConfigurationTestCase {

	private static final class AssertResetListener implements ConfigurationListener {

		private Object _oldValue;

		private boolean _hasBeenNotified = false;

		AssertResetListener(Object oldValue) {
			_oldValue = oldValue;
		}

		@Override
		public void onChange(ConfigurationChange change) {
			assertEquals(_oldValue, change.getOldValue());
			Object defaultValue = change.getProperty().getDefaultValue();
			assertEquals(defaultValue, change.getNewValue());

			_hasBeenNotified = true;
			_oldValue = change.getNewValue();
		}

		public boolean hasBeenNotified() {
			return _hasBeenNotified;
		}

	}

	public interface ScenarioTypeReset extends ConfigurationItem {

		String SIMPLE = "simple";

		String DEFAULT = "default";

		String MANDATORY = "mandatory";

		@Name(SIMPLE)
		int getSimple();

		void setSimple(int value);

		@IntDefault(1)
		@Name(DEFAULT)
		int getDefault();

		void setDefault(int value);

		@Mandatory
		@Name(MANDATORY)
		int getMandatory();

		void setMandatory(int value);

	}

	private final String SIMPLE_PROPERTY = ScenarioTypeReset.SIMPLE;

	private final String DEFAULT_PROPERTY = ScenarioTypeReset.DEFAULT;

	private final String MANDATORY_PROPERTY = ScenarioTypeReset.MANDATORY;

	private ScenarioTypeReset _config;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_config = create(ScenarioTypeReset.class);
	}

	@Override
	protected void tearDown() throws Exception {
		_config = null;
		super.tearDown();
	}

	public void testSimpleProperty() {
		_config.setSimple(2);
		assert _config.valueSet(getProperty(SIMPLE_PROPERTY));
		assertResetSucceeds(SIMPLE_PROPERTY);
	}

	public void testDefaultProperty() {
		_config.setDefault(2);
		assert _config.valueSet(getProperty(DEFAULT_PROPERTY));
		assertResetSucceeds(DEFAULT_PROPERTY);
	}

	public void testUnsetMandatoryProperty() {
		// Even a mandatory property has its intrinsic default value.
		assertEquals(0, _config.getMandatory());
	}

	public void testMandatoryProperty() {
		_config.setMandatory(2);
		assert _config.valueSet(getProperty(MANDATORY_PROPERTY));
		assertResetSucceeds(MANDATORY_PROPERTY);
	}

	private void assertResetSucceeds(String propertyName) {
		PropertyDescriptor property = getProperty(propertyName);
		final Object oldValue = _config.value(property);
		AssertResetListener listener = new AssertResetListener(oldValue);
		_config.addConfigurationListener(property, listener);
		_config.reset(property);
		assertFalse(_config.valueSet(property));
		assertEquals(property.getDefaultValue(), _config.value(property));
		assertTrue(listener.hasBeenNotified());
		// Call twice to assert it won't fail after the property has already been reset.
		_config.reset(property);
		// Check that valueSet has not been toggled to 'true'.
		assertFalse(_config.valueSet(property));
	}

	private static ConfigurationDescriptor getDescriptor() {
		return getConfigurationDescriptor(ScenarioTypeReset.class);
	}

	private static PropertyDescriptor getProperty(String propertyName) {
		return getDescriptor().getProperty(propertyName);
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return emptyMap();
	}

}
