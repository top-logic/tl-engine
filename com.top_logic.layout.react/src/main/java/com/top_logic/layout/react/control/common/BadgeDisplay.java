/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.layout.react.state.Badge;

/**
 * A badge as an element shows it: a count or short text, or a dot, in a color role.
 *
 * <p>
 * A control holding a badge sends it to the client as {@link #toState()}, the state described by
 * the message {@link Badge}; where the element shows no badge, the control holds {@code null} and
 * sends nothing.
 * </p>
 *
 * @param content
 *        What the badge shows, {@code null} for a {@link #dot() dot}. See {@link Badge#CONTENT__PROP}.
 * @param dot
 *        Whether the badge is a dot marking the element without counting. See
 *        {@link Badge#DOT__PROP}.
 * @param role
 *        The color role. See {@link Badge#ROLE__PROP}.
 * @param label
 *        The accessible name where the content does not say it itself, {@code null} for none. See
 *        {@link Badge#LABEL__PROP}.
 */
public record BadgeDisplay(String content, boolean dot, BadgeRole role, String label) {

	/**
	 * The state of this badge as the client receives it, see {@link Badge}.
	 */
	public Map<String, Object> toState() {
		Map<String, Object> result = new LinkedHashMap<>();
		if (content != null) {
			result.put(Badge.CONTENT__PROP, content);
		}
		if (dot) {
			result.put(Badge.DOT__PROP, Boolean.TRUE);
		}
		if (role != null && role != BadgeRole.BRAND) {
			result.put(Badge.ROLE__PROP, role.getExternalName());
		}
		if (label != null) {
			result.put(Badge.LABEL__PROP, label);
		}
		return result;
	}

}
