/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;

/**
 * Default implementation of {@link TLClassifier}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLClassifierImpl extends AbstractTLTypePart<TLEnumeration> implements TLClassifier {

	/**
	 * @see TLClassifier#isDefault()
	 */
	private boolean _default;

	TLClassifierImpl(TLModelImpl model, String name) {
		super(model, name);
	}

	@Override
	public boolean isDefault() {
		return _default;
	}

	@Override
	public void setDefault(boolean b) {
		_default = b;
	}

}
