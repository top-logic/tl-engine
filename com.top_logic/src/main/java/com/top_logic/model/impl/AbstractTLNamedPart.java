/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import com.top_logic.model.TLModel;
import com.top_logic.model.TLNamedPart;

/**
 * Default base implementation of {@link TLNamedPart}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTLNamedPart extends AbstractTLModelPart implements TLNamedPart {

	private String name;

	AbstractTLNamedPart(TLModel model, String name) {
		super(model);
		
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public void setName(String value) {
		this.name = value;
	}

}
