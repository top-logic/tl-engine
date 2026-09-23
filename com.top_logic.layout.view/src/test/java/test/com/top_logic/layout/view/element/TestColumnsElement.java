/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactColumnsControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.element.ColumnElement;
import com.top_logic.layout.view.element.ColumnsElement;

/**
 * Tests what a {@link ColumnsElement} makes of the {@link ColumnElement columns} written into it.
 *
 * <p>
 * The element is exercised through its public seam - a view read the way the application reads it,
 * and a control created for a view context.
 * </p>
 */
public class TestColumnsElement extends TestCase {

	private static final String CONTEXT_PATH = "/app";

	private static final String VIEW = """
			<view>
				<columns breakpoint="40rem">
					<column weight="2">
						<image resource="/main.png"/>
					</column>
					<column>
						<image resource="/side.png"/>
					</column>
				</columns>
			</view>
			""";

	private ColumnsElement _element;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_element = createColumns(VIEW);
	}

	/** Every configured column becomes one column of the control. */
	public void testOneControlPerColumn() {
		ReactControl control = createControl(_element);

		assertTrue("The columns layout renders through its own control, not " + control,
			control instanceof ReactColumnsControl);
		assertEquals("One column of the view is one child of the control.", 2,
			control.displayedChildren().size());
	}

	/** The columns are reported as the content of the element, in the order they are written. */
	public void testColumnsAreTheContent() {
		List<UIElement> columns = columns(_element);

		assertEquals("The view writes two columns.", 2, columns.size());
		assertTrue("A column of a columns layout is a column element, not " + columns.get(0),
			columns.get(0) instanceof ColumnElement);
	}

	/** A column takes the weight written on it, and weight 1 where none is written. */
	public void testColumnWeights() {
		List<UIElement> columns = columns(_element);

		assertEquals("The main column is written with twice the weight.", 2,
			((ColumnElement) columns.get(0)).getWeight());
		assertEquals("A column without a weight of its own takes an even share.", 1,
			((ColumnElement) columns.get(1)).getWeight());
	}

	/** The element tag is resolved inside a container as well, beside the tags of other elements. */
	public void testColumnsInsideAContainer() throws Exception {
		DefaultInstantiationContext instantiationContext = new DefaultInstantiationContext(TestColumnsElement.class);
		ViewElement.Config config = ViewLoader.parseConfig(List.of(CharacterContents.newContent("""
				<view>
					<panel>
						<columns>
							<column>
								<image resource="/only.png"/>
							</column>
						</columns>
					</panel>
				</view>
				""", "test-columns-in-panel.view.xml")));
		UIElement panel = instantiationContext.getInstance(config.getContent());
		instantiationContext.checkErrors();

		List<UIElement> children = ((ChildGroup.Elements) panel.getChildGroups().get(0)).children();
		assertEquals("The panel holds the columns layout.", 1, children.size());
		assertTrue("A <columns> child of a panel is a columns layout, not " + children.get(0),
			children.get(0) instanceof ColumnsElement);
	}

	/** A columns layout without any column has nothing to place. */
	public void testColumnsWithoutColumns() throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		new DefaultInstantiationContext(log).getInstance(columnsConfig("""
				<view>
					<columns/>
				</view>
				"""));

		List<String> errors = log.getErrors();
		assertFalse("A columns layout without a column is a configuration error.", errors.isEmpty());
		assertTrue("The failure names the element: " + errors, errors.get(0).contains("column"));
	}

	/** The columns of the given element, as it reports its content. */
	private static List<UIElement> columns(ColumnsElement element) {
		List<ChildGroup> groups = element.getChildGroups();
		assertEquals("The columns are reported as one group.", 1, groups.size());
		return ((ChildGroup.Elements) groups.get(0)).children();
	}

	/** The control the given element creates for a session. */
	private static ReactControl createControl(ColumnsElement element) {
		ViewContext context = new DefaultViewContext(new DefaultReactContext(CONTEXT_PATH, "test",
			new SSEUpdateQueue(), new ReactWindowRegistry("test")));

		return (ReactControl) element.createControl(context);
	}

	/** The columns element the given view shows. */
	private static ColumnsElement createColumns(String view) throws ConfigurationException {
		DefaultInstantiationContext instantiationContext = new DefaultInstantiationContext(TestColumnsElement.class);
		UIElement element = instantiationContext.getInstance(columnsConfig(view));
		instantiationContext.checkErrors();

		assertTrue("The view shows a columns layout, not " + element, element instanceof ColumnsElement);
		return (ColumnsElement) element;
	}

	/** The {@code columns} configuration the given view shows. */
	private static PolymorphicConfiguration<? extends UIElement> columnsConfig(String view)
			throws ConfigurationException {
		ViewElement.Config config =
			ViewLoader.parseConfig(List.of(CharacterContents.newContent(view, "test-columns.view.xml")));
		PolymorphicConfiguration<? extends UIElement> content = config.getContent();
		assertTrue("The view shows a columns layout, not " + content, content instanceof ColumnsElement.Config);
		return content;
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, which resolves the element tags of a view.
	 */
	public static Test suite() throws ModuleException {
		return ServiceTestSetup.createSetup(TestColumnsElement.class, TypeIndex.Module.INSTANCE);
	}

}
