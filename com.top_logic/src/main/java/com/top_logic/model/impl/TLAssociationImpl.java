/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.col.ObservableSet;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLScope;

/**
 * Default implementation of {@link TLAssociation}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLAssociationImpl extends AbstractTLStructuredType<TLAssociationPart> implements TLAssociation {

	private final Set<TLAssociation> subsets = new ObservableSet<>() {
		
		@Override
		protected void beforeAdd(TLAssociation key) {
			TLAssociationImpl union = (TLAssociationImpl) key;
			union.unionParts.add(TLAssociationImpl.this);
		}
		
		@Override
		protected void afterRemove(TLAssociation key) {
			TLAssociationImpl union = (TLAssociationImpl) key;
			union.unionParts.remove(TLAssociationImpl.this);
		}
		
	};
	
	// Computed properties.
	
	/*package protected*/ final Set<TLAssociation> unionParts = new HashSet<>();
	
	private final Set<TLAssociation> union = Collections.unmodifiableSet(unionParts);

	/*package protected*/ TLAssociationImpl(TLModelImpl model, String name) {
		super(model, name);
	}
	
	@Override
	void internalSetScope(TLScope value) {
		// Make sure, that the scope is actually a module.
		TLModule module = (TLModule) value;
		
		super.internalSetScope(module);
	}
	
	@Override
	public Set<TLAssociation> getUnions() {
		return this.subsets;
	}
	
	@Override
	public Set<TLAssociation> getSubsets() {
		return this.union;
	}
	
	@Override
	public List<TLAssociationPart> getAssociationParts() {
		return getLocalParts();
	}

}
