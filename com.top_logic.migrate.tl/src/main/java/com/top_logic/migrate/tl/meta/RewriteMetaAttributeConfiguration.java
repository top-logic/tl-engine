/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.meta;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * {@link EventRewriter} to adapt changes in the configuration of a {@link TLStructuredTypePart}.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class RewriteMetaAttributeConfiguration extends RewriteMetaAttribute {

	private final KnowledgeBase _srcKB;

	private final KnowledgeBase _destKB;

	protected RewriteMetaAttributeConfiguration(KnowledgeBase srcKB, KnowledgeBase destKB) {
		super();
		_srcKB = srcKB;
		_destKB = destKB;
	}

	@Override
	protected boolean rewriteMetaAttribute(ItemChange event) {
		return false;
	}

	/**
	 * Determines the attribute type of the changed {@link TLStructuredTypePart}
	 */
	protected Integer getAttributeType(ItemChange event) {
		Integer type = (Integer) event.getValues().get(ATTRIBUTE_TYPE);
		if (type == null) {
			// ItemUpdate does not contain values for not changed attributes
			type = resolveAttributeType(event);
		}
		return type;
	}

	private Integer resolveAttributeType(ItemChange event) {
		final ObjectKey originalObjectKey = event.getOriginalObject();
		KnowledgeItem originalObject = getSourceKB().resolveObjectKey(
			KBUtils.createHistoricObjectKey(originalObjectKey, originalObjectKey.getHistoryContext() - 1));

		try {
			return (Integer) originalObject.getAttributeValue(ATTRIBUTE_TYPE);
		} catch (NoSuchAttributeException ex) {
			throw new UnreachableAssertion(ex);
		}
	}

	/**
	 * Returns the given source {@link KnowledgeBase}.
	 */
	protected final KnowledgeBase getSourceKB() {
		return _srcKB;
	}

	/**
	 * Returns the given destination {@link KnowledgeBase}.
	 */
	protected final KnowledgeBase getDestKB() {
		return _destKB;
	}

}

