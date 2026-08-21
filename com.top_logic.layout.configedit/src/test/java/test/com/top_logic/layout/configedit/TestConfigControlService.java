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
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
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
import com.top_logic.layout.configedit.ConfigSelectFieldModel;
import com.top_logic.layout.configedit.DatePickerFormatProvider;
import com.top_logic.layout.form.values.edit.OptionMapping;
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
	 * A second {@code Date} format ({@link TimeOfDayAsDateValueProvider} stands in for the format
	 * that {@link ConfigControlService}'s configured format-provider map actually claims) - used
	 * to prove that an <em>unclaimed</em> {@code Date} format keeps the classic text behaviour,
	 * in contrast to {@link TimeOfDayAsDateValueProvider}.
	 */
	public static class OtherDateFormat extends AbstractConfigurationValueProvider<Date> {

		/** Creates an {@link OtherDateFormat}. */
		public OtherDateFormat() {
			super(Date.class);
		}

		@Override
		protected Date getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			throw new UnsupportedOperationException("Not exercised by these tests.");
		}

		@Override
		protected String getSpecificationNonNull(Date configValue) {
			throw new UnsupportedOperationException("Not exercised by these tests.");
		}
	}

	/**
	 * A {@code Date} format standing in for a real, registered value provider - used only to
	 * prove the format-provider map's superclass walk. The real
	 * {@link TimeOfDayAsDateValueProvider} cannot stand in for this itself: its constructor is
	 * private, so it has no subclass to test with.
	 */
	public static class BaseFormat extends AbstractConfigurationValueProvider<Date> {

		/** Creates a {@link BaseFormat}. */
		public BaseFormat() {
			super(Date.class);
		}

		@Override
		protected Date getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			throw new UnsupportedOperationException("Not exercised by these tests.");
		}

		@Override
		protected String getSpecificationNonNull(Date configValue) {
			throw new UnsupportedOperationException("Not exercised by these tests.");
		}
	}

	/**
	 * A pure specialization of {@link BaseFormat} - carries no members of its own, so any
	 * behaviour observed for a property using this format can only be explained by the mapping
	 * registered for its superclass.
	 */
	public static class SubFormat extends BaseFormat {
		// No additional members.
	}

	/** Options function offering two fixed times of day. */
	public static class Times extends Function0<List<Date>> {
		@Override
		public List<Date> apply() {
			return Arrays.asList(new Date(0), new Date(3_600_000));
		}
	}

	/**
	 * A minimal value type that is not {@code String}, {@code boolean}, numeric, or {@code Date} -
	 * none of the Java types {@link ConfigControlService}'s built-in widgets handle directly.
	 * Used only to prove that a property claimed by the format-provider map gets the plain model
	 * regardless of whether its Java type happens to be one of those built-in types.
	 */
	public static final class Coordinate {

		private final int _x;

		/** Creates a {@link Coordinate}. */
		public Coordinate(int x) {
			_x = x;
		}

		/** The coordinate value. */
		public int getX() {
			return _x;
		}
	}

	/**
	 * A format for {@link Coordinate} - stands in for a real value provider over a type outside
	 * createModel's built-in "directly editable" set, e.g. the spec's own {@code ResKey} case.
	 */
	public static class CoordinateFormat extends AbstractConfigurationValueProvider<Coordinate> {

		/** Creates a {@link CoordinateFormat}. */
		public CoordinateFormat() {
			super(Coordinate.class);
		}

		@Override
		protected Coordinate getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			throw new UnsupportedOperationException("Not exercised by these tests.");
		}

		@Override
		protected String getSpecificationNonNull(Coordinate configValue) {
			throw new UnsupportedOperationException("Not exercised by these tests.");
		}
	}

	/**
	 * The stored value of a {@link #SHAPE_REF} property - a name resolved against
	 * {@link Shapes}' fixed offering, formatted and parsed through {@link ShapeRefFormat}. Stands
	 * in for {@link com.top_logic.model.util.TLModelPartRef}: like that type, its options
	 * ({@link Shape}) are a <em>different</em> Java type than the value actually stored, so the
	 * two can only be translated through a non-identity {@link OptionMapping}
	 * ({@link ShapeMapping}) - without needing a whole {@code TLModel} to prove the same defect.
	 */
	public static final class ShapeRef {

		private final String _name;

		/** Creates a {@link ShapeRef}. */
		public ShapeRef(String name) {
			_name = name;
		}

		/** The referenced shape's name. */
		public String getName() {
			return _name;
		}

		@Override
		public String toString() {
			// Deliberately not the format's specification syntax: this is what a select control
			// would send back to the server for an option it could not otherwise identify, and
			// what the old, unfixed isSelect used to hand to ConfigSelectFieldModel#setValue.
			return "ShapeRef(" + _name + ")";
		}
	}

	/**
	 * An option offered for a {@link #SHAPE_REF} property - a different Java type than
	 * {@link ShapeRef}, the value the property actually stores. See {@link ShapeRef}'s own doc
	 * comment.
	 */
	public static final class Shape {

		private final String _name;

		/** Creates a {@link Shape}. */
		public Shape(String name) {
			_name = name;
		}

		/** The shape's name. */
		public String getName() {
			return _name;
		}
	}

	/** Options function offering two fixed shapes. */
	public static class Shapes extends Function0<List<Shape>> {
		@Override
		public List<Shape> apply() {
			return Arrays.asList(new Shape("circle"), new Shape("square"));
		}
	}

	/**
	 * Non-identity {@link OptionMapping} translating a {@link Shape} option into the
	 * {@link ShapeRef} a {@link #SHAPE_REF} property actually stores, and back - the same shape
	 * {@link com.top_logic.model.util.TLModelPartRef.PartMapping} has for the real case.
	 */
	public static class ShapeMapping implements OptionMapping {

		/** Singleton {@link ShapeMapping} instance. */
		public static final ShapeMapping INSTANCE = new ShapeMapping();

		@Override
		public Object toSelection(Object option) {
			return new ShapeRef(((Shape) option).getName());
		}

		@Override
		public Object asOption(Iterable<?> allOptions, Object selection) {
			String name = ((ShapeRef) selection).getName();
			for (Object option : allOptions) {
				if (((Shape) option).getName().equals(name)) {
					return option;
				}
			}
			return null;
		}
	}

	/**
	 * A format for {@link ShapeRef} - parses and formats a {@link ShapeRef} by its plain name, so
	 * a property using it can round-trip correctly through the format text field once
	 * {@code isSelect} correctly falls through to it.
	 */
	public static class ShapeRefFormat extends AbstractConfigurationValueProvider<ShapeRef> {

		/** Creates a {@link ShapeRefFormat}. */
		public ShapeRefFormat() {
			super(ShapeRef.class);
		}

		@Override
		protected ShapeRef getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			return new ShapeRef(propertyValue.toString());
		}

		@Override
		protected String getSpecificationNonNull(ShapeRef configValue) {
			return configValue.getName();
		}
	}

	/**
	 * Test-only subclass letting this test call the inherited, {@code protected}
	 * {@code startUp()} lifecycle method directly - the constructor of
	 * {@link ConfigControlService} itself never calls it (the module system does that, once the
	 * corresponding module is active), so a hand-built {@link ConfigControlService} instance
	 * needs a subclass that calls it explicitly. Needed to build a {@link ConfigControlService}
	 * instance around a hand-built {@link com.top_logic.layout.configedit.ConfigControlService.Config}
	 * in tests such as {@link #testFormatProviderMappingCoversSubclasses()} - the real singleton
	 * only ever loads its configuration from the application's own webapp config, which cannot
	 * reference a test-only value-provider class such as {@link BaseFormat}.
	 */
	private static final class TestableConfigControlService extends ConfigControlService {

		TestableConfigControlService(InstantiationContext context, Config config) {
			super(context, config);
			startUp();
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

		/** Property name for {@link #getOtherDateFormat()}. */
		String OTHER_DATE_FORMAT = "otherDateFormat";

		/** Property name for {@link #getEncryptedTimeOfDay()}. */
		String ENCRYPTED_TIME_OF_DAY = "encryptedTimeOfDay";

		/** Property name for {@link #getSubFormatted()}. */
		String SUB_FORMATTED = "subFormatted";

		/** Property name for {@link #getClaimedWithOptions()}. */
		String CLAIMED_WITH_OPTIONS = "claimedWithOptions";

		/** Property name for {@link #getCoordinate()}. */
		String COORDINATE = "coordinate";

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

		/** Property name for {@link #getShapeRef()}. */
		String SHAPE_REF = "shapeRef";

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
		 * A time of day - carries no date, only an hour and a minute. Its {@code @Format} is
		 * claimed by {@link ConfigControlService}'s configured format-provider map (see the
		 * service's {@code tl-layout-configedit.conf.config.xml}), so - unlike a property whose
		 * format is not claimed - it is edited in a time picker bound to the raw {@code Date}
		 * value, not as that format's text.
		 */
		@Name(TIME_OF_DAY)
		@Format(TimeOfDayAsDateValueProvider.class)
		Date getTimeOfDay();

		/**
		 * A {@code Date} property with its own {@code @Format}, like {@link #getTimeOfDay()}, but
		 * one the configured format-provider map does not claim - kept out of the date picker
		 * exactly like the classic declarative form's {@code ValueEditor} vetoes its
		 * {@code calendar} field, and edited as this format's text instead.
		 */
		@Name(OTHER_DATE_FORMAT)
		@Format(OtherDateFormat.class)
		Date getOtherDateFormat();

		/**
		 * Both {@code @Encrypted} and the same claimed format as {@link #getTimeOfDay()} - proves
		 * that {@code @Encrypted} keeps winning even when the property's format is one the
		 * configured map maps to a control of its own: a module must never be able to make a
		 * secret readable by mapping a control to its value provider.
		 */
		@Name(ENCRYPTED_TIME_OF_DAY)
		@Encrypted
		@Format(TimeOfDayAsDateValueProvider.class)
		Date getEncryptedTimeOfDay();

		/**
		 * A {@code Date} property formatted by {@link SubFormat}, a subclass of {@link BaseFormat}
		 * - used only by {@link #testFormatProviderMappingCoversSubclasses()} to prove that a
		 * mapping registered for {@link BaseFormat} also covers this subclass.
		 */
		@Name(SUB_FORMATTED)
		@Format(SubFormat.class)
		Date getSubFormatted();

		/**
		 * A {@code Date} property both claimed by the format-provider map (via
		 * {@link TimeOfDayAsDateValueProvider}, the same registration {@link #getTimeOfDay()}
		 * uses) and carrying its own {@code @Options} - proves that options win over the claim:
		 * the narrower, per-property statement of exactly this value set must not be overridden
		 * by the broader, per-value-provider-class registration.
		 */
		@Name(CLAIMED_WITH_OPTIONS)
		@Format(TimeOfDayAsDateValueProvider.class)
		@Options(fun = Times.class)
		Date getClaimedWithOptions();

		/**
		 * A property whose type ({@link Coordinate}) is not one of createModel's built-in
		 * "directly editable" types - used only by
		 * {@link #testFormatProviderMappingOnNonDirectlyEditableTypeGetsPlainModel()} to prove
		 * the domain-pairing invariant holds structurally, not just for the shipped {@code Date}
		 * case.
		 */
		@Name(COORDINATE)
		@Format(CoordinateFormat.class)
		Coordinate getCoordinate();

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

		/**
		 * A property whose {@code @Options} mapping ({@link ShapeMapping}) is not the identity -
		 * its options ({@link Shape}) are a different Java type than the value it actually stores
		 * ({@link ShapeRef}), mirroring {@link com.top_logic.model.util.TLModelPartRef}. Used only
		 * by {@link #testOptionMappingNotIdentityFallsThroughToFormatField()} to prove that such a
		 * property is edited as text through its own format, not by selecting.
		 */
		@Name(SHAPE_REF)
		@Format(ShapeRefFormat.class)
		@Options(fun = Shapes.class, mapping = ShapeMapping.class)
		ShapeRef getShapeRef();
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
	 * A {@code Date} property whose {@code @Format} ({@link TimeOfDayAsDateValueProvider}) is
	 * claimed by the configured format-provider map gets the time picker, bound to the raw
	 * {@code Date} value - not the generic format text field a specialized property otherwise
	 * gets. Both the model and the control are asserted: the model alone would pass even if the
	 * control were wrongly handed a format-aware model (a {@code String} where
	 * {@link ReactDatePickerControl}'s constructor expects a {@code Date}), and the control alone
	 * would pass even if the model still held text.
	 */
	public void testTimeOfDayFormatIsClaimedByDatePicker() {
		assertFalse("A property claimed by the format-provider map must not be parsed through its "
			+ "format - the claimed control binds to the raw typed value.",
			model(TestConfig.TIME_OF_DAY) instanceof ConfigFormatFieldModel);

		ReactControl control = control(TestConfig.TIME_OF_DAY);
		assertTrue("A time-of-day property must be edited in the date picker registered for its "
			+ "format.", control instanceof ReactDatePickerControl);
		assertEquals("The claimed control must be configured for the time part, not a bare date.",
			"time", control.scriptingScalarState().get("inputType"));
	}

	/**
	 * A {@code Date} property with its own {@code @Format} that the configured format-provider map
	 * does <em>not</em> claim is specialized just like {@link TestConfig#getTimeOfDay()} used to
	 * be before that map existed - it never reaches the date picker, it is edited as that
	 * format's text,
	 * exactly like the classic declarative form's {@code ValueEditor}/{@code PlainEditor}. The
	 * Java-type map alone could not distinguish this property from {@link TestConfig#TIME_OF_DAY}
	 * - both are {@code Date} - only the value provider's own class can.
	 */
	public void testDateWithUnclaimedFormatIsNotAPicker() {
		assertTrue("An unclaimed formatted Date property must be parsed through its format, not "
			+ "edited raw.", model(TestConfig.OTHER_DATE_FORMAT) instanceof ConfigFormatFieldModel);
		assertTrue("An unclaimed formatted Date property must not reach the date picker.",
			control(TestConfig.OTHER_DATE_FORMAT) instanceof ReactTextInputControl);
	}

	/**
	 * {@code @Encrypted} wins over a claim in the configured format-provider map, exactly like it
	 * wins over an explicit {@link ConfigControl} annotation: a module must never be able to make
	 * a secret readable by mapping a control to its value provider. Uses the very same
	 * {@link TimeOfDayAsDateValueProvider} format {@link #testTimeOfDayFormatIsClaimedByDatePicker()}
	 * proves is claimed - the only difference is {@code @Encrypted} - so the only possible
	 * explanation for a different outcome here is the encryption veto.
	 */
	public void testEncryptedWinsOverFormatProviderMapping() {
		assertTrue("An encrypted property must be parsed through its format even when that "
			+ "format is claimed by the configured map.",
			model(TestConfig.ENCRYPTED_TIME_OF_DAY) instanceof ConfigFormatFieldModel);

		ReactControl control = control(TestConfig.ENCRYPTED_TIME_OF_DAY);
		assertTrue("An encrypted property must be edited in a password field.",
			control instanceof ReactPasswordInputControl);
		assertFalse("The format mapping's date picker must not be reachable for an encrypted "
			+ "property.", control instanceof ReactDatePickerControl);
	}

	/**
	 * A property whose value provider ({@link SubFormat}) is a subclass of a mapping's registered
	 * class ({@link BaseFormat}) is claimed too: the format-provider map is looked up by the
	 * property's concrete value-provider class first, then by walking up its superclasses.
	 *
	 * <p>
	 * Exercised against a locally built {@link ConfigControlService} instance rather than the
	 * singleton: the singleton's only registered mapping ({@link TimeOfDayAsDateValueProvider})
	 * has a private constructor and so has no subclass to prove the walk with.
	 * </p>
	 */
	public void testFormatProviderMappingCoversSubclasses() {
		DatePickerFormatProvider.Config providerConfig =
			TypedConfiguration.newConfigItem(DatePickerFormatProvider.Config.class);
		set(providerConfig, "kind", ReactDatePickerControl.Kind.TIME);

		ConfigControlService.FormatMapping mapping =
			TypedConfiguration.newConfigItem(ConfigControlService.FormatMapping.class);
		set(mapping, ConfigControlService.FormatMapping.PROVIDER, BaseFormat.class);
		set(mapping, "impl", providerConfig);

		ConfigControlService.Config serviceConfig = TypedConfiguration.newConfigItem(ConfigControlService.Config.class);
		serviceConfig.getFormats().put(BaseFormat.class, mapping);

		ConfigControlService service = new TestableConfigControlService(
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, serviceConfig);

		PropertyDescriptor property = _config.descriptor().getProperty(TestConfig.SUB_FORMATTED);
		ConfigFieldModel model = service.createModel(_config, property);
		assertFalse("A property claimed through a superclass mapping must not be parsed through "
			+ "its format - the claimed control binds to the raw typed value.",
			model instanceof ConfigFormatFieldModel);

		ReactControl control = service.createControl(context(), model);
		assertTrue("A value provider that is a subclass of a mapped one must be claimed by the "
			+ "same mapping.", control instanceof ReactDatePickerControl);
		assertEquals("time", control.scriptingScalarState().get("inputType"));
	}

	/**
	 * The value-type-to-provider map ({@link ConfigControlService.Config#getProviders()}) is the
	 * extension point that lets a module above this one contribute a control by Java type - see
	 * this class's own JavaDoc. Registering a mapping for {@code String} must beat the built-in
	 * text input a {@code String} property would otherwise get - step 4 of the resolution chain,
	 * ahead of the built-in fallback (step 6).
	 */
	public void testProviderMappingByValueTypeBeatsBuiltInWidget() {
		PolymorphicConfiguration<ConfigControlProvider> impl =
			TypedConfiguration.newConfigItem(PolymorphicConfiguration.class);
		impl.setImplementationClass(FixedCheckboxProvider.class);

		ConfigControlService.ProviderMapping mapping =
			TypedConfiguration.newConfigItem(ConfigControlService.ProviderMapping.class);
		set(mapping, ConfigControlService.ProviderMapping.TYPE, String.class);
		set(mapping, "impl", impl);

		ConfigControlService.Config serviceConfig =
			TypedConfiguration.newConfigItem(ConfigControlService.Config.class);
		serviceConfig.getProviders().put(String.class, mapping);

		ConfigControlService service = new TestableConfigControlService(
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, serviceConfig);

		PropertyDescriptor property = _config.descriptor().getProperty(TestConfig.TEXT);
		ConfigFieldModel model = service.createModel(_config, property);

		ReactControl control = service.createControl(context(), model);
		assertTrue("A mapping registered for the property's Java type must beat the built-in "
			+ "widget that type would otherwise get.", control instanceof ReactCheckboxControl);
		assertFalse("The built-in text input must not be reachable once a mapping claims the type.",
			control instanceof ReactTextInputControl);
	}

	/**
	 * A property that is both claimed by the format-provider map and carries its own
	 * {@code @Options} still gets the select, in both the model and the control: the narrower,
	 * per-property option list wins over the broader, per-value-provider-class claim. Before this
	 * mapping existed, options always won; this guards that priority now that a second mechanism
	 * could compete for the same property.
	 */
	public void testOptionsWinOverFormatProviderMapping() {
		assertTrue("A property with its own @Options must be edited by selecting, even when its "
			+ "format is claimed by the configured map.",
			model(TestConfig.CLAIMED_WITH_OPTIONS) instanceof ConfigSelectFieldModel);

		ReactControl control = control(TestConfig.CLAIMED_WITH_OPTIONS);
		assertTrue("An options property must get the select control.",
			control instanceof ReactSelectFormFieldControl);
		assertFalse("The format mapping's date picker must not be reachable when @Options wins.",
			control instanceof ReactDatePickerControl);
	}

	/**
	 * A property claimed by the format-provider map gets the plain model even when its Java type
	 * ({@link Coordinate}) is not one of createModel's built-in "directly editable" types - the
	 * claim itself states that the control edits the value in its typed form, so the model must
	 * follow from the claim, not from a coincidental type match. Guards every future mapping, not
	 * just the shipped {@code Date} case: a mapping registered for a value provider over a type
	 * such as {@code ResKey} must not produce a domain mismatch between model and control.
	 */
	public void testFormatProviderMappingOnNonDirectlyEditableTypeGetsPlainModel() {
		ConfigControlService.FormatMapping mapping =
			TypedConfiguration.newConfigItem(ConfigControlService.FormatMapping.class);
		set(mapping, ConfigControlService.FormatMapping.PROVIDER, CoordinateFormat.class);

		PolymorphicConfiguration<ConfigControlProvider> impl =
			TypedConfiguration.newConfigItem(PolymorphicConfiguration.class);
		impl.setImplementationClass(FixedCheckboxProvider.class);
		set(mapping, "impl", impl);

		ConfigControlService.Config serviceConfig = TypedConfiguration.newConfigItem(ConfigControlService.Config.class);
		serviceConfig.getFormats().put(CoordinateFormat.class, mapping);

		ConfigControlService service = new TestableConfigControlService(
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, serviceConfig);

		PropertyDescriptor property = _config.descriptor().getProperty(TestConfig.COORDINATE);
		ConfigFieldModel model = service.createModel(_config, property);
		assertFalse("A claimed property must get the plain model regardless of whether its Java "
			+ "type is one of the built-in directly-editable types - the claim states that the "
			+ "control edits the value in its typed form.",
			model instanceof ConfigFormatFieldModel);

		ReactControl control = service.createControl(context(), model);
		assertTrue("The claimed control must be used, bound to the plain model.",
			control instanceof ReactCheckboxControl);
	}

	/**
	 * A property whose {@code @Options} mapping is not the identity ({@link ShapeMapping}, the
	 * same shape {@link com.top_logic.model.util.TLModelPartRef.PartMapping} has) must not be
	 * edited by selecting: the option ({@link Shape}) and the stored value ({@link ShapeRef}) are
	 * different Java types, so a select control could only send back an option's
	 * {@link Object#toString()}, which nothing here would translate back into a {@link ShapeRef}.
	 * Before the fix, {@code isSelect} only checked whether the property had options at all, so
	 * this property got {@link ConfigSelectFieldModel} regardless of the mapping - and a client
	 * round-trip through {@link ConfigSelectFieldModel#setValue(Object)} would throw an uncaught
	 * {@link IllegalArgumentException} instead of failing gracefully as a field error. The fix
	 * falls through to the format text field instead, which already round-trips the value
	 * correctly through {@link ShapeRefFormat}.
	 */
	public void testOptionMappingNotIdentityFallsThroughToFormatField() {
		assertFalse("A property whose option mapping is not the identity must not be edited by "
			+ "selecting - the option and the stored value are different types.",
			model(TestConfig.SHAPE_REF) instanceof ConfigSelectFieldModel);
		assertTrue("It must fall through to the format text field instead, which already knows "
			+ "how to parse and format the stored value.",
			model(TestConfig.SHAPE_REF) instanceof ConfigFormatFieldModel);

		ReactControl control = control(TestConfig.SHAPE_REF);
		assertTrue("Must be edited as text through its own format.",
			control instanceof ReactTextInputControl);
		assertFalse("Must not be edited by selecting: the option mapping would be silently ignored, "
			+ "the same defect TLModelPartRef has.", control instanceof ReactSelectFormFieldControl);

		// The format text field must actually round-trip the value correctly - the improvement
		// over both the old raw-string write and today's exception.
		ConfigFormatFieldModel model = (ConfigFormatFieldModel) model(TestConfig.SHAPE_REF);
		model.setValue("circle");
		assertEquals("circle", _config.getShapeRef().getName());
		assertEquals("circle", model.getValue());
		assertNull("A well-formed shape name must not be rejected.", model.getInputError());
	}

	/** Sets the named property of the given item, bypassing the need for a declared setter. */
	private static void set(ConfigurationItem item, String propertyName, Object value) {
		item.update(item.descriptor().getProperty(propertyName), value);
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
