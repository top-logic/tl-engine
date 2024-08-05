/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import com.top_logic.knowledge.wrap.ValueProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.factory.TLFactory;

/**
 * {@link TLFactory} creating {@link TransientTLObjectImpl}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TransientObjectFactory implements TLFactory {

	/**
	 * Singleton {@link TransientObjectFactory} instance.
	 */
	public static final TransientObjectFactory INSTANCE = new TransientObjectFactory();

	private TransientObjectFactory() {
		// Singleton constructor.
	}

	@Override
	public TLObject createObject(TLClass type, TLObject context, ValueProvider initialValues) {
		TLFactory.failIfAbstract(type);
		TransientTLObjectImpl result = new TransientTLObjectImpl(type, context);
		TLFactory.setupDefaultValues(context, result, type);
		return result;
	}

}
