/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.lang.ref.Reference;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * {@link ValidityChain} that holds {@link Reference} to a {@link KnowledgeItem} in the
 * {@link KnowledgeBase} cache.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class CacheValidity extends ValidityChainImpl<CacheValidity> {

	private final DBKnowledgeBase.IDReference _ref;

	public CacheValidity(DBKnowledgeBase.IDReference ref, long minValidity, long maxValidity) {
		super(minValidity, maxValidity);
		_ref = ref;
	}

	DBKnowledgeBase.IDReference getReference() {
		return _ref;
	}

	/**
	 * Removes the {@link CacheValidity}s (starting with this {@link CacheValidity}) which has the
	 * given reference. Moreover all older entries are removed.
	 * 
	 * @param reference
	 *        The reference to remove entry for.
	 * 
	 * @return The most recent {@link CacheValidity} in the {@link #formerValidity() chain}
	 *         (including this object) which has a value. May be <code>null</code> in case no
	 *         {@link CacheValidity} is longer valid.
	 */
	CacheValidity removeEntryFor(DBKnowledgeBase.IDReference reference) {
		if (_ref == reference) {
			return null;
		}
		CacheValidity validity = this;
		while (true) {
			CacheValidity formerValidity = validity.formerValidity();
			if (formerValidity == null) {
				/* There is no longer an entry with the given reference. That occurs when a "newer"
				 * reference is removed before an "older" one: Removing the "newer" one also removes
				 * the cache entry for older references. */
				break;
			}
			if (formerValidity._ref == reference) {
				validity.setFormerValidity(null);
				break;
			}
			validity = formerValidity;
		}
		return this;
	}

	@Override
	public String toString() {
		StringBuilder toString = new StringBuilder();
		toString.append("CacheValidity [ref=");
		toString.append(_ref);
		toString.append("]");
		appendValidityRange(toString, this);
		return toString.toString();
	}

}

