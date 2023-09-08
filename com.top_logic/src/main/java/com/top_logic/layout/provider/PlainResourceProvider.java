/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.ResourceProvider;

/**
 * {@link ResourceProvider} producing only labels using {@link Object#toString()}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PlainResourceProvider extends AbstractResourceProvider {

	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return null;
		}
		return object.toString();
	}

}
