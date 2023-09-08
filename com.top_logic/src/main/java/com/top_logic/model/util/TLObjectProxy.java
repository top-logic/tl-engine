/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.model.AbstractTLObject;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;

/**
 * Proxy implementation for the {@link TLObject} interface.
 * 
 * @see PersistentObject
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLObjectProxy extends AbstractTLObject {

	private final TLObject _impl;

	/**
	 * Creates a {@link TLObjectProxy}.
	 */
	public TLObjectProxy(TLObject impl) {
		_impl = impl;
	}

	/**
	 * Access to the original implementation.
	 */
	protected TLObject tImpl() {
		return _impl;
	}

	@Override
	public KnowledgeItem tHandle() {
		return _impl.tHandle();
	}

	@Override
	public ObjectKey tId() {
		return _impl.tId();
	}

	@Override
	public boolean tTransient() {
		return _impl.tTransient();
	}

	@Override
	public TLStructuredType tType() {
		return _impl.tType();
	}

}
