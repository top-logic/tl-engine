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
import com.top_logic.layout.structure.PopupDialogControl;

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
	 * Tests that a popup dialog opened while the window is detached is dropped instead of staying
	 * in the list of open popups without ever being attached.
	 */
	public void testDropPopupDialogsOpenedInDetachedState() throws IOException {
		BrowserWindowControl window = windowControl();
		window.detach();

		PopupDialogControl popup = newPopupDialog();
		window.openPopupDialog(popup);
		assertFalse(popup.isAttached());

		window.write(displayContext(), new TagWriter());

		assertEquals("Popup opened in detached state must be dropped.",
			Collections.emptyList(), window.getPopupDialogs());

		/* Ticket #29460: Closing the popup must not access the ID of the never attached control. */
		popup.getPopupDialogModel().setClosed();
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
