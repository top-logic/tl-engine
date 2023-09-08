/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLInstanceAccess;
import com.top_logic.model.TLObject;
import com.top_logic.model.internal.AbstractPersistentQuery;

/**
 * Access to persistent instances through {@link TLInstanceAccess}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PersistentInstanceAccess extends AbstractPersistentQuery implements TLInstanceAccess {

	@Override
	public CloseableIterator<TLObject> getAllDirectInstances(TLClass concreteClass) {
		return MetaElementUtil.iterateDirectInstances(concreteClass, TLObject.class);
	}

	@Override
	public CloseableIterator<TLObject> getAllInstances(TLClass classType) {
		return AttributeOperations.allInstancesIterator(classType, TLObject.class);
	}

}
