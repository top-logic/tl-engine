/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.layout.LabelPosition;
import com.top_logic.layout.react.control.layout.ReactFormLayoutControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.element.FieldElement;
import com.top_logic.layout.view.element.FormElement;
import com.top_logic.layout.view.element.PanelElement;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.PasswordInputControlProvider;
import com.top_logic.model.listen.ModelScope;

/**
 * Tests parsing and instantiation of {@link FormElement} and {@link FieldElement}.
 */
public class TestFormElement extends TestCase {

	/** The channel the forms of the layout test view display their object from. */
	private static final String DISPLAYED_OBJECT = "selectedItem";

	/** The {@code <form>} saying nothing about its grid. */
	private static final int DEFAULT_GRID = 0;

	/** The {@code <form>} stating the width of its grid and the position of its labels. */
	private static final int STATED_GRID = 1;

	/** The {@code <form>} taking a label position only a single field can take. */
	private static final int FIELD_LEVEL_POSITION = 2;

	private ViewContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultViewContext(new HeadlessReactContext());
		_context.registerChannel(DISPLAYED_OBJECT, new DefaultViewChannel(DISPLAYED_OBJECT));
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;

		super.tearDown();
	}

	/**
	 * Tests that a view XML with {@code <form>} and {@code <field>} elements can be parsed into
	 * configuration.
	 */
	public void testParseFormView() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestFormElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestFormElement.class, "test-form.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();

		context.checkErrors();
		assertNotNull("Config should be parsed", config);

		// The content should be a FormElement config.
		assertTrue("Content should be FormElement config",
			config.getContent() instanceof FormElement.Config);

		FormElement.Config formConfig = (FormElement.Config) config.getContent();

		// Verify input channel ref.
		assertNotNull("Input should be set", formConfig.getInput());
		assertEquals("Input channel name", "selectedItem", formConfig.getInput().getChannelName());

		// Verify edit mode channel ref.
		assertNotNull("EditMode should be set", formConfig.getEditMode());
		assertEquals("EditMode channel name", "isEditing", formConfig.getEditMode().getChannelName());

		// Verify dirty channel ref.
		assertNotNull("Dirty should be set", formConfig.getDirty());
		assertEquals("Dirty channel name", "isDirty", formConfig.getDirty().getChannelName());

		// Verify child FieldElement.
		assertEquals("Form should have one child", 1, formConfig.getChildren().size());
		PolymorphicConfiguration<? extends UIElement> childConfig = formConfig.getChildren().get(0);
		assertTrue("Child should be FieldElement config", childConfig instanceof FieldElement.Config);

		FieldElement.Config fieldConfig = (FieldElement.Config) childConfig;
		assertEquals("Field attribute", "name", fieldConfig.getAttribute());
	}

	/**
	 * Tests that a field names the control editing it, in the shape the model annotation of an
	 * attribute names it.
	 */
	public void testParseFieldInputControl() throws Exception {
		FieldElement.Config fieldConfig = readField("test-field-input-control.view.xml");

		PolymorphicConfiguration<? extends ReactFieldControlProvider> inputControl =
			fieldConfig.getInputControl();
		assertNotNull("The field names no control.", inputControl);
		assertEquals(PasswordInputControlProvider.class, inputControl.getImplementationClass());
	}

	/**
	 * Tests that a field saying nothing about its control leaves the choice to the model.
	 */
	public void testAFieldNeedNotNameItsControl() throws Exception {
		assertNull(readField("test-form.view.xml").getInputControl());
	}

	/**
	 * The single field of the form the given view file declares.
	 */
	private static FieldElement.Config readField(String viewFile) throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestFormElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestFormElement.class, viewFile);

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		FormElement.Config formConfig = (FormElement.Config) config.getContent();
		return (FieldElement.Config) formConfig.getChildren().get(0);
	}

	/**
	 * Tests that the parsed configuration can be instantiated into a UIElement tree.
	 */
	public void testInstantiateFormElement() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestFormElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestFormElement.class, "test-form.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();

		UIElement element = context.getInstance(config);
		context.checkErrors();
		assertNotNull("UIElement should be instantiated", element);
		assertTrue("Should be a ViewElement", element instanceof ViewElement);
	}

	/**
	 * Without a word about its grid, the form distributes its fields over three columns and lets
	 * each label follow the width available to it.
	 */
	public void testTheFieldsOfAFormStandInAResponsiveGrid() throws Exception {
		Map<String, Object> state = form(DEFAULT_GRID).scriptingScalarState();

		assertEquals(Integer.valueOf(3), state.get(ReactFormLayoutControl.MAX_COLUMNS));
		assertEquals(LabelPosition.AUTO.getExternalName(), state.get(ReactFormLayoutControl.LABEL_POSITION));
	}

	/** A stated grid width and label position are what the form lays its fields out with. */
	public void testStatedGridReachesTheForm() throws Exception {
		Map<String, Object> state = form(STATED_GRID).scriptingScalarState();

		assertEquals(Integer.valueOf(1), state.get(ReactFormLayoutControl.MAX_COLUMNS));
		assertEquals(LabelPosition.SIDE.getExternalName(), state.get(ReactFormLayoutControl.LABEL_POSITION));
	}

	/**
	 * A label position a single field takes is no position for a form, and saying so is a
	 * configuration error rather than something silently ignored.
	 */
	public void testAFieldLevelLabelPositionIsRejected() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		new DefaultInstantiationContext(log).getInstance(formConfig(FIELD_LEVEL_POSITION));

		assertTrue("A position only a single field can take must be reported.", log.hasErrors());
		String errors = log.getErrors().toString();
		assertTrue("The error must name the position rejected: " + errors,
			errors.contains(LabelPosition.AFTER.getExternalName()));
		assertTrue("...and the positions a form can lay its fields out in: " + errors,
			errors.contains(LabelPosition.AUTO.getExternalName()));
	}

	/** The control the {@code <form>} at the given position builds. */
	private FormControl form(int index) throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestFormElement.class);
		FormElement element = (FormElement) context.getInstance(formConfig(index));
		context.checkErrors();

		IReactControl control = element.createControl(_context);
		assertTrue("A " + FormElement.class.getSimpleName() + " must build a form, but is " + control,
			control instanceof FormControl);
		return (FormControl) control;
	}

	/** The {@code <form>} configuration at the given position in the layout test view. */
	private static FormElement.Config formConfig(int index) throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestFormElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestFormElement.class, "test-form-layout.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		assertTrue("The forms are held by a panel.", config.getContent() instanceof PanelElement.Config);
		List<PolymorphicConfiguration<? extends UIElement>> forms =
			((PanelElement.Config) config.getContent()).getChildren();

		PolymorphicConfiguration<? extends UIElement> entry = forms.get(index);
		assertTrue("Entry " + index + " must be a form, but is " + entry, entry instanceof FormElement.Config);
		return (FormElement.Config) entry;
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

	/**
	 * Test suite requiring the {@link TypeIndex} module for resolving the element tags, and the
	 * services a form resolves its no-model message with.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestFormElement.class,
				TypeIndex.Module.INSTANCE, ThreadContextManager.Module.INSTANCE));
	}
}
