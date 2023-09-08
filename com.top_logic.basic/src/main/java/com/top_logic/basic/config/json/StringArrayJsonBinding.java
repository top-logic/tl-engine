/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.json;

/**
 * {@link AbstractJsonArrayBinding} for {@link String} arrays.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StringArrayJsonBinding extends AbstractJsonArrayBinding<String> {

	@Override
	protected Class<String> getComponentType() {
		return String.class;
	}
}

