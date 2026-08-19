/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.AbstractConfigurationValueBinding;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Binding;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.time.TimeOfDayAsDateValueProvider;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.configedit.ConfigControl;
import com.top_logic.layout.configedit.ConfigControlProvider;
import com.top_logic.layout.configedit.ConfigControlService;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.configedit.ConfigFormatFieldModel;
import com.top_logic.layout.configedit.ConfigPropertyOptions;
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
	 * A {@link ConfigControlProvider} that always renders a checkbox, regardless of the property's
	 * actual type - deliberately mismatched, so getting this control back can only be explained by
	 * an explicit {@link ConfigControl} annotation, never by any built-in, type-based resolution.
	 */
	public static class FixedCheckboxProvider implements ConfigControlProvider {
		@Override
		public ReactControl createControl(ReactContext context, ConfigFieldModel model) {
			return new ReactCheckboxControl(context, model);
		}
	}

	/**
	 * A minimal {@code ConfigurationValueBinding} with no accompanying {@code @Format} - stands in
	 * for the framework's real "binding-only" bindings ({@code AbstractListBinding},
	 * {@code MapAttributeBinding}, {@code XMLFragmentString}), none of which pair with a
	 * {@code ConfigurationValueProvider}. Never actually exercised for XML I/O by these tests.
	 */
	public static class NoFormatBinding extends AbstractConfigurationValueBinding<List<String>> {
		@Override
		public void saveConfigItem(XMLStreamWriter out, List<String> item) throws XMLStreamException {
			throw new UnsupportedOperationException("Not exercised by these tests.");
		}

		@Override
		public List<String> loadConfigItem(XMLStreamReader in, List<String> baseValue)
				throws XMLStreamException, ConfigurationException {
			throw new UnsupportedOperationException("Not exercised by these tests.");
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

		/** Property name for {@link #getAnnotated()}. */
		String ANNOTATED = "annotated";

		/** Property name for {@link #getAnnotatedMode()}. */
		String ANNOTATED_MODE = "annotatedMode";

		/** Property name for {@link #getEncryptedAnnotated()}. */
		String ENCRYPTED_ANNOTATED = "encryptedAnnotated";

		/** Property name for {@link #getEncryptedMode()}. */
		String ENCRYPTED_MODE = "encryptedMode";

		/** Property name for {@link #getEncryptedColor()}. */
		String ENCRYPTED_COLOR = "encryptedColor";

		/** Property name for {@link #getBindingOnly()}. */
		String BINDING_ONLY = "bindingOnly";

		/** A mode to choose from. */
		enum Mode {
			/** The one mode. */
			ON,
			/** The other mode. */
			OFF;
		}

		/**
		 * A second enum, annotated at the type level with {@link ConfigControl} - proves the
		 * annotation is honored from the value type too, not just from the property: without it, a
		 * plain enum property would get the select, never a checkbox.
		 */
		@ConfigControl(FixedCheckboxProvider.class)
		enum AnnotatedMode {
			/** The one mode. */
			A,
			/** The other mode. */
			B;
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

		/**
		 * A {@code String} property naming its own control via {@link ConfigControl} - a plain
		 * {@code String} would otherwise get the text input, never a checkbox.
		 */
		@Name(ANNOTATED)
		@ConfigControl(FixedCheckboxProvider.class)
		String getAnnotated();

		/**
		 * An enum property whose control comes from its value type's own {@link ConfigControl}
		 * annotation, not from an annotation on the property itself.
		 */
		@Name(ANNOTATED_MODE)
		AnnotatedMode getAnnotatedMode();

		/**
		 * Both {@link Encrypted} and a {@link ConfigControl} annotation - {@code @Encrypted} must
		 * win: a module's custom control must never be able to expose a secret in the clear.
		 */
		@Name(ENCRYPTED_ANNOTATED)
		@Encrypted
		@ConfigControl(FixedCheckboxProvider.class)
		String getEncryptedAnnotated();

		/**
		 * An encrypted enum - without {@code isSelect} also checking {@code @Encrypted}, this
		 * would still resolve to a select (an enum has an intrinsic option list even without
		 * {@code @Options}), and the password control would then be handed that select-domain
		 * model.
		 */
		@Name(ENCRYPTED_MODE)
		@Encrypted
		Mode getEncryptedMode();

		/**
		 * An encrypted property with an explicit {@code @Options} annotation - the same hole as
		 * {@link #getEncryptedMode()}, but through the annotation-driven option list rather than
		 * an enum's intrinsic one.
		 */
		@Name(ENCRYPTED_COLOR)
		@Encrypted
		@Options(fun = Colors.class)
		String getEncryptedColor();

		/**
		 * A {@code COMPLEX} property with only a value binding and no value provider - this
		 * service has no way to put its value into any widget (not directly, not by selecting,
		 * not as text) and must reject it, the same as an {@code ITEM} property.
		 */
		@Name(BINDING_ONLY)
		@Binding(NoFormatBinding.class)
		List<String> getBindingOnly();
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

	/**
	 * An encrypted property is not shown in the clear, and is parsed through its format - the
	 * password control is a text control, so its model must hold text, not the raw typed value.
	 */
	public void testEncrypted() {
		assertTrue("An encrypted property must be parsed through its format.",
			model(TestConfig.SECRET) instanceof ConfigFormatFieldModel);
		assertTrue("An encrypted property must be edited in a password field.",
			control(TestConfig.SECRET) instanceof ReactPasswordInputControl);
	}

	/**
	 * An encrypted {@code boolean} property is edited in a password field too - the old resolution
	 * order checked {@code @Encrypted} only after the {@code boolean} branch of the type chain had
	 * already returned a checkbox, so this path never reached the check at all. Critically, its
	 * model must also be the format-aware {@link ConfigFormatFieldModel}: a plain
	 * {@link ConfigFieldModel} would hand the text-based password control a raw {@code Boolean},
	 * and a client edit would try to write a {@code String} into a {@code boolean} property - the
	 * exact domain mismatch this fix closes. Checking only the control's class (as an earlier
	 * version of this test did) cannot see that bug; the model must be checked too.
	 */
	public void testEncryptedBooleanIsPasswordNotCheckbox() {
		assertTrue("An encrypted boolean must be parsed through its format, not bound to the raw "
			+ "boolean value a text-based password control cannot handle.",
			model(TestConfig.SECRET_FLAG) instanceof ConfigFormatFieldModel);

		ReactControl control = control(TestConfig.SECRET_FLAG);
		assertTrue("An encrypted boolean must be edited in a password field.",
			control instanceof ReactPasswordInputControl);
		assertFalse("An encrypted boolean must not fall through to the checkbox.",
			control instanceof ReactCheckboxControl);
	}

	/**
	 * An encrypted enum must still be edited in the password field, not the select an enum
	 * otherwise gets - without {@code isSelect} also checking {@code @Encrypted}, this property
	 * would resolve to {@code ConfigSelectFieldModel}, and the password control would then be
	 * handed that select-domain model instead of text.
	 */
	public void testEncryptedEnumIsPasswordNotSelect() {
		assertTrue("An encrypted enum must be parsed through its format, not resolved to a select.",
			model(TestConfig.ENCRYPTED_MODE) instanceof ConfigFormatFieldModel);

		ReactControl control = control(TestConfig.ENCRYPTED_MODE);
		assertTrue("An encrypted enum must be edited in a password field.",
			control instanceof ReactPasswordInputControl);
		assertFalse("An encrypted enum must not fall through to the select.",
			control instanceof ReactSelectFormFieldControl);
	}

	/**
	 * The same hole as {@link #testEncryptedEnumIsPasswordNotSelect()}, but via an explicit
	 * {@code @Options} annotation rather than an enum's intrinsic option list.
	 */
	public void testEncryptedWithOptionsIsPasswordNotSelect() {
		assertTrue("An encrypted, optioned property must be parsed through its format.",
			model(TestConfig.ENCRYPTED_COLOR) instanceof ConfigFormatFieldModel);

		ReactControl control = control(TestConfig.ENCRYPTED_COLOR);
		assertTrue("An encrypted, optioned property must be edited in a password field.",
			control instanceof ReactPasswordInputControl);
		assertFalse("An encrypted, optioned property must not fall through to the select.",
			control instanceof ReactSelectFormFieldControl);
	}

	/**
	 * A plain {@code String} property naming its own control via a {@link ConfigControl}
	 * annotation on the property gets that control, not the built-in text input a plain
	 * {@code String} would otherwise get.
	 */
	public void testConfigControlAnnotationOnProperty() {
		assertTrue("The annotated control must be used instead of the built-in text input.",
			control(TestConfig.ANNOTATED) instanceof ReactCheckboxControl);
	}

	/**
	 * A {@link ConfigControl} annotation on the property's value type is honored too, not just on
	 * the property itself: without it, a plain enum property would get the select.
	 */
	public void testConfigControlAnnotationOnValueType() {
		assertTrue("The value type's annotated control must be used instead of the enum select.",
			control(TestConfig.ANNOTATED_MODE) instanceof ReactCheckboxControl);
	}

	/**
	 * {@code @Encrypted} wins over an explicit {@link ConfigControl} annotation: a module's own
	 * choice of control must never be able to expose a secret in the clear.
	 */
	public void testEncryptedWinsOverConfigControlAnnotation() {
		ReactControl control = control(TestConfig.ENCRYPTED_ANNOTATED);
		assertTrue("Encrypted must win over an explicit control annotation.",
			control instanceof ReactPasswordInputControl);
		assertFalse("The annotated control must not be reachable for an encrypted property.",
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
	 * {@link ConfigControlService#createModel(ConfigurationItem, PropertyDescriptor)} rejects an
	 * ITEM property outright - it must not silently fall through to a text field bound to a raw,
	 * complex {@link ConfigurationItem}. The property (atypically) also carries an
	 * {@link Options} annotation, to rule out the rejection being an accident of "no options
	 * resolved" rather than the kind check itself.
	 */
	public void testCreateModelRejectsItemKind() {
		PropertyDescriptor property = _config.descriptor().getProperty(TestConfig.NESTED);
		assertNotNull("Precondition: the property must actually have options for this test to be meaningful.",
			ConfigPropertyOptions.optionProvider(property));

		try {
			ConfigControlService.getInstance().createModel(_config, property);
			fail("An ITEM property must be rejected, not silently bound to a text field.");
		} catch (IllegalArgumentException expected) {
			// Expected: only PLAIN and REF properties are resolved.
		}
	}

	/**
	 * {@link ConfigControlService#createControl(ReactContext, ConfigFieldModel)} independently
	 * rejects an ITEM property too - the guarantee must not depend on every caller routing through
	 * {@link ConfigControlService#createModel(ConfigurationItem, PropertyDescriptor)} first. The
	 * model here is built directly (bypassing {@code createModel}'s own check), so this exercises
	 * {@code createControl}'s check in isolation.
	 */
	public void testCreateControlRejectsItemKind() {
		PropertyDescriptor property = _config.descriptor().getProperty(TestConfig.NESTED);
		ConfigFieldModel model = new ConfigFieldModel(_config, property);

		try {
			ConfigControlService.getInstance().createControl(context(), model);
			fail("An ITEM property must be rejected, not silently bound to a text field.");
		} catch (IllegalArgumentException expected) {
			// Expected: only PLAIN and REF properties are resolved.
		}
	}

	/**
	 * A {@code COMPLEX} property with only a value binding and no value provider must be rejected
	 * by {@code createModel} too - it has no format to turn its value into text, so it must not
	 * silently fall through to a plain {@link ConfigFieldModel} over the raw {@code List}.
	 */
	public void testCreateModelRejectsComplexWithoutValueProvider() {
		PropertyDescriptor property = _config.descriptor().getProperty(TestConfig.BINDING_ONLY);
		assertNull("Precondition: the property must actually have no value provider for this test "
			+ "to be meaningful.", property.getValueProvider());

		try {
			ConfigControlService.getInstance().createModel(_config, property);
			fail("A COMPLEX property without a value provider must be rejected, not silently bound "
				+ "to a plain field over the raw value.");
		} catch (IllegalArgumentException expected) {
			// Expected: COMPLEX is only accepted together with a value provider.
		}
	}

	/**
	 * {@code createControl} independently rejects the same binding-only {@code COMPLEX} property,
	 * exercised the same way as {@link #testCreateControlRejectsItemKind()}: by building the model
	 * directly, bypassing {@code createModel}'s own check.
	 */
	public void testCreateControlRejectsComplexWithoutValueProvider() {
		PropertyDescriptor property = _config.descriptor().getProperty(TestConfig.BINDING_ONLY);
		ConfigFieldModel model = new ConfigFieldModel(_config, property);

		try {
			ConfigControlService.getInstance().createControl(context(), model);
			fail("A COMPLEX property without a value provider must be rejected, not silently bound "
				+ "to a text field over the raw value.");
		} catch (IllegalArgumentException expected) {
			// Expected: COMPLEX is only accepted together with a value provider.
		}
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
