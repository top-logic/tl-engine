/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.configedit.ConfigFormatFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;

/**
 * Tests for {@link ConfigFormatFieldModel}.
 */
public class TestConfigFormatFieldModel extends TestCase {

	/** Configuration with properties that need their value provider to be read and written. */
	public interface TestConfig extends ConfigurationItem {

		/** Property name for {@link #getStart()}. */
		String START = "start";

		/** Property name for {@link #getDuration()}. */
		String DURATION = "duration";

		/**
		 * A point in time, written in the configuration's date format.
		 */
		@Name(START)
		Date getStart();

		/** @see #getStart() */
		void setStart(Date value);

		/**
		 * A duration in milliseconds, written as a human-readable amount of time.
		 */
		@Name(DURATION)
		@Format(MillisFormat.class)
		@LongDefault(0)
		long getDuration();

		/** @see #getDuration() */
		void setDuration(long value);
	}

	private TestConfig _config;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_config = TypedConfiguration.newConfigItem(TestConfig.class);
	}

	private ConfigFormatFieldModel model(String propertyName) {
		PropertyDescriptor property = _config.descriptor().getProperty(propertyName);
		return new ConfigFormatFieldModel(_config, property);
	}

	/**
	 * A typed value is handed out as the text its format produces, not as {@link Object#toString()}.
	 */
	public void testFormatsValueForDisplay() {
		_config.setDuration(90000);

		assertEquals("1min 30s", model(TestConfig.DURATION).getValue());
	}

	/**
	 * Entered text is parsed through the property's format before it reaches the configuration.
	 */
	public void testParsesEnteredText() {
		ConfigFormatFieldModel model = model(TestConfig.DURATION);

		model.setValue("2min");

		assertEquals("The parsed value must reach the configuration.", 120000L, _config.getDuration());
		assertNull("A well-formed input leaves no error.", model.getInputError());
	}

	/**
	 * Unparsable text becomes a field error and leaves the configuration untouched.
	 */
	public void testRejectsUnparsableText() {
		_config.setDuration(90000);
		ConfigFormatFieldModel model = model(TestConfig.DURATION);

		model.setValue("not a duration");

		assertEquals("The rejected input must not change the value.", 90000L, _config.getDuration());
		assertNotNull("The rejected input must be reported as an error.", model.getInputError());
	}

	/**
	 * A well-formed input after a rejected one clears the error.
	 */
	public void testClearsErrorAfterValidInput() {
		ConfigFormatFieldModel model = model(TestConfig.DURATION);
		model.setValue("not a duration");

		model.setValue("5s");

		assertNull("A well-formed input must clear the previous error.", model.getInputError());
		assertEquals(5000L, _config.getDuration());
	}

	/**
	 * Empty input clears the value instead of failing to parse.
	 */
	public void testEmptyInputClearsValue() {
		_config.setStart(new Date(0));
		ConfigFormatFieldModel model = model(TestConfig.START);

		model.setValue("");

		assertNull("Empty input must clear the value.", _config.getStart());
		assertNull("Empty input is not a parse error.", model.getInputError());
	}

	/**
	 * A freshly loaded field over a property that already holds a value must not report unsaved
	 * changes: the cached value and the default value must both live in the same (text) domain.
	 */
	public void testFreshModelIsNotDirty() {
		_config.setDuration(90000);

		ConfigFormatFieldModel model = model(TestConfig.DURATION);

		assertFalse("A freshly loaded field must not be dirty.", model.isDirty());
	}

	/**
	 * Writing the same text twice must reach the configuration only once: the second call is a
	 * no-op that neither re-parses nor notifies listeners again.
	 */
	public void testSettingSameTextTwiceWritesOnce() {
		ConfigFormatFieldModel model = model(TestConfig.DURATION);
		int[] changeCount = { 0 };
		model.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				changeCount[0]++;
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// Not relevant for this test.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// Not relevant for this test.
			}
		});

		model.setValue("2min");
		model.setValue("2min");

		assertEquals("The unchanged second write must not notify listeners again.", 1, changeCount[0]);
		assertEquals("The value must still reach the configuration once.", 120000L, _config.getDuration());
	}

	/**
	 * Suite requiring {@link TypeIndex} for {@link TypedConfiguration} and
	 * {@link ThreadContextManager} for the label resolution the error message uses.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestConfigFormatFieldModel.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE));
	}
}
