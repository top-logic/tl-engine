/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts.template;

/**
 * Style of a button.
 */
public enum ButtonStyle {
	/**
	 * The button is displayed as an icon button.
	 * 
	 * @implNote This is the default option, at least for reasons of backwards-compatibility.
	 */
	ICON,

	/**
	 * Button is displayed as full-fledged button.
	 */
	BUTTON,

}
