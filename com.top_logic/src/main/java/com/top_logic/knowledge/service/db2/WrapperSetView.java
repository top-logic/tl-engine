/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Set;

import com.top_logic.basic.col.ImmutableSetView;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;

/**
 * Immutable {@link Wrapper} set view of a set of {@link KnowledgeObject}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class WrapperSetView extends ImmutableSetView<KnowledgeObject, Wrapper> {
    
	/**
	 * Creates a {@link Wrapper} set view of the given set of
	 * {@link KnowledgeObject}s.
	 * 
	 * @param koSet
	 *        Set of {@link KnowledgeObject}s.
	 */
	public WrapperSetView(Set<KnowledgeObject> koSet) {
		super(koSet);
	}

	@Override
	protected boolean isCompatible(Object o) {
		return o instanceof Wrapper;
	}

	@Override
	protected KnowledgeObject getOriginalMember(Object viewMember) {
		return ((Wrapper) viewMember).tHandle();
	}

	@Override
	protected Wrapper getViewMember(Object originalMember) {
		return WrapperFactory.getWrapper((KnowledgeObject) originalMember);
	}

}