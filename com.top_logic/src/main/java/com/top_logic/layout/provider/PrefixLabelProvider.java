/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.layout.LabelProvider;

/**
 * Transforming {@link LabelProvider} that shortens labels provided by a base
 * {@link LabelProvider} to a given maximum length.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class PrefixLabelProvider implements LabelProvider {

	private static final char ELLIPSIS = '\u2026';

	private final LabelProvider labels;
	private final int prefixLength;

	public PrefixLabelProvider(LabelProvider labels, int prefixLength) {
		this.labels       = labels;
		this.prefixLength = prefixLength;
	}

	@Override
	public String getLabel(Object object) {
		String fullLabel = labels.getLabel(object);
		if (fullLabel == null) {
			return null;
		}
		if (fullLabel.length() > prefixLength) {
			return fullLabel.substring(0, prefixLength - 1) + ELLIPSIS;
		} else {
			return fullLabel;
		}
	}
}