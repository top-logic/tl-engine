/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.window;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * What a click in {@link ElementPicker pick mode} is resolved to.
 *
 * <p>
 * The kind travels to the browser in the {@link com.top_logic.layout.react.protocol.PickEvent} and
 * decides both what the client looks for under the pointer and which member of
 * {@link PickResult} the pick delivers.
 * </p>
 */
public enum PickKind implements ExternallyNamed {

	/**
	 * The source file of the view that rendered the clicked element.
	 *
	 * @see PickResult.ViewPicked
	 */
	VIEW("view"),

	/**
	 * The innermost mounted control containing the clicked element.
	 *
	 * @see PickResult.ControlPicked
	 */
	CONTROL("control"),

	;

	private final String _externalName;

	private PickKind(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

	/**
	 * The {@link PickKind} with the given {@link #getExternalName() external name}.
	 *
	 * @param externalName
	 *        The name as it appears on the wire, may be <code>null</code>.
	 * @return The matching kind, or <code>null</code> if the name matches none.
	 */
	public static PickKind byExternalName(String externalName) {
		for (PickKind kind : values()) {
			if (kind.getExternalName().equals(externalName)) {
				return kind;
			}
		}
		return null;
	}
}
