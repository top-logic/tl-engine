/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.export;

/**
 * {@link PreloadContribution} that contributes exactly one {@link PreloadOperation}.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SinglePreloadContribution implements PreloadContribution {

	private final PreloadOperation _preload;

	/**
	 * Creates a new {@link SinglePreloadContribution} contributing the given
	 * {@link PreloadOperation}.
	 * 
	 * @param preload
	 *        The {@link PreloadOperation} to contribute.
	 */
	public SinglePreloadContribution(PreloadOperation preload) {
		_preload = preload;
	}

	@Override
	public void contribute(PreloadBuilder preloadBuilder) {
		preloadBuilder.addPreload(_preload);
	}

}

