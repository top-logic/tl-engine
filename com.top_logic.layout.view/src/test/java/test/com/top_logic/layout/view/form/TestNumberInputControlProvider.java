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

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.config.constraint.check.ConstraintFailure;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.NumberDisplay;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.control.form.ReactSliderControl;
import com.top_logic.layout.react.control.form.SliderValueArguments;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.state.SliderState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.NamedInputs;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.NumberInputControlProvider;
import com.top_logic.layout.view.form.NumberInputControlProvider.Config;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link NumberInputControlProvider} - the shape a number is edited in, the range a
 * slider travels, computed by expressions over input channels, and the configurations refused for
 * not describing a range.
 */
public class TestNumberInputControlProvider extends TestCase {

	/** Label of the built field, which decides no display. */
	private static final String LABEL = "Amount";

	/** Name of the channel holding the range of the sliders under test. */
	private static final String TARGET = "target";

	/** Name of a second channel holding the unit. */
	private static final String UNIT = "unitChannel";

	/** A non-breaking space, written between a number and its unit. */
	private static final String NBSP = " ";

	/** The digits and separators the formats of this test write in. */
	private static final DecimalFormatSymbols SYMBOLS = new DecimalFormatSymbols(Locale.GERMAN);

	private ReactContext _context;

	private ViewContext _viewContext;

	private ViewChannel _target;

	private ViewChannel _unit;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_viewContext = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_target = new DefaultViewChannel(TARGET);
		_viewContext.registerChannel(TARGET, _target);
		_unit = new DefaultViewChannel(UNIT);
		_viewContext.registerChannel(UNIT, _unit);
	}

	/** Runs the test in an interaction: TL-Script is evaluated against the knowledge base. */
	@Override
	protected void runTest() throws Throwable {
		ThreadContext.<Void, Throwable, RuntimeException> inSystemContext(
			TestNumberInputControlProvider.class, () -> {
				super.runTest();
				return null;
			});
	}

	@Override
	protected void tearDown() throws Exception {
		_unit = null;
		_target = null;
		_viewContext = null;
		_context = null;

		super.tearDown();
	}

	/** Without a display stated, the number is typed as text. */
	public void testANumberIsTypedByDefault() throws Exception {
		assertEquals(NumberDisplay.INPUT, provider("").getDisplay());
		assertEquals(ReactNumberInputControl.class,
			control(new NumberInputControlProvider(), wholeNumbers()).getClass());
		assertEquals(ReactNumberInputControl.class, control(provider(""), wholeNumbers()).getClass());
	}

	/** A slider is a control of its own, drawn by the component that shows a handle on a track. */
	public void testASliderIsAControlOfItsOwn() throws Exception {
		ReactControl control = control(slider("0.0", "100.0", null), wholeNumbers());

		assertEquals(ReactSliderControl.class, control.getClass());
		assertEquals("TLSlider", control.getReactModule());
	}

	/** The client is handed the range the handle travels and the grid it snaps to. */
	public void testTheRangeReachesTheClient() throws Exception {
		Map<String, Object> state = state(control(slider("-5.0", "5.0", "0.5"), fractions()));

		assertEquals(-5.0, number(state, SliderState.MIN__PROP));
		assertEquals(5.0, number(state, SliderState.MAX__PROP));
		assertEquals(0.5, number(state, SliderState.STEP__PROP));
		assertEquals(Boolean.FALSE, state.get(FieldState.DISABLED__PROP));
	}

	/** A constant range may be computed rather than written as a number. */
	public void testAConstantRangeMayBeComputed() throws Exception {
		Map<String, Object> state = state(control(slider("2 * 5", "10 * 10", "100 / 20"), wholeNumbers()));

		assertEquals(10.0, number(state, SliderState.MIN__PROP));
		assertEquals(100.0, number(state, SliderState.MAX__PROP));
		assertEquals(5.0, number(state, SliderState.STEP__PROP));
	}

	/** A slider that is given no step moves by whole numbers. */
	public void testTheStepDefaultsToOne() throws Exception {
		assertEquals(1.0,
			number(state(control(slider("0.0", "10.0", null), wholeNumbers())), SliderState.STEP__PROP));
	}

	/** The text beside the handle is the value written in the format the field asks for. */
	public void testTheValueIsWrittenInTheFormatOfTheField() throws Exception {
		Map<String, Object> state =
			state(control(slider("0.0", "100.0", "0.5"), fractions(), new AbstractFieldModel(Double.valueOf(12.5))));

		assertEquals("The value travels as the number it is.", 12.5, number(state, FieldState.VALUE__PROP));
		assertEquals("The German format of the field writes the fraction with a comma.",
			"12,50", state.get(SliderState.VALUE_LABEL__PROP));
	}

	/**
	 * The bounds are written in the format of the value, so the client can make room for the widest
	 * text the value can take.
	 */
	public void testTheBoundsAreWrittenInTheFormatOfTheField() throws Exception {
		Map<String, Object> fractions = state(control(slider("-5.0", "100.0", "0.5"), fractions()));
		assertEquals("-5,00", fractions.get(SliderState.MIN_LABEL__PROP));
		assertEquals("100,00", fractions.get(SliderState.MAX_LABEL__PROP));

		Map<String, Object> wholeNumbers = state(control(slider("-5.0", "100.0", null), wholeNumbers()));
		assertEquals("-5", wholeNumbers.get(SliderState.MIN_LABEL__PROP));
		assertEquals("100", wholeNumbers.get(SliderState.MAX_LABEL__PROP));
	}

	/** A constant unit is written after the value and after both bounds. */
	public void testAConstantUnitIsWritten() throws Exception {
		NumberInputControlProvider provider = provider(slider() + range("0", "100", null) + attr(Config.UNIT, "'%'"));
		Map<String, Object> state =
			state(control(provider, wholeNumbers(), new AbstractFieldModel(Long.valueOf(12))));

		assertEquals("12" + NBSP + "%", state.get(SliderState.VALUE_LABEL__PROP));
		assertEquals("0" + NBSP + "%", state.get(SliderState.MIN_LABEL__PROP));
		assertEquals("100" + NBSP + "%", state.get(SliderState.MAX_LABEL__PROP));
	}

	/** An empty unit writes none. */
	public void testAnEmptyUnitIsNone() throws Exception {
		NumberInputControlProvider provider = provider(slider() + range("0", "100", null) + attr(Config.UNIT, "''"));
		Map<String, Object> state =
			state(control(provider, wholeNumbers(), new AbstractFieldModel(Long.valueOf(12))));

		assertEquals("12", state.get(SliderState.VALUE_LABEL__PROP));
	}

	/** A field holding no value has no text to show: it is not the smallest value of the range. */
	public void testAnEmptyFieldShowsNoText() throws Exception {
		Map<String, Object> state = state(control(slider("0.0", "100.0", null), fractions()));

		assertNull(state.get(FieldState.VALUE__PROP));
		assertNull(state.get(SliderState.VALUE_LABEL__PROP));
	}

	/**
	 * The number the client sends is stored as the kind of number the input field stores for the
	 * same attribute: a whole number where the field writes no fraction.
	 */
	public void testAClientNumberIsStoredAsTheFieldsKindOfNumber() throws Exception {
		assertEquals(Long.valueOf(42), slidTo(wholeNumbers(), 42.0));
		assertEquals(Double.valueOf(12.5), slidTo(fractions(), 12.5));

		assertEquals("A slider stores the kind of number its input field stores.",
			typed(wholeNumbers(), "42").getClass(), slidTo(wholeNumbers(), 42.0).getClass());
		assertEquals("A slider stores the kind of number its input field stores.",
			typed(fractions(), "12,5").getClass(), slidTo(fractions(), 12.5).getClass());
	}

	/** The text follows the handle: it is written again for the value the client sends. */
	public void testTheTextFollowsAValueTheClientSends() throws Exception {
		AbstractFieldModel model = new AbstractFieldModel(null);
		ReactControl control = control(slider("0.0", "100.0", "0.5"), fractions(), model);

		slide(control, 12.5);

		assertEquals("12,50", state(control).get(SliderState.VALUE_LABEL__PROP));
	}

	/** The display is configured by the name each shape is known by in a view. */
	public void testTheConfiguredDisplayIsReadFromItsName() throws Exception {
		assertEquals(NumberDisplay.SLIDER, readConfig("<input-control"
			+ slider() + attr(Config.MIN, "0.0") + attr(Config.MAX, "100.0") + "/>").getDisplay());
		assertEquals(NumberDisplay.INPUT, readConfig("<input-control/>").getDisplay());
	}

	/** A slider must be given the bounds of the range it travels. */
	public void testASliderWithoutBoundsIsRefused() throws Exception {
		assertProblems(readConfig("<input-control" + slider() + "/>"), Config.MIN, Config.MAX);
		assertProblems(readConfig("<input-control" + slider() + attr(Config.MIN, "0") + "/>"), Config.MAX);
	}

	/** A constant bound that is no number leaves the slider without a range: it is refused. */
	public void testAConstantBoundThatIsNoNumberIsRefused() throws Exception {
		assertRefused(slider() + range("null", "10", null), Config.MIN);
		assertRefused(slider() + range("0", "'ten'", null), Config.MAX);
	}

	/** A constant range must have room to travel in: its smallest value lies below its largest. */
	public void testAConstantRangeMustHaveRoom() throws Exception {
		assertRefused(slider() + range("10", "10", null), Config.MIN);
		assertRefused(slider() + range("10", "0", null), Config.MAX);
	}

	/** A handle moves forward: a constant step is above zero. */
	public void testAConstantStepMustBeAboveZero() throws Exception {
		assertRefused(slider() + range("0", "10", "0"), Config.STEP);
		assertRefused(slider() + range("0", "10", "-1"), Config.STEP);
	}

	/** A number typed as text has no range to state, so a configuration without bounds is whole. */
	public void testAnInputNeedsNoBounds() throws Exception {
		assertProblems(readConfig("<input-control/>"));
		assertProblems(readConfig("<input-control" + attr(Config.DISPLAY, NumberDisplay.INPUT.getExternalName())
			+ "/>"));
		provider(attr(Config.DISPLAY, NumberDisplay.INPUT.getExternalName()) + range("10", "0", "-1"));
	}

	/** The slider travels the range its input channel holds, unit included. */
	public void testTheRangeComesFromAnInputChannel() throws Exception {
		_target.set(dict("{'min': 0, 'max': 50, 'step': 5, 'unit': 'kg'}"));

		ReactControl control = control(inViewSlider(), _viewContext, wholeNumbers(),
			new AbstractFieldModel(Long.valueOf(10)));

		Map<String, Object> state = state(control);
		assertEquals(0.0, number(state, SliderState.MIN__PROP));
		assertEquals(50.0, number(state, SliderState.MAX__PROP));
		assertEquals(5.0, number(state, SliderState.STEP__PROP));
		assertEquals("0" + NBSP + "kg", state.get(SliderState.MIN_LABEL__PROP));
		assertEquals("50" + NBSP + "kg", state.get(SliderState.MAX_LABEL__PROP));
		assertEquals("10" + NBSP + "kg", state.get(SliderState.VALUE_LABEL__PROP));
		assertEquals(Boolean.FALSE, state.get(FieldState.DISABLED__PROP));
	}

	/** A changed channel moves the range, the grid and the unit of the slider. */
	public void testAChangedChannelMovesTheRange() throws Exception {
		_target.set(dict("{'min': 0, 'max': 50, 'step': 5, 'unit': 'kg'}"));
		ReactControl control = control(inViewSlider(), _viewContext, wholeNumbers(),
			new AbstractFieldModel(Long.valueOf(10)));

		_target.set(dict("{'min': -10, 'max': 10, 'step': 2, 'unit': '%'}"));

		Map<String, Object> state = state(control);
		assertEquals(-10.0, number(state, SliderState.MIN__PROP));
		assertEquals(10.0, number(state, SliderState.MAX__PROP));
		assertEquals(2.0, number(state, SliderState.STEP__PROP));
		assertEquals("-10" + NBSP + "%", state.get(SliderState.MIN_LABEL__PROP));
		assertEquals("10" + NBSP + "%", state.get(SliderState.MAX_LABEL__PROP));
		assertEquals("10" + NBSP + "%", state.get(SliderState.VALUE_LABEL__PROP));
	}

	/**
	 * A channel without a value leaves the slider without a range: it is disabled until the channel
	 * holds one.
	 */
	public void testAnEmptyChannelDisablesTheSlider() throws Exception {
		ReactControl control = control(inViewSlider(), _viewContext, wholeNumbers(), new AbstractFieldModel(null));

		Map<String, Object> empty = state(control);
		assertEquals(Boolean.TRUE, empty.get(FieldState.DISABLED__PROP));
		assertNull(empty.get(SliderState.MIN__PROP));
		assertNull(empty.get(SliderState.MAX__PROP));
		assertNull(empty.get(SliderState.MIN_LABEL__PROP));
		assertNull(empty.get(SliderState.MAX_LABEL__PROP));

		_target.set(dict("{'min': 0, 'max': 50}"));

		Map<String, Object> filled = state(control);
		assertEquals(Boolean.FALSE, filled.get(FieldState.DISABLED__PROP));
		assertEquals(50.0, number(filled, SliderState.MAX__PROP));
		assertEquals("A range without a step moves by whole numbers.",
			1.0, number(filled, SliderState.STEP__PROP));

		_target.set(dict("{'min': 50, 'max': 0}"));
		assertEquals("A range without room disables the slider again.",
			Boolean.TRUE, state(control).get(FieldState.DISABLED__PROP));
	}

	/** A slider no longer displayed no longer follows its channels. */
	public void testADisposedSliderNoLongerFollows() throws Exception {
		_target.set(dict("{'min': 0, 'max': 50}"));
		ReactControl control = control(inViewSlider(), _viewContext, wholeNumbers(), new AbstractFieldModel(null));

		control.cleanupTree();
		_target.set(dict("{'min': 0, 'max': 80}"));

		assertEquals(50.0, number(state(control), SliderState.MAX__PROP));
	}

	/** Several inputs are variables of their own, each under the name given with {@code as}. */
	public void testRenamedInputsAreVariablesUnderTheirName() throws Exception {
		NumberInputControlProvider provider = provider(slider()
			+ attr(NamedInputs.INPUTS, TARGET + " as r, " + UNIT + " as u")
			+ range("$r['min']", "$r['max']", null) + attr(Config.UNIT, "$u"));
		_target.set(dict("{'min': 1, 'max': 9}"));
		_unit.set("m");

		ReactControl control = control(provider, _viewContext, wholeNumbers(), new AbstractFieldModel(Long.valueOf(3)));
		assertEquals("9" + NBSP + "m", state(control).get(SliderState.MAX_LABEL__PROP));

		_unit.set(null);
		assertEquals("The unit channel changing alone rewrites the texts.",
			"9", state(control).get(SliderState.MAX_LABEL__PROP));
		assertEquals(Boolean.FALSE, state(control).get(FieldState.DISABLED__PROP));
	}

	/** Inputs are channels of a view: a slider reading them cannot be created outside of one. */
	public void testInputsNeedAView() throws Exception {
		NumberInputControlProvider provider = inViewSlider();
		try {
			control(provider, _context, wholeNumbers(), new AbstractFieldModel(null));
			fail("A slider reading channels must be refused outside of a view.");
		} catch (IllegalStateException ex) {
			// Expected.
		}
	}

	/** An input reads no range, so it needs no view even where inputs are declared. */
	public void testAnInputIgnoresItsInputs() throws Exception {
		NumberInputControlProvider provider = provider(attr(NamedInputs.INPUTS, TARGET)
			+ range("$target['min']", "$target['max']", null));

		assertEquals(ReactNumberInputControl.class,
			control(provider, _context, wholeNumbers(), new AbstractFieldModel(null)).getClass());
	}

	/** The slider of the ticket: a range read from a dictionary in a channel. */
	private NumberInputControlProvider inViewSlider() throws Exception {
		return provider(slider() + attr(NamedInputs.INPUTS, TARGET)
			+ range("$target['min']", "$target['max']", "$target['step']")
			+ attr(Config.UNIT, "$target['unit']"));
	}

	/** The value the given field holds after the client sends the given number to its slider. */
	private Object slidTo(FieldSpec field, double clientValue) throws Exception {
		AbstractFieldModel model = new AbstractFieldModel(null);
		ReactControl control = control(slider("-100.0", "100.0", "0.5"), field, model);

		slide(control, clientValue);

		return model.getValue();
	}

	/** The value the given field holds after the given text is typed into its input. */
	private Object typed(FieldSpec field, String text) {
		AbstractFieldModel model = new AbstractFieldModel(null);
		ReactControl control = control(new NumberInputControlProvider(), field, model);

		control.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, Map.of(SliderValueArguments.VALUE, text));

		return model.getValue();
	}

	private static void slide(ReactControl control, double value) {
		control.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED,
			Map.of(SliderValueArguments.VALUE, Double.valueOf(value)));
	}

	/** The control the given provider builds for an empty field of the given kind. */
	private ReactControl control(NumberInputControlProvider provider, FieldSpec field) {
		return control(provider, field, new AbstractFieldModel(null));
	}

	/** The control the given provider builds for the given field. */
	private ReactControl control(NumberInputControlProvider provider, FieldSpec field, AbstractFieldModel model) {
		return control(provider, _context, field, model);
	}

	/** The control the given provider builds for the given field in the given context. */
	private static ReactControl control(NumberInputControlProvider provider, ReactContext context, FieldSpec field,
			AbstractFieldModel model) {
		return provider.createControl(context, field, model);
	}

	/** The value of the given TL-Script dictionary literal. */
	private static Object dict(String source) throws ConfigurationException {
		return QueryExecutor.compile(ExprFormat.INSTANCE.getValue("dict", source)).execute();
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

	/** A provider offering a slider over the given constant range. */
	private static NumberInputControlProvider slider(String min, String max, String step) throws Exception {
		return provider(slider() + range(min, max, step));
	}

	/** The attribute asking for a slider. */
	private static String slider() {
		return attr(Config.DISPLAY, NumberDisplay.SLIDER.getExternalName());
	}

	/** The attributes stating the given range expressions, leaving out the {@code null} ones. */
	private static String range(String min, String max, String step) {
		return attr(Config.MIN, min) + attr(Config.MAX, max) + attr(Config.STEP, step);
	}

	/** The given XML attribute, or nothing for a {@code null} value. */
	private static String attr(String name, String value) {
		if (value == null) {
			return "";
		}
		return " " + name + "=\"" + value.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;") + "\"";
	}

	/** The provider the given attributes of an {@code input-control} configure. */
	private static NumberInputControlProvider provider(String attributes) throws Exception {
		Config config = readConfig("<input-control" + attributes + "/>");
		DefaultInstantiationContext context = new DefaultInstantiationContext(new BufferingProtocol());
		NumberInputControlProvider result = (NumberInputControlProvider) context.getInstance(config);
		context.checkErrors();
		return result;
	}

	/**
	 * Asserts that the provider the given attributes configure is refused on instantiation, with a
	 * message naming the given property.
	 */
	private static void assertRefused(String attributes, String property) throws Exception {
		Config config = readConfig("<input-control" + attributes + "/>");
		BufferingProtocol protocol = new BufferingProtocol();
		new DefaultInstantiationContext(protocol).getInstance(config);

		assertTrue("A slider without a range to travel must be refused: " + attributes, protocol.hasErrors());
		String error = protocol.getError();
		assertTrue("The message names the property '" + property + "': " + error,
			error.contains("'" + property + "'"));
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
	private static Config readConfig(String xml) throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestNumberInputControlProvider.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(rootTag(xml),
			TypedConfiguration.getConfigurationDescriptor(Config.class));

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml));
		Object result = reader.read();
		context.checkErrors();
		return (Config) result;
	}

	/** The name of the root element of the given XML. */
	private static String rootTag(String xml) {
		int end = xml.indexOf(' ');
		if (end < 0) {
			end = xml.indexOf('/');
		}
		return xml.substring(1, end);
	}

	/**
	 * Suite evaluating TL-Script against the application model in a knowledge base, with the
	 * resources a unit text is read with.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestNumberInputControlProvider.class,
			ServiceTestSetup.createStarterFactoryForModules(ThreadContextManager.Module.INSTANCE,
				ResourcesModule.Module.INSTANCE, PersistencyLayer.Module.INSTANCE, ModelService.Module.INSTANCE));
	}

}
