/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

/**
 * How an image is fitted into its bounds.
 */
public enum ImageFit implements DataEnum {

	/** The whole image is shown as large as possible, keeping its aspect ratio. */
	CONTAIN("contain"),

	/** The image covers the bounds, keeping its aspect ratio and cutting off what exceeds them. */
	COVER("cover"),

	/** The image is stretched to the bounds. */
	STRETCH("stretch");

	private final String _id;

	ImageFit(String id) {
		_id = id;
	}

	@Override
	public String id() {
		return _id;
	}

}
