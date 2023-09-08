/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter;

import com.top_logic.layout.LabelProvider;
import com.top_logic.model.TLNamedPart;

/**
 * Label provider for technical names of {@link TLNamedPart}s.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TechnicalNamesLabelProvider implements LabelProvider {

	/**
	 * Singleton instance.
	 */
	public static final TechnicalNamesLabelProvider INSTANCE = new TechnicalNamesLabelProvider();

	private TechnicalNamesLabelProvider() {
		// Singleton
	}

	@Override
	public String getLabel(Object object) {
		if (object instanceof TLNamedPart) {
			return ((TLNamedPart) object).getName();
		}

		return null;
	}

}
