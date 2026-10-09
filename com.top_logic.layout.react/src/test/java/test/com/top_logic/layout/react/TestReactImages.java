/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.ReactImages;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests for {@link ReactImages}.
 */
public class TestReactImages extends TestCase {

	private final ReactContext _context =
		new DefaultReactContext("/app", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));

	/**
	 * An image file reaches the client as the URL it is served at, not as its theme-local key, which
	 * the client has neither the context path nor the theme to complete.
	 */
	public void testImageFile() {
		ThemeImage image = ThemeImage.resourceIcon("/mimetypes/tl/TLReference.png",
			"/themes/core/mimetypes/tl/TLReference.png");

		assertEquals("/app/themes/core/mimetypes/tl/TLReference.png", ReactImages.encode(_context, image));
	}

	/**
	 * An image given by its theme-local key is resolved in the current theme and reaches the client
	 * as the URL of the file in that theme.
	 */
	public void testImageFileResolvedInTheme() {
		ThemeImage image = ThemeImage.internalDecode("/mimetypes/tl/TLProperty.png");
		ThemeImage.Img resolved = (ThemeImage.Img) image.resolve();

		String encoded = ReactImages.encode(_context, image);

		assertEquals("/app" + resolved.getFileLink(), encoded);
		assertTrue("Served from the theme folder: " + encoded, encoded.endsWith("/mimetypes/tl/TLProperty.png"));
	}

	/**
	 * Without a context, the URL of an image file is relative to the server root.
	 */
	public void testImageFileWithoutContext() {
		ThemeImage image = ThemeImage.resourceIcon("/mimetypes/tl/TLReference.png",
			"/themes/core/mimetypes/tl/TLReference.png");

		assertEquals("/themes/core/mimetypes/tl/TLReference.png", ReactImages.encode(null, image));
	}

	/**
	 * An icon font class needs no URL and keeps its encoded form.
	 */
	public void testCssIcon() {
		assertEquals("css:bi bi-pencil", ReactImages.encode(_context, ThemeImage.cssIcon("bi bi-pencil")));
		assertEquals("colored:bi bi-star",
			ReactImages.encode(_context, ThemeImage.coloredCssIcon("bi bi-star")));
	}

	/**
	 * The invisible image keeps its encoded form, which the client renders as nothing.
	 */
	public void testNone() {
		assertEquals(ThemeImage.none().toEncodedForm(), ReactImages.encode(_context, ThemeImage.none()));
	}

	/**
	 * No image stays no image.
	 */
	public void testNull() {
		assertNull(ReactImages.encode(_context, null));
	}

	/**
	 * The suite of tests, providing the theme images are resolved in.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(TestReactImages.class,
			ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
