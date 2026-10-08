/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react;

import junit.framework.TestCase;

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

}
