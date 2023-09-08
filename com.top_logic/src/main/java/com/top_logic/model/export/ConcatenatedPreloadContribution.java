/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.export;

import java.util.Collection;

/**
 * {@link PreloadContribution} that holds a sequence of {@link PreloadContribution} and let them
 * contribute to the {@link PreloadBuilder}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConcatenatedPreloadContribution implements PreloadContribution {

	/**
	 * Returns a {@link PreloadContribution} that contributes all {@link PreloadOperation} that are
	 * contributed by the {@link PreloadContribution} in the given sequence.
	 */
	public static PreloadContribution toPreloadContribution(Collection<? extends PreloadContribution> contributions) {
		switch (contributions.size()) {
			case 0:
				return EmptyPreloadContribution.INSTANCE;
			case 1:
				return contributions.iterator().next();
			default:
				return new ConcatenatedPreloadContribution(contributions);
		}
	}

	private final Iterable<? extends PreloadContribution> _contributions;

	/**
	 * Creates a new {@link ConcatenatedPreloadContribution}.
	 * 
	 * @param contributions
	 *        The {@link PreloadContribution} to delegate to.
	 */
	private ConcatenatedPreloadContribution(Iterable<? extends PreloadContribution> contributions) {
		_contributions = contributions;
	}

	@Override
	public void contribute(PreloadBuilder preloadBuilder) {
		for (PreloadContribution contribution : _contributions) {
			contribution.contribute(preloadBuilder);
		}

	}

}

