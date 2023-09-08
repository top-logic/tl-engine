/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.model.TLObject;

/**
 * Base class for {@link KnowledgeItem}s that support dynamic subtyping.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
abstract class WrappedKnowledgeItem extends DBKnowledgeObjectBase {

	private TLObject wrapper;

	WrappedKnowledgeItem(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
		super(kb, staticType);
	}

	@Override
	protected void initWrapper() {
		if (wrapper != null) {
			// Already initialized.
			return;
		}
		wrapper = KnowledgeItemImpl.createWrapper(this);
		if (wrapper instanceof PersistentObject) {
			((PersistentObject) wrapper).handleLoad();
		}
	}

	@Override
	protected void invalidateWrapper() {
		if (wrapper instanceof PersistentObject) {
			((PersistentObject) wrapper).handleDelete();
		}
	}

	@Override
	public TLObject getWrapper() {
		if (wrapper == null) {
			/* Wrapper not yet initialized. */
			initWrapper();
		}
		return wrapper;
	}

	@Override
	public String toString() {
		if (wrapper == null) {
			return super.toString();
		} else {
			return getClass().getSimpleName() + "[wrapper: " + wrapper.toString() + "]";
		}
	}

}
