/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

/**
 * The kind of value a {@link ReactTextInputControl} edits.
 *
 * <p>
 * The kind decides the {@link #htmlType() HTML input type} the browser is asked for - and with it
 * the on-screen keyboard a phone offers and the browser's own completion - as well as the link the
 * control offers next to the value and the way typed text is normalized.
 * </p>
 */
public enum InputType {

	/** Plain text, offering no link and no normalization. */
	TEXT("text"),

	/** A web address, opened in a new tab and completed to an absolute address when typed without a scheme. */
	URL("url"),

	/** An e-mail address, opened in the user's mail program. */
	EMAIL("email"),

	/** A phone number, opened in the user's phone program. */
	TEL("tel"),

	/** A search term, rendered with the browser's affordance for clearing it. */
	SEARCH("search");

	private final String _htmlType;

	private InputType(String htmlType) {
		_htmlType = htmlType;
	}

	/**
	 * The value of the {@code type} attribute of the HTML input element that edits this kind of
	 * value.
	 */
	public String htmlType() {
		return _htmlType;
	}

}
