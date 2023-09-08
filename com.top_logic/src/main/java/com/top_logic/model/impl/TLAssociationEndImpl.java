/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLType;

/**
 * Default implementation of {@link TLAssociationEnd}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLAssociationEndImpl extends AbstractStructuredTypePart<TLAssociation> implements TLAssociationEnd {

	/**
	 * @see #getReference()
	 */
	private TLReference reference;

	private boolean aggregate;
	private boolean composite;
	private boolean navigate = true;

	private HistoryType _historyType = HistoryType.CURRENT;


	/*package protected*/ TLAssociationEndImpl(TLModelImpl model, String name) {
		super(model, name);
	}

	@Override
	public void setType(TLType value) {
		internalSetType(value);
	}
	
	@Override
	public TLReference getReference() {
		return this.reference;
	}
	
	/*package protected*/ void internalSetReference(TLReference reference) {
		this.reference = reference;
	}
	
	@Override
	public boolean isAggregate() {
		return this.aggregate;
	}
	
	@Override
	public void setAggregate(boolean value) {
		this.aggregate = value;
	}

	@Override
	public boolean isComposite() {
		return this.composite;
	}
	
	@Override
	public void setComposite(boolean composite) {
		this.composite = composite;
	}

	@Override
	public boolean canNavigate() {
		return navigate;
	}
	
	@Override
	public void setNavigate(boolean value) {
		this.navigate = value;
	}

	@Override
	public HistoryType getHistoryType() {
		return _historyType;
	}

	@Override
	public void setHistoryType(HistoryType type) {
		_historyType = type;
	}

}
