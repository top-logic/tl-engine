/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.util.Collections;
import java.util.List;

import com.top_logic.base.locking.strategy.LockStrategy;
import com.top_logic.base.locking.token.Token;
import com.top_logic.base.locking.token.Token.Kind;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;

/**
 * Lock strategy for {@link BasicRuntimeModule}s.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLServiceLockStrategy implements LockStrategy<BasicRuntimeModule<? extends ManagedClass>> {

	private static final String PREFIX_SERVICE_TOKEN_NAME = "edit-runtime-module:";

	@Override
	public List<Token> createTokens(BasicRuntimeModule<? extends ManagedClass> model) {
		return Collections.singletonList(Token.newGlobalToken(Kind.EXCLUSIVE, createTokenName(model)));
	}

	private String createTokenName(BasicRuntimeModule<? extends ManagedClass> model) {
		return PREFIX_SERVICE_TOKEN_NAME + model.getImplementation();
	}

}
