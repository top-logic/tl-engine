/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.model.TLModuleSingleton;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLModuleSingletons;
import com.top_logic.model.TLObject;

/**
 * Transient default implementation of {@link TLModuleSingletons}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class TLModuleSingletonsImpl implements TLModuleSingletons {

	@Override
	public Collection<TLModuleSingleton> getSingletons(TLModule module) {
		return Collections.emptyList();
	}

	@Override
	public TLModuleSingleton getModuleAndName(TLObject singleton) {
		return null;
	}

	@Override
	public Collection<TLModuleSingleton> getAllSingletons() {
		return Collections.emptyList();
	}

	@Override
	public TLObject removeSingleton(TLModule module, String name) {
		return null;
	}

	@Override
	public TLObject addSingleton(TLModule module, String name, TLObject newSingleton) {
		return null;
	}

	@Override
	public TLObject getSingleton(TLModule module, String name) {
		return null;
	}

	@Override
	public String getSingletonName(TLModule module, TLObject singleton) {
		return null;
	}
}
