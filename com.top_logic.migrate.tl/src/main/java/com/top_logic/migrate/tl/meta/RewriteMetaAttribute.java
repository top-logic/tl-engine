/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.meta;

import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.element.meta.kbbased.KBBasedMetaAttribute;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.convert.RewritingEventVisitor;

/**
 * Super class for {@link RewritingEventVisitor} that rewrite {@link TLStructuredTypePart}.
 * 
 * @since 5.7.3.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class RewriteMetaAttribute extends RewritingEventVisitor {

	/** Name of the knowledge object type containing {@link TLStructuredTypePart} */
	protected static final String META_ATTRIBUTE_KO = KBBasedMetaAttribute.OBJECT_NAME;

	/** Name of the attribute storing the attribute type. */
	protected static final String ATTRIBUTE_TYPE = "attributeType";

	private boolean isMetaAttributeEvt(ItemEvent evt) {
		if (META_ATTRIBUTE_KO.equals(evt.getObjectType().getName())) {
			return true;
		}
		return false;
	}

	@Override
	protected Object visitItemChange(ItemChange event, Void arg) {
		Object eventHandle;
		if (isMetaAttributeEvt(event)) {
			boolean skipEvent = rewriteMetaAttribute(event);
			if (skipEvent) {
				eventHandle = SKIP_EVENT;
			} else {
				eventHandle = APPLY_EVENT;
			}
		} else {
			eventHandle = super.visitItemChange(event, arg);
		}
		return eventHandle;
	}

	/**
	 * Changes the given {@link TLStructuredTypePart} event.
	 * 
	 * @param event
	 *        the event for an {@link TLStructuredTypePart} to change.
	 * @return whether the event must be skipped.
	 */
	protected abstract boolean rewriteMetaAttribute(ItemChange event);

}

