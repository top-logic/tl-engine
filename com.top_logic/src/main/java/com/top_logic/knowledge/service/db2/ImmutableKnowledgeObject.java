/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.model.TLObject;

/**
 * Immutable {@link KnowledgeObject} that does allows dynamic suptypes.
 * 
 * @see StaticImmutableKnowledgeObject
 * @see DBKnowledgeObject The mutable variant.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class ImmutableKnowledgeObject extends ImmutableKnowledgeObjectBase {

	private TLObject _wrapper;

	/**
	 * Creates a new {@link ImmutableKnowledgeObject}.
	 * 
	 * @param kb
	 *        see {@link KnowledgeItem#getKnowledgeBase()}
	 * @param type
	 *        see {@link KnowledgeItem#tTable()}
	 */
	public ImmutableKnowledgeObject(DBKnowledgeBase kb, MOKnowledgeItem type) {
		super(kb, type);
	}

	@Override
	public void onLoad(PooledConnection readConnection) {
		super.onLoad(readConnection);
		assert _wrapper == null : "Already initialized.";
		_wrapper = KnowledgeItemImpl.createWrapper(this);
	}

	@Override
	public TLObject getWrapper() {
		return _wrapper;
	}

	@Override
	public String toString() {
		if (_wrapper == null) {
			return super.toString();
		} else {
			return getClass().getSimpleName() + "[wrapper: " + _wrapper.toString() + "]";
		}
	}

}


