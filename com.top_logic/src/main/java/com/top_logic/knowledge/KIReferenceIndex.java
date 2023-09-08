/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge;

import java.util.Collections;
import java.util.List;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReferenceIndex;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.BasicTypes;

/**
 * {@link MOReferenceIndex} based on a {@link KIReference}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class KIReferenceIndex extends MOReferenceIndex<KIReference> {

	/**
	 * Creates a new {@link KIReferenceIndex}.
	 * 
	 * @param reference
	 *        The {@link MOReference} to create index for.
	 */
	public KIReferenceIndex(KIReference reference) {
		super(reference);
	}

	@Override
	protected void addBranchColumn(MOStructure owner, List<DBAttribute> indexAttributes) {
		addDBMapping(owner, BasicTypes.BRANCH_ATTRIBUTE_NAME, indexAttributes);
	}

	private void addDBMapping(MOStructure owner, String attributeName, List<DBAttribute> indexAttributes) {
		MOAttribute attribute = owner.getAttributeOrNull(attributeName);
		if (attribute != null) {
			Collections.addAll(indexAttributes, attribute.getDbMapping());
		}
	}

	@Override
	protected void addRevisionColumn(MOStructure owner, List<DBAttribute> indexAttributes) {
		addDBMapping(owner, BasicTypes.REV_MAX_ATTRIBUTE_NAME, indexAttributes);
	}

}

