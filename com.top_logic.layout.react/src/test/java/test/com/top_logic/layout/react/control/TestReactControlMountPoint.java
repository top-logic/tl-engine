/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.io.IOException;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Tests the mount point {@link ReactControl#write(TagWriter)} renders: it names the control in
 * {@link ReactControl#MOUNT_ATTRIBUTE} and leaves the control ID as {@code id} to the root element
 * of the client component.
 */
public class TestReactControlMountPoint extends TestCase {

	/**
	 * The mount point carries the control ID in {@link ReactControl#MOUNT_ATTRIBUTE} and has no
	 * {@code id}.
	 */
	public void testMountPointCarriesNoId() throws IOException {
		ReactButtonControl control = new ReactButtonControl(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")),
			"Press me", ctx -> {
				// Not clicked.
			});

		TagWriter out = new TagWriter();
		control.write(out);
		String rendered = out.toString();

		assertTrue("The mount point must name the control: " + rendered,
			rendered.startsWith("<div " + ReactControl.MOUNT_ATTRIBUTE + "=\"" + control.getID() + "\""));
		assertFalse("The mount point must not carry an id: " + rendered,
			rendered.contains(" " + HTMLConstants.ID_ATTR + "="));
	}

}
