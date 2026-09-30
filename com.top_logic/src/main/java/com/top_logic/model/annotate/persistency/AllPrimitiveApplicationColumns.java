/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.persistency;

import java.util.function.Predicate;
import java.util.stream.Stream;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.util.MetaObjectUtils;

/**
 * Determines all primitive columns (no reference columns) that are no {@link MOAttribute#isSystem()
 * system} columns.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AllPrimitiveApplicationColumns extends AllTableColumns {

	@Override
	protected Stream<? extends MOAttribute> getColumns(MetaObject table) {
		return MetaObjectUtils.getAttributes(table).stream()
			.filter(Predicate.not(MOAttribute::isSystem))
			.filter(Predicate.not(MetaObjectUtils::isReference));
	}

}
