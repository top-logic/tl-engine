/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.theme;

import java.util.Collections;
import java.util.List;

import com.top_logic.base.locking.strategy.LockStrategy;
import com.top_logic.base.locking.token.Token;
import com.top_logic.base.locking.token.Token.Kind;
import com.top_logic.gui.config.ThemeConfig;

/**
 * {@link LockStrategy} for {@link ThemeConfig}s.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ThemeConfigLockStrategy implements LockStrategy<ThemeConfig> {

	@Override
	public List<Token> createTokens(ThemeConfig model) {
		return Collections.singletonList(Token.newGlobalToken(Kind.EXCLUSIVE, "edit-theme-config:" + model.getId()));
	}

}
