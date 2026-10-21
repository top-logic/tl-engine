/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.layout.view.navigation.FixtureViews;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.overlay.ReactPopoverControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.ControlState;
import com.top_logic.layout.react.state.PopoverState;
import com.top_logic.layout.react.state.PopoverState.Mode;
import com.top_logic.layout.react.state.PopoverState.Placement;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ClosePopoverAction;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.element.PopoverElement;
import com.top_logic.layout.view.navigation.RevealPath;

/**
 * Tests {@link PopoverElement} and {@link ClosePopoverAction}: the options a {@code <popover>} is
 * written with, its lazily created content sharing the channels of the view, and closing it from
 * within that content.
 */
public class TestPopoverElement extends TestCase {

	/** A configured popover with trigger content, followed by one written with defaults only. */
	private static final String VIEW = "popover.view.xml";

	/** The channel the view declares. */
	private static final String SELECTION = "selection";

	/**
	 * The contexts the {@link ContextProbe}s were created in, in creation order.
	 */
	static final List<ViewContext> PROBED = new ArrayList<>();

	private ViewContext _context;

	private ViewChannel _selection;

	private List<PopoverElement> _popovers;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PROBED.clear();

		_context = new DefaultViewContext(
			new DefaultReactContext("/app", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_selection = new DefaultViewChannel(SELECTION);
		_context.registerChannel(SELECTION, _selection);
		_popovers = popovers();
	}

	@Override
	protected void tearDown() throws Exception {
		PROBED.clear();
		_popovers = null;
		_selection = null;
		_context = null;

		super.tearDown();
	}

	/**
	 * Tests that placement and opening gesture arrive by their protocol names, together with label,
	 * icon and CSS class.
	 */
	public void testConfiguredOptions() {
		ReactPopoverControl popover = create(0);
		Map<?, ?> state = state(popover);

		assertEquals(Placement.TOP_END.protocolName(), state.get(PopoverState.PLACEMENT__PROP));
		assertEquals(Mode.HOVER.protocolName(), state.get(PopoverState.MODE__PROP));
		assertEquals("Filter", state.get(PopoverState.LABEL__PROP));
		assertEquals("css:fa-solid fa-filter", state.get(PopoverState.ICON__PROP));
		assertEquals("my-popover", state.get(ControlState.CSS_CLASS__PROP));
		assertNotNull("The trigger content is sent.", state.get(PopoverState.TRIGGER__PROP));
		assertEquals(Boolean.FALSE, state.get(PopoverState.OPEN__PROP));
	}

	/**
	 * Tests that a popover written without options opens on click below the start edge of its
	 * button, which shows the label alone.
	 */
	public void testDefaults() {
		Map<?, ?> state = state(create(1));
		assertEquals(Placement.BOTTOM_START.protocolName(), state.get(PopoverState.PLACEMENT__PROP));
		assertEquals(Mode.CLICK.protocolName(), state.get(PopoverState.MODE__PROP));
		assertNull("No trigger content is written.", state.get(PopoverState.TRIGGER__PROP));
		assertNull("No icon is written.", state.get(PopoverState.ICON__PROP));
	}

	/**
	 * Tests that the child groups report the trigger content as part of the popover and the content
	 * under the key revealing it.
	 */
	public void testChildGroups() {
		List<ChildGroup> groups = _popovers.get(0).getChildGroups();

		assertEquals(2, groups.size());
		assertEquals(ChildGroup.NO_KEY, groups.get(0).key());
		assertEquals(1, ((ChildGroup.Elements) groups.get(0)).children().size());
		assertEquals(PopoverElement.CONTENT_KEY, groups.get(1).key());
		assertEquals(2, ((ChildGroup.Elements) groups.get(1)).children().size());

		List<ChildGroup> plainGroups = _popovers.get(1).getChildGroups();
		assertEquals("A popover without trigger content.", List.of(),
			((ChildGroup.Elements) plainGroups.get(0)).children());
	}

	/**
	 * Tests that the content is created when the popover opens for the first time, and kept when it
	 * opens again.
	 */
	public void testContentCreatedOnFirstOpen() {
		ReactPopoverControl popover = create(0);
		assertTrue("No content before the popover opens.", PROBED.isEmpty());
		assertNull(state(popover).get(PopoverState.CONTENT__PROP));

		popover.open();
		assertEquals(1, PROBED.size());
		assertNotNull(state(popover).get(PopoverState.CONTENT__PROP));

		popover.close();
		popover.open();
		assertEquals("The content is kept while the popover is closed.", 1, PROBED.size());
	}

	/**
	 * Tests that the content sees the channels of the view the popover is written in.
	 */
	public void testContentSharesViewChannels() {
		ReactPopoverControl popover = create(0);
		popover.open();

		ViewContext contentContext = PROBED.get(0);
		assertSame(_selection, contentContext.resolveChannel(new ChannelRef(SELECTION)));
	}

	/**
	 * Tests that a {@code <close-popover/>} in a command of the content closes the popover.
	 */
	public void testClosePopoverFromContent() {
		ReactPopoverControl popover = create(0);
		popover.attach();
		popover.open();
		assertTrue(popover.isOpen());

		ReactButtonControl button = findButton(popover);
		button.executeCommand("click", Map.of());

		assertFalse("The button of the content closed the popover.", popover.isOpen());
	}

	/**
	 * Tests that a {@code <close-popover/>} closes the innermost popover enclosing it, and passes its
	 * input on.
	 */
	public void testCloseInnermostPopover() {
		ReactPopoverControl outer = create(0);
		outer.open();
		ReactPopoverControl inner = create(1);
		inner.open();

		Object input = new Object();
		assertSame(input, closeAction().execute(PROBED.get(1), input));

		assertFalse(inner.isOpen());
		assertTrue("The outer popover is left open.", outer.isOpen());
	}

	/**
	 * Tests that a {@code <close-popover/>} outside the content of a popover does nothing.
	 */
	public void testCloseOutsidePopover() {
		ReactPopoverControl popover = create(0);
		popover.open();

		Object input = new Object();
		assertSame(input, closeAction().execute(_context, input));
		assertTrue("A popover is closed only from within.", popover.isOpen());
	}

	/**
	 * Tests that revealing the content of a closed popover opens it, and that the content knows it
	 * lies behind the popover.
	 */
	public void testRevealOpensPopover() throws Exception {
		ReactPopoverControl popover = create(0);

		_context.getRevealRegistry().getContainer(_popovers.get(0), RevealPath.ROOT)
			.revealChild(PopoverElement.CONTENT_KEY);

		assertTrue(popover.isOpen());
		assertEquals(RevealPath.ROOT.append(_popovers.get(0), PopoverElement.CONTENT_KEY),
			RevealPath.of(PROBED.get(0)));
	}

	private ReactPopoverControl create(int index) {
		return (ReactPopoverControl) _popovers.get(index).createControl(_context);
	}

	private static ClosePopoverAction closeAction() {
		return new ClosePopoverAction(null, null);
	}

	private static ReactButtonControl findButton(ReactControl control) {
		if (control instanceof ReactButtonControl button) {
			return button;
		}
		for (ReactControl child : control.visibleChildren()) {
			ReactButtonControl found = findButton(child);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	private static List<PopoverElement> popovers() throws ConfigurationException {
		ViewElement view = new FixtureViews(TestPopoverElement.class).getView(VIEW);
		UIElement stack = ((ChildGroup.Elements) view.getChildGroups().get(0)).children().get(0);
		List<PopoverElement> result = new ArrayList<>();
		for (UIElement child : ((ChildGroup.Elements) stack.getChildGroups().get(0)).children()) {
			result.add((PopoverElement) child);
		}
		return result;
	}

	private static Map<?, ?> state(ReactControl control) {
		String json = control.stateAsJSON();
		try {
			return (Map<?, ?>) JSON.fromString(json);
		} catch (ParseException ex) {
			throw new AssertionError("Not a state object: " + json, ex);
		}
	}

	/**
	 * Element recording the context it is created in, standing for any content of a popover.
	 */
	public static class ContextProbe implements UIElement {

		/**
		 * Configuration of a {@link ContextProbe}.
		 */
		public interface Config extends UIElement.Config {

			@Override
			@ClassDefault(ContextProbe.class)
			Class<? extends UIElement> getImplementationClass();
		}

		/**
		 * Creates a {@link ContextProbe}.
		 */
		@CalledByReflection
		public ContextProbe(InstantiationContext context, Config config) {
			// No configuration.
		}

		@Override
		public IReactControl createControl(ViewContext context) {
			PROBED.add(context);
			return new ReactStackControl(context, List.of());
		}
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The button in the content observes its command through the model scope of the
	 *           window, which listens to the {@link com.top_logic.knowledge.service.KnowledgeBase}.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestPopoverElement.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, CommandCliqueService.Module.INSTANCE));
	}
}
