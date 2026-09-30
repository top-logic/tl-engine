/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.config.constraint.check.ConstraintFailure;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.NumberDisplay;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.control.form.ReactSliderControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.NumberInputControlProvider;

/**
 * Tests for {@link NumberInputControlProvider} - the shape a number is edited in, the range a
 * slider travels, and the constraints refusing a configuration that does not describe one.
 */
public class TestNumberInputControlProvider extends TestCase {

	/** Label of the built field, which decides no display. */
	private static final String LABEL = "Amount";

	/** The state key for the smallest value a slider can set. */
	private static final String MIN = "min";

	/** The state key for the largest value a slider can set. */
	private static final String MAX = "max";

	/** The state key for the distance between two values a slider can set. */
	private static final String STEP = "step";

	/** The state key carrying the value itself. */
	private static final String VALUE = "value";

	/** The state key carrying the value as written text. */
	private static final String VALUE_LABEL = "valueLabel";

	/** The digits and separators the formats of this test write in. */
	private static final DecimalFormatSymbols SYMBOLS = new DecimalFormatSymbols(Locale.GERMAN);

	private ReactContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;

		super.tearDown();
	}

	/** Without a display stated, the number is typed as text. */
	public void testANumberIsTypedByDefault() {
		assertEquals(NumberDisplay.INPUT, provider(null, null, null, null).getDisplay());
		assertEquals(ReactNumberInputControl.class,
			control(new NumberInputControlProvider(), wholeNumbers()).getClass());
		assertEquals(ReactNumberInputControl.class,
			control(provider(null, null, null, null), wholeNumbers()).getClass());
	}

	/** A slider is a control of its own, drawn by the component that shows a handle on a track. */
	public void testASliderIsAControlOfItsOwn() {
		ReactControl control = control(slider(0.0, 100.0, null), wholeNumbers());

		assertEquals(ReactSliderControl.class, control.getClass());
		assertEquals("TLSlider", control.getReactModule());
	}

	/** The client is handed the range the handle travels and the grid it snaps to. */
	public void testTheRangeReachesTheClient() {
		Map<String, Object> state = state(control(slider(-5.0, 5.0, 0.5), fractions()));

		assertEquals(-5.0, number(state, MIN));
		assertEquals(5.0, number(state, MAX));
		assertEquals(0.5, number(state, STEP));
	}

	/** A slider that is given no step moves by whole numbers. */
	public void testTheStepDefaultsToOne() {
		assertEquals(1.0, number(state(control(slider(0.0, 10.0, null), wholeNumbers())), STEP));
	}

	/** The text beside the handle is the value written in the format the field asks for. */
	public void testTheValueIsWrittenInTheFormatOfTheField() {
		Map<String, Object> state =
			state(control(slider(0.0, 100.0, 0.5), fractions(), new AbstractFieldModel(Double.valueOf(12.5))));

		assertEquals("The value travels as the number it is.", 12.5, number(state, VALUE));
		assertEquals("The German format of the field writes the fraction with a comma.",
			"12,50", state.get(VALUE_LABEL));
	}

	/** A field holding no value has no text to show: it is not the smallest value of the range. */
	public void testAnEmptyFieldShowsNoText() {
		Map<String, Object> state = state(control(slider(0.0, 100.0, null), fractions()));

		assertNull(state.get(VALUE));
		assertNull(state.get(VALUE_LABEL));
	}

	/**
	 * The number the client sends is stored as the kind of number the input field stores for the
	 * same attribute: a whole number where the field writes no fraction.
	 */
	public void testAClientNumberIsStoredAsTheFieldsKindOfNumber() {
		assertEquals(Long.valueOf(42), slidTo(wholeNumbers(), 42.0));
		assertEquals(Double.valueOf(12.5), slidTo(fractions(), 12.5));

		assertEquals("A slider stores the kind of number its input field stores.",
			typed(wholeNumbers(), "42").getClass(), slidTo(wholeNumbers(), 42.0).getClass());
		assertEquals("A slider stores the kind of number its input field stores.",
			typed(fractions(), "12,5").getClass(), slidTo(fractions(), 12.5).getClass());
	}

	/** The text follows the handle: it is written again for the value the client sends. */
	public void testTheTextFollowsAValueTheClientSends() {
		AbstractFieldModel model = new AbstractFieldModel(null);
		ReactControl control = control(slider(0.0, 100.0, 0.5), fractions(), model);

		control.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, Map.of(VALUE, Double.valueOf(12.5)));

		assertEquals("12,50", state(control).get(VALUE_LABEL));
	}

	/** The display is configured by the name each shape is known by in a view. */
	public void testTheConfiguredDisplayIsReadFromItsName() throws Exception {
		assertEquals(NumberDisplay.SLIDER, readConfig("<input-control "
			+ NumberInputControlProvider.Config.DISPLAY + "='" + NumberDisplay.SLIDER.getExternalName() + "' "
			+ NumberInputControlProvider.Config.MIN + "='0.0' "
			+ NumberInputControlProvider.Config.MAX + "='100.0'/>").getDisplay());
		assertEquals(NumberDisplay.INPUT, readConfig("<input-control/>").getDisplay());
	}

	/** A slider must be given the bounds of the range it travels. */
	public void testASliderWithoutBoundsIsRefused() throws ConfigurationException {
		assertProblems(config(NumberDisplay.SLIDER, null, null, null),
			NumberInputControlProvider.Config.MIN, NumberInputControlProvider.Config.MAX);
		assertProblems(config(NumberDisplay.SLIDER, Double.valueOf(0), null, null),
			NumberInputControlProvider.Config.MAX);
	}

	/** A range must have room to travel in: its smallest value lies below its largest. */
	public void testARangeMustHaveRoom() throws ConfigurationException {
		assertProblems(config(NumberDisplay.SLIDER, Double.valueOf(10), Double.valueOf(10), null),
			NumberInputControlProvider.Config.MIN, NumberInputControlProvider.Config.MAX);
		assertProblems(config(NumberDisplay.SLIDER, Double.valueOf(10), Double.valueOf(0), null),
			NumberInputControlProvider.Config.MIN, NumberInputControlProvider.Config.MAX);
	}

	/** A handle moves forward: its step is above zero. */
	public void testAStepMustBeAboveZero() throws ConfigurationException {
		assertProblems(config(NumberDisplay.SLIDER, Double.valueOf(0), Double.valueOf(10), Double.valueOf(0)),
			NumberInputControlProvider.Config.STEP);
		assertProblems(config(NumberDisplay.SLIDER, Double.valueOf(0), Double.valueOf(10), Double.valueOf(-1)),
			NumberInputControlProvider.Config.STEP);
	}

	/** A number typed as text has no range to state, so a configuration without bounds is whole. */
	public void testAnInputNeedsNoBounds() throws ConfigurationException {
		assertProblems(config(null, null, null, null));
		assertProblems(config(NumberDisplay.INPUT, null, null, null));
	}

	/** The value the given field holds after the client sends the given number to its slider. */
	private Object slidTo(FieldSpec field, double clientValue) {
		AbstractFieldModel model = new AbstractFieldModel(null);
		ReactControl control = control(slider(-100.0, 100.0, 0.5), field, model);

		control.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, Map.of(VALUE, Double.valueOf(clientValue)));

		return model.getValue();
	}

	/** The value the given field holds after the given text is typed into its input. */
	private Object typed(FieldSpec field, String text) {
		AbstractFieldModel model = new AbstractFieldModel(null);
		ReactControl control = control(new NumberInputControlProvider(), field, model);

		control.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, Map.of(VALUE, text));

		return model.getValue();
	}

	/** The control the given provider builds for an empty field of the given kind. */
	private ReactControl control(NumberInputControlProvider provider, FieldSpec field) {
		return control(provider, field, new AbstractFieldModel(null));
	}

	/** The control the given provider builds for the given field. */
	private ReactControl control(NumberInputControlProvider provider, FieldSpec field, AbstractFieldModel model) {
		return provider.createControl(_context, field, model);
	}

	/** The state the given control sends to its client. */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> state(ReactControl control) {
		String json = control.stateAsJSON();
		try {
			return (Map<String, Object>) JSON.fromString(json);
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state of a control is JSON, but is: " + json, ex);
		}
	}

	/** The number the given state holds under the given key. */
	private static double number(Map<String, Object> state, String key) {
		Object value = state.get(key);
		assertTrue(key + " holds a number, but holds: " + value, value instanceof Number);
		return ((Number) value).doubleValue();
	}

	/** A field whose format writes whole numbers. */
	private static FieldSpec wholeNumbers() {
		return field(new DecimalFormat("#,##0", SYMBOLS), Long.class);
	}

	/** A field whose format writes two decimal places. */
	private static FieldSpec fractions() {
		return field(new DecimalFormat("#,##0.00", SYMBOLS), Double.class);
	}

	/** A field of the given value type, written in the given format. */
	private static FieldSpec field(Format format, Class<?> valueType) {
		return FieldSpec.of(valueType, LABEL).setNumberFormat(format);
	}

	/** A provider offering a slider over the given range. */
	private static NumberInputControlProvider slider(Double min, Double max, Double step) {
		return provider(NumberDisplay.SLIDER, min, max, step);
	}

	/** A provider configured as stated, each option left out where {@code null} is given. */
	private static NumberInputControlProvider provider(NumberDisplay display, Double min, Double max, Double step) {
		return (NumberInputControlProvider) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(config(display, min, max, step));
	}

	/** A provider configuration stating the given options, leaving out the {@code null} ones. */
	private static NumberInputControlProvider.Config config(NumberDisplay display, Double min, Double max,
			Double step) {
		NumberInputControlProvider.Config config =
			TypedConfiguration.newConfigItem(NumberInputControlProvider.Config.class);
		set(config, NumberInputControlProvider.Config.DISPLAY, display);
		set(config, NumberInputControlProvider.Config.MIN, min);
		set(config, NumberInputControlProvider.Config.MAX, max);
		set(config, NumberInputControlProvider.Config.STEP, step);
		return config;
	}

	/** Sets the named property of the given configuration, unless no value is given. */
	private static void set(ConfigurationItem config, String property, Object value) {
		if (value != null) {
			config.update(config.descriptor().getProperty(property), value);
		}
	}

	/**
	 * Asserts that the given configuration is refused for exactly the named properties, and
	 * accepted where none are named.
	 */
	private static void assertProblems(ConfigurationItem config, String... properties) throws ConfigurationException {
		List<String> expected = new ArrayList<>(List.of(properties));
		Collections.sort(expected);

		assertEquals(expected, problems(config));
	}

	/** The properties the constraints of the given configuration report a problem on. */
	private static List<String> problems(ConfigurationItem config) throws ConfigurationException {
		ConstraintChecker checker = new ConstraintChecker();
		checker.check(config);

		List<String> result = new ArrayList<>();
		for (ConstraintFailure failure : checker.getFailures()) {
			result.add(failure.getContextProperty().getPropertyName());
		}
		Collections.sort(result);
		return result;
	}

	/** The provider configuration the given XML declares. */
	private static NumberInputControlProvider.Config readConfig(String xml) throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestNumberInputControlProvider.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(rootTag(xml),
			TypedConfiguration.getConfigurationDescriptor(NumberInputControlProvider.Config.class));

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml));
		Object result = reader.read();
		context.checkErrors();
		return (NumberInputControlProvider.Config) result;
	}

	/** The name of the root element of the given XML. */
	private static String rootTag(String xml) {
		int end = xml.indexOf(' ');
		if (end < 0) {
			end = xml.indexOf('/');
		}
		return xml.substring(1, end);
	}

	/** Suite loading the application configuration the read configurations are checked against. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(TestNumberInputControlProvider.class);
	}

}
