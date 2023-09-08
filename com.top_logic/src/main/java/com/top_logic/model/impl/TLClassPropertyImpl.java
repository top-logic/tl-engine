/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLModel;

/**
 * {@link TLPrimitiveImpl} as content of a {@link TLClass}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLClassPropertyImpl extends TLPropertyImpl<TLClass> implements TLClassProperty {

	TLClassPropertyImpl(TLModel model, String name) {
		super(model, name);
		updateDefinition();
	}

	@Override
	void internalSetOwner(TLClass value) {
		super.internalSetOwner(value);
		updateDefinition();
	}

}
