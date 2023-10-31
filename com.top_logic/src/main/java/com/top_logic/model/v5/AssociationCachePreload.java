/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.v5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.AbstractAssociationQuery;
import com.top_logic.model.TLObject;
import com.top_logic.model.export.ModelExportException;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.model.export.PreloadOperation;

/**
 * {@link PreloadOperation} that fills association caches of the context objects matching a given
 * query.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AssociationCachePreload implements PreloadOperation {
	private final AbstractAssociationQuery<?, ?> _query;

	/**
	 * Creates a {@link AssociationCachePreload}.
	 * 
	 * @param query
	 *        See {@link #getQuery()}.
	 */
	public AssociationCachePreload(AbstractAssociationQuery<?, ?> query) {
		_query = query;
	}

	/**
	 * The query to preload.
	 */
	public AbstractAssociationQuery<?, ?> getQuery() {
		return _query;
	}

	@Override
	public void prepare(PreloadContext context, Collection<?> baseObjects) {
		{
			List<KnowledgeObject> baseKOs = new ArrayList<>(baseObjects.size());
			for (Object obj : baseObjects) {
				TLObject wrapper = (TLObject) obj;
				// It does only make sense to retrieve wrapped objects from wrappers,
				// which are alive and therefore fetchable.
				if (wrapper.tValid()) {
					baseKOs.add((KnowledgeObject) wrapper.tHandle());
				}
			}

			if (baseKOs.size() == 0) {
				return;
			}

			KnowledgeBase kb = getWrappersKnowledgeBase(baseObjects);

			if (kb != null) {
				try {
					kb.fillCaches(baseKOs, _query);
				} catch (InvalidLinkException ex) {
					throw AssociationCachePreload.errorDanglingPointer(ex);
				}
			}
		}
	}

	/**
	 * {@link KnowledgeBase}, belonging to all the wrappers of the {@link Collection}, or null, if
	 * all the {@link TLObject}s of the {@link Collection} are invalid.
	 */
	protected final KnowledgeBase getWrappersKnowledgeBase(Collection<?> baseObjects) {
		for (Object baseObject : baseObjects) {
			// Can only retrieve knowledge base from living wrappers
			TLObject wrapper = (TLObject) baseObject;
			if (wrapper.tValid()) {
				return wrapper.tKnowledgeBase();
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		return _query.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		return obj instanceof AssociationCachePreload && ((AssociationCachePreload) obj)._query.equals(_query);
	}

	static RuntimeException errorDanglingPointer(InvalidLinkException ex) {
		throw new ModelExportException("Encountered dangling pointer.", ex);
	}
}