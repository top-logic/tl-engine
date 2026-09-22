/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.layout.ReactFormGroupControl;
import com.top_logic.layout.react.control.layout.ReactFormGroupControl.GroupBorder;
import com.top_logic.layout.react.control.layout.ReactFormLayoutControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.element.FieldsElement;
import com.top_logic.layout.view.element.FormElement;
import com.top_logic.layout.view.element.GroupElement;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.model.listen.ModelScope;

/**
 * Tests for {@link GroupElement} - the {@code <group>} element sectioning the fields of a form.
 *
 * <p>
 * A section is built from the same context its siblings are built in, so a field inside it reaches
 * the form it stands in just as a field beside it does. What the element decides beyond that is
 * how the section is displayed: under which heading, in which frame, folded or unfolded, and over
 * the whole grid or in a single column of it.
 * </p>
 */
public class TestGroupElement extends TestCase {

	/** The channel the form of the test view displays its object from. */
	private static final String DISPLAYED_OBJECT = "selectedItem";

	/** The section saying nothing about itself. */
	private static final int PLAIN_SECTION = 0;

	/** The framed, foldable section standing in a single column. */
	private static final int STATED_SECTION = 1;

	/** The {@code <fields>} grid holding a section of its own. */
	private static final int NESTED_IN_GRID = 2;

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
	 * A section saying nothing about itself carries no heading, is drawn without a frame, cannot
	 * be folded away, and takes the whole width of the form.
	 */
	public void testASectionSayingNothingIsAPlainFullWidthSection() throws Exception {
		ReactFormGroupControl section = section(PLAIN_SECTION);

		assertNull("A section without a label shows no heading.", section.getHeader());

		Map<String, Object> state = section.scriptingScalarState();
		assertEquals(GroupBorder.NONE.getExternalName(), state.get(ReactFormGroupControl.BORDER));
		assertEquals(Boolean.FALSE, state.get(ReactFormGroupControl.COLLAPSIBLE));
		assertEquals(Boolean.FALSE, state.get(ReactFormGroupControl.COLLAPSED));
		assertEquals("A section spans the whole width of its form.",
			Boolean.TRUE, state.get(ReactFormGroupControl.FULL_LINE));
		assertFalse("A section that cannot be folded is not folded.", section.isCollapsed());
	}

	/**
	 * The fields written into a section are the ones it renders, and they are built against the
	 * form the section stands in - which is what lets a field inside a section find its object at
	 * all.
	 */
	public void testTheFieldsOfASectionReachTheFormAroundIt() throws Exception {
		List<ReactControl> fields = body(section(PLAIN_SECTION));

		assertEquals("Every field written into the section must reach it.", 2, fields.size());
	}

	/** What a section states about its display is what it is built with. */
	public void testAStatedSectionIsBuiltAsStated() throws Exception {
		ReactFormGroupControl section = section(STATED_SECTION);

		ReactControl header = section.getHeader();
		assertTrue("A labelled section shows its label as its heading, but shows " + header,
			header instanceof ReactTextControl);
		assertEquals("Address", ((ReactTextControl) header).getText());

		Map<String, Object> state = section.scriptingScalarState();
		assertEquals(GroupBorder.OUTLINED.getExternalName(), state.get(ReactFormGroupControl.BORDER));
		assertEquals(Boolean.TRUE, state.get(ReactFormGroupControl.COLLAPSIBLE));
		assertEquals(Boolean.TRUE, state.get(ReactFormGroupControl.COLLAPSED));
		assertEquals("A section of a single column does not span the form.",
			Boolean.FALSE, state.get(ReactFormGroupControl.FULL_LINE));
		assertTrue("A section stated to start folded is folded.", section.isCollapsed());

		assertEquals("The heading is no content of the section.", 1, body(section).size());
	}

	/** A grid of fields sections its content the same way a form does. */
	public void testAGridOfFieldsHoldsSectionsToo() throws Exception {
		IReactControl grid = element(NESTED_IN_GRID).createControl(_context);
		assertTrue("A " + FieldsElement.class.getSimpleName() + " lays its content out as a form does, but is "
			+ grid, grid instanceof ReactFormLayoutControl);

		List<ReactControl> content = ((ReactFormLayoutControl) grid).scriptingChildren();
		assertEquals("The grid holds the section written into it.", 1, content.size());
		assertTrue("The content of the grid must be a section, but is " + content.get(0),
			content.get(0) instanceof ReactFormGroupControl);
		assertEquals("The section holds the text written into it.",
			1, body((ReactFormGroupControl) content.get(0)).size());
	}

	/**
	 * The controls a section renders as its content: everything it displays except its heading.
	 */
	private static List<ReactControl> body(ReactFormGroupControl section) {
		List<ReactControl> result = new ArrayList<>(section.scriptingChildren());
		result.remove(section.getHeader());
		return result;
	}

	/** The section at the given position within the form of the test view. */
	private ReactFormGroupControl section(int index) throws Exception {
		IReactControl control = form().scriptingChildren().get(index);
		assertTrue("Entry " + index + " must be a section, but is " + control,
			control instanceof ReactFormGroupControl);
		return (ReactFormGroupControl) control;
	}

	/** The form of the test view, with its content built. */
	private FormControl form() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestGroupElement.class);
		FormElement element = (FormElement) context.getInstance(formConfig());
		context.checkErrors();

		IReactControl control = element.createControl(_context);
		assertTrue("A " + FormElement.class.getSimpleName() + " must build a form, but is " + control,
			control instanceof FormControl);
		return (FormControl) control;
	}

	/** The element at the given position within the form of the test view. */
	private static UIElement element(int index) throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestGroupElement.class);
		UIElement result = context.getInstance(formConfig().getChildren().get(index));
		context.checkErrors();
		return result;
	}

	/** The {@code <form>} configuration of the test view. */
	private static FormElement.Config formConfig() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestGroupElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestGroupElement.class, "test-group.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		assertTrue("The sections are held by a form.", config.getContent() instanceof FormElement.Config);
		FormElement.Config formConfig = (FormElement.Config) config.getContent();

		List<PolymorphicConfiguration<? extends UIElement>> children = formConfig.getChildren();
		assertTrue("The plain section must be written as a group.",
			children.get(PLAIN_SECTION) instanceof GroupElement.Config);
		assertTrue("The stated section must be written as a group.",
			children.get(STATED_SECTION) instanceof GroupElement.Config);
		assertTrue("The nested section must stand in a grid of fields.",
			children.get(NESTED_IN_GRID) instanceof FieldsElement.Config);

		return formConfig;
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
	 * services a heading and a form message resolve their labels with.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestGroupElement.class,
				TypeIndex.Module.INSTANCE, ThreadContextManager.Module.INSTANCE));
	}
}
