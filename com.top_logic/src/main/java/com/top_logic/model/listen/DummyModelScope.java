/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.listen;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;

/**
 * {@link ModelScope} that actually does not report any changes.
 * 
 * <p>
 * Useful only for one-time rendering in export functions.
 * </p>
 */
public final class DummyModelScope implements ModelScope {
	@Override
	public boolean addModelListener(ModelListener listener) {
		return false;
	}

	@Override
	public boolean addModelListener(TLStructuredType type, ModelListener listener) {
		return false;
	}

	@Override
	public boolean addModelListener(TLObject object, ModelListener listener) {
		return false;
	}

	@Override
	public boolean removeModelListener(ModelListener listener) {
		return false;
	}

	@Override
	public boolean removeModelListener(TLStructuredType type, ModelListener listener) {
		return false;
	}

	@Override
	public boolean removeModelListener(TLObject object, ModelListener listener) {
		return false;
	}
}
