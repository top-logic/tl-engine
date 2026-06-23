/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

/**
 * Well-known, non-character keyboard keys usable in a {@link KeyStroke}.
 *
 * <p>
 * Each constant carries the canonical client-side key name (the value the browser reports as
 * {@code KeyboardEvent.key}), which is what a {@link KeyStroke} serializes for the React keyboard
 * dispatcher. Character keys (letters, digits, punctuation) are not part of this enum; use
 * {@link KeyStroke#of(char)} for those.
 * </p>
 */
public enum Key {

	/** The Enter / Return key. */
	ENTER("Enter"),

	/** The Escape key. */
	ESCAPE("Escape"),

	/** The Space bar. */
	SPACE("Space"),

	/** The up arrow. */
	ARROW_UP("ArrowUp"),

	/** The down arrow. */
	ARROW_DOWN("ArrowDown"),

	/** The left arrow. */
	ARROW_LEFT("ArrowLeft"),

	/** The right arrow. */
	ARROW_RIGHT("ArrowRight"),

	/** The Home key. */
	HOME("Home"),

	/** The End key. */
	END("End"),

	/** The Page-up key. */
	PAGE_UP("PageUp"),

	/** The Page-down key. */
	PAGE_DOWN("PageDown"),

	/** The Delete (forward-delete) key. */
	DELETE("Delete"),

	/** The Tab key. */
	TAB("Tab"),

	/** The Backspace key. */
	BACKSPACE("Backspace"),

	/** The Insert key. */
	INSERT("Insert"),

	/** Function key F1. */
	F1("F1"),
	/** Function key F2. */
	F2("F2"),
	/** Function key F3. */
	F3("F3"),
	/** Function key F4. */
	F4("F4"),
	/** Function key F5. */
	F5("F5"),
	/** Function key F6. */
	F6("F6"),
	/** Function key F7. */
	F7("F7"),
	/** Function key F8. */
	F8("F8"),
	/** Function key F9. */
	F9("F9"),
	/** Function key F10. */
	F10("F10"),
	/** Function key F11. */
	F11("F11"),
	/** Function key F12. */
	F12("F12");

	private final String _canonicalName;

	Key(String canonicalName) {
		_canonicalName = canonicalName;
	}

	/**
	 * The canonical client-side key name (matching {@code KeyboardEvent.key}).
	 */
	public String canonicalName() {
		return _canonicalName;
	}
}
