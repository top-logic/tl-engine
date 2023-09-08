/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.export;

import java.util.Collection;

/**
 * {@link PreloadContribution} that contributes a {@link Collection} of pre-computed
 * {@link PreloadOperation}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultPreloadContribution implements PreloadContribution {
	private final Collection<PreloadOperation> _operations;

	/**
	 * Creates a {@link DefaultPreloadContribution}.
	 * 
	 * @param operations
	 *        The {@link PreloadOperation}s to contribute.
	 */
	public DefaultPreloadContribution(Collection<PreloadOperation> operations) {
		_operations = operations;
	}

	@Override
	public void contribute(PreloadBuilder preloadBuilder) {
		for (PreloadOperation operation : _operations) {
			preloadBuilder.addPreload(operation);
		}
	}
}