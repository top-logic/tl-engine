/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.skip;

import java.util.Map;
import java.util.Set;

import com.top_logic.dob.meta.MOReference;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;

/**
 * Simple Implementation of {@link SkippedReferenceHandle}.
 * 
 * <p>
 * It which skips an creation if the attribute is mandatory. Otherwise the value is set to
 * <code>null</code>. If an update occurred, the update of the reference is removed.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultSkippedReferenceHandle implements SkippedReferenceHandle {

	@Override
	public boolean handleSkippedReference(ObjectCreation event, MOReference attribute,
			Set<ObjectBranchId> skippedObjects) {
		if (attribute.isMandatory()) {
			// attribute is mandatory and object is newly created, so skip creation.
			return true;
		} else {
			event.getValues().remove(attribute.getName());
		}
		return false;
	}

	@Override
	public boolean handleSkippedReference(ItemUpdate event, MOReference attribute, Set<ObjectBranchId> skippedObjects) {
		if (attribute.isMandatory()) {
			Map<String, Object> oldValues = event.getOldValues();
			if (oldValues != null) {
				oldValues.remove(attribute.getName());
			}
			event.getValues().remove(attribute.getName());
		}
		return false;
	}

}

