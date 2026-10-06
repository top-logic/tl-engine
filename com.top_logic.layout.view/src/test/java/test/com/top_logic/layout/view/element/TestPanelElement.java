/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.List;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactInsetControl;
import com.top_logic.layout.react.control.layout.ReactPanelControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.layout.ReactToolbarControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.element.InsetOptions;
import com.top_logic.layout.view.element.PanelElement;

/**
 * Tests the {@link InsetOptions#getInset() inset} of the body of a {@link PanelElement}.
 */
public class TestPanelElement extends TestCase {

	/** The view the panels under test are read from. */
	private static final String VIEW = "test-panel-inset.view.xml";

	/** Position of the panel setting the inset in the stack of the test view. */
	private static final int INSET_PANEL = 0;

	/** Position of the panel saying nothing about the inset in the stack of the test view. */
	private static final int FLUSH_PANEL = 1;

	/**
	 * A panel setting the inset wraps its body in an inset, which holds the body content itself.
	 */
	public void testAnInsetPanelWrapsItsBodyInAnInset() throws Exception {
		ReactControl body = body(INSET_PANEL);
		assertTrue("The body of an inset panel must be wrapped in an inset, but is " + body,
			body instanceof ReactInsetControl);

		List<ReactControl> content = ((ReactInsetControl) body).scriptingChildren();
		assertEquals("The inset holds exactly the body content.", 1, content.size());
		assertTrue("The inset must hold the stack of the two texts of the body, but holds " + content.get(0),
			content.get(0) instanceof ReactStackControl);
	}

	/** Without a word about the inset, the body content is the panel content, with nothing around. */
	public void testAPanelBodyIsFlushByDefault() throws Exception {
		ReactControl body = body(FLUSH_PANEL);
		assertTrue("The body of a panel not asking for an inset must be its content itself, but is " + body,
			body instanceof ReactStackControl);
	}

	/** The control displaying the body of the panel at the given position of the test view. */
	private static ReactControl body(int index) throws Exception {
		ReactControl stack = createContent();
		List<ReactControl> panels = stack.displayedChildren();
		ReactControl panel = panels.get(index);
		assertTrue("Entry " + index + " must be a panel, but is " + panel, panel instanceof ReactPanelControl);

		List<ReactControl> body = panel.displayedChildren().stream()
			.filter(child -> !(child instanceof ReactToolbarControl))
			.collect(Collectors.toList());
		assertEquals("A panel without title content displays exactly one body next to its toolbars.", 1,
			body.size());
		return body.get(0);
	}

	/** The control of the element the test view shows. */
	private static ReactControl createContent() throws ConfigurationException {
		ViewElement.Config view = ViewLoader.parseConfig(
			List.of(new ClassRelativeBinaryContent(TestPanelElement.class, VIEW)));

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestPanelElement.class);
		UIElement element = context.getInstance(view.getContent());
		context.checkErrors();

		return (ReactControl) element.createControl(new DefaultViewContext(new DefaultReactContext("/app", "test",
			new SSEUpdateQueue(), new ReactWindowRegistry("test"))));
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, which resolves the element tags of a view,
	 * the services that turn the configured label of a text into the text displayed, and the
	 * {@link CommandCliqueService} the toolbars of a panel are built with.
	 */
	public static Test suite() throws ModuleException {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestPanelElement.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
				CommandCliqueService.Module.INSTANCE));
	}

}
