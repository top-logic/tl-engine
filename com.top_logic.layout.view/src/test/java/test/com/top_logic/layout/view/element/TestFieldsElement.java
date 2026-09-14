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
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.layout.LabelPosition;
import com.top_logic.layout.react.control.layout.ReactFormLayoutControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.element.FieldsElement;
import com.top_logic.layout.view.element.PanelElement;
import com.top_logic.layout.view.element.ValueInputElement;

/**
 * Tests for {@link FieldsElement} - the {@code <fields>} element laying its content out as the
 * fields of a form.
 *
 * <p>
 * What the element decides is the grid: how many columns the fields are distributed over and where
 * their labels stand. The fields themselves are whatever the view puts inside - the inputs a view
 * owns are the case the element exists for - and reach the grid in the order they are written in.
 * </p>
 */
public class TestFieldsElement extends TestCase {

	/** The {@code <fields>} holding the inputs of the view, which are only described here. */
	private static final int OWNED_INPUTS = 0;

	/** The {@code <fields>} stating nothing about its grid. */
	private static final int DEFAULT_GRID = 1;

	/** The {@code <fields>} stating the width of its grid and the position of its labels. */
	private static final int STATED_GRID = 2;

	/** The {@code <fields>} taking a label position only a single field can take. */
	private static final int FIELD_LEVEL_POSITION = 3;

	private ViewContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultViewContext(new DefaultReactContext("", "test", new SSEUpdateQueue()));
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;

		super.tearDown();
	}

	/**
	 * The inputs a view owns stand in the grid, in the order they are written in, and without a
	 * word about the grid it is the responsive one.
	 */
	public void testTheInputsOfAViewStandInTheGrid() throws Exception {
		FieldsElement.Config fields = config(OWNED_INPUTS);

		List<PolymorphicConfiguration<? extends UIElement>> children = fields.getChildren();
		assertEquals("Every input written in the element must reach it.", 2, children.size());
		assertEquals("term", input(children, 0).getValue().getChannelName());
		assertEquals("status", input(children, 1).getValue().getChannelName());

		assertEquals("Without a word, the fields are distributed over three columns.",
			3, fields.getMaxColumns());
		assertEquals("Without a word, a label follows the width available to it.",
			LabelPosition.AUTO, fields.getLabelPosition());
	}

	/**
	 * The grid holds the controls of its fields, in the order the fields are written in, and lays
	 * them out responsively over three columns.
	 */
	public void testTheGridHoldsTheFieldsInOrder() throws Exception {
		ReactFormLayoutControl grid = grid(DEFAULT_GRID);

		List<ReactControl> fields = grid.scriptingChildren();
		assertEquals("Both fields must stand in the grid.", 2, fields.size());
		assertEquals("The first field written must stand first.", "First", text(fields, 0));
		assertEquals("...and the second one after it.", "Second", text(fields, 1));

		Map<String, Object> state = grid.scriptingScalarState();
		assertEquals(Integer.valueOf(3), state.get(ReactFormLayoutControl.MAX_COLUMNS));
		assertEquals(LabelPosition.AUTO.getExternalName(), state.get(ReactFormLayoutControl.LABEL_POSITION));
		assertEquals("Whether a value can be changed is the field's own business.",
			Boolean.FALSE, state.get(ReactFormLayoutControl.READ_ONLY));
	}

	/** A stated grid width and label position are what the grid is built with. */
	public void testStatedGridReachesTheLayout() throws Exception {
		Map<String, Object> state = grid(STATED_GRID).scriptingScalarState();

		assertEquals(Integer.valueOf(2), state.get(ReactFormLayoutControl.MAX_COLUMNS));
		assertEquals(LabelPosition.TOP.getExternalName(), state.get(ReactFormLayoutControl.LABEL_POSITION));
	}

	/**
	 * A label position a single field takes is no position for a grid of fields, and saying so is
	 * a configuration error rather than something silently ignored.
	 */
	public void testAFieldLevelLabelPositionIsRejected() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		new DefaultInstantiationContext(log).getInstance(config(FIELD_LEVEL_POSITION));

		assertTrue("A position only a single field can take must be reported.", log.hasErrors());
		String errors = log.getErrors().toString();
		assertTrue("The error must name the position rejected: " + errors,
			errors.contains(LabelPosition.HIDDEN.getExternalName()));
		assertTrue("...and the positions a grid of fields can take: " + errors,
			errors.contains(LabelPosition.AUTO.getExternalName()));
	}

	/** The layout the {@code <fields>} at the given position builds. */
	private ReactFormLayoutControl grid(int index) throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestFieldsElement.class);
		FieldsElement element = (FieldsElement) context.getInstance(config(index));
		context.checkErrors();

		IReactControl control = element.createControl(_context);
		assertTrue("A " + FieldsElement.class.getSimpleName() + " must lay its content out as a form does, but is "
			+ control, control instanceof ReactFormLayoutControl);
		return (ReactFormLayoutControl) control;
	}

	/** The text displayed by the field at the given position. */
	private static String text(List<ReactControl> fields, int index) {
		ReactControl field = fields.get(index);
		assertTrue("Field " + index + " must display a text, but is " + field, field instanceof ReactTextControl);
		return ((ReactTextControl) field).getText();
	}

	/** The {@code <fields>} configuration at the given position in the test view. */
	private static FieldsElement.Config config(int index) throws Exception {
		PolymorphicConfiguration<? extends UIElement> entry = parseFields().get(index);
		assertTrue("Entry " + index + " must be a field grid, but is " + entry,
			entry instanceof FieldsElement.Config);
		return (FieldsElement.Config) entry;
	}

	/** The {@code <value-input>} configuration at the given position within a {@code <fields>}. */
	private static ValueInputElement.Config input(List<PolymorphicConfiguration<? extends UIElement>> children,
			int index) {
		PolymorphicConfiguration<? extends UIElement> entry = children.get(index);
		assertTrue("Entry " + index + " must be a value input, but is " + entry,
			entry instanceof ValueInputElement.Config);
		return (ValueInputElement.Config) entry;
	}

	/** The {@code <fields>} configurations of the test view. */
	private static List<PolymorphicConfiguration<? extends UIElement>> parseFields() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestFieldsElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestFieldsElement.class, "test-fields.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		assertTrue("The field grids are held by a panel.", config.getContent() instanceof PanelElement.Config);
		return ((PanelElement.Config) config.getContent()).getChildren();
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module for resolving the element tags, and the
	 * services a displayed text resolves its label with.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestFieldsElement.class,
				TypeIndex.Module.INSTANCE, ThreadContextManager.Module.INSTANCE));
	}
}
