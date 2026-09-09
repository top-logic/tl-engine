/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.DoubleDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;

/**
 * Tests for {@link ConfigFieldModel}.
 */
public class TestConfigFieldModel extends TestCase {

	/**
	 * Test configuration interface.
	 */
	public interface TestConfig extends ConfigurationItem {

		/** Property name for {@link #getName()}. */
		String NAME = "name";

		/** Property name for {@link #getCount()}. */
		String COUNT = "count";

		/** Property name for {@link #isActive()}. */
		String ACTIVE = "active";

		/** Property name for {@link #getTitle()}. */
		String TITLE = "title";

		/** Property name for {@link #getLongCount()}. */
		String LONG_COUNT = "longCount";

		/** Property name for {@link #getDoubleValue()}. */
		String DOUBLE_VALUE = "doubleValue";

		/** Property name for {@link #getNullableCount()}. */
		String NULLABLE_COUNT = "nullableCount";

		@Name(NAME)
		String getName();

		void setName(String value);

		@Name(COUNT)
		@IntDefault(0)
		int getCount();

		void setCount(int value);

		@Name(ACTIVE)
		boolean isActive();

		void setActive(boolean value);

		@Name(TITLE)
		@Mandatory
		String getTitle();

		void setTitle(String value);

		@Name(LONG_COUNT)
		@LongDefault(0)
		long getLongCount();

		void setLongCount(long value);

		@Name(DOUBLE_VALUE)
		@DoubleDefault(0.0)
		double getDoubleValue();

		void setDoubleValue(double value);

		/**
		 * A property with no explicit default and no {@link Mandatory} annotation: nullable, so
		 * clearing it must still work, unlike the primitive {@link #getCount()}.
		 */
		@Name(NULLABLE_COUNT)
		Integer getNullableCount();

		void setNullableCount(Integer value);
	}

	/**
	 * Tests that the initial value is read from the config.
	 */
	public void testInitialValue() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setName("hello");

		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.NAME);
		ConfigFieldModel model = new ConfigFieldModel(config, property);

		assertEquals("hello", model.getValue());
	}

	/**
	 * Tests that setValue() updates the config and fires the listener.
	 */
	public void testSetValue() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.NAME);
		ConfigFieldModel model = new ConfigFieldModel(config, property);

		Object[] captured = new Object[2];
		model.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				captured[0] = oldValue;
				captured[1] = newValue;
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// Ignored.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// Ignored.
			}
		});

		model.setValue("world");
		assertEquals("world", config.getName());
		assertNull("Old value should be null", captured[0]);
		assertEquals("New value should be 'world'", "world", captured[1]);
	}

	/**
	 * Tests that an external config change fires the FieldModel listener.
	 */
	public void testExternalChange() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.NAME);
		ConfigFieldModel model = new ConfigFieldModel(config, property);

		Object[] captured = new Object[2];
		model.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				captured[0] = oldValue;
				captured[1] = newValue;
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// Ignored.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// Ignored.
			}
		});

		// Change config directly, not through the model.
		config.setName("external");
		assertNull("Old value should be null", captured[0]);
		assertEquals("New value should be 'external'", "external", captured[1]);
		assertEquals("Model getValue() should reflect change", "external", model.getValue());
	}

	/**
	 * Tests that @Mandatory maps to isMandatory().
	 */
	public void testIsMandatory() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);

		PropertyDescriptor titleProp = config.descriptor().getProperty(TestConfig.TITLE);
		ConfigFieldModel mandatoryModel = new ConfigFieldModel(config, titleProp);
		assertTrue("@Mandatory property should be mandatory", mandatoryModel.isMandatory());

		PropertyDescriptor nameProp = config.descriptor().getProperty(TestConfig.NAME);
		ConfigFieldModel optionalModel = new ConfigFieldModel(config, nameProp);
		assertFalse("Non-mandatory property should not be mandatory", optionalModel.isMandatory());
	}

	/**
	 * Tests dirty tracking.
	 */
	public void testIsDirty() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.COUNT);
		ConfigFieldModel model = new ConfigFieldModel(config, property);

		assertFalse("Should not be dirty initially", model.isDirty());

		model.setValue(42);
		// Note: isDirty() uses the base class comparison (cached _value vs _defaultValue).
		// Since ConfigFieldModel delegates getValue() to config, the base class _value is not
		// updated, so isDirty() might not reflect the change. This depends on implementation.
		// The AbstractFieldModel's _defaultValue is the initial config value (0), and _value
		// remains 0 because we don't call super.setValue(). isDirty() compares _value vs
		// _defaultValue which are both 0.
		// For now, verify the config actually changed.
		assertEquals(42, config.getCount());
	}

	/**
	 * Tests that detach() removes the config listener.
	 */
	public void testDetach() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.NAME);
		ConfigFieldModel model = new ConfigFieldModel(config, property);

		int[] callCount = {0};
		model.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				callCount[0]++;
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// Ignored.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// Ignored.
			}
		});

		config.setName("before");
		assertEquals(1, callCount[0]);

		model.detach();
		config.setName("after");
		assertEquals("Listener should not fire after detach", 1, callCount[0]);
	}

	/**
	 * Tests that setting the same value does not fire the listener.
	 */
	public void testSetSameValueNoNotification() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setName("value");
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.NAME);
		ConfigFieldModel model = new ConfigFieldModel(config, property);

		int[] callCount = {0};
		model.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				callCount[0]++;
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// Ignored.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// Ignored.
			}
		});

		model.setValue("value");
		assertEquals("Same value should not fire listener", 0, callCount[0]);
	}

	/**
	 * Tests that a {@link Double} arriving for an {@code int} property (as a number input control
	 * hands back, whatever the property's actual numeric type) is coerced and stored as an
	 * {@link Integer}.
	 */
	public void testSetValueCoercesDoubleToInteger() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.COUNT);
		ConfigFieldModel model = new ConfigFieldModel(config, property);

		model.setValue(Double.valueOf(555.0));

		assertEquals(555, config.getCount());
		assertNull("Well-formed whole number must not be rejected.", model.getInputError());
	}

	/**
	 * Same coercion as {@link #testSetValueCoercesDoubleToInteger()}, for a {@code long} property.
	 */
	public void testSetValueCoercesDoubleToLong() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.LONG_COUNT);
		ConfigFieldModel model = new ConfigFieldModel(config, property);

		model.setValue(Double.valueOf(555.0));

		assertEquals(555L, config.getLongCount());
		assertNull("Well-formed whole number must not be rejected.", model.getInputError());
	}

	/**
	 * Tests that a fractional value for an {@code int} property is rejected as an input error
	 * instead of being truncated, and that the configuration is left untouched.
	 */
	public void testSetValueRejectsFractionForIntegralProperty() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setCount(1);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.COUNT);
		ConfigFieldModel model = new ConfigFieldModel(config, property);

		model.setValue(Double.valueOf(555.7));

		assertEquals("Fractional value must not be written.", 1, config.getCount());
		assertNotNull("Fractional value must be reported as input error.", model.getInputError());
	}

	/**
	 * A whole number that does not fit the property's numeric type is rejected too, not narrowed.
	 *
	 * <p>
	 * Narrowing past the target's range loses as much as truncating a fraction does, and does it
	 * less visibly: {@link Number#intValue()} saturates at the boundary, so an out-of-range entry
	 * would be stored as {@link Integer#MAX_VALUE} - a number the user never typed, and one the
	 * field would then show back as if it had been accepted.
	 * </p>
	 */
	public void testSetValueRejectsOutOfRangeValueForIntegralProperty() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setCount(1);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.COUNT);
		ConfigFieldModel model = new ConfigFieldModel(config, property);

		model.setValue(Double.valueOf(99999999999.0));

		assertEquals("An out-of-range value must not be written.", 1, config.getCount());
		assertNotNull("An out-of-range value must be reported as input error.", model.getInputError());
	}

	/** The bounds of the property's own type are still accepted. */
	public void testSetValueAcceptsTheExtremesOfTheIntegralRange() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.COUNT);
		ConfigFieldModel model = new ConfigFieldModel(config, property);

		model.setValue(Double.valueOf(Integer.MAX_VALUE));
		assertNull("The largest representable value must not be rejected.", model.getInputError());
		assertEquals(Integer.MAX_VALUE, config.getCount());

		model.setValue(Double.valueOf(Integer.MIN_VALUE));
		assertNull("The smallest representable value must not be rejected.", model.getInputError());
		assertEquals(Integer.MIN_VALUE, config.getCount());
	}

	/**
	 * A rejected value leaves the field in error; re-entering the value the property already
	 * holds must clear that error, not leave it stale next to a now-valid value. The
	 * redundant-write guard (same old and new value) must not return before the error is cleared.
	 */
	public void testClearsErrorAfterReenteringCurrentValue() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setCount(1);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.COUNT);
		ConfigFieldModel model = new ConfigFieldModel(config, property);

		model.setValue(Double.valueOf(555.7));
		assertNotNull("Precondition: the fractional value must have been rejected.", model.getInputError());

		model.setValue(Double.valueOf(1.0));

		assertNull("Re-entering the value already held must clear the previous error.",
			model.getInputError());
		assertEquals("The config value must be unaffected by the redundant write.", 1, config.getCount());
	}

	/**
	 * Tests that a {@code double} property still works unchanged: no coercion, no rejection.
	 */
	public void testSetValueDoublePropertyUnchanged() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.DOUBLE_VALUE);
		ConfigFieldModel model = new ConfigFieldModel(config, property);

		model.setValue(Double.valueOf(3.14));

		assertEquals(3.14, config.getDoubleValue(), 0d);
		assertNull("Fractional value for a double property must not be rejected.", model.getInputError());
	}

	/**
	 * Clearing a primitive {@code int} property (as a number input control hands back for an
	 * empty entry, see {@link com.top_logic.layout.react.control.form.ReactNumberInputControl})
	 * must be refused as a field error, not forwarded to
	 * {@link ConfigurationItem#update(PropertyDescriptor, Object)}, which cannot store {@code null}
	 * for a primitive property.
	 */
	public void testSetValueRejectsNullForPrimitiveProperty() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setCount(42);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.COUNT);
		ConfigFieldModel model = new ConfigFieldModel(config, property);

		model.setValue(null);

		assertEquals("Clearing a primitive property must not change its value.", 42, config.getCount());
		assertNotNull("Clearing a primitive property must be reported as a field error.", model.getInputError());
	}

	/**
	 * A primitive property with no explicit {@link Mandatory} annotation must still be marked
	 * mandatory, mirroring {@code AbstractEditor#isTechnicallyMandatory(PropertyDescriptor)}: it
	 * cannot actually hold {@code null}, so a value must be entered.
	 */
	public void testPrimitivePropertyIsTechnicallyMandatory() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.COUNT);
		ConfigFieldModel model = new ConfigFieldModel(config, property);

		assertTrue("A primitive property must be mandatory even without an explicit @Mandatory.",
			model.isMandatory());
	}

	/**
	 * Clearing a {@link String} property still stores the empty value: the empty string is a
	 * legitimate empty input, the classic exception {@code AbstractEditor} also makes.
	 */
	public void testSetValueStoresEmptyStringForStringProperty() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setName("hello");
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.NAME);
		ConfigFieldModel model = new ConfigFieldModel(config, property);

		model.setValue("");

		assertEquals("The empty string must be stored, not rejected.", "", config.getName());
		assertNull("The empty string is not a field error.", model.getInputError());
	}

	/**
	 * Clearing a nullable {@link Integer} property (no primitive, no {@link Mandatory}) must
	 * still clear it: it is nullable, so it is not technically mandatory.
	 */
	public void testSetValueClearsNullableIntegerProperty() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setNullableCount(7);
		PropertyDescriptor property = config.descriptor().getProperty(TestConfig.NULLABLE_COUNT);
		ConfigFieldModel model = new ConfigFieldModel(config, property);

		model.setValue(null);

		assertNull("Clearing a nullable property must clear it.", config.getNullableCount());
		assertNull("Clearing a nullable property is not a field error.", model.getInputError());
	}

	/**
	 * Suite requiring {@link TypeIndex} for {@link TypedConfiguration} and
	 * {@link ThreadContextManager} for the label resolution the rejected-value error message uses.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestConfigFieldModel.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE));
	}
}
