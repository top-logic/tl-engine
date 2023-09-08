/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob;

import com.top_logic.dob.meta.AbstractMetaObject;
import com.top_logic.dob.meta.TypeContext;

/**
 * Implementation of internal type singletons.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class MOInternal extends AbstractMetaObject {

	private final Kind kind;

	public MOInternal(String name, Kind kind) {
		super(name);
		this.kind = kind;
	}

	@Override
	public Kind getKind() {
		return kind;
	}

	@Override
	public MetaObject copy() {
		throw new UnsupportedOperationException("Internal types are singletons.");
	}
	
	@Override
	public MetaObject resolve(TypeContext context) throws DataObjectException {
		return this;
	}


}
