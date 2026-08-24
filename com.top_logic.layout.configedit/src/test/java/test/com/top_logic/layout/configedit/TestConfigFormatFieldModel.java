/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.configedit.ConfigFormatFieldModel;

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

		/** Property name for {@link #getCounted()}. */
		String COUNTED = "counted";

		/** Property name for {@link #getTags()}. */
		String TAGS = "tags";

		/** Property name for {@link #getMandatoryTags()}. */
		String MANDATORY_TAGS = "mandatoryTags";

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

		/**
		 * A property whose {@link CountingFormat value provider} counts every parse, so a test
		 * can observe how often text was actually parsed, as opposed to how often the
		 * configuration happened to notify a change.
		 */
		@Name(COUNTED)
		@Format(CountingFormat.class)
		String getCounted();

		/** @see #getCounted() */
		void setCounted(String value);

		/**
		 * A {@link List} property with its own {@link CommaSeparatedStrings value provider}: not
		 * nullable (see {@link com.top_logic.basic.config.ListConfigValueProvider#isLegalValue}),
		 * but may legitimately be empty - the exception
		 * {@link ConfigFieldModel#isTechnicallyMandatory(PropertyDescriptor)} makes for a
		 * {@link List}/{@link java.util.Map}/{@link java.util.Set} type, even though this
		 * property's {@link PropertyDescriptor#kind()} is {@code PLAIN} (not {@code LIST}) because
		 * of the explicit {@link Format @Format}.
		 */
		@Name(TAGS)
		@Format(CommaSeparatedStrings.class)
		List<String> getTags();

		/** @see #getTags() */
		void setTags(List<String> value);

		/**
		 * Same shape as {@link #getTags()}, but explicitly {@link Mandatory}: still not
		 * technically mandatory by the {@link List} type exception, so clearing it must behave
		 * the same as clearing {@link #getTags()}.
		 */
		@Name(MANDATORY_TAGS)
		@Mandatory
		@Format(CommaSeparatedStrings.class)
		List<String> getMandatoryTags();

		/** @see #getMandatoryTags() */
		void setMandatoryTags(List<String> value);
	}

	/**
	 * {@link ConfigurationValueProvider} that counts every
	 * {@link #getValueNonEmpty(String, CharSequence)} call.
	 *
	 * <p>
	 * Used to tell apart a redundant write that {@link ConfigFormatFieldModel#setValue(Object)}'s
	 * own guard short-circuits before parsing from one that reaches the value provider a second
	 * time - a distinction the configuration's own change notification cannot make, since it
	 * suppresses a redundant {@link ConfigurationChange} for an unchanged typed value regardless
	 * of whether that value was freshly reparsed or not.
	 * </p>
	 */
	public static final class CountingFormat extends AbstractConfigurationValueProvider<String> {

		/** Singleton {@link CountingFormat} instance. */
		public static final CountingFormat INSTANCE = new CountingFormat();

		private int _parseCount;

		private CountingFormat() {
			super(String.class);
		}

		/**
		 * How often {@link #getValueNonEmpty(String, CharSequence)} has been called so far.
		 */
		public int getParseCount() {
			return _parseCount;
		}

		@Override
		protected String getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			_parseCount++;
			return propertyValue.toString();
		}

		@Override
		protected String getSpecificationNonNull(String configValue) {
			return configValue;
		}
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
	 * A rejected input leaves the field in error; re-entering the text that already matches the
	 * configuration's current value must clear that error, not leave it stale next to a now-valid
	 * display. The redundant-write guard (displayed text already matches the current value) must
	 * not return before the error is cleared.
	 */
	public void testClearsErrorAfterReenteringCurrentValue() {
		_config.setDuration(90000);
		ConfigFormatFieldModel model = model(TestConfig.DURATION);

		model.setValue("not a duration");
		assertNotNull("Precondition: the unparsable text must have been rejected.", model.getInputError());

		model.setValue("1min 30s");

		assertNull("Re-entering the text the configuration already holds must clear the previous "
			+ "error.", model.getInputError());
		assertEquals("The config value must be unaffected by the redundant write.", 90000L,
			_config.getDuration());
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
	 * Clearing a primitive {@code long} property (via the empty text a control hands back) must
	 * be refused as a field error, leaving the configuration untouched - the same rule
	 * {@link ConfigFieldModel#setValue(Object)} applies, reached here because the property's
	 * value provider ({@link MillisFormat}) answers {@code null} for empty text, just like it
	 * did before {@link ConfigFormatFieldModel#setValue(Object)} started asking the provider at
	 * all (see {@link #testEmptyInputNotRefusedForListProperty()} for the provider that answers
	 * something else).
	 */
	public void testEmptyInputRefusedForPrimitiveProperty() {
		_config.setDuration(90000);
		ConfigFormatFieldModel model = model(TestConfig.DURATION);

		model.setValue("");

		assertEquals("Clearing a primitive property must not change its value.", 90000L, _config.getDuration());
		assertNotNull("Clearing a primitive property must be reported as a field error.", model.getInputError());
	}

	/**
	 * Clearing a {@link String} property still clears it without an error: the empty string is a
	 * legitimate empty input, the classic exception {@code AbstractEditor} also makes.
	 */
	public void testEmptyInputStillClearsStringProperty() {
		_config.setCounted("x");
		ConfigFormatFieldModel model = model(TestConfig.COUNTED);

		model.setValue("");

		assertNull("Clearing a String property must still clear it.", _config.getCounted());
		assertNull("Clearing a String property is not a field error.", model.getInputError());
	}

	/**
	 * Clearing a {@link List}-typed property whose {@link CommaSeparatedStrings value provider}
	 * has no {@code @Format} exemption of its own must not be refused: the {@link List} type
	 * exception {@link ConfigFieldModel#isTechnicallyMandatory(PropertyDescriptor)} makes applies
	 * here even though the property's {@link PropertyDescriptor#kind()} is {@code PLAIN}, not
	 * {@code LIST} (see that method's JavaDoc).
	 */
	public void testEmptyInputNotRefusedForListProperty() {
		_config.setTags(Arrays.asList("a", "b"));
		ConfigFormatFieldModel model = model(TestConfig.TAGS);

		model.setValue("");

		assertNull("Clearing a List property must not be reported as a field error.", model.getInputError());
	}

	/**
	 * What clearing a {@link List}-typed property actually stores: the value provider's own empty
	 * value ({@link com.top_logic.basic.config.ListConfigValueProvider#getValueEmpty} answers
	 * {@link com.top_logic.basic.config.ListConfigValueProvider#defaultValue()}, an empty list),
	 * not {@code null} - {@code null} would misrepresent "no entries" as "no value at all", which
	 * a property that cannot actually be {@code null} does not have. Routing the empty text
	 * through the value provider (instead of hard-coding {@code null}, as this class used to)
	 * is what makes this the value actually stored - see
	 * {@link ConfigFormatFieldModel#setValue(Object)}.
	 */
	public void testEmptyInputStoresEmptyListForListProperty() {
		_config.setTags(Arrays.asList("a", "b"));
		ConfigFormatFieldModel model = model(TestConfig.TAGS);

		model.setValue("");

		assertEquals("Clearing a List property must store the empty list, not null.",
			Collections.emptyList(), _config.getTags());
	}

	/**
	 * An explicit {@link Mandatory} annotation on a {@link List}-typed property does not change
	 * the answer: the {@link List} type exception is unconditional, independent of
	 * {@link PropertyDescriptor#isMandatory()}.
	 */
	public void testEmptyInputNotRefusedForMandatoryListProperty() {
		_config.setMandatoryTags(Arrays.asList("a", "b"));
		ConfigFormatFieldModel model = model(TestConfig.MANDATORY_TAGS);

		model.setValue("");

		assertNull("Clearing a @Mandatory List property must not be reported as a field error.",
			model.getInputError());
		assertEquals("Clearing a @Mandatory List property must store the empty list, not null.",
			Collections.emptyList(), _config.getMandatoryTags());
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
	 * Writing the same text twice must reach the value provider only once.
	 *
	 * <p>
	 * Counting the value provider's parses (rather than the configuration's change
	 * notifications) is what actually tells the model's own early-return guard apart from the
	 * configuration's unrelated deduplication of an unchanged typed value: the latter would mute
	 * a second {@link ConfigurationChange} even if {@link ConfigFormatFieldModel#setValue(Object)}
	 * reparsed the text every time.
	 * </p>
	 */
	public void testSettingSameTextTwiceWritesOnce() {
		ConfigFormatFieldModel model = model(TestConfig.COUNTED);
		int before = CountingFormat.INSTANCE.getParseCount();

		model.setValue("a");
		model.setValue("a");

		assertEquals("The unchanged second write must not reach the value provider again.", before + 1,
			CountingFormat.INSTANCE.getParseCount());
		assertEquals("The value must still reach the configuration once.", "a", _config.getCounted());
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
