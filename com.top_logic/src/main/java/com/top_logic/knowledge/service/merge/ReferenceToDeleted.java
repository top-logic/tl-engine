/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.merge;

import com.top_logic.dob.meta.MOReference;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.model.TLObject;

/**
 * This session tries to create a reference to an item that is concurrently deleted.
 * 
 * <p>
 * The referrer item points via {@link #getReference()} to an item that is deleted in
 * {@link #getConcurrentChange()}. The referrer is either a new item, or an already existing item,
 * and the reference to changed to that object.
 * </p>
 * 
 * @see DeletingReference
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ReferenceToDeleted extends MergeConflictDescription {

	private final MOReference _reference;

	/** 
	 * Creates a new {@link ReferenceToDeleted}.
	 */
	public ReferenceToDeleted(UpdateEvent concurrentEvent, KnowledgeItem referrer, MOReference reference) {
		super(concurrentEvent, referrer);
		_reference = reference;
	}

	/**
	 * The reference which points to the deleted item.
	 */
	public MOReference getReference() {
		return _reference;
	}

	@Override
	void appendMessage(StringBuilder builder) {
		builder.append("The object '");
		TLObject wrapper = getConflictingItem().getWrapper();
		builder.append(wrapper);
		builder.append("' references the deleted object with identifier '");
		builder.append(getConflictingItem().getReferencedKey(getReference()));
		builder.append("' in attribute '");
		builder.append(getReference().getName());
		builder.append("'.");
	}

}

