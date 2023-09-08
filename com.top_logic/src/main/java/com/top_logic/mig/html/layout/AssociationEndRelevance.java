/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.Map;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOReference;

/**
 * Definition whether the value of a given reference attribute must be treated as updated, when the
 * link was changed (created, updated, or deleted).
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface AssociationEndRelevance {

	/**
	 * Whether the given reference of the given type is relevant.
	 * 
	 * @return if <code>null</code> the default is meant.
	 */
	Boolean isRelevant(MetaObject type, MOReference reference);

	/**
	 * A mapping from the references of a type to the relevance.
	 * 
	 * <p>
	 * If some {@link MOReference} is not present as key, the relevance is default.
	 * </p>
	 * 
	 * @return if <code>null</code>, each reference of the type is treated default.
	 */
	Map<MOReference, Boolean> relevanceByReference(MetaObject type);

}

