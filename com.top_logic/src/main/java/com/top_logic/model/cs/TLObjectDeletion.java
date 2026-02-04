/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.cs;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.TLObject;

/**
 * Representation of the deletion of a {@link TLObject}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class TLObjectDeletion extends TLObjectChange {

	/**
	 * Creates a new {@link TLObjectDeletion}.
	 */
	public TLObjectDeletion(TLObject object) {
		super(object);
	}

	@Override
	public ChangeType changeType() {
		return ChangeType.DELETE;
	}

	/**
	 * The deleted {@link TLObject}.
	 * 
	 * <p>
	 * <b>Note:</b> The returned object is a deleted object. Access to properties will fail.
	 * Accessing properties only works
	 * {@link KnowledgeBase#withoutModifications(com.top_logic.basic.util.ComputationEx2) without
	 * modifications}.
	 * </p>
	 * 
	 * @see com.top_logic.model.cs.TLObjectChange#object()
	 */
	@Override
	public TLObject object() {
		return super.object();
	}

}

