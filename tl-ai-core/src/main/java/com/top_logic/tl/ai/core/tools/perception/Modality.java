/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.perception;

/**
 * A non-text input modality that a model may or may not be able to perceive.
 *
 * @see ModelPerception#support(Modality)
 */
public enum Modality {

	/** A raster image (PNG, JPEG, GIF, {@code WebP}, ...). */
	IMAGE("image"),

	/** A PDF document. */
	PDF("pdf");

	private final String _catalogName;

	Modality(String catalogName) {
		_catalogName = catalogName;
	}

	/**
	 * The name this modality carries in the model catalogue's
	 * {@link com.top_logic.tl.ai.core.provider.catalog.ModelInfo.ModelModalities#input() input
	 * modality} list.
	 */
	public String catalogName() {
		return _catalogName;
	}
}
