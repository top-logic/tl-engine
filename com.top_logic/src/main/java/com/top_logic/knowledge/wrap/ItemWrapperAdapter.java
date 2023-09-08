/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.util.TLObjectProxy;

/**
 * Adapter of a {@link TLObject} to the {@link Wrapper} interface.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ItemWrapperAdapter extends TLObjectProxy implements Wrapper {

	/**
	 * Creates an {@link ItemWrapperAdapter}.
	 */
	public ItemWrapperAdapter(TLObject impl) {
		super(impl);
	}

	@Override
	public KnowledgeObject tHandle() {
		return (KnowledgeObject) super.tHandle();
	}

	@Override
	public abstract TLStructuredType tType();

	@Override
	public int compareTo(Wrapper o) {
		throw new UnsupportedOperationException();
	}

}
