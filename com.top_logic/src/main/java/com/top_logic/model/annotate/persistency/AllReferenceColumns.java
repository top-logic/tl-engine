/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.persistency;

import java.util.stream.Stream;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.service.db2.PersistentObject;

/**
 * Determines all {@link MOReference}s in the given table.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AllReferenceColumns extends AllTableColumns {

	@Override
	protected Stream<? extends MOAttribute> getColumns(MetaObject table) {
		return MetaObjectUtils.getReferences(table).stream().filter(AllReferenceColumns::isExcluded);
	}

	private static boolean isExcluded(MOAttribute attribute) {
		return PersistentObject.TYPE_REF.equals(attribute.getName());
	}

}
