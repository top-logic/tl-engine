/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Set;

import com.top_logic.basic.col.ImmutableSetView;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * Immutable {@link KnowledgeObject} set view of a set of object keys.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KOSetView extends ImmutableSetView<ObjectKey, KnowledgeObject> {

	/**
	 * The {@link KnowledgeBase} to retrieve corresponding objects from.
	 */
	private final DBKnowledgeBase kb;

	/**
	 * Creates a new {@link KnowledgeObject} set view for the given set of
	 * object keys.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to retrieve objects from.
	 * @param itemKeys
	 *        The set of {@link KnowledgeObject#tId() object keys}
	 */
	public KOSetView(DBKnowledgeBase kb, Set<? extends ObjectKey> itemKeys) {
		super(itemKeys);
		this.kb = kb;
	}

	@Override
	protected ObjectKey getOriginalMember(Object o) {
		return ((KnowledgeItem) o).tId();
	}

	@Override
	protected KnowledgeObject getViewMember(Object originalMember) {
		return (KnowledgeObject) kb.resolveObjectKey((ObjectKey) originalMember);
	}

	@Override
	protected boolean isCompatible(Object o) {
		return o instanceof KnowledgeItem;
	}

}
