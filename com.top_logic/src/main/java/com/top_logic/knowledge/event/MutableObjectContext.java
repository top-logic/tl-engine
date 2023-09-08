/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.IdentifiedObject;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * {@link ObjectContext} with mutable {@link #tId()}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MutableObjectContext implements ObjectContext {

	private ObjectKey _objectKey;

	private final KnowledgeBase _kb;

	/**
	 * Creates a {@link MutableObjectContext}.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to resolve objects.
	 */
	public MutableObjectContext(KnowledgeBase kb) {
		_kb = kb;
	}

	/**
	 * Installs the given {@link ObjectKey}.
	 * 
	 * @return A reference to this {@link ObjectContext}
	 */
	public ObjectContext getObjectContext(ObjectKey objectKey) {
		_objectKey = objectKey;
		return this;
	}

	/**
	 * Installs a current {@link ObjectKey} computed from the given {@link ObjectBranchId}.
	 * 
	 * @return A reference to this {@link ObjectContext}
	 */
	public ObjectContext getObjectContext(ObjectBranchId objectId) {
		return getObjectContext(objectId.toCurrentObjectKey());
	}

	/**
	 * TODO #2829: Delete TL 6 deprecation.
	 * 
	 * @deprecated Use {@link #tId()} instead
	 */
	@Deprecated
	@Override
	public final ObjectKey getObjectKey() {
		return tId();
	}

	@Override
	public ObjectKey tId() {
		return _objectKey;
	}

	@Override
	public MORepository getTypeRepository() {
		return _kb.getMORepository();
	}

	@Override
	public IdentifiedObject resolveObject(ObjectKey objectKey) {
		return _kb.resolveObjectKey(objectKey);
	}

	@Override
	public ObjectKey getKnownKey(ObjectKey key) {
		// just optimization.
		return key;
	}

}

