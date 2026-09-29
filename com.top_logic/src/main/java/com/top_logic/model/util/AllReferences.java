/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link AllAttributes} delivering only the {@link TLReference}s of all {@link TLClass}es.
 *
 * @see Options#fun()
 * @see AllReferenceAttributes Selecting a reference of an already selected type.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AllReferences extends AllAttributes {

	@Override
	protected boolean acceptPart(TLStructuredTypePart part) {
		return TLModelUtil.isReference(part);
	}

}
