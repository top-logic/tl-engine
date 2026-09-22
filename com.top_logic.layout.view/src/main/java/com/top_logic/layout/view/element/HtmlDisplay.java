/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.react.control.html.ReactHtmlControl;

/**
 * How a {@link HtmlElement} displays its HTML content.
 */
public enum HtmlDisplay implements ExternallyNamed {

	/**
	 * Part of the page around it (the default).
	 *
	 * <p>
	 * The content is inserted where the element stands and is styled by the page it lands in, so it
	 * reads as a section of that page. Only content the safety check accepts is inserted; content
	 * it rejects is replaced by a message saying so.
	 * </p>
	 */
	INLINE(ReactHtmlControl.DISPLAY_INLINE),

	/**
	 * A page of its own.
	 *
	 * <p>
	 * The content is shown as a document of its own, filling the space the element is given. It
	 * keeps the styles it brings along and takes none of the page's; scripts it carries are not
	 * executed.
	 * </p>
	 */
	DOCUMENT(ReactHtmlControl.DISPLAY_DOCUMENT),

	/**
	 * A scaled-down preview.
	 *
	 * <p>
	 * The content is shown as a document the reader looks at rather than reads: scaled to fit the
	 * space the element is given, the way a document list previews what it lists.
	 * </p>
	 */
	THUMBNAIL(ReactHtmlControl.DISPLAY_THUMBNAIL);

	private final String _externalName;

	HtmlDisplay(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
