/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.time.TimeOfDayAsDateValueProvider;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.configedit.ConfigControlService;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.configedit.ConfigFormatFieldModel;
import com.top_logic.layout.configedit.ConfigPropertyOptions;
import com.top_logic.layout.configedit.ConfigSelectFieldModel;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.control.form.ReactPasswordInputControl;
import com.top_logic.layout.react.control.form.ReactSelectFormFieldControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;

/**
 * Tests for {@link ConfigControlService}.
 */
public class TestConfigControlService extends TestCase {

	/** Options function offering three fixed colours. */
	public static class Colors extends Function0<List<String>> {
		@Override
		public List<String> apply() {
			return Arrays.asList("red", "green", "blue");
		}
	}

	/** Options function offering two placeholder tags, unrelated to the annotated property's own value type. */
	public static class Tags extends Function0<List<String>> {
		@Override
		public List<String> apply() {
			return Arrays.asList("a", "b");
		}
	}

	/**
	 * A {@code boolean} format ("yes"/"no" text) - used to verify that an explicit {@code @Format}
	 * vetoes the checkbox widget for a {@code boolean} property just like it vetoes the date picker
	 * for a {@code Date} property.
	 */
	public static class YesNoFormat extends AbstractConfigurationValueProvider<Boolean> {

		/** Creates a {@link YesNoFormat}. */
		public YesNoFormat() {
			super(Boolean.class);
		}

		@Override
		protected Boolean getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			return "yes".equalsIgnoreCase(propertyValue.toString());
		}

		@Override
		protected String getSpecificationNonNull(Boolean configValue) {
			return configValue ? "yes" : "no";
		}
	}

	/**
	 * Marker sub-item type, used only to give an ITEM-kind property that (atypically) also carries
	 * an {@link Options} annotation.
	 */
	public interface Nested extends ConfigurationItem {
		// No properties needed.
	}

	/** Configuration covering every value type the fallback distinguishes. */
	public interface TestConfig extends ConfigurationItem {

		/** Property name for {@link #getText()}. */
		String TEXT = "text";

		/** Property name for {@link #getCount()}. */
		String COUNT = "count";

		/** Property name for {@link #isFlag()}. */
		String FLAG = "flag";

		/** Property name for {@link #getRatio()}. */
		String RATIO = "ratio";

		/** Property name for {@link #getMode()}. */
		String MODE = "mode";

		/** Property name for {@link #getStart()}. */
		String START = "start";

		/** Property name for {@link #getTimeOfDay()}. */
		String TIME_OF_DAY = "timeOfDay";

		/** Property name for {@link #getYesNo()}. */
		String YES_NO = "yesNo";

		/** Property name for {@link #isSecretFlag()}. */
		String SECRET_FLAG = "secretFlag";

		/** Property name for {@link #getColor()}. */
		String COLOR = "color";

		/** Property name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Property name for {@link #getSecret()}. */
		String SECRET = "secret";

		/** Property name for {@link #getImplType()}. */
		String IMPL_TYPE = "implType";

		/** Property name for {@link #getNested()}. */
		String NESTED = "nested";

		/** A mode to choose from. */
		enum Mode {
			/** The one mode. */
			ON,
			/** The other mode. */
			OFF;
		}

		/** Free text. */
		@Name(TEXT)
		String getText();

		/** A whole number. */
		@Name(COUNT)
		@IntDefault(0)
		int getCount();

		/** A flag. */
		@Name(FLAG)
		boolean isFlag();

		/** A fraction. */
		@Name(RATIO)
		double getRatio();

		/** One of the modes. */
		@Name(MODE)
		Mode getMode();

		/** A point in time. */
		@Name(START)
		Date getStart();

		/**
		 * A time of day - carries no date, only an hour and a minute. Its own {@code @Format}
		 * must veto the date picker, exactly like the classic declarative form's
		 * {@code ValueEditor} vetoes its {@code calendar} field for the same reason.
		 */
		@Name(TIME_OF_DAY)
		@Format(TimeOfDayAsDateValueProvider.class)
		Date getTimeOfDay();

		/**
		 * A flag with its own (unusual, but valid) {@code @Format} - used only to verify that the
		 * specialization veto applies to every directly-editable type, not just {@code Date}.
		 * Wrapper-typed ({@code Boolean}, not {@code boolean}) only because a custom format's
		 * default value is {@code null}, which a primitive property cannot hold.
		 */
		@Name(YES_NO)
		@Format(YesNoFormat.class)
		Boolean getYesNo();

		/**
		 * An encrypted flag - unusual, but exercises the {@code @Encrypted} veto on a path
		 * (the {@code boolean} branch) the old resolution order never even reached it from.
		 */
		@Name(SECRET_FLAG)
		@Encrypted
		boolean isSecretFlag();

		/** One of the offered colours. */
		@Name(COLOR)
		@Options(fun = Colors.class)
		String getColor();

		/** An internationalized label. */
		@Name(LABEL)
		ResKey getLabel();

		/** A secret that must not be shown. */
		@Name(SECRET)
		@Encrypted
		String getSecret();

		/** A plain class-valued property, naming an implementation. */
		@Name(IMPL_TYPE)
		Class<?> getImplType();

		/**
		 * An ITEM property that (atypically) carries an options annotation - it must still never be
		 * edited by selecting, no matter what {@link ConfigPropertyOptions#optionProvider} answers
		 * for it.
		 */
		@Name(NESTED)
		@Options(fun = Tags.class)
		Nested getNested();
	}

	private TestConfig _config;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_config = TypedConfiguration.newConfigItem(TestConfig.class);
	}

	private ReactContext context() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue());
	}

	private ReactControl control(String propertyName) {
		PropertyDescriptor property = _config.descriptor().getProperty(propertyName);
		ConfigFieldModel model = ConfigControlService.getInstance().createModel(_config, property);
		return ConfigControlService.getInstance().createControl(context(), model);
	}

	private ConfigFieldModel model(String propertyName) {
		PropertyDescriptor property = _config.descriptor().getProperty(propertyName);
		return ConfigControlService.getInstance().createModel(_config, property);
	}

	/** A string property keeps the text input. */
	public void testString() {
		assertTrue(control(TestConfig.TEXT) instanceof ReactTextInputControl);
	}

	/** A boolean property keeps the checkbox. */
	public void testBoolean() {
		assertTrue(control(TestConfig.FLAG) instanceof ReactCheckboxControl);
	}

	/** An integral property keeps the number input. */
	public void testInt() {
		assertTrue(control(TestConfig.COUNT) instanceof ReactNumberInputControl);
	}

	/** A floating-point property keeps the number input. */
	public void testDouble() {
		assertTrue(control(TestConfig.RATIO) instanceof ReactNumberInputControl);
	}

	/** An enum property keeps the select. */
	public void testEnum() {
		assertTrue(control(TestConfig.MODE) instanceof ReactSelectFormFieldControl);
	}

	/** A property with an options annotation gets a select, not a text input. */
	public void testOptionsProperty() {
		assertTrue("A property with options must be edited by selecting.",
			control(TestConfig.COLOR) instanceof ReactSelectFormFieldControl);
	}

	/**
	 * A plain {@code Date} property, without a format of its own, is edited in a date picker for a
	 * bare date - the classic declarative form's {@code calendar} field, and the only case that
	 * ever reaches the date picker at all.
	 */
	public void testDate() {
		ReactControl control = control(TestConfig.START);
		assertTrue("A plain date must be edited in a date picker.", control instanceof ReactDatePickerControl);
		assertEquals("A plain date without a format means a bare date.",
			"date", control.scriptingScalarState().get("inputType"));
	}

	/**
	 * A {@code Date} property with its own {@code @Format} (here: a time of day) is specialized, so
	 * it never reaches the date picker at all - it is edited as that format's text, exactly like the
	 * classic declarative form's {@code ValueEditor}/{@code PlainEditor}. The date picker for a bare
	 * date could not show or accept a time of day anyway; going through the format sidesteps that
	 * question entirely rather than trying to guess the right picker kind.
	 */
	public void testDateWithFormatIsNotAPicker() {
		assertTrue("A formatted Date property must be parsed through its format, not edited raw.",
			model(TestConfig.TIME_OF_DAY) instanceof ConfigFormatFieldModel);
		assertTrue("A formatted Date property must not reach the date picker.",
			control(TestConfig.TIME_OF_DAY) instanceof ReactTextInputControl);
	}

	/**
	 * A {@code boolean} property with its own {@code @Format} is specialized just the same as a
	 * formatted {@code Date} - the veto is not special-cased to dates, it applies to every type the
	 * built-in widgets would otherwise handle directly.
	 */
	public void testBooleanWithFormatIsNotACheckbox() {
		assertTrue("A formatted boolean property must be parsed through its format, not edited raw.",
			model(TestConfig.YES_NO) instanceof ConfigFormatFieldModel);
		assertFalse("A formatted boolean property must not get the checkbox.",
			control(TestConfig.YES_NO) instanceof ReactCheckboxControl);
		assertTrue("A formatted boolean property is edited as its format's text.",
			control(TestConfig.YES_NO) instanceof ReactTextInputControl);
	}

	/** A typed property that is edited as text gets the format-aware model. */
	public void testTypedPropertyUsesFormatModel() {
		assertTrue("A typed property must be parsed through its format.",
			model(TestConfig.LABEL) instanceof ConfigFormatFieldModel);
	}

	/** A string property needs no format model. */
	public void testStringUsesPlainModel() {
		assertFalse("A string property needs no format.",
			model(TestConfig.TEXT) instanceof ConfigFormatFieldModel);
	}

	/** An encrypted property is not shown in the clear. */
	public void testEncrypted() {
		assertTrue("An encrypted property must be edited in a password field.",
			control(TestConfig.SECRET) instanceof ReactPasswordInputControl);
	}

	/**
	 * An encrypted {@code boolean} property is edited in a password field too - the old resolution
	 * order checked {@code @Encrypted} only after the {@code boolean} branch of the type chain had
	 * already returned a checkbox, so this path never reached the check at all.
	 */
	public void testEncryptedBooleanIsPasswordNotCheckbox() {
		ReactControl control = control(TestConfig.SECRET_FLAG);
		assertTrue("An encrypted boolean must be edited in a password field.",
			control instanceof ReactPasswordInputControl);
		assertFalse("An encrypted boolean must not fall through to the checkbox.",
			control instanceof ReactCheckboxControl);
	}

	/**
	 * A plain class-valued property is edited as text (its fully qualified name), not as a select -
	 * {@link ConfigPropertyOptions} offers no options for it.
	 */
	public void testClassValuedPropertyIsNotASelect() {
		PropertyDescriptor property = _config.descriptor().getProperty(TestConfig.IMPL_TYPE);
		assertNull("A plain class-valued property must not resolve to any options.",
			ConfigPropertyOptions.optionProvider(property));

		assertFalse("A plain class-valued property must not be edited by selecting.",
			control(TestConfig.IMPL_TYPE) instanceof ReactSelectFormFieldControl);
	}

	/**
	 * An ITEM property is never edited by selecting, even when {@link ConfigPropertyOptions}
	 * answers with an option provider for it - such a property is rendered by a nested editor or a
	 * dedicated type selector, never a plain select.
	 */
	public void testItemPropertyIsNeverSelect() {
		PropertyDescriptor property = _config.descriptor().getProperty(TestConfig.NESTED);
		assertNotNull("Precondition: the property must actually have options for this test to be meaningful.",
			ConfigPropertyOptions.optionProvider(property));

		ConfigFieldModel model = ConfigControlService.getInstance().createModel(_config, property);

		assertFalse("An ITEM property must not be edited by selecting from options.",
			model instanceof ConfigSelectFieldModel);
	}

	/**
	 * Suite starting the service under test.
	 *
	 * <p>
	 * {@link ConfigControlService} is a {@code ConfiguredManagedClass}: starting it fetches its
	 * configuration from {@link com.top_logic.basic.XMLProperties}, which needs to be bootstrapped
	 * first via {@link ModuleTestSetup}.
	 * </p>
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestConfigControlService.class,
				TypeIndex.Module.INSTANCE, ConfigControlService.Module.INSTANCE));
	}
}
