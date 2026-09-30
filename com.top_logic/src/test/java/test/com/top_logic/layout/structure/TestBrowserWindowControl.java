/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.structure;

import java.io.IOException;
import java.util.Collections;

import junit.framework.Test;

import test.com.top_logic.basic.StringWriterNonNull;
import test.com.top_logic.layout.TestControl;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.structure.BrowserWindowControl;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.DialogClosedListener;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.structure.PopupDialogModel;

/**
 * Test case for the popup dialog book-keeping of {@link BrowserWindowControl}.
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
