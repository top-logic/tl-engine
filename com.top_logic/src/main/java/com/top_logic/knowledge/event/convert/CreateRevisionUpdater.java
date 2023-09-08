/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.service.BasicTypes;

/**
 * {@link RewritingEventVisitor} that sets the {@link BasicTypes#REV_CREATE_ATTRIBUTE_NAME} to the
 * revision event.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CreateRevisionUpdater extends RewritingEventVisitor {

	@Override
	public Object visitCreateObject(ObjectCreation event, Void arg) {
		Object createRev = event.getValues().get(BasicTypes.REV_CREATE_ATTRIBUTE_NAME);
		if (createRev == null && !event.getValues().containsKey(BasicTypes.REV_CREATE_ATTRIBUTE_NAME)) {
			event.setValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME, null, event.getRevision());
		}
		return super.visitCreateObject(event, arg);
	}

}

