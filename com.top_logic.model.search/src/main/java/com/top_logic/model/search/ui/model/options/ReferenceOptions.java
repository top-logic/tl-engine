/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.options;

import static com.top_logic.basic.CollectionUtil.*;
import static java.util.Collections.*;

import java.util.List;

import com.top_logic.basic.func.Function2;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.search.ui.model.IncomingReferenceFilter;

/**
 * Option provider for {@link IncomingReferenceFilter#getReference()}.
 * <p>
 * Returns the {@link TLReference}s that store a value of the given {@link TLType}.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ReferenceOptions extends Function2<List<TLReference>, TLType, String> {

	/** The {@link ReferenceOptions} instance. */
	public static final ReferenceOptions INSTANCE = new ReferenceOptions();

	@Override
	public List<TLReference> apply(TLType contextType, String configName) {
		if (containsNull(contextType, configName)) {
			return emptyList();
		}
		if (!(contextType instanceof TLStructuredType)) {
			return emptyList();
		}
		return SearchTypeUtil.getReferences((TLStructuredType) contextType, configName);
	}

}
