/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.converters;

import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.Revision;

/**
 * {@link AbstractReferenceConversion} building an {@link ObjectKey} with the type of the changed
 * object as {@link ObjectKey#getObjectType() type}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SelfTypeReferenceConversion extends AbstractReferenceConversion {

	@Override
	public ObjectKey convertReference(ItemChange event, String dumpValue) {
		TLID dumpId = StringID.valueOf(dumpValue);

		ObjectBranchId selfKey = event.getObjectId();
		return new DefaultObjectKey(selfKey.getBranchId(), Revision.CURRENT_REV, selfKey.getObjectType(), dumpId);
	}

}
