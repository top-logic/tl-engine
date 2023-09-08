/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.merge;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.model.TLObject;

/**
 * This session tries to delete an object which is the new target of a reference.
 * 
 * <p>
 * Note: This is only a problem, because cleaning of references to this item occurs during deletion
 * of object. As the reference was not set when the item was deleted, the reference has not been
 * cleared.
 * </p>
 * 
 * @see ReferenceToDeleted
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeletingReference extends MergeConflictDescription {

	private final ObjectKey _referrerKey;

	private final MOReference _reference;

	private final KnowledgeItem _cachedReferrer;

	/**
	 * Creates a new {@link DeletingReference}.
	 */
	public DeletingReference(UpdateEvent concurrentEvent, KnowledgeItem deletedItem, KnowledgeItem cachedReferrer,
			ObjectKey referrerKey, MOReference reference) {
		super(concurrentEvent, deletedItem);
		_cachedReferrer = cachedReferrer;
		_referrerKey = referrerKey;
		_reference = reference;
	}

	/**
	 * The referrer which newly points via {@link #getReference()} to the item that is deleted by
	 * this session ({@link #getConflictingItem()}).
	 * 
	 * @return This method may return <code>null</code> in case the object was not loaded before by
	 *         the {@link KnowledgeBase}.
	 * 
	 * @see #getReferrerKey()
	 */
	public KnowledgeItem getCachedReferrer() {
		return _cachedReferrer;
	}

	/**
	 * The identifier of the referrer which newly points via {@link #getReference()} to the item
	 * that is deleted by this session ({@link #getConflictingItem()}).
	 * 
	 * @see #getCachedReferrer()
	 * @see #getReference()
	 */
	public ObjectKey getReferrerKey() {
		return _referrerKey;
	}

	/**
	 * The item identified by the {@link #getReferrerKey() referrer key} points to the
	 * {@link #getConflictingItem() item} to delete via this reference.
	 * 
	 * @see #getReferrerKey()
	 */
	public MOReference getReference() {
		return _reference;
	}

	@Override
	void appendMessage(StringBuilder builder) {
		builder.append("The object '");
		TLObject conflict = getConflictingItem().getWrapper();
		builder.append(conflict);
		builder.append("' can not be deleted. It is refered by ");
		if (getCachedReferrer() != null) {
			TLObject cached = getCachedReferrer().getWrapper();
			builder.append(cached);
		} else {
			builder.append("the object with identifier ");
			builder.append(getReferrerKey());
		}
		builder.append(" in attribute '");
		builder.append(getReference().getName());
		builder.append("'.");
	}

}

