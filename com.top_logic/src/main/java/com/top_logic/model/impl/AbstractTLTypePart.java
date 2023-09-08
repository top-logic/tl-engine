/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import com.top_logic.model.TLModel;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;

/**
 * Default base implementation of {@link TLTypePart}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTLTypePart<O extends TLType> extends AbstractTLNamedPart implements TLTypePart {

	private O owner;

	/* package protected */ AbstractTLTypePart(TLModel model, String name) {
		super(model, name);
	}

	@Override
	public O getOwner() {
		return this.owner;
	}
	
	/*package protected*/ void internalSetOwner(O value) {
		this.owner = value;
	}

}
