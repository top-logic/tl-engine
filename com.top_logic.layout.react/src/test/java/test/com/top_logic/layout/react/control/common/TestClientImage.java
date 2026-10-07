/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.common;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.common.ClientImage;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests the form in which {@link ClientImage} sends a {@link ThemeImage} to the client.
 */
public class TestClientImage extends TestCase {

	private static final String CONTEXT_PATH = "/my-app";

	private static final String IMAGE_PATH = "/mimetypes/tl/TLProperty.png";

	/** An icon-font image keeps its encoded form, the client renders the classes. */
	public void testCssIconKeepsItsEncodedForm() {
		ThemeImage icon = ThemeImage.internalDecode("css:fa-solid fa-house");

		assertEquals("css:fa-solid fa-house", ClientImage.encode(context(), icon));
	}

	/** A colored icon-font image keeps its encoded form as well. */
	public void testColoredCssIconKeepsItsEncodedForm() {
		ThemeImage icon = ThemeImage.internalDecode("colored:fa-solid fa-star");

		assertEquals("colored:fa-solid fa-star", ClientImage.encode(context(), icon));
	}

	/**
	 * An image resource becomes the URL the browser loads it from: the context path followed by the
	 * file link of the image in the current theme.
	 */
	public void testImageResourceBecomesContextPathURL() {
		ThemeImage image = ThemeImage.internalDecode(IMAGE_PATH);
		ThemeImage.Img resolved = (ThemeImage.Img) image.resolve();

		String encoded = ClientImage.encode(context(), image);

		assertEquals(CONTEXT_PATH + resolved.getFileLink(), encoded);
		assertTrue("Served below the context path: " + encoded, encoded.startsWith(CONTEXT_PATH + "/"));
		assertTrue("Served from the theme folder: " + encoded, encoded.endsWith(IMAGE_PATH));
	}

	/** The invisible image is sent as such. */
	public void testNoImage() {
		assertEquals(ThemeImage.none().toEncodedForm(), ClientImage.encode(context(), ThemeImage.none()));
		assertNull(ClientImage.encode(context(), null));
	}

	private static ReactContext context() {
		return new DefaultReactContext(CONTEXT_PATH, "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/**
	 * The suite of tests, requiring the theme images are resolved in.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(TestClientImage.class,
			ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
