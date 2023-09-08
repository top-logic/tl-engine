/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers;

import java.util.Collections;
import java.util.List;

import com.top_logic.base.locking.strategy.LockStrategy;
import com.top_logic.base.locking.token.Token;
import com.top_logic.base.locking.token.Token.Kind;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.gui.config.ThemeConfig.StyleSheetRef;

/**
 * {@link LockStrategy} for {@link com.top_logic.gui.config.ThemeConfig.StyleSheetRef}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StyleSheetLockStrategy implements LockStrategy<ThemeConfig.StyleSheetRef> {

	@Override
	public List<Token> createTokens(StyleSheetRef model) {
		return Collections.singletonList(Token.newGlobalToken(Kind.EXCLUSIVE, "edit-stylesheet:" + model.getName()));
	}

}
