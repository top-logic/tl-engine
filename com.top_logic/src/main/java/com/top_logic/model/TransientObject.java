/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import java.util.Collections;
import java.util.Set;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;

/**
 * {@link TLObject} without persistency.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TransientObject extends AbstractTLObject {

	@Override
	public KnowledgeItem tHandle() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean tValid() {
		return true;
	}

	@Override
	public Revision tRevision() {
		return Revision.CURRENT;
	}

	@Override
	public TLStructuredType tType() {
		return null;
	}

	@Override
	public Object tValue(TLStructuredTypePart part) {
		return null;
	}

	@Override
	public Set<? extends TLObject> tReferers(TLReference ref) {
		return Collections.emptySet();
	}

	@Override
	public void tUpdate(TLStructuredTypePart part, Object value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated Use {@link #tId()} instead
	 */
	@Override
	public ObjectKey getObjectKey() {
		return tId();
	}

	@Override
	public ObjectKey tId() {
		return null;
	}

	@Override
	public long tHistoryContext() {
		return Revision.CURRENT_REV;
	}

	@Override
	public KnowledgeBase tKnowledgeBase() {
		return null;
	}
}
