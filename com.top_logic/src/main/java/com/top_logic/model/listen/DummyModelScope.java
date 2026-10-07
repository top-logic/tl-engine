/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.listen;

import com.top_logic.basic.listener.Registration;
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
	public Registration addModelListener(ModelListener listener) {
		return Registration.NONE;
	}

	@Override
	public Registration addModelListener(TLStructuredType type, ModelListener listener) {
		return Registration.NONE;
	}

	@Override
	public Registration addModelListener(TLObject object, ModelListener listener) {
		return Registration.NONE;
	}

	@Deprecated
	@Override
	public boolean removeModelListener(ModelListener listener) {
		return false;
	}

	@Deprecated
	@Override
	public boolean removeModelListener(TLStructuredType type, ModelListener listener) {
		return false;
	}

	@Deprecated
	@Override
	public boolean removeModelListener(TLObject object, ModelListener listener) {
		return false;
	}
}
