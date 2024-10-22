/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.execution;

import com.top_logic.bpe.execution.model.Token;
import com.top_logic.element.layout.meta.ConfiguredAttributedTooltipProvider;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class TokenResourceProvider extends ConfiguredAttributedTooltipProvider {

	@Override
	public String getLabel(Object anObject) {
		if (anObject == null) {
			return null;
		}

		Token token = (Token) anObject;
		return token.getName();
	}

	@Override
	public ThemeImage getImage(Object object, Flavor flavor) {
		if (object == null) {
			return null;
		}
		Token token = (Token) object;
		return token.getIcon();
	}
}
