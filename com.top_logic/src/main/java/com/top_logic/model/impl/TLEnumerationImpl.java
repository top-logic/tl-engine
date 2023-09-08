/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import java.util.List;

import com.top_logic.basic.col.ObservableList;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;

/**
 * Default implementation of {@link TLEnumeration}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLEnumerationImpl extends AbstractTLType implements TLEnumeration {
	
	private List<TLClassifier> classifiers = new ObservableList<>() {
		@Override
		protected void beforeAdd(TLClassifier element) {
			((TLClassifierImpl) element).internalSetOwner(TLEnumerationImpl.this);
		}
		
		@Override
		protected void afterRemove(TLClassifier element) {
			((TLClassifierImpl) element).internalSetOwner(null);
			element.setDefault(false);
		}
	};

	TLEnumerationImpl(TLModel model, String name) {
		super(model, name);
	}

	@Override
	public List<TLClassifier> getClassifiers() {
		return this.classifiers;
	}

}
