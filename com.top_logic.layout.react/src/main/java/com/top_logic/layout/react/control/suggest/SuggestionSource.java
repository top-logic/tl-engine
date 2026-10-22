/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.suggest;

import java.util.List;

/**
 * Computes the {@link Suggestion}s offered for a text being typed.
 *
 * <p>
 * The source does all filtering: the suggestions it returns are offered as they are, in the order
 * given. Computing suggestions must not change the edited value.
 * </p>
 */
@FunctionalInterface
public interface SuggestionSource {

	/**
	 * The suggestions for the given text with the caret at the given position.
	 *
	 * @param text
	 *        The text typed so far, never {@code null}, possibly empty.
	 * @param caret
	 *        The caret position, a character offset in {@code text}.
	 * @return The suggestions to offer, never {@code null}. A suggestion whose range does not
	 *         {@link Suggestion#fits(String) fit} the text is not offered.
	 */
	List<Suggestion> suggest(String text, int caret);

}
