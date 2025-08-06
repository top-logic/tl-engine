/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.copy;

import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.model.TLObject;

class NestedOperation extends CopyOperationImpl {

	private CopyOperation _outer;

	/**
	 * Creates a {@link NestedOperation}.
	 */
	public NestedOperation(CopyOperation outer) {
		_outer = outer;
	}

	@Override
	public TLObject resolveCopy(TLObject orig) {
		return _outer.resolveCopy(orig);
	}

	@Override
	public void enterCopy(TLObject orig, TLObject copy) {
		_outer.enterCopy(orig, copy);
	}

	@Override
	protected Set<Entry<TLObject, TLObject>> localCopies() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void finish() {
		// Is done by the outermost copy operation.
	}

}