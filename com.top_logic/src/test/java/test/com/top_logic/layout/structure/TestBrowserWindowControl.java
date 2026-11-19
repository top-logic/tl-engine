/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.structure;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.ComponentTestUtils;
import test.com.top_logic.basic.StringWriterNonNull;
import test.com.top_logic.layout.TestControl;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.structure.BrowserWindowControl;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.DialogClosedListener;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.mig.html.layout.Layout;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.SimpleComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Test case for the popup dialog book-keeping of {@link BrowserWindowControl} and for the selection
 * of the component whose default command is executed on enter.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestBrowserWindowControl extends TestControl {

	/**
	 * Tests that a popup dialog does not survive a repaint of the window.
	 *
	 * <p>
	 * The client-side view of a popup is dropped together with the view of the window, because the
	 * popup anchor is written empty and open popups are not re-rendered.
	 * </p>
	 */
	public void testDropPopupDialogsOnRepaint() {
		BrowserWindowControl window = windowControl();

		PopupDialogControl popup = newPopupDialog();
		window.openPopupDialog(popup);
		revalidate(window, new StringWriterNonNull());
		assertTrue("Popup was not displayed.", popup.isAttached());
		assertEquals(Collections.singletonList(popup), window.getPopupDialogs());

		window.requestRepaint();

		assertEquals("Popup must not survive a repaint of the window.",
			Collections.emptyList(), window.getPopupDialogs());
		assertFalse("Popup must be detached with the window.", popup.isAttached());
		assertTrue("Popup must be closed with the window.", popup.getPopupDialogModel().isClosed());

		/* Ticket #29460: Closing the popup again must not access the ID of the detached control. */
		popup.getPopupDialogModel().setClosed();
	}

	/**
	 * Tests that opening a popup dialog while the window is detached is rejected instead of leaving
	 * it in the list of open popups without ever being attached.
	 */
	public void testRejectPopupDialogOpenedInDetachedState() throws IOException {
		BrowserWindowControl window = windowControl();
		window.detach();

		PopupDialogControl popup = newPopupDialog();
		window.openPopupDialog(popup);

		assertEquals("Popup opened in detached state must be rejected.",
			Collections.emptyList(), window.getPopupDialogs());
		assertTrue("The opener must be informed about the rejection.",
			popup.getPopupDialogModel().isClosed());

		window.write(displayContext(), new TagWriter());

		assertEquals(Collections.emptyList(), window.getPopupDialogs());
	}

	/**
	 * Tests that opening a popup dialog while a repaint of the window is pending is rejected.
	 *
	 * <p>
	 * Note: The window is still {@link BrowserWindowControl#isAttached() attached} in this
	 * situation, its client-side view is just about to be created from scratch. Therefore, checking
	 * the attach state alone is not sufficient.
	 * </p>
	 */
	public void testRejectPopupDialogOpenedWhileRepaintPending() throws IOException {
		BrowserWindowControl window = windowControl();
		window.requestRepaint();

		PopupDialogControl popup = newPopupDialog();
		window.openPopupDialog(popup);

		assertTrue("Window is still attached while a repaint is pending.", window.isAttached());
		assertEquals("Popup opened while a repaint is pending must be rejected.",
			Collections.emptyList(), window.getPopupDialogs());
		assertTrue("The opener must be informed about the rejection.",
			popup.getPopupDialogModel().isClosed());

		window.write(displayContext(), new TagWriter());

		assertEquals(Collections.emptyList(), window.getPopupDialogs());
	}

	/**
	 * Tests that dropping the popups while rendering a complete page does not notify their models.
	 *
	 * <p>
	 * In the rendering phase, listeners must not request repaints any more, see
	 * {@link com.top_logic.layout.LayoutContext#isInCommandPhase()}.
	 * </p>
	 */
	public void testDropPopupDialogsWhileRendering() {
		BrowserWindowControl window = windowControl();

		PopupDialogControl popup = newPopupDialog();
		window.openPopupDialog(popup);
		revalidate(window, new StringWriterNonNull());

		ClosedRecorder recorder = new ClosedRecorder();
		popup.getPopupDialogModel().addListener(PopupDialogModel.POPUP_DIALOG_CLOSED_PROPERTY, recorder);

		boolean before = setUpdatesEnabled(false);
		try {
			// This is what MainLayout.writeBody() does when rendering a complete page.
			window.detach();
		} finally {
			setUpdatesEnabled(before);
		}

		assertEquals("Popup must be dropped when the window is re-rendered.",
			Collections.emptyList(), window.getPopupDialogs());
		assertFalse("Popup models must not be notified during rendering.", recorder._notified);
	}

	/**
	 * Tests that enter in a component with a default command selects this component.
	 */
	public void testDefaultCommandOfSourceComponent() {
		Layout parent = newLayout(true);
		SimpleComponent source = newSimpleComponent(true);
		parent.addComponent(source);

		assertSame(source, BrowserWindowControl.findDefaultCommandComponent(source));
	}

	/**
	 * Tests that enter in a component without default command selects the nearest ancestor with a
	 * default command.
	 */
	public void testDefaultCommandOfAncestor() {
		Layout grandParent = newLayout(true);
		Layout parent = newLayout(false);
		SimpleComponent source = newSimpleComponent(false);
		SimpleComponent sibling = newSimpleComponent(true);
		grandParent.addComponent(parent);
		parent.addComponent(source);
		parent.addComponent(sibling);

		assertSame(grandParent, BrowserWindowControl.findDefaultCommandComponent(source));
	}

	/**
	 * Tests that no component is selected, if neither the source component nor any ancestor has a
	 * default command, so that the default commands of the whole window are executed.
	 */
	public void testNoDefaultCommandInAncestors() {
		Layout parent = newLayout(false);
		SimpleComponent source = newSimpleComponent(false);
		SimpleComponent sibling = newSimpleComponent(true);
		parent.addComponent(source);
		parent.addComponent(sibling);

		assertNull(BrowserWindowControl.findDefaultCommandComponent(source));
	}

	/**
	 * Tests that no component is selected without a source component, so that the default
	 * commands of the whole window are executed.
	 */
	public void testNoSourceComponent() {
		assertNull(BrowserWindowControl.findDefaultCommandComponent(null));
	}

	private static Layout newLayout(boolean withDefaultCommand) {
		Layout.Config config = ComponentTestUtils.newLayoutConfig(false);
		if (withDefaultCommand) {
			setDefaultAction(config);
		}
		return (Layout) TypedConfigUtil.createInstance(config);
	}

	private static SimpleComponent newSimpleComponent(boolean withDefaultCommand) {
		SimpleComponent.Config config = ComponentTestUtils.newSimpleComponentConfig("header", "content");
		config.setImplementationClass(SimpleComponent.class);
		if (withDefaultCommand) {
			setDefaultAction(config);
		}
		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
		return (SimpleComponent) context.getInstance(config);
	}

	private static void setDefaultAction(LayoutComponent.Config config) {
		CommandHandler.Config action = AbstractCommandHandler.createConfig(NoOpCommand.class, "testDefault");
		ConfigurationItem item = config;
		item.update(item.descriptor().getProperty(LayoutComponent.Config.DEFAULT_ACTION), action);
	}

	/**
	 * {@link CommandHandler} used as default command in the tests.
	 */
	public static class NoOpCommand extends AbstractCommandHandler {

		/**
		 * Creates a {@link NoOpCommand}.
		 */
		public NoOpCommand(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
				Map<String, Object> arguments) {
			return HandlerResult.DEFAULT_RESULT;
		}

	}

	private static class ClosedRecorder implements DialogClosedListener {

		boolean _notified;

		@Override
		public void handleDialogClosed(Object sender, Boolean oldValue, Boolean newValue) {
			_notified = true;
		}

	}

	private PopupDialogControl newPopupDialog() {
		FrameScope frameScope = displayContext().getExecutionScope().getFrameScope();
		DefaultPopupDialogModel model = new DefaultPopupDialogModel(DefaultLayoutData.DEFAULT_CONSTRAINT);
		return new PopupDialogControl(frameScope, model, "anchor");
	}

	public static Test suite() {
		return TestControl.suite(TestBrowserWindowControl.class);
	}

}
