/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.AbstractKnowledgeEventVisitor;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.KnowledgeEvent;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;

/**
 * Rewrites {@link KnowledgeEvent} by adapting the object identifier of the event and the
 * {@link ObjectKey}s that are contained in the values.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractObjectConverter extends AbstractValueConverter {

	/**
	 * Translates the given {@link ObjectBranchId identifier}.
	 * 
	 * @param event
	 *        The event that "contains" the given {@link ObjectBranchId}
	 * @param id
	 *        The {@link ObjectBranchId} to translate.
	 * 
	 * @return The translated {@link ObjectBranchId}.
	 */
	protected abstract ObjectBranchId translate(ItemEvent event, ObjectBranchId id);

	/**
	 * Translates the given {@link ObjectKey key}.
	 * 
	 * @param event
	 *        The event that "contains" the given {@link ObjectKey} as attribute value.
	 * @param attribute
	 *        The name of the attribute.
	 * @param value
	 *        The {@link ObjectKey} to translate.
	 * 
	 * @return The translated {@link ObjectKey}.
	 */
	protected abstract ObjectKey translate(ItemEvent event, String attribute, ObjectKey value);

	/**
	 * If some value is an {@link ObjectKey} it is transformed using
	 * {@link #translate(ItemEvent, String, ObjectKey)}.
	 * 
	 * @param event
	 *        the source event
	 * @param values
	 *        the values to transform
	 */
	@Override
	protected void translateValues(ItemChange event, Map<String, Object> values) {
		for (Entry<String, Object> entry : values.entrySet()) {
			final Object value = entry.getValue();
			if (value instanceof ObjectKey) {
				entry.setValue(translate(event, entry.getKey(), (ObjectKey) value));
			}
		}
	}

	/**
	 * Translates the {@link ItemEvent#getObjectId() id} of the element.
	 * 
	 * @see AbstractKnowledgeEventVisitor#visitItemEvent(ItemEvent, Object)
	 */
	@Override
	protected Object visitItemEvent(ItemEvent event, Void arg) {
		ObjectBranchId translatedId = translate(event, event.getObjectId());
		event.setObjectId(translatedId);
		return super.visitItemEvent(event, arg);
	}

}

