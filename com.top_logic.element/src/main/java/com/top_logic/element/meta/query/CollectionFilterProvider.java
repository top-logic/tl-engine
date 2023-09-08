/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.Collection;

import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLObject;

/**
 * Factory for {@link CollectionFilter}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CollectionFilterProvider {

	CollectionFilter getFilter(TLStructuredTypePart anAttribute, Collection<? extends TLObject> value, boolean doNegate,
			boolean searchForEmptyValues, String anAccessPath);

}
