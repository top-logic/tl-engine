/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * How an {@link AdaptiveDetailElement} presents its detail beside its selector on a wide viewport.
 *
 * <p>
 * On a narrow viewport the detail always replaces the selector, whichever value is configured.
 * </p>
 */
public enum DetailDisplay implements ExternallyNamed {

	/**
	 * The detail divides the available width with the selector, in a split whose separator can be
	 * dragged.
	 */
	SPLIT("split"),

	/**
	 * The detail overlays the selector as a panel sliding in from the right edge, leaving the
	 * selector at full width underneath.
	 */
	DRAWER("drawer");

	private final String _externalName;

	DetailDisplay(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
