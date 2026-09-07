/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The element types offered when adding a child to a {@link ConfigChildren}, prepared for display.
 *
 * <p>
 * A structural property accepts either a single fixed element type or one of many. Callers use
 * {@link #of(ConfigChildren)} to obtain the choices in display order and, when there is more than
 * {@link #isUnique() one}, let the user pick one before passing the {@link Choice#option() option} to
 * {@link ConfigChildren#newElement(Object)}.
 * </p>
 */
public final class ConfigTypeChoice {

	/**
	 * One selectable element type.
	 *
	 * @param label
	 *        The human-readable type name.
	 * @param option
	 *        The option value to pass to {@link ConfigChildren#newElement(Object)}.
	 */
	public record Choice(String label, Object option) {
		// record
	}

	private final List<Choice> _choices;

	private ConfigTypeChoice(List<Choice> choices) {
		_choices = choices;
	}

	/**
	 * The element types the given property accepts, ordered by label.
	 */
	public static ConfigTypeChoice of(ConfigChildren children) {
		List<Choice> choices = new ArrayList<>();
		for (Object option : children.allowedTypes().options()) {
			choices.add(new Choice(PolymorphicOptions.labelFor(option), option));
		}
		choices.sort(Comparator.comparing(Choice::label));
		return new ConfigTypeChoice(choices);
	}

	/**
	 * The available choices, ordered by {@link Choice#label() label}.
	 */
	public List<Choice> choices() {
		return _choices;
	}

	/**
	 * Whether there is nothing to choose, because the property accepts at most one element type.
	 *
	 * <p>
	 * The caller adds the {@link #single() single} type directly instead of asking the user.
	 * </p>
	 */
	public boolean isUnique() {
		return _choices.size() <= 1;
	}

	/**
	 * The only available option, or {@code null} if the property declares no options at all.
	 *
	 * @see #isUnique()
	 */
	public Object single() {
		return _choices.isEmpty() ? null : _choices.get(0).option();
	}
}
