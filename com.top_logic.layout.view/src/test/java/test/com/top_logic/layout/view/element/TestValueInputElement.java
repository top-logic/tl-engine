/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.format.DateStyle;
import com.top_logic.basic.format.TimeFormatDefinition;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.config.annotation.ConfigType;
import com.top_logic.element.meta.form.constraint.I18NConstants;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.NumberDisplay;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.form.ReactSliderControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.control.select.SelectDisplay;
import com.top_logic.layout.react.field.FieldControlRegistry;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.DatePickerState.Precision;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.state.FormFieldState;
import com.top_logic.layout.react.state.TypingFieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.element.PanelElement;
import com.top_logic.layout.view.element.SubmitOn;
import com.top_logic.layout.view.element.ValueInputElement;
import com.top_logic.layout.view.form.AttributeOptions;
import com.top_logic.layout.view.form.BooleanControlProvider;
import com.top_logic.layout.view.form.ChannelFieldBinding;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.NumberInputControlProvider;
import com.top_logic.layout.view.form.SelectControlProvider;
import com.top_logic.layout.view.form.TLInputControl;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.ui.BooleanPresentation;
import com.top_logic.model.annotate.ui.ClassificationDisplay;
import com.top_logic.model.annotate.ui.ClassificationDisplay.ClassificationPresentation;
import com.top_logic.model.annotate.ui.Format;
import com.top_logic.model.annotate.ui.FormatBase;
import com.top_logic.model.annotate.ui.ReferenceDisplay;
import com.top_logic.model.annotate.ui.ReferencePresentation;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.model.CompatibilityService;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link ValueInputElement} - the {@code <value-input>} element binding a channel to an input
 * control.
 *
 * <p>
 * Two properties are under test. First, the control: a value of a given type is entered the same
 * way whether a {@code <value-input>} carries it on a channel or a {@code <field>} carries it in an
 * attribute, because both describe the value with a {@link FieldSpec} built by
 * {@link FieldControlService} - from the type alone respectively from the attribute - and both
 * resolve the control from it. Second, the binding: what the channel receives reaches the field
 * model, and what the field model is given reaches the channel.
 * </p>
 *
 * <p>
 * Third, the submit hook: which controls report a submit of their own, that the submit a client
 * sends both stores the value and reports it, and that only a value the user produced counts as
 * committed. The {@link ValueInputElement.Config#getSubmitOn() submit gesture} decides for a typed
 * value whether Enter or every finished edit runs the command, and leaves a picked value alone.
 * </p>
 *
 * <p>
 * The model is transient, so the test needs neither a knowledge base nor the application model.
 * </p>
 */
public class TestValueInputElement extends TestCase {

	/** {@link ConfigType} value marking a datatype as a time of day. */
	private static final String CONFIG_TYPE_TIME = "TIME";

	/** Name of the property {@link TimeFormatDefinition.Config#getStyle()}. */
	private static final String TIME_STYLE = "style";

	/** Name of the module the types under test live in. */
	private static final String MODULE = "test.input";

	/** Label passed to both sides of a comparison, since the label decides no control. */
	private static final String LABEL = "Value";

	/**
	 * State key by which the client learns to submit on Enter.
	 *
	 * @implNote Restated here because
	 *           {@link com.top_logic.layout.react.control.form.ReactFormFieldControl} keeps it
	 *           protected for its subclasses.
	 */
	private static final String SUBMIT_ON_ENTER = "submitOnEnter";

	/**
	 * State key by which the client learns the text to show in the empty input.
	 *
	 * @implNote Restated here because
	 *           {@link com.top_logic.layout.react.control.form.ReactFormFieldControl} keeps it
	 *           protected for its subclasses.
	 */
	private static final String PLACEHOLDER = "placeholder";

	/**
	 * State key by which the client learns the icon to draw inside the input.
	 *
	 * @implNote Restated here because
	 *           {@link com.top_logic.layout.react.control.form.ReactFormFieldControl} keeps it
	 *           protected for its subclasses.
	 */
	private static final String ICON = "icon";

	/**
	 * State key by which the client learns to offer a button that empties the input.
	 *
	 * @implNote Restated here because
	 *           {@link com.top_logic.layout.react.control.form.ReactFormFieldControl} keeps it
	 *           protected for its subclasses.
	 */
	private static final String CLEARABLE = "clearable";

	/**
	 * State key by which the client learns how long to hold a typed value back.
	 *
	 * @implNote Restated here because
	 *           {@link com.top_logic.layout.react.control.form.ReactFormFieldControl} keeps it
	 *           protected for its subclasses.
	 */
	private static final String DEBOUNCE_MS = "debounceMs";

	/** Position of the text input of the test view running its command on every finished edit. */
	private static final int TEXT_ON_CHANGE = 8;

	/** Position of the text input of the test view running its command on Enter. */
	private static final int TEXT_ON_ENTER = 9;

	/** Position of the checkbox of the test view stating that its command runs on every edit. */
	private static final int FLAG_ON_CHANGE = 10;

	/** Position of the checkbox of the test view stating no submit gesture. */
	private static final int FLAG_ON_ENTER = 11;

	/** Position of the labelled enumeration input of the test view whose value is mandatory. */
	private static final int MANDATORY_CHOICE = 12;

	/** Position of the labelled enumeration input of the test view whose value may be empty. */
	private static final int OPTIONAL_CHOICE = 13;

	/** Position of the enumeration input of the test view annotated to be displayed as radios. */
	private static final int RADIO_CHOICE = 14;

	/** Position of the enumeration input of the test view annotated with a control. */
	private static final int ANNOTATED_CONTROL = 15;

	/**
	 * Position of the enumeration input of the test view naming a control of its own besides the
	 * annotated one.
	 */
	private static final int NAMED_OVER_ANNOTATED = 16;

	/** Position of the mandatory text input of the test view running its command on Enter. */
	private static final int MANDATORY_TEXT_ON_ENTER = 17;

	/**
	 * Position of the mandatory text input of the test view running its command on every finished
	 * edit.
	 */
	private static final int MANDATORY_TEXT_ON_CHANGE = 18;

	/** The number of inputs in the test view. */
	private static final int INPUT_COUNT = 19;

	/** The enumeration the choice inputs of the test view choose from, part of the application model. */
	private static final String CHANGE_TYPE = "tl.changelog:ChangeType";

	/** The channel the choice inputs of the test view write to. */
	private static final String CHANGE = "change";

	/** The channel the text inputs of the test view write to. */
	private static final String TERM = "term";

	/** The channel the checkboxes of the test view write to. */
	private static final String FLAG = "flag";

	/** The value a test enters in a text input. */
	private static final String ENTERED = "DEMO-1";

	/** The icon a search box carries, as the test view states it. */
	private static final String SEARCH_ICON = "css:fa-solid fa-magnifying-glass";

	private TLModelImpl _model;

	private TLModule _module;

	private TLClass _row;

	private ReactContext _context;

	private ViewContext _viewContext;

	/** The channel the text inputs of the test view write to. */
	private ViewChannel _term;

	/** The channel the checkboxes of the test view write to. */
	private ViewChannel _flag;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_model = new TLModelImpl();
		_module = TLModelUtil.addModule(_model, MODULE);
		_row = _model.addClass(_module, _module, "Row");
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_viewContext = new DefaultViewContext(new HeadlessReactContext());
		_term = new DefaultViewChannel(TERM);
		_viewContext.registerChannel(TERM, _term);
		_flag = new DefaultViewChannel(FLAG);
		_viewContext.registerChannel(FLAG, _flag);
		_viewContext.registerChannel(CHANGE, new DefaultViewChannel(CHANGE));
		RecordingCommand.EXECUTED.clear();
	}

	/**
	 * Runs the test in an interaction: an input resolves the type of its value in the application
	 * model, which reads the knowledge base.
	 */
	@Override
	protected void runTest() throws Throwable {
		ThreadContext.<Void, Throwable, RuntimeException> inSystemContext(TestValueInputElement.class,
			() -> {
				super.runTest();
				return null;
			});
	}

	@Override
	protected void tearDown() throws Exception {
		RecordingCommand.EXECUTED.clear();
		_flag = null;
		_term = null;
		_viewContext = null;
		_context = null;
		_row = null;
		_module = null;
		_model = null;

		super.tearDown();
	}

	/**
	 * The configuration of an {@code <value-input>}: the channel, the type, and the properties describing
	 * how the value is entered.
	 */
	public void testParseInputs() throws Exception {
		List<UIElement.Config> inputs = parseInputs();
		assertEquals("Every input of the test view must be parsed.", INPUT_COUNT, inputs.size());

		ValueInputElement.Config text = config(inputs, 0);
		assertEquals("term", text.getValue().getChannelName());
		assertEquals("Without a type, a value is a text.",
			ValueInputElement.DEFAULT_TYPE, text.getType().qualifiedName());
		assertFalse(text.getMultiple());
		assertFalse(text.getReadonly());
		assertNull(text.getOptions());
		assertEquals(Collections.emptyList(), text.getInputs());
		assertNotNull("A stated placeholder must reach the configuration.", text.getPlaceholder());
		assertEquals("A stated icon must reach the configuration.", SEARCH_ICON, text.getIcon());
		assertTrue("A search box states that it can be emptied.", text.getClearable());
		assertEquals("A duration is read as milliseconds.", Long.valueOf(500), text.getDebounce());
		assertTrue("Without a command named, the submit hook is a generic command: " + text.getOnSubmit(),
			text.getOnSubmit() instanceof GenericViewCommand.Config);
		assertEquals("The actions to run on the submitted value stand inside the element.",
			2, ((GenericViewCommand.Config) text.getOnSubmit()).getActions().size());
		assertEquals("Without a word, a typed value runs the command on Enter.", SubmitOn.ENTER, text.getSubmitOn());
		assertEquals("A stated submit gesture must reach the configuration.",
			SubmitOn.CHANGE, config(inputs, TEXT_ON_CHANGE).getSubmitOn());
		assertEquals(SubmitOn.ENTER, config(inputs, TEXT_ON_ENTER).getSubmitOn());

		assertEquals("tl.core:Boolean", config(inputs, 1).getType().qualifiedName());
		assertEquals("tl.core:Date", config(inputs, 2).getType().qualifiedName());

		ValueInputElement.Config state = config(inputs, 3);
		assertEquals(MODULE + ":Status", state.getType().qualifiedName());
		assertTrue("The state is displayed but not entered here.", state.getReadonly());
		assertNotNull("A stated label must reach the configuration.", state.getLabel());
		assertNotNull("A stated label position must reach the configuration.", state.getLabelPosition());

		assertNull("An input without the hook submits nothing.", config(inputs, 1).getOnSubmit());
		assertNull("An input that states no placeholder must have none.", config(inputs, 1).getPlaceholder());
		assertNull("An input that states no icon must have none.", config(inputs, 1).getIcon());
		assertFalse("An input is emptied by deleting its text unless it says otherwise.",
			config(inputs, 1).getClearable());
		assertNull("An input that states no delay waits for the span its control uses by default.",
			config(inputs, 1).getDebounce());
		assertNull("An input that names no control leaves the choice to the type of its value.",
			config(inputs, 1).getInputControl());

		ValueInputElement.Config owners = config(inputs, 4);
		assertTrue("Several owners are chosen at once.", owners.getMultiple());
		assertNotNull("The options expression must reach the configuration.", owners.getOptions());
		assertEquals("The options depend on one channel.", 1, owners.getInputs().size());
		assertEquals("project", owners.getInputs().get(0).getChannel().getChannelName());
	}

	/** A text is written in a text input, on a channel as in an attribute. */
	public void testTextIsWrittenInATextInput() {
		assertEnteredLike(datatype("Text", Kind.STRING, String.class), String.class, ReactTextInputControl.class);
	}

	/** A boolean is ticked in a checkbox, on a channel as in an attribute. */
	public void testBooleanIsTickedInACheckbox() {
		assertEnteredLike(datatype("Flag", Kind.BOOLEAN, Boolean.class), Boolean.class, ReactCheckboxControl.class);
	}

	/** A point in time is picked in a date picker, on a channel as in an attribute. */
	public void testDateIsPickedInADatePicker() {
		assertEnteredLike(datatype("Day", Kind.DATE, Date.class), Date.class, ReactDatePickerControl.class);
	}

	/** A time of day shown in a format writing seconds is edited with seconds. */
	public void testATimeShownWithSecondsIsEditedInSeconds() {
		assertEquals("A format writing seconds must give a field editing seconds.",
			Precision.SECONDS, timePicker(timeOfDay("TimeSeconds"), timeFormat(DateStyle.MEDIUM)).getPrecision());
	}

	/** A time of day shown in a format without seconds is edited in minutes. */
	public void testATimeShownWithoutSecondsIsEditedInMinutes() {
		assertEquals("A format writing no seconds must give a field editing minutes.",
			Precision.MINUTES, timePicker(timeOfDay("TimeMinutes"), timeFormat(DateStyle.SHORT)).getPrecision());
	}

	/**
	 * A time of day without a format of its own is edited in the precision of the format its type
	 * carries.
	 *
	 * <p>
	 * The attribute settings of the type
	 * ({@link com.top_logic.element.meta.AttributeSettings#getConfiguredTypeAnnotation(Class, TLType)},
	 * reached through {@link TLType#getAnnotation(Class)}) give a datatype of {@link ConfigType}
	 * {@value #CONFIG_TYPE_TIME} the {@code short-time} format, which writes no seconds, so a value
	 * without a {@link Format} of its own is edited in minutes.
	 * </p>
	 */
	public void testATimeWithoutFormatFollowsTheFormatOfItsType() throws ConfigurationException {
		TLPrimitive type = timeOfDay("TimeDefault");
		Format typeFormat = type.getAnnotation(Format.class);
		assertNotNull("The attribute settings must give a time of day a format.", typeFormat);
		Precision expected = ReactDatePickerControl.precision(ReactDatePickerControl.Kind.TIME,
			(DateFormat) DisplayAnnotations.toFormat(typeFormat));

		assertEquals("The format of the type must decide the precision.", Precision.MINUTES, expected);
		assertEquals(expected, timePicker(type, null).getPrecision());
	}

	/**
	 * A datatype of time-of-day values, marked as such by its {@link ConfigType}.
	 *
	 * @param name
	 *        The name of the datatype within the test module.
	 */
	private TLPrimitive timeOfDay(String name) {
		TLPrimitive type = datatype(name, Kind.DATE, Date.class);
		ConfigType configType = TypedConfiguration.newConfigItem(ConfigType.class);
		configType.setValue(CONFIG_TYPE_TIME);
		type.setAnnotation(configType);
		return type;
	}

	/**
	 * The picker a value of the given time-of-day type is entered in on a channel, the value
	 * declared with the given display format, or with none if it is {@code null}.
	 */
	private ReactDatePickerControl timePicker(TLPrimitive type, Format format) {
		ValueInputElement.Config declared = TypedConfiguration.newConfigItem(ValueInputElement.Config.class);
		if (format != null) {
			declared.getAnnotations().add(format);
		}
		AbstractFieldModel field = new AbstractFieldModel(null);
		FieldSpec spec = FieldControlService.fieldSpec(type, declared, LABEL, false, field);
		assertEquals(ReactDatePickerControl.Kind.TIME, spec.getDateKind());

		ReactControl control = FieldControlRegistry.getInstance().createControl(_context, spec, field);
		assertEquals(ReactDatePickerControl.class, control.getClass());
		return (ReactDatePickerControl) control;
	}

	/** A {@link Format} annotation showing a time of day in the given style. */
	private static Format timeFormat(DateStyle style) {
		TimeFormatDefinition.Config definition = TypedConfiguration.newConfigItem(TimeFormatDefinition.Config.class);
		definition.update(definition.descriptor().getProperty(TIME_STYLE), style);
		Format format = TypedConfiguration.newConfigItem(Format.class);
		format.update(format.descriptor().getProperty(FormatBase.DEFINITION), definition);
		return format;
	}

	/**
	 * A value of an enumeration is chosen from the classifiers of that enumeration, which is what
	 * an attribute of the same type offers as well.
	 *
	 * <p>
	 * The options make the field model a
	 * {@link com.top_logic.layout.form.model.SelectFieldModel}, which is what
	 * {@link FieldControlService#createFieldControl(ReactContext, TLType, FieldSpec, com.top_logic.layout.form.model.FieldModel)}
	 * answers with the select control for.
	 * </p>
	 */
	public void testEnumerationIsChosenFromItsClassifiers() {
		TLEnumeration status = _model.addEnumeration(_module, _module, "Status");
		TLClassifier open = TLModelUtil.addClassifier(status, "open");
		TLClassifier closed = TLModelUtil.addClassifier(status, "closed");

		assertEquals("The classifiers of the enumeration are the options.",
			List.of(open, closed), AttributeOptions.optionsFor(status));
	}

	/** A value of a primitive type is entered, so nothing offers it options. */
	public void testPrimitiveValueHasNoOptions() {
		assertNull("A text is written, not chosen.",
			AttributeOptions.optionsFor(datatype("Text", Kind.STRING, String.class)));
	}

	/** The input shows what the channel already holds when it appears. */
	public void testFieldStartsWithWhatTheChannelHolds() {
		ViewChannel channel = new DefaultViewChannel("value");
		channel.set("Hello");
		AbstractFieldModel field = new AbstractFieldModel(null);

		ChannelFieldBinding.bind(channel, field, false, false);

		assertEquals("Hello", field.getValue());
	}

	/** A value the channel receives from elsewhere appears in the input, and the other way round. */
	public void testChannelAndFieldFollowEachOther() {
		ViewChannel channel = new DefaultViewChannel("value");
		AbstractFieldModel field = new AbstractFieldModel(null);
		ChannelFieldBinding.bind(channel, field, false, false);

		channel.set("written elsewhere");
		assertEquals("A value the channel receives must appear in the input.",
			"written elsewhere", field.getValue());

		field.setValue("entered here");
		assertEquals("What the user enters must become the value of the channel.",
			"entered here", channel.get());
	}

	/** A disposed binding leaves the channel alone. */
	public void testDisposedBindingStopsFollowing() {
		ViewChannel channel = new DefaultViewChannel("value");
		AbstractFieldModel field = new AbstractFieldModel(null);
		ChannelFieldBinding binding = ChannelFieldBinding.bind(channel, field, false, false);

		binding.dispose();
		field.setValue("entered after the input went away");

		assertNull("A disposed binding must not write to the channel any more.", channel.get());
	}

	/**
	 * A single-valued selection reaches the channel as the value itself.
	 *
	 * <p>
	 * The select control represents its selection as a list whatever the value is, so without
	 * unwrapping the channel would hold a list of one - and an expression comparing the channel
	 * value to a classifier would never match.
	 * </p>
	 */
	public void testSingleSelectionIsUnwrappedForTheChannel() {
		ViewChannel channel = new DefaultViewChannel("value");
		SimpleSelectFieldModel field = new SimpleSelectFieldModel(null, List.of("a", "b"), false);
		ChannelFieldBinding.bind(channel, field, true, false);

		field.setValue(List.of("b"));
		assertEquals("b", channel.get());

		channel.set("a");
		assertEquals("A value from the channel must reach the control as its selection.",
			List.of("a"), field.getValue());
	}

	/** A multi-valued selection stays a collection on the channel. */
	public void testMultipleSelectionStaysACollection() {
		ViewChannel channel = new DefaultViewChannel("value");
		SimpleSelectFieldModel field = new SimpleSelectFieldModel(null, List.of("a", "b"), true);
		ChannelFieldBinding.bind(channel, field, true, true);

		field.setValue(List.of("a", "b"));
		assertEquals(List.of("a", "b"), channel.get());

		channel.set(List.of("b"));
		assertEquals(List.of("b"), field.getValue());
	}

	/**
	 * A field the user types in is finished by a gesture of the user, so it reports submits; in a
	 * text area Enter is part of the text, and there is no gesture left to submit with.
	 */
	public void testTypedTextHasASubmitGesture() {
		ReactTextInputControl singleLine = new ReactTextInputControl(_context, new AbstractFieldModel(null));
		assertTrue("A single-line text is submitted by the user.", singleLine.hasSubmitGesture());

		ReactTextInputControl area = new ReactTextInputControl(_context, new AbstractFieldModel(null));
		area.setMultiline(3);
		assertFalse("Enter belongs to the text of a text area.", area.hasSubmitGesture());
	}

	/** A field that is picked from has no submit gesture: every choice is already finished. */
	public void testPickedValueHasNoSubmitGesture() {
		ReactCheckboxControl checkbox = new ReactCheckboxControl(_context, new AbstractFieldModel(null), false);
		assertFalse(checkbox.hasSubmitGesture());
	}

	/**
	 * The submit a client sends stores the value in the field and reports exactly that value.
	 */
	public void testSubmitStoresAndReportsTheValue() {
		AbstractFieldModel field = new AbstractFieldModel(null);
		ReactTextInputControl control = new ReactTextInputControl(_context, field);
		AtomicReference<Object> submitted = new AtomicReference<>();
		HandlerResult refusal = HandlerResult.notExecutable(ExecutableState.NO_EXEC_NO_MODEL);
		control.setSubmitListener(value -> {
			submitted.set(value);
			return refusal;
		});

		assertEquals("The client is told to submit on Enter.",
			Boolean.TRUE, control.scriptingScalarState().get(SUBMIT_ON_ENTER));

		HandlerResult result =
			control.executeCommand(ReactFormFieldControl.SUBMIT_COMMAND, Map.of("value", "DEMO-1"));

		assertEquals("The submitted value reaches the field.", "DEMO-1", field.getValue());
		assertEquals("...and is reported as submitted.", "DEMO-1", submitted.get());
		assertSame("What the submit triggered is the result of the submit.", refusal, result);
	}

	/** Without a listener nothing is reported, and the client is not asked to submit at all. */
	public void testWithoutASubmitListenerNothingIsReported() {
		AbstractFieldModel field = new AbstractFieldModel(null);
		ReactTextInputControl control = new ReactTextInputControl(_context, field);

		assertNull("An input without the hook must not send submits.",
			control.scriptingScalarState().get(SUBMIT_ON_ENTER));

		control.executeCommand(ReactFormFieldControl.SUBMIT_COMMAND, Map.of("value", "DEMO-1"));

		assertEquals("The value is still stored.", "DEMO-1", field.getValue());
	}

	/**
	 * A value the user picks is reported once it has reached the channel, since a choice is
	 * finished in itself.
	 */
	public void testPickedValueIsReportedOnCommit() {
		ViewChannel channel = new DefaultViewChannel("value");
		SimpleSelectFieldModel field = new SimpleSelectFieldModel(null, List.of("a", "b"), false);
		ChannelFieldBinding binding = ChannelFieldBinding.bind(channel, field, true, false);
		AtomicReference<Object> committed = new AtomicReference<>();
		binding.setCommitListener(committed::set);

		field.setValue(List.of("b"));

		assertEquals("The choice reaches the channel.", "b", channel.get());
		assertEquals("...and is reported as committed.", "b", committed.get());
	}

	/** A value the channel receives from elsewhere is nothing the user committed. */
	public void testChannelValueIsNoCommit() {
		ViewChannel channel = new DefaultViewChannel("value");
		AbstractFieldModel field = new AbstractFieldModel(null);
		ChannelFieldBinding binding = ChannelFieldBinding.bind(channel, field, false, false);
		AtomicReference<Object> committed = new AtomicReference<>();
		binding.setCommitListener(committed::set);

		channel.set("written elsewhere");

		assertEquals("The input follows the channel.", "written elsewhere", field.getValue());
		assertNull("Nothing the user did, so nothing to run a command on.", committed.get());
	}

	/**
	 * The text stated for the empty input reaches the control that edits the value.
	 *
	 * <p>
	 * The placeholder is a property of the {@link FieldSpec}, so it is applied wherever a control
	 * is built from one - an input on a channel as well as a field of an attribute - instead of by
	 * the control or its caller.
	 * </p>
	 */
	public void testPlaceholderReachesTheInput() {
		AbstractFieldModel field = new AbstractFieldModel(null);
		TLType type = datatype("Text", Kind.STRING, String.class);
		FieldSpec spec = FieldControlService.fieldSpec(type, type, LABEL, false, field)
			.setPlaceholder("Search");

		ReactControl control = FieldControlRegistry.getInstance().createControl(_context, spec, field);

		assertEquals("The text shown in the empty input must reach the client.",
			"Search", control.scriptingScalarState().get(PLACEHOLDER));
	}

	/** Without a placeholder the input says nothing, so the client is told about none. */
	public void testWithoutAPlaceholderTheInputShowsNone() {
		AbstractFieldModel field = new AbstractFieldModel(null);
		TLType type = datatype("Plain", Kind.STRING, String.class);
		FieldSpec spec = FieldControlService.fieldSpec(type, type, LABEL, false, field);

		ReactControl control = FieldControlRegistry.getInstance().createControl(_context, spec, field);

		assertNull("An input without a placeholder must leave its text unset.",
			control.scriptingScalarState().get(PLACEHOLDER));
	}

	/**
	 * What turns a plain input into a search field - the icon it carries, the button that empties
	 * it, and how long it waits before reporting - reaches the control that edits the value.
	 *
	 * <p>
	 * Each of the three is a property of the {@link FieldSpec}, so they are applied wherever a
	 * control is built from one instead of by the control or its caller.
	 * </p>
	 */
	public void testSearchFieldPropertiesReachTheInput() throws IOException {
		AbstractFieldModel field = new AbstractFieldModel(null);

		String state = clientState(searchSpec(field), field);

		assertTrue("The icon must reach the client: " + state,
			state.contains(key(ICON) + "\"" + SEARCH_ICON + "\""));
		assertTrue("The clear button must reach the client: " + state,
			state.contains(key(CLEARABLE) + "true"));
		assertTrue("The delay must reach the client: " + state,
			state.contains(key(DEBOUNCE_MS) + "250"));
	}

	/**
	 * An input that asks for none of the three leaves them unset, so that the client keeps its own
	 * plain input and its own delay.
	 */
	public void testAPlainInputCarriesNoSearchProperties() throws IOException {
		AbstractFieldModel field = new AbstractFieldModel(null);
		TLType type = datatype("Plain", Kind.STRING, String.class);

		String state = clientState(FieldControlService.fieldSpec(type, type, LABEL, false, field), field);

		assertFalse("An input without an icon must not send one: " + state, state.contains(key(ICON)));
		assertFalse("An input emptied by deleting its text must not send a button: " + state,
			state.contains(key(CLEARABLE)));
		assertFalse("An input without a stated delay must leave the span to the client: " + state,
			state.contains(key(DEBOUNCE_MS)));
	}

	/**
	 * The three say how the input looks and how fast it reports, not what it holds, so an
	 * observation of the field leaves them out - while the placeholder, being the text a
	 * label-less input names itself by, stays.
	 */
	public void testSearchFieldPropertiesAreRenderingOnly() {
		AbstractFieldModel field = new AbstractFieldModel(null);

		Map<String, Object> projection =
			FieldControlRegistry.getInstance().createControl(_context, searchSpec(field), field)
				.scriptingScalarState();

		assertFalse("The icon is rendering-only.", projection.containsKey(ICON));
		assertFalse("The clear button is rendering-only.", projection.containsKey(CLEARABLE));
		assertFalse("The delay is rendering-only.", projection.containsKey(DEBOUNCE_MS));
		assertEquals("The text a label-less input names itself by stays observable.",
			"Search", projection.get(PLACEHOLDER));
	}

	/**
	 * A text input running its command on every finished edit asks the client for the commits of
	 * its edits, not for submits on Enter.
	 */
	public void testATextSubmittedOnChangeAsksForCommits() throws Exception {
		Map<String, Object> state = input(TEXT_ON_CHANGE).scriptingScalarState();

		assertEquals("The client must report each finished edit.",
			Boolean.TRUE, state.get(TypingFieldState.COMMIT_EDITS__PROP));
		assertFalse("Enter is no gesture of its own here, it finishes an edit like leaving the input does.",
			Boolean.TRUE.equals(state.get(FieldState.SUBMIT_ON_ENTER__PROP)));
	}

	/**
	 * A finished edit of a text input running its command on every finished edit runs the command
	 * over the entered value, which has reached the channel before, and what the command returns is
	 * the result of the commit.
	 */
	public void testAFinishedEditRunsTheCommand() throws Exception {
		ReactFormFieldControl control = input(TEXT_ON_CHANGE);

		control.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, Map.of("value", ENTERED));
		assertEquals("The entered value reaches the channel while the edit is still going on.",
			ENTERED, _term.get());
		assertEquals("A value still being edited runs no command.", List.of(), RecordingCommand.EXECUTED);

		HandlerResult result = control.executeCommand(ReactFormFieldControl.COMMIT_COMMAND, Map.of());

		assertEquals("The finished edit runs the command over the entered value.",
			List.of(ENTERED), RecordingCommand.EXECUTED);
		assertSame("What the command returns is the result of the commit.", RecordingCommand.RESULT, result);
	}

	/**
	 * Enter in a text input running its command on every finished edit is no submit: the client
	 * reports it as the commit of the edit, and a submit on its own stores the value only.
	 */
	public void testASubmitRunsNoCommandOnChange() throws Exception {
		ReactFormFieldControl control = input(TEXT_ON_CHANGE);

		control.executeCommand(ReactFormFieldControl.SUBMIT_COMMAND, Map.of("value", ENTERED));

		assertEquals("The value is stored.", ENTERED, _term.get());
		assertEquals("...but no command runs on a submit.", List.of(), RecordingCommand.EXECUTED);
	}

	/**
	 * A text input running its command on Enter asks the client for submits, not for the commits
	 * of its edits.
	 */
	public void testATextSubmittedOnEnterAsksForSubmits() throws Exception {
		Map<String, Object> state = input(TEXT_ON_ENTER).scriptingScalarState();

		assertEquals("The client must submit on Enter.",
			Boolean.TRUE, state.get(FieldState.SUBMIT_ON_ENTER__PROP));
		assertFalse("Leaving the input after an edit runs nothing, so the client need not report it.",
			Boolean.TRUE.equals(state.get(TypingFieldState.COMMIT_EDITS__PROP)));
	}

	/**
	 * In a text input running its command on Enter, the submit runs the command and a finished
	 * edit does not.
	 */
	public void testOnlyEnterRunsTheCommandOnEnter() throws Exception {
		ReactFormFieldControl control = input(TEXT_ON_ENTER);

		control.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, Map.of("value", "typed"));
		control.executeCommand(ReactFormFieldControl.COMMIT_COMMAND, Map.of());
		assertEquals("The typed value reaches the channel when the input is left.",
			"typed", _term.get());
		assertEquals("...but leaving the input runs no command.", List.of(), RecordingCommand.EXECUTED);

		HandlerResult result =
			control.executeCommand(ReactFormFieldControl.SUBMIT_COMMAND, Map.of("value", ENTERED));

		assertEquals("Enter runs the command over the submitted value.", List.of(ENTERED), RecordingCommand.EXECUTED);
		assertSame("What the command returns is the result of the submit.", RecordingCommand.RESULT, result);
	}

	/**
	 * A picked value runs the command with every choice, whatever submit gesture the input states.
	 */
	public void testAPickedValueIgnoresTheSubmitGesture() throws Exception {
		for (int index : new int[] { FLAG_ON_CHANGE, FLAG_ON_ENTER }) {
			_flag.set(null);
			RecordingCommand.EXECUTED.clear();
			ReactFormFieldControl control = input(index);

			Map<String, Object> state = control.scriptingScalarState();
			assertFalse("A choice is no edit to report: " + index,
				Boolean.TRUE.equals(state.get(TypingFieldState.COMMIT_EDITS__PROP)));
			assertFalse("A choice has no Enter to submit with: " + index,
				Boolean.TRUE.equals(state.get(FieldState.SUBMIT_ON_ENTER__PROP)));

			control.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, Map.of("value", Boolean.TRUE));

			assertEquals("The choice reaches the channel: " + index,
				Boolean.TRUE, _flag.get());
			assertEquals("...and runs the command: " + index, List.of(Boolean.TRUE), RecordingCommand.EXECUTED);
		}
	}

	/**
	 * An input declares its value in the words of an attribute: whether it is mandatory, and the
	 * annotations shaping its display.
	 */
	public void testParseValueDeclaration() throws Exception {
		List<UIElement.Config> inputs = parseInputs();

		assertTrue("A stated mandatory flag must reach the configuration.",
			config(inputs, MANDATORY_CHOICE).getMandatory());
		assertFalse("A value may be empty unless stated otherwise.",
			config(inputs, OPTIONAL_CHOICE).getMandatory());

		ClassificationDisplay radio = config(inputs, RADIO_CHOICE).getAnnotation(ClassificationDisplay.class);
		assertNotNull("A stated display annotation must reach the configuration.", radio);
		assertEquals(ClassificationPresentation.RADIO, radio.getValue());
		assertNull("An input without annotations states none.",
			config(inputs, OPTIONAL_CHOICE).getAnnotation(ClassificationDisplay.class));

		TLInputControl annotated = config(inputs, ANNOTATED_CONTROL).getAnnotation(TLInputControl.class);
		assertNotNull("A stated control annotation must reach the configuration.", annotated);
		assertTrue("The annotation names a select control: " + annotated.getImpl(),
			annotated.getImpl() instanceof SelectControlProvider.Config);
		assertEquals(SelectDisplay.SEGMENTED, ((SelectControlProvider.Config) annotated.getImpl()).getDisplay());
		assertNull("An annotated control is no control named by the input.",
			config(inputs, ANNOTATED_CONTROL).getInputControl());
	}

	/**
	 * A mandatory choice tells its select control and its label that it may not be empty, which
	 * is what makes the client offer no empty choice and mark the label as required.
	 */
	public void testAMandatoryChoiceIsMarkedRequired() throws Exception {
		ReactFormFieldChromeControl chrome = chrome(MANDATORY_CHOICE);
		assertEquals("The label of a mandatory value is marked as required.",
			Boolean.TRUE, chrome.scriptingScalarState().get(FormFieldState.REQUIRED__PROP));

		ReactFormFieldControl field = field(chrome);
		assertTrue("The field model of a mandatory value is mandatory.", field.getFieldModel().isMandatory());
		assertEquals("The client must learn that the selection may not be emptied.",
			Boolean.TRUE, field.scriptingScalarState().get(FieldState.MANDATORY__PROP));
	}

	/** A choice that does not say it is mandatory may be emptied. */
	public void testAnOptionalChoiceIsNotRequired() throws Exception {
		ReactFormFieldChromeControl chrome = chrome(OPTIONAL_CHOICE);
		assertFalse("The label of an optional value is not marked as required.",
			Boolean.TRUE.equals(chrome.scriptingScalarState().get(FormFieldState.REQUIRED__PROP)));

		ReactFormFieldControl field = field(chrome);
		assertFalse(field.getFieldModel().isMandatory());
		assertFalse("The client must offer the empty choice.",
			Boolean.TRUE.equals(field.scriptingScalarState().get(FieldState.MANDATORY__PROP)));
	}

	/**
	 * A choice annotated to be displayed as radio buttons is displayed exactly as a field of an
	 * attribute carrying the same annotation; without the annotation, it stays a list that opens
	 * on demand.
	 */
	public void testAnAnnotatedChoiceIsDisplayedLikeTheAttribute() throws Exception {
		ReactDropdownSelectControl input = select(input(RADIO_CHOICE));

		TLType type = TLModelUtil.findType(CHANGE_TYPE);
		TLStructuredTypePart part = _model.addClassProperty(_row, "radio", type);
		part.setAnnotation(ClassificationDisplay.display(ClassificationPresentation.RADIO));
		SimpleSelectFieldModel field = new SimpleSelectFieldModel(null, AttributeOptions.optionsFor(type), false);
		ReactDropdownSelectControl attribute =
			select(FieldControlService.getInstance().createFieldControl(_context, part, field));

		assertEquals(SelectDisplay.RADIO, input.getDisplay());
		assertEquals("The input is displayed as the attribute is.", attribute.getDisplay(), input.getDisplay());
		assertEquals(attribute.getOrientation(), input.getOrientation());
		assertEquals(attribute.getClass(), input.getClass());

		assertEquals("Without an annotation, the options are offered in a list that opens on demand.",
			SelectDisplay.DROPDOWN, select(field(chrome(OPTIONAL_CHOICE))).getDisplay());
	}

	/**
	 * An annotation the input does not state is taken from the type; one it states wins over the
	 * type.
	 */
	public void testTheDeclarationFallsBackToTheType() {
		TLEnumeration status = enumeration("Fallback");
		status.setAnnotation(ClassificationDisplay.display(ClassificationPresentation.RADIO_INLINE));
		SimpleSelectFieldModel field = choice(status, false);
		ValueInputElement.Config declared = TypedConfiguration.newConfigItem(ValueInputElement.Config.class);

		FieldSpec fromType = FieldControlService.fieldSpec(status, declared, LABEL, false, field);
		assertEquals("The input says nothing, so the type decides.", SelectDisplay.RADIO, fromType.getSelectDisplay());
		assertEquals(Orientation.HORIZONTAL, fromType.getSelectOrientation());

		declared.getAnnotations().add(ClassificationDisplay.display(ClassificationPresentation.DROP_DOWN));
		FieldSpec fromInput = FieldControlService.fieldSpec(status, declared, LABEL, false, field);
		assertEquals("The annotation of the input wins over the one of the type.",
			SelectDisplay.DROPDOWN, fromInput.getSelectDisplay());
	}

	/**
	 * A control annotation of the input selects the control; a control the input names itself wins
	 * over the annotation.
	 */
	public void testAControlAnnotationOfTheInputSelectsTheControl() throws Exception {
		assertEquals("The annotated control edits the value.",
			SelectDisplay.SEGMENTED, select(input(ANNOTATED_CONTROL)).getDisplay());
		assertEquals("The control named by the input wins over the annotated one.",
			SelectDisplay.CHIPS, select(input(NAMED_OVER_ANNOTATED)).getDisplay());
	}

	/**
	 * The control annotation is resolved from the declaration of the value, falling back to the
	 * type, behind a control named by the display - the order the resolution of an attribute
	 * follows.
	 */
	public void testTheControlAnnotationIsResolvedInOrder() {
		TLEnumeration status = enumeration("Controlled");
		SimpleSelectFieldModel field = choice(status, false);
		ValueInputElement.Config declared = TypedConfiguration.newConfigItem(ValueInputElement.Config.class);
		FieldSpec spec = FieldControlService.fieldSpec(status, declared, LABEL, false, field);
		FieldControlService service = controlService();

		assertEquals("Without an annotation, the type leads to a list that opens on demand.",
			SelectDisplay.DROPDOWN,
			select(service.createFieldControl(_context, status, declared, spec, field, null)).getDisplay());

		status.setAnnotation(inputControl(SelectDisplay.CHIPS));
		assertEquals("The control annotated at the type applies where the input states none.",
			SelectDisplay.CHIPS,
			select(service.createFieldControl(_context, status, declared, spec, field, null)).getDisplay());

		declared.getAnnotations().add(inputControl(SelectDisplay.SEGMENTED));
		assertEquals("The control annotated at the input wins over the type.",
			SelectDisplay.SEGMENTED,
			select(service.createFieldControl(_context, status, declared, spec, field, null)).getDisplay());

		assertEquals("A control named by the display wins over every annotation.",
			SelectDisplay.RADIO,
			select(service.createFieldControl(_context, status, declared, spec, field,
				selectDisplay(SelectDisplay.RADIO))).getDisplay());
	}

	/**
	 * A mandatory text input that is submitted empty reports the missing value and runs no
	 * command, while the empty value still reaches the channel; a given value runs the command.
	 */
	public void testAMandatoryTextRefusesAnEmptySubmit() throws Exception {
		ReactFormFieldControl control = input(MANDATORY_TEXT_ON_ENTER);
		AbstractFieldModel field = (AbstractFieldModel) control.getFieldModel();

		assertTrue(field.isMandatory());
		assertFalse("An input that appears empty does not complain before it is touched.", field.hasError());
		assertHasError(control, false);

		HandlerResult empty = control.executeCommand(ReactFormFieldControl.SUBMIT_COMMAND, Map.of("value", ""));

		assertNull("The empty value reaches the channel like any other, as no value.", _term.get());
		assertMissing(field);
		assertHasError(control, true);
		assertTrue("An empty mandatory value is reported at the field alone; the submit itself succeeds.",
			empty.isSuccess());
		assertEquals("No command runs on an empty mandatory value.", List.of(), RecordingCommand.EXECUTED);

		HandlerResult result =
			control.executeCommand(ReactFormFieldControl.SUBMIT_COMMAND, Map.of("value", ENTERED));

		assertFalse("A given value is no longer missing.", field.hasError());
		assertHasError(control, false);
		assertEquals("A given value runs the command.", List.of(ENTERED), RecordingCommand.EXECUTED);
		assertSame(RecordingCommand.RESULT, result);
	}

	/**
	 * A mandatory text input that is emptied while editing reports the missing value at once, and
	 * finishing the edit runs no command.
	 */
	public void testAMandatoryTextRefusesAnEmptyCommit() throws Exception {
		ReactFormFieldControl control = input(MANDATORY_TEXT_ON_CHANGE);
		AbstractFieldModel field = (AbstractFieldModel) control.getFieldModel();

		control.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, Map.of("value", ENTERED));
		assertFalse(field.hasError());
		control.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, Map.of("value", ""));

		assertNull("The emptied value reaches the channel, as no value.", _term.get());
		assertMissing(field);

		HandlerResult empty = control.executeCommand(ReactFormFieldControl.COMMIT_COMMAND, Map.of());
		assertTrue("An empty mandatory value is reported at the field alone; the commit itself succeeds.",
			empty.isSuccess());
		assertEquals("No command runs on an empty mandatory value.", List.of(), RecordingCommand.EXECUTED);

		control.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, Map.of("value", ENTERED));
		HandlerResult result = control.executeCommand(ReactFormFieldControl.COMMIT_COMMAND, Map.of());

		assertFalse(field.hasError());
		assertEquals("A given value runs the command.", List.of(ENTERED), RecordingCommand.EXECUTED);
		assertSame(RecordingCommand.RESULT, result);
	}

	/** A text input that may be empty runs its command on the empty value and reports nothing. */
	public void testAnOptionalTextSubmitsAnEmptyValue() throws Exception {
		ReactFormFieldControl control = input(TEXT_ON_ENTER);
		AbstractFieldModel field = (AbstractFieldModel) control.getFieldModel();

		HandlerResult result = control.executeCommand(ReactFormFieldControl.SUBMIT_COMMAND, Map.of("value", ""));

		assertFalse(field.isMandatory());
		assertFalse("An empty value may be given.", field.hasError());
		assertHasError(control, false);
		assertEquals("The empty value runs the command.", Collections.singletonList(null), RecordingCommand.EXECUTED);
		assertSame(RecordingCommand.RESULT, result);
	}

	/** Checks that the given field reports its value as missing. */
	private static void assertMissing(AbstractFieldModel field) {
		assertTrue("An emptied mandatory value is reported.", field.hasError());
		assertEquals(I18NConstants.ERROR_MANDATORY_FIELD_EMPTY, field.getError());
	}

	/** Checks whether the client is told about an error of the given control. */
	private static void assertHasError(ReactFormFieldControl control, boolean expected) {
		assertEquals("The client must learn whether the field has an error.",
			expected, Boolean.TRUE.equals(control.scriptingScalarState().get(FieldState.HAS_ERROR__PROP)));
	}

	/** A control annotation naming a select control offering its options in the given shape. */
	private static TLInputControl inputControl(SelectDisplay display) {
		TLInputControl annotation = TypedConfiguration.newConfigItem(TLInputControl.class);
		annotation.setImpl(selectDisplay(display));
		return annotation;
	}

	/**
	 * The input control built by the {@code <value-input>} at the given position of the test view.
	 */
	private ReactFormFieldControl input(int index) throws Exception {
		IReactControl control = element(index);
		assertTrue("An input without a label is the field control itself, but is " + control,
			control instanceof ReactFormFieldControl);
		return (ReactFormFieldControl) control;
	}

	/**
	 * The label around the input control built by the labelled {@code <value-input>} at the given
	 * position of the test view.
	 */
	private ReactFormFieldChromeControl chrome(int index) throws Exception {
		IReactControl control = element(index);
		assertTrue("An input with a label is wrapped in a chrome, but is " + control,
			control instanceof ReactFormFieldChromeControl);
		return (ReactFormFieldChromeControl) control;
	}

	/** The input control the given chrome labels. */
	private static ReactFormFieldControl field(ReactFormFieldChromeControl chrome) {
		List<ReactControl> children = chrome.displayedChildren();
		assertEquals("The chrome labels exactly one field: " + children, 1, children.size());
		assertTrue("The chrome labels a field control, but labels " + children.get(0),
			children.get(0) instanceof ReactFormFieldControl);
		return (ReactFormFieldControl) children.get(0);
	}

	/**
	 * The control built by the {@code <value-input>} at the given position of the test view.
	 */
	private IReactControl element(int index) throws Exception {
		ValueInputElement.Config config = config(parseInputs(), index);
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestValueInputElement.class);
		UIElement element = context.getInstance(config);
		context.checkErrors();

		return element.createControl(_viewContext);
	}

	/**
	 * {@link ViewCommand} recording the inputs it is run on, the submit command of the inputs of the
	 * test view testing the submit gesture.
	 */
	public static class RecordingCommand implements ViewCommand {

		/** The inputs the command was run on, in the order of the runs. */
		static final List<Object> EXECUTED = new ArrayList<>();

		/** The result of every run, distinguishable from the result of a run of no command. */
		static final HandlerResult RESULT = HandlerResult.notExecutable(ExecutableState.NO_EXEC_NO_MODEL);

		/**
		 * Configuration for {@link RecordingCommand}.
		 */
		public interface Config extends ViewCommand.Config {

			@Override
			@ClassDefault(RecordingCommand.class)
			Class<? extends ViewCommand> getImplementationClass();
		}

		/**
		 * Creates a {@link RecordingCommand} from configuration.
		 */
		@CalledByReflection
		public RecordingCommand(InstantiationContext context, Config config) {
			// Nothing to configure.
		}

		@Override
		public HandlerResult execute(ReactContext context, Object input) {
			EXECUTED.add(input);
			return RESULT;
		}
	}

	/**
	 * React context of a test that displays no persistent object, and therefore has no
	 * {@link ModelScope} to observe one in.
	 */
	private static final class HeadlessReactContext extends DefaultReactContext {

		HeadlessReactContext() {
			super("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		}

		@Override
		public ModelScope getModelScope() {
			return null;
		}
	}

	/** A text field described as a search box: an icon, a clear button and a delay of its own. */
	private FieldSpec searchSpec(AbstractFieldModel field) {
		TLType type = datatype("Term", Kind.STRING, String.class);
		return FieldControlService.fieldSpec(type, type, LABEL, false, field)
			.setPlaceholder("Search")
			.setIcon(SEARCH_ICON)
			.setClearable(true)
			.setDebounce(Long.valueOf(250));
	}

	/** The given state key as it is written into the serialized state. */
	private static String key(String name) {
		return "\"" + name + "\":";
	}

	/**
	 * The state the control built from the given description hands to the client.
	 *
	 * @implNote Read from the rendered element, into whose attribute the state is serialized, since
	 *           the map itself is visible to the control only.
	 */
	private String clientState(FieldSpec spec, AbstractFieldModel field) throws IOException {
		ReactControl control = FieldControlRegistry.getInstance().createControl(_context, spec, field);

		TagWriter out = new TagWriter();
		control.write(out);
		return out.toString().replace("&quot;", "\"");
	}

	/**
	 * An input names the control the value is entered in, in the shape a {@code <field>} names it.
	 */
	public void testParseInputControl() throws Exception {
		List<UIElement.Config> inputs = parseInputs();

		SelectControlProvider.Config chosen = inputControl(inputs, 5, SelectControlProvider.Config.class);
		assertEquals(SelectControlProvider.class, chosen.getImplementationClass());
		assertEquals("The shape the options are offered in must reach the configuration.",
			SelectDisplay.SEGMENTED, chosen.getDisplay());

		NumberInputControlProvider.Config dragged =
			inputControl(inputs, 6, NumberInputControlProvider.Config.class);
		assertEquals(NumberDisplay.SLIDER, dragged.getDisplay());
		assertEquals("The bounds of the range must reach the configuration.",
			Double.valueOf(0.0), dragged.getMin());
		assertEquals(Double.valueOf(10.0), dragged.getMax());
		assertEquals(Double.valueOf(1.0), dragged.getStep());

		BooleanControlProvider.Config flipped =
			inputControl(inputs, 7, BooleanControlProvider.Config.class);
		assertEquals(BooleanPresentation.SWITCH, flipped.getDisplay());
	}

	/**
	 * The control an input names edits the value, whatever control the type of that value would
	 * lead to.
	 */
	public void testTheNamedControlEditsTheValue() {
		TLEnumeration status = _model.addEnumeration(_module, _module, "Choice");
		TLModelUtil.addClassifier(status, "open");
		TLModelUtil.addClassifier(status, "closed");
		SimpleSelectFieldModel chosen =
			new SimpleSelectFieldModel(null, AttributeOptions.optionsFor(status), false);

		ReactControl control = entered(status, chosen, selectDisplay(SelectDisplay.SEGMENTED));

		assertTrue("A selection is made on a select control, but is made on " + control.getClass(),
			control instanceof ReactDropdownSelectControl);
		assertEquals("The named control must be built in the shape it was configured for.",
			SelectDisplay.SEGMENTED, ((ReactDropdownSelectControl) control).getDisplay());
	}

	/** Without a control named, the value is entered in the one its type leads to. */
	public void testWithoutANamedControlTheTypeDecides() {
		TLEnumeration status = _model.addEnumeration(_module, _module, "Plain");
		TLModelUtil.addClassifier(status, "open");
		SimpleSelectFieldModel chosen =
			new SimpleSelectFieldModel(null, AttributeOptions.optionsFor(status), false);

		ReactControl control = entered(status, chosen, null);

		assertEquals("A selection is offered in a list that opens on demand unless stated otherwise.",
			SelectDisplay.DROPDOWN, ((ReactDropdownSelectControl) control).getDisplay());
	}

	/**
	 * An enumeration displayed as radio buttons by its {@link ClassificationDisplay} is offered as
	 * a group of radio buttons, one below the other.
	 */
	public void testAnEnumerationAnnotatedAsRadioIsARadioGroup() {
		TLEnumeration status = enumeration("Radio");
		status.setAnnotation(ClassificationDisplay.display(ClassificationPresentation.RADIO));

		ReactDropdownSelectControl control = select(entered(status, choice(status, false), null));

		assertEquals(SelectDisplay.RADIO, control.getDisplay());
		assertEquals(Orientation.VERTICAL, control.getOrientation());
	}

	/**
	 * The annotation at an attribute wins over the one of its type: inline radio buttons stand side
	 * by side although the type asks for a list.
	 */
	public void testAnAttributeAnnotatedAsInlineRadioOverridesItsType() {
		TLEnumeration status = enumeration("Inline");
		status.setAnnotation(ClassificationDisplay.display(ClassificationPresentation.DROP_DOWN));
		TLStructuredTypePart part = _model.addClassProperty(_row, "inline", status);
		part.setAnnotation(ClassificationDisplay.display(ClassificationPresentation.RADIO_INLINE));

		SimpleSelectFieldModel field = choice(status, false);
		FieldSpec spec = FieldControlService.fieldSpec(status, part, LABEL, false, field);
		ReactDropdownSelectControl control =
			select(controlService().createFieldControl(_context, status, spec, field, null));

		assertEquals(SelectDisplay.RADIO, control.getDisplay());
		assertEquals(Orientation.HORIZONTAL, control.getOrientation());
	}

	/** A checklist is the group of buttons, a checkbox for each option of a multi-valued field. */
	public void testAChecklistIsARadioGroup() {
		TLEnumeration status = enumeration("Checklist");
		status.setAnnotation(ClassificationDisplay.display(ClassificationPresentation.CHECKLIST));

		ReactDropdownSelectControl control = select(entered(status, choice(status, true), null));

		assertEquals(SelectDisplay.RADIO, control.getDisplay());
		assertEquals(Orientation.VERTICAL, control.getOrientation());
	}

	/**
	 * A reference to objects of a class displayed as inline radio buttons by its
	 * {@link ReferenceDisplay} is offered as radio buttons side by side; a presentation without
	 * radio buttons stays a list that opens on demand.
	 */
	public void testAReferenceAnnotatedAsRadioIsARadioGroup() {
		TLClass target = _model.addClass(_module, _module, "Target");
		target.setAnnotation(ReferenceDisplay.display(ReferencePresentation.RADIO_INLINE));
		SimpleSelectFieldModel field = new SimpleSelectFieldModel(null, List.of("a", "b"), false);

		ReactDropdownSelectControl inline = select(entered(target, field, null));
		assertEquals(SelectDisplay.RADIO, inline.getDisplay());
		assertEquals(Orientation.HORIZONTAL, inline.getOrientation());

		target.setAnnotation(ReferenceDisplay.display(ReferencePresentation.TABLE));
		assertEquals(SelectDisplay.DROPDOWN, select(entered(target, field, null)).getDisplay());
	}

	/** A control named by the input overrides the display the model annotation asks for. */
	public void testTheNamedControlOverridesTheAnnotation() {
		TLEnumeration status = enumeration("Overridden");
		status.setAnnotation(ClassificationDisplay.display(ClassificationPresentation.RADIO_INLINE));
		SimpleSelectFieldModel field = choice(status, false);

		assertEquals(SelectDisplay.SEGMENTED,
			select(entered(status, field, selectDisplay(SelectDisplay.SEGMENTED))).getDisplay());
		assertEquals(SelectDisplay.DROPDOWN,
			select(entered(status, field, selectDisplay(SelectDisplay.DROPDOWN))).getDisplay());
	}

	/**
	 * A table cell offers the options in a list that opens on demand although the annotation asks
	 * for radio buttons; a display named for the control still wins.
	 */
	public void testACellIsADropdownWhateverTheAnnotationSays() {
		TLEnumeration status = enumeration("Cell");
		status.setAnnotation(ClassificationDisplay.display(ClassificationPresentation.RADIO_INLINE));
		SimpleSelectFieldModel field = choice(status, false);

		FieldSpec cell = FieldControlService.cellSpec(FieldControlService.fieldSpec(status, status, null, false, field));
		assertEquals(SelectDisplay.DROPDOWN,
			select(controlService().createFieldControl(_context, status, cell, field, null)).getDisplay());

		FieldSpec named = FieldControlService.cellSpec(FieldControlService.fieldSpec(status, status, null, false, field));
		assertEquals(SelectDisplay.RADIO,
			select(controlService().createFieldControl(_context, status, named, field,
				selectDisplay(SelectDisplay.RADIO))).getDisplay());
	}

	/** An enumeration of the given name with two classifiers. */
	private TLEnumeration enumeration(String name) {
		TLEnumeration result = _model.addEnumeration(_module, _module, name);
		TLModelUtil.addClassifier(result, "open");
		TLModelUtil.addClassifier(result, "closed");
		return result;
	}

	/** A field choosing from the classifiers of the given enumeration. */
	private static SimpleSelectFieldModel choice(TLEnumeration enumeration, boolean multiple) {
		return new SimpleSelectFieldModel(multiple ? List.of() : null, AttributeOptions.optionsFor(enumeration),
			multiple);
	}

	/** The given control as the select control a choice is made on. */
	private static ReactDropdownSelectControl select(ReactControl control) {
		assertTrue("A selection is made on a select control, but is made on " + control.getClass(),
			control instanceof ReactDropdownSelectControl);
		return (ReactDropdownSelectControl) control;
	}

	/** A number is dragged along a track where the input names a slider. */
	public void testANumberIsDraggedWhereASliderIsNamed() {
		TLType number = datatype("Amount", Kind.INT, Integer.class);
		AbstractFieldModel field = new AbstractFieldModel(null);

		assertEquals(ReactSliderControl.class, entered(number, field, sliderDisplay()).getClass());
	}

	/** A truth value is flipped on a switch where the input names one. */
	public void testATruthValueIsFlippedWhereASwitchIsNamed() {
		TLType flag = datatype("Switched", Kind.BOOLEAN, Boolean.class);
		AbstractFieldModel field = new AbstractFieldModel(null);

		ReactControl control = entered(flag, field, switchDisplay());

		assertEquals(BooleanPresentation.SWITCH, ((ReactCheckboxControl) control).getPresentation());
	}

	/**
	 * The control a value of the given type is entered in, with the given control named by the
	 * input or {@code null} to let the type decide.
	 */
	private ReactControl entered(TLType type, AbstractFieldModel field,
			PolymorphicConfiguration<? extends ReactFieldControlProvider> control) {
		FieldSpec spec = FieldControlService.fieldSpec(type, type, LABEL, false, field);
		return controlService().createFieldControl(_context, type, spec, field, control);
	}

	/** A select control offering its options in the given shape. */
	private static PolymorphicConfiguration<? extends ReactFieldControlProvider> selectDisplay(
			SelectDisplay display) {
		SelectControlProvider.Config config =
			TypedConfiguration.newConfigItem(SelectControlProvider.Config.class);
		config.update(config.descriptor().getProperty(SelectControlProvider.Config.DISPLAY), display);
		return config;
	}

	/** A number control dragging its value along a track of a small range. */
	private static PolymorphicConfiguration<? extends ReactFieldControlProvider> sliderDisplay() {
		NumberInputControlProvider.Config config =
			TypedConfiguration.newConfigItem(NumberInputControlProvider.Config.class);
		config.update(config.descriptor().getProperty(NumberInputControlProvider.Config.DISPLAY),
			NumberDisplay.SLIDER);
		config.update(config.descriptor().getProperty(NumberInputControlProvider.Config.MIN),
			Double.valueOf(0.0));
		config.update(config.descriptor().getProperty(NumberInputControlProvider.Config.MAX),
			Double.valueOf(10.0));
		return config;
	}

	/** A boolean control flipping its value on a switch. */
	private static PolymorphicConfiguration<? extends ReactFieldControlProvider> switchDisplay() {
		BooleanControlProvider.Config config =
			TypedConfiguration.newConfigItem(BooleanControlProvider.Config.class);
		config.update(config.descriptor().getProperty(BooleanControlProvider.Config.DISPLAY),
			BooleanPresentation.SWITCH);
		return config;
	}

	/**
	 * A {@link FieldControlService} resolving the controls, built without the service module: the
	 * resolution under test reads nothing the startup of the service fills.
	 */
	private static FieldControlService controlService() {
		FieldControlService.Config config = TypedConfiguration.newConfigItem(FieldControlService.Config.class);
		config.setImplementationClass(FieldControlService.class);
		return (FieldControlService) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(config);
	}

	/** The control configuration the input at the given position names. */
	private static <C extends PolymorphicConfiguration<? extends ReactFieldControlProvider>> C inputControl(
			List<UIElement.Config> inputs, int index, Class<C> expected) {
		PolymorphicConfiguration<? extends ReactFieldControlProvider> control =
			config(inputs, index).getInputControl();
		assertNotNull("Input " + index + " must name a control.", control);
		assertTrue("Input " + index + " must name a " + expected.getName() + ", but names " + control,
			expected.isInstance(control));
		return expected.cast(control);
	}

	/**
	 * Checks that a value of the given type is entered the same way on a channel as in an
	 * attribute.
	 *
	 * @param type
	 *        The type of the value.
	 * @param valueType
	 *        The Java type the values have, which decides the control.
	 * @param expected
	 *        The control the value is entered in.
	 */
	private void assertEnteredLike(TLType type, Class<?> valueType, Class<? extends ReactControl> expected) {
		TLStructuredTypePart part = _model.addClassProperty(_row, "value" + type.getName(), type);
		AbstractFieldModel field = new AbstractFieldModel(null);

		FieldSpec fromAttribute =
			FieldControlService.fieldSpec(type, part, LABEL, part.isMultiple(), field);
		FieldSpec fromType = FieldControlService.fieldSpec(type, type, LABEL, false, field);

		assertEquals("The Java type of the values comes from the model type either way.",
			valueType, fromType.getValueType());
		assertEquals(fromAttribute.getValueType(), fromType.getValueType());
		assertEquals(fromAttribute.getDateKind(), fromType.getDateKind());
		assertEquals(fromAttribute.getBooleanPresentation(), fromType.getBooleanPresentation());
		assertEquals(fromAttribute.isTriState(), fromType.isTriState());

		ReactFieldControlProvider viaType = FieldControlRegistry.getInstance().lookup(fromType.getValueType());
		ReactFieldControlProvider viaAttribute =
			FieldControlRegistry.getInstance().lookup(fromAttribute.getValueType());
		assertSame("Both sides must resolve the same control provider.", viaAttribute, viaType);

		ReactControl control = FieldControlRegistry.getInstance().createControl(_context, fromType, field);
		assertEquals(expected, control.getClass());
	}

	/**
	 * A datatype of the given kind whose values are of the given Java type.
	 *
	 * @param name
	 *        The name of the datatype within the test module.
	 * @param kind
	 *        The model kind of the datatype.
	 * @param applicationType
	 *        The Java type of the values, as the storage mapping of a datatype states it.
	 */
	private TLPrimitive datatype(String name, Kind kind, Class<?> applicationType) {
		return TLModelUtil.addDatatype(_module, _module, name, kind, valuesOf(applicationType));
	}

	/**
	 * A {@link StorageMapping} that only states the Java type of the values, which is what the
	 * control selection reads from it.
	 */
	private static <T> StorageMapping<T> valuesOf(Class<?> applicationType) {
		return new StorageMapping<>() {
			@SuppressWarnings("unchecked")
			@Override
			public Class<T> getApplicationType() {
				return (Class<T>) applicationType;
			}

			@SuppressWarnings("unchecked")
			@Override
			public T getBusinessObject(Object storageObject) {
				return (T) storageObject;
			}

			@Override
			public Object getStorageObject(Object businessObject) {
				return businessObject;
			}

			@Override
			public boolean isCompatible(Object businessObject) {
				return businessObject == null || applicationType.isInstance(businessObject);
			}
		};
	}

	/** The {@code <value-input>} configurations of the test view. */
	private List<UIElement.Config> parseInputs() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestValueInputElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestValueInputElement.class, "test-input.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		assertTrue("The inputs are held by a panel.", config.getContent() instanceof PanelElement.Config);
		return ((PanelElement.Config) config.getContent()).getChildren();
	}

	/** The {@code <value-input>} configuration at the given position. */
	private static ValueInputElement.Config config(List<UIElement.Config> inputs, int index) {
		PolymorphicConfiguration<? extends UIElement> entry = inputs.get(index);
		assertTrue("Entry " + index + " must be an input, but is " + entry, entry instanceof ValueInputElement.Config);
		return (ValueInputElement.Config) entry;
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module for resolving the element tags, the services
	 * a model part consults while answering for an annotation, and the model and the
	 * {@link FieldControlService} an input resolves its type and its control with.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestValueInputElement.class,
			ServiceTestSetup.createStarterFactoryForModules(
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				CompatibilityService.Module.INSTANCE, AttributeSettings.Module.INSTANCE,
				ModelService.Module.INSTANCE, FieldControlService.Module.INSTANCE));
	}
}
