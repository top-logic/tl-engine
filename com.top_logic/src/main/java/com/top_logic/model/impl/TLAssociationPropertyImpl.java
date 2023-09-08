/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationProperty;

/**
 * {@link TLPropertyImpl} as content of {@link TLAssociation}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLAssociationPropertyImpl extends TLPropertyImpl<TLAssociation> implements TLAssociationProperty {

	TLAssociationPropertyImpl(TLModelImpl model, String name) {
		super(model, name);
	}

}

