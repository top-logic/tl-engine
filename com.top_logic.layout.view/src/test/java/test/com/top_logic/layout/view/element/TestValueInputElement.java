/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.field.FieldControlRegistry;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.element.ValueInputElement;
import com.top_logic.layout.view.element.PanelElement;
import com.top_logic.layout.view.form.AttributeOptions;
import com.top_logic.layout.view.form.ChannelFieldBinding;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.CompatibilityService;

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
 * committed.
 * </p>
 *
 * <p>
 * The model is transient, so the test needs neither a knowledge base nor the application model.
 * </p>
 */
public class TestValueInputElement extends TestCase {

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

	private TLModelImpl _model;

	private TLModule _module;

	private TLClass _row;

	private ReactContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_model = new TLModelImpl();
		_module = TLModelUtil.addModule(_model, MODULE);
		_row = _model.addClass(_module, _module, "Row");
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue());
	}

	@Override
	protected void tearDown() throws Exception {
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
		List<PolymorphicConfiguration<? extends UIElement>> inputs = parseInputs();
		assertEquals("Every input of the test view must be parsed.", 5, inputs.size());

		ValueInputElement.Config text = config(inputs, 0);
		assertEquals("term", text.getValue().getChannelName());
		assertEquals("Without a type, a value is a text.",
			ValueInputElement.DEFAULT_TYPE, text.getType().qualifiedName());
		assertFalse(text.getMultiple());
		assertFalse(text.getReadonly());
		assertNull(text.getOptions());
		assertEquals(Collections.emptyList(), text.getInputs());
		assertTrue("Without a command named, the submit hook is a generic command: " + text.getOnSubmit(),
			text.getOnSubmit() instanceof GenericViewCommand.Config);
		assertEquals("The actions to run on the submitted value stand inside the element.",
			2, ((GenericViewCommand.Config) text.getOnSubmit()).getActions().size());

		assertEquals("tl.core:Boolean", config(inputs, 1).getType().qualifiedName());
		assertEquals("tl.core:Date", config(inputs, 2).getType().qualifiedName());

		ValueInputElement.Config state = config(inputs, 3);
		assertEquals(MODULE + ":Status", state.getType().qualifiedName());
		assertTrue("The state is displayed but not entered here.", state.getReadonly());
		assertNotNull("A stated label must reach the configuration.", state.getLabel());
		assertNotNull("A stated label position must reach the configuration.", state.getLabelPosition());

		assertNull("An input without the hook submits nothing.", config(inputs, 1).getOnSubmit());

		ValueInputElement.Config owners = config(inputs, 4);
		assertTrue("Several owners are chosen at once.", owners.getMultiple());
		assertNotNull("The options expression must reach the configuration.", owners.getOptions());
		assertEquals("The options depend on one channel.", 1, owners.getInputs().size());
		assertEquals("project", owners.getInputs().get(0).getChannelName());
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
		control.setSubmitListener(submitted::set);

		assertEquals("The client is told to submit on Enter.",
			Boolean.TRUE, control.scriptingScalarState().get(SUBMIT_ON_ENTER));

		control.executeCommand(ReactFormFieldControl.SUBMIT_COMMAND, Map.of("value", "DEMO-1"));

		assertEquals("The submitted value reaches the field.", "DEMO-1", field.getValue());
		assertEquals("...and is reported as submitted.", "DEMO-1", submitted.get());
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
		return new StorageMapping<T>() {
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
	private List<PolymorphicConfiguration<? extends UIElement>> parseInputs() throws Exception {
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
	private static ValueInputElement.Config config(List<PolymorphicConfiguration<? extends UIElement>> inputs, int index) {
		PolymorphicConfiguration<? extends UIElement> entry = inputs.get(index);
		assertTrue("Entry " + index + " must be an input, but is " + entry, entry instanceof ValueInputElement.Config);
		return (ValueInputElement.Config) entry;
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module for resolving the element tags, and the
	 * services a model part consults while answering for an annotation.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestValueInputElement.class,
				TypeIndex.Module.INSTANCE, CompatibilityService.Module.INSTANCE,
				AttributeSettings.Module.INSTANCE));
	}
}
