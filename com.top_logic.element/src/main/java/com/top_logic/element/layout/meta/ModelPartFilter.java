/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import com.top_logic.basic.col.Filter;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLModelPart;

/**
 * {@link Filter} for {@link TLModelPart} based on a single {@link ModelKind}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ModelPartFilter implements Filter<TLModelPart> {

	private final ModelKind _kind;

	/**
	 * Creates a {@link ModelPartFilter}.
	 * 
	 * @param kind
	 *        See {@link #getKind()}
	 */
	public ModelPartFilter(ModelKind kind) {
		_kind = kind;
	}

	/**
	 * The {@link ModelKind} of accepted instances.
	 */
	public ModelKind getKind() {
		return _kind;
	}

	@Override
	public boolean accept(TLModelPart anObject) {
		return anObject.getModelKind() == _kind;
	}

}
